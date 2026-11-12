package mx.edu.utez.sij.peritajes.model;

// Un documento del expediente: nombre y detalle ("4 páginas · 1.2 MB")
public class DocumentoExpediente {

    private final String nombre;
    private final String detalle;

    public DocumentoExpediente(String nombre, String detalle) {
        this.nombre = nombre;
        this.detalle = detalle;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDetalle() {
        return detalle;
    }
}
