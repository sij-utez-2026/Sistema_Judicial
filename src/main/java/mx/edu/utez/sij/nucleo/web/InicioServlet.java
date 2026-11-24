package mx.edu.utez.sij.nucleo.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.acceso.Acceso;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;

/**
 * Página de inicio del sistema. URL: / (el "" de @WebServlet es exactamente la raíz de la aplicación).
 * - Administrador general (SISTEMA_ADMIN): una tarjeta por módulo. Vista: WEB-INF/jsp/nucleo/inicio.jsp
 * - Los demás roles: van directo al inicio de su módulo (no ven los otros).
 * Sin sesión, FiltroNucleo ya mandó al login.
 */
@WebServlet("")
public class InicioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioSesion usuario = Acceso.usuario(request);
        if (usuario != null && !usuario.isAdministradorGeneral()) {
            ModuloSistema modulo = usuario.getModulo();
            if (modulo != null) {
                Vistas.redirigir(request, response, "/" + modulo.getPrefijo());
                return;
            }
            // Su rol no empieza con el nombre de un módulo: no tiene a dónde entrar
            request.setAttribute("error", "Su rol (" + usuario.getRol() + ") no tiene un módulo asignado."
                    + " Comuníquese con el administrador del sistema");
        }
        Vistas.mostrar(request, response, "nucleo/inicio");
    }
}
