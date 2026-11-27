package mx.edu.utez.sij.nucleo.correo;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Envío de correos HTML por SMTP (INTEGRADORES). docs/ARQUITECTURA.md: "Correo: Jakarta Mail, solo dentro
 * del núcleo; apagado en desarrollo". Por ahora lo usa la recuperación de contraseña (RecuperarServlet).
 *
 * Toda la configuración está en src/main/resources/correo.properties (cada quien el suyo; NO se sube a
 * GitHub). Copiar correo.example.properties:
 *   correo.activo     true = se envía de verdad; false (o sin archivo) = solo se escribe en la consola
 *   correo.host       servidor SMTP, por ejemplo smtp.gmail.com
 *   correo.puerto     587 (STARTTLS)
 *   correo.usuario    cuenta que envía
 *   correo.password   contraseña de aplicación de esa cuenta (en Gmail: "Contraseñas de aplicaciones")
 *   correo.remitente  nombre que ve quien recibe, por ejemplo "Sistema Judicial Integral"
 *   correo.url_sistema dirección del sistema para los enlaces (vacía = la de la petición; solo en desarrollo)
 *
 * Uso: Correo.enviar("persona@pj.mx", "Asunto", "<p>Contenido en HTML</p>");
 * Si falla el envío lanza IllegalStateException con el motivo.
 */
public final class Correo {

    private static final String ARCHIVO = "correo.properties";

    private static Properties configuracion;

    private Correo() {
    }

    public static void enviar(String para, String asunto, String html) {
        Properties archivo = configuracion();
        if (!"true".equalsIgnoreCase(valor(archivo, "correo.activo", "false"))) {
            // Desarrollo: no se envía nada, se escribe en la consola de Tomcat para poder probar
            System.out.println("[CORREO APAGADO] Para: " + para + " | Asunto: " + asunto + "\n" + html);
            return;
        }

        String host = obligatorio(archivo, "correo.host");
        String puerto = valor(archivo, "correo.puerto", "587");
        String usuario = obligatorio(archivo, "correo.usuario");
        // Las contraseñas de aplicación de Gmail se muestran con espacios ("abcd efgh ..."): se quitan
        String password = obligatorio(archivo, "correo.password").replace(" ", "");
        String remitente = valor(archivo, "correo.remitente", "Sistema Judicial Integral");

        Properties smtp = new Properties();
        smtp.put("mail.smtp.auth", "true");
        smtp.put("mail.smtp.starttls.enable", "true");
        smtp.put("mail.smtp.starttls.required", "true");
        smtp.put("mail.smtp.host", host);
        smtp.put("mail.smtp.port", puerto);
        smtp.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        smtp.put("mail.smtp.ssl.trust", host);
        smtp.put("mail.smtp.connectiontimeout", "10000");
        smtp.put("mail.smtp.timeout", "10000");

        Session sesion = Session.getInstance(smtp, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, password);
            }
        });

        try {
            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(usuario, remitente, StandardCharsets.UTF_8.name()));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(para));
            mensaje.setSubject(asunto);
            mensaje.setContent(html, "text/html; charset=utf-8");
            Transport.send(mensaje);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("No se pudo enviar el correo a " + para + ": " + e.getMessage(), e);
        }
    }

    /**
     * Dirección del sistema para los enlaces de los correos (correo.url_sistema, por ejemplo
     * https://sij.ejemplo.mx/sij), sin "/" al final. null si no está configurada.
     */
    public static String urlSistema() {
        String url = valor(configuracion(), "correo.url_sistema", null);
        return url == null ? null : url.replaceAll("/+$", "");
    }

    // Se lee una sola vez. Si no existe el archivo, el correo queda apagado.
    private static synchronized Properties configuracion() {
        if (configuracion == null) {
            Properties propiedades = new Properties();
            try (InputStream entrada = Correo.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
                if (entrada != null) {
                    // UTF-8 para que el remitente pueda llevar acentos
                    propiedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer " + ARCHIVO, e);
            }
            configuracion = propiedades;
        }
        return configuracion;
    }

    private static String valor(Properties archivo, String clave, String porOmision) {
        String valor = limpiar(archivo.getProperty(clave));
        return (valor == null || valor.isEmpty()) ? porOmision : valor;
    }

    private static String obligatorio(Properties archivo, String clave) {
        String valor = valor(archivo, clave, null);
        if (valor == null) {
            throw new IllegalStateException("Falta configurar el correo: " + clave
                    + " (copia src/main/resources/correo.example.properties como " + ARCHIVO + ")");
        }
        return valor;
    }

    // Quita espacios y comillas que se hayan puesto por error alrededor del valor ("valor" o 'valor')
    private static String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        valor = valor.trim();
        if (valor.length() >= 2) {
            char primero = valor.charAt(0);
            char ultimo = valor.charAt(valor.length() - 1);
            if ((primero == '"' && ultimo == '"') || (primero == '\'' && ultimo == '\'')) {
                valor = valor.substring(1, valor.length() - 1).trim();
            }
        }
        return valor;
    }
}
