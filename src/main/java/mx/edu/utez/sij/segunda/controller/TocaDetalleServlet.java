package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.segunda.service.TocaService;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.util.Optional;

/**
 * Detalle de la toca con el expediente completo (RF 6.1).
 * URL: GET /segunda/tocas/detalle?id=5
 * Vista: WEB-INF/jsp/segunda/tocas/detalle.jsp
 *
 * Un magistrado solo puede ver las tocas de su sala. Un admin puede ver todas.
 *
 * TODO: solo SEGUNDA_MAGISTRADO y SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/tocas/detalle")
public class TocaDetalleServlet extends HttpServlet {

    private final TocaService tocaService = Segunda.tocaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (!SegundaPermisos.esUsuarioSegunda(usuario)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Long id = Parametros.numero(request, "id");
        if (id == null) {
            Vistas.noEncontrado(response);
            return;
        }
        Optional<Toca> toca = tocaService.buscar(id);
        if (toca.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        if (SegundaPermisos.esMagistrado(usuario)
                && !usuario.getId().equals(toca.get().getMagistradoUsuarioId())) {
            Vistas.noEncontrado(response);
            return;
        }

        request.setAttribute("toca", toca.get());
        request.setAttribute("usuario", usuario);
        request.setAttribute("apelacionPdfDisponible", Segunda.documentoDao()
                .buscarUltimo(toca.get().getId(), "Escrito de apelación").isPresent());
        request.setAttribute("expediente", tocaService.expedienteDe(toca.get()));
        request.setAttribute("audiencias", tocaService.audienciasDe(toca.get()));
        request.setAttribute("resolucionPrimera", tocaService.resolucionPrimeraDe(toca.get()));
        request.setAttribute("dictamenes", tocaService.dictamenesDe(toca.get()));
        request.setAttribute("ponencias", Segunda.ponenciaService().deToca(id));
        request.setAttribute("ponenciaHoy", Segunda.ponenciaService().tienePonenciaHoy(id));
        request.setAttribute("resolucionVigente",
                Segunda.resolucionService().deToca(id).stream()
                        .filter(r -> "VIGENTE".equals(r.getEstado().name()))
                        .findFirst().orElse(null));

        Vistas.mostrar(request, response, "segunda/tocas/detalle");
    }
}
