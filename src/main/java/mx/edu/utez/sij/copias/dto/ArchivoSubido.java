package mx.edu.utez.sij.copias.dto;

import java.io.InputStream;

/**
 * Archivo que llega en un formulario (por ejemplo la INE). El servlet lo arma con el Part:
 *     new ArchivoSubido(parte.getSubmittedFileName(), parte.getSize(), parte.getInputStream())
 * Así los services no dependen de la web y se pueden probar sin Tomcat.
 */
public record ArchivoSubido(String nombre, long tamano, InputStream contenido) {

    public boolean estaVacio() {
        return nombre == null || nombre.isBlank() || tamano <= 0;
    }
}
