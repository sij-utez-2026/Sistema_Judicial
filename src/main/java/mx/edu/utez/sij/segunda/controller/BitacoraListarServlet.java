package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.dto.BitacoraItemDto;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/segunda/bitacora")
public class BitacoraListarServlet extends HttpServlet {
    private static final List<Integer> SIZES = List.of(10, 25, 50, 100);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (usuario == null || !SegundaPermisos.esAdmin(usuario)) { response.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        String q = Parametros.texto(request, "q");
        String accion = Parametros.texto(request, "accion");
        LocalDate desde = fecha(request.getParameter("desde"), "desde", request);
        LocalDate hasta = fecha(request.getParameter("hasta"), "hasta", request);
        int page = Math.max(0, Parametros.entero(request, "page", 0));
        int size = Parametros.entero(request, "size", 25);
        if (!SIZES.contains(size)) size = 25;
        boolean filtrosValidos = request.getAttribute("error") == null;
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            request.setAttribute("error", "La fecha inicial no puede ser posterior a la fecha final.");
            filtrosValidos = false;
        }
        Pagina<BitacoraItemDto> resultados = filtrosValidos
                ? Segunda.bitacoraDao().buscar(q, accion, desde, hasta, page, size)
                : new Pagina<>(List.of(), page, size, 0);
        request.setAttribute("pagina", resultados);
        request.setAttribute("acciones", Segunda.bitacoraDao().acciones());
        request.setAttribute("tamanosPagina", SIZES);
        request.setAttribute("q", q); request.setAttribute("accion", accion);
        request.setAttribute("desde", desde); request.setAttribute("hasta", hasta);
        request.setAttribute("size", size); request.setAttribute("usuario", usuario);
        request.setAttribute("hayFiltros", q != null || accion != null || desde != null || hasta != null);
        Vistas.mostrar(request, response, "segunda/bitacora/lista");
    }

    private LocalDate fecha(String value, String field, HttpServletRequest request) {
        if (value == null || value.isBlank()) return null;
        try { return LocalDate.parse(value); }
        catch (DateTimeParseException e) {
            request.setAttribute("error", "El filtro de fecha no es válido.");
            return null;
        }
    }
}
