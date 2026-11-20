package mx.edu.utez.sij.oficialia.model.dao;


import mx.edu.utez.sij.oficialia.model.Materia;
import mx.edu.utez.sij.oficialia.utils.InMemoryDataStore;

import java.util.ArrayList;
import java.util.List;

public class MateriaDao implements Dao<Materia> {

    private final InMemoryDataStore dataStore = InMemoryDataStore.getInstance();

    @Override
    public List<Materia> getAll() {
        return new ArrayList<>(dataStore.getMaterias());
    }

    @Override
    public Materia getById(String clave) {
        if (clave == null) return null;
        for (Materia m : dataStore.getMaterias()) {
            if (m.getClave().equalsIgnoreCase(clave.trim())) {
                return m;
            }
        }
        return null;
    }

    @Override
    public boolean save(Materia item) {
        if (item == null || item.getClave() == null) return false;
        if (getById(item.getClave()) != null) {
            return false;
        }
        return dataStore.getMaterias().add(item);
    }

    @Override
    public boolean update(Materia item) {
        if (item == null || item.getClave() == null) return false;
        List<Materia> list = dataStore.getMaterias();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getClave().equalsIgnoreCase(item.getClave().trim())) {
                list.set(i, item);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(String clave) {
        if (clave == null) return false;
        return dataStore.getMaterias().removeIf(m -> m.getClave().equalsIgnoreCase(clave.trim()));
    }
}

