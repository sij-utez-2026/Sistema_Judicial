package mx.edu.utez.sij.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.oficialia.model.Distrito;
import mx.edu.utez.sij.oficialia.model.dao.DistritoDao;

import java.io.IOException;
import java.util.List;

@WebServlet("/oficialia/distritos")
public class DistritoServlet extends HttpServlet {

    private final DistritoDao distritoDao = new DistritoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (OficialiaAuth.exigirSuperAdmin(request, response) == null) {
            return;
        }
        String query = Parametros.texto(request, "q");
        List<Distrito> distritos = distritoDao.listar(query);

        request.setAttribute("distritos", distritos);
        request.setAttribute("query", query);
        Vistas.mostrar(request, response, "oficialia/distritos");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (OficialiaAuth.exigirSuperAdmin(request, response) == null) {
            return;
        }
        String action = Parametros.texto(request, "action");
        String error = null;

        if ("crear".equals(action)) {
            Distrito distrito = new Distrito(
                    Parametros.texto(request, "clave"),
                    Parametros.texto(request, "nombre"),
                    Parametros.texto(request, "cabecera"),
                    "ACTIVO"
            );
            error = distritoDao.crear(distrito);
            if (error == null) {
                FlashMessage.exito(request, "Distrito registrado", "El distrito quedó activo en el catálogo.");
            }
        } else if ("toggle".equals(action)) {
            error = distritoDao.toggleEstado(Parametros.texto(request, "clave"));
            if (error == null) {
                FlashMessage.exito(request, "Estado actualizado", null);
            }
        } else if ("editar".equals(action)) {
            Distrito distrito = new Distrito(
                    Parametros.texto(request, "clave"),
                    Parametros.texto(request, "nombre"),
                    Parametros.texto(request, "cabecera"),
                    null
            );
            error = distritoDao.actualizar(distrito);
            if (error == null) {
                FlashMessage.exito(request, "Distrito actualizado", null);
            }
        } else if ("eliminar".equals(action)) {
            error = distritoDao.eliminar(Parametros.texto(request, "clave"));
            if (error == null) {
                FlashMessage.exito(request, "Distrito eliminado", null);
            }
        }

        if (error != null) {
            FlashMessage.error(request, error);
        }
        Vistas.redirigir(request, response, "/oficialia/distritos");
    }
}
