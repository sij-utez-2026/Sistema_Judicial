package mx.edu.utez.sij.copias.temporal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.sij.copias.RolesCopias;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TEMPORAL: "Modo de prueba" y revisión de rol (se borra junto con AccesoTemporal).
 * Usa una petición y una sesión falsas muy simples (solo guardan atributos), sin Tomcat.
 */
class AccesoTemporalTest {

    private final SesionTemporal sesion = new SesionTemporal();
    private final AccesoTemporal acceso = new AccesoTemporal(sesion);

    private final Map<String, Object> atributosSesion = new HashMap<>();
    private final HttpSession sesionWeb = (HttpSession) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{HttpSession.class}, (proxy, metodo, args) -> switch (metodo.getName()) {
                case "getAttribute" -> atributosSesion.get((String) args[0]);
                case "setAttribute" -> atributosSesion.put((String) args[0], args[1]);
                default -> null;
            });
    private final HttpServletRequest peticion = (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{HttpServletRequest.class}, (proxy, metodo, args) ->
                    metodo.getName().equals("getSession") ? sesionWeb : null);

    private int errorEnviado;
    private final HttpServletResponse respuesta = (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{HttpServletResponse.class}, (proxy, metodo, args) -> {
                if (metodo.getName().equals("sendError")) {
                    errorEnviado = (Integer) args[0];
                }
                return null;
            });

    @Test
    void porDefectoEsTrabajador() throws Exception {
        assertEquals(RolesCopias.PERSONAL, sesion.rol(peticion));
        assertFalse(sesion.esAdmin(peticion));
        assertTrue(acceso.exigirRol(peticion, respuesta, RolesCopias.PERSONAL));
    }

    @Test
    void cadaRolSoloEntraASusPantallas() throws Exception {
        assertFalse(acceso.exigirRol(peticion, respuesta, RolesCopias.ADMIN));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, errorEnviado);

        sesion.cambiarRol(peticion, RolesCopias.ADMIN);
        assertTrue(sesion.esAdmin(peticion));
        assertTrue(acceso.exigirRol(peticion, respuesta, RolesCopias.ADMIN));
        assertFalse(acceso.exigirRol(peticion, respuesta, RolesCopias.PERSONAL));
    }

    @Test
    void soloAceptaLosRolesDelModulo() {
        assertThrows(ReglaNegocioException.class, () -> sesion.cambiarRol(peticion, "SISTEMA_ADMIN"));
        assertEquals(RolesCopias.PERSONAL, sesion.rol(peticion));
    }
}
