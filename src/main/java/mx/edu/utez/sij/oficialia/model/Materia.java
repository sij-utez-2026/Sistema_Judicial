package mx.edu.utez.sij.oficialia.model;

import java.io.Serializable;

public class Materia implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clave;
    private String nombre;
    private String descripcion;
    private String estado; // "Activo" o "Inactivo"

    public Materia() {
        this.estado = "Activo";
    }

    public Materia(String clave, String nombre, String descripcion) {
        this.clave = clave;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = "Activo";
    }

    public Materia(String clave, String nombre, String descripcion, String estado) {
        this.clave = clave;
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

