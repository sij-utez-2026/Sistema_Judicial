package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.Peritajes;
import mx.edu.utez.sij.peritajes.dto.PeritoForm;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.service.PeritoService;
import mx.edu.utez.sij.peritajes.service.ReglaNegocioException;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * RF 3.3 Edición de perito (en la misma pantalla está el cambio de estado, RF 3.4).
 * URL: GET /peritajes/peritos/editar?id=5 (formulario) · POST /peritajes/peritos/editar?id=5 (guardar)
 * Vista: WEB-INF/jsp/peritajes/peritos/editar.jsp
 *
 * TODO: solo PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/peritajes/peritos/editar")
public class PeritoEditarServlet extends HttpServlet {

    private final PeritoService peritoService = Peritajes.peritoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<Perito> perito = buscar(request);
        if (perito.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }
        request.setAttribute("peritoForm", peritoService.formularioDe(perito.get()));
        mostrar(request, response, perito.get());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<Perito> perito = buscar(request);
        if (perito.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        PeritoForm form = PeritoFormSupport.leer(request);
        Map<String, String> errores = Validador.validar(form);
        if (errores.isEmpty()) {
            try {
                Perito actualizado = peritoService.actualizar(perito.get().getId(), form);
                FlashMessage.exito(request, "Datos actualizados correctamente", actualizado.getNombreCompleto() + " · "
                        + actualizado.getNumeroRegistro() + " actualizado. Su estado permanece "
                        + (actualizado.isActivo() ? "Activo." : "Inactivo."));
                Vistas.redirigir(request, response, "/peritajes/peritos");
                return;
            } catch (ReglaNegocioException e) {
                request.setAttribute("error", e.getMessage());
            }
        }

        request.setAttribute("peritoForm", form);
        request.setAttribute("errores", errores);
        mostrar(request, response, perito.get());
    }

    private Optional<Perito> buscar(HttpServletRequest request) {
        Long id = Parametros.numero(request, "id");
        return id == null ? Optional.empty() : peritoService.buscar(id);
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response, Perito perito)
            throws ServletException, IOException {
        request.setAttribute("perito", perito);
        request.setAttribute("carga", peritoService.carga(perito.getId()));
        request.setAttribute("peritajesActivos", peritoService.peritajesActivos(perito.getId()));
        PeritoFormSupport.datosDelFormulario(request, peritoService);
        Vistas.mostrar(request, response, "peritajes/peritos/editar");
    }
}
