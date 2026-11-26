package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.service.ResolucionService;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;

@WebServlet("/segunda/resoluciones/anular")
public class ResolucionAnularServlet extends HttpServlet {

    private final ResolucionService resolucionService = Segunda.resolucionService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (!SegundaPermisos.esAdmin(usuario)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Long id = Parametros.numero(request, "id");
        String motivo = Parametros.texto(request, "motivo");
        if (id == null) { Vistas.noEncontrado(response); return; }

        try {
            resolucionService.anular(id, motivo);
            FlashMessage.exito(request, "Resolución anulada", motivo);
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
        }
        Vistas.redirigir(request, response, "/segunda/resoluciones");
    }
}
