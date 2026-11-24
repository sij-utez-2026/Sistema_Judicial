package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.ArchivoDictamen;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.DictamenDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;

import java.io.IOException;
import java.io.InputStream;

// RF 4.4 Subir un archivo de soporte al dictamen (PDF, Word o imagen, máximo 25 MB).
// URL: POST /peritajes/mis-casos/dictamen/archivos?id=5  (multipart/form-data con el campo "archivo")
// Regresa al expediente con el resultado. Si el archivo pasa de 25 MB, FiltroNucleo muestra la página 413.
// Permiso: PERITAJES_PERITO y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/mis-casos/dictamen/archivos")
@MultipartConfig(maxFileSize = 25L * 1024 * 1024)
public class DictamenArchivoSubirServlet extends HttpServlet {

    private final PeritajeDao peritajeDao = new PeritajeDao();
    private final DictamenDao dao = new DictamenDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirPerito(request, response);
        if (usuario == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        Long id = Parametros.numero(request, "id");
        if (id == null) {
            Vistas.noEncontrado(response);
            return;
        }
        Peritaje peritaje = peritajeDao.getById(id);

        Part archivo = request.getPart("archivo");      // null si no viene
        try (InputStream contenido = archivo == null ? null : archivo.getInputStream()) {
            String nombre = archivo == null ? null : archivo.getSubmittedFileName();
            long tamano = archivo == null ? 0 : archivo.getSize();
            String error = dao.subirArchivo(peritaje, usuario, nombre, tamano, contenido);
            if (error == null) {
                FlashMessage.exito(request, "Archivo cargado", nombre + " · " + ArchivoDictamen.textoTamano(tamano)
                        + ". El borrador sigue editable hasta la entrega definitiva.");
            } else {
                FlashMessage.error(request, error);
            }
        }
        Vistas.redirigir(request, response, "/peritajes/mis-casos/detalle?id=" + id);
    }
}
