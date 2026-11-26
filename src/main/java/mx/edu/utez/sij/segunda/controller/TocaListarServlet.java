package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.model.EstadoToca;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.segunda.service.TocaService;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;

/**
 * Listado de tocas del magistrado (RF 5.x) o de todas (RF 4.2, admin).
 * URL: GET /segunda/tocas?q=&estado=&salaId=&page=0&size=25
 * Vista: WEB-INF/jsp/segunda/tocas/lista.jsp
 *
 * Regla: si el usuario es SEGUNDA_MAGISTRADO, solo ve las tocas de su sala;
 * si es SEGUNDA_ADMIN, ve todas.
 *
 * TODO: solo SEGUNDA_MAGISTRADO y SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/tocas")
public class TocaListarServlet extends HttpServlet {

    private final TocaService tocaService = Segunda.tocaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion usuario = Segunda.usuarioActual(request);

        String q = Parametros.texto(request, "q");
        String estadoTexto = Parametros.texto(request, "estado");
        Long salaId = Parametros.numero(request, "salaId");
        int page = Parametros.entero(request, "page", 0);
        int size = Parametros.entero(request, "size", TocaService.TAMANO_PAGINA);

        EstadoToca estado = (estadoTexto == null || estadoTexto.isBlank())
                ? null : EstadoToca.desdeTexto(estadoTexto);

        // Magistrado: solo las suyas. Admin: todas (puede filtrar por sala si quiere).
        Long soloDeMagistrado = SegundaPermisos.esMagistrado(usuario) ? usuario.getId() : null;

        Pagina<Toca> pagina = tocaService.listar(q, estado, salaId, soloDeMagistrado, page, size);

        request.setAttribute("pagina", pagina);
        request.setAttribute("usuario", usuario);
        request.setAttribute("estados", EstadoToca.values());
        request.setAttribute("salas", Segunda.salaService().activas());
        request.setAttribute("tamanosPagina", TocaService.TAMANOS_PAGINA);
        request.setAttribute("totalRegistradas", tocaService.totalRegistradas());
        request.setAttribute("totalEnPonencia", tocaService.totalPorEstado(EstadoToca.EN_PONENCIA));
        request.setAttribute("totalResueltas", tocaService.totalPorEstado(EstadoToca.RESUELTA));
        request.setAttribute("totalAsignadas", tocaService.totalPorEstado(EstadoToca.ASIGNADA));
        request.setAttribute("q", q);
        request.setAttribute("estado", estadoTexto);
        request.setAttribute("salaId", salaId);
        request.setAttribute("size", pagina.getSize());
        request.setAttribute("hayFiltros", q != null || estado != null || salaId != null);

        Vistas.mostrar(request, response, "segunda/tocas/lista");
    }
}


