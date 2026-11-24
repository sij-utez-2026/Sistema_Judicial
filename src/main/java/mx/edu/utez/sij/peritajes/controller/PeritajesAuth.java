package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.acceso.Acceso;
import mx.edu.utez.sij.nucleo.web.UsuarioVista;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;

import java.io.IOException;

// Permisos de cada pantalla del módulo. Al inicio de doGet / doPost de cada servlet:
//
//     UsuarioPeritajes usuario = PeritajesAuth.exigirAdministrador(request, response);
//     if (usuario == null) {
//         return;                 // ya mandó al login o respondió 403
//     }
//
// El inicio de sesión es del núcleo (/login): FiltroNucleo ya revisó que haya sesión y que el rol pueda entrar
// a /peritajes (un rol PERITAJES_* o el administrador general). Aquí se revisa el permiso de cada pantalla
// (peritajes.md, sección 3):
// - casos, catálogo y bitácora → ADMIN, PERITO_ADMIN y el administrador general;
// - mis peritajes y dictamen → PERITO y PERITO_ADMIN (el administrador general no ve dictámenes);
// - inicio → todos.
final class PeritajesAuth {

    private PeritajesAuth() {
    }

    // Cualquier usuario que pueda entrar a Peritajes. Deja el usuario en la petición como "usuarioPeritajes"
    // (para las JSP y el menú) y su rol de Peritajes en el menú lateral del núcleo ("usuarioActual").
    static UsuarioPeritajes exigirSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (Acceso.usuario(request) == null) {
            Vistas.redirigir(request, response, "/login");
            return null;
        }
        UsuarioPeritajes usuario = actual(request);
        if (usuario == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        request.setAttribute("usuarioPeritajes", usuario);
        request.setAttribute("usuarioActual", new UsuarioVista(usuario.getNombre(), usuario.getRol()));
        return usuario;
    }

    // Catálogo, casos, reasignación y bitácora
    static UsuarioPeritajes exigirAdministrador(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UsuarioPeritajes usuario = exigirSesion(request, response);
        if (usuario == null) {
            return null;
        }
        if (!usuario.isAdministra()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return usuario;
    }

    // Mis peritajes y dictamen
    static UsuarioPeritajes exigirPerito(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UsuarioPeritajes usuario = exigirSesion(request, response);
        if (usuario == null) {
            return null;
        }
        if (!usuario.isAtiendeCasos()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return usuario;
    }

    // El usuario en sesión como usuario de Peritajes. null si no hay sesión o no puede entrar al módulo.
    static UsuarioPeritajes actual(HttpServletRequest request) {
        return UsuarioPeritajes.de(Acceso.usuario(request));
    }
}
