package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * RF 5.1 Entregas: copias pagadas que falta entregar (COPIAS_PERSONAL).
 * URL: GET /copias/entregas
 */
@WebServlet("/copias/entregas")
public class EntregaListarServlet extends HttpServlet {

    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!acceso.exigirRol(request, response, RolesCopias.PERSONAL)) {
            return;
        }
        request.setAttribute("pendientes", Copias.entregaService().pendientes());
        Vistas.mostrar(request, response, "copias/entregas/lista");
    }

    /** Solo dos destinos posibles después de avisar o entregar (no se redirige a una URL que llegue en la petición). */
    static String regresar(Long id, String volver) {
        return "detalle".equals(volver) ? "/copias/solicitudes/detalle?id=" + id : "/copias/entregas";
    }
}
