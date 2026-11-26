package mx.edu.utez.sij.oficialia.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.oficialia.model.Juzgado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class JuzgadoDao {

    private static final String SELECT =
            "SELECT j.clave_juzgado, j.nombre, j.estado, j.clave_distrito, d.nombre AS nombre_distrito"
                    + " FROM juzgado_Ofc j"
                    + " JOIN distrito_Ofc d ON d.clave_distrito = j.clave_distrito";

    public List<Juzgado> listar(String q) {
        StringBuilder sql = new StringBuilder(SELECT);
        boolean tieneFiltro = q != null && !q.isBlank();
        if (tieneFiltro) {
            sql.append(" WHERE LOWER(j.clave_juzgado) LIKE ? OR LOWER(j.nombre) LIKE ? OR LOWER(d.nombre) LIKE ? OR LOWER(j.estado) LIKE ?");
        }
        sql.append(" ORDER BY j.clave_juzgado ASC");

        List<Juzgado> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (tieneFiltro) {
                String patron = "%" + q.trim().toLowerCase() + "%";
                ps.setString(1, patron);
                ps.setString(2, patron);
                ps.setString(3, patron);
                ps.setString(4, patron);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leer(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los juzgados", e);
        }
    }

    public List<Juzgado> listarActivos() {
        String sql = SELECT + " WHERE j.estado = 'ACTIVO' ORDER BY j.nombre ASC";
        List<Juzgado> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(leer(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los juzgados activos", e);
        }
    }

    public int contar() {
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM juzgado_Ofc");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo contar los juzgados", e);
        }
    }

    public Juzgado getById(String clave) {
        if (clave == null || clave.isBlank()) {
            return null;
        }
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE j.clave_juzgado = ?")) {
            ps.setString(1, clave.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el juzgado", e);
        }
    }

    public String crear(Juzgado juzgado) {
        if (juzgado.getClave() == null || juzgado.getClave().isBlank()) {
            return "La clave del juzgado es obligatoria.";
        }
        if (juzgado.getClave().trim().length() > 10) {
            return "La clave del juzgado no debe exceder 10 caracteres.";
        }
        if (juzgado.getNombre() == null || juzgado.getNombre().isBlank()) {
            return "El nombre del juzgado es obligatorio.";
        }
        if (juzgado.getClaveDistrito() == null || juzgado.getClaveDistrito().isBlank()) {
            return "Seleccione un distrito válido.";
        }
        String clave = juzgado.getClave().trim().toUpperCase();
        if (getById(clave) != null) {
            return "Ya existe un juzgado registrado con la clave " + clave + ".";
        }
        String sql = "INSERT INTO juzgado_Ofc (clave_juzgado, nombre, estado, clave_distrito) VALUES (?, ?, 'ACTIVO', ?)";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setString(2, juzgado.getNombre().trim());
            ps.setString(3, juzgado.getClaveDistrito().trim());
            ps.executeUpdate();
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "No se pudo registrar el juzgado: verifique que la clave no esté duplicada y el distrito exista.";
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar el juzgado", e);
        }
    }

    public String actualizar(Juzgado juzgado) {
        if (juzgado.getClave() == null || juzgado.getClave().isBlank()) {
            return "No se especificó la clave del juzgado.";
        }
        if (juzgado.getNombre() == null || juzgado.getNombre().isBlank()) {
            return "El nombre del juzgado es obligatorio.";
        }
        if (juzgado.getClaveDistrito() == null || juzgado.getClaveDistrito().isBlank()) {
            return "Seleccione un distrito válido.";
        }
        String sql = "UPDATE juzgado_Ofc SET nombre = ?, clave_distrito = ? WHERE clave_juzgado = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, juzgado.getNombre().trim());
            ps.setString(2, juzgado.getClaveDistrito().trim());
            ps.setString(3, juzgado.getClave().trim());
            if (ps.executeUpdate() == 0) {
                return "No se encontró el juzgado a actualizar.";
            }
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "El distrito seleccionado no existe.";
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el juzgado", e);
        }
    }

    public String toggleEstado(String clave) {
        Juzgado actual = getById(clave);
        if (actual == null) {
            return "No se encontró el juzgado especificado.";
        }
        String nuevoEstado = actual.isActivo() ? "INACTIVO" : "ACTIVO";
        String sql = "UPDATE juzgado_Ofc SET estado = ? WHERE clave_juzgado = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, actual.getClave());
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cambiar el estado del juzgado", e);
        }
    }

    public String eliminar(String clave) {
        if (clave == null || clave.isBlank()) {
            return "No se especificó el juzgado a eliminar.";
        }
        String sql = "DELETE FROM juzgado_Ofc WHERE clave_juzgado = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave.trim());
            if (ps.executeUpdate() == 0) {
                return "No se encontró el juzgado a eliminar.";
            }
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "No se puede eliminar el juzgado porque tiene oficialías o mesas asociadas; puedes desactivarlo con el interruptor.";
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el juzgado", e);
        }
    }

    private Juzgado leer(ResultSet rs) throws SQLException {
        return new Juzgado(
                rs.getString("clave_juzgado"),
                rs.getString("nombre"),
                rs.getString("clave_distrito"),
                rs.getString("nombre_distrito"),
                rs.getString("estado")
        );
    }
}
