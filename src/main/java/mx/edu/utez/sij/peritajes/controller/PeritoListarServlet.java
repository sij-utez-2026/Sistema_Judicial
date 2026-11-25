package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.dao.MateriaDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritoDao;

import java.io.IOException;

// RF 3.2 Catálogo de peritos, con filtros y paginación.
// URL: GET /peritajes/peritos?q=&especialidad=&materia=CIV&estado=activo|inactivo&page=0&size=25
// Vista: WEB-INF/jsp/peritajes/peritos/lista.jsp
// Permiso: PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/peritos")
public class PeritoListarServlet extends HttpServlet {

    private final PeritoDao dao = new PeritoDao();
    private final MateriaDao materiaDao = new MateriaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PeritajesAuth.exigirAdministrador(request, response) == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        String q = Parametros.texto(request, "q");
        String especialidad = Parametros.texto(request, "especialidad");
        Long materia = Parametros.numero(request, "materia");   // id_materia
        String estado = Parametros.texto(request, "estado");
        int page = Parametros.entero(request, "page", 0);
        int size = Parametros.entero(request, "size", PeritoDao.TAMANO_PAGINA);

        // estado: "activo", "inactivo" o vacío (todos)
        Boolean activo = null;
        if ("activo".equals(estado)) {
            activo = true;
        } else if ("inactivo".equals(estado)) {
            activo = false;
        }

        // Cada perito ya trae sus materias y su carga activa (materiasTexto, cargaAsignados, cargaEnProceso)
        Pagina<Perito> pagina = dao.buscar(q, especialidad, materia, activo, page, size);

        request.setAttribute("pagina", pagina);
        request.setAttribute("totalRegistrados", dao.contar());
        request.setAttribute("totalActivos", dao.contarPorEstado(true));
        request.setAttribute("totalInactivos", dao.contarPorEstado(false));
        request.setAttribute("totalesPorMateria", dao.totalesPorMateria());
        request.setAttribute("especialidades", dao.especialidadesRegistradas());
        request.setAttribute("tamanosPagina", PeritoDao.TAMANOS_PAGINA);
        request.setAttribute("materias", materiaDao.listarActivas());
        // Filtros actuales, para mantenerlos en el formulario y en la paginación
        request.setAttribute("q", q);
        request.setAttribute("especialidad", especialidad);
        request.setAttribute("materia", materia);
        request.setAttribute("estado", estado);
        request.setAttribute("size", pagina.getSize());
        request.setAttribute("hayFiltros", q != null || especialidad != null || materia != null || activo != null);

        Vistas.mostrar(request, response, "peritajes/peritos/lista");
    }
}
