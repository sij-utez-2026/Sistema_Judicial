package mx.edu.utez.sij.segunda.model;

/**
 * Estados permitidos por toca_Seg.estado en el esquema SQL vigente.
 * ASIGNADA es el nombre interno que usa el módulo para el valor persistido "Nueva".
 */
public enum EstadoToca {
    ASIGNADA("Nueva"),
    EN_PONENCIA("En ponencia"),
    RESUELTA("Resuelto");

    private final String etiqueta;

    EstadoToca(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Alias para usar desde EL: ${toca.estado.etiqueta()}. */
    public String etiqueta() {
        return etiqueta;
    }

    public static EstadoToca desdeTexto(String texto) {
        if (texto == null) return ASIGNADA;
        for (EstadoToca e : values()) {
            if (e.name().equalsIgnoreCase(texto) || e.etiqueta.equalsIgnoreCase(texto)) {
                return e;
            }
        }
        return ASIGNADA;
    }

    public boolean esFinal() {
        return this == RESUELTA;
    }
}
