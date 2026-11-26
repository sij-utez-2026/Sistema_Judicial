package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.Magistrado;

import java.util.*;

/**
 * TEMPORAL: guarda los magistrados en memoria mientras no hay base de datos.
 * Se borra cuando exista MagistradoDaoMySql (ver los pasos en MagistradoDao).
 */
public class MagistradoDaoMemoria implements MagistradoDao {

    private final Map<Long, Magistrado> magistrados = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public synchronized Optional<Magistrado> findById(Long id) {
        return Optional.ofNullable(magistrados.get(id));
    }

    @Override
    public synchronized Magistrado save(Magistrado magistrado) {
        if (magistrado.getId() == null) {
            magistrado.setId(siguienteId++);
        }
        magistrados.put(magistrado.getId(), magistrado);
        return magistrado;
    }

    @Override
    public synchronized long count() {
        return magistrados.size();
    }

    @Override
    public synchronized long countByActivo(boolean activo) {
        return magistrados.values().stream().filter(m -> m.isActivo() == activo).count();
    }

    @Override
    public synchronized boolean existsByCorreoIgnoreCase(String correo) {
        if (correo == null) return false;
        return magistrados.values().stream()
                .anyMatch(m -> m.getCorreo().equalsIgnoreCase(correo));
    }

    @Override
    public synchronized boolean existsBySalaId(Long salaId, Long excluirId) {
        if (salaId == null) return false;
        return magistrados.values().stream().anyMatch(m ->
                salaId.equals(m.getSalaId())
                        && (excluirId == null || !m.getId().equals(excluirId)));
    }

    @Override
    public synchronized boolean existsByUsuarioId(Long usuarioId) {
        if (usuarioId == null) return false;
        return magistrados.values().stream().anyMatch(m -> usuarioId.equals(m.getUsuarioId()));
    }

    @Override
    public synchronized Pagina<Magistrado> buscar(String texto, Boolean activo,
                                                  int pagina, int tamano) {
        String t = (texto == null) ? null : texto.toLowerCase();

        List<Magistrado> filtrados = magistrados.values().stream()
                .filter(m -> t == null
                        || m.getNombreCompleto().toLowerCase().contains(t)
                        || m.getCorreo().toLowerCase().contains(t))
                .filter(m -> activo == null || m.isActivo() == activo)
                .sorted(Comparator.comparing(Magistrado::getNombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Magistrado::getApellidos, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return Pagina.de(filtrados, pagina, tamano);
    }

    @Override
    public synchronized List<Magistrado> activos() {
        return magistrados.values().stream()
                .filter(Magistrado::isActivo)
                .sorted(Comparator.comparing(Magistrado::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}

