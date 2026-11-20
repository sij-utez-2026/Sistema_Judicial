package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.Entrega;

import java.util.Optional;

/** TEMPORAL: ver DaoMemoria. */
public class EntregaDaoMemoria extends DaoMemoria<Entrega> implements EntregaDao {

    @Override
    protected Long idDe(Entrega registro) {
        return registro.getId();
    }

    @Override
    protected void asignarId(Entrega registro, Long id) {
        registro.setId(id);
    }

    @Override
    public Optional<Entrega> findBySolicitudId(Long solicitudId) {
        return filtrar(e -> e.getSolicitudId().equals(solicitudId)).stream().findFirst();
    }
}
