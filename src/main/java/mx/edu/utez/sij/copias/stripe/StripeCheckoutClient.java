package mx.edu.utez.sij.copias.stripe;

import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.service.ReglaNegocioException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Cliente HTTP oficial hacia la API REST de Stripe Checkout usando java.net.http.HttpClient (Java 21).
 */
public class StripeCheckoutClient {

    private static final Pattern PATRON_SESSION_ID = Pattern.compile("^[A-Za-z0-9_-]{8,200}$");

    private final StripeConfig config;
    private final HttpClient httpClient;

    public StripeCheckoutClient(StripeConfig config) {
        this(config, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build());
    }

    public StripeCheckoutClient(StripeConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    public StripeCheckoutSession crearSesion(SolicitudCopia solicitud, String successUrl, String cancelUrl) {
        if (solicitud == null || solicitud.getCostoCentavos() <= 0) {
            throw new ReglaNegocioException("El monto de la solicitud no es válido para iniciar el pago.");
        }
        String secretKey = config.obtenerSecretKey();
        String moneda = config.obtenerMoneda();

        Map<String, String> formulario = new LinkedHashMap<>();
        formulario.put("mode", "payment");
        formulario.put("payment_method_types[0]", "card");
        formulario.put("line_items[0][price_data][currency]", moneda);
        formulario.put("line_items[0][price_data][unit_amount]", String.valueOf(solicitud.getCostoCentavos()));
        formulario.put("line_items[0][price_data][product_data][name]",
                "Copias certificadas · Folio " + solicitud.getFolioSolicitud());
        formulario.put("line_items[0][price_data][product_data][description]",
                "Expediente " + solicitud.getNumeroExpediente()
                        + (solicitud.getJuzgadoNombre() != null ? " · " + solicitud.getJuzgadoNombre() : ""));
        formulario.put("line_items[0][quantity]", "1");
        formulario.put("client_reference_id", solicitud.getFolioSolicitud());
        formulario.put("metadata[folio_solicitud]", solicitud.getFolioSolicitud());
        formulario.put("metadata[numero_expediente]", solicitud.getNumeroExpediente());
        formulario.put("success_url", successUrl);
        formulario.put("cancel_url", cancelUrl);

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(config.obtenerApiBaseUrl() + "/v1/checkout/sessions"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + secretKey)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(codificarFormulario(formulario), StandardCharsets.UTF_8))
                .build();

        Map<String, Object> respuesta = ejecutar(peticion);
        StripeCheckoutSession sesion = mapearSesion(respuesta);
        if (sesion.url() == null || sesion.url().isBlank()) {
            throw new ReglaNegocioException("Stripe no devolvió la URL de pago de Checkout.");
        }
        return sesion;
    }

    public StripeCheckoutSession consultarSesion(String sessionId) {
        if (sessionId == null || !PATRON_SESSION_ID.matcher(sessionId.trim()).matches()) {
            throw new ReglaNegocioException("El identificador de sesión de pago de Stripe no es válido.");
        }
        String secretKey = config.obtenerSecretKey();
        String idCodificado = URLEncoder.encode(sessionId.trim(), StandardCharsets.UTF_8);

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(config.obtenerApiBaseUrl() + "/v1/checkout/sessions/" + idCodificado))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + secretKey)
                .GET()
                .build();

        Map<String, Object> respuesta = ejecutar(peticion);
        return mapearSesion(respuesta);
    }

    private Map<String, Object> ejecutar(HttpRequest peticion) {
        HttpResponse<String> respuesta;
        try {
            respuesta = httpClient.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReglaNegocioException("Se interrumpió la comunicación con Stripe. Intente de nuevo.");
        } catch (IOException e) {
            throw new ReglaNegocioException("No fue posible comunicarse con Stripe en este momento. Intente de nuevo.");
        }

        Map<String, Object> json;
        try {
            json = JsonSimple.parsearObjeto(respuesta.body());
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("La respuesta recibida de Stripe no tiene un formato válido.");
        }

        if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
            Map<String, Object> errorObj = JsonSimple.objeto(json, "error");
            String mensaje = JsonSimple.texto(errorObj, "message");
            if (mensaje == null || mensaje.isBlank()) {
                mensaje = "código HTTP " + respuesta.statusCode();
            }
            throw new ReglaNegocioException("Stripe rechazó la operación: " + mensaje);
        }
        return json;
    }

    private static StripeCheckoutSession mapearSesion(Map<String, Object> json) {
        Map<String, Object> metadata = JsonSimple.objeto(json, "metadata");
        Object paymentIntentObj = json.get("payment_intent");
        String paymentIntentId = null;
        if (paymentIntentObj instanceof String s) {
            paymentIntentId = s;
        } else if (paymentIntentObj instanceof Map<?, ?> mapa) {
            Object id = mapa.get("id");
            paymentIntentId = id == null ? null : String.valueOf(id);
        }
        return new StripeCheckoutSession(
                JsonSimple.texto(json, "id"),
                JsonSimple.texto(json, "url"),
                JsonSimple.texto(json, "status"),
                JsonSimple.texto(json, "payment_status"),
                JsonSimple.texto(json, "currency"),
                JsonSimple.enteroLargo(json, "amount_total"),
                paymentIntentId,
                JsonSimple.texto(json, "client_reference_id"),
                JsonSimple.texto(metadata, "folio_solicitud")
        );
    }

    static String codificarFormulario(Map<String, String> campos) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entrada : campos.entrySet()) {
            if (entrada.getValue() == null) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(entrada.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entrada.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
