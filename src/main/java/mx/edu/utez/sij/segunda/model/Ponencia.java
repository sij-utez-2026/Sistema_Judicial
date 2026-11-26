package mx.edu.utez.sij.segunda.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Ponencia de una toca (RF 5.2).
 *
 * Una toca puede tener varias ponencias; el historial no se borra.
 * La fecha y la hora se exponen juntas como `fechaHora`; SQL las guarda separadas.
 * La duración es fija de 1 hora (el DFR no dice duración, pero se necesita para detectar choques).
 *
 * Tabla SQL: ponencia_Seg usa id_toca, id_magistrado, fecha_sesion, hora_inicio y hora_fin.
 */
public class Ponencia {

    public static final int DURACION_HORAS = 1;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA  = DateTimeFormatter.ofPattern("HH:mm");

    private Long id;
    private Long tocaId;
    private Long magistradoId;
    private LocalDateTime fechaHora;
    private EstadoPonencia estado = EstadoPonencia.PROGRAMADA;

    // Solo para mostrar en pantalla
    private String tocaFolio;

    // -------- helpers de fecha/hora para las JSP --------
    public LocalDate getFecha() {
        return fechaHora == null ? null : fechaHora.toLocalDate();
    }

    public LocalTime getHora() {
        return fechaHora == null ? null : fechaHora.toLocalTime();
    }

    public LocalTime getHoraFin() {
        return fechaHora == null ? null : fechaHora.toLocalTime().plusHours(DURACION_HORAS);
    }

    public String getFechaTexto() {
        return fechaHora == null ? "" : fechaHora.format(FORMATO_FECHA);
    }

    public String getHoraTexto() {
        return fechaHora == null ? "" : fechaHora.format(FORMATO_HORA);
    }

    public String getHorarioTexto() {
        if (fechaHora == null) return "";
        return getFechaTexto() + " · " + getHoraTexto() + " – "
                + fechaHora.toLocalTime().plusHours(DURACION_HORAS).format(FORMATO_HORA);
    }

    public boolean esFutura() {
        return fechaHora != null && fechaHora.isAfter(LocalDateTime.now());
    }

    public boolean esHoy() {
        return fechaHora != null && fechaHora.toLocalDate().equals(LocalDate.now());
    }

    // -------- getters y setters --------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTocaId() { return tocaId; }
    public void setTocaId(Long tocaId) { this.tocaId = tocaId; }

    public Long getMagistradoId() { return magistradoId; }
    public void setMagistradoId(Long magistradoId) { this.magistradoId = magistradoId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public EstadoPonencia getEstado() { return estado; }
    public void setEstado(EstadoPonencia estado) { this.estado = estado; }

    public String getTocaFolio() { return tocaFolio; }
    public void setTocaFolio(String tocaFolio) { this.tocaFolio = tocaFolio; }
}

