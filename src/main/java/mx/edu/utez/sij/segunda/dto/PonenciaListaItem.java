package mx.edu.utez.sij.segunda.dto;

import mx.edu.utez.sij.segunda.model.EstadoPonencia;
import mx.edu.utez.sij.segunda.model.Ponencia;

/** Proyección de ponencia y datos de la toca para la pantalla de listado. */
public final class PonenciaListaItem {

    private final Long id;
    private final Long tocaId;
    private final String folioToca;
    private final String numeroExpediente;
    private final String horarioTexto;
    private final EstadoPonencia estado;

    public PonenciaListaItem(Ponencia ponencia, String folioToca, String numeroExpediente) {
        this.id = ponencia.getId();
        this.tocaId = ponencia.getTocaId();
        this.folioToca = folioToca;
        this.numeroExpediente = numeroExpediente;
        this.horarioTexto = ponencia.getHorarioTexto();
        this.estado = ponencia.getEstado();
    }

    public Long getId() { return id; }
    public Long getTocaId() { return tocaId; }
    public String getFolioToca() { return folioToca; }
    public String getNumeroExpediente() { return numeroExpediente; }
    public String getHorarioTexto() { return horarioTexto; }
    public EstadoPonencia getEstado() { return estado; }
}
