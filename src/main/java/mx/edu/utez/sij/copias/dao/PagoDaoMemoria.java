package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.Pago;
import mx.edu.utez.sij.copias.service.ReglaNegocioException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Almacenamiento en memoria de pagos garantizando unicidad por solicitud y por sesión de Stripe.
 */
public class PagoDaoMemoria implements PagoDao {

    private final Map<Long, Pago> porId = new LinkedHashMap<>();
    private final Map<Long, Pago> porSolicitudId = new LinkedHashMap<>();
    private final Map<String, Pago> porFolioPago = new LinkedHashMap<>();
    private final Map<String, Pago> porStripeSessionId = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public synchronized Optional<Pago> buscarPorSolicitudId(Long solicitudId) {
        if (solicitudId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(porSolicitudId.get(solicitudId));
    }

    @Override
    public synchronized Optional<Pago> buscarPorFolioPago(String folioPago) {
        if (folioPago == null || folioPago.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(porFolioPago.get(folioPago.trim().toUpperCase()));
    }

    @Override
    public synchronized Optional<Pago> buscarPorStripeSessionId(String stripeSessionId) {
        if (stripeSessionId == null || stripeSessionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(porStripeSessionId.get(stripeSessionId.trim()));
    }

    @Override
    public synchronized Pago guardar(Pago pago) {
        if (pago.getSolicitudId() != null && porSolicitudId.containsKey(pago.getSolicitudId())) {
            throw new ReglaNegocioException("Esta solicitud ya cuenta con un pago registrado.");
        }
        if (pago.getStripeSessionId() != null && porStripeSessionId.containsKey(pago.getStripeSessionId())) {
            throw new ReglaNegocioException("La sesión de pago ya fue registrada.");
        }
        if (pago.getId() == null) {
            pago.setId(siguienteId++);
        }
        String folioNormalizado = pago.getFolioPago().trim().toUpperCase();
        pago.setFolioPago(folioNormalizado);
        porId.put(pago.getId(), pago);
        if (pago.getSolicitudId() != null) {
            porSolicitudId.put(pago.getSolicitudId(), pago);
        }
        porFolioPago.put(folioNormalizado, pago);
        if (pago.getStripeSessionId() != null && !pago.getStripeSessionId().isBlank()) {
            porStripeSessionId.put(pago.getStripeSessionId().trim(), pago);
        }
        return pago;
    }
}
