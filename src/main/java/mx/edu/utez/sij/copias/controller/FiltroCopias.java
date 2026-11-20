package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;

import java.io.IOException;

/**
 * Datos comunes para todas las pantallas de Copias (antes era un @ControllerAdvice).
 * - coloresCopias: los colores del área que escribe tags/copias/estilos.tag.
 * - esAdminCopias: qué menú mostrar (trabajador o administrador).
 * Solo pone datos para las vistas: no revisa permisos ni CSRF (eso lo hace FiltroNucleo).
 */
@WebFilter("/copias/*")
public class FiltroCopias extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws IOException, ServletException {
        request.setAttribute("coloresCopias", Copias.coloresService().actuales());
        request.setAttribute("esAdminCopias", Copias.sesion().esAdmin(request));
        cadena.doFilter(request, response);
    }
}
