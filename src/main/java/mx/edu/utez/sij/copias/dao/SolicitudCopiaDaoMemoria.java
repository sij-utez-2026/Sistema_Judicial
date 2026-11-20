package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.nucleo.web.Pagina;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** TEMPORAL: ver DaoMemoria. */
public class SolicitudCopiaDaoMemoria extends DaoMemoria<SolicitudCopia> implements SolicitudCopiaDao {

    @Override
    protected Long idDe(SolicitudCopia registro) {
        return registro.getId();
    }

    @Override
    protected void asignarId(SolicitudCopia registro, Long id) {
        registro.setId(id);
    }

    @Override
    public boolean existsByFolio(String folio) {
        return !filtrar(s -> s.getFolio().equalsIgnoreCase(folio)).isEmpty();
    }

    @Override
    public long countByEstado(EstadoSolicitud estado) {
        return filtrar(s -> s.getEstado() == estado).size();
    }

    @Override
    public List<SolicitudCopia> findByEstadoInOrderByFechaSolicitudAsc(Collection<EstadoSolicitud> estados) {
        return filtrar(s -> estados.contains(s.getEstado())).stream()
                .sorted(Comparator.comparing(SolicitudCopia::getFechaSolicitud))
                .toList();
    }

    @Override
    public Pagina<SolicitudCopia> buscar(String texto, EstadoSolicitud estado, int pagina, int tamano) {
        String t = texto == null ? null : texto.toLowerCase(Locale.ROOT);
        List<SolicitudCopia> encontradas = filtrar(s -> t == null
                || s.getFolio().toLowerCase(Locale.ROOT).contains(t)
                || s.getNumeroExpediente().toLowerCase(Locale.ROOT).contains(t)
                || s.getSolicitanteNombre().toLowerCase(Locale.ROOT).contains(t))
                .stream()
                .filter(s -> estado == null || s.getEstado() == estado)
                .sorted(Comparator.comparing(SolicitudCopia::getFechaSolicitud).reversed())
                .toList();
        return Pagina.de(encontradas, pagina, tamano);
    }
}
