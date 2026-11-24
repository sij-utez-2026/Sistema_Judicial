package mx.edu.utez.sij.peritajes.model;

// Un documento anexo de la demanda (tabla documento_Ofc de Oficialía)
public class DocumentoExpediente {

    private final Long id;
    private final String nombre;

    public DocumentoExpediente(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
