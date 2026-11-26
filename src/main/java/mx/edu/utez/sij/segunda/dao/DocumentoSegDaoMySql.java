package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Lectura de documentos BLOB asociados a tocas de Segunda Instancia. */
public class DocumentoSegDaoMySql {
    public record Documento(byte[] contenido, String titulo) { }

    public Optional<Documento> buscarUltimo(Long tocaId, String tipo) {
        if (tocaId == null || tipo == null || tipo.isBlank()) return Optional.empty();
        String sql = "SELECT documento,titulo FROM documento_Seg WHERE id_toca=? AND tipo=? "
                + "ORDER BY fecha_carga DESC,id_documento DESC LIMIT 1";
        try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, tocaId);
            ps.setString(2, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new Documento(rs.getBytes("documento"), rs.getString("titulo")))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error JDBC al leer documento de Segunda Instancia", e);
        }
    }
}
