package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.PeritoDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Revisa el rol antes de cada pantalla del módulo. Al inicio de doGet / doPost:
//
//     UsuarioPeritajes usuario = PeritajesAuth.exigirAdministrador(request, response);
//     if (usuario == null) {
//         return;                 // ya respondió 403
//     }
//
// TEMPORAL: "Ver como". Mientras no hay inicio de sesión, el usuario se elige en el inicio del módulo y se
// guarda en la sesión. Cuando los integradores agreguen el inicio de sesión (FiltroNucleo, paso 4):
// - el usuario sale de SesionService del núcleo y exigir... revisa el rol del usuario en sesión,
// - se borran opciones(), cambiar(), VerComoServlet y tags/peritajes/ver-como.tag.
final class PeritajesAuth {

    private static final String ATRIBUTO_SESION = "peritajes.verComo";

    // Usuarios de los bocetos. No son peritos del catálogo (no hay sus datos), así que Rafael no tiene casos.
    private static final UsuarioPeritajes ADRIANA = new UsuarioPeritajes("adriana", "Adriana Montes Gil",
            "Administrador", UsuarioPeritajes.ADMIN, null);
    private static final UsuarioPeritajes RAFAEL = new UsuarioPeritajes("rafael", "Rafael Cruz Lara",
            "Perito Administrador", UsuarioPeritajes.PERITO_ADMIN, null);

    private static final PeritoDao peritoDao = new PeritoDao();

    private PeritajesAuth() {
    }

    // Catálogo, casos y bitácora: PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN.
    // Deja el usuario en la petición como "usuarioPeritajes" para las JSP. Si no tiene permiso responde 403 y regresa null.
    static UsuarioPeritajes exigirAdministrador(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UsuarioPeritajes usuario = actual(request.getSession());
        if (!usuario.isAdministra()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        request.setAttribute("usuarioPeritajes", usuario);
        return usuario;
    }

    // Mis peritajes y dictamen: PERITAJES_PERITO y PERITAJES_PERITO_ADMIN. Se usa igual que exigirAdministrador.
    static UsuarioPeritajes exigirPerito(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UsuarioPeritajes usuario = actual(request.getSession());
        if (!usuario.isAtiendeCasos()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        request.setAttribute("usuarioPeritajes", usuario);
        return usuario;
    }

    // Administrador, Perito Administrador y cada perito activo del catálogo (un perito inactivo no entra, DFR RN 2)
    static List<UsuarioPeritajes> opciones() {
        List<UsuarioPeritajes> opciones = new ArrayList<>(List.of(ADRIANA, RAFAEL));
        peritoDao.listarActivos().stream()
                .sorted(Comparator.comparing(Perito::getNombreCompleto))
                .map(p -> new UsuarioPeritajes("perito-" + p.getId(), p.getNombreCompleto(), "Perito Judicial",
                        UsuarioPeritajes.PERITO, p.getId()))
                .forEach(opciones::add);
        return opciones;
    }

    // El usuario elegido; si no se ha elegido (o ya no es válido), el Administrador
    static UsuarioPeritajes actual(HttpSession sesion) {
        Object clave = sesion.getAttribute(ATRIBUTO_SESION);
        for (UsuarioPeritajes usuario : opciones()) {
            if (usuario.getClave().equals(clave)) {
                return usuario;
            }
        }
        return ADRIANA;
    }

    // Cambia el usuario de "Ver como". null si la clave no existe.
    static UsuarioPeritajes cambiar(HttpSession sesion, String clave) {
        for (UsuarioPeritajes usuario : opciones()) {
            if (usuario.getClave().equals(clave)) {
                sesion.setAttribute(ATRIBUTO_SESION, usuario.getClave());
                return usuario;
            }
        }
        return null;
    }
}
