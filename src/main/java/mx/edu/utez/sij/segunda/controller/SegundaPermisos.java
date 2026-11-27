package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;
import mx.edu.utez.sij.segunda.Segunda;

import java.io.IOException;

/** Permisos finos del módulo, usando exclusivamente la sesión del núcleo. */
public final class SegundaPermisos {
    private SegundaPermisos() { }

    public static boolean esAdmin(UsuarioSesion usuario) {
        return usuario != null && ("SEGUNDA_ADMIN".equals(usuario.getRol()) || usuario.isAdministradorGeneral());
    }

    public static boolean esMagistrado(UsuarioSesion usuario) {
        return usuario != null && "SEGUNDA_MAGISTRADO".equals(usuario.getRol());
    }

    public static boolean esUsuarioSegunda(UsuarioSesion usuario) {
        return esAdmin(usuario) || esMagistrado(usuario);
    }

    public static boolean requiereAcceso(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (esUsuarioSegunda(Segunda.usuarioActual(request))) return true;
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Operación exclusiva de Segunda Instancia");
        return false;
    }

    public static boolean requiereAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (esAdmin(Segunda.usuarioActual(request))) return true;
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Operación exclusiva de administración de Segunda Instancia");
        return false;
    }
}
