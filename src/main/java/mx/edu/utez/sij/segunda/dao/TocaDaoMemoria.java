package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.EstadoToca;
import mx.edu.utez.sij.segunda.model.Toca;

import java.util.*;

/**
 * TEMPORAL: guarda las tocas en memoria mientras no hay base de datos.
 * Se borra cuando exista TocaDaoMySql (ver los pasos en TocaDao).
 */
public class TocaDaoMemoria implements TocaDao {

    private final Map<Long, Toca> tocas = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public synchronized Optional<Toca> findById(Long id) {
        return Optional.ofNullable(tocas.get(id));
    }

    @Override
    public synchronized Optional<Toca> findByFolio(String folioToca) {
        if (folioToca == null) return Optional.empty();
        return tocas.values().stream()
                .filter(t -> folioToca.equalsIgnoreCase(t.getFolioToca()))
                .findFirst();
    }

    @Override
    public synchronized Toca save(Toca toca) {
        if (toca.getId() == null) {
            toca.setId(siguienteId++);
        }
        tocas.put(toca.getId(), toca);
        return toca;
    }

    @Override
    public synchronized long count() {
        return tocas.size();
    }

    @Override
    public synchronized long countByEstado(EstadoToca estado) {
        return tocas.values().stream().filter(t -> t.getEstado() == estado).count();
    }

    @Override
    public synchronized boolean existsByFolio(String folioToca) {
        if (folioToca == null) return false;
        return tocas.values().stream()
                .anyMatch(t -> folioToca.equalsIgnoreCase(t.getFolioToca()));
    }

    @Override
    public synchronized boolean existsByNumeroExpediente(String numeroExpediente) {
        if (numeroExpediente == null) return false;
        return tocas.values().stream()
                .anyMatch(t -> numeroExpediente.equalsIgnoreCase(t.getNumeroExpediente())
                        && !t.getEstado().esFinal());
    }

    @Override
    public synchronized boolean tieneTocasPendientesPorSala(Long salaId) {
        if (salaId == null) return false;
        return tocas.values().stream()
                .anyMatch(t -> salaId.equals(t.getSalaId()) && !t.getEstado().esFinal());
    }

    @Override
    public synchronized boolean tieneTocasPendientesPorMagistrado(Long magistradoUsuarioId) {
        if (magistradoUsuarioId == null) return false;
        return tocas.values().stream()
                .anyMatch(t -> magistradoUsuarioId.equals(t.getMagistradoUsuarioId())
                        && !t.getEstado().esFinal());
    }

    @Override
    public synchronized Pagina<Toca> buscar(String texto, EstadoToca estado, Long salaId,
                                            Long soloDeMagistradoUsuarioId,
                                            int pagina, int tamano) {
        String t = (texto == null) ? null : texto.toLowerCase();

        List<Toca> filtradas = tocas.values().stream()
                .filter(x -> t == null
                        || x.getFolioToca().toLowerCase().contains(t)
                        || x.getNumeroExpediente().toLowerCase().contains(t))
                .filter(x -> estado == null || x.getEstado() == estado)
                .filter(x -> salaId == null || salaId.equals(x.getSalaId()))
                .filter(x -> soloDeMagistradoUsuarioId == null
                        || soloDeMagistradoUsuarioId.equals(x.getMagistradoUsuarioId()))
                .sorted(Comparator.comparing(Toca::getFechaRecepcion,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .toList();

        return Pagina.de(filtradas, pagina, tamano);
    }

    @Override
    public synchronized List<Toca> pendientesDeMagistrado(Long magistradoUsuarioId) {
        if (magistradoUsuarioId == null) return List.of();
        return tocas.values().stream()
                .filter(t -> magistradoUsuarioId.equals(t.getMagistradoUsuarioId()))
                .filter(t -> !t.getEstado().esFinal())
                .sorted(Comparator.comparing(Toca::getFechaRecepcion,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .toList();
    }
}


