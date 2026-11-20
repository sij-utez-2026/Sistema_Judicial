package mx.edu.utez.sij.copias.model;


import java.time.LocalDateTime;

/** Cada cambio de estado de una solicitud: quién y cuándo (tabla cop_solicitud_historial). */
public class HistorialSolicitud {

    private Long id;

    private Long solicitudId;

    // Nulo cuando se crea la solicitud
    private EstadoSolicitud estadoAnterior;

    private EstadoSolicitud estadoNuevo;

    private Long usuarioId;

    private String comentario;

    private LocalDateTime fecha;

    public String getFechaTexto() {
        return Formatos.fechaHora(fecha);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }

    public EstadoSolicitud getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoSolicitud estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoSolicitud getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoSolicitud estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
