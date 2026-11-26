package mx.edu.utez.sij.oficialia.model;


import java.io.Serializable;

public class Distrito implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clave;
    private String nombre;
    private String cabecera;
    private String municipios;
    private String estado; // "Activo" o "Inactivo"

    public Distrito() {
        this.estado = "Activo";
    }

    public Distrito(String clave, String nombre, String estado) {
        this.clave = clave;
        this.nombre = nombre;
        this.estado = estado != null ? estado : "Activo";
    }

    public Distrito(String clave, String nombre, String cabecera, String municipios, String estado) {
        this.clave = clave;
        this.nombre = nombre;
        this.cabecera = cabecera;
        this.municipios = municipios;
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

    public String getCabecera() {
        return cabecera;
    }

    public void setCabecera(String cabecera) {
        this.cabecera = cabecera;
    }

    public String getMunicipios() {
        return municipios;
    }

    public void setMunicipios(String municipios) {
        this.municipios = municipios;
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
