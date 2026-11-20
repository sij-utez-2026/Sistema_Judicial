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
 * RF 3.1 Rechazar una solicitud privada con motivo: queda "Cancelado" y ya no se puede pagar (COPIAS_PERSONAL).
 * URL: POST /copias/validacion/rechazar  con  id=5 y motivo=...
 */
@WebServlet("/copias/validacion/rechazar")
public class ValidacionRechazarServlet extends HttpServlet {

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
            Copias.validacionService().rechazar(solicitud.getId(), Parametros.texto(request, "motivo"), usuario.getId());
            FlashMessage.exito(request, "Solicitud rechazada", solicitud.getFolio() + " quedó Cancelada y no se podrá pagar.");
            Vistas.redirigir(request, response, "/copias/validacion");
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            Vistas.redirigir(request, response, "/copias/validacion/detalle?id=" + solicitud.getId());
        }
    }
}
