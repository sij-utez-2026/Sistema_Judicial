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
import mx.edu.utez.sij.segunda.model.Sala;
import mx.edu.utez.sij.segunda.service.SalaService;

import java.io.IOException;

/**
 * RF 3.4 Baja o reactivación de sala (no se borra).
 * URL: POST /segunda/salas/estado  con  id=5 y activa=true|false
 * Regresa a la pantalla de edición con el resultado.
 *
 * TODO: solo SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/salas/estado")
public class SalaEstadoServlet extends HttpServlet {

    private final SalaService salaService = Segunda.salaService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        Long id = Parametros.numero(request, "id");
        if (id == null || salaService.buscar(id).isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        try {
            Sala sala = salaService.cambiarEstado(id, Parametros.booleano(request, "activa"));
            Segunda.registrarBitacora(request, Segunda.usuarioActual(request), "sala_Seg", sala.getId(),
                    sala.isActiva() ? "Sala reactivada" : "Sala desactivada",
                    sala.getNombre() + " · activa=" + sala.isActiva(), sala.isActiva() ? "green" : "gold");
            FlashMessage.exito(request,
                    sala.isActiva() ? "Sala reactivada" : "Sala desactivada",
                    sala.getNombre() + (sala.isActiva()
                            ? " vuelve a estar disponible para asignación de tocas."
                            : " quedó Inactiva y ya no aparecerá en las asignaciones."));
        } catch (ReglaNegocioException e) {
            FlashMessage.agregar(request, "errorEstado", e.getMessage());
        }
        Vistas.redirigir(request, response, "/segunda/salas/editar?id=" + id);
    }
}
