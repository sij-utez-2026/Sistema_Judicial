package mx.edu.utez.sij.copias.controller.admin;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.copias.temporal.UsuarioServiceTemporal.Usuario;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * RF 6.1 Desactivar o reactivar a un trabajador del área (COPIAS_ADMIN).
 * URL: POST /copias/admin/personal/estado  con  id=5 y activo=true|false
 */
@WebServlet("/copias/admin/personal/estado")
public class TrabajadorEstadoServlet extends HttpServlet {

    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioCopias administrador = acceso.exigirRol(request, response, RolesCopias.ADMIN);
        if (administrador == null) {
            return;
        }
        Long id = Parametros.numero(request, "id");
        boolean activo = Parametros.booleano(request, "activo");
        try {
            Usuario usuario = Copias.personalService().cambiarEstado(id, activo);
            FlashMessage.exito(request, activo ? "Trabajador reactivado" : "Trabajador desactivado",
                    usuario.getNombre() + (activo ? " puede volver a entrar al sistema." : " ya no puede entrar al sistema."));
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
        }
        Vistas.redirigir(request, response, "/copias/admin/personal");
    }
}
