package mx.edu.utez.sij.oficialia.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.oficialia.model.Materia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class MateriaDao {

    public List<Materia> listar(String q) {
        StringBuilder sql = new StringBuilder(
                "SELECT clave_materia, nombre, descripcion, estado FROM materia_Ofc");
        boolean tieneFiltro = q != null && !q.isBlank();
        if (tieneFiltro) {
            sql.append(" WHERE LOWER(clave_materia) LIKE ? OR LOWER(nombre) LIKE ? OR LOWER(COALESCE(descripcion, '')) LIKE ? OR LOWER(estado) LIKE ?");
        }
        sql.append(" ORDER BY clave_materia ASC");

        List<Materia> lista = new ArrayList<>();
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
            throw new IllegalStateException("No se pudieron consultar las materias", e);
        }
    }

    public int contar() {
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM materia_Ofc");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo contar las materias", e);
        }
    }

    public Materia getById(String clave) {
        if (clave == null || clave.isBlank()) {
            return null;
        }
        String sql = "SELECT clave_materia, nombre, descripcion, estado FROM materia_Ofc WHERE clave_materia = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar la materia", e);
        }
    }

    public String crear(Materia materia) {
        if (materia.getClave() == null || materia.getClave().isBlank()) {
            return "La clave de la materia es obligatoria.";
        }
        if (materia.getClave().trim().length() > 10) {
            return "La clave de la materia no debe exceder 10 caracteres.";
        }
        if (materia.getNombre() == null || materia.getNombre().isBlank()) {
            return "El nombre de la materia es obligatorio.";
        }
        String clave = materia.getClave().trim().toUpperCase();
        if (getById(clave) != null) {
            return "Ya existe una materia registrada con la clave " + clave + ".";
        }
        String sql = "INSERT INTO materia_Ofc (clave_materia, nombre, descripcion, estado) VALUES (?, ?, ?, 'ACTIVO')";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setString(2, materia.getNombre().trim());
            ps.setString(3, vacioANull(materia.getDescripcion()));
            ps.executeUpdate();
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "Ya existe una materia registrada con esa clave o nombre.";
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar la materia", e);
        }
    }

    public String actualizar(Materia materia) {
        if (materia.getClave() == null || materia.getClave().isBlank()) {
            return "No se especificó la clave de la materia.";
        }
        if (materia.getNombre() == null || materia.getNombre().isBlank()) {
            return "El nombre de la materia es obligatorio.";
        }
        String sql = "UPDATE materia_Ofc SET nombre = ?, descripcion = ? WHERE clave_materia = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, materia.getNombre().trim());
            ps.setString(2, vacioANull(materia.getDescripcion()));
            ps.setString(3, materia.getClave().trim());
            if (ps.executeUpdate() == 0) {
                return "No se encontró la materia a actualizar.";
            }
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "Ya existe otra materia con ese nombre.";
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar la materia", e);
        }
    }

    public String toggleEstado(String clave) {
        Materia actual = getById(clave);
        if (actual == null) {
            return "No se encontró la materia especificada.";
        }
        String nuevoEstado = actual.isActivo() ? "INACTIVO" : "ACTIVO";
        String sql = "UPDATE materia_Ofc SET estado = ? WHERE clave_materia = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, actual.getClave());
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cambiar el estado de la materia", e);
        }
    }

    public String eliminar(String clave) {
        if (clave == null || clave.isBlank()) {
            return "No se especificó la materia a eliminar.";
        }
        String sql = "DELETE FROM materia_Ofc WHERE clave_materia = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave.trim());
            if (ps.executeUpdate() == 0) {
                return "No se encontró la materia a eliminar.";
            }
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "No se puede eliminar la materia porque está asociada a demandas, mesas o peritos; puedes desactivarla con el interruptor.";
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar la materia", e);
        }
    }

    private Materia leer(ResultSet rs) throws SQLException {
        return new Materia(
                rs.getString("clave_materia"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getString("estado")
        );
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
