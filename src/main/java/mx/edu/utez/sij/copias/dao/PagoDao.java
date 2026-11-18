package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.Pago;

import java.util.Optional;

/**
 * Acceso a los registros de pago de copias certificadas (tabla cop_pago).
 *
 * Para pasar a MySQL: implementar PagoDaoMySql con PreparedStatement y ConexionBD.
 */
public interface PagoDao {

    Optional<Pago> buscarPorSolicitudId(Long solicitudId);

    Optional<Pago> buscarPorFolioPago(String folioPago);

    Optional<Pago> buscarPorStripeSessionId(String stripeSessionId);

    Pago guardar(Pago pago);
}
