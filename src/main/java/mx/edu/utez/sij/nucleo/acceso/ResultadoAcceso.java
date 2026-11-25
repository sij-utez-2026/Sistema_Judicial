package mx.edu.utez.sij.nucleo.acceso;

/** Resultado de iniciar sesión: el usuario si entró, o el mensaje de error. (INTEGRADORES) */
public class ResultadoAcceso {

    private final UsuarioSesion usuario;
    private final String error;

    private ResultadoAcceso(UsuarioSesion usuario, String error) {
        this.usuario = usuario;
        this.error = error;
    }

    public static ResultadoAcceso exito(UsuarioSesion usuario) {
        return new ResultadoAcceso(usuario, null);
    }

    public static ResultadoAcceso error(String mensaje) {
        return new ResultadoAcceso(null, mensaje);
    }

    public UsuarioSesion getUsuario() {
        return usuario;
    }

    public String getError() {
        return error;
    }
}
