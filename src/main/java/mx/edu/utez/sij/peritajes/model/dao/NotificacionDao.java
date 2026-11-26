package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.peritajes.model.Notificacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// Avisos a los usuarios (RF 5.1, tabla notificacion_Per). Se muestran en el inicio de cada usuario.
// Pendiente con el núcleo: el envío por correo (enviado_correo se queda en FALSE mientras no haya correo).
public class NotificacionDao {

    // Eventos que originan un aviso (evento_origen)
    public static final String ASIGNACION = "ASIGNACION";
    public static final String REASIGNACION_ENTRANTE = "REASIGNACION_ENTRANTE";
    public static final String REASIGNACION_SALIENTE = "REASIGNACION_SALIENTE";
    public static final String ENTREGA_DICTAMEN = "ENTREGA_DICTAMEN";

    // Se llama dentro de la transacción de la acción que origina el aviso
    public void avisar(Connection con, Long usuarioId, Long peritajeId, String evento, String mensaje)
            throws SQLException {
        String sql = "INSERT INTO notificacion_Per (id_usuario, id_peritaje, evento_origen, mensaje) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            if (peritajeId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setLong(2, peritajeId);
            }
            ps.setString(3, evento);
            ps.setString(4, mensaje);
            ps.executeUpdate();
        }
    }

    // Los avisos más recientes del usuario (la tabla no tiene fecha: el id más alto es el más reciente)
    public List<Notificacion> recientes(Long usuarioId, int cuantos) {
        String sql = "SELECT id_notificacion, id_peritaje, evento_origen, mensaje FROM notificacion_Per "
                + "WHERE id_usuario = ? ORDER BY id_notificacion DESC LIMIT ?";
        List<Notificacion> avisos = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ps.setInt(2, cuantos);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    avisos.add(new Notificacion(rs.getLong("id_notificacion"), Jdbc.largo(rs, "id_peritaje"),
                            rs.getString("evento_origen"), rs.getString("mensaje")));
                }
            }
            return avisos;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron consultar los avisos", e);
        }
    }
}
