package mx.edu.utez.sij.peritajes.model;

// Materia del catálogo judicial de Oficialía (tabla materia_Ofc): clave (CIV, FAM...) y nombre (Civil, Familiar...).
public class Materia {

    private final String clave;
    private final String nombre;

    public Materia(String clave, String nombre) {
        this.clave = clave;
        this.nombre = nombre;
    }

    public String getClave() {
        return clave;
    }

    public String getNombre() {
        return nombre;
    }
}
