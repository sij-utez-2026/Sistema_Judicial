package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.DictamenDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;

import java.io.IOException;

// RF 4.4 Entrega definitiva del dictamen (la pantalla pide confirmación antes).
// URL: POST /peritajes/mis-casos/dictamen/finalizar?id=5  con  contenido y version (lo que hay en el editor)
// Regresa al expediente, que ya se muestra en solo lectura.
// Permiso: PERITAJES_PERITO y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/mis-casos/dictamen/finalizar")
public class DictamenFinalizarServlet extends HttpServlet {

    private final PeritajeDao peritajeDao = new PeritajeDao();
    private final DictamenDao dao = new DictamenDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        Long version = Parametros.numero(request, "version");

        String error = dao.finalizar(peritaje, usuario, Parametros.crudo(request, "contenido"),
                version == null ? null : version.intValue());
        if (error == null) {
            FlashMessage.exito(request, "Dictamen entregado", peritaje.getFolio()
                    + " quedó en estado Dictamen Entregado. Ya no se puede modificar.");
        } else {
            FlashMessage.error(request, error);
        }
        Vistas.redirigir(request, response, "/peritajes/mis-casos/detalle?id=" + id);
    }
}
