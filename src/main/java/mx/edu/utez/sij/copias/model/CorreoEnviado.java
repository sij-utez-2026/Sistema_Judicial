package mx.edu.utez.sij.copias.model;


import java.time.LocalDateTime;

/**
 * Registro de los correos enviados al solicitante (tabla cop_correo) [Propuesta].
 * Sirve para saber si ya se envió el comprobante o el aviso, y para reintentar si falló.
 */
public class CorreoEnviado {

    private Long id;

    private Long solicitudId;

    private TipoCorreo tipo;

    private String destinatario;

    private Long enviadoPor;

    private LocalDateTime enviadoEn;

    private boolean exito;

    public String getEnviadoEnTexto() {
        return Formatos.fechaHora(enviadoEn);
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

    public TipoCorreo getTipo() {
        return tipo;
    }

    public void setTipo(TipoCorreo tipo) {
        this.tipo = tipo;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public Long getEnviadoPor() {
        return enviadoPor;
    }

    public void setEnviadoPor(Long enviadoPor) {
        this.enviadoPor = enviadoPor;
    }

    public LocalDateTime getEnviadoEn() {
        return enviadoEn;
    }

    public void setEnviadoEn(LocalDateTime enviadoEn) {
        this.enviadoEn = enviadoEn;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }
}
