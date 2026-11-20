package mx.edu.utez.sij.oficialia.model;


import java.io.Serializable;

public class Oficialia implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clave;
    private String nombre;
    private String juzgado;
    private String distrito;
    private String titular;
    private String horario;
    private String estado; // "Activo" o "Inactivo"

    public Oficialia() {
        this.estado = "Activo";
    }

    public Oficialia(String clave, String nombre, String juzgado, String estado) {
        this.clave = clave;
        this.nombre = nombre;
        this.juzgado = juzgado;
        this.estado = estado != null ? estado : "Activo";
    }

    public Oficialia(String clave, String nombre, String juzgado, String distrito, String titular, String horario, String estado) {
        this.clave = clave;
        this.nombre = nombre;
        this.juzgado = juzgado;
        this.distrito = distrito;
        this.titular = titular;
        this.horario = horario;
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

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
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

