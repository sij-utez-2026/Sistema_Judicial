package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.service.SolicitudCopiaService;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Detalle de una solicitud: datos, siguiente paso según su estado, historial y correos (COPIAS_PERSONAL).
 * URL: GET /copias/solicitudes/detalle?id=5
 */
@WebServlet("/copias/solicitudes/detalle")
public class SolicitudDetalleServlet extends HttpServlet {

    private final SolicitudCopiaService solicitudService = Copias.solicitudService();
    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.PERSONAL);
        if (usuario == null) {
            return;
        }
        Long id = Parametros.numero(request, "id");
        SolicitudCopia solicitud = id == null ? null : solicitudService.buscar(id).orElse(null);
        if (solicitud == null) {
            Vistas.noEncontrado(response);
            return;
        }
        request.setAttribute("solicitud", solicitud);
        request.setAttribute("historial", Copias.historialService().historial(id));
        request.setAttribute("entrega", Copias.entregaService().entregaDe(id).orElse(null));
        request.setAttribute("correos", Copias.correoService().enviados(id));
        Vistas.mostrar(request, response, "copias/solicitudes/detalle");
    }
}
