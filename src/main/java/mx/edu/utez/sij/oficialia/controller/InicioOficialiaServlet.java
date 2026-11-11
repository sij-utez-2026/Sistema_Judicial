package mx.edu.utez.sij.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Página de inicio del módulo (/oficialia). Cámbienla por el inicio según el rol que pide su DFR.
 * Vista: WEB-INF/jsp/oficialia/inicio.jsp
 *
 * TODO: revisar el rol cuando los integradores agreguen el inicio de sesión.
 */
@WebServlet("/oficialia")
public class InicioOficialiaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Vistas.mostrar(request, response, "oficialia/inicio");
    }
}
