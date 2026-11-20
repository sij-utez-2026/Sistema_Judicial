package mx.edu.utez.sij.copias.service;

import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.Formatos;
import mx.edu.utez.sij.copias.model.HistorialSolicitud;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.dao.HistorialSolicitudDao;
import mx.edu.utez.sij.copias.dao.SolicitudCopiaDao;
import mx.edu.utez.sij.copias.temporal.SesionTemporal;

import java.util.List;

/**
 * Único lugar donde cambia el estado de una solicitud: guarda el cambio y lo anota en el historial.
 * TODO núcleo: registrar también cada cambio con bitacoraService.registrar(...) en la misma transacción.
 */
public class HistorialService {

    private final SolicitudCopiaDao solicitudDao;
    private final HistorialSolicitudDao historialDao;
    private final SesionTemporal sesion;

    public HistorialService(SolicitudCopiaDao solicitudDao,
                            HistorialSolicitudDao historialDao,
                            SesionTemporal sesion) {
        this.solicitudDao = solicitudDao;
        this.historialDao = historialDao;
        this.sesion = sesion;
    }

    /** Primer registro del historial, al crear la solicitud (estado anterior nulo). */
    public void registrarCreacion(SolicitudCopia solicitud, String comentario) {
        anotar(solicitud.getId(), null, solicitud.getEstado(), comentario);
    }

    public SolicitudCopia cambiarEstado(SolicitudCopia solicitud, EstadoSolicitud nuevo, String comentario) {
        EstadoSolicitud anterior = solicitud.getEstado();
        solicitud.setEstado(nuevo);
        solicitud.setActualizadoEn(Formatos.ahora());
        SolicitudCopia guardada = solicitudDao.save(solicitud);
        anotar(solicitud.getId(), anterior, nuevo, comentario);
        return guardada;
    }

    public List<HistorialSolicitud> historial(Long solicitudId) {
        return historialDao.findBySolicitudIdOrderByFechaAsc(solicitudId);
    }

    private void anotar(Long solicitudId, EstadoSolicitud anterior, EstadoSolicitud nuevo, String comentario) {
        HistorialSolicitud registro = new HistorialSolicitud();
        registro.setSolicitudId(solicitudId);
        registro.setEstadoAnterior(anterior);
        registro.setEstadoNuevo(nuevo);
        registro.setUsuarioId(sesion.usuarioId());
        registro.setComentario(comentario);
        registro.setFecha(Formatos.ahora());
        historialDao.save(registro);
    }
}
