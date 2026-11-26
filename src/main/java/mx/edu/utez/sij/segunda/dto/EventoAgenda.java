package mx.edu.utez.sij.segunda.dto;

import java.time.LocalDateTime;

/**
 * Un renglón de la agenda: una ponencia programada vista como evento de calendario.
 * Solo lectura.
 */
public record EventoAgenda(
        Long ponenciaId,
        Long tocaId,
        String folioToca,
        String numeroExpediente,
        String salaNombre,
        String magistradoNombre,
        LocalDateTime fechaHora,
        String estado
) {
    /** Getters JavaBean para acceso seguro desde Jakarta EL/JSTL. */
    public Long getPonenciaId() { return ponenciaId; }
    public Long getTocaId() { return tocaId; }
    public String getFolioToca() { return folioToca; }
    public String getNumeroExpediente() { return numeroExpediente; }
    public String getSalaNombre() { return salaNombre; }
    public String getMagistradoNombre() { return magistradoNombre; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getEstado() { return estado; }

    public String getHoraTexto() {
        return fechaHora == null ? "" :
                String.format("%02d:%02d", fechaHora.getHour(), fechaHora.getMinute());
    }

    public String getDescripcion() {
        return folioToca + " · " + numeroExpediente;
    }

    public String getEstadoClase() {
        return estado == null ? "" : "estado-" + estado;
    }
}

