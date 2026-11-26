package mx.edu.utez.sij.segunda.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Versión anterior del PDF de una resolución (RF 7.3).
 * Guarda el PDF que se reemplazó, el motivo y la fecha.
 */
public class VersionResolucion {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private Long resolucionId;
    private String pdfUrl;
    private String motivo;
    private LocalDateTime fecha;

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResolucionId() { return resolucionId; }
    public void setResolucionId(Long resolucionId) { this.resolucionId = resolucionId; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}


