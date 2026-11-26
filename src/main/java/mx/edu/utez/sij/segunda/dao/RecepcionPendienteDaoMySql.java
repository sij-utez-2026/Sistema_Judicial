package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.segunda.dto.RecepcionPendienteDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Consulta el expediente enviado por Oficialía y la sala inicial, sin permitir editarlos en Segunda. */
public class RecepcionPendienteDaoMySql {
    private static final String SELECT = "SELECT si.num_expediente, d.id_materia AS clave_materia, mat.nombre AS materia_nombre, "
            + "j.id_distrito AS clave_distrito, dis.nombre AS distrito_nombre, j.id_juzgado AS clave_juzgado, j.nombre AS juzgado_nombre, "
            + "si.id_sala, sala.nombre AS sala_nombre, "
            + "TRIM(CONCAT_WS(' ', pd.nombre, pd.apellido_paterno, pd.apellido_materno)) AS demandante, "
            + "TRIM(CONCAT_WS(' ', pdd.nombre, pdd.apellido_paterno, pdd.apellido_materno)) AS demandado "
            + "FROM segunda_instancia_Ofc si "
            + "JOIN expediente_Ofc e ON e.num_expediente = si.num_expediente "
            + "JOIN demanda_Ofc d ON d.folio = e.folio "
            + "JOIN materia_Ofc mat ON mat.id_materia = d.id_materia "
            + "JOIN mesa_audiencia_Aud mesa ON mesa.id_mesa = e.id_mesa "
            + "JOIN juzgado_Ofc j ON j.id_juzgado = mesa.clave_juzgado "
            + "JOIN distrito_Ofc dis ON dis.id_distrito = j.id_distrito "
            + "JOIN sala_Seg sala ON sala.id_sala = si.id_sala "
            + "LEFT JOIN persona_Ofc pd ON pd.id_persona = d.id_demandante "
            + "LEFT JOIN persona_Ofc pdd ON pdd.id_persona = d.id_demandado ";
    private static final String PENDIENTE = " WHERE si.estado IN ('REGISTRADA', 'ENVIADA') "
            + "AND NOT EXISTS (SELECT 1 FROM toca_Seg t WHERE t.id_segunda_instancia = si.id_segunda_instancia) ";

    public List<RecepcionPendienteDto> listar() {
        String sql = SELECT + PENDIENTE + " ORDER BY si.fecha_registro DESC, si.id_segunda_instancia DESC";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<RecepcionPendienteDto> resultado = new ArrayList<>();
            while (rs.next()) resultado.add(mapear(rs));
            return resultado;
        } catch (SQLException e) {
            throw new IllegalStateException("Error JDBC al consultar expedientes pendientes de recepción", e);
        }
    }

    public Optional<RecepcionPendienteDto> buscarPendiente(String numeroExpediente) {
        if (numeroExpediente == null || numeroExpediente.isBlank()) return Optional.empty();
        String sql = SELECT + PENDIENTE + " AND si.num_expediente = ? LIMIT 1";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numeroExpediente.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error JDBC al consultar el expediente pendiente de recepción", e);
        }
    }

    private RecepcionPendienteDto mapear(ResultSet rs) throws SQLException {
        long salaId = rs.getLong("id_sala");
        Long sala = rs.wasNull() ? null : salaId;
        return new RecepcionPendienteDto(rs.getString("num_expediente"), rs.getString("clave_materia"),
                rs.getString("materia_nombre"), rs.getString("clave_distrito"), rs.getString("distrito_nombre"),
                rs.getString("clave_juzgado"), rs.getString("juzgado_nombre"), sala,
                rs.getString("sala_nombre"), rs.getString("demandante"), rs.getString("demandado"));
    }
}
