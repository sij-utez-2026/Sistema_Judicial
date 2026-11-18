package mx.edu.utez.sij.copias.stripe;

import com.sun.net.httpserver.HttpServer;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.model.TipoDemanda;
import mx.edu.utez.sij.copias.service.ReglaNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StripeCheckoutClientTest {

    private HttpServer servidor;
    private String baseUrl;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.start();
        baseUrl = "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void detenerServidor() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    void creaSesionCheckoutEnMxnConAutorizacionBearer() {
        AtomicReference<String> authRecibida = new AtomicReference<>();
        AtomicReference<String> cuerpoRecibido = new AtomicReference<>();

        servidor.createContext("/v1/checkout/sessions", exchange -> {
            authRecibida.set(exchange.getRequestHeaders().getFirst("Authorization"));
            cuerpoRecibido.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] respuesta = """
                    {
                      "id": "cs_test_sesion123",
                      "url": "https://checkout.stripe.com/c/pay/cs_test_sesion123",
                      "currency": "mxn",
                      "amount_total": 15000,
                      "status": "open",
                      "payment_status": "unpaid"
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, respuesta.length);
            exchange.getResponseBody().write(respuesta);
            exchange.close();
        });

        StripeConfig config = new StripeConfig(Map.of(
                StripeConfig.VAR_SECRET_KEY, "sk_test_clave_en_variable",
                StripeConfig.VAR_API_BASE_URL, baseUrl
        )::get);
        StripeCheckoutClient cliente = new StripeCheckoutClient(config);

        SolicitudCopia solicitud = new SolicitudCopia(
                1L, "CC-2026-A7K9M2", "EXP-2026-00104",
                1L, "Juzgado Primero Civil", 1L, "Civil",
                TipoDemanda.PUBLICA, EstadoSolicitud.LISTO_PARA_PAGO,
                new BigDecimal("150.00"), LocalDateTime.now());

        StripeCheckoutSession sesion = cliente.crearSesion(
                solicitud,
                "http://localhost:8080/sij/copias/publico/pago/retorno?session_id={CHECKOUT_SESSION_ID}",
                "http://localhost:8080/sij/copias/publico/pago?folio=CC-2026-A7K9M2&cancelado=1");

        assertEquals("Bearer sk_test_clave_en_variable", authRecibida.get());
        assertEquals("cs_test_sesion123", sesion.id());
        assertEquals("https://checkout.stripe.com/c/pay/cs_test_sesion123", sesion.url());

        String decodificado = URLDecoder.decode(cuerpoRecibido.get(), StandardCharsets.UTF_8);
        assertTrue(decodificado.contains("mode=payment"));
        assertTrue(decodificado.contains("line_items[0][price_data][currency]=mxn"));
        assertTrue(decodificado.contains("line_items[0][price_data][unit_amount]=15000"));
        assertTrue(decodificado.contains("client_reference_id=CC-2026-A7K9M2"));
        assertTrue(decodificado.contains("{CHECKOUT_SESSION_ID}"));
    }

    @Test
    void rechazaIdentificadorDeSesionInvalidoAntesDeLlamarApi() {
        StripeConfig config = new StripeConfig(Map.of(
                StripeConfig.VAR_SECRET_KEY, "sk_test_clave_en_variable",
                StripeConfig.VAR_API_BASE_URL, baseUrl
        )::get);
        StripeCheckoutClient cliente = new StripeCheckoutClient(config);

        assertThrows(ReglaNegocioException.class, () -> cliente.consultarSesion("../admin"));
        assertThrows(ReglaNegocioException.class, () -> cliente.consultarSesion(" "));
    }

    @Test
    void propagaMensajeCuandoStripeRespondeErrorHttp() {
        servidor.createContext("/v1/checkout/sessions", exchange -> {
            byte[] respuesta = """
                    {
                      "error": {
                        "message": "Invalid currency specified"
                      }
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, respuesta.length);
            exchange.getResponseBody().write(respuesta);
            exchange.close();
        });

        StripeConfig config = new StripeConfig(Map.of(
                StripeConfig.VAR_SECRET_KEY, "sk_test_clave_en_variable",
                StripeConfig.VAR_API_BASE_URL, baseUrl
        )::get);
        StripeCheckoutClient cliente = new StripeCheckoutClient(config);

        SolicitudCopia solicitud = new SolicitudCopia(
                1L, "CC-2026-A7K9M2", "EXP-2026-00104",
                1L, "Juzgado Primero Civil", 1L, "Civil",
                TipoDemanda.PUBLICA, EstadoSolicitud.LISTO_PARA_PAGO,
                new BigDecimal("150.00"), LocalDateTime.now());

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> cliente.crearSesion(solicitud, "http://localhost/ok", "http://localhost/cancel"));
        assertTrue(ex.getMessage().contains("Invalid currency specified"));
    }
}
