package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.SolicitudCopia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Almacenamiento en memoria de solicitudes mientras no se conecte MySQL.
 */
public class SolicitudDaoMemoria implements SolicitudDao {

    private final Map<Long, SolicitudCopia> porId = new LinkedHashMap<>();
    private final Map<String, Long> idPorFolio = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public synchronized Optional<SolicitudCopia> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public synchronized Optional<SolicitudCopia> buscarPorFolio(String folioSolicitud) {
        if (folioSolicitud == null || folioSolicitud.isBlank()) {
            return Optional.empty();
        }
        Long id = idPorFolio.get(folioSolicitud.trim().toUpperCase());
        return id == null ? Optional.empty() : Optional.ofNullable(porId.get(id));
    }

    @Override
    public synchronized List<SolicitudCopia> listarTodas() {
        return new ArrayList<>(porId.values());
    }

    @Override
    public synchronized SolicitudCopia guardar(SolicitudCopia solicitud) {
        if (solicitud.getId() == null) {
            solicitud.setId(siguienteId++);
        }
        String folioNormalizado = solicitud.getFolioSolicitud().trim().toUpperCase();
        solicitud.setFolioSolicitud(folioNormalizado);
        porId.put(solicitud.getId(), solicitud);
        idPorFolio.put(folioNormalizado, solicitud.getId());
        return solicitud;
    }

    @Override
    public synchronized void actualizarEstado(Long id, EstadoSolicitud nuevoEstado) {
        SolicitudCopia solicitud = porId.get(id);
        if (solicitud != null) {
            solicitud.setEstado(nuevoEstado);
        }
    }

    @Override
    public synchronized long contar() {
        return porId.size();
    }
}
