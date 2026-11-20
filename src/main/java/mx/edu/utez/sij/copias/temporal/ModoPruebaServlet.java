package mx.edu.utez.sij.copias.temporal;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * TEMPORAL: "Modo de prueba" del menú. Elige con qué persona se usa Copias (un trabajador o el administrador
 * del Equipo 5, o un usuario de prueba de Oficialía: admin_op / usuario_op).
 * URL: POST /copias/modo-prueba  con  usuarioId=5
 * Se borra cuando exista el inicio de sesión del núcleo: la persona saldrá de la cuenta con la que se entra.
 */
@WebServlet("/copias/modo-prueba")
public class ModoPruebaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UsuarioCopias usuario = Copias.sesion().elegirUsuarioDePrueba(request, Parametros.numero(request, "usuarioId"));
            FlashMessage.exito(request, "Modo de prueba: " + usuario.getNombre(),
                    "Entra como " + usuario.getRolTexto() + (usuario.isDeOficialia() ? " desde Oficialía de Partes." : " de Copias."));
            Vistas.redirigir(request, response, usuario.isAdmin() ? "/copias/admin" : "/copias");
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            Vistas.redirigir(request, response, "/copias");
        }
    }
}
