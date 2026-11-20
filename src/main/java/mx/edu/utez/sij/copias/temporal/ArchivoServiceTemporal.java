package mx.edu.utez.sij.copias.temporal;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * TEMPORAL: imita ArchivoService del núcleo (docs/ARQUITECTURA.md, 7.5) guardando los archivos en memoria
 * (se pierden al reiniciar). Cuando exista el núcleo se cambia en Copias.java y se borra esta clase.
 *
 * Recibe el nombre, el tamaño y el contenido del archivo (no el Part del servlet), para no depender de la web.
 */
public class ArchivoServiceTemporal {

    /** Archivo guardado, como lo devolverá archivoService.descargar(id). */
    public record Archivo(Long id, String nombreOriginal, byte[] contenido) {
    }

    private final Map<Long, Archivo> archivos = new LinkedHashMap<>();
    private long siguienteId = 1;

    /**
     * Valida extensión y tamaño, guarda el archivo y devuelve su id (en el núcleo: id de core_archivo).
     *
     * @param tiposPermitidos extensiones en minúsculas sin punto, por ejemplo ["pdf", "jpg", "png"]
     */
    public synchronized Long guardar(String modulo, String nombre, long tamano, InputStream contenido,
                                     List<String> tiposPermitidos, int tamanoMaximoMB) {
        if (nombre == null || nombre.isBlank() || tamano <= 0 || contenido == null) {
            throw new ReglaNegocioException("Seleccione un archivo");
        }
        int punto = nombre.lastIndexOf('.');
        String extension = punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
        if (extension.equals("jpeg")) {
            extension = "jpg";
        }
        if (!tiposPermitidos.contains(extension)) {
            throw new ReglaNegocioException("El archivo no es válido: solo se permiten "
                    + String.join(", ", tiposPermitidos).toUpperCase(Locale.ROOT));
        }
        if (tamano > tamanoMaximoMB * 1024L * 1024L) {
            throw new ReglaNegocioException("El archivo no es válido: el tamaño máximo es de " + tamanoMaximoMB + " MB");
        }
        try {
            Archivo guardado = new Archivo(siguienteId++, nombre, contenido.readAllBytes());
            archivos.put(guardado.id(), guardado);
            return guardado.id();
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo guardar el archivo. Intente de nuevo");
        }
    }

    public synchronized Optional<Archivo> descargar(Long id) {
        return Optional.ofNullable(archivos.get(id));
    }
}
