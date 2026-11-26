package mx.edu.utez.sij.nucleo.acceso;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enlaces de recuperación de contraseña (INTEGRADORES). docs/ARQUITECTURA.md 7.1: vigentes 15 minutos y de
 * un solo uso. Se guardan en memoria: si se reinicia Tomcat, los enlaces pendientes dejan de servir y hay
 * que pedir otro.
 *
 * Flujo: RecuperarServlet llama generar(correo) y manda el enlace; RestablecerServlet usa correoDe(token)
 * para saber de quién es y, al cambiar la contraseña, usar(token) para que no se pueda volver a usar.
 */
public final class TokenRecuperacion {

    public static final Duration VIGENCIA = Duration.ofMinutes(15);

    private static final SecureRandom AZAR = new SecureRandom();

    // token -> correo y hasta cuándo sirve
    private static final Map<String, Pendiente> TOKENS = new ConcurrentHashMap<>();

    private TokenRecuperacion() {
    }

    /** Crea un token nuevo para el correo. Los enlaces anteriores de ese mismo correo dejan de servir. */
    public static String generar(String correo) {
        limpiarVencidos();
        TOKENS.values().removeIf(p -> p.correo().equalsIgnoreCase(correo));
        byte[] bytes = new byte[32];
        AZAR.nextBytes(bytes);
        // Base64 para URL: solo letras, números, "-" y "_" (no hay que escaparlo en el enlace)
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        TOKENS.put(token, new Pendiente(correo, Instant.now().plus(VIGENCIA)));
        return token;
    }

    /** El correo dueño del token, o null si no existe, ya venció o ya se usó. */
    public static String correoDe(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        limpiarVencidos();
        Pendiente pendiente = TOKENS.get(token);
        return pendiente == null ? null : pendiente.correo();
    }

    /** Invalida el token después de cambiar la contraseña (un solo uso). */
    public static void usar(String token) {
        if (token != null) {
            TOKENS.remove(token);
        }
    }

    private static void limpiarVencidos() {
        Instant ahora = Instant.now();
        TOKENS.values().removeIf(p -> ahora.isAfter(p.vence()));
    }

    private record Pendiente(String correo, Instant vence) {
    }
}
