package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.EstadoResolucion;
import mx.edu.utez.sij.segunda.model.Resolucion;
import mx.edu.utez.sij.segunda.model.SentidoResolucion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO MySQL de resolucion_Seg; el PDF se conserva como BLOB en documento_Seg. */
public class ResolucionDaoMySql implements ResolucionDao {
    private static final int MAX_PDF = 15 * 1024 * 1024;
    private static final String BASE = "SELECT r.id_resolucion,r.id_toca,r.id_magistrado,r.sentido,r.cuerpo,"
            + "r.observaciones,r.url_pdf,r.fecha_emision,t.numero_toca,si.num_expediente,t.id_sala,m.id_usuario "
            + "FROM resolucion_Seg r JOIN toca_Seg t ON t.id_toca=r.id_toca "
            + "JOIN segunda_instancia_Ofc si ON si.id_segunda_instancia=t.id_segunda_instancia "
            + "JOIN magistrado_Seg m ON m.id_magistrado=r.id_magistrado ";

    @Override
    public Optional<Resolucion> findById(Long id) {
        if (id == null) return Optional.empty();
        try (Connection con = DaoJdbcSupport.obtener();
             PreparedStatement ps = con.prepareStatement(BASE + "WHERE r.id_resolucion=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DaoJdbcSupport.error("buscar resolución", e);
        }
    }

    @Override
    public Resolucion save(Resolucion resolucion) {
        if (resolucion.getEstado() == EstadoResolucion.ANULADA) {
            throw new UnsupportedOperationException("El esquema vigente no permite anular resoluciones; no se modificó la base.");
        }
        try (Connection con = DaoJdbcSupport.obtener()) {
            con.setAutoCommit(false);
            try {
                boolean nuevo = resolucion.getId() == null;
                String sql = nuevo
                        ? "INSERT INTO resolucion_Seg (id_toca,id_magistrado,sentido,cuerpo,observaciones,url_pdf) VALUES (?,?,?,?,?,?)"
                        : "UPDATE resolucion_Seg SET id_toca=?,id_magistrado=?,sentido=?,cuerpo=?,observaciones=?,url_pdf=? WHERE id_resolucion=?";
                try (PreparedStatement ps = con.prepareStatement(sql,
                        nuevo ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
                    ps.setLong(1, resolucion.getTocaId());
                    ps.setLong(2, resolucion.getMagistradoId());
                    ps.setString(3, resolucion.getSentido().getEtiqueta());
                    ps.setString(4, resolucion.getComentarios());
                    ps.setString(5, null);
                    ps.setString(6, resolucion.getPdfUrl());
                    if (!nuevo) ps.setLong(7, resolucion.getId());
                    if (ps.executeUpdate() != 1) throw new SQLException("La resolución no pudo guardarse");
                    if (nuevo) {
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (!keys.next()) throw new SQLException("MySQL no devolvió el identificador de resolución");
                            resolucion.setId(keys.getLong(1));
                        }
                    }
                }
                guardarPdf(con, resolucion);
                con.commit();
            } catch (SQLException | RuntimeException e) {
                try { con.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw DaoJdbcSupport.error("guardar resolución", e);
        }
        return findById(resolucion.getId())
                .orElseThrow(() -> new IllegalStateException("No se pudo releer la resolución guardada"));
    }

    @Override
    public boolean registrarConCierreAtomico(Resolucion resolucion, String descripcionHistorial) {
        try (Connection con = DaoJdbcSupport.obtener()) {
            con.setAutoCommit(false);
            try {
                String estado;
                long magistradoToca;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT estado,id_magistrado FROM toca_Seg WHERE id_toca=? FOR UPDATE")) {
                    ps.setLong(1, resolucion.getTocaId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new ReglaNegocioException("La toca ya no existe");
                        estado = rs.getString("estado");
                        magistradoToca = rs.getLong("id_magistrado");
                        if (rs.wasNull()) throw new ReglaNegocioException("La toca no tiene magistrado asignado");
                    }
                }
                if (!"En ponencia".equals(estado)) {
                    throw new ReglaNegocioException("La toca ya no se encuentra en ponencia");
                }
                if (magistradoToca != resolucion.getMagistradoId()) {
                    throw new ReglaNegocioException("El magistrado asignado a la toca cambió; vuelva a cargar la pantalla");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_resolucion FROM resolucion_Seg WHERE id_toca=? LIMIT 1 FOR UPDATE")) {
                    ps.setLong(1, resolucion.getTocaId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new ReglaNegocioException("La toca ya tiene una resolución registrada");
                    }
                }

                String sql = "INSERT INTO resolucion_Seg (id_toca,id_magistrado,sentido,cuerpo,observaciones,url_pdf) VALUES (?,?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, resolucion.getTocaId());
                    ps.setLong(2, resolucion.getMagistradoId());
                    ps.setString(3, resolucion.getSentido().getEtiqueta());
                    ps.setString(4, resolucion.getComentarios());
                    ps.setString(5, null);
                    ps.setString(6, resolucion.getPdfUrl());
                    if (ps.executeUpdate() != 1) throw new SQLException("No se guardó la resolución");
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("MySQL no devolvió el identificador de resolución");
                        resolucion.setId(keys.getLong(1));
                    }
                }
                guardarPdf(con, resolucion);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE toca_Seg SET estado='Resuelto' WHERE id_toca=? AND estado='En ponencia'")) {
                    ps.setLong(1, resolucion.getTocaId());
                    if (ps.executeUpdate() != 1) throw new SQLException("No se pudo cerrar la toca como Resuelto");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO historial_toca_Seg (id_toca,descripcion) VALUES (?,?)")) {
                    ps.setLong(1, resolucion.getTocaId());
                    ps.setString(2, limitar(descripcionHistorial, 300));
                    if (ps.executeUpdate() != 1) throw new SQLException("No se registró el historial de resolución");
                }
                con.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                try { con.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw DaoJdbcSupport.error("registrar resolución, PDF y cierre de toca", e);
        }
    }

    private static String limitar(String texto, int maximo) {
        String valor = texto == null || texto.isBlank() ? "Resolución registrada" : texto.trim();
        return valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }

    private void guardarPdf(Connection con, Resolucion resolucion) throws SQLException {
        byte[] pdf = resolucion.getPdfDocumento();
        if (pdf == null) return;
        if (pdf.length < 5 || pdf.length > MAX_PDF || pdf[0] != '%' || pdf[1] != 'P'
                || pdf[2] != 'D' || pdf[3] != 'F' || pdf[4] != '-') {
            throw new IllegalArgumentException("El archivo no es un PDF válido o excede 15 MB");
        }
        String titulo = resolucion.getNombrePdf();
        if (titulo == null || titulo.isBlank()) titulo = "Resolucion.pdf";
        titulo = titulo.replace('\\', '/');
        titulo = titulo.substring(titulo.lastIndexOf('/') + 1).trim();
        if (titulo.length() > 200) titulo = titulo.substring(titulo.length() - 200);
        if (titulo.isBlank()) titulo = "Resolucion.pdf";
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO documento_Seg (id_toca,titulo,tipo,documento) VALUES (?,?,?,?)")) {
            ps.setLong(1, resolucion.getTocaId());
            ps.setString(2, titulo);
            ps.setString(3, "Resolución");
            ps.setBytes(4, pdf);
            if (ps.executeUpdate() != 1) throw new SQLException("No se guardó el PDF de la resolución");
        }
    }

