package mx.edu.utez.sij.nucleo.acceso;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Contraseña nueva desde el enlace del correo de recuperación (INTEGRADORES).
 * URL: GET /recuperar/restablecer?token=... (formulario) · POST /recuperar/restablecer (guardar). Ruta pública.
 * Vista: WEB-INF/jsp/nucleo/restablecer.jsp
 *
 * El token sale de TokenRecuperacion: si no existe, ya venció o ya se usó, se muestra el aviso y el enlace
 * para pedir otro. Al guardar, el token se invalida y se regresa al inicio de sesión.
 */
@WebServlet("/recuperar/restablecer")
public class RestablecerServlet extends HttpServlet {

    private final AccesoDao dao = new AccesoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mostrar(request, response, Parametros.texto(request, "token"), null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = Parametros.texto(request, "token");
        String correo = TokenRecuperacion.correoDe(token);
        if (correo == null) {
            mostrar(request, response, token, null);
            return;
        }
        String error = dao.restablecerContrasena(correo, Parametros.crudo(request, "nueva"),
                Parametros.crudo(request, "confirmacion"));
        if (error != null) {
            // Las contraseñas nunca se regresan al formulario
            mostrar(request, response, token, error);
            return;
        }
        TokenRecuperacion.usar(token);
        FlashMessage.exito(request, "Contraseña actualizada", "Ya puede iniciar sesión con su contraseña nueva.");
        Vistas.redirigir(request, response, "/login");
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response, String token, String error)
            throws ServletException, IOException {
        String correo = TokenRecuperacion.correoDe(token);
        request.setAttribute("enlaceValido", correo != null);
        request.setAttribute("token", token);
        request.setAttribute("correo", correo);
        if (error != null) {
            request.setAttribute("error", error);
        }
        Vistas.mostrar(request, response, "nucleo/restablecer");
    }
}
