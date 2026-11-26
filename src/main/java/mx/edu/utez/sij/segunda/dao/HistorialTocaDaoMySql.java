package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.segunda.model.HistorialToca;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO MySQL para historial_toca_Seg; la tabla sólo conserva una descripción y fecha. */
public class HistorialTocaDaoMySql implements HistorialTocaDao {
    @Override public HistorialToca save(HistorialToca h) {
        String descripcion = descripcion(h);
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO historial_toca_Seg (id_toca, descripcion) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1, h.getTocaId()); p.setString(2, descripcion); p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) { if (r.next()) h.setId(r.getLong(1)); }
            h.setAccion(descripcion); h.setDetalle(null); return h;
        } catch (SQLException e) { throw DaoJdbcSupport.error("guardar historial de toca", e); }
    }
    @Override public List<HistorialToca> findByTocaId(Long tocaId) {
        List<HistorialToca> resultado = new ArrayList<>();
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(
                "SELECT id_historial,id_toca,descripcion,fecha_hora FROM historial_toca_Seg WHERE id_toca=? ORDER BY fecha_hora,id_historial")) {
            p.setLong(1, tocaId); try (ResultSet r = p.executeQuery()) {
                while (r.next()) { HistorialToca h = new HistorialToca(); h.setId(r.getLong("id_historial")); h.setTocaId(r.getLong("id_toca")); h.setAccion(r.getString("descripcion"));
                    var ts = r.getTimestamp("fecha_hora"); if (ts != null) h.setFecha(ts.toLocalDateTime()); resultado.add(h); }
            }
            return List.copyOf(resultado);
        } catch (SQLException e) { throw DaoJdbcSupport.error("consultar historial de toca", e); }
    }
    private static String descripcion(HistorialToca h) {
        String accion = h.getAccion() == null ? "Actualización" : h.getAccion().trim();
        String detalle = h.getDetalle() == null ? "" : h.getDetalle().trim();
        String texto = detalle.isBlank() ? accion : accion + " · " + detalle;
        return texto.length() <= 300 ? texto : texto.substring(0, 300);
    }
}
