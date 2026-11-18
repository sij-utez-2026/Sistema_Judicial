package mx.edu.utez.sij.copias.service;

import mx.edu.utez.sij.copias.dao.PagoDao;
import mx.edu.utez.sij.copias.dao.SolicitudDao;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.Pago;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.stripe.StripeCheckoutClient;
import mx.edu.utez.sij.copias.stripe.StripeCheckoutSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio para el pago de copias certificadas mediante Stripe Checkout en MXN (RF 4.1).
 */
public class PagoService {

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    private static final String CARACTERES_FOLIO = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final SolicitudDao solicitudDao;
    private final PagoDao pagoDao;
    private final StripeCheckoutClient stripeClient;
    private final Object bloqueoPagos = new Object();

    public PagoService(SolicitudDao solicitudDao, PagoDao pagoDao, StripeCheckoutClient stripeClient) {
        this.solicitudDao = solicitudDao;
        this.pagoDao = pagoDao;
        this.stripeClient = stripeClient;
    }

    public Optional<SolicitudCopia> buscarSolicitudPorFolio(String folio) {
        if (folio == null || folio.isBlank()) {
            return Optional.empty();
        }
        return solicitudDao.buscarPorFolio(folio.trim());
    }

    public List<SolicitudCopia> listarSolicitudes() {
        return solicitudDao.listarTodas();
    }

    public Optional<Pago> buscarPagoPorSolicitud(Long solicitudId) {
        return pagoDao.buscarPorSolicitudId(solicitudId);
    }

    public Pago obtenerComprobante(String folioPago, String folioSolicitud) {
        Optional<Pago> pagoOpt = Optional.empty();
        if (folioPago != null && !folioPago.isBlank()) {
            pagoOpt = pagoDao.buscarPorFolioPago(folioPago.trim());
        } else if (folioSolicitud != null && !folioSolicitud.isBlank()) {
            Optional<SolicitudCopia> solOpt = solicitudDao.buscarPorFolio(folioSolicitud.trim());
            if (solOpt.isPresent()) {
                pagoOpt = pagoDao.buscarPorSolicitudId(solOpt.get().getId());
            }
        }
        Pago pago = pagoOpt.orElseThrow(() -> new ReglaNegocioException("No se encontró el comprobante de pago solicitado."));

        SolicitudCopia solicitud = solicitudDao.buscarPorId(pago.getSolicitudId())
                .orElseThrow(() -> new ReglaNegocioException("No se encontró la solicitud asociada al comprobante."));
        if (solicitud.getEstado() == EstadoSolicitud.ENTREGADO) {
            throw new ReglaNegocioException("El folio ya fue entregado y quedó inhabilitado para reimpresión.");
        }
        return pago;
    }

    /**
     * Valida que la solicitud pueda pagarse y crea una sesión oficial en Stripe Checkout en MXN.
     */
    public StripeCheckoutSession iniciarCheckoutStripe(String folioSolicitud, String urlBaseAplicacion) {
        if (folioSolicitud == null || folioSolicitud.isBlank()) {
            throw new ReglaNegocioException("Debe indicar el folio de la solicitud para iniciar el pago.");
        }
        if (urlBaseAplicacion == null || urlBaseAplicacion.isBlank()) {
            throw new ReglaNegocioException("No se pudo determinar la URL de retorno de la aplicación.");
        }

        SolicitudCopia solicitud = solicitudDao.buscarPorFolio(folioSolicitud.trim())
                .orElseThrow(() -> new ReglaNegocioException("No se encontraron registros con ese folio."));

        validarSolicitudPagable(solicitud);

        String baseLimpia = urlBaseAplicacion.endsWith("/")
                ? urlBaseAplicacion.substring(0, urlBaseAplicacion.length() - 1)
                : urlBaseAplicacion;
        String folioCodificado = URLEncoder.encode(solicitud.getFolioSolicitud(), StandardCharsets.UTF_8);

        // Stripe sustituye literalmente {CHECKOUT_SESSION_ID} al regresar al sitio
        String successUrl = baseLimpia + "/copias/publico/pago/retorno?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = baseLimpia + "/copias/publico/pago?folio=" + folioCodificado + "&cancelado=1";

        return stripeClient.crearSesion(solicitud, successUrl, cancelUrl);
    }

    /**
     * Verifica la sesión directamente con Stripe y registra el pago de forma idempotente y atómica.
     */
    public Pago confirmarPagoStripe(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ReglaNegocioException("Falta el identificador de sesión de Stripe para validar el pago.");
        }
        String sessionIdLimpio = sessionId.trim();

