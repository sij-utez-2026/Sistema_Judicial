package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.http.HttpServletRequest;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.segunda.dto.MagistradoForm;
import mx.edu.utez.sij.segunda.service.MagistradoService;
import mx.edu.utez.sij.segunda.service.SalaService;

/**
 * Lee el formulario de magistrado (registro y edición) y prepara los datos que necesitan sus pantallas.
 */
final class MagistradoFormSupport {

    private MagistradoFormSupport() {
    }

    static MagistradoForm leer(HttpServletRequest request) {
        MagistradoForm form = new MagistradoForm();
        form.setNombre(Parametros.crudo(request, "nombre"));
        form.setApellidos(Parametros.crudo(request, "apellidos"));
        form.setCorreo(Parametros.crudo(request, "correo"));
        form.setSalaId(Parametros.numero(request, "salaId"));
        return form;
    }

    static void datosDelFormulario(HttpServletRequest request, SalaService salaService) {
        request.setAttribute("salas", salaService.activas());
    }

    static void datosDelFormulario(HttpServletRequest request, SalaService salaService,
                                   MagistradoService magistradoService) {
        datosDelFormulario(request, salaService);
        request.setAttribute("magistrados", magistradoService.activos());
    }
}


