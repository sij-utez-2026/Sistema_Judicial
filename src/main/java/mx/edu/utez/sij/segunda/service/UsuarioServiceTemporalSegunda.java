package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.nucleo.acceso.Contrasena;
import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Cuentas centrales de magistrados administradas desde Segunda Instancia. */
public class UsuarioServiceTemporalSegunda {
    public static final String ROL_MAGISTRADO = "SEGUNDA_MAGISTRADO";

    public static class UsuarioTemporal {
        private Long id;
        private String nombre;
        private String apellidos;
        private String correo;
        private boolean activo = true;
        private String rol;
        private LocalDateTime creadoEn;
        private String contrasenaTemporal;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
        public String getRol() { return rol; }
        public void setRol(String rol) { this.rol = rol; }
        public LocalDateTime getCreadoEn() { return creadoEn; }
        public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
        public String getContrasenaTemporal() { return contrasenaTemporal; }
        public void setContrasenaTemporal(String contrasenaTemporal) { this.contrasenaTemporal = contrasenaTemporal; }
        public String getNombreCompleto() { return nombre + (apellidos == null || apellidos.isBlank() ? "" : " " + apellidos); }
    }

    /** Localiza una cuenta activa con rol explícito SEGUNDA_MAGISTRADO. */
    public UsuarioTemporal buscarCuentaMagistrado(String correo) {
        String sql = "SELECT u.id_usuario,u.nombre,u.apellido_paterno,u.apellido_materno,u.correo,u.fecha_creacion "
                + "FROM user_Seg u JOIN Login l ON LOWER(l.correo)=LOWER(u.correo) JOIN Rol r ON r.id_rol=u.id_rol "
                + "WHERE LOWER(u.correo)=LOWER(?) AND u.Status=TRUE AND r.rol=? LIMIT 1";
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, correo.trim());
            p.setString(2, ROL_MAGISTRADO);
            try (ResultSet rs = p.executeQuery()) {
                if (!rs.next()) throw new ReglaNegocioException("La cuenta no existe, está inactiva o no tiene rol SEGUNDA_MAGISTRADO");
                return mapear(rs, true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo localizar la cuenta de magistrado", e);
        }
    }

