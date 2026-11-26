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
 * Historial inalterable de la toca (RF 6.2).
 * URL: GET /segunda/tocas/historial?id=5
 * Vista: WEB-INF/jsp/segunda/tocas/historial.jsp
 *
 * TODO: solo SEGUNDA_MAGISTRADO y SEGUNDA_ADMIN, cuando exista el inicio de sesión.
 */
@WebServlet("/segunda/tocas/historial")
public class TocaHistorialServlet extends HttpServlet {

    private final TocaService tocaService = Segunda.tocaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (SegundaPermisos.esMagistrado(usuario)
                && !usuario.getId().equals(toca.get().getMagistradoUsuarioId())) {
            Vistas.noEncontrado(response);
            return;
        }

        request.setAttribute("toca", toca.get());
        request.setAttribute("usuario", usuario);
        request.setAttribute("historial", tocaService.historial(id));

        Vistas.mostrar(request, response, "segunda/tocas/historial");
    }
}


