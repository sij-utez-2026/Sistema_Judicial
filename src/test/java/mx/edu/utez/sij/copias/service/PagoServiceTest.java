package mx.edu.utez.sij.copias.service;

import com.sun.net.httpserver.HttpServer;
import mx.edu.utez.sij.copias.DatosInicialesCopias;
import mx.edu.utez.sij.copias.dao.PagoDaoMemoria;
import mx.edu.utez.sij.copias.dao.SolicitudDaoMemoria;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.Pago;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.model.TipoDemanda;
import mx.edu.utez.sij.copias.stripe.StripeCheckoutClient;
import mx.edu.utez.sij.copias.stripe.StripeCheckoutSession;
import mx.edu.utez.sij.copias.stripe.StripeConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagoServiceTest {

    private HttpServer servidor;
    private SolicitudDaoMemoria solicitudDao;
    private PagoDaoMemoria pagoDao;
    private PagoService pagoService;
    private AtomicReference<String> respuestaConsultaStripe;

    @BeforeEach
    void preparar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        respuestaConsultaStripe = new AtomicReference<>("""
                {
                  "id": "cs_test_pago_ok_1",
                  "status": "complete",
                  "payment_status": "paid",
                  "currency": "mxn",
                  "amount_total": 15000,
                  "payment_intent": "pi_test_pago_ok_1",
                  "client_reference_id": "CC-2026-A7K9M2",
                  "metadata": {
                    "folio_solicitud": "CC-2026-A7K9M2"
                  }
                }
                """);

        servidor.createContext("/v1/checkout/sessions", exchange -> {
            String cuerpo;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                cuerpo = """
                        {
                          "id": "cs_test_pago_ok_1",
                          "url": "https://checkout.stripe.com/c/pay/cs_test_pago_ok_1",
                          "currency": "mxn",
                          "amount_total": 15000,
                          "status": "open",
                          "payment_status": "unpaid"
                        }
                        """;
            } else {
                cuerpo = respuestaConsultaStripe.get();
            }
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        servidor.start();

        String baseUrl = "http://127.0.0.1:" + servidor.getAddress().getPort();
        StripeConfig config = new StripeConfig(Map.of(
                StripeConfig.VAR_SECRET_KEY, "sk_test_prueba_unitaria",
                StripeConfig.VAR_API_BASE_URL, baseUrl
        )::get);

        solicitudDao = new SolicitudDaoMemoria();
        pagoDao = new PagoDaoMemoria();
        new DatosInicialesCopias(solicitudDao, pagoDao).cargar();
        pagoService = new PagoService(solicitudDao, pagoDao, new StripeCheckoutClient(config));
    }

    @AfterEach
    void cerrar() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    void iniciaCheckoutYRegistraPagoIdempotenteEnMxn() {
        StripeCheckoutSession sesion = pagoService.iniciarCheckoutStripe(
                "cc-2026-a7k9m2", "http://localhost:8080/sij");

        assertEquals("https://checkout.stripe.com/c/pay/cs_test_pago_ok_1", sesion.url());

        Pago registrado = pagoService.confirmarPagoStripe("cs_test_pago_ok_1");
        assertNotNull(registrado.getId());
        assertTrue(registrado.getFolioPago().startsWith("PAG-"));
        assertEquals("MXN", registrado.getMoneda());
        assertEquals(new BigDecimal("150.00"), registrado.getMonto());
        assertEquals("PAGADO", registrado.getEstadoPago());
        assertFalse(registrado.isSimulado());

        SolicitudCopia actualizada = solicitudDao.buscarPorFolio("CC-2026-A7K9M2").orElseThrow();
        assertEquals(EstadoSolicitud.PAGADO_EN_PREPARACION, actualizada.getEstado());

        // Refrescar la URL de retorno con el mismo session_id no duplica el pago
        Pago repetido = pagoService.confirmarPagoStripe("cs_test_pago_ok_1");
        assertEquals(registrado.getFolioPago(), repetido.getFolioPago());
    }

    @Test
    void noPermitePagarSolicitudesEnEsperaCanceladasOPagadas() {
        assertThrows(ReglaNegocioException.class,
                () -> pagoService.iniciarCheckoutStripe("CC-2026-C9W4R1", "http://localhost:8080/sij"));
        assertThrows(ReglaNegocioException.class,
                () -> pagoService.iniciarCheckoutStripe("CC-2026-D2N6T8", "http://localhost:8080/sij"));
        assertThrows(ReglaNegocioException.class,
                () -> pagoService.iniciarCheckoutStripe("CC-2026-E5H1V7", "http://localhost:8080/sij"));
    }

    @Test
    void rechazaSesionNoPagadaOMonedaDistintaAMxnOMontoAlterado() {
        respuestaConsultaStripe.set("""
                {
                  "id": "cs_test_no_pagado",
                  "status": "open",
                  "payment_status": "unpaid",
                  "currency": "mxn",
                  "amount_total": 15000,
                  "client_reference_id": "CC-2026-A7K9M2"
                }
                """);
        assertThrows(ReglaNegocioException.class, () -> pagoService.confirmarPagoStripe("cs_test_no_pagado"));

        respuestaConsultaStripe.set("""
                {
                  "id": "cs_test_usd",
                  "status": "complete",
                  "payment_status": "paid",
                  "currency": "usd",
                  "amount_total": 15000,
                  "client_reference_id": "CC-2026-A7K9M2"
                }
                """);
        assertThrows(ReglaNegocioException.class, () -> pagoService.confirmarPagoStripe("cs_test_usd"));

        respuestaConsultaStripe.set("""
                {
                  "id": "cs_test_monto_mal",
                  "status": "complete",
                  "payment_status": "paid",
                  "currency": "mxn",
                  "amount_total": 500,
                  "client_reference_id": "CC-2026-A7K9M2"
                }
                """);
        assertThrows(ReglaNegocioException.class, () -> pagoService.confirmarPagoStripe("cs_test_monto_mal"));
    }

    @Test
    void inhabilitaComprobanteCuandoLaSolicitudYaFueEntregada() {
        SolicitudCopia entregada = solicitudDao.guardar(new SolicitudCopia(
                null, "CC-2026-ENT999", "EXP-2026-00999",
                1L, "Juzgado Primero Civil", 1L, "Civil",
                TipoDemanda.PUBLICA, EstadoSolicitud.ENTREGADO,
                new BigDecimal("150.00"), LocalDateTime.now()));
        Pago pago = new Pago();
        pago.setSolicitudId(entregada.getId());
        pago.setFolioSolicitud(entregada.getFolioSolicitud());
        pago.setNumeroExpediente(entregada.getNumeroExpediente());
        pago.setFolioPago("PAG-2026-ENT999");
        pago.setMonto(new BigDecimal("150.00"));
        pago.setMoneda("MXN");
        pago.setStripeSessionId("cs_test_entregada");
        pago.setEstadoPago("PAGADO");
        pago.setFecha(LocalDateTime.now());
        pagoDao.guardar(pago);

        assertThrows(ReglaNegocioException.class,
                () -> pagoService.obtenerComprobante("PAG-2026-ENT999", null));
    }
}
