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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Recibe el retorno desde checkout.stripe.com, verifica el pago con Stripe y muestra el comprobante.
 */
@WebServlet("/copias/publico/pago/retorno")
public class PagoStripeRetornoServlet extends HttpServlet {

    private final PagoService pagoService = Copias.pagoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sessionId = Parametros.texto(request, "session_id");
        try {
            Pago pago = pagoService.confirmarPagoStripe(sessionId);
            FlashMessage.exito(request, "Pago registrado exitosamente",
                    "Folio de pago: " + pago.getFolioPago() + " · " + pago.getMontoFormateado());
            Vistas.redirigir(request, response,
                    "/copias/publico/pago/comprobante?folioPago="
                            + URLEncoder.encode(pago.getFolioPago(), StandardCharsets.UTF_8));
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            Vistas.redirigir(request, response, "/copias/publico/pago");
        }
    }
}
