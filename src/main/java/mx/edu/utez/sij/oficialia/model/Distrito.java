package mx.edu.utez.sij.oficialia.model;

import java.io.Serializable;

public class Distrito implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clave;
    private String nombre;
    private String cabecera;
    private String estado; // "ACTIVO" o "INACTIVO"

    public Distrito() {
        this.estado = "ACTIVO";
    }

    public Distrito(String clave, String nombre, String cabecera, String estado) {
        this.clave = clave;
        this.nombre = nombre;
        this.cabecera = cabecera;
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

    public String getCabecera() {
        return cabecera;
    }

    public void setCabecera(String cabecera) {
        this.cabecera = cabecera;
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
