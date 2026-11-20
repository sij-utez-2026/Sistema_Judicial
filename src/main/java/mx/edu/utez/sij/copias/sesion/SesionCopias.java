package mx.edu.utez.sij.copias.sesion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.copias.temporal.UsuarioServiceTemporal;
import mx.edu.utez.sij.copias.temporal.UsuarioServiceTemporal.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * ÚNICO lugar donde Copias decide quién está usando el módulo. Lo busca en este orden:
 *
 * 1. El usuario que inició sesión con el login del núcleo ("usuarioSesion": id, nombre y roles,
 *    docs/ARQUITECTURA.md sección 10). Así funciona igual si entra desde el menú de Copias o desde el
 *    botón "Copias certificadas" de Oficialía: es la misma sesión. [Pendiente del núcleo, ver delNucleo]
 * 2. TEMPORAL — "Modo de prueba": la persona que se eligió en el menú (un trabajador o el administrador de
 *    Copias, o un usuario de prueba de Oficialía). Si no se ha elegido a nadie, el primer trabajador activo.
 *
 * El rol dentro de Copias sale de RolesCopias.rolEnCopias(...) (admin_op → administrador, usuario_op → trabajador).
 * Copias nunca escribe ni borra atributos de sesión de otros módulos: los suyos empiezan con "copias.".
 */
public class SesionCopias {

    static final String ATRIBUTO_USUARIO_PRUEBA = "copias.usuarioPruebaId";

    private final UsuarioServiceTemporal usuarios;

    public SesionCopias(UsuarioServiceTemporal usuarios) {
        this.usuarios = usuarios;
    }

    /** Quién usa Copias en esta petición, o null si nadie con un rol de Copias. */
    public UsuarioCopias usuario(HttpServletRequest request) {
        UsuarioCopias delNucleo = delNucleo(request);
        if (delNucleo != null) {
            return delNucleo;
        }
        return delModoPrueba(request);
    }

    public boolean esAdmin(HttpServletRequest request) {
        UsuarioCopias usuario = usuario(request);
        return usuario != null && usuario.isAdmin();
    }

    // ===== 1. Login del núcleo =====

    /**
     * TODO núcleo: cuando exista el login, leer aquí el usuario en sesión y quitar el "Modo de prueba". Algo así:
     *
     *     HttpSession sesion = request.getSession(false);
     *     Object u = sesion == null ? null : sesion.getAttribute("usuarioSesion");
     *     if (u instanceof UsuarioSesion us) {
     *         return crear(us.getId(), us.getNombre(), us.getRoles());
     *     }
     *     return null;
     */
    private UsuarioCopias delNucleo(HttpServletRequest request) {
        return null;
    }

    /** Arma el usuario de Copias a partir de los datos de la cuenta (o null si no tiene rol de Copias). */
    static UsuarioCopias crear(Long id, String nombre, List<String> roles) {
        String rol = RolesCopias.rolEnCopias(roles);
        if (rol == null) {
            return null;
        }
        return new UsuarioCopias(id, nombre, rol, RolesCopias.vieneDeOficialia(roles));
    }

    // ===== 2. TEMPORAL: Modo de prueba =====

    /** Usuarios activos que pueden entrar a Copias, para el selector del "Modo de prueba". */
    public List<UsuarioCopias> usuariosDePrueba() {
        List<UsuarioCopias> lista = new ArrayList<>();
        for (Usuario cuenta : usuarios.todos()) {
            UsuarioCopias usuario = cuenta.isActivo() ? crear(cuenta) : null;
            if (usuario != null) {
                lista.add(usuario);
            }
        }
        return lista;
    }

    /** Guarda en la sesión a quién se eligió. Solo acepta usuarios activos con un rol de Copias. */
    public UsuarioCopias elegirUsuarioDePrueba(HttpServletRequest request, Long id) {
        UsuarioCopias usuario = id == null ? null : usuarios.buscar(id)
                .filter(Usuario::isActivo)
                .map(SesionCopias::crear)
                .orElse(null);
        if (usuario == null) {
            throw new ReglaNegocioException("Ese usuario no puede entrar a Copias");
        }
        request.getSession().setAttribute(ATRIBUTO_USUARIO_PRUEBA, usuario.getId());
        return usuario;
    }

    private UsuarioCopias delModoPrueba(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        Object id = sesion == null ? null : sesion.getAttribute(ATRIBUTO_USUARIO_PRUEBA);
        if (id instanceof Long elegido) {
            UsuarioCopias usuario = usuarios.buscar(elegido).filter(Usuario::isActivo).map(SesionCopias::crear).orElse(null);
            if (usuario != null) {
                return usuario;
            }
        }
        // Si no se eligió a nadie (o lo desactivaron): el primer trabajador activo
        return usuariosDePrueba().stream().filter(u -> !u.isAdmin()).findFirst().orElse(null);
    }

    private static UsuarioCopias crear(Usuario cuenta) {
        return crear(cuenta.getId(), cuenta.getNombre(), new ArrayList<>(cuenta.getRoles()));
    }
}
