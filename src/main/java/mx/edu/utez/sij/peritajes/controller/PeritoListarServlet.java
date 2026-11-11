package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.Peritajes;
import mx.edu.utez.sij.peritajes.dto.CargaPerito;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.service.PeritoService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RF 3.2 Catálogo de peritos, con filtros y paginación.
 * URL: GET /peritajes/peritos?q=&especialidad=&materiaId=&estado=activo|inactivo&page=0&size=25
 * Vista: WEB-INF/jsp/peritajes/peritos/lista.jsp
 *
 * TODO: solo PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/peritajes/peritos")
public class PeritoListarServlet extends HttpServlet {

    private final PeritoService peritoService = Peritajes.peritoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String q = Parametros.texto(request, "q");
        String especialidad = Parametros.texto(request, "especialidad");
        Long materiaId = Parametros.numero(request, "materiaId");
        String estado = Parametros.texto(request, "estado");
        int page = Parametros.entero(request, "page", 0);
        int size = Parametros.entero(request, "size", PeritoService.TAMANO_PAGINA);

        // estado: "activo", "inactivo" o vacío (todos)
        Boolean activo = null;
        if ("activo".equals(estado)) {
            activo = true;
        } else if ("inactivo".equals(estado)) {
            activo = false;
        }

        Pagina<Perito> pagina = peritoService.listar(q, especialidad, materiaId, activo, page, size);

        // Materias y carga de cada perito de la página
        Map<Long, String> materiasPorPerito = new LinkedHashMap<>();
        Map<Long, CargaPerito> cargaPorPerito = new LinkedHashMap<>();
        for (Perito perito : pagina.getContent()) {
            materiasPorPerito.put(perito.getId(), peritoService.nombresMaterias(perito));
            cargaPorPerito.put(perito.getId(), peritoService.carga(perito.getId()));
        }

        request.setAttribute("pagina", pagina);
        request.setAttribute("materiasPorPerito", materiasPorPerito);
        request.setAttribute("cargaPorPerito", cargaPorPerito);
        request.setAttribute("totalRegistrados", peritoService.totalRegistrados());
        request.setAttribute("totalActivos", peritoService.totalActivos());
        request.setAttribute("totalInactivos", peritoService.totalInactivos());
        request.setAttribute("totalesPorMateria", peritoService.totalesPorMateria());
        request.setAttribute("especialidades", peritoService.especialidadesRegistradas());
        request.setAttribute("tamanosPagina", PeritoService.TAMANOS_PAGINA);
        request.setAttribute("materias", peritoService.materiasActivas());
        // Filtros actuales, para mantenerlos en el formulario y en la paginación
        request.setAttribute("q", q);
        request.setAttribute("especialidad", especialidad);
        request.setAttribute("materiaId", materiaId);
        request.setAttribute("estado", estado);
        request.setAttribute("size", pagina.getSize());
        request.setAttribute("hayFiltros", q != null || especialidad != null || materiaId != null || activo != null);

        Vistas.mostrar(request, response, "peritajes/peritos/lista");
    }
}
