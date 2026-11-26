package mx.edu.utez.sij.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.oficialia.model.dao.DistritoDao;
import mx.edu.utez.sij.oficialia.model.dao.JuzgadoDao;
import mx.edu.utez.sij.oficialia.model.dao.MateriaDao;
import mx.edu.utez.sij.oficialia.model.dao.OficialiaDao;

import java.io.IOException;

@WebServlet("/oficialia")
public class InicioServlet extends HttpServlet {

    private final DistritoDao distritoDao = new DistritoDao();
    private final JuzgadoDao juzgadoDao = new JuzgadoDao();
    private final OficialiaDao oficialiaDao = new OficialiaDao();
    private final MateriaDao materiaDao = new MateriaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion usuario = OficialiaAuth.exigirSesion(request, response);
        if (usuario == null) {
            return;
        }
        if (usuario.isAdministradorGeneral()) {
            request.setAttribute("totalDistritos", distritoDao.contar());
            request.setAttribute("totalJuzgados", juzgadoDao.contar());
            request.setAttribute("totalOficialias", oficialiaDao.contar());
            request.setAttribute("totalMaterias", materiaDao.contar());
            Vistas.mostrar(request, response, "oficialia/inicioAdmin");
            return;
        }
        Vistas.mostrar(request, response, "oficialia/inicio");
    }
}
