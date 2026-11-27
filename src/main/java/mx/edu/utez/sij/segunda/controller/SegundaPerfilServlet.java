package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.dto.PerfilSegundaDto;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;

/** Muestra únicamente los datos de perfil de la cuenta autenticada. */
@WebServlet("/segunda/perfil")
public class SegundaPerfilServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SegundaPermisos.requiereAcceso(request, response)) return;
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        boolean magistrado = SegundaPermisos.esMagistrado(usuario);
        PerfilSegundaDto perfil = new PerfilSegundaDto(
                usuario.getNombre(), usuario.getCorreo(),
                java.util.List.of(usuario.getRol()), true, magistrado, null, null);
        request.setAttribute("perfil", perfil);
        Vistas.mostrar(request, response, "segunda/perfil/detalle");
    }
}
