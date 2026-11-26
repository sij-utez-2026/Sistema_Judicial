package mx.edu.utez.sij.segunda.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Toca de segunda instancia (RF 5.1 – 6.2, 7.x).
 */
public class Toca {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private String folioToca;
    private String numeroExpediente;
    private String apelante;
    private String materiaId;
    private String juzgadoOrigenId;
    private String distritoId;
    private Long salaId;
    private Long magistradoUsuarioId;          // se guarda al crear la toca
    private Long magistradoId;                 // FK toca_Seg.id_magistrado, distinto de id_usuario
    private Long documentoApelacionArchivoId;
    private byte[] documentoApelacionPdf;
    private String nombreDocumentoApelacion;
    private EstadoToca estado = EstadoToca.ASIGNADA;
    private LocalDateTime fechaRecepcion;

    // Solo para mostrar en pantalla
    private String materiaNombre;
    private String juzgadoNombre;
    private String distritoNombre;
    private String salaNombre;
    private String magistradoNombre;

    public boolean esResuelta()  { return estado == EstadoToca.RESUELTA; }
    public boolean esFinal()     { return estado != null && estado.esFinal(); }

    /** Para las JSP: fecha legible (LocalDateTime → "19/11/2026 02:38"). */
    public String getFechaRecepcionTexto() {
        return fechaRecepcion == null ? "" : fechaRecepcion.format(FORMATO_FECHA);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFolioToca() { return folioToca; }
    public void setFolioToca(String folioToca) { this.folioToca = folioToca; }

    public String getNumeroExpediente() { return numeroExpediente; }
    public void setNumeroExpediente(String numeroExpediente) { this.numeroExpediente = numeroExpediente; }

    public String getApelante() { return apelante; }
    public void setApelante(String apelante) { this.apelante = apelante; }

    public String getMateriaId() { return materiaId; }
    public void setMateriaId(String materiaId) { this.materiaId = materiaId; }

    public String getJuzgadoOrigenId() { return juzgadoOrigenId; }
    public void setJuzgadoOrigenId(String juzgadoOrigenId) { this.juzgadoOrigenId = juzgadoOrigenId; }

    public String getDistritoId() { return distritoId; }
    public void setDistritoId(String distritoId) { this.distritoId = distritoId; }

    public Long getSalaId() { return salaId; }
    public void setSalaId(Long salaId) { this.salaId = salaId; }

    public Long getMagistradoUsuarioId() { return magistradoUsuarioId; }
    public void setMagistradoUsuarioId(Long magistradoUsuarioId) {
        this.magistradoUsuarioId = magistradoUsuarioId;
    }

    public Long getMagistradoId() { return magistradoId; }
    public void setMagistradoId(Long magistradoId) { this.magistradoId = magistradoId; }

    public Long getDocumentoApelacionArchivoId() { return documentoApelacionArchivoId; }
    public void setDocumentoApelacionArchivoId(Long documentoApelacionArchivoId) {
        this.documentoApelacionArchivoId = documentoApelacionArchivoId;
    }

    public byte[] getDocumentoApelacionPdf() { return documentoApelacionPdf; }
    public void setDocumentoApelacionPdf(byte[] documentoApelacionPdf) { this.documentoApelacionPdf = documentoApelacionPdf; }
    public String getNombreDocumentoApelacion() { return nombreDocumentoApelacion; }
    public void setNombreDocumentoApelacion(String nombreDocumentoApelacion) { this.nombreDocumentoApelacion = nombreDocumentoApelacion; }

    public EstadoToca getEstado() { return estado; }
    public void setEstado(EstadoToca estado) { this.estado = estado; }

    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDateTime fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }

    public String getMateriaNombre() { return materiaNombre; }
    public void setMateriaNombre(String materiaNombre) { this.materiaNombre = materiaNombre; }

    public String getJuzgadoNombre() { return juzgadoNombre; }
    public void setJuzgadoNombre(String juzgadoNombre) { this.juzgadoNombre = juzgadoNombre; }

    public String getDistritoNombre() { return distritoNombre; }
    public void setDistritoNombre(String distritoNombre) { this.distritoNombre = distritoNombre; }

    public String getSalaNombre() { return salaNombre; }
    public void setSalaNombre(String salaNombre) { this.salaNombre = salaNombre; }

    public String getMagistradoNombre() { return magistradoNombre; }
    public void setMagistradoNombre(String magistradoNombre) { this.magistradoNombre = magistradoNombre; }
}
