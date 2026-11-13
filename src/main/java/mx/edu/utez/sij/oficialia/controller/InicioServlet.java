package mx.edu.utez.sij.oficialia.controller;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.oficialia.model.dao.DistritoDao;
import mx.edu.utez.sij.oficialia.model.dao.JuzgadoDao;
import mx.edu.utez.sij.oficialia.model.dao.MateriaDao;
import mx.edu.utez.sij.oficialia.model.dao.OficialiaDao;

import java.io.IOException;

@WebServlet(name = "InicioServlet", value = "/inicio")
public class InicioServlet extends HttpServlet {

    private final DistritoDao distritoDao = new DistritoDao();
    private final JuzgadoDao juzgadoDao = new JuzgadoDao();
    private final OficialiaDao oficialiaDao = new OficialiaDao();
    private final MateriaDao materiaDao = new MateriaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int totalDistritos = distritoDao.getAll().size();
        int totalJuzgados = juzgadoDao.getAll().size();
        int totalOficialias = oficialiaDao.getAll().size();
        int totalMaterias = materiaDao.getAll().size();

        request.setAttribute("totalDistritos", totalDistritos);
        request.setAttribute("totalJuzgados", totalJuzgados);
        request.setAttribute("totalOficialias", totalOficialias);
        request.setAttribute("totalMaterias", totalMaterias);

        request.getRequestDispatcher("/inicioAdmin.jsp").forward(request, response);
    }
}