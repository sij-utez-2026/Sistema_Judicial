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
 * RF 4.3 Asignar o cambiar la sala de un magistrado (uno a uno).
 * URL: POST /segunda/magistrados/salas  con  id=5 y salaId=3
 * Regresa a la pantalla de edición con el resultado.
 *
 * TODO: solo SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/magistrados/salas")
public class MagistradoSalasServlet extends HttpServlet {

    private final MagistradoService magistradoService = Segunda.magistradoService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        Long id = Parametros.numero(request, "id");
        if (id == null || magistradoService.buscar(id).isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }
        Long salaId = Parametros.numero(request, "salaId");

        try {
            Magistrado m = magistradoService.asignarSala(id, salaId);
            Segunda.registrarBitacora(request, Segunda.usuarioActual(request), "magistrado_Seg", m.getId(),
                    "Sala asignada a magistrado", m.getNombreCompleto() + " · sala " + m.getSalaId(), "blue");
            FlashMessage.exito(request, "Sala asignada",
                    m.getNombreCompleto() + " quedó en la " + m.getSalaNombre() + ".");
        } catch (ReglaNegocioException e) {
            FlashMessage.agregar(request, "errorSala", e.getMessage());
        }
        Vistas.redirigir(request, response, "/segunda/magistrados/editar?id=" + id);
    }
}
