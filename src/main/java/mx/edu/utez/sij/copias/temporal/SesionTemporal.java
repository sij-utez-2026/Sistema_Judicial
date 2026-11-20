package mx.edu.utez.sij.copias.temporal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.sij.copias.RolesCopias;

/**
 * TEMPORAL: usuario en sesión mientras no existe el inicio de sesión del núcleo.
 *
 * Para poder probar las dos vistas, el rol se guarda en la sesión del navegador y se cambia con el
 * "Modo de prueba" del menú (ModoPruebaServlet). No hay usuario ni contraseña.
 * Si no se ha elegido nada, el rol es COPIAS_PERSONAL.
 *
 * Los services no leen la sesión: el servlet lee el dato y se lo pasa (docs/MIGRACION_SPRING_A_SERVLETS.md).
 * Cuando exista el núcleo: usar el usuario en sesión del núcleo y borrar esta clase, ModoPruebaServlet y AccesoTemporal.
 */
public class SesionTemporal {

    private static final String ATRIBUTO_ROL = "copias.rolTemporal";

    /** Id de core_usuario de quien hace la acción. Hoy siempre el "usuario de prueba" (id 1). */
    public Long usuarioId() {
        return 1L;
    }

    public String rol(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        Object rol = sesion == null ? null : sesion.getAttribute(ATRIBUTO_ROL);
        return rol == null ? RolesCopias.PERSONAL : rol.toString();
    }

    public boolean esAdmin(HttpServletRequest request) {
        return RolesCopias.ADMIN.equals(rol(request));
    }

    /** Solo acepta los dos roles del módulo. */
    public void cambiarRol(HttpServletRequest request, String rol) {
        if (!RolesCopias.PERSONAL.equals(rol) && !RolesCopias.ADMIN.equals(rol)) {
            throw new ReglaNegocioException("Rol no válido");
        }
        request.getSession().setAttribute(ATRIBUTO_ROL, rol);
    }
}
