package mx.edu.utez.sij.peritajes.model;

import java.time.LocalDateTime;
import java.util.List;

// Datos de la demanda que el perito consulta (RF 4.2). Son de Oficialía (demanda_Ofc, persona_Ofc,
// expediente_Ofc y documento_Ofc); Peritajes solo los lee.
public class Expediente {

    private final String folioDemanda;
    private final String numeroExpediente;   // null si la demanda aún no tiene expediente
    private final String juzgado;
    private final String descripcion;
    private final String demandante;
    private final String demandado;
    private final LocalDateTime fechaRecepcion;
    private final List<DocumentoExpediente> documentos;

    public Expediente(String folioDemanda, String numeroExpediente, String juzgado, String descripcion,
                      String demandante, String demandado, LocalDateTime fechaRecepcion,
                      List<DocumentoExpediente> documentos) {
        this.folioDemanda = folioDemanda;
        this.numeroExpediente = numeroExpediente;
        this.juzgado = juzgado;
        this.descripcion = descripcion;
        this.demandante = demandante;
        this.demandado = demandado;
        this.fechaRecepcion = fechaRecepcion;
        this.documentos = documentos;
    }

    public String getFechaRecepcionTexto() {
        return FormatoFecha.fechaHora(fechaRecepcion);
    }

    public String getFolioDemanda() {
        return folioDemanda;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getJuzgado() {
        return juzgado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDemandante() {
        return demandante;
    }

    public String getDemandado() {
        return demandado;
    }

    public List<DocumentoExpediente> getDocumentos() {
        return documentos;
    }
}
