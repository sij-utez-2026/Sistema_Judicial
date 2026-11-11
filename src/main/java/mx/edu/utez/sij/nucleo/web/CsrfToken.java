package mx.edu.utez.sij.nucleo.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Token contra CSRF (que otra página envíe formularios a nombre del usuario).
 *
 * - FiltroNucleo crea un token por sesión y lo deja en la petición como "_csrf".
 * - &lt;n:csrf/&gt; lo escribe como campo oculto en cada &lt;form method="post"&gt;.
 * - El layout lo deja en &lt;meta name="_csrf"&gt; para los fetch con POST (SIJ.encabezadosCsrf en comun.js).
 * - FiltroNucleo rechaza con 403 todo POST que no traiga el token correcto.
 *
 * Los nombres de los getters son los mismos que usaba Spring Security, así las JSP no cambiaron.
 */
public class CsrfToken {

    public static final String PARAMETRO = "_csrf";
    public static final String ENCABEZADO = "X-CSRF-TOKEN";
    static final String ATRIBUTO_SESION = "sij.csrf";

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final String token;

    private CsrfToken(String token) {
        this.token = token;
    }

    /** El token de la sesión; lo crea si todavía no existe. */
    static CsrfToken deLaSesion(HttpServletRequest request) {
        HttpSession sesion = request.getSession();
        String token = (String) sesion.getAttribute(ATRIBUTO_SESION);
        if (token == null) {
            byte[] bytes = new byte[32];
            ALEATORIO.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            sesion.setAttribute(ATRIBUTO_SESION, token);
        }
        return new CsrfToken(token);
    }

    /** true si la petición trae el token de su sesión (en el formulario o en el encabezado). */
    static boolean esValido(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute(ATRIBUTO_SESION) == null) {
            return false;
        }
        String esperado = (String) sesion.getAttribute(ATRIBUTO_SESION);
        String recibido = request.getHeader(ENCABEZADO);
        if (recibido == null) {
            recibido = request.getParameter(PARAMETRO);
        }
        return recibido != null && MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8), recibido.getBytes(StandardCharsets.UTF_8));
    }

    public String getToken() {
        return token;
    }

    public String getParameterName() {
        return PARAMETRO;
    }

    public String getHeaderName() {
        return ENCABEZADO;
    }
}
