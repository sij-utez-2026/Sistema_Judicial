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
import mx.edu.utez.sij.copias.service.EntregaService;
import mx.edu.utez.sij.copias.service.SolicitudCopiaService;
import mx.edu.utez.sij.copias.service.ValidacionService;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;
import java.util.Map;

/**
 * Inicio del módulo (/copias). Trabajador: resumen de solicitudes por estado y últimas solicitudes
 * (WEB-INF/jsp/copias/inicio.jsp). Administrador: se le manda a su inicio (/copias/admin).
 */
@WebServlet("/copias")
public class InicioCopiasServlet extends HttpServlet {

    private final SolicitudCopiaService solicitudService = Copias.solicitudService();
    private final ValidacionService validacionService = Copias.validacionService();
    private final EntregaService entregaService = Copias.entregaService();
    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Cada rol tiene su propio inicio
        if (Copias.sesion().esAdmin(request)) {
            Vistas.redirigir(request, response, "/copias/admin");
            return;
        }
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.PERSONAL);
        if (usuario == null) {
            return;
        }
        Map<EstadoSolicitud, Long> totales = solicitudService.totalesPorEstado();
        request.setAttribute("totales", totales);
        request.setAttribute("estados", EstadoSolicitud.values());
        request.setAttribute("totalSolicitudes", solicitudService.total());
        request.setAttribute("porValidar", validacionService.pendientes().size());
        request.setAttribute("porEntregar", entregaService.pendientes().size());
        request.setAttribute("sinPagar", totales.get(EstadoSolicitud.LISTO_PARA_PAGO) + totales.get(EstadoSolicitud.APROBADO));
        request.setAttribute("recientes", solicitudService.listar(null, null, 0, 10).getContent());
        Vistas.mostrar(request, response, "copias/inicio");
    }
}
