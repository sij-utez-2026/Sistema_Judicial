package mx.edu.utez.sij.copias;

import java.util.Collection;
import java.util.Locale;

/**
 * Los dos roles del módulo (docs/modulos/copias-certificadas.md, sección 3) y su equivalencia con
 * los roles de Oficialía de Partes, porque Copias funciona como una sección más de Oficialía:
 *
 *   admin_op   (administrador de usuarios de OP) → COPIAS_ADMIN     (trabajadores y colores)
 *   usuario_op (usuario de OP)                   → COPIAS_PERSONAL  (captura, valida, cobra y entrega)
 *
 * Es el ÚNICO lugar donde se decide qué rol tiene alguien dentro de Copias. Si Oficialía cambia el nombre
 * de sus roles, solo se cambian las constantes de abajo.
 */
public final class RolesCopias {

    /** Trabajador o juzgado: captura, valida, cobra y entrega. */
    public static final String PERSONAL = "COPIAS_PERSONAL";

    /** Administrador del área: trabajadores y colores de Copias. */
    public static final String ADMIN = "COPIAS_ADMIN";

    /** Roles de Oficialía de Partes (nombres que dio el Equipo 1). */
    public static final String OFICIALIA_ADMIN = "admin_op";
    public static final String OFICIALIA_USUARIO = "usuario_op";

    /** Inicio de Oficialía, para el enlace "Volver a Oficialía" (inicio del módulo según ARQUITECTURA). */
    public static final String INICIO_OFICIALIA = "/oficialia";

    private RolesCopias() {
    }

    /**
     * Rol dentro de Copias según los roles del usuario (puede tener varios, de varios módulos).
     * Si tiene los dos, gana el de administrador. Devuelve null si no puede entrar a Copias.
     */
    public static String rolEnCopias(Collection<String> roles) {
        boolean admin = false;
        boolean personal = false;
        for (String rol : roles) {
            String r = rol == null ? "" : rol.trim();
            if (r.equals(ADMIN) || r.toLowerCase(Locale.ROOT).equals(OFICIALIA_ADMIN)) {
                admin = true;
            } else if (r.equals(PERSONAL) || r.toLowerCase(Locale.ROOT).equals(OFICIALIA_USUARIO)) {
                personal = true;
            }
        }
        if (admin) {
            return ADMIN;
        }
        return personal ? PERSONAL : null;
    }

    /** true si entra con un rol de Oficialía (para mostrar "Volver a Oficialía"). */
    public static boolean vieneDeOficialia(Collection<String> roles) {
        return roles.stream().anyMatch(r -> r != null
                && (r.trim().equalsIgnoreCase(OFICIALIA_ADMIN) || r.trim().equalsIgnoreCase(OFICIALIA_USUARIO)));
    }

    /** "Administrador" o "Trabajador", para mostrar en pantalla. */
    public static String texto(String rolCopias) {
        return ADMIN.equals(rolCopias) ? "Administrador" : "Trabajador";
    }
}
