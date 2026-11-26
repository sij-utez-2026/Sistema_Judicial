package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.Sala;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO MySQL para sala_Seg; la asignación de magistrado se deriva de magistrado_Seg.id_sala. */
public class SalaDaoMySql implements SalaDao {
    private static final String BASE = "SELECT s.id_sala, s.nombre, s.clave_distrito, s.direccion, s.estatus, "
            + "s.fecha_creacion, m.id_usuario AS magistrado_usuario_id, "
            + "CONCAT_WS(' ', u.nombre, u.apellido_paterno, u.apellido_materno) AS magistrado_nombre, "
            + "d.nombre AS distrito_nombre FROM sala_Seg s "
            + "JOIN distrito_Ofc d ON d.id_distrito = s.clave_distrito "
            + "LEFT JOIN magistrado_Seg m ON m.id_sala = s.id_sala AND m.estatus = TRUE "
            + "LEFT JOIN user_Seg u ON u.id_usuario = m.id_usuario ";

    @Override public Optional<Sala> findById(Long id) {
        if (id == null) return Optional.empty();
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(BASE + "WHERE s.id_sala = ?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(map(r)) : Optional.empty(); }
        } catch (SQLException e) { throw DaoJdbcSupport.error("buscar sala", e); }
    }

    @Override public Sala save(Sala sala) {
        String sql = sala.getId() == null
                ? "INSERT INTO sala_Seg (clave_distrito, nombre, direccion, estatus) VALUES (?, ?, ?, ?)"
                : "UPDATE sala_Seg SET clave_distrito = ?, nombre = ?, direccion = ?, estatus = ? WHERE id_sala = ?";
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(sql,
                sala.getId() == null ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
            p.setString(1, sala.getDistritoId());
            p.setString(2, sala.getNombre());
            p.setString(3, sala.getDireccion());
            p.setBoolean(4, sala.isActiva());
            if (sala.getId() != null) p.setLong(5, sala.getId());
            if (p.executeUpdate() != 1) throw new IllegalStateException("La sala no existe o no pudo guardarse");
            if (sala.getId() == null) try (ResultSet k = p.getGeneratedKeys()) { if (k.next()) sala.setId(k.getLong(1)); }
            return findById(sala.getId()).orElseThrow(() -> new IllegalStateException("No se pudo releer la sala guardada"));
        } catch (SQLException e) { throw DaoJdbcSupport.error("guardar sala", e); }
    }

