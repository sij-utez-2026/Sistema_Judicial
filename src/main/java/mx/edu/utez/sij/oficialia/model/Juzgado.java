package mx.edu.utez.sij.oficialia.model;


import java.io.Serializable;

public class Juzgado implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clave;
    private String nombre;
    private String distrito;
    private String materia;
    private String estado; // "Activo" o "Inactivo"

    public Juzgado() {
        this.estado = "Activo";
    }

    public Juzgado(String clave, String nombre, String distrito, String estado) {
        this.clave = clave;
        this.nombre = nombre;
        this.distrito = distrito;
        this.estado = estado != null ? estado : "Activo";
    }

    public Juzgado(String clave, String nombre, String distrito, String materia, String estado) {
        this.clave = clave;
        this.nombre = nombre;
        this.distrito = distrito;
        this.materia = materia;
        this.estado = estado != null ? estado : "Activo";
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getEstado() {
        return estado != null ? estado : "Activo";
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isActivo() {
        return "Activo".equalsIgnoreCase(this.estado);
    }
}
