package mx.edu.utez.sij.peritajes.model;

import java.time.LocalDateTime;

public class RegistroBitacora {

    private final LocalDateTime fecha;
    private final String usuario;
    private final String rol;
    private final String evento;
    private final String referencia;   // folio del peritaje o número de registro del perito
    private final String detalle;

    public RegistroBitacora(LocalDateTime fecha, String usuario, String rol, String evento,
                            String referencia, String detalle) {
        this.fecha = fecha;
        this.usuario = usuario;
        this.rol = rol;
        this.evento = evento;
        this.referencia = referencia;
        this.detalle = detalle;
    }

    public String getFechaTexto() {
        return FormatoFecha.fecha(fecha);
    }

    public String getHoraTexto() {
        return FormatoFecha.hora(fecha) + " h";
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }

    public String getEvento() {
        return evento;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getDetalle() {
        return detalle;
    }
}
