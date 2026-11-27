package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.model.EstadoResolucion;
import mx.edu.utez.sij.segunda.model.Resolucion;
import mx.edu.utez.sij.segunda.service.ResolucionService;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;

@WebServlet("/segunda/resoluciones")
public class ResolucionListarServlet extends HttpServlet {

    private final ResolucionService resolucionService = Segunda.resolucionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (!SegundaPermisos.esUsuarioSegunda(usuario)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String q = Parametros.texto(request, "q");
        Long salaId = Parametros.numero(request, "salaId");
        int page = Parametros.entero(request, "page", 0);
        int size = Parametros.entero(request, "size", ResolucionService.TAMANO_PAGINA);

        EstadoResolucion estado = null;

        Long soloDeMagistrado = SegundaPermisos.esMagistrado(usuario) ? usuario.getId() : null;
        Pagina<Resolucion> pagina = resolucionService.listar(q, estado, salaId, soloDeMagistrado, page, size);

        request.setAttribute("pagina", pagina);
        request.setAttribute("usuario", usuario);
        request.setAttribute("salas", Segunda.salaService().activas());
        request.setAttribute("tamanosPagina", ResolucionService.TAMANOS_PAGINA);
        request.setAttribute("q", q);
        request.setAttribute("salaId", salaId);
        request.setAttribute("size", pagina.getSize());
        request.setAttribute("hayFiltros", q != null || estado != null || salaId != null);

        Vistas.mostrar(request, response, "segunda/resoluciones/lista");
    }
}
