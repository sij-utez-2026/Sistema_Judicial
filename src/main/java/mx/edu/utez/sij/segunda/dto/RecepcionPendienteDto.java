package mx.edu.utez.sij.segunda.dto;

/** Datos de origen leídos de Oficialía para una recepción; no son editables en el formulario. */
public class RecepcionPendienteDto {
    private final String numeroExpediente;
    private final String materiaId;
    private final String materiaNombre;
    private final String distritoId;
    private final String distritoNombre;
    private final String juzgadoOrigenId;
    private final String juzgadoNombre;
    private final Long salaId;
    private final String salaNombre;
    private final String demandante;
    private final String demandado;

    public RecepcionPendienteDto(String numeroExpediente, String materiaId, String materiaNombre,
                                 String distritoId, String distritoNombre, String juzgadoOrigenId,
                                 String juzgadoNombre, Long salaId, String salaNombre,
                                 String demandante, String demandado) {
        this.numeroExpediente = numeroExpediente;
        this.materiaId = materiaId;
        this.materiaNombre = materiaNombre;
        this.distritoId = distritoId;
        this.distritoNombre = distritoNombre;
        this.juzgadoOrigenId = juzgadoOrigenId;
        this.juzgadoNombre = juzgadoNombre;
        this.salaId = salaId;
        this.salaNombre = salaNombre;
        this.demandante = demandante;
        this.demandado = demandado;
    }

    public String getNumeroExpediente() { return numeroExpediente; }
    public String getMateriaId() { return materiaId; }
    public String getMateriaNombre() { return materiaNombre; }
    public String getDistritoId() { return distritoId; }
    public String getDistritoNombre() { return distritoNombre; }
    public String getJuzgadoOrigenId() { return juzgadoOrigenId; }
    public String getJuzgadoNombre() { return juzgadoNombre; }
    public Long getSalaId() { return salaId; }
    public String getSalaNombre() { return salaNombre; }
    public String getDemandante() { return demandante; }
    public String getDemandado() { return demandado; }
}
