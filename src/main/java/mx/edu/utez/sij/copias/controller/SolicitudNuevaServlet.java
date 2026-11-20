package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.dto.ArchivoSubido;
import mx.edu.utez.sij.copias.dto.SolicitudForm;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.service.SolicitudCopiaService;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ExpedienteApi.ExpedienteDto;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;
import java.util.Map;

/**
 * RF 2.1 y 2.2: el trabajador captura la solicitud con los datos que le da el solicitante (COPIAS_PERSONAL).
 * URL: GET  /copias/solicitudes/nueva                  paso 1: buscar el expediente
 *      GET  /copias/solicitudes/nueva?expediente=...   paso 2: datos del solicitante (y la INE si es privado)
 *      POST /copias/solicitudes/nueva                  guardar (multipart, por la INE)
 * La INE puede ser de hasta 5 MB; si se pasa, FiltroNucleo muestra la página 413.
 */
@WebServlet("/copias/solicitudes/nueva")
@MultipartConfig(maxFileSize = SolicitudCopiaService.TAMANO_MAXIMO_INE_MB * 1024L * 1024L)
public class SolicitudNuevaServlet extends HttpServlet {

    private final SolicitudCopiaService solicitudService = Copias.solicitudService();
    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!acceso.exigirRol(request, response, RolesCopias.PERSONAL)) {
            return;
        }
        String expediente = Parametros.texto(request, "expediente");
        SolicitudForm form = new SolicitudForm();
        form.setNumeroExpediente(expediente);
        request.setAttribute("solicitudForm", form);
        if (expediente != null) {
            try {
                request.setAttribute("expediente", solicitudService.buscarExpediente(expediente));
            } catch (ReglaNegocioException e) {
                request.setAttribute("error", e.getMessage());
            }
        }
        mostrar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!acceso.exigirRol(request, response, RolesCopias.PERSONAL)) {
            return;
        }
        SolicitudForm form = new SolicitudForm();
        form.setNumeroExpediente(Parametros.texto(request, "numeroExpediente"));
        form.setSolicitanteNombre(Parametros.texto(request, "solicitanteNombre"));
        form.setSolicitanteRfc(Parametros.texto(request, "solicitanteRfc"));
        form.setSolicitanteCorreo(Parametros.texto(request, "solicitanteCorreo"));
        request.setAttribute("solicitudForm", form);

        ExpedienteDto expediente;
        try {
            expediente = solicitudService.buscarExpediente(form.getNumeroExpediente());
        } catch (ReglaNegocioException e) {
            request.setAttribute("error", e.getMessage());
            mostrar(request, response);
            return;
        }
        request.setAttribute("expediente", expediente);

        Map<String, String> errores = Validador.validar(form);
        if (!errores.isEmpty()) {
            request.setAttribute("errores", errores);
            mostrar(request, response);
            return;
        }

        try {
            SolicitudCopia solicitud = solicitudService.registrar(form, ine(request));
            FlashMessage.exito(request, "Solicitud registrada", solicitud.getFolio() + (solicitud.isPrivada()
                    ? " · Debe esperar la aprobación antes de pagar."
                    : " · Expediente público: lista para el pago."));
            Vistas.redirigir(request, response, "/copias/solicitudes/detalle?id=" + solicitud.getId());
        } catch (ReglaNegocioException e) {
            request.setAttribute("error", e.getMessage());
            mostrar(request, response);
        }
    }

    // La INE del formulario (null si no viene)
    private ArchivoSubido ine(HttpServletRequest request) throws IOException, ServletException {
        Part parte = request.getPart("ine");
        if (parte == null || parte.getSize() == 0) {
            return null;
        }
        return new ArchivoSubido(parte.getSubmittedFileName(), parte.getSize(), parte.getInputStream());
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("tiposIne", ".pdf,.jpg,.jpeg,.png");
        request.setAttribute("maxMbIne", SolicitudCopiaService.TAMANO_MAXIMO_INE_MB);
        Vistas.mostrar(request, response, "copias/solicitudes/nueva");
    }
}
