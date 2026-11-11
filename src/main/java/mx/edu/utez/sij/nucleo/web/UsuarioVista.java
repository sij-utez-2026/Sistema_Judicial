package mx.edu.utez.sij.nucleo.web;

/**
 * Nombre y rol del usuario en sesión, como se muestran en el menú lateral.
 * Es una clase con getters (y no un record) porque en una JSP ${usuarioActual.iniciales}
 * solo funciona con métodos getX().
 */
public class UsuarioVista {

    private final String nombre;
    private final String rol;

    public UsuarioVista(String nombre, String rol) {
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    /** "Usuario de prueba" → "UP". */
    public String getIniciales() {
        StringBuilder iniciales = new StringBuilder();
        for (String palabra : nombre.trim().split("\\s+")) {
            if (!palabra.isEmpty() && iniciales.length() < 2) {
                iniciales.append(Character.toUpperCase(palabra.charAt(0)));
            }
        }
        return iniciales.toString();
    }
}
