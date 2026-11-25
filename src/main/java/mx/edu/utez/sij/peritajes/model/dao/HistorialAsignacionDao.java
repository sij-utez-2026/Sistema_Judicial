package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.peritajes.model.HistorialAsignacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Historial de asignaciones de cada peritaje (tabla asignacion_Per). Las filas las agrega PeritajeDao.
public class HistorialAsignacionDao {

    // Asignación automática y reasignaciones, de la más antigua a la más reciente, con los nombres.
    public List<HistorialAsignacion> listarPorPeritaje(Long peritajeId) {
        String sql = "SELECT a.id_asignacion, a.id_perito_asignado, a.id_perito_saliente, a.id_usuario_cambio,"
                + " a.motivo_reasignacion, a.fecha_hora_asignacion,"
                + " CONCAT_WS(' ', n.nombre, n.apellido_paterno, n.apellido_materno) AS nuevo,"
                + " CONCAT_WS(' ', s.nombre, s.apellido_paterno, s.apellido_materno) AS saliente,"
                + " CONCAT_WS(' ', c.nombre, c.apellido_paterno, c.apellido_materno) AS cambio"
                + " FROM asignacion_Per a"
                + " JOIN user_Per n ON n.id_usuario = a.id_perito_asignado"
                + " LEFT JOIN user_Per s ON s.id_usuario = a.id_perito_saliente"
                + " LEFT JOIN user_Per c ON c.id_usuario = a.id_usuario_cambio"
                + " WHERE a.id_peritaje = ? ORDER BY a.id_asignacion";
        List<HistorialAsignacion> historial = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, peritajeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialAsignacion h = new HistorialAsignacion();
                    h.setId(rs.getLong("id_asignacion"));
                    h.setPeritajeId(peritajeId);
                    h.setPeritoNuevoId(rs.getLong("id_perito_asignado"));
                    h.setPeritoAnteriorId(Jdbc.largo(rs, "id_perito_saliente"));
                    h.setRealizadoPorId(Jdbc.largo(rs, "id_usuario_cambio"));
                    h.setMotivo(rs.getString("motivo_reasignacion"));
                    h.setFecha(Jdbc.fecha(rs, "fecha_hora_asignacion"));
                    h.setPeritoNuevoNombre(rs.getString("nuevo"));
                    h.setPeritoAnteriorNombre(rs.getString("saliente"));
                    String cambio = rs.getString("cambio");
                    if (h.isAutomatica()) {
                        h.setRealizadoPor("Sistema");
                    } else {
                        // Sin usuario de Peritajes: la hizo el administrador general (el nombre está en la bitácora)
                        h.setRealizadoPor(cambio == null || cambio.isBlank() ? "el Administrador general" : cambio);
                    }
                    historial.add(h);
                }
            }
            return historial;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo consultar el historial de asignaciones", e);
        }
    }
}
