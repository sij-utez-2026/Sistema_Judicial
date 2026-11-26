package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;

import java.sql.*;

/** Ejecuta una reasignación administrativa y registra su origen, destino y motivo. */
public class ReasignacionDaoMySql {

    /**
     * Cambia de inmediato la sala y el magistrado de la toca, y deja el registro
     * en reasignacion_seg usando el esquema actual (sin estado ni solicitante).
     */
    public ResultadoReasignacion reasignar(Long tocaId, Long salaDestinoId, String motivo) {
        if (tocaId == null || salaDestinoId == null
                || motivo == null || motivo.isBlank() || motivo.trim().length() > 300) {
            throw new IllegalArgumentException("Seleccione la toca y la sala destino e indique un motivo de hasta 300 caracteres.");
        }

        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                long salaOrigenId;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_sala, estado FROM toca_Seg WHERE id_toca = ? FOR UPDATE")) {
                    ps.setLong(1, tocaId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("La toca no existe.");
                        if ("Resuelto".equals(rs.getString("estado"))) {
                            throw new IllegalArgumentException("No se puede reasignar una toca resuelta.");
                        }
                        salaOrigenId = rs.getLong("id_sala");
                        if (rs.wasNull()) {
                            throw new IllegalArgumentException("La toca no tiene una sala de origen registrada.");
                        }
                    }
                }

                if (salaOrigenId == salaDestinoId) {
                    throw new IllegalArgumentException("Seleccione una sala distinta a la sala actual.");
                }

                Long magistradoDestinoId = magistradoActivoDeSala(con, salaDestinoId);
                if (magistradoDestinoId == null) {
                    throw new IllegalArgumentException("La sala destino debe estar activa y tener un magistrado activo asignado.");
                }

                long reasignacionId;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO reasignacion_seg (id_toca, id_sala_origen, id_sala_destino, motivo) "
                                + "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, tocaId);
                    ps.setLong(2, salaOrigenId);
                    ps.setLong(3, salaDestinoId);
                    ps.setString(4, motivo.trim());
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("No se insertó el registro de reasignación.");
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("MySQL no devolvió el identificador de reasignación.");
                        }
                        reasignacionId = keys.getLong(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE toca_Seg SET id_sala = ?, id_magistrado = ? "
                                + "WHERE id_toca = ? AND estado <> 'Resuelto'")) {
                    ps.setLong(1, salaDestinoId);
                    ps.setLong(2, magistradoDestinoId);
                    ps.setLong(3, tocaId);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalArgumentException("La toca ya no está disponible para reasignación.");
                    }
                }

                con.commit();
                return new ResultadoReasignacion(
                        reasignacionId, tocaId, salaOrigenId, salaDestinoId, magistradoDestinoId);
            } catch (SQLException | RuntimeException e) {
                rollback(con, e);
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw DaoJdbcSupport.error("realizar la reasignación", e);
        }
    }

    private Long magistradoActivoDeSala(Connection con, long salaId) throws SQLException {
        String sql = "SELECT m.id_magistrado FROM sala_Seg s "
                + "JOIN magistrado_Seg m ON m.id_sala = s.id_sala AND m.estatus = TRUE "
                + "JOIN user_Seg u ON u.id_usuario = m.id_usuario AND u.Status = TRUE "
                + "WHERE s.id_sala = ? AND s.estatus = TRUE ORDER BY m.id_magistrado LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, salaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("id_magistrado") : null;
            }
        }
    }

    private void rollback(Connection con, Throwable causa) {
        try {
            con.rollback();
        } catch (SQLException errorRollback) {
            causa.addSuppressed(errorRollback);
        }
    }

    public record ResultadoReasignacion(
            long reasignacionId,
            long tocaId,
            long salaOrigenId,
            long salaDestinoId,
            long magistradoDestinoId) {
    }
}
