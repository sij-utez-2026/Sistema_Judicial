package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * RF 3.1 Aprobar una solicitud privada: queda "Aprobado" y ya se puede pagar (COPIAS_PERSONAL).
 * URL: POST /copias/validacion/aprobar  con  id=5
 */
@WebServlet("/copias/validacion/aprobar")
public class ValidacionAprobarServlet extends HttpServlet {

    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.PERSONAL);
        if (usuario == null) {
            return;
        }
        SolicitudCopia solicitud = ValidacionDetalleServlet.privada(Parametros.numero(request, "id"));
        if (solicitud == null) {
            Vistas.noEncontrado(response);
            return;
        }
        try {
            Copias.validacionService().aprobar(solicitud.getId(), usuario.getId());
            FlashMessage.exito(request, "Solicitud aprobada", solicitud.getFolio() + " · Ya se puede realizar el pago.");
            Vistas.redirigir(request, response, "/copias/solicitudes/detalle?id=" + solicitud.getId());
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            Vistas.redirigir(request, response, "/copias/validacion/detalle?id=" + solicitud.getId());
        }
    }
}
