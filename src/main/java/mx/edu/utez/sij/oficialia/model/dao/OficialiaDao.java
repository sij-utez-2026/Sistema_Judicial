package mx.edu.utez.sij.oficialia.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.oficialia.model.Oficialia;
import mx.edu.utez.sij.oficialia.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OficialiaDao {

    public static final String ROL_OFICIALIA_ADMIN = "OFICIALIA_ADMIN";

    private static final String SELECT =
            "SELECT o.clave_oficialia, o.nombre, o.estado, o.clave_juzgado,"
                    + " j.nombre AS nombre_juzgado, d.nombre AS nombre_distrito,"
                    + " adm.id_usuario AS admin_id, adm.nombre_completo AS admin_nombre, adm.correo AS admin_correo"
                    + " FROM oficialia_Ofc o"
                    + " JOIN juzgado_Ofc j ON j.clave_juzgado = o.clave_juzgado"
                    + " JOIN distrito_Ofc d ON d.clave_distrito = j.clave_distrito"
                    + " LEFT JOIN ("
                    + "   SELECT op.clave_oficialia, u.id_usuario,"
                    + "          CONCAT_WS(' ', u.nombre, u.apellido_paterno, NULLIF(u.apellido_materno, '')) AS nombre_completo,"
                    + "          u.correo"
                    + "   FROM oficialia_personal_Ofc op"
                    + "   JOIN user_Ofc u ON u.id_usuario = op.id_usuario"
                    + "   JOIN Rol r ON r.id_rol = u.id_rol"
                    + "   WHERE r.rol = '" + ROL_OFICIALIA_ADMIN + "'"
                    + "     AND op.id_op_trabajador = ("
                    + "         SELECT MAX(op2.id_op_trabajador)"
                    + "         FROM oficialia_personal_Ofc op2"
                    + "         JOIN user_Ofc u2 ON u2.id_usuario = op2.id_usuario"
                    + "         JOIN Rol r2 ON r2.id_rol = u2.id_rol"
                    + "         WHERE op2.clave_oficialia = op.clave_oficialia"
                    + "           AND r2.rol = '" + ROL_OFICIALIA_ADMIN + "'"
                    + "     )"
                    + " ) adm ON adm.clave_oficialia = o.clave_oficialia";

    public List<Oficialia> listar(String q) {
        StringBuilder sql = new StringBuilder(SELECT);
        boolean tieneFiltro = q != null && !q.isBlank();
        if (tieneFiltro) {
            sql.append(" WHERE LOWER(o.clave_oficialia) LIKE ? OR LOWER(o.nombre) LIKE ? OR LOWER(j.nombre) LIKE ?"
                    + " OR LOWER(COALESCE(adm.nombre_completo, '')) LIKE ? OR LOWER(o.estado) LIKE ?");
        }
        sql.append(" ORDER BY o.clave_oficialia ASC");

        List<Oficialia> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (tieneFiltro) {
                String patron = "%" + q.trim().toLowerCase() + "%";
                ps.setString(1, patron);
                ps.setString(2, patron);
                ps.setString(3, patron);
                ps.setString(4, patron);
                ps.setString(5, patron);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leer(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar las oficialías", e);
        }
    }

    public int contar() {
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM oficialia_Ofc");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo contar las oficialías", e);
        }
    }

    public Oficialia getById(String clave) {
        if (clave == null || clave.isBlank()) {
            return null;
        }
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE o.clave_oficialia = ?")) {
            ps.setString(1, clave.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar la oficialía", e);
        }
    }

    /**
     * Crea una oficialía junto con su administrador de oficialía (user_Ofc + oficialia_personal_Ofc)
     * en una sola transacción. El trigger de BD sincroniza el usuario hacia Login.
     */
    public String crearConAdministrador(Oficialia oficialia, Usuario admin, String contrasenaCifrada) {
        String errorOfi = validarOficialia(oficialia);
        if (errorOfi != null) {
            return errorOfi;
        }
        String errorAdmin = validarAdmin(admin);
        if (errorAdmin != null) {
            return errorAdmin;
        }
        String clave = oficialia.getClave().trim().toUpperCase();
        String correo = admin.getCorreo().trim().toLowerCase();

        try (Connection con = ConexionBD.obtener()) {
            if (existeClave(con, clave)) {
                return "Ya existe una oficialía registrada con la clave " + clave + ".";
            }
            if (existeCorreoEnSistema(con, correo)) {
                return "Ya existe un usuario registrado con el correo " + correo + ".";
            }

            con.setAutoCommit(false);
            try {
                int idRol = obtenerORegistrarRolAdmin(con);

                String sqlOfi = "INSERT INTO oficialia_Ofc (clave_oficialia, nombre, estado, clave_juzgado) VALUES (?, ?, 'ACTIVO', ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlOfi)) {
                    ps.setString(1, clave);
                    ps.setString(2, oficialia.getNombre().trim());
                    ps.setString(3, oficialia.getClaveJuzgado().trim());
                    ps.executeUpdate();
                }

                long idUsuario = insertarUsuarioOficialia(con, admin, correo, contrasenaCifrada, idRol);
                vincularPersonal(con, idUsuario, clave);

                con.commit();
                return null;
            } catch (SQLIntegrityConstraintViolationException e) {
                con.rollback();
                return "No se pudo guardar: la clave de oficialía o el correo ya están registrados, o el juzgado no es válido.";
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar la oficialía y su administrador", e);
        }
    }

    /**
     * Crea un nuevo usuario con rol OFICIALIA_ADMIN y lo asigna como administrador de una oficialía existente.
     */
    public String asignarNuevoAdministrador(String claveOficialia, Usuario admin, String contrasenaCifrada) {
        if (claveOficialia == null || claveOficialia.isBlank()) {
            return "No se especificó la oficialía.";
        }
        String errorAdmin = validarAdmin(admin);
        if (errorAdmin != null) {
            return errorAdmin;
        }
        String clave = claveOficialia.trim();
        String correo = admin.getCorreo().trim().toLowerCase();

        try (Connection con = ConexionBD.obtener()) {
            if (!existeClave(con, clave)) {
                return "La oficialía seleccionada no existe.";
            }
            if (existeCorreoEnSistema(con, correo)) {
                return "Ya existe un usuario registrado con el correo " + correo + ".";
            }

            con.setAutoCommit(false);
            try {
                int idRol = obtenerORegistrarRolAdmin(con);
                long idUsuario = insertarUsuarioOficialia(con, admin, correo, contrasenaCifrada, idRol);

                // Desvincula al administrador anterior de esta oficialía (solo rol OFICIALIA_ADMIN)
                String sqlLimpiar = "DELETE op FROM oficialia_personal_Ofc op"
                        + " JOIN user_Ofc u ON u.id_usuario = op.id_usuario"
                        + " JOIN Rol r ON r.id_rol = u.id_rol"
                        + " WHERE op.clave_oficialia = ? AND r.rol = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlLimpiar)) {
                    ps.setString(1, clave);
                    ps.setString(2, ROL_OFICIALIA_ADMIN);
                    ps.executeUpdate();
                }

                vincularPersonal(con, idUsuario, clave);
                con.commit();
                return null;
            } catch (SQLIntegrityConstraintViolationException e) {
                con.rollback();
                return "El correo " + correo + " ya está registrado en el sistema.";
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo asignar el administrador de la oficialía", e);
        }
    }

    public String actualizar(Oficialia oficialia) {
        String errorOfi = validarOficialia(oficialia);
        if (errorOfi != null) {
            return errorOfi;
        }
        String sql = "UPDATE oficialia_Ofc SET nombre = ?, clave_juzgado = ? WHERE clave_oficialia = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, oficialia.getNombre().trim());
            ps.setString(2, oficialia.getClaveJuzgado().trim());
            ps.setString(3, oficialia.getClave().trim());
            if (ps.executeUpdate() == 0) {
                return "No se encontró la oficialía a actualizar.";
            }
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "El juzgado seleccionado no existe.";
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar la oficialía", e);
        }
    }

    public String toggleEstado(String clave) {
        Oficialia actual = getById(clave);
        if (actual == null) {
            return "No se encontró la oficialía especificada.";
        }
        String nuevoEstado = actual.isActivo() ? "INACTIVO" : "ACTIVO";
        String sql = "UPDATE oficialia_Ofc SET estado = ? WHERE clave_oficialia = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, actual.getClave());
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cambiar el estado de la oficialía", e);
        }
    }

    public String eliminar(String clave) {
        if (clave == null || clave.isBlank()) {
            return "No se especificó la oficialía a eliminar.";
        }
        String claveLimpia = clave.trim();
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM oficialia_personal_Ofc WHERE clave_oficialia = ?")) {
                    ps.setString(1, claveLimpia);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM oficialia_Ofc WHERE clave_oficialia = ?")) {
                    ps.setString(1, claveLimpia);
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return "No se encontró la oficialía a eliminar.";
                    }
                }
                con.commit();
                return null;
            } catch (SQLIntegrityConstraintViolationException e) {
                con.rollback();
                return "No se puede eliminar la oficialía porque tiene demandas o registros asociados; puedes desactivarla con el interruptor.";
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar la oficialía", e);
        }
    }

    private String validarOficialia(Oficialia o) {
        if (o.getClave() == null || o.getClave().isBlank()) {
            return "La clave de la oficialía es obligatoria.";
        }
        if (o.getClave().trim().length() > 10) {
            return "La clave de la oficialía no debe exceder 10 caracteres.";
        }
        if (o.getNombre() == null || o.getNombre().isBlank()) {
            return "El nombre de la oficialía es obligatorio.";
        }
        if (o.getClaveJuzgado() == null || o.getClaveJuzgado().isBlank()) {
            return "Seleccione un juzgado válido.";
        }
        return null;
    }

    private String validarAdmin(Usuario admin) {
        if (admin == null) {
            return "Capture los datos del administrador de oficialía.";
        }
        if (admin.getNombre() == null || admin.getNombre().isBlank()) {
            return "El nombre del administrador es obligatorio.";
        }
        if (admin.getApellidoPaterno() == null || admin.getApellidoPaterno().isBlank()) {
            return "El apellido paterno del administrador es obligatorio.";
        }
        if (admin.getCorreo() == null || admin.getCorreo().isBlank() || !admin.getCorreo().contains("@")) {
            return "Capture un correo electrónico válido para el administrador.";
        }
        return null;
    }

    private boolean existeClave(Connection con, String clave) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM oficialia_Ofc WHERE clave_oficialia = ?")) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean existeCorreoEnSistema(Connection con, String correo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM Login WHERE correo = ?")) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM user_Ofc WHERE correo = ?")) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int obtenerORegistrarRolAdmin(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO Rol (rol) VALUES (?)")) {
            ps.setString(1, ROL_OFICIALIA_ADMIN);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT id_rol FROM Rol WHERE rol = ?")) {
            ps.setString(1, ROL_OFICIALIA_ADMIN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new IllegalStateException("No se pudo obtener el rol " + ROL_OFICIALIA_ADMIN);
    }

    private long insertarUsuarioOficialia(Connection con, Usuario admin, String correo, String contrasenaCifrada, int idRol)
            throws SQLException {
        String sql = "INSERT INTO user_Ofc (nombre, apellido_paterno, apellido_materno, correo, contrasena, id_rol, Status)"
                + " VALUES (?, ?, ?, ?, ?, ?, TRUE)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, admin.getNombre().trim());
            ps.setString(2, admin.getApellidoPaterno().trim());
            String materno = admin.getApellidoMaterno();
            ps.setString(3, (materno == null || materno.isBlank()) ? null : materno.trim());
            ps.setString(4, correo);
            ps.setString(5, contrasenaCifrada);
            ps.setInt(6, idRol);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private void vincularPersonal(Connection con, long idUsuario, String claveOficialia) throws SQLException {
        String sql = "INSERT INTO oficialia_personal_Ofc (id_usuario, clave_oficialia) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setString(2, claveOficialia);
            ps.executeUpdate();
        }
    }

    private Oficialia leer(ResultSet rs) throws SQLException {
        Oficialia ofi = new Oficialia();
        ofi.setClave(rs.getString("clave_oficialia"));
        ofi.setNombre(rs.getString("nombre"));
        ofi.setEstado(rs.getString("estado"));
        ofi.setClaveJuzgado(rs.getString("clave_juzgado"));
        ofi.setJuzgado(rs.getString("nombre_juzgado"));
        ofi.setDistrito(rs.getString("nombre_distrito"));
        long adminId = rs.getLong("admin_id");
        ofi.setAdminId(rs.wasNull() ? null : adminId);
        ofi.setAdminNombre(rs.getString("admin_nombre"));
        ofi.setAdminCorreo(rs.getString("admin_correo"));
        return ofi;
    }
}
