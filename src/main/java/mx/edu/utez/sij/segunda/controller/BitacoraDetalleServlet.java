package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;

@WebServlet("/segunda/bitacora/detalle")
public class BitacoraDetalleServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (usuario == null || !SegundaPermisos.esAdmin(usuario)) { response.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        Long id = Parametros.numero(request, "id");
        if (id == null) { Vistas.noEncontrado(response); return; }
        var registro = Segunda.bitacoraDao().buscar(id);
        if (registro.isEmpty()) { Vistas.noEncontrado(response); return; }
        request.setAttribute("registro", registro.get());
        Vistas.mostrar(request, response, "segunda/bitacora/detalle");
    }
}
