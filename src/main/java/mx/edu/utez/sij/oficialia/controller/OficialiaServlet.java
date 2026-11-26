package mx.edu.utez.sij.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.acceso.Contrasena;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.oficialia.model.Oficialia;
import mx.edu.utez.sij.oficialia.model.Usuario;
import mx.edu.utez.sij.oficialia.model.dao.JuzgadoDao;
import mx.edu.utez.sij.oficialia.model.dao.OficialiaDao;

import java.io.IOException;
import java.util.List;

@WebServlet("/oficialia/oficialias")
public class OficialiaServlet extends HttpServlet {

    private final OficialiaDao oficialiaDao = new OficialiaDao();
    private final JuzgadoDao juzgadoDao = new JuzgadoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (OficialiaAuth.exigirSuperAdmin(request, response) == null) {
            return;
        }
        String query = Parametros.texto(request, "q");
        List<Oficialia> oficialias = oficialiaDao.listar(query);

        request.setAttribute("oficialias", oficialias);
        request.setAttribute("juzgados", juzgadoDao.listarActivos());
        request.setAttribute("query", query);
        Vistas.mostrar(request, response, "oficialia/ificialias");
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
            Oficialia oficialia = new Oficialia(
                    Parametros.texto(request, "clave"),
                    Parametros.texto(request, "nombre"),
                    Parametros.texto(request, "claveJuzgado"),
                    "ACTIVO"
            );
            Usuario admin = leerAdmin(request);
            String contrasena = Parametros.crudo(request, "adminContrasena");
            error = Contrasena.validarNueva(contrasena);
            if (error == null) {
                error = oficialiaDao.crearConAdministrador(oficialia, admin, Contrasena.cifrar(contrasena));
            }
            if (error == null) {
                FlashMessage.exito(request, "Oficialía registrada",
                        oficialia.getNombre() + " quedó vinculada a " + admin.getNombreCompleto()
                                + " (" + admin.getCorreo() + ").");
            }
        } else if ("asignarAdmin".equals(action)) {
            String claveOficialia = Parametros.texto(request, "clave");
            Usuario admin = leerAdmin(request);
            String contrasena = Parametros.crudo(request, "adminContrasena");
            error = Contrasena.validarNueva(contrasena);
            if (error == null) {
                error = oficialiaDao.asignarNuevoAdministrador(claveOficialia, admin, Contrasena.cifrar(contrasena));
            }
            if (error == null) {
                FlashMessage.exito(request, "Administrador asignado",
                        admin.getNombreCompleto() + " (" + admin.getCorreo() + ") quedó asignado a la oficialía.");
            }
        } else if ("toggle".equals(action)) {
            error = oficialiaDao.toggleEstado(Parametros.texto(request, "clave"));
            if (error == null) {
                FlashMessage.exito(request, "Estado actualizado", null);
            }
        } else if ("editar".equals(action)) {
            Oficialia oficialia = new Oficialia(
                    Parametros.texto(request, "clave"),
                    Parametros.texto(request, "nombre"),
                    Parametros.texto(request, "claveJuzgado"),
                    null
            );
            error = oficialiaDao.actualizar(oficialia);
            if (error == null) {
                FlashMessage.exito(request, "Oficialía actualizada", null);
            }
        } else if ("eliminar".equals(action)) {
            error = oficialiaDao.eliminar(Parametros.texto(request, "clave"));
            if (error == null) {
                FlashMessage.exito(request, "Oficialía eliminada", null);
            }
        }

        if (error != null) {
            FlashMessage.error(request, error);
        }
        Vistas.redirigir(request, response, "/oficialia/oficialias");
    }

    private Usuario leerAdmin(HttpServletRequest request) {
        return new Usuario(
                Parametros.texto(request, "adminNombre"),
                Parametros.texto(request, "adminApellidoPaterno"),
                Parametros.texto(request, "adminApellidoMaterno"),
                Parametros.texto(request, "adminCorreo"),
                OficialiaDao.ROL_OFICIALIA_ADMIN
        );
    }
}

