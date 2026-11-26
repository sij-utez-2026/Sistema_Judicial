package mx.edu.utez.sij.segunda.model;

/**
 * Estado de la resolución (RF 7.3 y 7.4).
 */
public enum EstadoResolucion {
    VIGENTE("Vigente"),
    ANULADA("Anulada");

    private final String etiqueta;

    EstadoResolucion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() { return etiqueta; }
    public String etiqueta() { return etiqueta; }
}


