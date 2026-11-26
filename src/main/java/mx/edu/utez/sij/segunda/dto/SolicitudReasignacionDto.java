package mx.edu.utez.sij.segunda.dto;

public class SolicitudReasignacionDto {
    private final Long id;
    private final Long tocaId;
    private final String folioToca;
    private final String salaOrigen;
    private final String salaDestino;
    private final String motivo;
    private final String solicitante;

    public SolicitudReasignacionDto(Long id, Long tocaId, String folioToca, String salaOrigen,
                                    String salaDestino, String motivo, String solicitante) {
        this.id = id;
        this.tocaId = tocaId;
        this.folioToca = folioToca;
        this.salaOrigen = salaOrigen;
        this.salaDestino = salaDestino;
        this.motivo = motivo;
        this.solicitante = solicitante;
    }

    public Long getId() { return id; }
    public Long getTocaId() { return tocaId; }
    public String getFolioToca() { return folioToca; }
    public String getSalaOrigen() { return salaOrigen; }
    public String getSalaDestino() { return salaDestino; }
    public String getMotivo() { return motivo; }
    public String getSolicitante() { return solicitante; }
}
