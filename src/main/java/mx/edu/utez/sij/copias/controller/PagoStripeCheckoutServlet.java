package mx.edu.utez.sij.copias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.service.PagoService;
import mx.edu.utez.sij.copias.service.ReglaNegocioException;
import mx.edu.utez.sij.copias.stripe.StripeCheckoutSession;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Crea la sesión de Stripe Checkout en MXN y redirige al usuario a checkout.stripe.com (RF 4.1).
 */
@WebServlet("/copias/publico/pago/stripe")
public class PagoStripeCheckoutServlet extends HttpServlet {

    private final PagoService pagoService = Copias.pagoService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String folio = Parametros.texto(request, "folio");
        String urlBase = resolverUrlBase(request);

        try {
            StripeCheckoutSession sesion = pagoService.iniciarCheckoutStripe(folio, urlBase);
            response.sendRedirect(sesion.url());
            return;
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
            String destino = "/copias/publico/pago";
            if (folio != null) {
                destino += "?folio=" + URLEncoder.encode(folio, StandardCharsets.UTF_8);
            }
            Vistas.redirigir(request, response, destino);
        }
    }

    private static String resolverUrlBase(HttpServletRequest request) {
        String configurada = Copias.stripeConfig().obtenerAppBaseUrl();
        if (configurada != null) {
            return configurada;
        }
        String esquema = request.getScheme();
        String servidor = request.getServerName();
        int puerto = request.getServerPort();
        String contexto = request.getContextPath();

        boolean puertoEstandar = ("http".equalsIgnoreCase(esquema) && puerto == 80)
                || ("https".equalsIgnoreCase(esquema) && puerto == 443);
        String puertoTexto = puertoEstandar ? "" : ":" + puerto;
        return esquema + "://" + servidor + puertoTexto + contexto;
    }
}
