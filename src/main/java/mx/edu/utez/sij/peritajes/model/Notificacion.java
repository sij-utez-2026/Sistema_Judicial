package mx.edu.utez.sij.peritajes.model;

// Aviso para un usuario (tabla notificacion_Per, RF 5.1). Se muestra en su inicio (dashboard).
public class Notificacion {

    private final Long id;
    private final Long peritajeId;
    private final String evento;
    private final String mensaje;

    public Notificacion(Long id, Long peritajeId, String evento, String mensaje) {
        this.id = id;
        this.peritajeId = peritajeId;
        this.evento = evento;
        this.mensaje = mensaje;
    }

    public Long getId() {
        return id;
    }

    public Long getPeritajeId() {
        return peritajeId;
    }

    public String getEvento() {
        return evento;
    }

    public String getMensaje() {
        return mensaje;
    }
}
