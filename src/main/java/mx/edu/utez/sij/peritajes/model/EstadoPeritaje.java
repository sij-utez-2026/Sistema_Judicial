package mx.edu.utez.sij.peritajes.model;

// Estados del peritaje (docs/modulos/peritajes.md, sección 5; DFR regla de negocio 1)
public enum EstadoPeritaje {

    PENDIENTE_ASIGNACION("Pendiente de Asignación"),
    ASIGNADO("Asignado"),
    EN_PROCESO("En Proceso"),
    DICTAMEN_ENTREGADO("Dictamen Entregado"),
    CANCELADO("Cancelado");

    private final String texto;

    EstadoPeritaje(String texto) {
        this.texto = texto;
    }

    // Texto para mostrar en pantalla
    public String getTexto() {
        return texto;
    }

    // Carga activa = Asignado + En Proceso (regla de negocio 3 del DFR)
    public boolean isActivo() {
        return this == ASIGNADO || this == EN_PROCESO;
    }

    // "EN_PROCESO" → EN_PROCESO; vacío o desconocido → null (sin filtro)
    public static EstadoPeritaje deTexto(String texto) {
        for (EstadoPeritaje estado : values()) {
            if (estado.name().equals(texto)) {
                return estado;
            }
        }
        return null;
    }
}
