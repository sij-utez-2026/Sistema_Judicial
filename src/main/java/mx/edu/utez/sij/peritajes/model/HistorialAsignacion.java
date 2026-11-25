package mx.edu.utez.sij.peritajes.model;

import java.time.LocalDateTime;

// Cada asignación o reasignación de un peritaje (tabla asignacion_Per).
// Sin perito saliente = asignación automática del sistema (RF 2.2); con perito saliente = reasignación manual (RF 2.3).
// En la manual, id_usuario_cambio es quien la hizo; NULL = el administrador general (no está en user_Per).
public class HistorialAsignacion {

    private Long id;
    private Long peritajeId;
    private Long peritoAnteriorId;        // id_perito_saliente; null en la asignación automática
    private Long peritoNuevoId;           // id_perito_asignado
    private Long realizadoPorId;          // id_usuario_cambio; null = el sistema
    private String motivo;                // obligatorio en la manual (RF 2.3)
    private LocalDateTime fecha;

    // Nombres (los llena HistorialAsignacionDao)
    private String peritoAnteriorNombre;
    private String peritoNuevoNombre;
    private String realizadoPor;

    public String getFechaTexto() {
        return FormatoFecha.fechaHora(fecha);
    }

    public boolean isAutomatica() {
        return peritoAnteriorId == null;
    }

    public TipoAsignacion getTipo() {
        return isAutomatica() ? TipoAsignacion.AUTOMATICA : TipoAsignacion.MANUAL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPeritajeId() {
        return peritajeId;
    }

    public void setPeritajeId(Long peritajeId) {
        this.peritajeId = peritajeId;
    }

    public Long getPeritoAnteriorId() {
        return peritoAnteriorId;
    }

    public void setPeritoAnteriorId(Long peritoAnteriorId) {
        this.peritoAnteriorId = peritoAnteriorId;
    }

    public Long getPeritoNuevoId() {
        return peritoNuevoId;
    }

    public void setPeritoNuevoId(Long peritoNuevoId) {
        this.peritoNuevoId = peritoNuevoId;
    }

    public Long getRealizadoPorId() {
        return realizadoPorId;
    }

    public void setRealizadoPorId(Long realizadoPorId) {
        this.realizadoPorId = realizadoPorId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getPeritoAnteriorNombre() {
        return peritoAnteriorNombre;
    }

    public void setPeritoAnteriorNombre(String peritoAnteriorNombre) {
        this.peritoAnteriorNombre = peritoAnteriorNombre;
    }

    public String getPeritoNuevoNombre() {
        return peritoNuevoNombre;
    }

    public void setPeritoNuevoNombre(String peritoNuevoNombre) {
        this.peritoNuevoNombre = peritoNuevoNombre;
    }

    public String getRealizadoPor() {
        return realizadoPor;
    }

    public void setRealizadoPor(String realizadoPor) {
        this.realizadoPor = realizadoPor;
    }
}
