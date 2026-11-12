package mx.edu.utez.sij.peritajes.model;

import java.time.LocalDateTime;

// Archivo anexo de un dictamen (PDF, Word o imagen, máximo 25 MB; RF 4.4).
// TEMPORAL: el archivo se guarda en memoria dentro del dictamen. Cuando exista ArchivoService del núcleo,
// se guarda con archivoService.guardar("PERITAJES", ...) y aquí solo queda el id de core_archivo (tabla per_dictamen_archivo).
public class ArchivoDictamen {

    private final Long id;
    private final String nombreOriginal;
    private final String tipoContenido;
    private final long tamanoBytes;
    private final byte[] contenido;
    private final LocalDateTime subidoEn;

    public ArchivoDictamen(Long id, String nombreOriginal, String tipoContenido, byte[] contenido, LocalDateTime subidoEn) {
        this.id = id;
        this.nombreOriginal = nombreOriginal;
        this.tipoContenido = tipoContenido;
        this.tamanoBytes = contenido.length;
        this.contenido = contenido;
        this.subidoEn = subidoEn;
    }

    // "2.1 MB", "350 KB"
    public static String textoTamano(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
        return Math.max(1, bytes / 1024) + " KB";
    }

    public String getTamanoTexto() {
        return textoTamano(tamanoBytes);
    }

    // "PDF", "Word" o "Imagen", para la lista de archivos
    public String getTipoTexto() {
        String nombre = nombreOriginal.toLowerCase();
        if (nombre.endsWith(".pdf")) {
            return "PDF";
        }
        if (nombre.endsWith(".doc") || nombre.endsWith(".docx")) {
            return "Word";
        }
        return "Imagen";
    }

    public Long getId() {
        return id;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public LocalDateTime getSubidoEn() {
        return subidoEn;
    }
}
