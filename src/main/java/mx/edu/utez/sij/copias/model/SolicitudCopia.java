package mx.edu.utez.sij.copias.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Solicitud de copias certificadas (tabla cop_solicitud).
 */
public class SolicitudCopia {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm 'h'", Locale.forLanguageTag("es-MX"));

    private Long id;
    private String folioSolicitud;
    private String numeroExpediente;
    private Long juzgadoId;
    private String juzgadoNombre;
    private Long materiaId;
    private String materiaNombre;
    private TipoDemanda tipo;
    private EstadoSolicitud estado;
    private BigDecimal costo;
    private LocalDateTime fechaSolicitud;

    public SolicitudCopia() {
    }

    public SolicitudCopia(Long id, String folioSolicitud, String numeroExpediente,
                          Long juzgadoId, String juzgadoNombre, Long materiaId, String materiaNombre,
                          TipoDemanda tipo, EstadoSolicitud estado, BigDecimal costo, LocalDateTime fechaSolicitud) {
        this.id = id;
        this.folioSolicitud = folioSolicitud;
        this.numeroExpediente = numeroExpediente;
        this.juzgadoId = juzgadoId;
        this.juzgadoNombre = juzgadoNombre;
        this.materiaId = materiaId;
        this.materiaNombre = materiaNombre;
        this.tipo = tipo;
        this.estado = estado;
        this.costo = costo;
        this.fechaSolicitud = fechaSolicitud;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFolioSolicitud() {
        return folioSolicitud;
    }

    public void setFolioSolicitud(String folioSolicitud) {
        this.folioSolicitud = folioSolicitud;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public void setNumeroExpediente(String numeroExpediente) {
        this.numeroExpediente = numeroExpediente;
    }

    public Long getJuzgadoId() {
        return juzgadoId;
    }

    public void setJuzgadoId(Long juzgadoId) {
        this.juzgadoId = juzgadoId;
    }

    public String getJuzgadoNombre() {
        return juzgadoNombre;
    }

    public void setJuzgadoNombre(String juzgadoNombre) {
        this.juzgadoNombre = juzgadoNombre;
    }

    public Long getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(Long materiaId) {
        this.materiaId = materiaId;
    }

    public String getMateriaNombre() {
        return materiaNombre;
    }

    public void setMateriaNombre(String materiaNombre) {
        this.materiaNombre = materiaNombre;
    }

    public TipoDemanda getTipo() {
        return tipo;
    }

    public void setTipo(TipoDemanda tipo) {
        this.tipo = tipo;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    /** Monto en centavos para Stripe Checkout (por ejemplo $150.00 MXN -> 15000). */
    public long getCostoCentavos() {
        if (costo == null) {
            return 0L;
        }
        return costo.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    }

    public String getCostoFormateado() {
        if (costo == null) {
            return "$0.00 MXN";
        }
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "$" + formato.format(costo.setScale(2, RoundingMode.HALF_UP)) + " MXN";
    }

    public String getFechaSolicitudFormateada() {
        return fechaSolicitud == null ? "" : fechaSolicitud.format(FORMATO_FECHA);
    }

    public boolean isPagable() {
        return estado != null && estado.esPagable();
    }
}
