package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.dto.MagistradoForm;
import mx.edu.utez.sij.segunda.model.Magistrado;
import mx.edu.utez.sij.segunda.service.MagistradoService;
import mx.edu.utez.sij.segunda.service.SalaService;

import java.io.IOException;
import java.util.Map;

/**
 * RF 4.1 Registro de magistrado + RF 4.3 asignación de sala.
 * URL: GET /segunda/magistrados/nuevo · POST /segunda/magistrados/nuevo
 * Vista: WEB-INF/jsp/segunda/magistrados/registro.jsp
 *
 * TODO: solo SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/magistrados/nuevo")
public class MagistradoRegistrarServlet extends HttpServlet {

    private final MagistradoService magistradoService = Segunda.magistradoService();
    private final SalaService salaService = Segunda.salaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        request.setAttribute("magistradoForm", new MagistradoForm());
        MagistradoFormSupport.datosDelFormulario(request, salaService);
        Vistas.mostrar(request, response, "segunda/magistrados/registro");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        MagistradoForm form = MagistradoFormSupport.leer(request);
        Map<String, String> errores = Validador.validar(form);

        if (errores.isEmpty()) {
            try {
                Magistrado m = magistradoService.registrar(form);
                Segunda.registrarBitacora(request, Segunda.usuarioActual(request), "magistrado_Seg", m.getId(),
                        "Magistrado registrado", m.getNombreCompleto() + " · usuario " + m.getUsuarioId(), "green");
                FlashMessage.exito(request, "Registro exitoso",
                        m.getNombreCompleto() + " quedó registrado como magistrado"
                                + (m.isSinSala() ? " (sin sala asignada)."
                                : " en la " + m.getSalaNombre() + ".")
                                + " Contraseña temporal: " + m.getContrasenaTemporal()
                                + ". Cámbiela desde Mi perfil.");
                Vistas.redirigir(request, response, "/segunda/magistrados");
                return;
            } catch (ReglaNegocioException e) {
                request.setAttribute("error", e.getMessage());
            }
        }

        request.setAttribute("magistradoForm", form);
        request.setAttribute("errores", errores);
        MagistradoFormSupport.datosDelFormulario(request, salaService);
        Vistas.mostrar(request, response, "segunda/magistrados/registro");
    }
}
