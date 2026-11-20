package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * RF 3.1 Bandeja de solicitudes privadas en espera del visto bueno (COPIAS_PERSONAL).
 * URL: GET /copias/validacion
 */
@WebServlet("/copias/validacion")
public class ValidacionListarServlet extends HttpServlet {

    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.PERSONAL);
        if (usuario == null) {
            return;
        }
        request.setAttribute("pendientes", Copias.validacionService().pendientes());
        Vistas.mostrar(request, response, "copias/validacion/lista");
    }
}
