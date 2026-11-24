package mx.edu.utez.sij.peritajes.model;

import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

// Usuario con el que se trabaja en Peritajes: sale del usuario en sesión del núcleo (UsuarioSesion).
// - Usuarios de Peritajes (tabla user_Per): PERITAJES_ADMIN, PERITAJES_PERITO_ADMIN o PERITAJES_PERITO.
//   Los peritos son usuarios con rol de perito: su id de user_Per es también su id de perito.
// - Administrador general (SISTEMA_ADMIN): no está en user_Per (id null). Hace todo lo del administrador,
//   pero no atiende casos ni ve dictámenes (decisión del equipo).
public class UsuarioPeritajes {

    public static final String ADMIN = "PERITAJES_ADMIN";
    public static final String PERITO_ADMIN = "PERITAJES_PERITO_ADMIN";
    public static final String PERITO = "PERITAJES_PERITO";
    public static final String ADMIN_GENERAL = UsuarioSesion.ADMINISTRADOR_GENERAL;

    // Quien hace las acciones automáticas (asignación automática). En la bitácora se guarda con id_usuario NULL.
    public static final UsuarioPeritajes SISTEMA = new UsuarioPeritajes(null, "Sistema", null, "SISTEMA");

    private final Long id;            // id_usuario de user_Per (null = el sistema o el administrador general)
    private final String nombre;      // nombre completo
    private final String correo;
    private final String rolSistema;  // PERITAJES_ADMIN, PERITAJES_PERITO_ADMIN, PERITAJES_PERITO o SISTEMA_ADMIN

    public UsuarioPeritajes(Long id, String nombre, String correo, String rolSistema) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rolSistema = rolSistema;
    }

    // El usuario del núcleo como usuario de Peritajes; null si no puede entrar al módulo
    public static UsuarioPeritajes de(UsuarioSesion sesion) {
        if (sesion == null) {
            return null;
        }
        if (sesion.isAdministradorGeneral()) {
            return new UsuarioPeritajes(null, sesion.getNombre(), sesion.getCorreo(), ADMIN_GENERAL);
        }
        boolean dePeritajes = "user_Per".equals(sesion.getTabla())
                && (ADMIN.equals(sesion.getRol()) || esRolDePerito(sesion.getRol()));
        return dePeritajes ? new UsuarioPeritajes(sesion.getId(), sesion.getNombre(), sesion.getCorreo(), sesion.getRol()) : null;
    }

    // Texto del rol para mostrarlo en pantalla
    public static String textoRol(String rolSistema) {
        if (ADMIN.equals(rolSistema)) {
            return "Administrador";
        }
        if (PERITO_ADMIN.equals(rolSistema)) {
            return "Perito Administrador";
        }
        if (PERITO.equals(rolSistema)) {
            return "Perito Judicial";
        }
        if (ADMIN_GENERAL.equals(rolSistema)) {
            return "Administrador general";
        }
        return "Sistema";
    }

    // true si el texto es uno de los dos roles de perito (los que aparecen en el catálogo y atienden casos)
    public static boolean esRolDePerito(String rolSistema) {
        return PERITO.equals(rolSistema) || PERITO_ADMIN.equals(rolSistema);
    }

    // Catálogo, casos, reasignación, bitácora y panel (peritajes.md, sección 3)
    public boolean isAdministra() {
        return ADMIN.equals(rolSistema) || PERITO_ADMIN.equals(rolSistema) || ADMIN_GENERAL.equals(rolSistema);
    }

    // Mis peritajes y dictámenes (el administrador general no: no ve dictámenes)
    public boolean isAtiendeCasos() {
        return esRolDePerito(rolSistema);
    }

    public boolean isAdministradorGeneral() {
        return ADMIN_GENERAL.equals(rolSistema);
    }

    // El perito es el mismo usuario; null si el usuario no atiende casos
    public Long getPeritoId() {
        return isAtiendeCasos() ? id : null;
    }

    public String getRol() {
        return textoRol(rolSistema);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRolSistema() {
        return rolSistema;
    }
}
