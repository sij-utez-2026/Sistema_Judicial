package mx.edu.utez.sij.peritajes.model;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

// Solicitud de peritaje que llega de Audiencias (RF 2.1) y su seguimiento hasta el dictamen.
// Tabla per_peritaje (docs/modulos/peritajes.md, sección 4). numeroExpediente y audienciaId son de otros módulos.
public class Peritaje {

    private Long id;
    private String folio;                 // SGPJ-AAAA-NNNN, lo genera el sistema
    private String numeroExpediente;      // de Oficialía; se repite si el expediente pide varios peritajes
    private Long audienciaId;             // de Audiencias
    private Set<Long> materiasIds = new LinkedHashSet<>();
    private String observaciones;         // texto de la solicitud de intervención
    private EstadoPeritaje estado;
    private Long peritoId;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaApertura;  // primera vez que el perito abre el expediente (RF 4.2)
    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaCancelacion;

    // Campos de solo lectura que llena PeritajeDao para las pantallas
    private String juzgado;
    private String materiasTexto;         // "Civil · Familiar"
    private String peritoNombre;
    private String especialidadesPerito;  // "Contabilidad, Auditoría"

    public String getFechaSolicitudTexto() {
        return FormatoFecha.fechaHora(fechaSolicitud);
    }

    public String getFechaAsignacionTexto() {
        return FormatoFecha.fechaHora(fechaAsignacion);
    }

    public String getFechaAperturaTexto() {
        return FormatoFecha.fechaHora(fechaApertura);
    }

    public String getFechaEntregaTexto() {
        return FormatoFecha.fechaHoraSegundos(fechaEntrega);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public void setNumeroExpediente(String numeroExpediente) {
        this.numeroExpediente = numeroExpediente;
    }

    public Long getAudienciaId() {
        return audienciaId;
    }

    public void setAudienciaId(Long audienciaId) {
        this.audienciaId = audienciaId;
    }

    public Set<Long> getMateriasIds() {
        return materiasIds;
    }

    public void setMateriasIds(Set<Long> materiasIds) {
        this.materiasIds = materiasIds;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public EstadoPeritaje getEstado() {
        return estado;
    }

    public void setEstado(EstadoPeritaje estado) {
        this.estado = estado;
    }

    public Long getPeritoId() {
        return peritoId;
    }

    public void setPeritoId(Long peritoId) {
        this.peritoId = peritoId;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getJuzgado() {
        return juzgado;
    }

    public void setJuzgado(String juzgado) {
        this.juzgado = juzgado;
    }

    public String getMateriasTexto() {
        return materiasTexto;
    }

    public void setMateriasTexto(String materiasTexto) {
        this.materiasTexto = materiasTexto;
    }

    public String getPeritoNombre() {
        return peritoNombre;
    }

    public void setPeritoNombre(String peritoNombre) {
        this.peritoNombre = peritoNombre;
    }

    public String getEspecialidadesPerito() {
        return especialidadesPerito;
    }

    public void setEspecialidadesPerito(String especialidadesPerito) {
        this.especialidadesPerito = especialidadesPerito;
    }
}
