package mx.edu.utez.sij.copias.stripe;

import mx.edu.utez.sij.copias.service.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StripeConfigTest {

    @Test
    void usaMxnPorOmisionYVariablesSinHardcodear() {
        Map<String, String> variables = Map.of(
                StripeConfig.VAR_SECRET_KEY, "sk_test_variable_simulada",
                StripeConfig.VAR_PUBLISHABLE_KEY, "pk_test_variable_simulada",
                StripeConfig.VAR_API_BASE_URL, "https://api.stripe.com/"
        );

        StripeConfig config = new StripeConfig(variables::get);

        assertTrue(config.estaConfigurada());
        assertEquals("sk_test_variable_simulada", config.obtenerSecretKey());
        assertEquals("pk_test_variable_simulada", config.obtenerPublishableKey());
        assertEquals("mxn", config.obtenerMoneda());
        assertEquals("https://api.stripe.com", config.obtenerApiBaseUrl());
        assertNull(config.obtenerAppBaseUrl());
    }

    @Test
    void exigeVariableDeLlaveSecreta() {
        StripeConfig config = new StripeConfig(nombre -> null);

        assertFalse(config.estaConfigurada());
        assertThrows(ReglaNegocioException.class, config::obtenerSecretKey);
    }

    @Test
    void rechazaMonedaInvalida() {
        Map<String, String> variables = Map.of(
                StripeConfig.VAR_SECRET_KEY, "sk_test_variable_simulada",
                StripeConfig.VAR_MONEDA, "12"
        );
        StripeConfig config = new StripeConfig(variables::get);

        assertThrows(ReglaNegocioException.class, config::obtenerMoneda);
    }

    @Test
    void rechazaUrlsSinEsquemaHttpOHostValido() {
        StripeConfig apiInvalida = new StripeConfig(Map.of(
                StripeConfig.VAR_API_BASE_URL, "api.stripe.com"
        )::get);
        assertThrows(ReglaNegocioException.class, apiInvalida::obtenerApiBaseUrl);

        StripeConfig appInvalida = new StripeConfig(Map.of(
                StripeConfig.VAR_APP_BASE_URL, "ftp://localhost:8080/sij"
        )::get);
        assertThrows(ReglaNegocioException.class, appInvalida::obtenerAppBaseUrl);

        StripeConfig appValida = new StripeConfig(Map.of(
                StripeConfig.VAR_APP_BASE_URL, "http://localhost:8080/sij/"
        )::get);
        assertEquals("http://localhost:8080/sij", appValida.obtenerAppBaseUrl());
    }
}
