package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Ruta retirada funcionalmente: toca_Seg.estado no incluye "Cancelada por acuerdo".
 * Se conserva el endpoint solo para rechazar clientes antiguos de forma explícita.
 */
@WebServlet("/segunda/tocas/cancelar-acuerdo")
public class TocaCancelarAcuerdoServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendError(HttpServletResponse.SC_CONFLICT,
                "El esquema actual de Segunda Instancia no permite cancelar una toca por acuerdo.");
    }
}
