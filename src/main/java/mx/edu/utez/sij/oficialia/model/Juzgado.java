package mx.edu.utez.sij.oficialia.model;

import java.io.Serializable;

public class Juzgado implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clave;
    private String nombre;
    private String claveDistrito;
    private String distrito; // Nombre del distrito para mostrar en tabla
    private String estado;   // "ACTIVO" o "INACTIVO"

    public Juzgado() {
        this.estado = "ACTIVO";
    }

    public Juzgado(String clave, String nombre, String claveDistrito, String distrito, String estado) {
        this.clave = clave;
        this.nombre = nombre;
        this.claveDistrito = claveDistrito;
        this.distrito = distrito;
        this.estado = estado != null ? estado : "ACTIVO";
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

    public String getClaveDistrito() {
        return claveDistrito;
    }

    public void setClaveDistrito(String claveDistrito) {
        this.claveDistrito = claveDistrito;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getEstado() {
        return estado != null ? estado : "ACTIVO";
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isActivo() {
        return "ACTIVO".equalsIgnoreCase(this.estado);
    }

    public String getEstadoTexto() {
        return isActivo() ? "Activo" : "Inactivo";
    }
}
