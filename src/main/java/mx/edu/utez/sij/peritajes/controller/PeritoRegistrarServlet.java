package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.Peritajes;
import mx.edu.utez.sij.peritajes.dto.PeritoForm;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.service.PeritoService;
import mx.edu.utez.sij.peritajes.service.ReglaNegocioException;

import java.io.IOException;
import java.util.Map;

/**
 * RF 3.1 Registro de perito.
 * URL: GET /peritajes/peritos/nuevo (formulario) · POST /peritajes/peritos/nuevo (guardar)
 * Vista: WEB-INF/jsp/peritajes/peritos/registro.jsp
 *
 * TODO: solo PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/peritajes/peritos/nuevo")
public class PeritoRegistrarServlet extends HttpServlet {

    private final PeritoService peritoService = Peritajes.peritoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("peritoForm", new PeritoForm());
        PeritoFormSupport.datosDelFormulario(request, peritoService);
        Vistas.mostrar(request, response, "peritajes/peritos/registro");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PeritoForm form = PeritoFormSupport.leer(request);
        Map<String, String> errores = Validador.validar(form);

        if (errores.isEmpty()) {
            try {
                Perito perito = peritoService.registrar(form);
                // RF 3.1: confirmación de registro exitoso, en estado Activo
                FlashMessage.exito(request, "Registro exitoso", perito.getNombreCompleto() + " · "
                        + perito.getNumeroRegistro() + " quedó registrado en estado Activo.");
                Vistas.redirigir(request, response, "/peritajes/peritos");
                return;
            } catch (ReglaNegocioException e) {
                request.setAttribute("error", e.getMessage());
            }
        }

        // Con errores: se vuelve a mostrar el formulario con lo que capturó
        request.setAttribute("peritoForm", form);
        request.setAttribute("errores", errores);
        PeritoFormSupport.datosDelFormulario(request, peritoService);
        Vistas.mostrar(request, response, "peritajes/peritos/registro");
    }
}
