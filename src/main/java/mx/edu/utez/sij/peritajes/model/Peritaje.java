package mx.edu.utez.sij.peritajes.model;

import java.time.LocalDateTime;

// Peritaje (tabla peritaje_Per): atiende una solicitud de Audiencias (solicitud_perito_Aud) hasta el dictamen.
// La demanda y la materia salen de la solicitud; el perito, de la asignación vigente (asignacion_Per).
// El folio del peritaje es su id (decisión del equipo).
public class Peritaje {

    private Long id;
    private Long solicitudId;             // id_solicitud_perito (Audiencias)
    private EstadoPeritaje estado;
    private LocalDateTime fechaSolicitud; // fecha_creacion
    private LocalDateTime fechaApertura;  // primera vez que el perito abre el expediente (RF 4.2)
    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaCancelacion;

    // De la solicitud y la demanda (solo lectura)
    private String folioDemanda;
    private String numeroExpediente;      // null si la demanda aún no tiene expediente
    private String claveMateria;
    private String materia;               // nombre: "Civil"
    private String descripcionDemanda;
    private String juzgado;

    // De la asignación vigente (solo lectura)
    private Long peritoId;
    private String peritoNombre;
    private String especialidadesPerito;  // "Contabilidad, Auditoría"
    private LocalDateTime fechaAsignacion;

    public String getFolio() {
        return id == null ? "" : String.valueOf(id);
    }

    // "Exp. 438/2026" o, si no hay expediente, "Demanda PER-PRB-0438"
    public String getReferenciaDemanda() {
        if (numeroExpediente != null && !numeroExpediente.isBlank()) {
            return "Exp. " + numeroExpediente;
        }
        return "Demanda " + folioDemanda;
    }

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

    public String getPeritoNombreTexto() {
        return peritoNombre == null ? "Sin asignar" : peritoNombre;
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

    public EstadoPeritaje getEstado() {
        return estado;
    }

    public void setEstado(EstadoPeritaje estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
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

    public String getFolioDemanda() {
        return folioDemanda;
    }

    public void setFolioDemanda(String folioDemanda) {
        this.folioDemanda = folioDemanda;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public void setNumeroExpediente(String numeroExpediente) {
        this.numeroExpediente = numeroExpediente;
    }

    public String getClaveMateria() {
        return claveMateria;
    }

    public void setClaveMateria(String claveMateria) {
        this.claveMateria = claveMateria;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getDescripcionDemanda() {
        return descripcionDemanda;
    }

    public void setDescripcionDemanda(String descripcionDemanda) {
        this.descripcionDemanda = descripcionDemanda;
    }

    public String getJuzgado() {
        return juzgado;
    }

    public void setJuzgado(String juzgado) {
        this.juzgado = juzgado;
    }

    public Long getPeritoId() {
        return peritoId;
    }

    public void setPeritoId(Long peritoId) {
        this.peritoId = peritoId;
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

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
}