        synchronized (bloqueoPagos) {
            Optional<Pago> existentePorSesion = pagoDao.buscarPorStripeSessionId(sessionIdLimpio);
            if (existentePorSesion.isPresent()) {
                return existentePorSesion.get();
            }

            StripeCheckoutSession sesion = stripeClient.consultarSesion(sessionIdLimpio);

            if (!"complete".equalsIgnoreCase(sesion.status()) || !"paid".equalsIgnoreCase(sesion.paymentStatus())) {
                throw new ReglaNegocioException("El pago aún no aparece confirmado como pagado en Stripe.");
            }
            if (!"mxn".equalsIgnoreCase(sesion.currency())) {
                throw new ReglaNegocioException("La moneda cobrada (" + sesion.currency() + ") no corresponde a MXN.");
            }

            String folio = sesion.clientReferenceId();
            if (folio == null || folio.isBlank()) {
                folio = sesion.folioSolicitudMetadata();
            }
            if (folio == null || folio.isBlank()) {
                throw new ReglaNegocioException("La sesión de Stripe no incluye el folio de la solicitud.");
            }

            String folioFinal = folio;
            SolicitudCopia solicitud = solicitudDao.buscarPorFolio(folioFinal)
                    .orElseThrow(() -> new ReglaNegocioException("No se encontró la solicitud " + folioFinal + " para registrar el pago."));

            Optional<Pago> existentePorSolicitud = pagoDao.buscarPorSolicitudId(solicitud.getId());
            if (existentePorSolicitud.isPresent()) {
                Pago yaRegistrado = existentePorSolicitud.get();
                if (sessionIdLimpio.equals(yaRegistrado.getStripeSessionId())) {
                    return yaRegistrado;
                }
                throw new ReglaNegocioException("Esta solicitud ya cuenta con un pago registrado.");
            }

            validarSolicitudPagable(solicitud);

            long esperadoCentavos = solicitud.getCostoCentavos();
            if (sesion.amountTotal() == null || sesion.amountTotal() != esperadoCentavos) {
                throw new ReglaNegocioException("El monto cobrado en Stripe no coincide con el costo de la solicitud.");
            }

            Pago pago = new Pago();
            pago.setSolicitudId(solicitud.getId());
            pago.setFolioSolicitud(solicitud.getFolioSolicitud());
            pago.setNumeroExpediente(solicitud.getNumeroExpediente());
            pago.setFolioPago(generarFolioPago());
            pago.setMonto(solicitud.getCosto());
            pago.setMoneda("MXN");
            pago.setStripeSessionId(sesion.id() != null ? sesion.id() : sessionIdLimpio);
            pago.setStripePaymentIntentId(sesion.paymentIntentId());
            pago.setEstadoPago("PAGADO");
            pago.setMetodoPago("Stripe Checkout (Tarjeta)");
            pago.setFecha(LocalDateTime.now(ZONA));
            pago.setSimulado(false);

            Pago guardado = pagoDao.guardar(pago);
            solicitudDao.actualizarEstado(solicitud.getId(), EstadoSolicitud.PAGADO_EN_PREPARACION);
            return guardado;
        }
    }

    public String motivoNoPagable(SolicitudCopia solicitud) {
        if (solicitud == null || solicitud.getEstado() == null) {
            return "La solicitud no se encuentra disponible para pago.";
        }
        return switch (solicitud.getEstado()) {
            case EN_ESPERA_VALIDACION -> "La solicitud está en espera de validación por el personal del juzgado. Debe esperar la aprobación antes de pagar.";
            case CANCELADO -> "Esta solicitud fue cancelada y no puede ser pagada.";
            case PAGADO_EN_PREPARACION -> "Esta solicitud ya cuenta con un pago registrado y está en preparación.";
            case ENTREGADO -> "Esta solicitud ya fue entregada y su folio quedó inhabilitado.";
            case LISTO_PARA_PAGO, APROBADO -> null;
        };
    }

    private void validarSolicitudPagable(SolicitudCopia solicitud) {
        if (pagoDao.buscarPorSolicitudId(solicitud.getId()).isPresent()) {
            throw new ReglaNegocioException("Esta solicitud ya cuenta con un pago registrado.");
        }
        String motivo = motivoNoPagable(solicitud);
        if (motivo != null) {
            throw new ReglaNegocioException(motivo);
        }
    }

    private String generarFolioPago() {
        int anio = LocalDateTime.now(ZONA).getYear();
        String folio;
        do {
            StringBuilder sufijo = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sufijo.append(CARACTERES_FOLIO.charAt(ALEATORIO.nextInt(CARACTERES_FOLIO.length())));
            }
            folio = "PAG-" + anio + "-" + sufijo;
        } while (pagoDao.buscarPorFolioPago(folio).isPresent());
        return folio;
    }
}
