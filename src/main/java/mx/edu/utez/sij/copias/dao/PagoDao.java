package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.Pago;

import java.util.Optional;

/** Acceso a pagos simulados (tabla cop_pago, bd/50_copias.sql). */
public interface PagoDao {

    /**
     * SQL: {@code INSERT INTO cop_pago (solicitud_id, folio_pago, monto, simulado, registrado_por, fecha)
     * VALUES (?, ?, ?, ?, ?, ?)}
     */
    Pago save(Pago pago);

    /** SQL: {@code SELECT * FROM cop_pago WHERE solicitud_id = ?} */
    Optional<Pago> findBySolicitudId(Long solicitudId);

    /** SQL: {@code SELECT * FROM cop_pago WHERE folio_pago = ?} */
    Optional<Pago> findByFolioPago(String folioPago);

    /** SQL: {@code SELECT COUNT(*) > 0 FROM cop_pago WHERE folio_pago = ?} */
    boolean existsByFolioPago(String folioPago);
}
