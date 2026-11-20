package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.CorreoEnviado;

import java.util.Comparator;
import java.util.List;

/** TEMPORAL: ver DaoMemoria. */
public class CorreoEnviadoDaoMemoria extends DaoMemoria<CorreoEnviado> implements CorreoEnviadoDao {

    @Override
    protected Long idDe(CorreoEnviado registro) {
        return registro.getId();
    }

    @Override
    protected void asignarId(CorreoEnviado registro, Long id) {
        registro.setId(id);
    }

    @Override
    public List<CorreoEnviado> findBySolicitudIdOrderByEnviadoEnDesc(Long solicitudId) {
        return filtrar(c -> c.getSolicitudId().equals(solicitudId)).stream()
                .sorted(Comparator.comparing(CorreoEnviado::getEnviadoEn).thenComparing(CorreoEnviado::getId).reversed())
                .toList();
    }
}
