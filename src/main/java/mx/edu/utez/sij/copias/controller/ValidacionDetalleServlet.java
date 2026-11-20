package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.service.ValidacionService;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * RF 3.1 Visto bueno: datos del solicitante, su INE y las partes del expediente para compararlas (COPIAS_PERSONAL).
 * URL: GET /copias/validacion/detalle?id=5
 * Las públicas no pasan por validación (404).
 */
@WebServlet("/copias/validacion/detalle")
public class ValidacionDetalleServlet extends HttpServlet {

    private final ValidacionService validacionService = Copias.validacionService();
    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!acceso.exigirRol(request, response, RolesCopias.PERSONAL)) {
            return;
        }
        SolicitudCopia solicitud = privada(Parametros.numero(request, "id"));
        if (solicitud == null) {
            Vistas.noEncontrado(response);
            return;
        }
        request.setAttribute("solicitud", solicitud);
        request.setAttribute("expediente", validacionService.expediente(solicitud).orElse(null));
        request.setAttribute("coincidencias", validacionService.partesQueCoinciden(solicitud));
        request.setAttribute("enEspera", solicitud.getEstado() == EstadoSolicitud.EN_ESPERA_VALIDACION);
        request.setAttribute("motivoMaximo", ValidacionService.MOTIVO_MAXIMO);
        if (solicitud.getIneArchivoId() != null) {
            try {
                String nombre = validacionService.ine(solicitud.getId()).nombreOriginal();
                request.setAttribute("ineEsPdf", nombre.toLowerCase().endsWith(".pdf"));
            } catch (ReglaNegocioException e) {
                request.setAttribute("error", e.getMessage());
            }
        }
        Vistas.mostrar(request, response, "copias/validacion/detalle");
    }

    /** La solicitud si existe y es privada; si no, null. La usan también los servlets de aprobar, rechazar e INE. */
    static SolicitudCopia privada(Long id) {
        if (id == null) {
            return null;
        }
        return Copias.solicitudService().buscar(id).filter(SolicitudCopia::isPrivada).orElse(null);
    }
}
