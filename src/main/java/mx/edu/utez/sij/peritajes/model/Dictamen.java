package mx.edu.utez.sij.peritajes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Dictamen del peritaje (tabla dictamen_Per; uno por peritaje). Si el peritaje se reasigna,
// el perito nuevo sigue con el mismo borrador (decisión del equipo).
public class Dictamen {

    private Long id;
    private Long peritajeId;
    private String contenido = "";      // HTML del editor (Quill)
    private boolean finalizado;         // es_finalizado
    private int version;                // cada guardado suma 1 (autoguardado, RF 4.3)
    private LocalDateTime actualizadoEn;
    private List<ArchivoDictamen> archivos = new ArrayList<>();

    public boolean isBorrador() {
        return !finalizado;
    }

    // version 0 = todavía no se ha guardado nada
    public boolean isGuardado() {
        return version > 0;
    }

    public String getActualizadoEnHora() {
        return FormatoFecha.hora(actualizadoEn);
    }

    public String getActualizadoEnTexto() {
        return FormatoFecha.fechaHoraSegundos(actualizadoEn);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPeritajeId() {
        return peritajeId;
    }

    public void setPeritajeId(Long peritajeId) {
        this.peritajeId = peritajeId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public List<ArchivoDictamen> getArchivos() {
        return archivos;
    }

    public void setArchivos(List<ArchivoDictamen> archivos) {
        this.archivos = archivos;
    }
}
