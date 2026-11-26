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
 * RF 2.1 Consulta de distritos judiciales (solo lectura).
 * URL: GET /segunda/distritos
 * Vista: WEB-INF/jsp/segunda/distritos/lista.jsp
 *
 * Los distritos son catálogo de Oficialía (RN 3): aquí NO se guardan ni se editan.
 * Se leen del catálogo (hoy CatalogoTemporalSegunda; después CatalogoJudicialApi).
 *
 * TODO: solo SEGUNDA_ADMIN y SEGUNDA_MAGISTRADO, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/distritos")
public class DistritoListarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAdmin(request, response)) return;
        request.setAttribute("distritos", Segunda.catalogo().distritosActivos());
        Vistas.mostrar(request, response, "segunda/distritos/lista");
    }
}


