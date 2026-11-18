package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.service.PagoService;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;
import java.util.Optional;

/**
 * Portal público para consultar el folio y mostrar el resumen previo al pago con Stripe Checkout (RF 4.1).
 */
@WebServlet("/copias/publico/pago")
public class PagoConsultarServlet extends HttpServlet {

    private final PagoService pagoService = Copias.pagoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String folioCrudo = Parametros.crudo(request, "folio");
        String folio = Parametros.texto(request, "folio");
        String cancelado = Parametros.texto(request, "cancelado");

        if ("1".equals(cancelado)) {
            request.setAttribute("avisoCancelado", true);
        }

        request.setAttribute("folio", folio);
        request.setAttribute("stripeConfigurado", Copias.stripeConfig().estaConfigurada());
        request.setAttribute("solicitudesDemo", pagoService.listarSolicitudes());

        if (folioCrudo != null && folio == null) {
            request.setAttribute("error", "Ingrese un folio de solicitud válido.");
        } else if (folio != null) {
            Optional<SolicitudCopia> solicitudOpt = pagoService.buscarSolicitudPorFolio(folio);
            if (solicitudOpt.isEmpty()) {
                request.setAttribute("error", "No se encontraron registros con ese folio.");
            } else {
                SolicitudCopia solicitud = solicitudOpt.get();
                request.setAttribute("solicitud", solicitud);
                request.setAttribute("motivoNoPagable", pagoService.motivoNoPagable(solicitud));
                pagoService.buscarPagoPorSolicitud(solicitud.getId())
                        .ifPresent(pago -> request.setAttribute("pagoExistente", pago));
            }
        }

        Vistas.mostrar(request, response, "copias/publico/pago");
    }
}
