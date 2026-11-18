package mx.edu.utez.sij.copias.model;

/**
 * Tipo de demanda según Oficialía de Partes.
 */
public enum TipoDemanda {

    PUBLICA("Pública"),
    PRIVADA("Privada");

    private final String etiqueta;

    TipoDemanda(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
