package mx.edu.utez.sij.copias.service;

import mx.edu.utez.sij.copias.dao.HistorialSolicitudDao;
import mx.edu.utez.sij.copias.dao.SolicitudCopiaDao;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.Formatos;
import mx.edu.utez.sij.copias.model.HistorialSolicitud;
import mx.edu.utez.sij.copias.model.SolicitudCopia;

import java.util.List;

/**
 * Único lugar donde cambia el estado de una solicitud: guarda el cambio y lo anota en el historial
 * con quién lo hizo (usuarioId: id de core_usuario, lo pasa el servlet).
 * TODO núcleo: registrar también cada cambio con bitacoraService.registrar(...) en la misma transacción.
 */
public class HistorialService {

    private final SolicitudCopiaDao solicitudDao;
    private final HistorialSolicitudDao historialDao;

    public HistorialService(SolicitudCopiaDao solicitudDao, HistorialSolicitudDao historialDao) {
        this.solicitudDao = solicitudDao;
        this.historialDao = historialDao;
    }

    /** Primer registro del historial, al crear la solicitud (estado anterior nulo). */
    public void registrarCreacion(SolicitudCopia solicitud, String comentario, Long usuarioId) {
        anotar(solicitud.getId(), null, solicitud.getEstado(), comentario, usuarioId);
    }

    public SolicitudCopia cambiarEstado(SolicitudCopia solicitud, EstadoSolicitud nuevo, String comentario, Long usuarioId) {
        EstadoSolicitud anterior = solicitud.getEstado();
        solicitud.setEstado(nuevo);
        solicitud.setActualizadoEn(Formatos.ahora());
        SolicitudCopia guardada = solicitudDao.save(solicitud);
        anotar(solicitud.getId(), anterior, nuevo, comentario, usuarioId);
        return guardada;
    }

    public List<HistorialSolicitud> historial(Long solicitudId) {
        return historialDao.findBySolicitudIdOrderByFechaAsc(solicitudId);
    }

    private void anotar(Long solicitudId, EstadoSolicitud anterior, EstadoSolicitud nuevo, String comentario,
                        Long usuarioId) {
        HistorialSolicitud registro = new HistorialSolicitud();
        registro.setSolicitudId(solicitudId);
        registro.setEstadoAnterior(anterior);
        registro.setEstadoNuevo(nuevo);
        registro.setUsuarioId(usuarioId);
        registro.setComentario(comentario);
        registro.setFecha(Formatos.ahora());
        historialDao.save(registro);
    }
}
