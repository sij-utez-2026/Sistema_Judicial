package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.model.Sala;
import mx.edu.utez.sij.segunda.service.TocaService;

import java.io.IOException;
import java.util.Optional;

/**
 * Muestra una sala y las tocas que tiene asociadas.
 * URL: GET /segunda/salas/detalle?id=5
 * Vista: WEB-INF/jsp/segunda/salas/detalle.jsp
 */
@WebServlet("/segunda/salas/detalle")
public class SalaDetalleServlet extends HttpServlet {

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

        Optional<Sala> sala = Segunda.salaService().buscar(id);
        if (sala.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        request.setAttribute("sala", sala.get());
        request.setAttribute("tocas", tocaService.listar(null, null, id, null, 0, 100).getContent());
        Vistas.mostrar(request, response, "segunda/salas/detalle");
    }
}
