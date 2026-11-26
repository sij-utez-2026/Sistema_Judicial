package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.EstadoResolucion;
import mx.edu.utez.sij.segunda.model.Resolucion;
import mx.edu.utez.sij.segunda.model.Toca;

import java.util.*;
import java.util.function.Function;

public class ResolucionDaoMemoria implements ResolucionDao {

    private final Map<Long, Resolucion> resoluciones = new LinkedHashMap<>();
    private long siguienteId = 1;

    private final Function<Long, Toca> tocaResolver;

    public ResolucionDaoMemoria(Function<Long, Toca> tocaResolver) {
        this.tocaResolver = tocaResolver;
    }

    @Override
    public synchronized Optional<Resolucion> findById(Long id) {
        return Optional.ofNullable(resoluciones.get(id));
    }

    @Override
    public synchronized Resolucion save(Resolucion resolucion) {
        if (resolucion.getId() == null) {
            resolucion.setId(siguienteId++);
        }
        resoluciones.put(resolucion.getId(), resolucion);
        return resolucion;
    }

    @Override
    public synchronized long count() {
        return resoluciones.size();
    }

    @Override
    public synchronized List<Resolucion> findByTocaId(Long tocaId) {
        return resoluciones.values().stream()
                .filter(r -> tocaId.equals(r.getTocaId()))
                .sorted(Comparator.comparing(Resolucion::getFecha,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @Override
    public synchronized Optional<Resolucion> findVigenteByTocaId(Long tocaId) {
        return resoluciones.values().stream()
                .filter(r -> tocaId.equals(r.getTocaId()))
                .filter(r -> r.getEstado() == EstadoResolucion.VIGENTE)
                .findFirst();
    }

    @Override
    public synchronized Pagina<Resolucion> buscar(String texto, EstadoResolucion estado, Long salaId,
                                                  Long soloDeMagistradoUsuarioId,
                                                  int pagina, int tamano) {
        String t = (texto == null) ? null : texto.toLowerCase();

        List<Resolucion> filtradas = resoluciones.values().stream()
                .filter(r -> {
                    Toca toca = tocaResolver.apply(r.getTocaId());
                    if (toca == null) return false;
                    if (t != null && !toca.getFolioToca().toLowerCase().contains(t)
                            && !toca.getNumeroExpediente().toLowerCase().contains(t)) {
                        return false;
                    }
                    if (salaId != null && !salaId.equals(toca.getSalaId())) return false;
                    if (soloDeMagistradoUsuarioId != null
                            && !soloDeMagistradoUsuarioId.equals(toca.getMagistradoUsuarioId())) {
                        return false;
                    }
                    return true;
                })
                .filter(r -> estado == null || r.getEstado() == estado)
                .sorted(Comparator.comparing(Resolucion::getFecha,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        return Pagina.de(filtradas, pagina, tamano);
    }
}


