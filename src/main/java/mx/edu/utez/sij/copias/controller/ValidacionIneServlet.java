package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ArchivoServiceTemporal.Archivo;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * La INE de una solicitud privada. Solo COPIAS_PERSONAL; nunca tiene una URL pública ni aparece en el comprobante.
 * URL: GET /copias/validacion/ine?id=5  (se muestra dentro de la página de validación)
 */
@WebServlet("/copias/validacion/ine")
public class ValidacionIneServlet extends HttpServlet {

    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.PERSONAL);
        if (usuario == null) {
            return;
        }
        SolicitudCopia solicitud = ValidacionDetalleServlet.privada(Parametros.numero(request, "id"));
        if (solicitud == null) {
            Vistas.noEncontrado(response);
            return;
        }
        Archivo archivo;
        try {
            archivo = Copias.validacionService().ine(solicitud.getId());
        } catch (ReglaNegocioException e) {
            Vistas.noEncontrado(response);
            return;
        }
        // El tipo se decide por la extensión ya validada, no por lo que mandó el navegador
        response.setContentType(tipo(archivo.nombreOriginal()));
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''"
                + URLEncoder.encode(archivo.nombreOriginal(), StandardCharsets.UTF_8).replace("+", "%20"));
        response.setContentLength(archivo.contenido().length);
        response.getOutputStream().write(archivo.contenido());
    }

    private String tipo(String nombre) {
        String minusculas = nombre.toLowerCase(Locale.ROOT);
        if (minusculas.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (minusculas.endsWith(".png")) {
            return "image/png";
        }
        return "image/jpeg";
    }
}