    @Override public long count() { return contar("SELECT COUNT(*) FROM sala_Seg"); }
    @Override public long countByActiva(boolean activa) {
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM sala_Seg WHERE estatus = ?")) {
            p.setBoolean(1, activa); try (ResultSet r = p.executeQuery()) { return r.next() ? r.getLong(1) : 0; }
        } catch (SQLException e) { throw DaoJdbcSupport.error("contar salas", e); }
    }

    @Override public boolean existsByNombreAndDistrito(String nombre, String distritoId, Long excluirId) {
        String sql = "SELECT 1 FROM sala_Seg WHERE LOWER(nombre) = LOWER(?) AND clave_distrito = ? AND (? IS NULL OR id_sala <> ?) LIMIT 1";
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, nombre); p.setString(2, distritoId); if (excluirId == null) p.setNull(3, Types.BIGINT); else p.setLong(3, excluirId);
            if (excluirId == null) p.setNull(4, Types.BIGINT); else p.setLong(4, excluirId);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        } catch (SQLException e) { throw DaoJdbcSupport.error("verificar nombre de sala", e); }
    }

    @Override public boolean existsByMagistradoUsuarioId(Long usuarioId, Long excluirSalaId) {
        if (usuarioId == null) return false;
        String sql = "SELECT 1 FROM magistrado_Seg WHERE id_usuario = ? AND id_sala IS NOT NULL AND (? IS NULL OR id_sala <> ?) LIMIT 1";
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, usuarioId); if (excluirSalaId == null) p.setNull(2, Types.BIGINT); else p.setLong(2, excluirSalaId);
            if (excluirSalaId == null) p.setNull(3, Types.BIGINT); else p.setLong(3, excluirSalaId);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        } catch (SQLException e) { throw DaoJdbcSupport.error("verificar asignación de sala", e); }
    }

    @Override public Pagina<Sala> buscar(String texto, String distritoId, Boolean activa, int pagina, int tamano) {
        String where = " WHERE (? IS NULL OR LOWER(s.nombre) LIKE ?) AND (? IS NULL OR s.clave_distrito = ?) AND (? IS NULL OR s.estatus = ?)";
        String sql = BASE + where + " ORDER BY s.nombre LIMIT ? OFFSET ?";
        String t = texto == null ? null : "%" + texto.toLowerCase() + "%";
        List<Sala> lista = new ArrayList<>(); long total;
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, t); p.setString(2, t); p.setString(3, distritoId); p.setString(4, distritoId);
            setBooleanOrNull(p, 5, activa); setBooleanOrNull(p, 6, activa); p.setInt(7, tamano); p.setInt(8, Pagina.offset(pagina, tamano));
            try (ResultSet r = p.executeQuery()) { while (r.next()) lista.add(map(r)); }
            try (PreparedStatement cp = c.prepareStatement("SELECT COUNT(*) FROM sala_Seg s" + where)) {
                cp.setString(1, t); cp.setString(2, t); cp.setString(3, distritoId); cp.setString(4, distritoId);
                setBooleanOrNull(cp, 5, activa); setBooleanOrNull(cp, 6, activa); try (ResultSet r = cp.executeQuery()) { total = r.next() ? r.getLong(1) : 0; }
            }
            return new Pagina<>(lista, Math.max(pagina, 0), tamano, total);
        } catch (SQLException e) { throw DaoJdbcSupport.error("listar salas", e); }
    }

    @Override public List<Sala> activas() {
        List<Sala> lista = new ArrayList<>();
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(BASE + "WHERE s.estatus = TRUE ORDER BY s.nombre"); ResultSet r = p.executeQuery()) {
            while (r.next()) lista.add(map(r)); return List.copyOf(lista);
        } catch (SQLException e) { throw DaoJdbcSupport.error("listar salas activas", e); }
    }

    @Override public boolean tieneTocasPendientes(Long salaId) {
        if (salaId == null) return false;
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement("SELECT 1 FROM toca_Seg WHERE id_sala = ? AND estado <> 'Resuelto' LIMIT 1")) {
            p.setLong(1, salaId); try (ResultSet r = p.executeQuery()) { return r.next(); }
        } catch (SQLException e) { throw DaoJdbcSupport.error("consultar tocas pendientes por sala", e); }
    }

    private long contar(String sql) { try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) { return r.next() ? r.getLong(1) : 0; } catch (SQLException e) { throw DaoJdbcSupport.error("contar salas", e); } }
    private static void setBooleanOrNull(PreparedStatement p, int i, Boolean v) throws SQLException { if (v == null) p.setNull(i, Types.BOOLEAN); else p.setBoolean(i, v); }
    private static Sala map(ResultSet r) throws SQLException {
        Sala s = new Sala(); s.setId(r.getLong("id_sala")); s.setNombre(r.getString("nombre")); s.setDistritoId(r.getString("clave_distrito")); s.setDireccion(r.getString("direccion"));
        s.setActiva(r.getBoolean("estatus")); var ts = r.getTimestamp("fecha_creacion"); if (ts != null) s.setCreadaEn(ts.toLocalDateTime());
        long mid = r.getLong("magistrado_usuario_id"); s.setMagistradoUsuarioId(r.wasNull() ? null : mid); s.setMagistradoNombre(r.getString("magistrado_nombre")); s.setDistritoNombre(r.getString("distrito_nombre")); return s;
    }
}
