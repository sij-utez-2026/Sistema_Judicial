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
import mx.edu.utez.sij.segunda.model.Magistrado;
import mx.edu.utez.sij.segunda.service.MagistradoService;

import java.io.IOException;

/**
 * RF 4.2 Listado de magistrados con filtros y paginación.
 * URL: GET /segunda/magistrados?q=&estado=activo|inactivo&page=0&size=25
 * Vista: WEB-INF/jsp/segunda/magistrados/lista.jsp
 *
 * TODO: solo SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/magistrados")
public class MagistradoListarServlet extends HttpServlet {

    private final MagistradoService magistradoService = Segunda.magistradoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        String q = Parametros.texto(request, "q");
        String estado = Parametros.texto(request, "estado");
        int page = Parametros.entero(request, "page", 0);
        int size = Parametros.entero(request, "size", MagistradoService.TAMANO_PAGINA);

        Boolean activo = null;
        if ("activo".equals(estado)) activo = true;
        else if ("inactivo".equals(estado)) activo = false;

        Pagina<Magistrado> pagina = magistradoService.listar(q, activo, page, size);

        request.setAttribute("pagina", pagina);
        request.setAttribute("totalRegistrados", magistradoService.totalRegistrados());
        request.setAttribute("totalActivos", magistradoService.totalActivos());
        request.setAttribute("totalInactivos", magistradoService.totalInactivos());
        request.setAttribute("salas", Segunda.salaService().activas());
        request.setAttribute("tamanosPagina", MagistradoService.TAMANOS_PAGINA);
        request.setAttribute("q", q);
        request.setAttribute("estado", estado);
        request.setAttribute("size", pagina.getSize());
        request.setAttribute("hayFiltros", q != null || activo != null);

        Vistas.mostrar(request, response, "segunda/magistrados/lista");
    }
}

