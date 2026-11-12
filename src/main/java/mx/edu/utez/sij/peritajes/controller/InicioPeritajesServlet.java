package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

// Página de inicio del módulo (/peritajes): accesos según el rol y el selector "Ver como" (TEMPORAL).
// Vista: WEB-INF/jsp/peritajes/inicio.jsp
// TODO: el inicio según el rol que piden los bocetos (Panel de administración, Coordinación, Mi espacio)
// todavía no se hace.
@WebServlet("/peritajes")
public class InicioPeritajesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("usuarioPeritajes", PeritajesAuth.actual(request.getSession()));
        request.setAttribute("opcionesVerComo", PeritajesAuth.opciones());
        Vistas.mostrar(request, response, "peritajes/inicio");
    }
}
