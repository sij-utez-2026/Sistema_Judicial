package mx.edu.utez.sij.peritajes.model;

import java.util.List;

// Los datos del expediente que Peritajes muestra (número, juzgado y documentos). El expediente es de Oficialía:
// cuando exista ExpedienteApi (docs/CONTRATOS.md 3.2) se usa su ExpedienteDto y ExpedienteDao se borra.
public class Expediente {

    private final String numeroExpediente;
    private final String juzgado;
    private final List<DocumentoExpediente> documentos;

    public Expediente(String numeroExpediente, String juzgado, List<DocumentoExpediente> documentos) {
        this.numeroExpediente = numeroExpediente;
        this.juzgado = juzgado;
        this.documentos = documentos;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getJuzgado() {
        return juzgado;
    }

    public List<DocumentoExpediente> getDocumentos() {
        return documentos;
    }
}
