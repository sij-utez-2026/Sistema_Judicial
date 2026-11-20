package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.service.SolicitudCopiaService;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * RF 2.3 Registro de solicitudes con su estado, búsqueda, filtro y paginación (COPIAS_PERSONAL).
 * URL: GET /copias/solicitudes?q=...&estado=...&page=0&size=10
 */
@WebServlet("/copias/solicitudes")
public class SolicitudListarServlet extends HttpServlet {

    private final SolicitudCopiaService solicitudService = Copias.solicitudService();
    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.PERSONAL);
        if (usuario == null) {
            return;
        }
        String q = Parametros.texto(request, "q");
        EstadoSolicitud estado = estado(Parametros.texto(request, "estado"));
        Pagina<SolicitudCopia> pagina = solicitudService.listar(q, estado, Parametros.entero(request, "page", 0),
                Parametros.entero(request, "size", SolicitudCopiaService.TAMANO_PAGINA));

        request.setAttribute("pagina", pagina);
        request.setAttribute("q", q);
        request.setAttribute("estado", estado);
        request.setAttribute("estados", EstadoSolicitud.values());
        request.setAttribute("totales", solicitudService.totalesPorEstado());
        request.setAttribute("totalSolicitudes", solicitudService.total());
        request.setAttribute("size", pagina.getSize());
        request.setAttribute("tamanosPagina", SolicitudCopiaService.TAMANOS_PAGINA);
        request.setAttribute("hayFiltros", q != null || estado != null);
        Vistas.mostrar(request, response, "copias/solicitudes/lista");
    }

    // Si el estado no es válido, no se filtra
    private EstadoSolicitud estado(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return EstadoSolicitud.valueOf(texto);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
