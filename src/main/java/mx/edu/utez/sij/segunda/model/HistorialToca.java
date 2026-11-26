package mx.edu.utez.sij.segunda.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Renglón del historial de una toca (RF 6.2). Es inalterable.
 */
public class HistorialToca {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private Long tocaId;
    private LocalDateTime fecha;
    private Long usuarioId;
    private String usuarioNombre;
    private String accion;
    private String detalle;

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTocaId() { return tocaId; }
    public void setTocaId(Long tocaId) { this.tocaId = tocaId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}

