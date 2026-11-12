package mx.edu.utez.sij.peritajes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Dictamen {

    private Long id;
    private Long peritajeId;
    private Long peritoId;
    private String contenido = "";      // HTML del editor (Quill)
    private EstadoDictamen estado = EstadoDictamen.BORRADOR;
    private int version;
    private LocalDateTime actualizadoEn;
    private LocalDateTime finalizadoEn;
    private List<ArchivoDictamen> archivos = new ArrayList<>();

    public boolean isBorrador() {
        return estado == EstadoDictamen.BORRADOR;
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

    public Long getPeritoId() {
        return peritoId;
    }

    public void setPeritoId(Long peritoId) {
        this.peritoId = peritoId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public EstadoDictamen getEstado() {
        return estado;
    }

    public void setEstado(EstadoDictamen estado) {
        this.estado = estado;
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

    public LocalDateTime getFinalizadoEn() {
        return finalizadoEn;
    }

    public void setFinalizadoEn(LocalDateTime finalizadoEn) {
        this.finalizadoEn = finalizadoEn;
    }

    public List<ArchivoDictamen> getArchivos() {
        return archivos;
    }

    public void setArchivos(List<ArchivoDictamen> archivos) {
        this.archivos = archivos;
    }
}
