package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.dto.SalaForm;
import mx.edu.utez.sij.segunda.model.Sala;
import mx.edu.utez.sij.segunda.service.SalaService;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * RF 3.3 Edición de sala (en la misma pantalla va el cambio de estado, RF 3.4).
 * URL: GET /segunda/salas/editar?id=5 · POST /segunda/salas/editar?id=5
 * Vista: WEB-INF/jsp/segunda/salas/editar.jsp
 *
 * TODO: solo SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/salas/editar")
public class SalaEditarServlet extends HttpServlet {

    private final SalaService salaService = Segunda.salaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        Optional<Sala> sala = buscar(request);
        if (sala.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }
        request.setAttribute("salaForm", salaService.formularioDe(sala.get()));
        mostrar(request, response, sala.get());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        Optional<Sala> sala = buscar(request);
        if (sala.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        SalaForm form = SalaFormSupport.leer(request);
        Map<String, String> errores = Validador.validar(form);

        if (errores.isEmpty()) {
            try {
                Sala actualizada = salaService.actualizar(sala.get().getId(), form);
                FlashMessage.exito(request, "Datos actualizados correctamente",
                        actualizada.getNombre() + " · "
                                + (actualizada.isActiva() ? "Activa." : "Inactiva."));
                Vistas.redirigir(request, response, "/segunda/salas");
                return;
            } catch (ReglaNegocioException e) {
                request.setAttribute("error", e.getMessage());
            }
        }

        request.setAttribute("salaForm", form);
        request.setAttribute("errores", errores);
        mostrar(request, response, sala.get());
    }

    private Optional<Sala> buscar(HttpServletRequest request) {
        Long id = Parametros.numero(request, "id");
        return id == null ? Optional.empty() : salaService.buscar(id);
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response, Sala sala)
            throws ServletException, IOException {
        request.setAttribute("sala", sala);
        SalaFormSupport.datosDelFormulario(request, salaService);
        Vistas.mostrar(request, response, "segunda/salas/editar");
    }
}

