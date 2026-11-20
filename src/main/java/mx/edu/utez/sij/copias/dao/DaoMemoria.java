package mx.edu.utez.sij.copias.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * TEMPORAL: base de los DAO en memoria mientras no hay base de datos (los datos se pierden al reiniciar Tomcat).
 * Se borra junto con todas las clases ...DaoMemoria cuando existan las ...DaoMySql.
 */
public abstract class DaoMemoria<T> {

    private final Map<Long, T> registros = new LinkedHashMap<>();
    private long siguienteId = 1;

    protected abstract Long idDe(T registro);

    protected abstract void asignarId(T registro, Long id);

    public synchronized Optional<T> findById(Long id) {
        return Optional.ofNullable(registros.get(id));
    }

    /** Si no tiene id lo inserta (como AUTO_INCREMENT); si ya tiene, lo actualiza. */
    public synchronized T save(T registro) {
        if (idDe(registro) == null) {
            asignarId(registro, siguienteId++);
        }
        registros.put(idDe(registro), registro);
        return registro;
    }

    public synchronized List<T> findAll() {
        return new ArrayList<>(registros.values());
    }

    public synchronized long count() {
        return registros.size();
    }

    /** Registros que cumplen la condición, en el orden en que se guardaron. */
    protected synchronized List<T> filtrar(Predicate<T> condicion) {
        return registros.values().stream().filter(condicion).toList();
    }
}
