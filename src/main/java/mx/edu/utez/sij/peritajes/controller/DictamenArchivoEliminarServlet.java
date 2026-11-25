package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.DictamenDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;

import java.io.IOException;

// Quitar un archivo del dictamen mientras siga en borrador (peritajes.md 6.6).
// URL: POST /peritajes/mis-casos/dictamen/archivos/eliminar?id=5&archivoId=2
// Regresa al expediente con el resultado.
// Permiso: PERITAJES_PERITO y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/mis-casos/dictamen/archivos/eliminar")
public class DictamenArchivoEliminarServlet extends HttpServlet {

    private final PeritajeDao peritajeDao = new PeritajeDao();
    private final DictamenDao dao = new DictamenDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirPerito(request, response);
        if (usuario == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        Long id = Parametros.numero(request, "id");
        Long archivoId = Parametros.numero(request, "archivoId");
        if (id == null || archivoId == null) {
            Vistas.noEncontrado(response);
            return;
        }

        String error = dao.quitarArchivo(peritajeDao.getById(id), usuario, archivoId);
        if (error == null) {
            FlashMessage.exito(request, "Archivo quitado del dictamen", null);
        } else {
            FlashMessage.error(request, error);
        }
        Vistas.redirigir(request, response, "/peritajes/mis-casos/detalle?id=" + id);
    }
}
