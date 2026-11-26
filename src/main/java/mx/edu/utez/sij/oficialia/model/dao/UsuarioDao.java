package mx.edu.utez.sij.oficialia.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.oficialia.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDao {

    public Usuario buscarPorId(long idUsuario) {
        String sql = "SELECT u.id_usuario, u.nombre, u.apellido_paterno, u.apellido_materno, u.correo, u.Status, r.rol"
                + " FROM user_Ofc u JOIN Rol r ON r.id_rol = u.id_rol WHERE u.id_usuario = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Usuario u = new Usuario();
                u.setId(rs.getLong("id_usuario"));
                u.setNombre(rs.getString("nombre"));
                u.setApellidoPaterno(rs.getString("apellido_paterno"));
                u.setApellidoMaterno(rs.getString("apellido_materno"));
                u.setCorreo(rs.getString("correo"));
                u.setActivo(rs.getBoolean("Status"));
                u.setRol(rs.getString("rol"));
                return u;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el usuario", e);
        }
    }

    public String actualizarNombre(long idUsuario, String nombre, String apellidoPaterno, String apellidoMaterno) {
        if (nombre == null || nombre.isBlank() || apellidoPaterno == null || apellidoPaterno.isBlank()) {
            return "El nombre y apellido paterno son obligatorios.";
        }
        String sql = "UPDATE user_Ofc SET nombre = ?, apellido_paterno = ?, apellido_materno = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setString(2, apellidoPaterno.trim());
            ps.setString(3, (apellidoMaterno == null || apellidoMaterno.isBlank()) ? null : apellidoMaterno.trim());
            ps.setLong(4, idUsuario);
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el usuario", e);
        }
    }
}
