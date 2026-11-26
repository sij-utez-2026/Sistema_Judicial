package mx.edu.utez.sij.nucleo.web;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.acceso.Acceso;
import mx.edu.utez.sij.nucleo.acceso.AccesoDao;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Filtro común del sistema (INTEGRADORES). Corre antes de TODOS los servlets y JSP:
 *
 * 1. UTF-8 en la petición y la respuesta (acentos y ñ).
 * 2. Encabezados de seguridad y sin caché en las páginas dinámicas.
 * 3. CSRF: rechaza con 403 todo POST sin el token correcto (ver CsrfToken y &lt;n:csrf/&gt;).
 * 4. Inicio de sesión y permiso por módulo (lista blanca: lo que no está permitido se bloquea).
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

    private final AccesoDao accesoDao = new AccesoDao();

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

        // 4. Inicio de sesión y permiso por módulo (docs/ARQUITECTURA.md, sección 10).
        //    Públicas: /login, /recuperar, /recuperar/restablecer, /recursos/** (arriba) y /copias/publico/**.
        //    Sin sesión, todo lo demás va a /login. A /<modulo>/** solo entra el administrador general
        //    o quien tiene un rol de ese módulo (PERITAJES_*, COPIAS_*...); si no, 403.
        //    En las páginas de error (dispatch ERROR) se conserva el usuario de la petición original.
        UsuarioSesion usuario = Acceso.usuario(request);
        if (peticionNormal) {
            usuario = Acceso.cargar(request, accesoDao);
            boolean publica = esPublica(ruta);
            if (usuario == null && !publica) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            ModuloSistema modulo = moduloDeLaRuta(ruta);
            if (usuario != null && !publica && modulo != null && !usuario.puedeEntrar(modulo)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // 5. Datos del layout
        request.setAttribute("_csrf", CsrfToken.deLaSesion(request));
        request.setAttribute("fechaHoraActual", LocalDateTime.now(ZONA).format(FORMATO_FECHA).replace(".", ""));
        // Solo los módulos a los que puede entrar (el administrador general, los cinco)
        List<ModuloSistema> modulos = new ArrayList<>();
        for (ModuloSistema modulo : MODULOS) {
            if (usuario != null && usuario.puedeEntrar(modulo)) {
                modulos.add(modulo);
            }
        }
        request.setAttribute("modulosSistema", modulos);
        // Nombre y rol del menú lateral (cada módulo puede cambiarlo por un rol más preciso)
        if (usuario != null) {
            request.setAttribute("usuarioActual", new UsuarioVista(usuario.getNombre(), usuario.getRolTexto()));
        }

        // 6. Mensajes flash
        if (peticionNormal) {
            FlashMessage.entregar(request);
        }

        chain.doFilter(request, response);
    }

    // Rutas que no piden sesión (además de /recursos/**, que se atiende arriba)
    private boolean esPublica(String ruta) {
        return ruta.equals("/login") || ruta.equals("/recuperar") || ruta.equals("/recuperar/restablecer")
                || ruta.startsWith("/copias/publico/") || ruta.equals("/copias/publico");
    }

    // /peritajes/casos → PERITAJES; null si la ruta no es de un módulo (/, /cuenta/password...)
    private ModuloSistema moduloDeLaRuta(String ruta) {
        for (ModuloSistema modulo : MODULOS) {
            String prefijo = "/" + modulo.getPrefijo();
            if (ruta.equals(prefijo) || ruta.startsWith(prefijo + "/")) {
                return modulo;
            }
        }
        return null;
    }
}
