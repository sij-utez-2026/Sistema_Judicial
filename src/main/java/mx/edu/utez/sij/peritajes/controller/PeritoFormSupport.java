package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.http.HttpServletRequest;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.peritajes.dto.PeritoForm;
import mx.edu.utez.sij.peritajes.service.PeritoService;

import java.util.ArrayList;
import java.util.List;

/**
 * Lee el formulario de perito (registro y edición) y prepara los datos que necesitan sus pantallas.
 * Lo comparten PeritoRegistrarServlet y PeritoEditarServlet.
 */
final class PeritoFormSupport {

    private PeritoFormSupport() {
    }

    /** Arma el PeritoForm con lo que llegó del navegador, tal cual (las validaciones van después). */
    static PeritoForm leer(HttpServletRequest request) {
        PeritoForm form = new PeritoForm();
        form.setNumeroRegistro(Parametros.crudo(request, "numeroRegistro"));
        form.setNombre(Parametros.crudo(request, "nombre"));
        form.setApellidoPaterno(Parametros.crudo(request, "apellidoPaterno"));
        form.setApellidoMaterno(Parametros.crudo(request, "apellidoMaterno"));
        form.setCorreo(Parametros.crudo(request, "correo"));
        form.setTelefono(Parametros.crudo(request, "telefono"));
        // especialidades[0], especialidades[1], ... (peritos.js agrega y quita renglones)
        List<String> especialidades = Parametros.lista(request, "especialidades");
        form.setEspecialidades(especialidades.isEmpty() ? new ArrayList<>(List.of("")) : especialidades);
        form.setMateriasIds(Parametros.numeros(request, "materiasIds"));
        return form;
    }

    /** Datos que el formulario necesita además del PeritoForm. */
    static void datosDelFormulario(HttpServletRequest request, PeritoService peritoService) {
        request.setAttribute("materias", peritoService.materiasActivas());
    }
}
