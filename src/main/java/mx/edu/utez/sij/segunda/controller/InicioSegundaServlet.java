package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;

import java.io.IOException;

/**
 * Panel principal de Segunda Instancia (/segunda).
 * La vista muestra opciones distintas para SEGUNDA_ADMIN y SEGUNDA_MAGISTRADO.
 */
@WebServlet("/segunda")
public class InicioSegundaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Segunda.usuarioActual(request);
        Vistas.mostrar(request, response, "segunda/inicio");
    }
}
