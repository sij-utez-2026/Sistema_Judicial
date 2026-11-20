package mx.edu.utez.sij.copias.temporal;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * TEMPORAL: cambia entre la vista del trabajador y la del administrador (selector "Modo de prueba" del menú).
 * URL: POST /copias/modo-prueba  con  rol=COPIAS_PERSONAL|COPIAS_ADMIN
 * Se borra cuando exista el inicio de sesión del núcleo: el rol saldrá de la cuenta con la que se entra.
 */
@WebServlet("/copias/modo-prueba")
public class ModoPruebaServlet extends HttpServlet {

    private final SesionTemporal sesion = Copias.sesion();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rol = Parametros.texto(request, "rol");
        try {
            sesion.cambiarRol(request, rol);
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            Vistas.redirigir(request, response, "/copias");
            return;
        }
        boolean admin = RolesCopias.ADMIN.equals(rol);
        FlashMessage.exito(request, "Modo de prueba: " + (admin ? "Administrador" : "Trabajador"), null);
        Vistas.redirigir(request, response, admin ? "/copias/admin" : "/copias");
    }
}
