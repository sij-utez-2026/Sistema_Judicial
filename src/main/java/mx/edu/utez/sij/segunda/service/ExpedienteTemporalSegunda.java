package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Consultas de lectura al expediente y antecedentes de Oficialía/Audiencias vinculados por el esquema. */
public class ExpedienteTemporalSegunda {
    public record ParteTemporalDto(String nombre, String rfc, String rol) { }
    public record AudienciaTemporalDto(Long id, LocalDateTime fechaHora, String tipo, String estado,
                                       String resultado, String acuerdos, Long actaFirmadaArchivoId) { }
    public record DictamenTemporalDto(String folioPeritaje, String peritoNombre, LocalDateTime fechaEntrega,
                                      String contenidoHtml, List<Long> archivosIds) { }
    public record ResolucionPrimeraTemporalDto(String numeroExpediente, String sentido, String resolucion,
                                               LocalDateTime fecha) { }
    public record ExpedienteTemporal(String numeroExpediente, String materia, String tipo, String juzgado,
                                     String estado, LocalDateTime fechaRegistro, List<ParteTemporalDto> partes) { }

    public Optional<ExpedienteTemporal> buscarExpediente(String numero) {
        if (numero == null || numero.isBlank()) return Optional.empty();
        String sql = "SELECT e.num_expediente,e.clasificacion,e.estado,e.fecha_creacion,m.nombre AS materia,j.nombre AS juzgado, "
                + "pd.nombre AS demandante,pd.apellido_paterno AS demandante_paterno,pd.apellido_materno AS demandante_materno,pd.rfc AS demandante_rfc, "
                + "pr.nombre AS demandado,pr.apellido_paterno AS demandado_paterno,pr.apellido_materno AS demandado_materno,pr.rfc AS demandado_rfc "
                + "FROM expediente_Ofc e JOIN demanda_Ofc d ON d.folio=e.folio "
                + "JOIN materia_Ofc m ON m.id_materia=d.id_materia "
                + "JOIN mesa_audiencia_Aud me ON me.id_mesa=e.id_mesa JOIN juzgado_Ofc j ON j.id_juzgado=me.clave_juzgado "
                + "LEFT JOIN persona_Ofc pd ON pd.id_persona=d.id_demandante LEFT JOIN persona_Ofc pr ON pr.id_persona=d.id_demandado "
                + "WHERE e.num_expediente=?";
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, numero.trim());
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return Optional.empty();
                List<ParteTemporalDto> partes = new ArrayList<>();
                String demandante = nombre(r.getString("demandante"), r.getString("demandante_paterno"), r.getString("demandante_materno"));
                String demandado = nombre(r.getString("demandado"), r.getString("demandado_paterno"), r.getString("demandado_materno"));
                if (demandante != null) partes.add(new ParteTemporalDto(demandante, r.getString("demandante_rfc"), "Demandante"));
                if (demandado != null) partes.add(new ParteTemporalDto(demandado, r.getString("demandado_rfc"), "Demandado"));
                var ts = r.getTimestamp("fecha_creacion");
                return Optional.of(new ExpedienteTemporal(r.getString("num_expediente"), r.getString("materia"),
                        r.getString("clasificacion"), r.getString("juzgado"), r.getString("estado"),
                        ts == null ? null : ts.toLocalDateTime(), List.copyOf(partes)));
            }
        } catch (SQLException e) { throw new IllegalStateException("No se pudo consultar el expediente de Oficialía", e); }
    }

    public List<AudienciaTemporalDto> audienciasDeExpediente(String numero) {
        String sql = "SELECT a.id_audiencia,a.fecha_hora_inicio,a.estado_audiencia,a.declaracion_demandante,a.declaracion_demandado,a.declaracion_rep_legales,a.acta_firmada_url "
                + "FROM audiencia_Aud a JOIN expediente_Ofc e ON e.folio=a.folio WHERE e.num_expediente=? ORDER BY a.fecha_hora_inicio";
        List<AudienciaTemporalDto> out = new ArrayList<>();
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, numero); try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    String decl = unir(r.getString("declaracion_demandante"), r.getString("declaracion_demandado"), r.getString("declaracion_rep_legales"));
                    var ts = r.getTimestamp("fecha_hora_inicio");
                    out.add(new AudienciaTemporalDto(r.getLong("id_audiencia"), ts == null ? null : ts.toLocalDateTime(),
                            "Audiencia", r.getString("estado_audiencia"), decl, null, null));
                }
            }
            return List.copyOf(out);
        } catch (SQLException e) { throw new IllegalStateException("No se pudieron consultar las audiencias del expediente", e); }
    }

    public Optional<ResolucionPrimeraTemporalDto> resolucionPrimera(String numero) {
        String sql = "SELECT r.sentido,r.resolucion,r.fecha_resolucion FROM resolucion_demanda_Aud r "
                + "JOIN expediente_Ofc e ON e.folio=r.folio WHERE e.num_expediente=? LIMIT 1";
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, numero); try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return Optional.empty(); var ts = r.getTimestamp("fecha_resolucion");
                return Optional.of(new ResolucionPrimeraTemporalDto(numero, r.getString("sentido"), r.getString("resolucion"), ts == null ? null : ts.toLocalDateTime()));
            }
        } catch (SQLException e) { throw new IllegalStateException("No se pudo consultar la resolución de primera instancia", e); }
    }

    /** El dump recibido no contiene el DDL que relaciona solicitud_perito_Aud con el expediente; no muestra fixtures. */
    public List<DictamenTemporalDto> dictamenesDeExpediente(String numero) { return List.of(); }

    private static String nombre(String a, String b, String c) {
        if (a == null) return null;
        String x = (a + (b == null || b.isBlank() ? "" : " " + b) + (c == null || c.isBlank() ? "" : " " + c)).trim();
        return x.isBlank() ? null : x;
    }
    private static String unir(String... valores) {
        StringBuilder b = new StringBuilder(); for (String v : valores) if (v != null && !v.isBlank()) { if (b.length() > 0) b.append("\n\n"); b.append(v.trim()); } return b.length() == 0 ? null : b.toString();
    }
}
