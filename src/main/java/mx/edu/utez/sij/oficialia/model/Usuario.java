package mx.edu.utez.sij.oficialia.model;

import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correo;
    private String rol;
    private boolean activo;

    public Usuario() {
        this.activo = true;
    }

    public Usuario(String nombre, String apellidoPaterno, String apellidoMaterno, String correo, String rol) {
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.correo = correo;
        this.rol = rol;
        this.activo = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        if (nombre != null && !nombre.isBlank()) {
            sb.append(nombre.trim());
        }
        if (apellidoPaterno != null && !apellidoPaterno.isBlank()) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(apellidoPaterno.trim());
        }
        if (apellidoMaterno != null && !apellidoMaterno.isBlank()) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(apellidoMaterno.trim());
        }
        return sb.toString();
    }
}
