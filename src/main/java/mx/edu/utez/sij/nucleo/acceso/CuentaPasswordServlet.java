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
 * Cambiar contraseña con la sesión iniciada (INTEGRADORES): actual, nueva y confirmación.
 * URL: GET /cuenta/password (formulario) · POST /cuenta/password (guardar). Cualquier usuario con sesión.
 * Vista: WEB-INF/jsp/nucleo/password.jsp
 */
@WebServlet("/cuenta/password")
public class CuentaPasswordServlet extends HttpServlet {

    private final AccesoDao dao = new AccesoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Vistas.mostrar(request, response, "nucleo/password");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String error = dao.cambiarContrasena(Acceso.usuario(request), Parametros.crudo(request, "actual"),
                Parametros.crudo(request, "nueva"), Parametros.crudo(request, "confirmacion"));
        if (error == null) {
            FlashMessage.exito(request, "Contraseña actualizada", "Use la nueva contraseña la próxima vez que inicie sesión.");
            Vistas.redirigir(request, response, "/");
            return;
        }
        // Las contraseñas nunca se regresan al formulario
        request.setAttribute("error", error);
        Vistas.mostrar(request, response, "nucleo/password");
    }
}
