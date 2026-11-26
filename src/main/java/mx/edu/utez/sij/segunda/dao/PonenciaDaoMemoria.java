package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.segunda.model.Ponencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * TEMPORAL: guarda las ponencias en memoria mientras no hay base de datos.
 * Se borra cuando exista PonenciaDaoMySql.
 */
public class PonenciaDaoMemoria implements PonenciaDao {

    private final Map<Long, Ponencia> ponencias = new LinkedHashMap<>();
    private long siguienteId = 1;

    // Para findByMagistradoEntre necesitamos saber a qué magistrado pertenece cada toca.
    // Como este DAO no conoce TocaDao, se le inyecta un "resolvedor" simple.
    private final java.util.function.Function<Long, Long> tocaAMagistrado;

    public PonenciaDaoMemoria(java.util.function.Function<Long, Long> tocaAMagistrado) {
        this.tocaAMagistrado = tocaAMagistrado;
    }

    @Override
    public synchronized Optional<Ponencia> findById(Long id) {
        return Optional.ofNullable(ponencias.get(id));
    }

    @Override
    public synchronized Ponencia save(Ponencia ponencia) {
        if (ponencia.getId() == null) {
            ponencia.setId(siguienteId++);
        }
        ponencias.put(ponencia.getId(), ponencia);
        return ponencia;
    }

    @Override
    public synchronized List<Ponencia> findByTocaId(Long tocaId) {
        return ponencias.values().stream()
                .filter(p -> tocaId.equals(p.getTocaId()))
                .sorted(Comparator.comparing(Ponencia::getFechaHora,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    public synchronized List<Ponencia> findByMagistradoEntre(Long magistradoUsuarioId,
                                                             LocalDateTime desde, LocalDateTime hasta) {
        if (magistradoUsuarioId == null) return List.of();
        return ponencias.values().stream()
                .filter(p -> {
                    Long mag = tocaAMagistrado.apply(p.getTocaId());
                    return magistradoUsuarioId.equals(mag);
                })
                .filter(p -> p.getFechaHora() != null
                        && !p.getFechaHora().isBefore(desde)
                        && !p.getFechaHora().isAfter(hasta))
                .toList();
    }

    @Override
    public synchronized boolean tienePonenciaHoy(Long tocaId) {
        LocalDate hoy = LocalDate.now();
        return ponencias.values().stream()
                .anyMatch(p -> tocaId.equals(p.getTocaId())
                        && p.getFechaHora() != null
                        && p.getFechaHora().toLocalDate().equals(hoy)
                        && p.getEstado() == mx.edu.utez.sij.segunda.model.EstadoPonencia.PROGRAMADA);
    }

    @Override
    public synchronized List<Ponencia> entre(LocalDateTime desde, LocalDateTime hasta) {
        return ponencias.values().stream()
                .filter(p -> p.getFechaHora() != null
                        && !p.getFechaHora().isBefore(desde)
                        && !p.getFechaHora().isAfter(hasta))
                .sorted(Comparator.comparing(Ponencia::getFechaHora))
                .toList();
    }
}


