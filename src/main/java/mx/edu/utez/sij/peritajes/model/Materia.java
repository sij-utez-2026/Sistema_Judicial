package mx.edu.utez.sij.peritajes.model;

// Materia del catálogo judicial de Oficialía (tabla materia_Ofc): id (id_materia, numérico desde la
// versión 4 de la base) y nombre (Civil, Familiar...).
// En perito_materia_Per y solicitud_perito_Aud la columna se sigue llamando clave_materia, pero guarda este id.
public class Materia {

    private final Long id;
    private final String nombre;

    public Materia(Long id, String nombre) {
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
