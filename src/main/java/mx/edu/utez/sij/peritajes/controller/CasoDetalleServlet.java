package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.dao.HistorialAsignacionDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;

import java.io.IOException;

// Detalle del peritaje para administradores: fundamento de la asignación automática, historial y
// ventana de reasignación manual (RF 2.2 y 2.3).
// URL: GET /peritajes/casos/detalle?id=5
// Vista: WEB-INF/jsp/peritajes/casos/detalle.jsp
// Permiso: PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/casos/detalle")
public class CasoDetalleServlet extends HttpServlet {

    private final PeritajeDao dao = new PeritajeDao();
    private final HistorialAsignacionDao historialDao = new HistorialAsignacionDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PeritajesAuth.exigirAdministrador(request, response) == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        Peritaje peritaje = dao.getById(Parametros.numero(request, "id"));
        if (peritaje == null) {
            Vistas.noEncontrado(response);
            return;
        }

        request.setAttribute("peritaje", peritaje);
        request.setAttribute("historial", historialDao.listarPorPeritaje(peritaje.getId()));
        // Para la ventana de reasignación: elegibles sin el perito actual (RF 2.3)
        request.setAttribute("puedeReasignar", peritaje.getEstado().isActivo());
        request.setAttribute("candidatos", dao.candidatos(peritaje.getMateriaId(), peritaje.getPeritoId()));
        Vistas.mostrar(request, response, "peritajes/casos/detalle");
    }
}
