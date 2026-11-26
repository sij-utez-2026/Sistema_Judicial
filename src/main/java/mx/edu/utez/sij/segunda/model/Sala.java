package mx.edu.utez.sij.segunda.model;

import java.time.LocalDateTime;

/**
 * Sala de segunda instancia (RF 3.1 – 3.4).
 *
 * Mapeo SQL: sala_Seg.clave_distrito es VARCHAR(10); la asignación de magistrado
 * se deriva de magistrado_Seg.id_sala y no se guarda como columna en esta tabla.
 */
public class Sala {

    private Long id;
    private String nombre;                  // único por distrito
    private String distritoId;                // clave_distrito VARCHAR(10) del catálogo compartido
    private String direccion;
    private Long magistradoUsuarioId;       // core_usuario; null = sin magistrado
    private boolean activa = true;
    private LocalDateTime creadaEn;
    private LocalDateTime actualizadaEn;

    // Solo para mostrar en pantalla (no se guarda)
    private String distritoNombre;
    private String magistradoNombre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDistritoId() { return distritoId; }
    public void setDistritoId(String distritoId) { this.distritoId = distritoId; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Long getMagistradoUsuarioId() { return magistradoUsuarioId; }
    public void setMagistradoUsuarioId(Long magistradoUsuarioId) { this.magistradoUsuarioId = magistradoUsuarioId; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public LocalDateTime getCreadaEn() { return creadaEn; }
    public void setCreadaEn(LocalDateTime creadaEn) { this.creadaEn = creadaEn; }

    public LocalDateTime getActualizadaEn() { return actualizadaEn; }
    public void setActualizadaEn(LocalDateTime actualizadaEn) { this.actualizadaEn = actualizadaEn; }

    public String getDistritoNombre() { return distritoNombre; }
    public void setDistritoNombre(String distritoNombre) { this.distritoNombre = distritoNombre; }

    public String getMagistradoNombre() { return magistradoNombre; }
    public void setMagistradoNombre(String magistradoNombre) { this.magistradoNombre = magistradoNombre; }

    public boolean isSinMagistrado() { return magistradoUsuarioId == null; }
}

