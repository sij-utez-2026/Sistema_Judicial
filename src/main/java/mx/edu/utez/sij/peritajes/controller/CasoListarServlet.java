package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.EstadoPeritaje;
import mx.edu.utez.sij.peritajes.model.dao.MateriaDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritoDao;

import java.io.IOException;

// RF 2.2 y 2.3 Solicitudes y peritajes (administradores): lista con filtros por folio o expediente,
// materia, estado y perito; 25 por página.
// URL: GET /peritajes/casos?q=&materia=CIV&estado=ASIGNADO&peritoId=&page=0
// Vista: WEB-INF/jsp/peritajes/casos/lista.jsp
// Permiso: PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN.
// Antes de listar recibe las solicitudes nuevas de Audiencias y las asigna (RF 2.1 y 2.2).
@WebServlet("/peritajes/casos")
public class CasoListarServlet extends HttpServlet {

    private final PeritajeDao dao = new PeritajeDao();
    private final PeritoDao peritoDao = new PeritoDao();
    private final MateriaDao materiaDao = new MateriaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PeritajesAuth.exigirAdministrador(request, response) == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        dao.recibirSolicitudes();
        String q = Parametros.texto(request, "q");
        String materia = Parametros.texto(request, "materia");
        EstadoPeritaje estado = EstadoPeritaje.deTexto(Parametros.texto(request, "estado"));
        Long peritoId = Parametros.numero(request, "peritoId");
        int page = Parametros.entero(request, "page", 0);

        request.setAttribute("pagina", dao.buscar(q, materia, estado, peritoId, page));
        // Llaves de texto ("ASIGNADO") para leerlas en la JSP: ${porEstado['ASIGNADO']}
        request.setAttribute("porEstado", dao.contarPorEstado());
        request.setAttribute("total", dao.contar());
        request.setAttribute("estados", EstadoPeritaje.values());
        request.setAttribute("materias", materiaDao.listarActivas());
        request.setAttribute("peritos", peritoDao.listarTodos());
        // Filtros actuales
        request.setAttribute("q", q);
        request.setAttribute("materia", materia);
        request.setAttribute("estado", estado == null ? "" : estado.name());
        request.setAttribute("peritoId", peritoId);
        request.setAttribute("hayFiltros", q != null || materia != null || estado != null || peritoId != null);

        Vistas.mostrar(request, response, "peritajes/casos/lista");
    }
}
