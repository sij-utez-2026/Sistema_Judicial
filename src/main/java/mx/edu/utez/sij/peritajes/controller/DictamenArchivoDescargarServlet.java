package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.ArchivoDictamen;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.DictamenDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// Descargar un archivo del dictamen (solo el perito del caso).
// URL: GET /peritajes/mis-casos/dictamen/archivo?id=5&archivoId=2
// Si no existe o no es suyo responde 404.
// Permiso: PERITAJES_PERITO y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/mis-casos/dictamen/archivo")
public class DictamenArchivoDescargarServlet extends HttpServlet {

    private final PeritajeDao peritajeDao = new PeritajeDao();
    private final DictamenDao dao = new DictamenDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirPerito(request, response);
        if (usuario == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        Peritaje peritaje = peritajeDao.getById(Parametros.numero(request, "id"));
        Long archivoId = Parametros.numero(request, "archivoId");
        ArchivoDictamen archivo = (peritaje == null || archivoId == null) ? null : dao.getArchivo(peritaje, usuario, archivoId);
        if (archivo == null) {
            Vistas.noEncontrado(response);
            return;
        }

        response.setContentType(archivo.getTipoContenido() == null
                ? "application/octet-stream" : archivo.getTipoContenido());
        // filename* lleva el nombre con acentos (RFC 6266); los espacios van como %20
        String nombre = URLEncoder.encode(archivo.getNombreOriginal(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + nombre);
        response.setContentLength(archivo.getContenido().length);
        response.getOutputStream().write(archivo.getContenido());
    }
}
