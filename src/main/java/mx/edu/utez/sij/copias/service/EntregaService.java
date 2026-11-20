package mx.edu.utez.sij.copias.service;

import mx.edu.utez.sij.copias.model.CorreoEnviado;
import mx.edu.utez.sij.copias.model.Entrega;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.Formatos;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.dao.EntregaDao;
import mx.edu.utez.sij.copias.dao.SolicitudCopiaDao;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;

import java.util.List;
import java.util.Optional;

/**
 * Entrega física (RF 5.1). Después del pago hay dos caminos:
 * - la copia está disponible: se imprime y se entrega al momento → "Entregado";
 * - no está disponible: queda "Pagado y en preparación"; cuando esté lista se avisa por correo
 *   ("Lista para recoger") y se entrega cuando el solicitante llegue.
 * Al entregar, el folio queda inhabilitado (RN-03).
 */
public class EntregaService {

    private static final List<EstadoSolicitud> PENDIENTES =
            List.of(EstadoSolicitud.PAGADO_EN_PREPARACION, EstadoSolicitud.LISTO_PARA_RECOGER);

    private final EntregaDao entregaDao;
    private final SolicitudCopiaDao solicitudDao;
    private final SolicitudCopiaService solicitudService;
    private final HistorialService historialService;
    private final CorreoCopiasService correoService;

    public EntregaService(EntregaDao entregaDao, SolicitudCopiaDao solicitudDao,
                          SolicitudCopiaService solicitudService, HistorialService historialService,
                          CorreoCopiasService correoService) {
        this.entregaDao = entregaDao;
        this.solicitudDao = solicitudDao;
        this.solicitudService = solicitudService;
        this.historialService = historialService;
        this.correoService = correoService;
    }

    /** Pagadas que falta entregar, las más antiguas primero. */
    public List<SolicitudCopia> pendientes() {
        return solicitudDao.findByEstadoInOrderByFechaSolicitudAsc(PENDIENTES);
    }

    /** Avisa por correo que la copia ya está lista. Se puede repetir si el solicitante no lo recibió. */
    public CorreoEnviado avisarListaParaRecoger(Long solicitudId, Long usuarioId) {
        SolicitudCopia solicitud = pendienteDeEntrega(solicitudId);
        CorreoEnviado correo = correoService.enviarListaParaRecoger(solicitud, usuarioId);
        if (!correo.isExito()) {
            throw new ReglaNegocioException("No se pudo enviar el correo. Intente de nuevo");
        }
        if (solicitud.getEstado() == EstadoSolicitud.PAGADO_EN_PREPARACION) {
            historialService.cambiarEstado(solicitud, EstadoSolicitud.LISTO_PARA_RECOGER,
                    "Aviso enviado a " + solicitud.getSolicitanteCorreo(), usuarioId);
        }
        return correo;
    }

    /** Registra la entrega física de la copia impresa. usuarioId: quién entrega (lo pasa el servlet). */
    public Entrega entregar(Long solicitudId, Long usuarioId) {
        SolicitudCopia solicitud = pendienteDeEntrega(solicitudId);

        Entrega entrega = new Entrega();
        entrega.setSolicitudId(solicitud.getId());
        entrega.setEntregadoPor(usuarioId);
        entrega.setFecha(Formatos.ahora());
        Entrega guardada = entregaDao.save(entrega);

        historialService.cambiarEstado(solicitud, EstadoSolicitud.ENTREGADO, "Copia impresa y entregada en ventanilla", usuarioId);
        return guardada;
    }

    public Optional<Entrega> entregaDe(Long solicitudId) {
        return entregaDao.findBySolicitudId(solicitudId);
    }

    private SolicitudCopia pendienteDeEntrega(Long solicitudId) {
        SolicitudCopia solicitud = solicitudService.obtener(solicitudId);
        if (solicitud.getEstado() == EstadoSolicitud.ENTREGADO) {
            throw new ReglaNegocioException("La copia ya fue entregada: el folio " + solicitud.getFolio() + " está inhabilitado");
        }
        if (!solicitud.getEstado().pendienteDeEntrega()) {
            throw new ReglaNegocioException("La solicitud aún no está pagada (" + solicitud.getEstado().getTexto() + ")");
        }
        return solicitud;
    }
}
