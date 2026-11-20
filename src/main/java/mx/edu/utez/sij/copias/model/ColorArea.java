package mx.edu.utez.sij.copias.model;


import java.time.LocalDateTime;

/**
 * Un color de las pantallas de Copias que configura COPIAS_ADMIN (tabla cop_color).
 * variable: una variable CSS del núcleo, por ejemplo "--sij-primario". valor: "#RRGGBB".
 * Solo cambia las pantallas de Copias: el estilos.css común no se toca.
 */
public class ColorArea {

    private Long id;

    private String variable;

    private String valor;

    private Long actualizadoPor;

    private LocalDateTime actualizadoEn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Long getActualizadoPor() {
        return actualizadoPor;
    }

    public void setActualizadoPor(Long actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
