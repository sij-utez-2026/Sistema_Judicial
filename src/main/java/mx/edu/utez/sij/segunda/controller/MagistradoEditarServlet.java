package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.dto.MagistradoForm;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.model.Magistrado;
import mx.edu.utez.sij.segunda.service.MagistradoService;
import mx.edu.utez.sij.segunda.service.SalaService;

import java.io.IOException;
import java.util.Optional;
import java.util.Map;

/**
 * RF 4.4 Edición de magistrado (en la misma pantalla va el cambio de estado, RF 4.5).
 * URL: GET /segunda/magistrados/editar?id=5 · POST /segunda/magistrados/editar?id=5
 * Vista: WEB-INF/jsp/segunda/magistrados/editar.jsp
 *
 * TODO: solo SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/magistrados/editar")
public class MagistradoEditarServlet extends HttpServlet {

    private final MagistradoService magistradoService = Segunda.magistradoService();
    private final SalaService salaService = Segunda.salaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        Optional<Magistrado> m = buscar(request);
        if (m.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }
        request.setAttribute("magistradoForm", magistradoService.formularioDe(m.get()));
        mostrar(request, response, m.get());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        Optional<Magistrado> m = buscar(request);
        if (m.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }
        MagistradoForm form = MagistradoFormSupport.leer(request);
        Map<String, String> errores = Validador.validar(form);
        if (errores.isEmpty()) {
            try {
                magistradoService.actualizar(m.get().getId(), form);
                FlashMessage.exito(request, "Datos actualizados", "La cuenta del magistrado fue actualizada.");
                Vistas.redirigir(request, response, "/segunda/magistrados/editar?id=" + m.get().getId());
                return;
            } catch (RuntimeException e) {
                request.setAttribute("error", e.getMessage());
            }
        }
        request.setAttribute("magistradoForm", form);
        request.setAttribute("errores", errores);
        mostrar(request, response, m.get());
    }

    private Optional<Magistrado> buscar(HttpServletRequest request) {
        Long id = Parametros.numero(request, "id");
        return id == null ? Optional.empty() : magistradoService.buscar(id);
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response, Magistrado m)
            throws ServletException, IOException {
        request.setAttribute("magistrado", m);
        MagistradoFormSupport.datosDelFormulario(request, salaService);
        Vistas.mostrar(request, response, "segunda/magistrados/editar");
    }
}
