package mx.edu.utez.sij.nucleo.acceso;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Inicio de sesión y cambio de contraseña de TODO el sistema (INTEGRADORES). docs/ARQUITECTURA.md 7.1 y 10.
 *
 * - La contraseña, los intentos fallidos y el bloqueo están en la tabla Login (un renglón por correo).
 * - El nombre, el rol y si está activo están en la tabla user_* del módulo del usuario.
 * - Las contraseñas se guardan cifradas con Contrasena.cifrar(...) (PBKDF2).
 */
public class AccesoDao {

    // Mensajes unificados (docs/ARQUITECTURA.md 7.1)
    public static final String CREDENCIALES_INCORRECTAS = "Credenciales incorrectas";
    public static final String CUENTA_INACTIVA = "Cuenta inactiva. Comuníquese con el administrador de su módulo";

    private static final int INTENTOS_MAXIMOS = 3;
    private static final int MINUTOS_BLOQUEO = 15;
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    // Tablas de usuarios de los módulos. Son nombres fijos (no los escribe el usuario), por eso se pueden
    // poner en el texto del SQL; los datos siempre van con "?".
    private static final List<String> TABLAS = List.of("user_Ofc", "user_Aud", "user_Per", "user_Seg");

    /**
     * Orden de las revisiones:
     * 1. el correo no está en Login → "Credenciales incorrectas",
     * 2. la cuenta está bloqueada → cuántos minutos faltan,
     * 3. la contraseña no coincide → suma un intento; al tercero bloquea 15 minutos,
     * 4. se busca al usuario en las tablas user_*: si está inactivo → "Cuenta inactiva"
     *    (después de la contraseña, para no revelar a quien no la conoce si la cuenta existe),
     * 5. entra: se reinician los intentos.
     */
    public ResultadoAcceso iniciarSesion(String correo, String contrasena) {
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isEmpty()) {
            return ResultadoAcceso.error("Escriba su correo y su contraseña");
        }
        LocalDateTime ahora = LocalDateTime.now(ZONA);
        try (Connection con = ConexionBD.obtener()) {
            String guardada;
            int intentos;
            LocalDateTime bloqueadoHasta;
            String correoGuardado;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT correo, contrasena, intentos_fallidos, bloqueado_hasta FROM Login WHERE correo = ?")) {
                ps.setString(1, correo.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return ResultadoAcceso.error(CREDENCIALES_INCORRECTAS);
                    }
                    correoGuardado = rs.getString("correo");
                    guardada = rs.getString("contrasena");
                    intentos = rs.getInt("intentos_fallidos");
                    bloqueadoHasta = rs.getObject("bloqueado_hasta", LocalDateTime.class);
                }
            }

            if (bloqueadoHasta != null && bloqueadoHasta.isAfter(ahora)) {
                long minutos = Duration.between(ahora, bloqueadoHasta).toMinutes() + 1;
                return ResultadoAcceso.error("Cuenta bloqueada temporalmente por 3 intentos fallidos. Intente de nuevo en "
                        + minutos + (minutos == 1 ? " minuto" : " minutos"));
            }

            if (!Contrasena.coincide(contrasena, guardada)) {
                intentos++;
                if (intentos >= INTENTOS_MAXIMOS) {
                    actualizarIntentos(con, correoGuardado, 0, ahora.plusMinutes(MINUTOS_BLOQUEO));
                    return ResultadoAcceso.error(CREDENCIALES_INCORRECTAS + ". Por 3 intentos fallidos la cuenta quedó"
                            + " bloqueada " + MINUTOS_BLOQUEO + " minutos");
                }
                actualizarIntentos(con, correoGuardado, intentos, null);
                return ResultadoAcceso.error(CREDENCIALES_INCORRECTAS);
            }

            for (String tabla : TABLAS) {
                try (PreparedStatement ps = con.prepareStatement(select(tabla) + " WHERE u.correo = ?")) {
                    ps.setString(1, correoGuardado);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            if (!rs.getBoolean("Status")) {
                                return ResultadoAcceso.error(CUENTA_INACTIVA);
                            }
                            actualizarIntentos(con, correoGuardado, 0, null);
                            return ResultadoAcceso.exito(leer(rs, tabla));
                        }
                    }
                }
            }
            // Está en Login pero en ninguna tabla de usuarios (no debería pasar con los triggers)
            return ResultadoAcceso.error(CREDENCIALES_INCORRECTAS);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo iniciar sesión", e);
        }
    }

    /**
     * El usuario de la sesión, leído otra vez de su tabla (FiltroNucleo lo hace en cada petición): si lo dieron
     * de baja o le cambiaron el rol, se nota de inmediato. null si ya no existe o está inactivo.
     */
    public UsuarioSesion getActivo(UsuarioSesion usuario) {
        if (usuario == null || !TABLAS.contains(usuario.getTabla())) {
            return null;
        }
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(select(usuario.getTabla()) + " WHERE u.id_usuario = ? AND u.Status = TRUE")) {
            ps.setLong(1, usuario.getId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs, usuario.getTabla()) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el usuario de la sesión", e);
        }
    }

    /**
     * Cambio de contraseña con la sesión iniciada. Regresa el error, o null si se cambió.
     * Se cambia en la tabla user_* del usuario y el trigger AFTER UPDATE la copia a Login.
     */
    public String cambiarContrasena(UsuarioSesion usuario, String actual, String nueva, String confirmacion) {
        if (actual == null || actual.isEmpty()) {
            return "Escriba su contraseña actual";
        }
        String error = Contrasena.validarNueva(nueva);
        if (error != null) {
            return error;
        }
        if (!nueva.equals(confirmacion)) {
            return "La confirmación no coincide con la contraseña nueva";
        }
        if (!TABLAS.contains(usuario.getTabla())) {
            return "El usuario no existe";
        }
        try (Connection con = ConexionBD.obtener()) {
            String guardada;
            try (PreparedStatement ps = con.prepareStatement("SELECT contrasena FROM Login WHERE correo = ?")) {
                ps.setString(1, usuario.getCorreo());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "El usuario no existe";
                    }
                    guardada = rs.getString(1);
                }
            }
            if (!Contrasena.coincide(actual, guardada)) {
                return "La contraseña actual no es correcta";
            }
            if (Contrasena.coincide(nueva, guardada)) {
                return "La contraseña nueva debe ser distinta de la actual";
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE " + usuario.getTabla() + " SET contrasena = ? WHERE id_usuario = ?")) {
                ps.setString(1, Contrasena.cifrar(nueva));
                ps.setLong(2, usuario.getId());
                ps.executeUpdate();
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cambiar la contraseña", e);
        }
    }

    private void actualizarIntentos(Connection con, String correo, int intentos, LocalDateTime bloqueadoHasta)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE Login SET intentos_fallidos = ?, bloqueado_hasta = ? WHERE correo = ?")) {
            ps.setInt(1, intentos);
            ps.setObject(2, bloqueadoHasta);
            ps.setString(3, correo);
            ps.executeUpdate();
        }
    }

    private String select(String tabla) {
        return "SELECT u.id_usuario, u.nombre, u.apellido_paterno, u.apellido_materno, u.correo, u.Status, r.rol"
                + " FROM " + tabla + " u JOIN Rol r ON r.id_rol = u.id_rol";
    }

    private UsuarioSesion leer(ResultSet rs, String tabla) throws SQLException {
        String nombre = rs.getString("nombre") + " " + rs.getString("apellido_paterno");
        String materno = rs.getString("apellido_materno");
        if (materno != null && !materno.isBlank()) {
            nombre += " " + materno;
        }
        return new UsuarioSesion(rs.getLong("id_usuario"), tabla, nombre, rs.getString("correo"), rs.getString("rol"));
    }
}
