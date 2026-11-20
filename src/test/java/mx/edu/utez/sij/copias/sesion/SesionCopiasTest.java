package mx.edu.utez.sij.copias.sesion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.copias.temporal.UsuarioServiceTemporal;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Quién usa Copias (SesionCopias), la equivalencia de roles con Oficialía (RolesCopias) y el permiso (AccesoTemporal).
 * Usa una petición y una sesión falsas muy simples (solo guardan atributos), sin Tomcat.
 */
class SesionCopiasTest {

    private final UsuarioServiceTemporal usuarios = new UsuarioServiceTemporal();
    private final SesionCopias sesion = new SesionCopias(usuarios);
    private final AccesoTemporal acceso = new AccesoTemporal(sesion);

    private final Long admin = usuarios.crear("Admin Copias", "admin@example.com", RolesCopias.ADMIN).getId();
    private final Long trabajador = usuarios.crear("Trabajadora", "t@example.com", RolesCopias.PERSONAL).getId();
    private final Long adminOp = usuarios.crear("Admin OP", "aop@example.com", "admin_op").getId();
    private final Long usuarioOp = usuarios.crear("Usuario OP", "uop@example.com", "usuario_op").getId();
    private final Long ajeno = usuarios.crear("Perito", "p@example.com", "PERITAJES_PERITO").getId();

    // ===== Petición y sesión falsas =====
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
    void equivalenciaDeRolesConOficialia() {
        assertEquals(RolesCopias.ADMIN, RolesCopias.rolEnCopias(List.of("admin_op")));
        assertEquals(RolesCopias.PERSONAL, RolesCopias.rolEnCopias(List.of("usuario_op")));
        assertEquals(RolesCopias.ADMIN, RolesCopias.rolEnCopias(List.of("COPIAS_ADMIN")));
        assertEquals(RolesCopias.PERSONAL, RolesCopias.rolEnCopias(List.of("COPIAS_PERSONAL")));
        // Con roles de varios módulos gana el de administrador
        assertEquals(RolesCopias.ADMIN, RolesCopias.rolEnCopias(List.of("usuario_op", "COPIAS_ADMIN")));
        // Sin rol de Copias ni de Oficialía no entra; el superadministrador no se toca por ahora
        assertNull(RolesCopias.rolEnCopias(List.of("PERITAJES_PERITO")));
        assertNull(RolesCopias.rolEnCopias(List.of("Superadministrador")));
        assertTrue(RolesCopias.vieneDeOficialia(List.of("usuario_op")));
        assertFalse(RolesCopias.vieneDeOficialia(List.of("COPIAS_PERSONAL")));
    }

    @Test
    void sinElegirANadieEntraElPrimerTrabajador() {
        UsuarioCopias usuario = sesion.usuario(peticion);
        assertEquals(trabajador, usuario.getId());
        assertEquals(RolesCopias.PERSONAL, usuario.getRol());
        assertFalse(usuario.isDeOficialia());
    }

    @Test
    void usuarioDeOficialiaEntraComoTrabajadorYAdminOpComoAdministrador() {
        sesion.elegirUsuarioDePrueba(peticion, usuarioOp);
        UsuarioCopias uop = sesion.usuario(peticion);
        assertEquals("Usuario OP", uop.getNombre());
        assertEquals(RolesCopias.PERSONAL, uop.getRol());
        assertTrue(uop.isDeOficialia());

        sesion.elegirUsuarioDePrueba(peticion, adminOp);
        assertTrue(sesion.esAdmin(peticion));
        assertTrue(sesion.usuario(peticion).isDeOficialia());
    }

    @Test
    void soloSeEligenUsuariosActivosConRolDeCopias() {
        assertThrows(ReglaNegocioException.class, () -> sesion.elegirUsuarioDePrueba(peticion, ajeno));
        assertThrows(ReglaNegocioException.class, () -> sesion.elegirUsuarioDePrueba(peticion, 999L));
        assertEquals(4, sesion.usuariosDePrueba().size());

        // Si desactivan al elegido, vuelve al primer trabajador activo
        sesion.elegirUsuarioDePrueba(peticion, usuarioOp);
        usuarios.cambiarActivo(usuarioOp, false);
        assertEquals(trabajador, sesion.usuario(peticion).getId());
    }

    @Test
    void cadaRolSoloEntraASusPantallas() throws Exception {
        UsuarioCopias usuario = acceso.exigirRol(peticion, respuesta, RolesCopias.PERSONAL);
        assertNotNull(usuario);
        assertNull(acceso.exigirRol(peticion, respuesta, RolesCopias.ADMIN));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, errorEnviado);

        sesion.elegirUsuarioDePrueba(peticion, admin);
        assertEquals(admin, acceso.exigirRol(peticion, respuesta, RolesCopias.ADMIN).getId());
        assertNull(acceso.exigirRol(peticion, respuesta, RolesCopias.PERSONAL));
    }
}
