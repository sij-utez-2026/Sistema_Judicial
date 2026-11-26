package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.EstadoToca;
import mx.edu.utez.sij.segunda.model.HistorialToca;
import mx.edu.utez.sij.segunda.model.Toca;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementación JDBC directa del acceso a tocas de segunda instancia. */
public class TocaDaoMySql implements TocaDao {
    private static final String FROM = " FROM toca_Seg t "
            + "JOIN segunda_instancia_Ofc si ON si.id_segunda_instancia = t.id_segunda_instancia "
            + "LEFT JOIN expediente_Ofc e ON e.num_expediente = si.num_expediente "
            + "LEFT JOIN demanda_Ofc d ON d.folio = e.folio "
            + "LEFT JOIN mesa_audiencia_Aud mesa ON mesa.id_mesa = e.id_mesa "
            + "LEFT JOIN juzgado_Ofc j ON j.id_juzgado = mesa.clave_juzgado "
            + "LEFT JOIN distrito_Ofc dis ON dis.id_distrito = j.id_distrito "
            + "LEFT JOIN materia_Ofc mat ON mat.id_materia = d.id_materia "
            + "LEFT JOIN sala_Seg sala ON sala.id_sala = t.id_sala "
            + "LEFT JOIN magistrado_Seg mag ON mag.id_magistrado = t.id_magistrado "
            + "LEFT JOIN user_Seg u ON u.id_usuario = mag.id_usuario ";
    private static final String SELECT = "SELECT t.id_toca, t.numero_toca, si.num_expediente, "
            + "d.id_materia AS clave_materia, mat.nombre AS materia_nombre, j.id_juzgado AS juzgado_origen_id, "
            + "j.nombre AS juzgado_nombre, j.id_distrito AS distrito_id, dis.nombre AS distrito_nombre, "
            + "t.id_sala, sala.nombre AS sala_nombre, mag.id_magistrado, mag.id_usuario AS magistrado_usuario_id, "
            + "u.nombre AS magistrado_nombre, u.apellido_paterno, u.apellido_materno, "
            + "t.estado, t.fecha_recepcion, t.apelante" + FROM;

