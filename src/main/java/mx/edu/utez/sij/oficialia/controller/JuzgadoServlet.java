package mx.edu.utez.sij.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.oficialia.model.Juzgado;
import mx.edu.utez.sij.oficialia.model.dao.DistritoDao;
import mx.edu.utez.sij.oficialia.model.dao.JuzgadoDao;

import java.io.IOException;
import java.util.List;

@WebServlet("/oficialia/juzgados")
public class JuzgadoServlet extends HttpServlet {

    private final JuzgadoDao juzgadoDao = new JuzgadoDao();
    private final DistritoDao distritoDao = new DistritoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (OficialiaAuth.exigirSuperAdmin(request, response) == null) {
            return;
        }
        String query = Parametros.texto(request, "q");
        List<Juzgado> juzgados = juzgadoDao.listar(query);

        request.setAttribute("juzgados", juzgados);
        request.setAttribute("distritos", distritoDao.listarActivos());
        request.setAttribute("query", query);
        Vistas.mostrar(request, response, "oficialia/juzgados");
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
            Juzgado juzgado = new Juzgado(
                    Parametros.texto(request, "clave"),
                    Parametros.texto(request, "nombre"),
                    Parametros.texto(request, "claveDistrito"),
                    null,
                    "ACTIVO"
            );
            error = juzgadoDao.crear(juzgado);
            if (error == null) {
                FlashMessage.exito(request, "Juzgado registrado", "El juzgado quedó activo en el catálogo.");
            }
        } else if ("toggle".equals(action)) {
            error = juzgadoDao.toggleEstado(Parametros.texto(request, "clave"));
            if (error == null) {
                FlashMessage.exito(request, "Estado actualizado", null);
            }
        } else if ("editar".equals(action)) {
            Juzgado juzgado = new Juzgado(
                    Parametros.texto(request, "clave"),
                    Parametros.texto(request, "nombre"),
                    Parametros.texto(request, "claveDistrito"),
                    null,
                    null
            );
            error = juzgadoDao.actualizar(juzgado);
            if (error == null) {
                FlashMessage.exito(request, "Juzgado actualizado", null);
            }
        } else if ("eliminar".equals(action)) {
            error = juzgadoDao.eliminar(Parametros.texto(request, "clave"));
            if (error == null) {
                FlashMessage.exito(request, "Juzgado eliminado", null);
            }
        }

        if (error != null) {
            FlashMessage.error(request, error);
        }
        Vistas.redirigir(request, response, "/oficialia/juzgados");
    }
}
