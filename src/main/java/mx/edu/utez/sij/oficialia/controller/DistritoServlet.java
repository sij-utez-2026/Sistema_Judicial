package mx.edu.utez.sij.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.oficialia.model.Distrito;
import mx.edu.utez.sij.oficialia.model.dao.DistritoDao;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DistritoServlet", value = "/distritos")
public class DistritoServlet extends HttpServlet {

    private final DistritoDao distritoDao = new DistritoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("q");
        List<Distrito> distritos = distritoDao.getAll();

        if (query != null && !query.trim().isEmpty()) {
            String qLower = query.toLowerCase().trim();
            distritos = distritos.stream()
                    .filter(d -> d.getClave().toLowerCase().contains(qLower)
                            || d.getNombre().toLowerCase().contains(qLower)
                            || (d.getEstado() != null && d.getEstado().toLowerCase().contains(qLower)))
                    .toList();
        }

        request.setAttribute("distritos", distritos);
        request.setAttribute("query", query);
        request.getRequestDispatcher("/distritos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("crear".equals(action)) {
            String clave = request.getParameter("clave");
            String nombre = request.getParameter("nombre");

            if (distritoDao.getById(clave) != null) {
                response.sendRedirect(request.getContextPath() + "/distritos?error=duplicado");
                return;
            }

            Distrito distrito = new Distrito(clave, nombre, "Activo");
            distritoDao.save(distrito);
            response.sendRedirect(request.getContextPath() + "/distritos?msg=creado");

        } else if ("toggle".equals(action)) {
            String clave = request.getParameter("clave");
            Distrito d = distritoDao.getById(clave);
            if (d != null) {
                d.setEstado("Activo".equalsIgnoreCase(d.getEstado()) ? "Inactivo" : "Activo");
                distritoDao.update(d);
            }
            response.sendRedirect(request.getContextPath() + "/distritos?msg=actualizado");

        } else if ("editar".equals(action)) {
            String clave = request.getParameter("clave");
            String nombre = request.getParameter("nombre");
            String estado = request.getParameter("estado");

            Distrito d = distritoDao.getById(clave);
            if (d != null) {
                d.setNombre(nombre);
                if (estado != null) d.setEstado(estado);
                distritoDao.update(d);
            }
            response.sendRedirect(request.getContextPath() + "/distritos?msg=actualizado");

        } else if ("eliminar".equals(action)) {
            String clave = request.getParameter("clave");
            distritoDao.delete(clave);
            response.sendRedirect(request.getContextPath() + "/distritos?msg=eliminado");
        } else {
            response.sendRedirect(request.getContextPath() + "/distritos");
        }
    }
}

