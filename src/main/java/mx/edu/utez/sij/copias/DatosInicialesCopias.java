package mx.edu.utez.sij.copias;

import mx.edu.utez.sij.copias.dao.PagoDao;
import mx.edu.utez.sij.copias.dao.SolicitudDao;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.Pago;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.model.TipoDemanda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Datos de prueba iniciales para probar el flujo de pago en línea (RF 4.1)
 * mientras se integran las pantallas de captura y validación.
 */
public class DatosInicialesCopias {

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    private final SolicitudDao solicitudDao;
    private final PagoDao pagoDao;

    public DatosInicialesCopias(SolicitudDao solicitudDao, PagoDao pagoDao) {
        this.solicitudDao = solicitudDao;
        this.pagoDao = pagoDao;
    }

    public void cargar() {
        if (solicitudDao.contar() > 0) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now(ZONA);

        solicitudDao.guardar(new SolicitudCopia(
                null, "CC-2026-A7K9M2", "EXP-2026-00104",
                1L, "Juzgado Primero Civil", 1L, "Civil",
                TipoDemanda.PUBLICA, EstadoSolicitud.LISTO_PARA_PAGO,
                new BigDecimal("150.00"), ahora.minusHours(3)));

        solicitudDao.guardar(new SolicitudCopia(
                null, "CC-2026-B3P8Q5", "EXP-2026-00219",
                2L, "Juzgado Segundo Familiar", 2L, "Familiar",
                TipoDemanda.PRIVADA, EstadoSolicitud.APROBADO,
                new BigDecimal("250.00"), ahora.minusDays(1)));

        solicitudDao.guardar(new SolicitudCopia(
                null, "CC-2026-C9W4R1", "EXP-2026-00308",
                3L, "Juzgado Primero Mercantil", 3L, "Mercantil",
                TipoDemanda.PRIVADA, EstadoSolicitud.EN_ESPERA_VALIDACION,
                new BigDecimal("180.00"), ahora.minusHours(5)));

        solicitudDao.guardar(new SolicitudCopia(
                null, "CC-2026-D2N6T8", "EXP-2026-00087",
                4L, "Juzgado Penal de Primera Instancia", 4L, "Penal",
                TipoDemanda.PRIVADA, EstadoSolicitud.CANCELADO,
                new BigDecimal("200.00"), ahora.minusDays(2)));

        SolicitudCopia pagada = solicitudDao.guardar(new SolicitudCopia(
                null, "CC-2026-E5H1V7", "EXP-2026-00042",
                1L, "Juzgado Primero Civil", 1L, "Civil",
                TipoDemanda.PUBLICA, EstadoSolicitud.PAGADO_EN_PREPARACION,
                new BigDecimal("150.00"), ahora.minusDays(1).minusHours(4)));

        Pago pagoEjemplo = new Pago();
        pagoEjemplo.setSolicitudId(pagada.getId());
        pagoEjemplo.setFolioSolicitud(pagada.getFolioSolicitud());
        pagoEjemplo.setNumeroExpediente(pagada.getNumeroExpediente());
        pagoEjemplo.setFolioPago("PAG-2026-9M3X4K");
        pagoEjemplo.setMonto(pagada.getCosto());
        pagoEjemplo.setMoneda("MXN");
        pagoEjemplo.setStripeSessionId("cs_test_ejemplo_inicial");
        pagoEjemplo.setStripePaymentIntentId("pi_ejemplo_inicial");
        pagoEjemplo.setEstadoPago("PAGADO");
        pagoEjemplo.setMetodoPago("Stripe Checkout (Tarjeta)");
        pagoEjemplo.setFecha(ahora.minusDays(1));
        pagoEjemplo.setSimulado(false);
        pagoDao.guardar(pagoEjemplo);
    }
}
