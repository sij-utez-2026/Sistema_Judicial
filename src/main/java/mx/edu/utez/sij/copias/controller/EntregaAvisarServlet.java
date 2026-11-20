package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.copias.model.CorreoEnviado;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Avisa por correo al solicitante que su copia está lista para recoger (COPIAS_PERSONAL).
 * URL: POST /copias/entregas/avisar  con  id=5 y volver=detalle|lista
 */
@WebServlet("/copias/entregas/avisar")
public class EntregaAvisarServlet extends HttpServlet {

    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.PERSONAL);
        if (usuario == null) {
            return;
        }
        Long id = Parametros.numero(request, "id");
        if (id == null || Copias.solicitudService().buscar(id).isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }
        try {
            CorreoEnviado correo = Copias.entregaService().avisarListaParaRecoger(id, usuario.getId());
            FlashMessage.exito(request, "Aviso enviado",
                    "Se avisó a " + correo.getDestinatario() + " que su copia está lista para recoger.");
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
        }
        Vistas.redirigir(request, response, EntregaListarServlet.regresar(id, Parametros.texto(request, "volver")));
    }
}
