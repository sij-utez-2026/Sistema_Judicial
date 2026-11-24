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
 * Recuperación de contraseña (INTEGRADORES). Diseño "Recuperación de contraseña".
 * URL: GET /recuperar (formulario) · POST /recuperar (correo). Ruta pública.
 * Vista: WEB-INF/jsp/nucleo/recuperar.jsp
 *
 * TEMPORAL: por ahora SOLO muestra la pantalla y el aviso "Solicitud recibida" (decisión del equipo):
 * no envía nada porque todavía no hay correo. Cuando exista, aquí se genera el enlace de un solo uso
 * vigente 15 minutos (docs/ARQUITECTURA.md 7.1) y se escribe en la consola en desarrollo.
 * El aviso es el mismo exista o no el correo, para no revelar qué cuentas existen.
 */
@WebServlet("/recuperar")
public class RecuperarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Vistas.mostrar(request, response, "nucleo/recuperar");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String correo = Parametros.texto(request, "correo");
        if (correo == null) {
            request.setAttribute("error", "Escriba el correo de su cuenta institucional");
            Vistas.mostrar(request, response, "nucleo/recuperar");
            return;
        }
        FlashMessage.agregar(request, "solicitudRecibida", true);
        FlashMessage.agregar(request, "correo", correo);
        Vistas.redirigir(request, response, "/recuperar");
    }
}
