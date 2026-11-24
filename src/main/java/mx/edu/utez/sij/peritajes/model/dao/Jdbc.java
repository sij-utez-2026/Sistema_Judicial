package mx.edu.utez.sij.peritajes.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

// Ayudas pequeñas para los DAO del módulo.
final class Jdbc {

    private Jdbc() {
    }

    // Pone los valores de una lista en los ? de la consulta (para los filtros opcionales que se arman en el DAO).
    // Regresa el número del siguiente ? libre.
    static int asignar(PreparedStatement ps, List<Object> parametros) throws SQLException {
        int i = 1;
        for (Object valor : parametros) {
            ps.setObject(i++, valor);
        }
        return i;
    }

    // Long que puede ser NULL en la base
    static Long largo(ResultSet rs, String columna) throws SQLException {
        long valor = rs.getLong(columna);
        return rs.wasNull() ? null : valor;
    }

    static LocalDateTime fecha(ResultSet rs, String columna) throws SQLException {
        return rs.getObject(columna, LocalDateTime.class);
    }
}
