package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.peritajes.model.Materia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Materias del catálogo de Oficialía (tabla materia_Ofc). Peritajes solo las lee.
public class MateriaDao {

    public List<Materia> listarActivas() {
        String sql = "SELECT clave_materia, nombre FROM materia_Ofc WHERE estado = 'ACTIVO' ORDER BY nombre";
        List<Materia> materias = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                materias.add(new Materia(rs.getString("clave_materia"), rs.getString("nombre")));
            }
            return materias;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron consultar las materias", e);
        }
    }

    // true si hay al menos una y todas son materias activas (evita claves alteradas en el formulario)
    public boolean todasExisten(Collection<String> claves) {
        if (claves == null || claves.isEmpty()) {
            return false;
        }
        List<String> activas = listarActivas().stream().map(Materia::getClave).toList();
        return activas.containsAll(claves);
    }
}
