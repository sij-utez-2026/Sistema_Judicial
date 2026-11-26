package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.Sala;

import java.util.*;

/**
 * TEMPORAL: guarda las salas en memoria mientras no hay base de datos (se pierden al reiniciar).
 * Se borra cuando exista SalaDaoMySql (ver los pasos en SalaDao).
 */
public class SalaDaoMemoria implements SalaDao {

    private final Map<Long, Sala> salas = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public synchronized Optional<Sala> findById(Long id) {
        return Optional.ofNullable(salas.get(id));
    }

    @Override
    public synchronized Sala save(Sala sala) {
        if (sala.getId() == null) {
            sala.setId(siguienteId++);
        }
        salas.put(sala.getId(), sala);
        return sala;
    }

    @Override
    public synchronized long count() {
        return salas.size();
    }

    @Override
    public synchronized long countByActiva(boolean activa) {
        return salas.values().stream().filter(s -> s.isActiva() == activa).count();
    }

    @Override
    public synchronized boolean existsByNombreAndDistrito(String nombre, String distritoId, Long excluirId) {
        return salas.values().stream().anyMatch(s ->
                s.getNombre().equalsIgnoreCase(nombre)
                        && s.getDistritoId().equals(distritoId)
                        && (excluirId == null || !s.getId().equals(excluirId)));
    }

    @Override
    public synchronized boolean existsByMagistradoUsuarioId(Long magistradoUsuarioId, Long excluirSalaId) {
        if (magistradoUsuarioId == null) return false;
        return salas.values().stream().anyMatch(s ->
                magistradoUsuarioId.equals(s.getMagistradoUsuarioId())
                        && (excluirSalaId == null || !s.getId().equals(excluirSalaId)));
    }

    @Override
    public synchronized Pagina<Sala> buscar(String texto, String distritoId, Boolean activa,
                                            int pagina, int tamano) {
        String t = (texto == null) ? null : texto.toLowerCase();

        List<Sala> filtradas = salas.values().stream()
                .filter(s -> t == null || s.getNombre().toLowerCase().contains(t))
                .filter(s -> distritoId == null || distritoId.equals(s.getDistritoId()))
                .filter(s -> activa == null || s.isActiva() == activa)
                .sorted(Comparator.comparing(Sala::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return Pagina.de(filtradas, pagina, tamano);
    }

    @Override
    public synchronized List<Sala> activas() {
        return salas.values().stream()
                .filter(Sala::isActiva)
                .sorted(Comparator.comparing(Sala::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public synchronized boolean tieneTocasPendientes(Long salaId) {
        // TODO equipo 4: consultar TocaDao cuando exista. Por ahora no hay tocas.
        return false;
    }
}


