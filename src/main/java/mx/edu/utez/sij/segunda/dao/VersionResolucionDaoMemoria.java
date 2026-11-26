package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.segunda.model.VersionResolucion;

import java.util.*;

public class VersionResolucionDaoMemoria implements VersionResolucionDao {

    private final Map<Long, VersionResolucion> versiones = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public synchronized VersionResolucion save(VersionResolucion version) {
        if (version.getId() == null) {
            version.setId(siguienteId++);
        }
        versiones.put(version.getId(), version);
        return version;
    }

    @Override
    public synchronized List<VersionResolucion> findByResolucionId(Long resolucionId) {
        List<VersionResolucion> lista = new ArrayList<>();
        for (VersionResolucion v : versiones.values()) {
            if (resolucionId.equals(v.getResolucionId())) {
                lista.add(v);
            }
        }
        lista.sort(Comparator.comparing(VersionResolucion::getFecha,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return lista;
    }
}


