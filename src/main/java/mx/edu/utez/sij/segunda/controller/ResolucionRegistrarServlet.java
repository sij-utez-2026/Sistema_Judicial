package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.dto.ResolucionForm;
import mx.edu.utez.sij.segunda.model.Resolucion;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.segunda.service.ResolucionService;
import mx.edu.utez.sij.segunda.service.TocaService;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Registra una resolución (RF 7.1).
 * URL: GET/POST /segunda/tocas/resolucion?id=5
 * Vista: WEB-INF/jsp/segunda/tocas/resolucion.jsp
 */
@WebServlet("/segunda/tocas/resolucion")
public class ResolucionRegistrarServlet extends HttpServlet {

    private final TocaService tocaService = Segunda.tocaService();
    private final ResolucionService resolucionService = Segunda.resolucionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Optional<Toca> toca = buscarTocaPermitida(request, response);
        if (toca.isEmpty()) return;

        request.setAttribute("toca", toca.get());
        request.setAttribute("resolucionForm", new ResolucionForm());
        Vistas.mostrar(request, response, "segunda/tocas/resolucion");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Optional<Toca> toca = buscarTocaPermitida(request, response);
        if (toca.isEmpty()) return;

        ResolucionForm form = new ResolucionForm();
        form.setSentido(Parametros.texto(request, "sentido"));
        form.setComentarios(Parametros.crudo(request, "comentarios"));
        Map<String, String> errores = Validador.validar(form);

        if (errores.isEmpty()) {
            try {
                Resolucion resolucion = resolucionService.registrar(toca.get().getId(), form);
                UsuarioSesion usuario = Segunda.usuarioActual(request);
                Segunda.registrarBitacora(request, usuario, "resolucion_Seg", resolucion.getId(),
                        "Resolución registrada",
                        "Toca " + toca.get().getFolioToca() + " · sentido " + resolucion.getSentido().getEtiqueta(), "green");
                FlashMessage.exito(request, "Resolución registrada",
                        "Sentido: " + resolucion.getSentido().getEtiqueta());
                Vistas.redirigir(request, response, "/segunda/tocas/detalle?id=" + toca.get().getId());
                return;
            } catch (ReglaNegocioException | IllegalArgumentException e) {
                request.setAttribute("error", e.getMessage());
            }
        }

        request.setAttribute("errores", errores);
        request.setAttribute("toca", toca.get());
        request.setAttribute("resolucionForm", form);
        Vistas.mostrar(request, response, "segunda/tocas/resolucion");
    }

    private Optional<Toca> buscarTocaPermitida(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long id = Parametros.numero(request, "id");
        if (id == null) {
            Vistas.noEncontrado(response);
            return Optional.empty();
        }

        Optional<Toca> toca = tocaService.buscar(id);
        if (toca.isEmpty()) {
            Vistas.noEncontrado(response);
            return Optional.empty();
        }

        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (!SegundaPermisos.esMagistrado(usuario)
                || !usuario.getId().equals(toca.get().getMagistradoUsuarioId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return Optional.empty();
        }
        return toca;
    }
}
