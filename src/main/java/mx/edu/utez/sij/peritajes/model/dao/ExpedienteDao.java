package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.peritajes.model.DocumentoExpediente;
import mx.edu.utez.sij.peritajes.model.Expediente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Datos de la demanda para el expediente del perito (RF 4.2). Las tablas son de Oficialía (demanda_Ofc,
// persona_Ofc, expediente_Ofc, oficialia_Ofc, juzgado_Ofc y documento_Ofc): Peritajes SOLO las lee
// (decisión del equipo: el script integrado dice que Peritajes consulta la demanda).
public class ExpedienteDao {

    // null si la demanda no existe
    public Expediente buscar(String folioDemanda) {
        String sql = "SELECT d.folio, d.descripcion, d.fecha_recepcion, e.num_expediente, j.nombre AS juzgado,"
                + " CONCAT_WS(' ', dte.nombre, dte.apellido_paterno, dte.apellido_materno) AS demandante,"
                + " CONCAT_WS(' ', ddo.nombre, ddo.apellido_paterno, ddo.apellido_materno) AS demandado"
                + " FROM demanda_Ofc d"
                + " LEFT JOIN expediente_Ofc e ON e.folio = d.folio"
                + " LEFT JOIN oficialia_Ofc o ON o.id_oficialia = d.id_oficialia"
                + " LEFT JOIN juzgado_Ofc j ON j.id_juzgado = o.id_juzgado"
                + " LEFT JOIN persona_Ofc dte ON dte.id_persona = d.id_demandante"
                + " LEFT JOIN persona_Ofc ddo ON ddo.id_persona = d.id_demandado"
                + " WHERE d.folio = ?";
        // [Confirmar con Oficialía] documento_Ofc solo tiene ruta_archivo (BLOB), sin nombre ni tipo:
        // por ahora se listan como "Documento N" y no se descargan.
        // Desde la versión 4 de la base, documento_Ofc se liga a la demanda por id_demanda
        String sqlDocumentos = "SELECT doc.id_documento FROM documento_Ofc doc"
                + " JOIN demanda_Ofc d ON d.id_demanda = doc.id_demanda"
                + " WHERE d.folio = ? ORDER BY doc.id_documento";
        if (folioDemanda == null) {
            return null;
        }
        try (Connection con = ConexionBD.obtener()) {
            List<DocumentoExpediente> documentos = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sqlDocumentos)) {
                ps.setString(1, folioDemanda);
                try (ResultSet rs = ps.executeQuery()) {
                    int numero = 1;
                    while (rs.next()) {
                        documentos.add(new DocumentoExpediente(rs.getLong("id_documento"), "Documento " + numero++));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, folioDemanda);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    return new Expediente(rs.getString("folio"), rs.getString("num_expediente"),
                            rs.getString("juzgado"), rs.getString("descripcion"), vacioANull(rs.getString("demandante")),
                            vacioANull(rs.getString("demandado")), Jdbc.fecha(rs, "fecha_recepcion"), documentos);
                }
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo consultar la demanda", e);
        }
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto;
    }
}
