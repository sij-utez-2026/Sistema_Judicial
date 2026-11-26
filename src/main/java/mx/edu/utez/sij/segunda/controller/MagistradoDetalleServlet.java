package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.model.Magistrado;
import mx.edu.utez.sij.segunda.service.TocaService;

import java.io.IOException;
import java.util.Optional;

/**
 * Muestra los datos de un magistrado y las tocas que tiene asignadas.
 * URL: GET /segunda/magistrados/detalle?id=5
 * Vista: WEB-INF/jsp/segunda/magistrados/detalle.jsp
 */
@WebServlet("/segunda/magistrados/detalle")
public class MagistradoDetalleServlet extends HttpServlet {

    private final TocaService tocaService = Segunda.tocaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        Long id = Parametros.numero(request, "id");
        if (id == null) {
            Vistas.noEncontrado(response);
            return;
        }

        Optional<Magistrado> magistrado = Segunda.magistradoService().buscar(id);
        if (magistrado.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        request.setAttribute("magistrado", magistrado.get());
        request.setAttribute("tocas", tocaService.listar(
                null, null, null, magistrado.get().getId(), 0, 100).getContent());
        Vistas.mostrar(request, response, "segunda/magistrados/detalle");
    }
}
