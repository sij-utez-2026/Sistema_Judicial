package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Utilidades comunes de los adaptadores JDBC de Segunda Instancia. */
final class DaoJdbcSupport {
    private DaoJdbcSupport() { }

    static Connection obtener() {
        try {
            return ConexionBD.obtener();
        } catch (SQLException | RuntimeException e) {
            throw new IllegalStateException("No se pudo obtener conexión JDBC para Segunda Instancia", e);
        }
    }

    static IllegalStateException error(String operacion, SQLException e) {
        return new IllegalStateException("Error JDBC en Segunda Instancia al " + operacion, e);
    }

    static LocalDateTime fecha(ResultSetValue value) throws SQLException {
        Timestamp timestamp = value.getTimestamp();
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    @FunctionalInterface
    interface ResultSetValue {
        Timestamp getTimestamp() throws SQLException;
    }

    static long total(Connection con, String sql) throws SQLException {
        try (var ps = con.prepareStatement(sql); var rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }
}
