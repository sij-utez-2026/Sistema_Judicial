package mx.edu.utez.sij.audiencias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Página de inicio del módulo (/audiencias). Cámbienla por el inicio según el rol que pide su DFR.
 * Vista: WEB-INF/jsp/audiencias/inicio.jsp
 *
 * TODO: revisar el rol cuando los integradores agreguen el inicio de sesión.
 */
@WebServlet("/audiencias")
public class InicioAudienciasServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Vistas.mostrar(request, response, "audiencias/inicio");
    }
}
