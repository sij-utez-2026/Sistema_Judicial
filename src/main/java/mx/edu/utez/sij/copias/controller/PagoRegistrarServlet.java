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
 * RF 4.1 Pantalla y registro del pago simulado (COPIAS_PERSONAL).
 * URL: GET  /copias/pagos/registrar?id=5
 *      POST /copias/pagos/registrar  con id=5
 */
@WebServlet("/copias/pagos/registrar")
public class PagoRegistrarServlet extends HttpServlet {

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
        if (id == null || solicitudService.buscar(id).isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }
        try {
            SolicitudCopia solicitud = pagoService.prepararPago(id);
            request.setAttribute("solicitud", solicitud);
            Vistas.mostrar(request, response, "copias/pagos/registrar");
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            Vistas.redirigir(request, response, "/copias/solicitudes/detalle?id=" + id);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.PERSONAL);
        if (usuario == null) {
            return;
        }
        Long id = Parametros.numero(request, "id");
        if (id == null || solicitudService.buscar(id).isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }
        try {
            Pago pago = pagoService.registrarPago(id, usuario.getId());
            FlashMessage.exito(request, "Pago simulado registrado",
                    "Comprobante " + pago.getFolioPago() + " · " + pago.getMontoTexto() + " MXN.");
            Vistas.redirigir(request, response, "/copias/pagos/comprobante?id=" + id);
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            Vistas.redirigir(request, response, "/copias/solicitudes/detalle?id=" + id);
        }
    }
}
