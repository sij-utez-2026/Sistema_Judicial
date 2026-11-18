package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.model.Pago;
import mx.edu.utez.sij.copias.service.PagoService;
import mx.edu.utez.sij.copias.service.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Muestra el comprobante imprimible de un pago registrado (RF 4.1).
 */
@WebServlet("/copias/publico/pago/comprobante")
public class PagoComprobanteServlet extends HttpServlet {

    private final PagoService pagoService = Copias.pagoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String folioPago = Parametros.texto(request, "folioPago");
        String folio = Parametros.texto(request, "folio");

        if (folioPago == null && folio == null) {
            Vistas.noEncontrado(response);
            return;
        }

        try {
            Pago pago = pagoService.obtenerComprobante(folioPago, folio);
            request.setAttribute("pago", pago);
            pagoService.buscarSolicitudPorFolio(pago.getFolioSolicitud())
                    .ifPresent(s -> request.setAttribute("solicitud", s));
            Vistas.mostrar(request, response, "copias/publico/comprobante");
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            Vistas.redirigir(request, response, "/copias/publico/pago");
        }
    }
}
