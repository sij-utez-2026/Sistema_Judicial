package mx.edu.utez.sij.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.oficialia.model.Materia;
import mx.edu.utez.sij.oficialia.model.dao.MateriaDao;

import java.io.IOException;
import java.util.List;

@WebServlet("/oficialia/materias")
public class MateriaServlet extends HttpServlet {

    private final MateriaDao materiaDao = new MateriaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (OficialiaAuth.exigirSuperAdmin(request, response) == null) {
            return;
        }
        String query = Parametros.texto(request, "q");
        List<Materia> materias = materiaDao.listar(query);

        request.setAttribute("materias", materias);
        request.setAttribute("query", query);
        Vistas.mostrar(request, response, "oficialia/materias");
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
            Materia materia = new Materia(
                    Parametros.texto(request, "clave"),
                    Parametros.texto(request, "nombre"),
                    Parametros.texto(request, "descripcion"),
                    "ACTIVO"
            );
            error = materiaDao.crear(materia);
            if (error == null) {
                FlashMessage.exito(request, "Materia registrada", "La materia quedó activa en el catálogo.");
            }
        } else if ("toggle".equals(action)) {
            error = materiaDao.toggleEstado(Parametros.texto(request, "clave"));
            if (error == null) {
                FlashMessage.exito(request, "Estado actualizado", null);
            }
        } else if ("editar".equals(action)) {
            Materia materia = new Materia(
                    Parametros.texto(request, "clave"),
                    Parametros.texto(request, "nombre"),
                    Parametros.texto(request, "descripcion"),
                    null
            );
            error = materiaDao.actualizar(materia);
            if (error == null) {
                FlashMessage.exito(request, "Materia actualizada", null);
            }
        } else if ("eliminar".equals(action)) {
            error = materiaDao.eliminar(Parametros.texto(request, "clave"));
            if (error == null) {
                FlashMessage.exito(request, "Materia eliminada", null);
            }
        }

        if (error != null) {
            FlashMessage.error(request, error);
        }
        Vistas.redirigir(request, response, "/oficialia/materias");
    }
}
