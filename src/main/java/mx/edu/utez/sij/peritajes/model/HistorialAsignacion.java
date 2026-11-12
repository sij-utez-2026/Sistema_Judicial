package mx.edu.utez.sij.peritajes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Cada asignación o reasignación de un peritaje (tabla per_historial_asignacion, peritajes.md sección 4).
// candidatos: los peritos elegibles y su carga en ese momento ("Fundamento de asignación automática").
// [Propuesta] En la base de datos sería una tabla hija per_historial_candidato (historial_id, perito_id, carga_previa).
public class HistorialAsignacion {

    private Long id;
    private Long peritajeId;
    private Long peritoAnteriorId;        // null en la asignación automática
    private Long peritoNuevoId;
    private TipoAsignacion tipo;
    private String motivo;                // obligatorio en la manual (RF 2.3)
    private String realizadoPor;          // TODO núcleo: usuario_id de core_usuario
    private LocalDateTime fecha;
    private List<CandidatoAsignacion> candidatos = new ArrayList<>();

    // Campos de solo lectura que llena HistorialAsignacionDao para la pantalla de detalle
    private String peritoAnteriorNombre;
    private String peritoNuevoNombre;

    public String getFechaTexto() {
        return FormatoFecha.fechaHora(fecha);
    }

    public boolean isAutomatica() {
        return tipo == TipoAsignacion.AUTOMATICA;
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

    public TipoAsignacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoAsignacion tipo) {
        this.tipo = tipo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getRealizadoPor() {
        return realizadoPor;
    }

    public void setRealizadoPor(String realizadoPor) {
        this.realizadoPor = realizadoPor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<CandidatoAsignacion> getCandidatos() {
        return candidatos;
    }

    public void setCandidatos(List<CandidatoAsignacion> candidatos) {
        this.candidatos = candidatos;
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
}
