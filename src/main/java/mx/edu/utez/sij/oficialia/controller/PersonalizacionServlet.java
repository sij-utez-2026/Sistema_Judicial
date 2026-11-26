package mx.edu.utez.sij.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.oficialia.model.Usuario;
import mx.edu.utez.sij.oficialia.model.dao.UsuarioDao;

import java.io.IOException;

@WebServlet("/oficialia/personalizacion")
public class PersonalizacionServlet extends HttpServlet {

    private final UsuarioDao usuarioDao = new UsuarioDao();

   /* @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion sesion = OficialiaAuth.exigirSuperAdmin(request, response);
        if (sesion == null) {
            return;
        }
        if (sesion.getId() != null) {
            request.setAttribute("usuarioPerfil", usuarioDao.buscarPorId(sesion.getId()));
        }
        Vistas.mostrar(request, response, "oficialia/personalizacion");
    }*/

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion sesion = OficialiaAuth.exigirSuperAdmin(request, response);
        if (sesion == null) {
            return;
        }
        String nombre = Parametros.texto(request, "nombre");
        String apellidoPaterno = Parametros.texto(request, "apellidoPaterno");
        String apellidoMaterno = Parametros.texto(request, "apellidoMaterno");

        if (nombre != null && apellidoPaterno != null && sesion.getId() != null) {
            String error = usuarioDao.actualizarNombre(sesion.getId(), nombre, apellidoPaterno, apellidoMaterno);
            if (error != null) {
                FlashMessage.error(request, error);
                Vistas.redirigir(request, response, "/oficialia/personalizacion");
                return;
            }
        }
        FlashMessage.exito(request, "Personalización guardada",
                "La identidad institucional y preferencias se aplicaron correctamente.");
        Vistas.redirigir(request, response, "/oficialia/personalizacion");
    }
}
