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
import mx.edu.utez.sij.segunda.model.Magistrado;
import mx.edu.utez.sij.segunda.service.MagistradoService;

import java.io.IOException;

/**
 * RF 4.5 Baja o reactivación de magistrado (no se borra).
 * URL: POST /segunda/magistrados/estado  con  id=5 y activo=true|false
 * Regresa a la pantalla de edición con el resultado.
 *
 * TODO: solo SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/magistrados/estado")
public class MagistradoEstadoServlet extends HttpServlet {

    private final MagistradoService magistradoService = Segunda.magistradoService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        Long id = Parametros.numero(request, "id");
        if (id == null || magistradoService.buscar(id).isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        try {
            Magistrado m = magistradoService.cambiarEstado(id, Parametros.booleano(request, "activo"));
            FlashMessage.exito(request,
                    m.isActivo() ? "Magistrado reactivado" : "Magistrado dado de baja",
                    m.getNombreCompleto() + (m.isActivo()
                            ? " vuelve a poder recibir tocas."
                            : " quedó Inactivo. Su sala fue liberada."));
        } catch (ReglaNegocioException e) {
            FlashMessage.agregar(request, "errorEstado", e.getMessage());
        }
        Vistas.redirigir(request, response, "/segunda/magistrados/editar?id=" + id);
    }
}

