package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Lectura de los catálogos oficiales compartidos por los módulos. */
public class CatalogoTemporalSegunda {
    public record DistritoDto(String clave, String nombre) { }
    public record MateriaDto(String clave, String nombre) { }
    public record JuzgadoDto(String clave, String nombre, String materiaClave, String distritoClave) { }

    public List<DistritoDto> distritosActivos() {
        String sql = "SELECT id_distrito, nombre FROM distrito_Ofc WHERE estado = 'ACTIVO' ORDER BY nombre";
        List<DistritoDto> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultado.add(new DistritoDto(rs.getString(1), rs.getString(2)));
            return List.copyOf(resultado);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los distritos activos", e);
        }
    }

    public Optional<DistritoDto> distrito(String clave) {
        if (clave == null || clave.isBlank()) return Optional.empty();
        String sql = "SELECT id_distrito, nombre FROM distrito_Ofc WHERE id_distrito = ? AND estado = 'ACTIVO'";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new DistritoDto(rs.getString(1), rs.getString(2))) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el distrito", e);
        }
    }

    public List<MateriaDto> materiasActivas() {
        String sql = "SELECT id_materia, nombre FROM materia_Ofc WHERE estado = 'ACTIVO' ORDER BY nombre";
        List<MateriaDto> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultado.add(new MateriaDto(rs.getString(1), rs.getString(2)));
            return List.copyOf(resultado);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar las materias activas", e);
        }
    }

    public Optional<MateriaDto> materia(String clave) {
        if (clave == null || clave.isBlank()) return Optional.empty();
        String sql = "SELECT id_materia, nombre FROM materia_Ofc WHERE id_materia = ? AND estado = 'ACTIVO'";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new MateriaDto(rs.getString(1), rs.getString(2))) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar la materia", e);
        }
    }

    public List<JuzgadoDto> juzgados(String distritoClave) {
        String sql = "SELECT id_juzgado, nombre, id_distrito FROM juzgado_Ofc "
                + "WHERE estado = 'ACTIVO' AND (? IS NULL OR id_distrito = ?) ORDER BY nombre";
        List<JuzgadoDto> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, distritoClave);
            ps.setString(2, distritoClave);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(new JuzgadoDto(rs.getString(1), rs.getString(2), null, rs.getString(3)));
            }
            return List.copyOf(resultado);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los juzgados activos", e);
        }
    }
}
