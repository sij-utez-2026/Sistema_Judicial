package mx.edu.utez.sij.oficialia.model.dao;


import mx.edu.utez.sij.oficialia.model.Usuario;
import mx.edu.utez.sij.oficialia.utils.InMemoryDataStore;

import java.util.ArrayList;
import java.util.List;

public class UsuarioDao implements Dao<Usuario> {

    private final InMemoryDataStore dataStore = InMemoryDataStore.getInstance();

    @Override
    public List<Usuario> getAll() {
        // ==========================================================
        // FUTURO SQL CON BASE DE DATOS:
        // List<Usuario> list = new ArrayList<>();
        // String sql = "SELECT * FROM usuarios";
        // try (Connection con = SQLConnector.getConnection();
        //      PreparedStatement ps = con.prepareStatement(sql);
        //      ResultSet rs = ps.executeQuery()) { ... }
        // ==========================================================
        return new ArrayList<>(dataStore.getUsuarios());
    }

    @Override
    public Usuario getById(String id) {
        try {
            int intId = Integer.parseInt(id);
            for (Usuario u : dataStore.getUsuarios()) {
                if (u.getId() == intId) {
                    return u;
                }
            }
        } catch (NumberFormatException ignored) {}
        return null;
    }

    public Usuario autenticar(String correo, String contrasena) {
        if (correo == null || contrasena == null) return null;
        for (Usuario u : dataStore.getUsuarios()) {
            if (u.getCorreo().equalsIgnoreCase(correo.trim()) && u.getContrasena().equals(contrasena)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public boolean save(Usuario item) {
        if (item == null) return false;
        item.setId(dataStore.getUsuarios().size() + 1);
        return dataStore.getUsuarios().add(item);
    }

    @Override
    public boolean update(Usuario item) {
        if (item == null) return false;
        List<Usuario> list = dataStore.getUsuarios();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == item.getId()) {
                list.set(i, item);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        try {
            int intId = Integer.parseInt(id);
            return dataStore.getUsuarios().removeIf(u -> u.getId() == intId);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

