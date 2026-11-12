package mx.edu.utez.sij.peritajes.model;

// Materia del catálogo judicial (Civil, Familiar...). El catálogo es de Oficialía (docs/CONTRATOS.md 3.1);
// mientras no exista CatalogoJudicialApi, MateriaDao tiene las materias de prueba.
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
