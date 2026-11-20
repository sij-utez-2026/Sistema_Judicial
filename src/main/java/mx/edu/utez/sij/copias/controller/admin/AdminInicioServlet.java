package mx.edu.utez.sij.copias.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.service.ColoresCopiasService;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.UsuarioServiceTemporal.Usuario;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;
import java.util.List;

/**
 * Inicio del administrador del área (COPIAS_ADMIN): trabajadores y colores.
 * URL: GET /copias/admin · Vista: WEB-INF/jsp/copias/admin/inicio.jsp
 */
@WebServlet("/copias/admin")
public class AdminInicioServlet extends HttpServlet {

    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!acceso.exigirRol(request, response, RolesCopias.ADMIN)) {
            return;
        }
        List<Usuario> trabajadores = Copias.personalService().trabajadores();
        long activos = trabajadores.stream().filter(Usuario::isActivo).count();
        ColoresCopiasService colores = Copias.coloresService();
        request.setAttribute("totalTrabajadores", trabajadores.size());
        request.setAttribute("trabajadoresActivos", activos);
        request.setAttribute("trabajadoresInactivos", trabajadores.size() - activos);
        request.setAttribute("coloresPersonalizados", !colores.actuales().equals(colores.iniciales()));
        Vistas.mostrar(request, response, "copias/admin/inicio");
    }
}
