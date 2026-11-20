package mx.edu.utez.sij.copias.model;


import java.time.LocalDateTime;

/**
 * Entrega física de la copia (tabla cop_entrega). Después de entregar, el folio queda inhabilitado (RN-03).
 */
public class Entrega {

    private Long id;

    private Long solicitudId;

    private Long entregadoPor;

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

    public Long getEntregadoPor() {
        return entregadoPor;
    }

    public void setEntregadoPor(Long entregadoPor) {
        this.entregadoPor = entregadoPor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
