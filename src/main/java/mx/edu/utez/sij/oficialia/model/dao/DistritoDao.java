package mx.edu.utez.sij.oficialia.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.oficialia.model.Distrito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class DistritoDao {

    public List<Distrito> listar(String q) {
        StringBuilder sql = new StringBuilder(
                "SELECT clave_distrito, nombre, cabecera, estado FROM distrito_Ofc");
        boolean tieneFiltro = q != null && !q.isBlank();
        if (tieneFiltro) {
            sql.append(" WHERE LOWER(clave_distrito) LIKE ? OR LOWER(nombre) LIKE ? OR LOWER(COALESCE(cabecera, '')) LIKE ? OR LOWER(estado) LIKE ?");
        }
        sql.append(" ORDER BY clave_distrito ASC");

        List<Distrito> lista = new ArrayList<>();
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
            throw new IllegalStateException("No se pudieron consultar los distritos", e);
        }
    }

    public List<Distrito> listarActivos() {
        String sql = "SELECT clave_distrito, nombre, cabecera, estado FROM distrito_Ofc WHERE estado = 'ACTIVO' ORDER BY nombre ASC";
        List<Distrito> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(leer(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los distritos activos", e);
        }
    }

    public int contar() {
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM distrito_Ofc");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo contar los distritos", e);
        }
    }

    public Distrito getById(String clave) {
        if (clave == null || clave.isBlank()) {
            return null;
        }
        String sql = "SELECT clave_distrito, nombre, cabecera, estado FROM distrito_Ofc WHERE clave_distrito = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el distrito", e);
        }
    }

    public String crear(Distrito distrito) {
        if (distrito.getClave() == null || distrito.getClave().isBlank()) {
            return "La clave del distrito es obligatoria.";
        }
        if (distrito.getClave().trim().length() > 10) {
            return "La clave del distrito no debe exceder 10 caracteres.";
        }
        if (distrito.getNombre() == null || distrito.getNombre().isBlank()) {
            return "El nombre del distrito es obligatorio.";
        }
        String clave = distrito.getClave().trim().toUpperCase();
        if (getById(clave) != null) {
            return "Ya existe un distrito registrado con la clave " + clave + ".";
        }
        String sql = "INSERT INTO distrito_Ofc (clave_distrito, nombre, cabecera, estado) VALUES (?, ?, ?, 'ACTIVO')";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setString(2, distrito.getNombre().trim());
            ps.setString(3, vacioANull(distrito.getCabecera()));
            ps.executeUpdate();
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "Ya existe un distrito registrado con la clave " + clave + ".";
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar el distrito", e);
        }
    }

    public String actualizar(Distrito distrito) {
        if (distrito.getClave() == null || distrito.getClave().isBlank()) {
            return "No se especificó la clave del distrito.";
        }
        if (distrito.getNombre() == null || distrito.getNombre().isBlank()) {
            return "El nombre del distrito es obligatorio.";
        }
        String sql = "UPDATE distrito_Ofc SET nombre = ?, cabecera = ? WHERE clave_distrito = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, distrito.getNombre().trim());
            ps.setString(2, vacioANull(distrito.getCabecera()));
            ps.setString(3, distrito.getClave().trim());
            if (ps.executeUpdate() == 0) {
                return "No se encontró el distrito a actualizar.";
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el distrito", e);
        }
    }

    public String toggleEstado(String clave) {
        Distrito actual = getById(clave);
        if (actual == null) {
            return "No se encontró el distrito especificado.";
        }
        String nuevoEstado = actual.isActivo() ? "INACTIVO" : "ACTIVO";
        String sql = "UPDATE distrito_Ofc SET estado = ? WHERE clave_distrito = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, actual.getClave());
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cambiar el estado del distrito", e);
        }
    }

    public String eliminar(String clave) {
        if (clave == null || clave.isBlank()) {
            return "No se especificó el distrito a eliminar.";
        }
        String sql = "DELETE FROM distrito_Ofc WHERE clave_distrito = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave.trim());
            if (ps.executeUpdate() == 0) {
                return "No se encontró el distrito a eliminar.";
            }
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "No se puede eliminar el distrito porque tiene juzgados o registros asociados; puedes desactivarlo con el interruptor.";
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el distrito", e);
        }
    }

    private Distrito leer(ResultSet rs) throws SQLException {
        return new Distrito(
                rs.getString("clave_distrito"),
                rs.getString("nombre"),
                rs.getString("cabecera"),
                rs.getString("estado")
        );
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
