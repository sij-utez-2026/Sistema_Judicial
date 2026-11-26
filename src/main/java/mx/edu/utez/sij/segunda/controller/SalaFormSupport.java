package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.http.HttpServletRequest;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.segunda.dto.SalaForm;
import mx.edu.utez.sij.segunda.service.SalaService;

/**
 * Lee el formulario de sala (registro y edición) y prepara los datos que necesitan sus pantallas.
 * Lo comparten SalaRegistrarServlet y SalaEditarServlet.
 */
final class SalaFormSupport {

    private SalaFormSupport() {
    }

    static SalaForm leer(HttpServletRequest request) {
        SalaForm form = new SalaForm();
        form.setNombre(Parametros.crudo(request, "nombre"));
        form.setDistritoId(Parametros.texto(request, "distritoId"));
        form.setDireccion(Parametros.crudo(request, "direccion"));
        form.setMagistradoUsuarioId(Parametros.numero(request, "magistradoUsuarioId"));
        return form;
    }

    static void datosDelFormulario(HttpServletRequest request, SalaService salaService) {
        request.setAttribute("distritos", salaService.distritosActivos());
    }
}


