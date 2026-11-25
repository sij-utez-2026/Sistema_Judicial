package mx.edu.utez.sij.peritajes.model;

// Estados del peritaje (docs/modulos/peritajes.md, sección 5; DFR regla de negocio 1).
// El texto es exactamente el valor del ENUM de peritaje_Per.estado_peritaje.
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

    // Texto para mostrar en pantalla y valor en la base de datos
    public String getTexto() {
        return texto;
    }

    // Carga activa = Asignado + En Proceso (regla de negocio 3 del DFR)
    public boolean isActivo() {
        return this == ASIGNADO || this == EN_PROCESO;
    }

    // "EN_PROCESO" (parámetro de la URL) → EN_PROCESO; vacío o desconocido → null (sin filtro)
    public static EstadoPeritaje deTexto(String texto) {
        for (EstadoPeritaje estado : values()) {
            if (estado.name().equals(texto)) {
                return estado;
            }
        }
        return null;
    }

    // "En Proceso" (valor de la base de datos) → EN_PROCESO
    public static EstadoPeritaje deBaseDatos(String valor) {
        for (EstadoPeritaje estado : values()) {
            if (estado.texto.equals(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de peritaje desconocido: " + valor);
    }
}
