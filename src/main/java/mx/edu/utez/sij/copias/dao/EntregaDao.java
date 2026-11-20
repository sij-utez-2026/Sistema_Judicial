package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.Entrega;

import java.util.Optional;

/** Entregas (tabla cop_entrega). Para MySQL: ver SolicitudCopiaDao. */
public interface EntregaDao {

    /** SQL: INSERT INTO cop_entrega (solicitud_id, entregado_por, fecha) VALUES (?, ?, ?) */
    Entrega save(Entrega entrega);

    /** SQL: {@code SELECT * FROM cop_entrega WHERE solicitud_id = ?} */
    Optional<Entrega> findBySolicitudId(Long solicitudId);
}
