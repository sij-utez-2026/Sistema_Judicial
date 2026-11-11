package mx.edu.utez.sij.nucleo.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mensajes que sobreviven a UNA redirección (patrón Post/Redirect/Get).
 *
 * <pre>
 * // En el servlet, después de guardar:
 * FlashMessage.exito(request, "Registro exitoso", "Elena Salgado · PER-0048 quedó Activo.");
 * Vistas.redirigir(request, response, "/peritajes/peritos");
 * </pre>
 *
 * FiltroNucleo los pasa a la siguiente petición como atributos ("exito", "exitoDetalle", "error", ...)
 * y los borra de la sesión. El layout los muestra con &lt;n:mensajes/&gt;.
 *
 * Cuando NO hay redirección (por ejemplo, un formulario con errores que se vuelve a mostrar),
 * se usa directamente request.setAttribute("error", "...").
 */
public final class FlashMessage {

    static final String ATRIBUTO_SESION = "sij.flash";

    private FlashMessage() {
    }

    /** Aviso verde arriba a la derecha. El detalle es opcional (puede ser null). */
    public static void exito(HttpServletRequest request, String titulo, String detalle) {
        agregar(request, "exito", titulo);
        if (detalle != null) {
            agregar(request, "exitoDetalle", detalle);
        }
    }

    /** Alerta roja arriba del contenido. */
    public static void error(HttpServletRequest request, String mensaje) {
        agregar(request, "error", mensaje);
    }

    /** Cualquier otro dato que deba llegar a la siguiente pantalla (por ejemplo "errorEstado"). */
    @SuppressWarnings("unchecked")
    public static void agregar(HttpServletRequest request, String nombre, Object valor) {
        HttpSession sesion = request.getSession();
        Map<String, Object> pendientes = (Map<String, Object>) sesion.getAttribute(ATRIBUTO_SESION);
        if (pendientes == null) {
            pendientes = new LinkedHashMap<>();
            sesion.setAttribute(ATRIBUTO_SESION, pendientes);
        }
        pendientes.put(nombre, valor);
    }

    /** Lo llama FiltroNucleo: copia los mensajes pendientes a la petición actual y los borra de la sesión. */
    @SuppressWarnings("unchecked")
    static void entregar(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return;
        }
        Map<String, Object> pendientes = (Map<String, Object>) sesion.getAttribute(ATRIBUTO_SESION);
        if (pendientes != null) {
            sesion.removeAttribute(ATRIBUTO_SESION);
            pendientes.forEach(request::setAttribute);
        }
    }
}
