package mx.edu.utez.sij.copias.model;

/**
 * Estados posibles de una solicitud de copias certificadas (DFR SGCC 2.3 y 5).
 */
public enum EstadoSolicitud {

    LISTO_PARA_PAGO("Listo para pago"),
    EN_ESPERA_VALIDACION("En espera de validación"),
    APROBADO("Aprobado"),
    CANCELADO("Cancelado"),
    PAGADO_EN_PREPARACION("Pagado y en preparación"),
    ENTREGADO("Entregado");

    private final String etiqueta;

    EstadoSolicitud(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Solo se permite el pago cuando la solicitud pública está lista o la privada fue aprobada. */
    public boolean esPagable() {
        return this == LISTO_PARA_PAGO || this == APROBADO;
    }
}
