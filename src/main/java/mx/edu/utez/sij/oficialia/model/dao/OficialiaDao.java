package mx.edu.utez.sij.oficialia.model.dao;


import mx.edu.utez.sij.oficialia.model.Oficialia;
import mx.edu.utez.sij.oficialia.utils.InMemoryDataStore;

import java.util.ArrayList;
import java.util.List;

public class OficialiaDao implements Dao<Oficialia> {

    private final InMemoryDataStore dataStore = InMemoryDataStore.getInstance();

    @Override
    public List<Oficialia> getAll() {
        return new ArrayList<>(dataStore.getOficialias());
    }

    @Override
    public Oficialia getById(String clave) {
        if (clave == null) return null;
        for (Oficialia o : dataStore.getOficialias()) {
            if (o.getClave().equalsIgnoreCase(clave.trim())) {
                return o;
            }
        }
        return null;
    }

    @Override
    public boolean save(Oficialia item) {
        if (item == null || item.getClave() == null) return false;
        if (getById(item.getClave()) != null) {
            return false;
        }
        return dataStore.getOficialias().add(item);
    }

    @Override
    public boolean update(Oficialia item) {
        if (item == null || item.getClave() == null) return false;
        List<Oficialia> list = dataStore.getOficialias();
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
        return dataStore.getOficialias().removeIf(o -> o.getClave().equalsIgnoreCase(clave.trim()));
    }
}

