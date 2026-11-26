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
import mx.edu.utez.sij.segunda.model.Sala;
import mx.edu.utez.sij.segunda.service.SalaService;

import java.io.IOException;

/**
 * RF 3.2 Listado de salas con filtros y paginación.
 * URL: GET /segunda/salas?q=&distritoId=&estado=activa|inactiva&page=0&size=25
 * Vista: WEB-INF/jsp/segunda/salas/lista.jsp
 *
 * TODO: solo SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/salas")
public class SalaListarServlet extends HttpServlet {

    private final SalaService salaService = Segunda.salaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        String q = Parametros.texto(request, "q");
        String distritoId = Parametros.texto(request, "distritoId");
        String estado = Parametros.texto(request, "estado");
        int page = Parametros.entero(request, "page", 0);
        int size = Parametros.entero(request, "size", SalaService.TAMANO_PAGINA);

        Boolean activa = null;
        if ("activa".equals(estado)) activa = true;
        else if ("inactiva".equals(estado)) activa = false;

        Pagina<Sala> pagina = salaService.listar(q, distritoId, activa, page, size);

        request.setAttribute("pagina", pagina);
        request.setAttribute("totalRegistradas", salaService.totalRegistradas());
        request.setAttribute("totalActivas", salaService.totalActivas());
        request.setAttribute("totalInactivas", salaService.totalInactivas());
        request.setAttribute("distritos", salaService.distritosActivos());
        request.setAttribute("tamanosPagina", SalaService.TAMANOS_PAGINA);
        request.setAttribute("q", q);
        request.setAttribute("distritoId", distritoId);
        request.setAttribute("estado", estado);
        request.setAttribute("size", pagina.getSize());
        request.setAttribute("hayFiltros", q != null || distritoId != null || activa != null);

        Vistas.mostrar(request, response, "segunda/salas/lista");
    }
}


