package mx.edu.utez.sij.oficialia.model.dao;



import mx.edu.utez.sij.oficialia.model.Distrito;
import mx.edu.utez.sij.oficialia.utils.InMemoryDataStore;

import java.util.ArrayList;
import java.util.List;

public class DistritoDao implements Dao<Distrito> {

    private final InMemoryDataStore dataStore = InMemoryDataStore.getInstance();

    @Override
    public List<Distrito> getAll() {
        // ==========================================================
        // FUTURO SQL CON BASE DE DATOS:
        // List<Distrito> list = new ArrayList<>();
        // String sql = "SELECT clave, nombre, cabecera, municipios FROM distritos ORDER BY clave ASC";
        // try (Connection con = SQLConnector.getConnection();
        //      PreparedStatement ps = con.prepareStatement(sql);
        //      ResultSet rs = ps.executeQuery()) {
        //     while (rs.next()) {
        //         list.add(new Distrito(rs.getString("clave"), rs.getString("nombre"), rs.getString("cabecera"), rs.getString("municipios")));
        //     }
        // } catch (SQLException e) { e.printStackTrace(); }
        // return list;
        // ==========================================================
        return new ArrayList<>(dataStore.getDistritos());
    }

    @Override
    public Distrito getById(String clave) {
        if (clave == null) return null;
        for (Distrito d : dataStore.getDistritos()) {
            if (d.getClave().equalsIgnoreCase(clave.trim())) {
                return d;
            }
        }
        return null;
    }

    @Override
    public boolean save(Distrito item) {
        if (item == null || item.getClave() == null) return false;
        // Evitar duplicados por clave
        if (getById(item.getClave()) != null) {
            return false;
        }
        return dataStore.getDistritos().add(item);
    }

    @Override
    public boolean update(Distrito item) {
        if (item == null || item.getClave() == null) return false;
        List<Distrito> list = dataStore.getDistritos();
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
        return dataStore.getDistritos().removeIf(d -> d.getClave().equalsIgnoreCase(clave.trim()));
    }
}

