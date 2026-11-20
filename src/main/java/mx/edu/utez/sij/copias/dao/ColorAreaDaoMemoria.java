package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.ColorArea;

import java.util.Optional;

/** TEMPORAL: ver DaoMemoria. */
public class ColorAreaDaoMemoria extends DaoMemoria<ColorArea> implements ColorAreaDao {

    @Override
    protected Long idDe(ColorArea registro) {
        return registro.getId();
    }

    @Override
    protected void asignarId(ColorArea registro, Long id) {
        registro.setId(id);
    }

    @Override
    public Optional<ColorArea> findByVariable(String variable) {
        return filtrar(c -> c.getVariable().equals(variable)).stream().findFirst();
    }
}
