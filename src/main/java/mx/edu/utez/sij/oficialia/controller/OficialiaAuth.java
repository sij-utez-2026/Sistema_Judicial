package mx.edu.utez.sij.oficialia.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.acceso.Acceso;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;
import mx.edu.utez.sij.nucleo.web.ModuloSistema;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;

/**
 * Verificación de sesión y permisos por pantalla del módulo de Oficialía de Partes.
 */
final class OficialiaAuth {

    private OficialiaAuth() {
    }

    static UsuarioSesion exigirSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioSesion usuario = Acceso.usuario(request);
        if (usuario == null) {
            Vistas.redirigir(request, response, "/login");
            return null;
        }
        if (!usuario.puedeEntrar(ModuloSistema.OFICIALIA)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return usuario;
    }

    static UsuarioSesion exigirSuperAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioSesion usuario = exigirSesion(request, response);
        if (usuario == null) {
            return null;
        }
        if (!usuario.isAdministradorGeneral()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return usuario;
    }
}


