package mx.edu.utez.sij.copias.stripe;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonSimpleTest {

    @Test
    void parseaRespuestaDeCheckoutSessionConObjetosAnidados() {
        String json = """
                {
                  "id": "cs_test_a1B2c3D4",
                  "object": "checkout.session",
                  "amount_total": 15000,
                  "currency": "mxn",
                  "status": "complete",
                  "payment_status": "paid",
                  "payment_intent": "pi_3QxYz123",
                  "client_reference_id": "CC-2026-A7K9M2",
                  "url": "https:\\/\\/checkout.stripe.com\\/c\\/pay\\/cs_test_a1B2c3D4",
                  "livemode": false,
                  "metadata": {
                    "folio_solicitud": "CC-2026-A7K9M2",
                    "numero_expediente": "EXP-2026-00104"
                  },
                  "customer_details": null
                }
                """;

        Map<String, Object> objeto = JsonSimple.parsearObjeto(json);

        assertEquals("cs_test_a1B2c3D4", JsonSimple.texto(objeto, "id"));
        assertEquals(15000L, JsonSimple.enteroLargo(objeto, "amount_total"));
        assertEquals("mxn", JsonSimple.texto(objeto, "currency"));
        assertEquals("paid", JsonSimple.texto(objeto, "payment_status"));
        assertEquals("https://checkout.stripe.com/c/pay/cs_test_a1B2c3D4", JsonSimple.texto(objeto, "url"));
        assertNull(JsonSimple.texto(objeto, "customer_details"));
        Map<String, Object> metadata = JsonSimple.objeto(objeto, "metadata");
        assertEquals("CC-2026-A7K9M2", JsonSimple.texto(metadata, "folio_solicitud"));
    }

    @Test
    void parseaErrorDeStripe() {
        String json = """
                {
                  "error": {
                    "message": "Invalid API Key provided",
                    "type": "invalid_request_error"
                  }
                }
                """;

        Map<String, Object> raiz = JsonSimple.parsearObjeto(json);
        Map<String, Object> error = JsonSimple.objeto(raiz, "error");
        assertEquals("Invalid API Key provided", JsonSimple.texto(error, "message"));
    }

    @Test
    void rechazaJsonVacioOMalformado() {
        assertThrows(IllegalArgumentException.class, () -> JsonSimple.parsearObjeto(""));
        assertThrows(IllegalArgumentException.class, () -> JsonSimple.parsearObjeto("{\"id\": "));
        assertThrows(IllegalArgumentException.class, () -> JsonSimple.parsearObjeto("[1, 2, 3]"));
    }
}
