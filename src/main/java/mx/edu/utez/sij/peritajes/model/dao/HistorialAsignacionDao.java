package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.peritajes.model.HistorialAsignacion;
import mx.edu.utez.sij.peritajes.model.Perito;

import java.util.Comparator;
import java.util.List;

import static mx.edu.utez.sij.peritajes.model.dao.BaseDatosMemoria.HISTORIAL;

// Historial de asignaciones de cada peritaje (tabla per_historial_asignacion).
public class HistorialAsignacionDao {

    private final PeritoDao peritoDao = new PeritoDao();

    // SQL: INSERT INTO per_historial_asignacion (...) VALUES (...)  (+ sus candidatos en per_historial_candidato)
    public boolean create(HistorialAsignacion historial) {
        historial.setId(BaseDatosMemoria.ID_HISTORIAL.getAndIncrement());
        HISTORIAL.add(historial);
        return true;
    }

    // Asignación automática y reasignaciones, de la más antigua a la más reciente, con los nombres de los peritos.
    // SQL: SELECT ... FROM per_historial_asignacion WHERE peritaje_id = ? ORDER BY fecha
    public List<HistorialAsignacion> listarPorPeritaje(Long peritajeId) {
        List<HistorialAsignacion> historial = HISTORIAL.stream()
                .filter(h -> h.getPeritajeId().equals(peritajeId))
                .sorted(Comparator.comparing(HistorialAsignacion::getFecha))
                .toList();
        for (HistorialAsignacion h : historial) {
            h.setPeritoAnteriorNombre(nombrePerito(h.getPeritoAnteriorId()));
            h.setPeritoNuevoNombre(nombrePerito(h.getPeritoNuevoId()));
        }
        return historial;
    }

    private String nombrePerito(Long peritoId) {
        Perito perito = peritoDao.getById(peritoId);
        return perito == null ? "" : perito.getNombreCompleto();
    }
}
