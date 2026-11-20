package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.model.CorreoEnviado;
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
 * RF 4.1 Reenviar el comprobante de pago por correo al solicitante (COPIAS_PERSONAL).
 * URL: POST /copias/pagos/reenviar  con id=5
 */
@WebServlet("/copias/pagos/reenviar")
public class PagoReenviarComprobanteServlet extends HttpServlet {

    private final PagoService pagoService = Copias.pagoService();
    private final SolicitudCopiaService solicitudService = Copias.solicitudService();
    private final AccesoTemporal acceso = Copias.acceso();

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
            CorreoEnviado envio = pagoService.reenviarComprobante(id, usuario.getId());
            if (envio.isExito()) {
                FlashMessage.exito(request, "Comprobante reenviado",
                        "Se envió el comprobante a " + envio.getDestinatario() + ".");
            } else {
                FlashMessage.error(request, "No se pudo enviar el correo a " + envio.getDestinatario() + ".");
            }
            Vistas.redirigir(request, response, "/copias/pagos/comprobante?id=" + id);
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            Vistas.redirigir(request, response, "/copias/solicitudes/detalle?id=" + id);
        }
    }
}
