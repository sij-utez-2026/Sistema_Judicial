package mx.edu.utez.sij.segunda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulario de registro de resolución (RF 7.1).
 */
public class ResolucionForm {

    @NotBlank(message = "Seleccione el sentido de la resolución")
    private String sentido;

    @NotBlank(message = "Escriba el resolutivo de la resolución")
    @Size(max = 2000, message = "Los comentarios no deben pasar de 2000 caracteres")
    private String comentarios;

    public String getSentido() { return sentido; }
    public void setSentido(String sentido) { this.sentido = sentido; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }
}

