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
import mx.edu.utez.sij.segunda.dto.SalaForm;
import mx.edu.utez.sij.segunda.model.Sala;
import mx.edu.utez.sij.segunda.service.SalaService;

import java.io.IOException;
import java.util.Map;

/**
 * RF 3.1 Registro de sala.
 * URL: GET /segunda/salas/nueva (formulario) · POST /segunda/salas/nueva (guardar)
 * Vista: WEB-INF/jsp/segunda/salas/registro.jsp
 *
 * TODO: solo SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/salas/nueva")
public class SalaRegistrarServlet extends HttpServlet {

    private final SalaService salaService = Segunda.salaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        request.setAttribute("salaForm", new SalaForm());
        SalaFormSupport.datosDelFormulario(request, salaService);
        Vistas.mostrar(request, response, "segunda/salas/registro");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        SalaForm form = SalaFormSupport.leer(request);
        Map<String, String> errores = Validador.validar(form);

        if (errores.isEmpty()) {
            try {
                Sala sala = salaService.registrar(form);
                Segunda.registrarBitacora(request, Segunda.usuarioActual(request), "sala_Seg", sala.getId(),
                        "Sala registrada", sala.getNombre() + " · distrito " + sala.getDistritoId(), "green");
                FlashMessage.exito(request, "Registro exitoso",
                        sala.getNombre() + " quedó registrada como activa.");
                Vistas.redirigir(request, response, "/segunda/salas");
                return;
            } catch (ReglaNegocioException e) {
                request.setAttribute("error", e.getMessage());
            }
        }

        request.setAttribute("salaForm", form);
        request.setAttribute("errores", errores);
        SalaFormSupport.datosDelFormulario(request, salaService);
        Vistas.mostrar(request, response, "segunda/salas/registro");
    }
}
