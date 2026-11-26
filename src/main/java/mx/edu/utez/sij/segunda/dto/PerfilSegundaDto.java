package mx.edu.utez.sij.segunda.dto;

import java.util.List;

/** Datos de solo lectura de la cuenta y adscripción actual de Segunda Instancia. */
public final class PerfilSegundaDto {
    private final String nombreCompleto;
    private final String correo;
    private final List<String> roles;
    private final boolean activo;
    private final boolean magistrado;
    private final String salaNombre;
    private final String distritoNombre;

    public PerfilSegundaDto(String nombreCompleto, String correo, List<String> roles,
                            boolean activo, boolean magistrado, String salaNombre, String distritoNombre) {
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.roles = List.copyOf(roles);
        this.activo = activo;
        this.magistrado = magistrado;
        this.salaNombre = salaNombre;
        this.distritoNombre = distritoNombre;
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public String getCorreo() { return correo; }
    public List<String> getRoles() { return roles; }
    public boolean isActivo() { return activo; }
    public boolean isMagistrado() { return magistrado; }
    public String getSalaNombre() { return salaNombre; }
    public String getDistritoNombre() { return distritoNombre; }
}
