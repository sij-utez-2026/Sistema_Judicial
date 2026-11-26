package mx.edu.utez.sij.peritajes.model;

import java.util.Map;

// Archivo anexo de un dictamen (tabla anexo_dictamen_Per): PDF, Word o imagen, máximo 25 MB (RF 4.4).
// contenido solo se carga para descargarlo (en las listas va null, así no se leen los archivos completos).
public class ArchivoDictamen {

    // formato_archivo → tipo de contenido para la descarga
    private static final Map<String, String> TIPOS = Map.of(
            "PDF", "application/pdf",
            "DOC", "application/msword",
            "DOCX", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "PNG", "image/png",
            "JPG", "image/jpeg",
            "JPEG", "image/jpeg");

    private final Long id;
    private final String nombreOriginal;
    private final String formato;       // PDF, DOCX, PNG...
    private final double tamanoMb;
    private final byte[] contenido;

    public ArchivoDictamen(Long id, String nombreOriginal, String formato, double tamanoMb, byte[] contenido) {
        this.id = id;
        this.nombreOriginal = nombreOriginal;
        this.formato = formato;
        this.tamanoMb = tamanoMb;
        this.contenido = contenido;
    }

    // "2.1 MB", "350 KB"
    public static String textoTamano(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
        return Math.max(1, bytes / 1024) + " KB";
    }

    // Megabytes con 2 decimales, como la columna tamano_mb
    public static double megabytes(long bytes) {
        return Math.round(bytes / (1024.0 * 1024.0) * 100) / 100.0;
    }

    public String getTamanoTexto() {
        return textoTamano(Math.round(tamanoMb * 1024 * 1024));
    }

    // "PDF", "Word" o "Imagen", para la lista de archivos
    public String getTipoTexto() {
        if ("PDF".equals(formato)) {
            return "PDF";
        }
        if ("DOC".equals(formato) || "DOCX".equals(formato)) {
            return "Word";
        }
        return "Imagen";
    }

    public String getTipoContenido() {
        return TIPOS.getOrDefault(formato, "application/octet-stream");
    }

    public Long getId() {
        return id;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public String getFormato() {
        return formato;
    }

    public double getTamanoMb() {
        return tamanoMb;
    }

    public byte[] getContenido() {
        return contenido;
    }
}
