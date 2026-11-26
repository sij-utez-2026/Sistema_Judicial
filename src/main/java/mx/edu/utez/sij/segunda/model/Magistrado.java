package mx.edu.utez.sij.segunda.model;

import java.time.LocalDateTime;

/**
 * Magistrado de segunda instancia (RF 4.1 – 4.5).
 *
 * El perfil se relaciona con user_Seg mediante id_usuario; la sala asignada se guarda
 * en magistrado_Seg.id_sala. id y usuarioId son claves distintas.
 *
 * TEMPORAL: mientras no hay MySQL, se guarda en memoria (MagistradoDaoMemoria).
 */
public class Magistrado {

    private Long id;                       // id del magistrado (no es el id de usuario)
    private Long usuarioId;                // user_Seg.id_usuario
    private String nombre;
    private String apellidos;
    private String correo;
    private Long salaId;                   // sala_Seg.id_sala; null = sin sala
    private boolean activo = true;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private String contrasenaTemporal;

    // Solo para mostrar en pantalla
    private String salaNombre;

    public String getNombreCompleto() {
        return nombre + (apellidos != null && !apellidos.isBlank() ? " " + apellidos : "");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public Long getSalaId() { return salaId; }
    public void setSalaId(Long salaId) { this.salaId = salaId; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public String getContrasenaTemporal() { return contrasenaTemporal; }
    public void setContrasenaTemporal(String contrasenaTemporal) { this.contrasenaTemporal = contrasenaTemporal; }

    public String getSalaNombre() { return salaNombre; }
    public void setSalaNombre(String salaNombre) { this.salaNombre = salaNombre; }

    public boolean isSinSala() { return salaId == null; }
}
