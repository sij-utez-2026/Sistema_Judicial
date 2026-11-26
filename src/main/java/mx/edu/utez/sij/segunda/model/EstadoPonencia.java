package mx.edu.utez.sij.segunda.model;

/**
 * Estados de una ponencia (DFR, sección 5).
 *
 * PROGRAMADA   → agendada para el futuro (estado inicial)
 * REALIZADA    → ya se celebró
 * REPROGRAMADA → se movió a otra fecha (la anterior queda como histórico)
 */
public enum EstadoPonencia {
    PROGRAMADA("Programada"),
    REALIZADA("Realizada"),
    REPROGRAMADA("Reprogramada");

    private final String etiqueta;

    EstadoPonencia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }
}


