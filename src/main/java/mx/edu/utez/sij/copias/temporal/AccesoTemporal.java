package mx.edu.utez.sij.copias.temporal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * TEMPORAL: revisa el rol del "Modo de prueba" mientras el núcleo no tiene inicio de sesión.
 * Si no corresponde, responde 403 y se ve la página de error del sistema (error/403.jsp).
 *
 * Cada servlet la llama al inicio de doGet y doPost:
 *     if (!acceso.exigirRol(request, response, RolesCopias.PERSONAL)) return;
 * Cuando FiltroNucleo revise los roles por ruta, esas líneas y esta clase se borran.
 */
public class AccesoTemporal {

    private final SesionTemporal sesion;

    public AccesoTemporal(SesionTemporal sesion) {
        this.sesion = sesion;
    }

    /** true si puede seguir; false si ya respondió 403 (el servlet debe hacer return). */
    public boolean exigirRol(HttpServletRequest request, HttpServletResponse response, String rol) throws IOException {
        if (!rol.equals(sesion.rol(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
