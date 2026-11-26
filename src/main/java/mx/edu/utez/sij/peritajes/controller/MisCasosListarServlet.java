package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.EstadoPeritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;

import java.io.IOException;

// RF 4.1 Consulta de mis peritajes: solo los del perito en sesión.
// URL: GET /peritajes/mis-casos?q=&estado=activos|todos|ASIGNADO|...&page=0
// Vista: WEB-INF/jsp/peritajes/mis-casos/lista.jsp
// Permiso: PERITAJES_PERITO y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/mis-casos")
public class MisCasosListarServlet extends HttpServlet {

    private final PeritajeDao dao = new PeritajeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirPerito(request, response);
        if (usuario == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        dao.recibirSolicitudes();
        String q = Parametros.texto(request, "q");
        String estado = Parametros.texto(request, "estado");
        if (estado == null) {
            estado = "activos";
        }
        int page = Parametros.entero(request, "page", 0);

        request.setAttribute("pagina", dao.misPeritajes(usuario, q, estado, page));
        request.setAttribute("asignados", dao.contarDelPerito(usuario, EstadoPeritaje.ASIGNADO));
        request.setAttribute("enProceso", dao.contarDelPerito(usuario, EstadoPeritaje.EN_PROCESO));
        request.setAttribute("entregados", dao.contarDelPerito(usuario, EstadoPeritaje.DICTAMEN_ENTREGADO));
        request.setAttribute("q", q);
        request.setAttribute("estado", estado);
        Vistas.mostrar(request, response, "peritajes/mis-casos/lista");
    }
}
