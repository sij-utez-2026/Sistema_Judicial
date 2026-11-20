package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.Tarifa;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

/** TEMPORAL: ver DaoMemoria. */
public class TarifaDaoMemoria extends DaoMemoria<Tarifa> implements TarifaDao {

    @Override
    protected Long idDe(Tarifa registro) {
        return registro.getId();
    }

    @Override
    protected void asignarId(Tarifa registro, Long id) {
        registro.setId(id);
    }

    @Override
    public Optional<Tarifa> findVigente(LocalDate fecha) {
        return filtrar(t -> !t.getVigenteDesde().isAfter(fecha)).stream()
                .max(Comparator.comparing(Tarifa::getVigenteDesde));
    }
}
