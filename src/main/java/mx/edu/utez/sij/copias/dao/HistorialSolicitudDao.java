package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.HistorialSolicitud;

import java.util.List;

/** Historial de estados (tabla cop_solicitud_historial). Para MySQL: ver SolicitudCopiaDao. */
public interface HistorialSolicitudDao {

    /** SQL: INSERT INTO cop_solicitud_historial (...) VALUES (...) */
    HistorialSolicitud save(HistorialSolicitud historial);

    /** Del primer cambio al último. SQL: {@code SELECT * FROM cop_solicitud_historial WHERE solicitud_id = ? ORDER BY fecha, id} */
    List<HistorialSolicitud> findBySolicitudIdOrderByFechaAsc(Long solicitudId);
}
