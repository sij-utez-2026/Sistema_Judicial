package mx.edu.utez.sij.copias;

/**
 * Los dos roles del módulo (docs/modulos/copias-certificadas.md, sección 3).
 * Cuando el núcleo tenga inicio de sesión, FiltroNucleo revisará estos roles por ruta (ARQUITECTURA, sección 10).
 */
public final class RolesCopias {

    /** Trabajador o juzgado: captura, valida, cobra y entrega. */
    public static final String PERSONAL = "COPIAS_PERSONAL";

    /** Administrador del área: trabajadores y colores de Copias. */
    public static final String ADMIN = "COPIAS_ADMIN";

    private RolesCopias() {
    }
}
