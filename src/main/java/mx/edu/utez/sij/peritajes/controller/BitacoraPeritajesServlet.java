package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.dao.BitacoraDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// RF 5.2 Bitácora de auditoría del módulo: solo consulta, del más reciente al más antiguo,
// con filtros por rango de fechas, usuario, tipo de evento y referencia.
// URL: GET /peritajes/bitacora?desde=2026-11-01&hasta=2026-11-30&usuario=&evento=&q=&page=0
// Vista: WEB-INF/jsp/peritajes/bitacora.jsp
// Permiso: PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/bitacora")
public class BitacoraPeritajesServlet extends HttpServlet {

    private final BitacoraDao dao = new BitacoraDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PeritajesAuth.exigirAdministrador(request, response) == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        LocalDate desde = fecha(request, "desde");
        LocalDate hasta = fecha(request, "hasta");
        String usuario = Parametros.texto(request, "usuario");
        String evento = Parametros.texto(request, "evento");
        String q = Parametros.texto(request, "q");
        int page = Parametros.entero(request, "page", 0);

        request.setAttribute("pagina", dao.buscar(desde, hasta, usuario, evento, q, page, PeritajeDao.TAMANO_PAGINA));
        request.setAttribute("usuarios", dao.usuarios());
        request.setAttribute("eventos", BitacoraDao.EVENTOS);
        request.setAttribute("desde", desde);
        request.setAttribute("hasta", hasta);
        request.setAttribute("usuario", usuario);
        request.setAttribute("evento", evento);
        request.setAttribute("q", q);
        request.setAttribute("hayFiltros", desde != null || hasta != null || usuario != null || evento != null || q != null);
        Vistas.mostrar(request, response, "peritajes/bitacora");
    }

    // Fecha del campo <input type="date"> (2026-11-30); null si falta o no es válida
    private LocalDate fecha(HttpServletRequest request, String nombre) {
        String texto = Parametros.texto(request, nombre);
        if (texto == null) {
            return null;
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
