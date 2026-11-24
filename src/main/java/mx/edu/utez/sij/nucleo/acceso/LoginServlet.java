package mx.edu.utez.sij.nucleo.acceso;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Inicio de sesión de todo el sistema (INTEGRADORES). docs/ARQUITECTURA.md 7.1.
 * URL: GET /login (formulario) · POST /login (correo y contrasena). Ruta pública.
 * Vista: WEB-INF/jsp/nucleo/login.jsp (diseño "Acceso institucional").
 * Si entra, va a / : el administrador general ve los cinco módulos; los demás van directo al suyo.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AccesoDao dao = new AccesoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (Acceso.usuario(request) != null) {
            Vistas.redirigir(request, response, "/");     // ya tiene sesión
            return;
        }
        Vistas.mostrar(request, response, "nucleo/login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String correo = Parametros.texto(request, "correo");
        ResultadoAcceso resultado = dao.iniciarSesion(correo, Parametros.crudo(request, "contrasena"));

        if (resultado.getUsuario() != null) {
            Acceso.iniciar(request, resultado.getUsuario());
            Vistas.redirigir(request, response, "/");
            return;
        }
        // La contraseña nunca se regresa al formulario
        request.setAttribute("error", resultado.getError());
        request.setAttribute("correo", correo);
        Vistas.mostrar(request, response, "nucleo/login");
    }
}
