package mx.edu.utez.sij.copias.controller.admin;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Vuelve a la paleta institucional (COPIAS_ADMIN).
 * URL: POST /copias/admin/colores/restablecer
 */
@WebServlet("/copias/admin/colores/restablecer")
public class ColoresRestablecerServlet extends HttpServlet {

    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.ADMIN);
        if (usuario == null) {
            return;
        }
        Copias.coloresService().restablecer(usuario.getId());
        FlashMessage.exito(request, "Colores restablecidos", "Se volvió a la paleta institucional.");
        Vistas.redirigir(request, response, "/copias/admin/colores");
    }
}
