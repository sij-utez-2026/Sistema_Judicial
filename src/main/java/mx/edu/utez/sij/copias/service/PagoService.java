package mx.edu.utez.sij.copias.service;

import mx.edu.utez.sij.copias.dao.PagoDao;
import mx.edu.utez.sij.copias.model.CorreoEnviado;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.Formatos;
import mx.edu.utez.sij.copias.model.Pago;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.model.TipoCorreo;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * Pago simulado de copias certificadas (RF 4.1).
 * No realiza cargos reales: valida que la solicitud pueda pagarse, registra cop_pago (CP-AAAA-XXXXXX),
 * cambia el estado a PAGADO_EN_PREPARACION y envía el comprobante por correo al solicitante.
 */
public class PagoService {

    private static final String CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LARGO_FOLIO = 6;

    private final SecureRandom aleatorio = new SecureRandom();
    private final PagoDao pagoDao;
    private final SolicitudCopiaService solicitudService;
    private final HistorialService historialService;
    private final CorreoCopiasService correoService;

    public PagoService(PagoDao pagoDao, SolicitudCopiaService solicitudService,
                       HistorialService historialService, CorreoCopiasService correoService) {
        this.pagoDao = pagoDao;
        this.solicitudService = solicitudService;
        this.historialService = historialService;
        this.correoService = correoService;
    }

    /** Verifica que la solicitud exista y pueda pagarse según las reglas del DFR (RN-01, 6.4). */
    public SolicitudCopia prepararPago(Long solicitudId) {
        SolicitudCopia solicitud = solicitudService.obtener(solicitudId);
        validarPagable(solicitud);
        return solicitud;
    }

    /**
     * Registra el pago simulado (una sola vez por solicitud), actualiza el historial y envía el comprobante.
     * usuarioId: trabajador que registra el cobro (de SesionCopias).
     */
    public synchronized Pago registrarPago(Long solicitudId, Long usuarioId) {
        SolicitudCopia solicitud = prepararPago(solicitudId);

        Pago pago = new Pago();
        pago.setSolicitudId(solicitud.getId());
        pago.setFolioPago(generarFolioPago());
        pago.setMonto(solicitud.getCosto());
        pago.setSimulado(true);
        pago.setRegistradoPor(usuarioId);
        pago.setFecha(Formatos.ahora());

        Pago guardado = pagoDao.save(pago);
        historialService.cambiarEstado(solicitud, EstadoSolicitud.PAGADO_EN_PREPARACION,
                "Pago simulado registrado · Comprobante " + guardado.getFolioPago(), usuarioId);
        enviarCorreoComprobante(solicitud, guardado, usuarioId);
        return guardado;
    }

    public Optional<Pago> pagoDe(Long solicitudId) {
        return pagoDao.findBySolicitudId(solicitudId);
    }

    /**
     * Obtiene el comprobante de una solicitud pagada.
     * Si la copia ya fue entregada, el folio queda inhabilitado (RN-03).
     */
    public Pago obtenerComprobante(Long solicitudId) {
        SolicitudCopia solicitud = solicitudService.obtener(solicitudId);
        if (solicitud.getEstado() == EstadoSolicitud.ENTREGADO) {
            throw new ReglaNegocioException(
                    "La copia ya fue entregada: el folio " + solicitud.getFolio() + " está inhabilitado");
        }
        return pagoDao.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new ReglaNegocioException("La solicitud aún no tiene un pago registrado"));
    }

    public CorreoEnviado reenviarComprobante(Long solicitudId, Long usuarioId) {
        SolicitudCopia solicitud = solicitudService.obtener(solicitudId);
        Pago pago = obtenerComprobante(solicitudId);
        return enviarCorreoComprobante(solicitud, pago, usuarioId);
    }

    private void validarPagable(SolicitudCopia solicitud) {
        if (pagoDao.findBySolicitudId(solicitud.getId()).isPresent()) {
            throw new ReglaNegocioException("La solicitud ya cuenta con un pago registrado");
        }
        EstadoSolicitud estado = solicitud.getEstado();
        if (estado == EstadoSolicitud.EN_ESPERA_VALIDACION) {
            throw new ReglaNegocioException("Debe esperar la aprobación antes de pagar");
        }
        if (estado == EstadoSolicitud.CANCELADO) {
            throw new ReglaNegocioException("La solicitud fue cancelada y no se puede pagar");
        }
        if (estado == EstadoSolicitud.ENTREGADO) {
            throw new ReglaNegocioException(
                    "La copia ya fue entregada: el folio " + solicitud.getFolio() + " está inhabilitado");
        }
        if (!estado.sePuedePagar()) {
            throw new ReglaNegocioException("La solicitud ya cuenta con un pago registrado");
        }
    }

    private CorreoEnviado enviarCorreoComprobante(SolicitudCopia solicitud, Pago pago, Long usuarioId) {
        String cuerpo = """
                Estimado(a) %s:

                Se registró el pago simulado de su solicitud de copias certificadas.

                Comprobante:           %s
                Folio de la solicitud: %s
                Expediente:            %s
                Juzgado:               %s
                Materia:               %s
                RFC del solicitante:   %s
                Fecha de pago:         %s
                Importe:               %s MXN
                Estado actual:         %s

                Pago simulado — no se realiza ningún cargo.
                Conserve este comprobante junto con una identificación oficial para recoger sus copias en ventanilla.
                """.formatted(
                solicitud.getSolicitanteNombre(),
                pago.getFolioPago(),
                solicitud.getFolio(),
                solicitud.getNumeroExpediente(),
                solicitud.getJuzgado(),
                solicitud.getMateria(),
                solicitud.getSolicitanteRfc(),
                pago.getFechaTexto(),
                pago.getMontoTexto(),
                solicitud.getEstado().getTexto());
        return correoService.enviar(solicitud, TipoCorreo.COMPROBANTE,
                "Comprobante de pago · " + solicitud.getFolio(), cuerpo, usuarioId);
    }

    private String generarFolioPago() {
        String folio;
        do {
            StringBuilder sufijo = new StringBuilder(LARGO_FOLIO);
            for (int i = 0; i < LARGO_FOLIO; i++) {
                sufijo.append(CARACTERES.charAt(aleatorio.nextInt(CARACTERES.length())));
            }
            folio = "CP-" + Formatos.ahora().getYear() + "-" + sufijo;
        } while (pagoDao.existsByFolioPago(folio));
        return folio;
    }
}
