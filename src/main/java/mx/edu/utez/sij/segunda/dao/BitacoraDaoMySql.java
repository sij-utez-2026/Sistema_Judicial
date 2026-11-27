package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.dto.BitacoraItemDto;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Consulta solo lectura de bitacora_Seg conforme al DDL entregado. */
public class BitacoraDaoMySql {
    private static final String FROM = " FROM bitacora_Seg b LEFT JOIN user_Seg u ON u.id_usuario=b.id_usuario ";
    private static final String SELECT = "SELECT b.id_bitacora,b.fecha_hora,b.tipo_fuente,b.nombre_tabla,"
            + "b.id_registro,b.accion,b.id_usuario,CONCAT_WS(' ',u.nombre,u.apellido_paterno,u.apellido_materno) AS usuario_nombre,"
            + "CAST(b.valores_anteriores AS CHAR) AS valores_anteriores,CAST(b.valores_nuevos AS CHAR) AS valores_nuevos,"
            + "b.detalle,b.tono" + FROM;

    public void registrar(String tipoFuente, String tabla, Long registroId, String accion,
                          Long usuarioId, String detalle, String tono) {
        String sql = "INSERT INTO bitacora_Seg (tipo_fuente,nombre_tabla,id_registro,accion,id_usuario,detalle,tono) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, limitar(tipoFuente, 50));
            ps.setString(2, limitar(tabla, 100));
            if (registroId == null) ps.setNull(3, Types.BIGINT); else ps.setLong(3, registroId);
            ps.setString(4, limitar(accion, 200));
            if (usuarioId == null) ps.setNull(5, Types.BIGINT); else ps.setLong(5, usuarioId);
            ps.setString(6, limitar(detalle, 500));
            ps.setString(7, tono == null ? "blue" : tono);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error JDBC al registrar evento en bitacora_Seg", e);
        }
    }

    /** Registra el actor real sin asumir que IDs de otros módulos pertenecen a user_Seg. */
    public void registrar(String tipoFuente, String tabla, Long registroId, String accion,
                          UsuarioSesion actor, String detalle, String tono) {
        Long usuarioSegId = null;
        String detalleConActor = detalle;
        if (actor != null && "user_Seg".equalsIgnoreCase(actor.getTabla()) && actor.getId() != null
                && existeUsuarioSeg(actor.getId())) {
            usuarioSegId = actor.getId();
        } else if (actor != null) {
            detalleConActor = "Actor: " + actor.getNombre() + " (" + actor.getRol() + "). "
                    + (detalle == null ? "" : detalle);
        }
        registrar(tipoFuente, tabla, registroId, accion, usuarioSegId, detalleConActor, tono);
    }

    private boolean existeUsuarioSeg(Long id) {
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM user_Seg WHERE id_usuario=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo validar el actor de bitacora_Seg", e);
        }
    }

    public Pagina<BitacoraItemDto> buscar(String texto, String accion, LocalDate desde, LocalDate hasta,
                                          int pagina, int tamano) {
        int page = Math.max(0, pagina), size = Math.max(1, Math.min(100, tamano));
        String where = " WHERE (? IS NULL OR LOWER(CONCAT_WS(' ',b.accion,b.tipo_fuente,b.nombre_tabla,b.detalle,u.nombre,u.apellido_paterno,u.apellido_materno)) LIKE ?)"
                + " AND (? IS NULL OR b.accion=?) AND (? IS NULL OR DATE(b.fecha_hora)>=?) AND (? IS NULL OR DATE(b.fecha_hora)<=?)";
        String like = texto == null || texto.isBlank() ? null : "%" + texto.trim().toLowerCase() + "%";
        List<BitacoraItemDto> items = new ArrayList<>();
        long total;
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(SELECT + where
                     + " ORDER BY b.fecha_hora DESC,b.id_bitacora DESC LIMIT ? OFFSET ?")) {
            bindFiltros(ps, like, accion, desde, hasta);
            ps.setInt(9, size); ps.setLong(10, Pagina.offset(page, size));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) items.add(mapear(rs)); }
            try (PreparedStatement count = con.prepareStatement("SELECT COUNT(*)" + FROM + where)) {
                bindFiltros(count, like, accion, desde, hasta);
                try (ResultSet rs = count.executeQuery()) { total = rs.next() ? rs.getLong(1) : 0; }
            }
            return new Pagina<>(items, page, size, total);
        } catch (SQLException e) {
            throw new IllegalStateException("Error JDBC al consultar bitacora_Seg", e);
        }
    }

    public Optional<BitacoraItemDto> buscar(Long id) {
        if (id == null) return Optional.empty();
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(
                SELECT + " WHERE b.id_bitacora=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) {
            throw new IllegalStateException("Error JDBC al consultar detalle de bitacora_Seg", e);
        }
    }

    public List<String> acciones() {
        String sql = "SELECT DISTINCT accion FROM bitacora_Seg ORDER BY accion";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> salida = new ArrayList<>();
            while (rs.next()) salida.add(rs.getString(1));
            return List.copyOf(salida);
        } catch (SQLException e) {
            throw new IllegalStateException("Error JDBC al consultar acciones de bitacora_Seg", e);
        }
    }

    private void bindFiltros(PreparedStatement ps, String like, String accion,
                             LocalDate desde, LocalDate hasta) throws SQLException {
        ps.setString(1, like); ps.setString(2, like);
        ps.setString(3, accion); ps.setString(4, accion);
        if (desde == null) { ps.setNull(5, Types.DATE); ps.setNull(6, Types.DATE); }
        else { ps.setDate(5, Date.valueOf(desde)); ps.setDate(6, Date.valueOf(desde)); }
        if (hasta == null) { ps.setNull(7, Types.DATE); ps.setNull(8, Types.DATE); }
        else { ps.setDate(7, Date.valueOf(hasta)); ps.setDate(8, Date.valueOf(hasta)); }
    }

    private BitacoraItemDto mapear(ResultSet rs) throws SQLException {
        long usuarioId = rs.getLong("id_usuario"); Long usuario = rs.wasNull() ? null : usuarioId;
        long registroId = rs.getLong("id_registro"); Long registro = rs.wasNull() ? null : registroId;
        Timestamp fecha = rs.getTimestamp("fecha_hora");
        String usuarioNombre = rs.getString("usuario_nombre");
        String detalle = rs.getString("detalle");
        if ((usuarioNombre == null || usuarioNombre.isBlank())
                && detalle != null && detalle.startsWith("Actor: ")) {
            int finNombre = detalle.indexOf(" (", 7);
            if (finNombre > 7) usuarioNombre = detalle.substring(7, finNombre);
        }
        return new BitacoraItemDto(rs.getLong("id_bitacora"), fecha == null ? null : fecha.toLocalDateTime(),
                rs.getString("tipo_fuente"), rs.getString("nombre_tabla"), registro, rs.getString("accion"),
                usuario, usuarioNombre, rs.getString("valores_anteriores"),
                rs.getString("valores_nuevos"), detalle, rs.getString("tono"));
    }

    private String limitar(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
