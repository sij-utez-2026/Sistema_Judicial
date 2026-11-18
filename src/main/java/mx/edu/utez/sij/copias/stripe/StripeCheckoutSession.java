package mx.edu.utez.sij.copias.stripe;

/**
 * Datos relevantes de una sesión de Stripe Checkout (API /v1/checkout/sessions).
 */
public record StripeCheckoutSession(
        String id,
        String url,
        String status,
        String paymentStatus,
        String currency,
        Long amountTotal,
        String paymentIntentId,
        String clientReferenceId,
        String folioSolicitudMetadata
) {
}