    /** Crea user_Seg; el trigger vigente copia las credenciales a Login. */
    public UsuarioTemporal crearCuenta(String nombre, String apellidos, String correo) {
        String temporal = Contrasena.generarTemporal();
        String[] partes = separarApellidos(apellidos);
        try (Connection c = ConexionBD.obtener()) {
            c.setAutoCommit(false);
            try {
                int idRol;
                try (PreparedStatement p = c.prepareStatement("SELECT id_rol FROM Rol WHERE rol = ? LIMIT 1")) {
                    p.setString(1, ROL_MAGISTRADO);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new ReglaNegocioException("No existe el rol SEGUNDA_MAGISTRADO en la base de datos");
                        idRol = r.getInt(1);
                    }
                }
                UsuarioTemporal u = new UsuarioTemporal();
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO user_Seg (nombre, apellido_paterno, apellido_materno, correo, contrasena, id_rol, Status) "
                                + "VALUES (?, ?, ?, ?, ?, ?, TRUE)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1, nombre.trim());
                    p.setString(2, partes[0]);
                    if (partes[1] == null) p.setNull(3, java.sql.Types.VARCHAR); else p.setString(3, partes[1]);
                    p.setString(4, correo.trim().toLowerCase());
                    p.setString(5, Contrasena.cifrar(temporal));
                    p.setInt(6, idRol);
                    if (p.executeUpdate() != 1) throw new IllegalStateException("No se pudo crear la cuenta del magistrado");
                    try (ResultSet keys = p.getGeneratedKeys()) {
                        if (!keys.next()) throw new IllegalStateException("La base no devolvió el id de usuario");
                        u.setId(keys.getLong(1));
                    }
                }
                c.commit();
                u.setNombre(nombre.trim());
                u.setApellidos(apellidos.trim());
                u.setCorreo(correo.trim().toLowerCase());
                u.setActivo(true);
                u.setRol(ROL_MAGISTRADO);
                u.setContrasenaTemporal(temporal);
                return u;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear la cuenta del magistrado", e);
        }
    }

    public void actualizarDatos(Long usuarioId, String nombre, String apellidos) {
        String[] partes = separarApellidos(apellidos);
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(
                "UPDATE user_Seg SET nombre = ?, apellido_paterno = ?, apellido_materno = ? "
                        + "WHERE id_usuario = ? AND id_rol = (SELECT id_rol FROM Rol WHERE rol = ?)")) {
            p.setString(1, nombre.trim());
            p.setString(2, partes[0]);
            if (partes[1] == null) p.setNull(3, java.sql.Types.VARCHAR); else p.setString(3, partes[1]);
            p.setLong(4, usuarioId);
            p.setString(5, ROL_MAGISTRADO);
            if (p.executeUpdate() != 1) throw new ReglaNegocioException("La cuenta del magistrado no existe");
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar la cuenta del magistrado", e);
        }
    }

    /** Bloquea también el acceso central de la cuenta, sin borrar el usuario ni tocar otras tablas. */
    public void desactivar(Long usuarioId) { cambiarEstadoCuenta(usuarioId, false); }
    public void reactivar(Long usuarioId) { cambiarEstadoCuenta(usuarioId, true); }

    public Optional<UsuarioTemporal> buscar(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT u.id_usuario,u.nombre,u.apellido_paterno,u.apellido_materno,u.correo,u.Status,r.rol,u.fecha_creacion "
                + "FROM user_Seg u JOIN Rol r ON r.id_rol=u.id_rol WHERE u.id_usuario=?";
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs, false)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el usuario de Segunda", e);
        }
    }

    public List<UsuarioTemporal> listar() {
        String sql = "SELECT u.id_usuario,u.nombre,u.apellido_paterno,u.apellido_materno,u.correo,"
                + "u.Status,r.rol,u.fecha_creacion FROM user_Seg u JOIN Rol r ON r.id_rol=u.id_rol "
                + "WHERE r.rol=? ORDER BY u.apellido_paterno,u.apellido_materno,u.nombre,u.id_usuario";
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, ROL_MAGISTRADO);
            try (ResultSet rs = p.executeQuery()) {
                java.util.ArrayList<UsuarioTemporal> usuarios = new java.util.ArrayList<>();
                while (rs.next()) usuarios.add(mapear(rs, false));
                return List.copyOf(usuarios);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron listar las cuentas de magistrado", e);
        }
    }

    public boolean existeCorreo(String correo) {
        if (correo == null) return false;
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(
                "SELECT 1 FROM Login WHERE LOWER(correo)=LOWER(?) LIMIT 1")) {
            p.setString(1, correo.trim());
            try (ResultSet rs = p.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo verificar el correo", e);
        }
    }

    private void cambiarEstadoCuenta(Long usuarioId, boolean activo) {
        if (usuarioId == null) throw new IllegalArgumentException("El identificador de usuario es obligatorio");
        String update = "UPDATE user_Seg SET Status=? WHERE id_usuario=? "
                + "AND id_rol=(SELECT id_rol FROM Rol WHERE rol=? LIMIT 1)";
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(update)) {
            p.setBoolean(1, activo);
            p.setLong(2, usuarioId);
            p.setString(3, ROL_MAGISTRADO);
            int actualizados = p.executeUpdate();
            if (actualizados == 0) {
                try (PreparedStatement existe = c.prepareStatement(
                        "SELECT 1 FROM user_Seg u JOIN Rol r ON r.id_rol=u.id_rol "
                                + "WHERE u.id_usuario=? AND r.rol=?")) {
                    existe.setLong(1, usuarioId);
                    existe.setString(2, ROL_MAGISTRADO);
                    try (ResultSet rs = existe.executeQuery()) {
                        if (!rs.next()) throw new ReglaNegocioException("La cuenta del magistrado no existe");
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el estado de la cuenta del magistrado", e);
        }
    }

    private UsuarioTemporal mapear(ResultSet rs, boolean soloActiva) throws SQLException {
        UsuarioTemporal u = new UsuarioTemporal();
        u.setId(rs.getLong("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        String paterno = rs.getString("apellido_paterno");
        String materno = rs.getString("apellido_materno");
        u.setApellidos((paterno == null ? "" : paterno) + (materno == null || materno.isBlank() ? "" : " " + materno));
        u.setCorreo(rs.getString("correo"));
        u.setActivo(soloActiva || rs.getBoolean("Status"));
        u.setRol(ROL_MAGISTRADO);
        var ts = rs.getTimestamp("fecha_creacion");
        if (ts != null) u.setCreadoEn(ts.toLocalDateTime());
        return u;
    }

    private String[] separarApellidos(String apellidos) {
        String texto = apellidos == null ? "" : apellidos.trim().replaceAll("\\s+", " ");
        if (texto.isBlank()) return new String[]{"", null};
        int espacio = texto.indexOf(' ');
        return espacio < 0 ? new String[]{texto, null} : new String[]{texto.substring(0, espacio), texto.substring(espacio + 1)};
    }
}
