package mx.edu.utez.sij.nucleo.acceso;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.correo.Correo;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Recuperación de contraseña (INTEGRADORES). Diseño "Recuperación de contraseña".
 * URL: GET /recuperar (formulario) · POST /recuperar (correo). Ruta pública.
 * Vista: WEB-INF/jsp/nucleo/recuperar.jsp
 *
 * Si el correo es de una cuenta activa, se genera un enlace de un solo uso vigente 15 minutos
 * (TokenRecuperacion, docs/ARQUITECTURA.md 7.1) y se manda por correo (nucleo/correo/Correo).
 * Con el correo apagado (correo.properties), el mensaje con el enlace se escribe en la consola de Tomcat.
 * El aviso es el mismo exista o no el correo, para no revelar qué cuentas existen.
 */
@WebServlet("/recuperar")
public class RecuperarServlet extends HttpServlet {

    private final AccesoDao dao = new AccesoDao();

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

        String cuenta = dao.correoParaRecuperar(correo);
        if (cuenta != null) {
            String token = TokenRecuperacion.generar(cuenta);
            String enlace = urlBase(request) + "/recuperar/restablecer?token=" + token;
            try {
                Correo.enviar(cuenta, "Restablecer contraseña · Sistema Judicial Integral", mensaje(enlace));
            } catch (IllegalStateException e) {
                // No se le dice al usuario (revelaría que la cuenta existe); queda en el log de Tomcat
                log("No se pudo enviar el correo de recuperación", e);
            }
        }

        FlashMessage.agregar(request, "solicitudRecibida", true);
        FlashMessage.agregar(request, "correo", correo);
        Vistas.redirigir(request, response, "/recuperar");
    }

    // Dirección del sistema para el enlace. Primero la de correo.properties (correo.url_sistema): en el servidor
    // real debe estar puesta, porque la de la petición sale del encabezado Host, que el navegador puede falsificar
    // para que el enlace apunte a otro sitio. Sin ella (desarrollo): http://localhost:8080/sij
    private String urlBase(HttpServletRequest request) {
        String configurada = Correo.urlSistema();
        if (configurada != null) {
            return configurada;
        }
        String url = request.getRequestURL().toString();
        return url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
    }

    private String mensaje(String enlace) {
        long minutos = TokenRecuperacion.VIGENCIA.toMinutes();
        return "<div style=\"font-family:Arial,sans-serif;font-size:15px;color:#1f2933;max-width:520px\">"
                + "<h2 style=\"color:#1f3a2c\">Restablecer contraseña</h2>"
                + "<p>Recibimos una solicitud para restablecer la contraseña de su cuenta del Sistema Judicial Integral.</p>"
                + "<p><a href=\"" + enlace + "\" style=\"display:inline-block;padding:10px 18px;background:#1f3a2c;"
                + "color:#ffffff;text-decoration:none;border-radius:6px\">Crear contraseña nueva</a></p>"
                + "<p>El enlace sirve <strong>una sola vez</strong> y vence en <strong>" + minutos + " minutos</strong>.</p>"
                + "<p>Si usted no lo pidió, ignore este correo: su contraseña no cambia.</p>"
                + "<p style=\"font-size:12px;color:#6b7280\">Si el botón no funciona, copie este enlace en su navegador:<br>"
                + enlace + "</p>"
                + "</div>";
    }
}
