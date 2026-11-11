package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.Peritajes;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.service.PeritoService;
import mx.edu.utez.sij.peritajes.service.ReglaNegocioException;

import java.io.IOException;

/**
 * RF 3.4 Baja o reactivación de perito (no se borra).
 * URL: POST /peritajes/peritos/estado  con  id=5 y activo=true|false
 * Regresa a la pantalla de edición con el resultado.
 *
 * TODO: solo PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/peritajes/peritos/estado")
public class PeritoEstadoServlet extends HttpServlet {

    private final PeritoService peritoService = Peritajes.peritoService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = Parametros.numero(request, "id");
        if (id == null || peritoService.buscar(id).isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        try {
            Perito perito = peritoService.cambiarEstado(id, Parametros.booleano(request, "activo"));
            // RF 3.4: confirmación de baja o reactivación
            FlashMessage.exito(request, perito.isActivo() ? "Perito reactivado" : "Perito dado de baja",
                    perito.getNombreCompleto() + " · " + perito.getNumeroRegistro()
                            + (perito.isActivo() ? " vuelve a participar en la asignación automática."
                                                 : " quedó Inactivo y ya no recibe asignaciones."));
        } catch (ReglaNegocioException e) {
            FlashMessage.agregar(request, "errorEstado", e.getMessage());
        }
        Vistas.redirigir(request, response, "/peritajes/peritos/editar?id=" + id);
    }
}
