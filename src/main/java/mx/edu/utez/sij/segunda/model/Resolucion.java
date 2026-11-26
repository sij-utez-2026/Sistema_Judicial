package mx.edu.utez.sij.segunda.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Resolución de segunda instancia (RF 7.1 – 7.4).
 *
 * La tabla resolucion_Seg requiere id_toca e id_magistrado y contiene sentido, cuerpo,
 * observaciones, url_pdf y fecha_emision. El esquema recibido no define estado ni versiones.
 * El PDF temporal de la petición se inserta como documento BLOB en documento_Seg; pdfUrl puede ser NULL.
 */
public class Resolucion {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private Long tocaId;
    private Long magistradoId;
    private SentidoResolucion sentido;
    private String pdfUrl;          // columna url_pdf del DDL; NULL para PDF almacenado en documento_Seg
    private byte[] pdfDocumento;
    private String nombrePdf;
    private String comentarios;
    private EstadoResolucion estado = EstadoResolucion.VIGENTE;
    private String motivoAnulacion;
    private LocalDateTime fecha;

    // Solo para mostrar
    private String tocaFolio;
    private String tocaExpediente;

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTocaId() { return tocaId; }
    public void setTocaId(Long tocaId) { this.tocaId = tocaId; }

    public Long getMagistradoId() { return magistradoId; }
    public void setMagistradoId(Long magistradoId) { this.magistradoId = magistradoId; }

    public SentidoResolucion getSentido() { return sentido; }
    public void setSentido(SentidoResolucion sentido) { this.sentido = sentido; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public byte[] getPdfDocumento() { return pdfDocumento; }
    public void setPdfDocumento(byte[] pdfDocumento) { this.pdfDocumento = pdfDocumento; }
    public String getNombrePdf() { return nombrePdf; }
    public void setNombrePdf(String nombrePdf) { this.nombrePdf = nombrePdf; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }

    public EstadoResolucion getEstado() { return estado; }
    public void setEstado(EstadoResolucion estado) { this.estado = estado; }

    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String motivoAnulacion) { this.motivoAnulacion = motivoAnulacion; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getTocaFolio() { return tocaFolio; }
    public void setTocaFolio(String tocaFolio) { this.tocaFolio = tocaFolio; }

    public String getTocaExpediente() { return tocaExpediente; }
    public void setTocaExpediente(String tocaExpediente) { this.tocaExpediente = tocaExpediente; }
}
