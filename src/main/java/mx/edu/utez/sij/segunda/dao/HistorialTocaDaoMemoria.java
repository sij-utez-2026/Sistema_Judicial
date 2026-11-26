package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.segunda.model.HistorialToca;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TEMPORAL: guarda el historial en memoria mientras no hay base de datos.
 * Se borra cuando exista HistorialTocaDaoMySql.
 */
public class HistorialTocaDaoMemoria implements HistorialTocaDao {

    private final Map<Long, HistorialToca> historial = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public synchronized HistorialToca save(HistorialToca h) {
        if (h.getId() == null) {
            h.setId(siguienteId++);
        }
        historial.put(h.getId(), h);
        return h;
    }

    @Override
    public synchronized List<HistorialToca> findByTocaId(Long tocaId) {
        List<HistorialToca> lista = new ArrayList<>();
        for (HistorialToca h : historial.values()) {
            if (tocaId.equals(h.getTocaId())) {
                lista.add(h);
            }
        }
        lista.sort(java.util.Comparator.comparing(HistorialToca::getFecha,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        return lista;
    }
}


