package mx.edu.utez.sij.copias;

import mx.edu.utez.sij.copias.dao.PagoDao;
import mx.edu.utez.sij.copias.dao.PagoDaoMemoria;
import mx.edu.utez.sij.copias.dao.SolicitudDao;
import mx.edu.utez.sij.copias.dao.SolicitudDaoMemoria;
import mx.edu.utez.sij.copias.service.PagoService;
import mx.edu.utez.sij.copias.stripe.StripeCheckoutClient;
import mx.edu.utez.sij.copias.stripe.StripeConfig;

/**
 * Objetos del módulo de Copias Certificadas (EQUIPO 5).
 * Mantiene una única instancia compartida entre todos los servlets del módulo.
 */
public final class Copias {

    private static final SolicitudDao SOLICITUD_DAO = new SolicitudDaoMemoria();
    private static final PagoDao PAGO_DAO = new PagoDaoMemoria();
    private static final StripeConfig STRIPE_CONFIG = StripeConfig.desdeEntorno();
    private static final StripeCheckoutClient STRIPE_CLIENT = new StripeCheckoutClient(STRIPE_CONFIG);
    private static final PagoService PAGO_SERVICE = new PagoService(SOLICITUD_DAO, PAGO_DAO, STRIPE_CLIENT);

    private Copias() {
    }

    public static SolicitudDao solicitudDao() {
        return SOLICITUD_DAO;
    }

    public static PagoDao pagoDao() {
        return PAGO_DAO;
    }

    public static StripeConfig stripeConfig() {
        return STRIPE_CONFIG;
    }

    public static PagoService pagoService() {
        return PAGO_SERVICE;
    }
}
