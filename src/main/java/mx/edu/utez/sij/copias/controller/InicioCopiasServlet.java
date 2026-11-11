package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Página de inicio del módulo (/copias). Cámbienla por el inicio según el rol que pide su DFR.
 * Vista: WEB-INF/jsp/copias/inicio.jsp
 *
 * TODO: revisar el rol cuando los integradores agreguen el inicio de sesión.
 */
@WebServlet("/copias")
public class InicioCopiasServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Vistas.mostrar(request, response, "copias/inicio");
    }
}
