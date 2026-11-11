package mx.edu.utez.sij.nucleo.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Muestra una JSP o redirige a otra URL. Lo usan todos los servlets.
 *
 * <pre>
 * Vistas.mostrar(request, response, "peritajes/peritos/lista");
 *     → src/main/webapp/WEB-INF/jsp/peritajes/peritos/lista.jsp (la URL del navegador no cambia)
 *
 * Vistas.redirigir(request, response, "/peritajes/peritos");
 *     → el navegador pide /sij/peritajes/peritos (se usa después de un POST exitoso: patrón PRG)
 * </pre>
 */
public final class Vistas {

    private static final String CARPETA_JSP = "/WEB-INF/jsp/";

    private Vistas() {
    }

    /** Hace forward a WEB-INF/jsp/{vista}.jsp. Los datos se pasan antes con request.setAttribute(...). */
    public static void mostrar(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        request.getRequestDispatcher(CARPETA_JSP + vista + ".jsp").forward(request, response);
    }

    /** Redirige a una URL del sistema. La ruta empieza con "/" y no lleva el contexto (/sij): se agrega solo. */
    public static void redirigir(HttpServletRequest request, HttpServletResponse response, String ruta)
            throws IOException {
        response.sendRedirect(request.getContextPath() + ruta);
    }

    /** Responde 404 con la página de error común (WEB-INF/jsp/error/404.jsp, configurada en web.xml). */
    public static void noEncontrado(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
