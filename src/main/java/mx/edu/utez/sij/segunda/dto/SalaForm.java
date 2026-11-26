package mx.edu.utez.sij.segunda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulario de registro (RF 3.1) y edición (RF 3.3) de salas.
 */
public class SalaForm {

    @NotBlank(message = "El nombre de la sala es obligatorio")
    @Size(max = 100, message = "El nombre no debe pasar de 100 caracteres")
    private String nombre;

    @NotBlank(message = "Seleccione un distrito")
    private String distritoId;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no debe pasar de 255 caracteres")
    private String direccion;

    // null = sin magistrado asignado (permitido al registrar; RF 4.3 lo asigna después)
    private Long magistradoUsuarioId;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDistritoId() { return distritoId; }
    public void setDistritoId(String distritoId) { this.distritoId = distritoId; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Long getMagistradoUsuarioId() { return magistradoUsuarioId; }
    public void setMagistradoUsuarioId(Long magistradoUsuarioId) { this.magistradoUsuarioId = magistradoUsuarioId; }
}


