package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.Magistrado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO MySQL para magistrado_Seg unido a la cuenta real de user_Seg. */
public class MagistradoDaoMySql implements MagistradoDao {
    private static final String BASE = "SELECT m.id_magistrado, m.id_usuario, m.id_sala, m.estatus, m.fecha_creacion, "
            + "u.nombre, u.apellido_paterno, u.apellido_materno, u.correo, s.nombre AS sala_nombre "
            + "FROM magistrado_Seg m JOIN user_Seg u ON u.id_usuario = m.id_usuario "
            + "LEFT JOIN sala_Seg s ON s.id_sala = m.id_sala ";

    @Override public Optional<Magistrado> findById(Long id) {
        if (id == null) return Optional.empty();
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(BASE + "WHERE m.id_magistrado = ?")) {
            p.setLong(1, id); try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(map(r)) : Optional.empty(); }
        } catch (SQLException e) { throw DaoJdbcSupport.error("buscar magistrado", e); }
    }

    @Override public Magistrado save(Magistrado m) {
        try (Connection c = DaoJdbcSupport.obtener()) {
            c.setAutoCommit(false);
            try {
                if (m.getId() == null) {
                    if (m.getUsuarioId() == null) {
                        throw new IllegalArgumentException("El magistrado debe vincularse con un usuario real");
                    }
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO magistrado_Seg (id_usuario, id_sala, estatus) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        p.setLong(1, m.getUsuarioId()); setLong(p, 2, m.getSalaId()); p.setBoolean(3, m.isActivo());
                        if (p.executeUpdate() != 1) throw new IllegalStateException("No se pudo registrar el magistrado");
                        try (ResultSet k = p.getGeneratedKeys()) { if (k.next()) m.setId(k.getLong(1)); }
                    }
                } else {
                    try (PreparedStatement p = c.prepareStatement("UPDATE magistrado_Seg SET id_sala = ?, estatus = ? WHERE id_magistrado = ? AND id_usuario = ?")) {
                        if (m.getUsuarioId() == null) throw new IllegalArgumentException("El magistrado no tiene cuenta vinculada");
                        setLong(p, 1, m.getSalaId()); p.setBoolean(2, m.isActivo()); p.setLong(3, m.getId()); p.setLong(4, m.getUsuarioId());
                        if (p.executeUpdate() != 1) throw new IllegalStateException("El registro de magistrado no existe");
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback(); throw e;
            } finally { c.setAutoCommit(true); }
        } catch (SQLException e) { throw DaoJdbcSupport.error("guardar magistrado", e); }
        return findById(m.getId()).orElseThrow(() -> new IllegalStateException("No se pudo releer el magistrado guardado"));
    }

    @Override public long count() { return contar("SELECT COUNT(*) FROM magistrado_Seg"); }
    @Override public long countByActivo(boolean activo) {
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM magistrado_Seg WHERE estatus = ?")) {
            p.setBoolean(1, activo); try (ResultSet r = p.executeQuery()) { return r.next() ? r.getLong(1) : 0; }
        } catch (SQLException e) { throw DaoJdbcSupport.error("contar magistrados", e); }
    }
    @Override public boolean existsByCorreoIgnoreCase(String correo) {
        if (correo == null) return false;
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement("SELECT 1 FROM user_Seg u JOIN magistrado_Seg m ON m.id_usuario = u.id_usuario WHERE LOWER(u.correo) = LOWER(?) LIMIT 1")) {
            p.setString(1, correo.trim()); try (ResultSet r = p.executeQuery()) { return r.next(); }
        } catch (SQLException e) { throw DaoJdbcSupport.error("verificar correo de magistrado", e); }
    }
    @Override public boolean existsBySalaId(Long salaId, Long excluirId) {
        if (salaId == null) return false;
        String sql = "SELECT 1 FROM magistrado_Seg WHERE id_sala = ? AND (? IS NULL OR id_magistrado <> ?) LIMIT 1";
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, salaId); setLong(p, 2, excluirId); setLong(p, 3, excluirId); try (ResultSet r = p.executeQuery()) { return r.next(); }
        } catch (SQLException e) { throw DaoJdbcSupport.error("verificar sala de magistrado", e); }
    }
    @Override public boolean existsByUsuarioId(Long usuarioId) {
        if (usuarioId == null) return false;
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement("SELECT 1 FROM magistrado_Seg WHERE id_usuario = ? LIMIT 1")) {
            p.setLong(1, usuarioId); try (ResultSet r = p.executeQuery()) { return r.next(); }
        } catch (SQLException e) { throw DaoJdbcSupport.error("verificar usuario magistrado", e); }
    }
    @Override public Pagina<Magistrado> buscar(String texto, Boolean activo, int pagina, int tamano) {
        String where = " WHERE (? IS NULL OR LOWER(CONCAT_WS(' ',u.nombre,u.apellido_paterno,u.apellido_materno,u.correo)) LIKE ?) AND (? IS NULL OR m.estatus = ?)";
        String t = texto == null ? null : "%" + texto.toLowerCase() + "%"; List<Magistrado> lista = new ArrayList<>(); long total;
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(BASE + where + " ORDER BY u.nombre,u.apellido_paterno LIMIT ? OFFSET ?")) {
            p.setString(1, t); p.setString(2, t); setBoolean(p, 3, activo); setBoolean(p, 4, activo); p.setInt(5, tamano); p.setInt(6, Pagina.offset(pagina, tamano));
            try (ResultSet r = p.executeQuery()) { while (r.next()) lista.add(map(r)); }
            try (PreparedStatement cp = c.prepareStatement("SELECT COUNT(*) FROM magistrado_Seg m JOIN user_Seg u ON u.id_usuario = m.id_usuario" + where)) {
                cp.setString(1, t); cp.setString(2, t); setBoolean(cp, 3, activo); setBoolean(cp, 4, activo); try (ResultSet r = cp.executeQuery()) { total = r.next() ? r.getLong(1) : 0; }
            }
            return new Pagina<>(lista, Math.max(0, pagina), tamano, total);
        } catch (SQLException e) { throw DaoJdbcSupport.error("listar magistrados", e); }
    }
    @Override public List<Magistrado> activos() {
        List<Magistrado> lista = new ArrayList<>();
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(BASE + "WHERE m.estatus = TRUE AND u.Status = TRUE ORDER BY u.nombre,u.apellido_paterno"); ResultSet r = p.executeQuery()) {
            while (r.next()) lista.add(map(r)); return List.copyOf(lista);
        } catch (SQLException e) { throw DaoJdbcSupport.error("listar magistrados activos", e); }
    }

    private long contar(String sql) { try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) { return r.next() ? r.getLong(1) : 0; } catch (SQLException e) { throw DaoJdbcSupport.error("contar magistrados", e); } }
    private static void setLong(PreparedStatement p, int i, Long v) throws SQLException { if (v == null) p.setNull(i, Types.BIGINT); else p.setLong(i, v); }
    private static void setBoolean(PreparedStatement p, int i, Boolean v) throws SQLException { if (v == null) p.setNull(i, Types.BOOLEAN); else p.setBoolean(i, v); }
    private static Magistrado map(ResultSet r) throws SQLException {
        Magistrado m = new Magistrado(); m.setId(r.getLong("id_magistrado")); m.setUsuarioId(r.getLong("id_usuario")); m.setSalaId((Long) r.getObject("id_sala"));
        m.setNombre(r.getString("nombre")); String paterno = r.getString("apellido_paterno"), materno = r.getString("apellido_materno");
        m.setApellidos((paterno == null ? "" : paterno) + (materno == null || materno.isBlank() ? "" : " " + materno)); m.setCorreo(r.getString("correo")); m.setActivo(r.getBoolean("estatus"));
        var ts = r.getTimestamp("fecha_creacion"); if (ts != null) m.setCreadoEn(ts.toLocalDateTime()); m.setSalaNombre(r.getString("sala_nombre")); return m;
    }
}
