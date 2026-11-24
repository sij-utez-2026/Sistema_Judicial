package mx.edu.utez.sij.nucleo.acceso;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * La sesión del usuario (INTEGRADORES). Lo usan el núcleo y los módulos:
 *
 * <pre>
 * UsuarioSesion usuario = Acceso.usuario(request);   // nunca es null dentro de /&lt;modulo&gt;/**: FiltroNucleo ya lo revisó
 * if (usuario.isAdministradorGeneral()) { ... }
 * </pre>
 *
 * FiltroNucleo ya revisa que haya sesión y que el rol pueda entrar al módulo de la URL.
 * El permiso de cada pantalla (por ejemplo, solo PERITAJES_ADMIN) lo revisa el servlet del módulo.
 */
public final class Acceso {

    static final String ATRIBUTO_SESION = "usuarioSesion";

    private Acceso() {
    }

    /** El usuario de la petición (lo deja FiltroNucleo). null si no hay sesión. */
    public static UsuarioSesion usuario(HttpServletRequest request) {
        Object usuario = request.getAttribute(ATRIBUTO_SESION);
        return usuario instanceof UsuarioSesion u ? u : null;
    }

    /**
     * Lo llama FiltroNucleo en cada petición: lee el usuario de la sesión, lo vuelve a leer de la base
     * (si lo dieron de baja o le cambiaron el rol, se nota de inmediato) y lo deja en la petición.
     * Regresa null si no hay sesión o si el usuario ya no está activo (en ese caso cierra la sesión).
     */
    public static UsuarioSesion cargar(HttpServletRequest request, AccesoDao dao) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute(ATRIBUTO_SESION) instanceof UsuarioSesion guardado)) {
            return null;
        }
        UsuarioSesion actual = dao.getActivo(guardado);
        if (actual == null) {
            sesion.invalidate();
            return null;
        }
        sesion.setAttribute(ATRIBUTO_SESION, actual);
        request.setAttribute(ATRIBUTO_SESION, actual);
        return actual;
    }

    /**
     * Después de un inicio de sesión correcto: se cambia la sesión por una nueva (evita que alguien fije
     * la sesión de otro, docs/ARQUITECTURA.md sección 10) y se guarda el usuario.
     */
    static void iniciar(HttpServletRequest request, UsuarioSesion usuario) {
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        request.getSession(true).setAttribute(ATRIBUTO_SESION, usuario);
    }

    static void cerrar(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }
}
