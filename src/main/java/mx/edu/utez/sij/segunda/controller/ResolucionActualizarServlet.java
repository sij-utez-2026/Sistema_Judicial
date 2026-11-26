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
import mx.edu.utez.sij.segunda.model.Resolucion;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.util.Optional;

/**
 * Registra una nueva versión simulada del PDF; ArchivoService aún no forma parte del módulo.
 * La operación conserva la anterior y exige motivo (RF 7.3).
 * URL: POST /segunda/resoluciones/actualizar con id y motivo.
 */
@WebServlet("/segunda/resoluciones/actualizar")
public class ResolucionActualizarServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = Parametros.numero(request, "id");
        if (id == null) {
            Vistas.noEncontrado(response);
            return;
        }

        Optional<Resolucion> resolucion = Segunda.resolucionService().buscar(id);
        if (resolucion.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }
        Optional<Toca> toca = Segunda.tocaService().buscar(resolucion.get().getTocaId());
        if (toca.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (!SegundaPermisos.esMagistrado(usuario)
                || !usuario.getId().equals(toca.get().getMagistradoUsuarioId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String motivo = Parametros.texto(request, "motivo");
        try {
            Segunda.resolucionService().actualizarPdf(id, motivo);
            FlashMessage.exito(request, "Versión de resolución actualizada", motivo);
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
        }
        Vistas.redirigir(request, response, "/segunda/resoluciones/detalle?id=" + id);
    }
}
