package mx.edu.utez.sij.oficialia.model;

import java.io.Serializable;

public class Oficialia implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clave;
    private String nombre;
    private String claveJuzgado;
    private String juzgado;
    private String distrito;
    private Long adminId;
    private String adminNombre;
    private String adminCorreo;
    private String estado; // "ACTIVO" o "INACTIVO"

    public Oficialia() {
        this.estado = "ACTIVO";
    }

    public Oficialia(String clave, String nombre, String claveJuzgado, String estado) {
        this.clave = clave;
        this.nombre = nombre;
        this.claveJuzgado = claveJuzgado;
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

    public String getClaveJuzgado() {
        return claveJuzgado;
    }

    public void setClaveJuzgado(String claveJuzgado) {
        this.claveJuzgado = claveJuzgado;
    }

    public String getJuzgado() {
        return juzgado;
    }

    public void setJuzgado(String juzgado) {
        this.juzgado = juzgado;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminNombre() {
        return adminNombre;
    }

    public void setAdminNombre(String adminNombre) {
        this.adminNombre = adminNombre;
    }

    public String getAdminCorreo() {
        return adminCorreo;
    }

    public void setAdminCorreo(String adminCorreo) {
        this.adminCorreo = adminCorreo;
    }

    public boolean isTieneAdministrador() {
        return adminNombre != null && !adminNombre.isBlank();
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
