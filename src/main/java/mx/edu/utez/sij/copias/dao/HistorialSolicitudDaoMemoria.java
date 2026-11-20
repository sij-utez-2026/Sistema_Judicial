package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.HistorialSolicitud;

import java.util.Comparator;
import java.util.List;

/** TEMPORAL: ver DaoMemoria. */
public class HistorialSolicitudDaoMemoria extends DaoMemoria<HistorialSolicitud> implements HistorialSolicitudDao {

    @Override
    protected Long idDe(HistorialSolicitud registro) {
        return registro.getId();
    }

    @Override
    protected void asignarId(HistorialSolicitud registro, Long id) {
        registro.setId(id);
    }

    @Override
    public List<HistorialSolicitud> findBySolicitudIdOrderByFechaAsc(Long solicitudId) {
        return filtrar(h -> h.getSolicitudId().equals(solicitudId)).stream()
                .sorted(Comparator.comparing(HistorialSolicitud::getFecha).thenComparing(HistorialSolicitud::getId))
                .toList();
    }
}
