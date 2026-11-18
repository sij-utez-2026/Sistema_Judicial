package mx.edu.utez.sij.copias.stripe;

import mx.edu.utez.sij.copias.service.ReglaNegocioException;

import java.net.URI;
import java.util.Locale;
import java.util.function.Function;

/**
 * Configuración de Stripe leída exclusivamente desde variables de entorno o propiedades JVM.
 * Nunca contiene llaves ni credenciales escritas en código fuente.
 */
public class StripeConfig {

    public static final String VAR_SECRET_KEY = "STRIPE_SECRET_KEY";
    public static final String VAR_PUBLISHABLE_KEY = "STRIPE_PUBLISHABLE_KEY";
    public static final String VAR_MONEDA = "STRIPE_MONEDA";
    public static final String VAR_API_BASE_URL = "STRIPE_API_BASE_URL";
    public static final String VAR_APP_BASE_URL = "SIJ_APP_BASE_URL";

    public static final String MONEDA_POR_OMISION = "mxn";
    public static final String API_BASE_POR_OMISION = "https://api.stripe.com";

    private final Function<String, String> lectorVariables;

    public StripeConfig() {
        this(StripeConfig::leerDesdeSistema);
    }

    public StripeConfig(Function<String, String> lectorVariables) {
        this.lectorVariables = lectorVariables;
    }

    public static StripeConfig desdeEntorno() {
        return new StripeConfig();
    }

    private static String leerDesdeSistema(String nombreVariable) {
        String propiedad = System.getProperty(nombreVariable);
        if (propiedad != null && !propiedad.isBlank()) {
            return propiedad.trim();
        }
        String entorno = System.getenv(nombreVariable);
        return (entorno == null || entorno.isBlank()) ? null : entorno.trim();
    }

    public String obtenerSecretKey() {
        String clave = lectorVariables.apply(VAR_SECRET_KEY);
        if (clave == null || clave.isBlank()) {
            throw new ReglaNegocioException(
                    "Falta configurar la variable de entorno " + VAR_SECRET_KEY + " para iniciar el pago en Stripe.");
        }
        return clave.trim();
    }

    public String obtenerPublishableKey() {
        String clave = lectorVariables.apply(VAR_PUBLISHABLE_KEY);
        return (clave == null || clave.isBlank()) ? null : clave.trim();
    }

    public boolean estaConfigurada() {
        String clave = lectorVariables.apply(VAR_SECRET_KEY);
        return clave != null && !clave.isBlank();
    }

    public String obtenerMoneda() {
        String valor = lectorVariables.apply(VAR_MONEDA);
        if (valor == null || valor.isBlank()) {
            return MONEDA_POR_OMISION;
        }
        String normalizada = valor.trim().toLowerCase(Locale.ROOT);
        if (!normalizada.matches("[a-z]{3}")) {
            throw new ReglaNegocioException("La variable " + VAR_MONEDA + " debe ser un código de moneda ISO de 3 letras.");
        }
        return normalizada;
    }

    public String obtenerApiBaseUrl() {
        String valor = lectorVariables.apply(VAR_API_BASE_URL);
        if (valor == null || valor.isBlank()) {
            return API_BASE_POR_OMISION;
        }
        return validarUrlHttp(quitarBarraFinal(valor.trim()), VAR_API_BASE_URL);
    }

    public String obtenerAppBaseUrl() {
        String valor = lectorVariables.apply(VAR_APP_BASE_URL);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return validarUrlHttp(quitarBarraFinal(valor.trim()), VAR_APP_BASE_URL);
    }

    private static String validarUrlHttp(String url, String nombreVariable) {
        try {
            URI uri = URI.create(url);
            String esquema = uri.getScheme();
            if ((!"http".equalsIgnoreCase(esquema) && !"https".equalsIgnoreCase(esquema))
                    || uri.getHost() == null || uri.getHost().isBlank()) {
                throw new ReglaNegocioException("La variable " + nombreVariable + " debe ser una URL http/https válida.");
            }
            return url;
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("La variable " + nombreVariable + " no tiene un formato de URL válido.");
        }
    }

    private static String quitarBarraFinal(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
