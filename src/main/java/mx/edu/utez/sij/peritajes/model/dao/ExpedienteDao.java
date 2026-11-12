package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.peritajes.model.DocumentoExpediente;
import mx.edu.utez.sij.peritajes.model.Expediente;

import java.util.List;
import java.util.Map;

// TEMPORAL: expedientes de prueba mientras Oficialía no implementa ExpedienteApi (docs/CONTRATOS.md 3.2).
// Los datos son los de los bocetos de Figma. Cuando exista el contrato, se usa expedienteApi.buscar(numeroExpediente)
// y esta clase se borra. Los documentos se listan pero no se pueden descargar: eso necesita ArchivoService del núcleo.
public class ExpedienteDao {

    private static final Map<String, Expediente> EXPEDIENTES = Map.of(
            "438/2026", new Expediente("438/2026", "Juzgado Segundo Civil · Cuernavaca", List.of(
                    new DocumentoExpediente("Solicitud_pericial.pdf", "4 páginas · 1.2 MB"),
                    new DocumentoExpediente("Anexo_tecnico_C17.pdf", "8 páginas · 3.4 MB"))),
            "391/2026", new Expediente("391/2026", "Juzgado Primero Familiar de Cuernavaca", List.of()),
            "512/2026", new Expediente("512/2026", "Juzgado 3.º Civil", List.of()),
            "207/2026", new Expediente("207/2026", "Juzgado 1.º Penal", List.of()),
            "184/2026", new Expediente("184/2026", "Juzgado Mercantil", List.of()),
            "365/2026", new Expediente("365/2026", "Juzgado 2.º Civil", List.of()));

    // null si el expediente no existe
    public Expediente buscar(String numeroExpediente) {
        return numeroExpediente == null ? null : EXPEDIENTES.get(numeroExpediente);
    }

    // Nombre del juzgado, o vacío si el expediente no está
    public String juzgado(String numeroExpediente) {
        Expediente expediente = buscar(numeroExpediente);
        return expediente == null ? "" : expediente.getJuzgado();
    }
}
