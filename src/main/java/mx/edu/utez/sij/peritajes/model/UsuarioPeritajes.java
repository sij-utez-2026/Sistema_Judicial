package mx.edu.utez.sij.peritajes.model;

// Usuario con el que se trabaja en el módulo y su rol.
// TEMPORAL: mientras no hay inicio de sesión, se elige en "Ver como" (PeritajesAuth). Cuando exista el inicio
// de sesión del núcleo se usa SesionService y los permisos se revisan con el rol del usuario en sesión.
public class UsuarioPeritajes {

    public static final String ADMIN = "PERITAJES_ADMIN";
    public static final String PERITO_ADMIN = "PERITAJES_PERITO_ADMIN";
    public static final String PERITO = "PERITAJES_PERITO";

    // Quien hace las acciones automáticas (asignación automática, datos iniciales)
    public static final UsuarioPeritajes SISTEMA = new UsuarioPeritajes("sistema", "Sistema", "Sistema", "SISTEMA", null);

    private final String clave;
    private final String nombre;
    private final String rol;        // texto: "Administrador", "Perito Administrador", "Perito Judicial"
    private final String rolSistema; // PERITAJES_ADMIN, PERITAJES_PERITO_ADMIN o PERITAJES_PERITO
    private final Long peritoId;     // el perito del catálogo, si atiende casos

    public UsuarioPeritajes(String clave, String nombre, String rol, String rolSistema, Long peritoId) {
        this.clave = clave;
        this.nombre = nombre;
        this.rol = rol;
        this.rolSistema = rolSistema;
        this.peritoId = peritoId;
    }

    // Catálogo, casos, reasignación, bitácora y panel (peritajes.md, sección 3)
    public boolean isAdministra() {
        return ADMIN.equals(rolSistema) || PERITO_ADMIN.equals(rolSistema);
    }

    // Mis peritajes y dictámenes
    public boolean isAtiendeCasos() {
        return PERITO.equals(rolSistema) || PERITO_ADMIN.equals(rolSistema);
    }

    public String getClave() {
        return clave;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    public String getRolSistema() {
        return rolSistema;
    }

    public Long getPeritoId() {
        return peritoId;
    }
}
