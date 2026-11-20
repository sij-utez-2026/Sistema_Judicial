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
 * RF 5.1 Registrar la entrega física de la copia impresa. El folio queda inhabilitado (RN-03) (COPIAS_PERSONAL).
 * URL: POST /copias/entregas/entregar  con  id=5 y volver=detalle|lista
 */
@WebServlet("/copias/entregas/entregar")
public class EntregaEntregarServlet extends HttpServlet {

    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.PERSONAL);
        if (usuario == null) {
            return;
        }
        Long id = Parametros.numero(request, "id");
        SolicitudCopia solicitud = id == null ? null : Copias.solicitudService().buscar(id).orElse(null);
        if (solicitud == null) {
            Vistas.noEncontrado(response);
            return;
        }
        try {
            Copias.entregaService().entregar(id, usuario.getId());
            FlashMessage.exito(request, "Entrega registrada correctamente", solicitud.getFolio() + " · El folio quedó inhabilitado.");
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
        }
        Vistas.redirigir(request, response, EntregaListarServlet.regresar(id, Parametros.texto(request, "volver")));
    }
}
