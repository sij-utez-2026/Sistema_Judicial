package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.model.Resolucion;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.util.Optional;

/**
 * Muestra el detalle de una resolución. Un magistrado solo consulta las de sus tocas;
 * el administrador puede consultar todas.
 * URL: GET /segunda/resoluciones/detalle?id=5
 * Vista: WEB-INF/jsp/segunda/resoluciones/detalle.jsp
 */
@WebServlet("/segunda/resoluciones/detalle")
public class ResolucionDetalleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long id = Parametros.numero(request, "id");
        if (id == null) {
            Vistas.noEncontrado(response);
            return;
        }

        Optional<Resolucion> resolucion = Segunda.resolucionService().buscar(id);
        if (resolucion.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        Optional<Toca> toca = Segunda.tocaService().buscar(resolucion.get().getTocaId());
        if (toca.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (!SegundaPermisos.esUsuarioSegunda(usuario)
                || (SegundaPermisos.esMagistrado(usuario)
                && !usuario.getId().equals(toca.get().getMagistradoUsuarioId()))) {
            Vistas.noEncontrado(response);
            return;
        }

        request.setAttribute("resolucion", resolucion.get());
        request.setAttribute("usuario", usuario);
        request.setAttribute("pdfDisponible", Segunda.documentoDao()
                .buscarUltimo(toca.get().getId(), "Resolución").isPresent());
        Vistas.mostrar(request, response, "segunda/resoluciones/detalle");
    }
}
