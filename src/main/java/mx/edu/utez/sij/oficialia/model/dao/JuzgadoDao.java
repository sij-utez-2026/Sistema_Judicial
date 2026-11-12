package mx.edu.utez.sij.oficialia.model.dao;




import mx.edu.utez.sij.oficialia.model.Juzgado;
import mx.edu.utez.sij.oficialia.utils.InMemoryDataStore;

import java.util.ArrayList;
import java.util.List;

public class JuzgadoDao implements Dao<Juzgado> {

    private final InMemoryDataStore dataStore = InMemoryDataStore.getInstance();

    @Override
    public List<Juzgado> getAll() {
        // ==========================================================
        // FUTURO SQL CON BASE DE DATOS:
        // List<Juzgado> list = new ArrayList<>();
        // String sql = "SELECT clave, nombre, distrito, materia FROM juzgados.jsp ORDER BY clave ASC";
        // try (Connection con = SQLConnector.getConnection();
        //      PreparedStatement ps = con.prepareStatement(sql);
        //      ResultSet rs = ps.executeQuery()) { ... }
        // ==========================================================
        return new ArrayList<>(dataStore.getJuzgados());
    }

    @Override
    public Juzgado getById(String clave) {
        if (clave == null) return null;
        for (Juzgado j : dataStore.getJuzgados()) {
            if (j.getClave().equalsIgnoreCase(clave.trim())) {
                return j;
            }
        }
        return null;
    }

    @Override
    public boolean save(Juzgado item) {
        if (item == null || item.getClave() == null) return false;
        if (getById(item.getClave()) != null) {
            return false;
        }
        return dataStore.getJuzgados().add(item);
    }

    @Override
    public boolean update(Juzgado item) {
        if (item == null || item.getClave() == null) return false;
        List<Juzgado> list = dataStore.getJuzgados();
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
        return dataStore.getJuzgados().removeIf(j -> j.getClave().equalsIgnoreCase(clave.trim()));
    }
}

