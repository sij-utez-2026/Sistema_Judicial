package mx.edu.utez.sij.copias.sesion;

import mx.edu.utez.sij.copias.RolesCopias;

/**
 * Quién está usando Copias en esta petición: su id (core_usuario), nombre y rol dentro de Copias.
 * Lo arma SesionCopias. Clase con getters para poder leerla desde las JSP.
 */
public class UsuarioCopias {

    private final Long id;
    private final String nombre;
    private final String rol;              // COPIAS_PERSONAL o COPIAS_ADMIN
    private final boolean deOficialia;     // entra con un rol de Oficialía (admin_op / usuario_op)

    public UsuarioCopias(Long id, String nombre, String rol, boolean deOficialia) {
        this.id = id;
        this.nombre = nombre;
        this.rol = rol;
        this.deOficialia = deOficialia;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    public boolean isAdmin() {
        return RolesCopias.ADMIN.equals(rol);
    }

    public boolean isDeOficialia() {
        return deOficialia;
    }

    /** "Trabajador" o "Administrador". */
    public String getRolTexto() {
        return RolesCopias.texto(rol);
    }
}
