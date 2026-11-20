package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;

import java.io.IOException;

/**
 * Datos comunes para todas las pantallas de Copias (antes era un @ControllerAdvice).
 * - coloresCopias:    los colores del área que escribe tags/copias/estilos.tag.
 * - usuarioCopias:    quién usa Copias (SesionCopias); menu.jsp muestra su nombre y su rol.
 * - esAdminCopias:    qué menú mostrar (trabajador o administrador).
 * - inicioOficialia:  a dónde lleva "Volver a Oficialía" (solo se muestra si entró con un rol de Oficialía).
 * - usuariosDePrueba: TEMPORAL, las personas del selector "Modo de prueba".
 * Solo pone datos para las vistas: no revisa permisos ni CSRF (eso lo hace FiltroNucleo) y no toca
 * atributos de sesión de otros módulos.
 */
@WebFilter("/copias/*")
public class FiltroCopias extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws IOException, ServletException {
        UsuarioCopias usuario = Copias.sesion().usuario(request);
        request.setAttribute("coloresCopias", Copias.coloresService().actuales());
        request.setAttribute("usuarioCopias", usuario);
        request.setAttribute("esAdminCopias", usuario != null && usuario.isAdmin());
        request.setAttribute("inicioOficialia", RolesCopias.INICIO_OFICIALIA);
        request.setAttribute("usuariosDePrueba", Copias.sesion().usuariosDePrueba());
        cadena.doFilter(request, response);
    }
}
