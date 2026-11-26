package mx.edu.utez.sij.segunda.model;

/**
 * Sentido de la resolución (RF 7.1).
 */
public enum SentidoResolucion {
    CONFIRMA("Confirma"),
    REVOCA("Revoca"),
    MODIFICA("Modifica");

    private final String etiqueta;

    SentidoResolucion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() { return etiqueta; }
    public String etiqueta() { return etiqueta; }

    public static SentidoResolucion desdeTexto(String texto) {
        if (texto == null) return CONFIRMA;
        for (SentidoResolucion s : values()) {
            if (s.name().equalsIgnoreCase(texto) || s.etiqueta.equalsIgnoreCase(texto)) {
                return s;
            }
        }
        return CONFIRMA;
    }
}