    @Override
    public Optional<Toca> findById(Long id) {
        if (id == null) return Optional.empty();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE t.id_toca = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw error("consultar la toca por identificador", e);
        }
    }

    @Override
    public Optional<Toca> findByFolio(String folioToca) {
        if (folioToca == null) return Optional.empty();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE LOWER(t.numero_toca) = LOWER(?)")) {
            ps.setString(1, folioToca);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw error("consultar la toca por folio", e);
        }
    }

    @Override
    public Toca save(Toca toca) {
        if (toca == null) throw new IllegalArgumentException("La toca es obligatoria");
        if (toca.getId() == null) return insertar(toca, null);
        actualizar(toca);
        return toca;
    }

    @Override
    public boolean recibirAtomico(Toca toca, HistorialToca historial) {
        if (toca == null || toca.getId() != null || historial == null) {
            throw new IllegalArgumentException("La recepción nueva y su historial son obligatorios");
        }
        insertar(toca, historial);
        return true;
    }

    private Toca insertar(Toca toca, HistorialToca historial) {
        if (toca.getDocumentoApelacionPdf() != null) {
            validarPdfRecepcion(toca.getDocumentoApelacionPdf());
        }
        String consultaSala = "SELECT si.id_segunda_instancia, si.id_sala, si.estado, "
                + "TRIM(CONCAT_WS(' ', pd.nombre, pd.apellido_paterno, pd.apellido_materno)) AS demandante, "
                + "TRIM(CONCAT_WS(' ', pdd.nombre, pdd.apellido_paterno, pdd.apellido_materno)) AS demandado "
                + "FROM segunda_instancia_Ofc si JOIN expediente_Ofc e ON e.num_expediente = si.num_expediente "
                + "JOIN demanda_Ofc d ON d.folio = e.folio "
                + "LEFT JOIN persona_Ofc pd ON pd.id_persona = d.id_demandante "
                + "LEFT JOIN persona_Ofc pdd ON pdd.id_persona = d.id_demandado "
                + "WHERE si.num_expediente = ? FOR UPDATE";
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                int idSegundaInstancia;
                Long salaId;
                String apelanteGuardar;
                try (PreparedStatement ps = con.prepareStatement(consultaSala)) {
                    ps.setString(1, toca.getNumeroExpediente());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("No existe segunda instancia para el expediente indicado");
                        }
                        idSegundaInstancia = rs.getInt("id_segunda_instancia");
                        String estadoOrigen = rs.getString("estado");
                        if (!("REGISTRADA".equals(estadoOrigen) || "ENVIADA".equals(estadoOrigen))) {
                            throw new IllegalStateException("La segunda instancia ya fue recibida o no está disponible");
                        }
                        long idSala = rs.getLong("id_sala");
                        salaId = rs.wasNull() ? null : idSala;
                        String nombreParte = "Demandante".equals(toca.getApelante())
                                ? rs.getString("demandante") : "Demandado".equals(toca.getApelante())
                                ? rs.getString("demandado") : null;
                        if (toca.getApelante() != null && (nombreParte == null || nombreParte.isBlank())) {
                            throw new IllegalStateException("No se encontró el nombre de la parte seleccionada como apelante");
                        }
                        apelanteGuardar = toca.getApelante() == null ? null
                                : toca.getApelante() + ": " + nombreParte;
                    }
                }
                if (salaId == null) {
                    throw new IllegalStateException("La segunda instancia no tiene sala asociada");
                }
                if (toca.getSalaId() != null && !toca.getSalaId().equals(salaId)) {
                    throw new IllegalStateException("La sala se toma de segunda_instancia_Ofc y no puede cambiarse al recibir");
                }
                LocalDateTime fechaRecepcion = toca.getFechaRecepcion() == null
                        ? LocalDateTime.now() : toca.getFechaRecepcion();
                String estado = etiqueta(toca.getEstado());
                String insert = "INSERT INTO toca_Seg "
                        + "(numero_toca, id_segunda_instancia, apelante, id_sala, id_magistrado, estado, fecha_recepcion) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                long idToca;
                try (PreparedStatement ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, toca.getFolioToca());
                    ps.setInt(2, idSegundaInstancia);
                    ps.setString(3, apelanteGuardar);
                    ps.setLong(4, salaId);
                    setLongOrNull(ps, 5, toca.getMagistradoId());
                    ps.setString(6, estado);
                    ps.setTimestamp(7, Timestamp.valueOf(fechaRecepcion));
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("No se insertó la toca");
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("MySQL no devolvió el identificador de la toca");
                        idToca = keys.getLong(1);
                    }
                }
                guardarDocumentoRecepcion(con, idToca, toca);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE segunda_instancia_Ofc SET estado = 'RECIBIDA' WHERE id_segunda_instancia = ?")) {
                    ps.setInt(1, idSegundaInstancia);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("No se pudo marcar la segunda instancia como RECIBIDA");
                    }
                }
                if (historial != null) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO historial_toca_Seg (id_toca,descripcion) VALUES (?,?)")) {
                        ps.setLong(1, idToca);
                        ps.setString(2, descripcionRecepcion(historial));
                        if (ps.executeUpdate() != 1) throw new SQLException("No se registró el historial de recepción");
                    }
                }
                con.commit();
                toca.setId(idToca);
                toca.setSalaId(salaId);
                toca.setFechaRecepcion(fechaRecepcion);
                return toca;
            } catch (SQLException | RuntimeException e) {
                rollback(con, e);
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw error("registrar la toca", e);
        }
    }

    private void guardarDocumentoRecepcion(Connection con, long idToca, Toca toca) throws SQLException {
        byte[] pdf = toca.getDocumentoApelacionPdf();
        if (pdf == null) return;
        validarPdfRecepcion(pdf);
        String titulo = toca.getNombreDocumentoApelacion();
        if (titulo == null || titulo.isBlank()) titulo = "Escrito de apelación.pdf";
        titulo = titulo.replace('\\', '/');
        titulo = titulo.substring(titulo.lastIndexOf('/') + 1).trim();
        if (titulo.length() > 200) titulo = titulo.substring(titulo.length() - 200);
        if (titulo.isBlank()) titulo = "Escrito de apelación.pdf";
        String sql = "INSERT INTO documento_Seg (id_toca, titulo, tipo, documento) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idToca);
            ps.setString(2, titulo);
            ps.setString(3, "Escrito de apelación");
            ps.setBytes(4, pdf);
            if (ps.executeUpdate() != 1) throw new SQLException("No se guardó el PDF de apelación");
        }
    }

    private void validarPdfRecepcion(byte[] pdf) {
        if (pdf == null || pdf.length < 5 || pdf.length > 15 * 1024 * 1024
                || pdf[0] != '%' || pdf[1] != 'P' || pdf[2] != 'D' || pdf[3] != 'F' || pdf[4] != '-') {
            throw new IllegalArgumentException("Adjunte un PDF de apelación válido de hasta 15 MB");
        }
    }

    private String descripcionRecepcion(HistorialToca historial) {
        String accion = historial.getAccion() == null || historial.getAccion().isBlank()
                ? "Recepción de la toca" : historial.getAccion().trim();
        String detalle = historial.getDetalle();
        String texto = detalle == null || detalle.isBlank() ? accion : accion + " · " + detalle.trim();
        return texto.length() <= 300 ? texto : texto.substring(0, 300);
    }

    private void actualizar(Toca toca) {
        String sql = "UPDATE toca_Seg SET numero_toca = ?, apelante = ?, id_sala = ?, id_magistrado = ?, "
                + "estado = ?, fecha_recepcion = ? WHERE id_toca = ?";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, toca.getFolioToca());
            ps.setString(2, toca.getApelante());
            setLongOrNull(ps, 3, toca.getSalaId());
            setLongOrNull(ps, 4, toca.getMagistradoId());
            ps.setString(5, etiqueta(toca.getEstado()));
            if (toca.getFechaRecepcion() == null) ps.setTimestamp(6, null);
            else ps.setTimestamp(6, Timestamp.valueOf(toca.getFechaRecepcion()));
            ps.setLong(7, toca.getId());
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("No existe la toca que se intenta actualizar");
            }
        } catch (SQLException e) {
            throw error("actualizar la toca", e);
        }
    }

    @Override
    public long count() {
        return contar("SELECT COUNT(*) FROM toca_Seg", List.of());
    }

    @Override
    public long countByEstado(EstadoToca estado) {
        if (estado == null) return count();
        return contar("SELECT COUNT(*) FROM toca_Seg WHERE estado = ?", List.of(etiqueta(estado)));
    }

    @Override
    public boolean existsByFolio(String folioToca) {
        if (folioToca == null) return false;
        return existe("SELECT 1 FROM toca_Seg WHERE LOWER(numero_toca) = LOWER(?) LIMIT 1", folioToca);
    }

    @Override
    public boolean existsByNumeroExpediente(String numeroExpediente) {
        if (numeroExpediente == null) return false;
        String sql = "SELECT 1 FROM toca_Seg t JOIN segunda_instancia_Ofc si "
                + "ON si.id_segunda_instancia = t.id_segunda_instancia "
                + "WHERE LOWER(si.num_expediente) = LOWER(?) AND t.estado <> 'Resuelto' LIMIT 1";
        return existe(sql, numeroExpediente);
    }

    @Override
    public boolean tieneTocasPendientesPorSala(Long salaId) {
        if (salaId == null) return false;
        String sql = "SELECT 1 FROM toca_Seg WHERE id_sala = ? AND estado <> 'Resuelto' LIMIT 1";
        return existe(sql, salaId);
    }

    @Override
    public boolean tieneTocasPendientesPorMagistrado(Long magistradoUsuarioId) {
        if (magistradoUsuarioId == null) return false;
        String sql = "SELECT 1 FROM toca_Seg t JOIN magistrado_Seg m ON m.id_magistrado = t.id_magistrado "
                + "JOIN user_Seg u ON u.id_usuario = m.id_usuario "
                + "WHERE u.id_usuario = ? AND t.estado <> 'Resuelto' LIMIT 1";
        return existe(sql, magistradoUsuarioId);
    }

    @Override
    public Pagina<Toca> buscar(String texto, EstadoToca estado, Long salaId,
                               Long soloDeMagistradoUsuarioId, int pagina, int tamano) {
        int numeroPagina = Math.max(0, pagina);
        int size = Math.max(1, tamano);
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (texto != null && !texto.isBlank()) {
            where.append(" AND (LOWER(t.numero_toca) LIKE LOWER(?) OR LOWER(si.num_expediente) LIKE LOWER(?))");
            String like = "%" + texto.trim() + "%";
            parametros.add(like);
            parametros.add(like);
        }
        if (estado != null) {
            where.append(" AND t.estado = ?");
            parametros.add(etiqueta(estado));
        }
        if (salaId != null) {
            where.append(" AND t.id_sala = ?");
            parametros.add(salaId);
        }
        if (soloDeMagistradoUsuarioId != null) {
            where.append(" AND u.id_usuario = ?");
            parametros.add(soloDeMagistradoUsuarioId);
        }
        String sqlCuenta = "SELECT COUNT(*)" + FROM + where;
        String sqlPagina = SELECT + where + " ORDER BY t.fecha_recepcion DESC, t.id_toca DESC LIMIT ? OFFSET ?";
        try (Connection con = ConexionBD.obtener()) {
            long total;
            try (PreparedStatement ps = con.prepareStatement(sqlCuenta)) {
                vincular(ps, parametros);
                try (ResultSet rs = ps.executeQuery()) {
                    total = rs.next() ? rs.getLong(1) : 0L;
                }
            }
            List<Toca> resultados = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sqlPagina)) {
                int indice = vincular(ps, parametros);
                ps.setInt(indice++, size);
                ps.setLong(indice, (long) numeroPagina * size);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) resultados.add(mapear(rs));
                }
            }
            return new Pagina<>(resultados, numeroPagina, size, total);
        } catch (SQLException e) {
            throw error("buscar tocas", e);
        }
    }

    @Override
    public List<Toca> pendientesDeMagistrado(Long magistradoUsuarioId) {
        if (magistradoUsuarioId == null) return List.of();
        String sql = SELECT + " WHERE u.id_usuario = ? AND t.estado <> 'Resuelto' "
                + "ORDER BY t.fecha_recepcion DESC, t.id_toca DESC";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, magistradoUsuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Toca> resultado = new ArrayList<>();
                while (rs.next()) resultado.add(mapear(rs));
                return resultado;
            }
        } catch (SQLException e) {
            throw error("consultar tocas pendientes del magistrado", e);
        }
    }

    private long contar(String sql, List<?> parametros) {
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            vincular(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw error("contar tocas", e);
        }
    }

    private boolean existe(String sql, Object parametro) {
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro instanceof Long id) ps.setLong(1, id);
            else ps.setString(1, (String) parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw error("comprobar existencia de tocas", e);
        }
    }

    private Toca mapear(ResultSet rs) throws SQLException {
        Toca toca = new Toca();
        toca.setId(rs.getLong("id_toca"));
        toca.setFolioToca(rs.getString("numero_toca"));
        toca.setNumeroExpediente(rs.getString("num_expediente"));
        toca.setApelante(rs.getString("apelante"));
        toca.setMateriaId(rs.getString("clave_materia"));
        toca.setMateriaNombre(rs.getString("materia_nombre"));
        toca.setJuzgadoOrigenId(rs.getString("juzgado_origen_id"));
        toca.setJuzgadoNombre(rs.getString("juzgado_nombre"));
        toca.setDistritoId(rs.getString("distrito_id"));
        toca.setDistritoNombre(rs.getString("distrito_nombre"));
        toca.setSalaId(nullableLong(rs, "id_sala"));
        toca.setSalaNombre(rs.getString("sala_nombre"));
        toca.setMagistradoId(nullableLong(rs, "id_magistrado"));
        toca.setMagistradoUsuarioId(nullableLong(rs, "magistrado_usuario_id"));
        toca.setMagistradoNombre(nombreCompleto(rs));
        toca.setEstado(EstadoToca.desdeTexto(rs.getString("estado")));
        Timestamp fecha = rs.getTimestamp("fecha_recepcion");
        toca.setFechaRecepcion(fecha == null ? null : fecha.toLocalDateTime());
        return toca;
    }

    private String nombreCompleto(ResultSet rs) throws SQLException {
        String nombre = rs.getString("magistrado_nombre");
        String paterno = rs.getString("apellido_paterno");
        String materno = rs.getString("apellido_materno");
        StringBuilder completo = new StringBuilder();
        agregarNombre(completo, nombre);
        agregarNombre(completo, paterno);
        agregarNombre(completo, materno);
        return completo.length() == 0 ? null : completo.toString();
    }

    private void agregarNombre(StringBuilder destino, String parte) {
        if (parte != null && !parte.isBlank()) {
            if (destino.length() > 0) destino.append(' ');
            destino.append(parte.trim());
        }
    }

    private Long nullableLong(ResultSet rs, String columna) throws SQLException {
        long valor = rs.getLong(columna);
        return rs.wasNull() ? null : valor;
    }

    private void setLongOrNull(PreparedStatement ps, int indice, Long valor) throws SQLException {
        if (valor == null) ps.setNull(indice, Types.BIGINT);
        else ps.setLong(indice, valor);
    }

    private int vincular(PreparedStatement ps, List<?> parametros) throws SQLException {
        int indice = 1;
        for (Object parametro : parametros) {
            if (parametro instanceof String texto) ps.setString(indice++, texto);
            else if (parametro instanceof Long valor) ps.setLong(indice++, valor);
            else throw new IllegalArgumentException("Tipo de parámetro SQL no compatible");
        }
        return indice;
    }

    private String etiqueta(EstadoToca estado) {
        if (estado == null) return EstadoToca.ASIGNADA.getEtiqueta();
        return estado.getEtiqueta();
    }

    private void rollback(Connection con, Throwable causa) {
        try {
            con.rollback();
        } catch (SQLException rollbackError) {
            causa.addSuppressed(rollbackError);
        }
    }

    private IllegalStateException error(String operacion, SQLException causa) {
        return new IllegalStateException("Error JDBC al " + operacion + " en Segunda Instancia", causa);
    }
}