    @Override
    public long count() {
        try (Connection con = DaoJdbcSupport.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM resolucion_Seg");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw DaoJdbcSupport.error("contar resoluciones", e);
        }
    }

    @Override
    public List<Resolucion> findByTocaId(Long tocaId) {
        return listar(BASE + "WHERE r.id_toca=? ORDER BY r.fecha_emision DESC", ps -> ps.setLong(1, tocaId));
    }

    @Override
    public Optional<Resolucion> findVigenteByTocaId(Long tocaId) {
        return findByTocaId(tocaId).stream().findFirst();
    }

    @Override
    public Pagina<Resolucion> buscar(String texto, EstadoResolucion estado, Long salaId,
                                     Long usuarioId, int pagina, int tamano) {
        if (estado == EstadoResolucion.ANULADA) return new Pagina<>(List.of(), Math.max(0, pagina), tamano, 0);
        String where = " WHERE (? IS NULL OR LOWER(CONCAT_WS(' ',t.numero_toca,si.num_expediente)) LIKE ?)"
                + " AND (? IS NULL OR t.id_sala=?) AND (? IS NULL OR m.id_usuario=?)";
        String from = " FROM resolucion_Seg r JOIN toca_Seg t ON t.id_toca=r.id_toca"
                + " JOIN segunda_instancia_Ofc si ON si.id_segunda_instancia=t.id_segunda_instancia"
                + " JOIN magistrado_Seg m ON m.id_magistrado=r.id_magistrado";
        String like = texto == null ? null : "%" + texto.toLowerCase() + "%";
        List<Resolucion> salida = new ArrayList<>();
        long total;
        try (Connection con = DaoJdbcSupport.obtener();
             PreparedStatement ps = con.prepareStatement(BASE + where
                     + " ORDER BY r.fecha_emision DESC LIMIT ? OFFSET ?")) {
            ps.setString(1, like); ps.setString(2, like);
            setLong(ps, 3, salaId); setLong(ps, 4, salaId);
            setLong(ps, 5, usuarioId); setLong(ps, 6, usuarioId);
            ps.setInt(7, tamano); ps.setInt(8, Pagina.offset(pagina, tamano));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) salida.add(map(rs)); }
            try (PreparedStatement count = con.prepareStatement("SELECT COUNT(*)" + from + where)) {
                count.setString(1, like); count.setString(2, like);
                setLong(count, 3, salaId); setLong(count, 4, salaId);
                setLong(count, 5, usuarioId); setLong(count, 6, usuarioId);
                try (ResultSet rs = count.executeQuery()) { total = rs.next() ? rs.getLong(1) : 0; }
            }
            return new Pagina<>(salida, Math.max(0, pagina), tamano, total);
        } catch (SQLException e) {
            throw DaoJdbcSupport.error("listar resoluciones", e);
        }
    }

    private List<Resolucion> listar(String sql, Binder binder) {
        List<Resolucion> salida = new ArrayList<>();
        try (Connection con = DaoJdbcSupport.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) salida.add(map(rs)); }
            return List.copyOf(salida);
        } catch (SQLException e) {
            throw DaoJdbcSupport.error("consultar resoluciones", e);
        }
    }

    private static void setLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, Types.BIGINT); else ps.setLong(index, value);
    }

    private static Resolucion map(ResultSet rs) throws SQLException {
        Resolucion resolucion = new Resolucion();
        resolucion.setId(rs.getLong("id_resolucion"));
        resolucion.setTocaId(rs.getLong("id_toca"));
        resolucion.setMagistradoId(rs.getLong("id_magistrado"));
        resolucion.setSentido(SentidoResolucion.desdeTexto(rs.getString("sentido")));
        resolucion.setComentarios(rs.getString("cuerpo"));
        resolucion.setPdfUrl(rs.getString("url_pdf"));
        var fecha = rs.getTimestamp("fecha_emision");
        if (fecha != null) resolucion.setFecha(fecha.toLocalDateTime());
        resolucion.setEstado(EstadoResolucion.VIGENTE);
        resolucion.setTocaFolio(rs.getString("numero_toca"));
        resolucion.setTocaExpediente(rs.getString("num_expediente"));
        return resolucion;
    }

    @FunctionalInterface
    private interface Binder { void bind(PreparedStatement ps) throws SQLException; }
}
