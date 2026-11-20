package mx.edu.utez.sij.copias.temporal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.sesion.SesionCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;

import java.io.IOException;

/**
 * TEMPORAL: revisa el rol dentro de Copias mientras FiltroNucleo no revisa los permisos por ruta.
 * Si no corresponde, responde 403 y se ve la página de error del sistema (error/403.jsp).
 *
 * Cada servlet la llama al inicio de doGet y doPost, y de paso obtiene quién hace la acción:
 *     UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.PERSONAL);
 *     if (usuario == null) return;
 * Cuando el núcleo revise los permisos, se cambia por su ayudante (ARQUITECTURA, sección 10) y esta clase se borra.
 */
public class AccesoTemporal {

    private final SesionCopias sesion;

    public AccesoTemporal(SesionCopias sesion) {
        this.sesion = sesion;
    }

    /** El usuario si tiene el rol; si no, responde 403 y devuelve null (el servlet debe hacer return). */
    public UsuarioCopias exigirRol(HttpServletRequest request, HttpServletResponse response, String rol) throws IOException {
        UsuarioCopias usuario = sesion.usuario(request);
        if (usuario == null || !rol.equals(usuario.getRol())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return usuario;
    }
}
