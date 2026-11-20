package mx.edu.utez.sij.copias.model;

import mx.edu.utez.sij.copias.temporal.ExpedienteApi.TipoExpediente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Solicitud de copia certificada (tabla cop_solicitud, docs/modulos/copias-diccionario-datos.md).
 *
 * Hoy los datos viven en SolicitudCopiaDaoMemoria; con MySQL los guarda SolicitudCopiaDaoMySql (bd/50_copias.sql).
 *
 * Del expediente de Oficialía solo se guardan los datos esenciales (número, tipo, juzgado y materia);
 * las partes se consultan con ExpedienteApi al validar y no se guardan aquí.
 * Columnas *_por: id de core_usuario (FK cuando exista el núcleo).
 */
public class SolicitudCopia {

    private Long id;

    // CC-AAAA-XXXXXX, con parte aleatoria para que no se pueda adivinar
    private String folio;

    // Datos esenciales del expediente (Oficialía, sin FK)
    private String numeroExpediente;

    private TipoExpediente tipo;

    private Long juzgadoId;

    // Nombre del juzgado y de la materia al momento de la solicitud, para el registro y el comprobante
    private String juzgado;

    private Long materiaId;

    private String materia;

    // Solicitante (no tiene cuenta: el trabajador captura sus datos)
    private String solicitanteNombre;

    private String solicitanteRfc;

    private String solicitanteCorreo;

    // INE: id de core_archivo. Solo en expedientes privados
    private Long ineArchivoId;

    private EstadoSolicitud estado;

    private Long tarifaId;

    // Costo congelado al crear la solicitud
    private BigDecimal costo;

    private String motivoRechazo;

    private Long capturadoPor;

    private Long validadoPor;

    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaValidacion;

    private LocalDateTime actualizadoEn;

    public boolean isPrivada() {
        return tipo == TipoExpediente.PRIVADO;
    }

    public String getTipoTexto() {
        return isPrivada() ? "Privado" : "Público";
    }

    public String getCostoTexto() {
        return Formatos.dinero(costo);
    }

    public String getFechaSolicitudTexto() {
        return Formatos.fechaHora(fechaSolicitud);
    }

    public String getFechaValidacionTexto() {
        return Formatos.fechaHora(fechaValidacion);
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

    public TipoExpediente getTipo() {
        return tipo;
    }

    public void setTipo(TipoExpediente tipo) {
        this.tipo = tipo;
    }

    public Long getJuzgadoId() {
        return juzgadoId;
    }

    public void setJuzgadoId(Long juzgadoId) {
        this.juzgadoId = juzgadoId;
    }

    public String getJuzgado() {
        return juzgado;
    }

    public void setJuzgado(String juzgado) {
        this.juzgado = juzgado;
    }

    public Long getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(Long materiaId) {
        this.materiaId = materiaId;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getSolicitanteNombre() {
        return solicitanteNombre;
    }

    public void setSolicitanteNombre(String solicitanteNombre) {
        this.solicitanteNombre = solicitanteNombre;
    }

    public String getSolicitanteRfc() {
        return solicitanteRfc;
    }

    public void setSolicitanteRfc(String solicitanteRfc) {
        this.solicitanteRfc = solicitanteRfc;
    }

    public String getSolicitanteCorreo() {
        return solicitanteCorreo;
    }

    public void setSolicitanteCorreo(String solicitanteCorreo) {
        this.solicitanteCorreo = solicitanteCorreo;
    }

    public Long getIneArchivoId() {
        return ineArchivoId;
    }

    public void setIneArchivoId(Long ineArchivoId) {
        this.ineArchivoId = ineArchivoId;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public Long getTarifaId() {
        return tarifaId;
    }

    public void setTarifaId(Long tarifaId) {
        this.tarifaId = tarifaId;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public Long getCapturadoPor() {
        return capturadoPor;
    }

    public void setCapturadoPor(Long capturadoPor) {
        this.capturadoPor = capturadoPor;
    }

    public Long getValidadoPor() {
        return validadoPor;
    }

    public void setValidadoPor(Long validadoPor) {
        this.validadoPor = validadoPor;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaValidacion() {
        return fechaValidacion;
    }

    public void setFechaValidacion(LocalDateTime fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
