package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.model.Pago;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.service.PagoService;
import mx.edu.utez.sij.copias.service.SolicitudCopiaService;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * RF 4.1 Comprobante imprimible del pago simulado (COPIAS_PERSONAL).
 * URL: GET /copias/pagos/comprobante?id=5
 */
@WebServlet("/copias/pagos/comprobante")
public class PagoComprobanteServlet extends HttpServlet {

    private final PagoService pagoService = Copias.pagoService();
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
        try {
            Pago pago = pagoService.obtenerComprobante(id);
            request.setAttribute("solicitud", solicitud);
            request.setAttribute("pago", pago);
            Vistas.mostrar(request, response, "copias/pagos/comprobante");
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            Vistas.redirigir(request, response, "/copias/solicitudes/detalle?id=" + id);
        }
    }
}
