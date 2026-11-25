package mx.edu.utez.sij.nucleo.acceso;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Cerrar sesión (INTEGRADORES). Es POST con el token CSRF (botón "Cerrar sesión" del layout), para que un
 * enlace de otra página no pueda cerrar la sesión.
 * URL: POST /logout
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Acceso.cerrar(request);
        FlashMessage.exito(request, "Sesión cerrada", null);
        Vistas.redirigir(request, response, "/login");
    }
}
