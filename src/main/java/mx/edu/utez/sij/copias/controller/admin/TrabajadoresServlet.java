package mx.edu.utez.sij.copias.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.dto.PersonalForm;
import mx.edu.utez.sij.copias.service.PersonalCopiasService;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.copias.temporal.UsuarioServiceTemporal.Usuario;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;
import java.util.Map;

/**
 * RF 6.1 Trabajadores del área (COPIAS_ADMIN): lista y alta de COPIAS_PERSONAL.
 * URL: GET /copias/admin/personal · POST /copias/admin/personal (agregar)
 */
@WebServlet("/copias/admin/personal")
public class TrabajadoresServlet extends HttpServlet {

    private final PersonalCopiasService personalService = Copias.personalService();
    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!acceso.exigirRol(request, response, RolesCopias.ADMIN)) {
            return;
        }
        request.setAttribute("personalForm", new PersonalForm());
        mostrar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!acceso.exigirRol(request, response, RolesCopias.ADMIN)) {
            return;
        }
        PersonalForm form = new PersonalForm();
        form.setNombre(Parametros.texto(request, "nombre"));
        form.setCorreo(Parametros.texto(request, "correo"));
        Map<String, String> errores = Validador.validar(form);

        if (errores.isEmpty()) {
            try {
                Usuario usuario = personalService.registrar(form);
                FlashMessage.exito(request, "Trabajador agregado correctamente",
                        usuario.getNombre() + " · " + usuario.getCorreo() + " ya puede atender solicitudes.");
                Vistas.redirigir(request, response, "/copias/admin/personal");
                return;
            } catch (ReglaNegocioException e) {
                request.setAttribute("error", e.getMessage());
            }
        }
        request.setAttribute("personalForm", form);
        request.setAttribute("errores", errores);
        mostrar(request, response);
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("trabajadores", personalService.trabajadores());
        Vistas.mostrar(request, response, "copias/admin/personal");
    }
}
