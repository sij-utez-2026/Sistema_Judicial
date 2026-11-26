package mx.edu.utez.sij.nucleo.acceso;

import mx.edu.utez.sij.nucleo.web.ModuloSistema;

import java.io.Serializable;

/**
 * Usuario que inició sesión en el sistema (INTEGRADORES). Se guarda en la sesión como "usuarioSesion".
 *
 * Cada usuario vive en la tabla user_* de su módulo (user_Ofc, user_Aud, user_Per o user_Seg) y tiene un solo rol.
 * - SISTEMA_ADMIN (administrador general): entra a los cinco módulos.
 * - Los demás roles empiezan con el nombre de su módulo (PERITAJES_PERITO, COPIAS_JUEZ...) y solo entran a ese.
 *
 * Los módulos lo leen con Acceso.usuario(request).
 */
public class UsuarioSesion implements Serializable {

    public static final String ADMINISTRADOR_GENERAL = "SISTEMA_ADMIN";

    private final Long id;          // id_usuario en su tabla
    private final String tabla;     // user_Ofc, user_Aud, user_Per o user_Seg
    private final String nombre;    // nombre completo
    private final String correo;
    private final String rol;       // texto de la tabla Rol

    public UsuarioSesion(Long id, String tabla, String nombre, String correo, String rol) {
        this.id = id;
        this.tabla = tabla;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public boolean isAdministradorGeneral() {
        return ADMINISTRADOR_GENERAL.equals(rol);
    }

    // El módulo de su rol (PERITAJES_PERITO → PERITAJES); null si el rol no empieza con un módulo
    public ModuloSistema getModulo() {
        for (ModuloSistema modulo : ModuloSistema.values()) {
            if (rol != null && rol.startsWith(modulo.name() + "_")) {
                return modulo;
            }
        }
        return null;
    }

    // El administrador general entra a todos; los demás, solo al módulo de su rol
    public boolean puedeEntrar(ModuloSistema modulo) {
        return isAdministradorGeneral() || modulo == getModulo();
    }

    // Texto para el menú lateral. Cada módulo puede mostrar uno más preciso ("Perito Administrador")
    public String getRolTexto() {
        if (isAdministradorGeneral()) {
            return "Administrador general";
        }
        ModuloSistema modulo = getModulo();
        return modulo == null ? rol : modulo.getNombre();
    }

    public Long getId() {
        return id;
    }

    public String getTabla() {
        return tabla;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() {
        return rol;
    }
}
