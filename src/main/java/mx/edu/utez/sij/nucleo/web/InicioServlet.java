package mx.edu.utez.sij.nucleo.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Página de inicio del sistema: una tarjeta por módulo.
 * URL: / (el "" de @WebServlet es exactamente la raíz de la aplicación).
 * Vista: WEB-INF/jsp/nucleo/inicio.jsp
 */
@WebServlet("")
public class InicioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Vistas.mostrar(request, response, "nucleo/inicio");
    }
}
