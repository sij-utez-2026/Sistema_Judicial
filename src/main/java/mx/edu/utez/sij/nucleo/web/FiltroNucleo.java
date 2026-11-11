package mx.edu.utez.sij.nucleo.web;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Filtro común del sistema (INTEGRADORES). Corre antes de TODOS los servlets y JSP:
 *
 * 1. UTF-8 en la petición y la respuesta (acentos y ñ).
 * 2. Encabezados de seguridad y sin caché en las páginas dinámicas.
 * 3. CSRF: rechaza con 403 todo POST sin el token correcto (ver CsrfToken y &lt;n:csrf/&gt;).
 * 4. TODO núcleo: inicio de sesión y permisos por rol (lista blanca: lo que no está permitido se bloquea).
 * 5. Datos que el layout necesita en todas las pantallas (fecha, módulos, usuario y token CSRF).
 * 6. Mensajes flash pendientes de la petición anterior (FlashMessage).
 *
 * Los módulos NO lo modifican; si necesitan algo aquí, abren un issue para los integradores.
 */
@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.ERROR})
public class FiltroNucleo extends HttpFilter {

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm 'h'", Locale.forLanguageTag("es-MX"));
    private static final List<ModuloSistema> MODULOS = List.of(ModuloSistema.values());

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 1. UTF-8 (antes de leer cualquier parámetro)
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Los archivos de recursos/ (CSS, JS, imágenes) pasan sin más trabajo
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (ruta.startsWith("/recursos/")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Encabezados de seguridad
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");
        response.setHeader("Cache-Control", "no-store");

        boolean peticionNormal = request.getDispatcherType() == DispatcherType.REQUEST;

        // 3. CSRF en todo POST
        if (peticionNormal && "POST".equalsIgnoreCase(request.getMethod())) {
            boolean tokenValido;
            try {
                tokenValido = CsrfToken.esValido(request);
            } catch (IllegalStateException e) {
                // Tomcat no pudo leer el formulario: casi siempre un archivo más grande que el @MultipartConfig del servlet
                response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                return;
            }
            if (!tokenValido) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // 4. TODO núcleo: cuando exista el inicio de sesión, aquí se revisa que haya sesión y que el rol
        //    pueda entrar a la ruta (docs/ARQUITECTURA.md, sección 10). Las rutas públicas: /login,
        //    /recuperar, /recursos/** y /copias/publico/**.

        // 5. Datos del layout
        request.setAttribute("_csrf", CsrfToken.deLaSesion(request));
        request.setAttribute("fechaHoraActual", LocalDateTime.now(ZONA).format(FORMATO_FECHA).replace(".", ""));
        // TODO núcleo: con el inicio de sesión, solo los módulos de los roles del usuario
        request.setAttribute("modulosSistema", MODULOS);
        // TODO núcleo: el usuario en sesión (SesionService)
        request.setAttribute("usuarioActual", new UsuarioVista("Usuario de prueba", "Sin sesión (pendiente del núcleo)"));

        // 6. Mensajes flash
        if (peticionNormal) {
            FlashMessage.entregar(request);
        }

        chain.doFilter(request, response);
    }
}
