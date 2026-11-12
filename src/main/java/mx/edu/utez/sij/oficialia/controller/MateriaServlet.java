package mx.edu.utez.sij.oficialia.controller;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.oficialia.model.Materia;
import mx.edu.utez.sij.oficialia.model.dao.MateriaDao;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "MateriaServlet", value = "/materias")
public class MateriaServlet extends HttpServlet {

    private final MateriaDao materiaDao = new MateriaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("q");
        List<Materia> materias = materiaDao.getAll();

        if (query != null && !query.trim().isEmpty()) {
            String qLower = query.toLowerCase().trim();
            materias = materias.stream()
                    .filter(m -> m.getClave().toLowerCase().contains(qLower)
                            || m.getNombre().toLowerCase().contains(qLower)
                            || (m.getDescripcion() != null && m.getDescripcion().toLowerCase().contains(qLower))
                            || (m.getEstado() != null && m.getEstado().toLowerCase().contains(qLower)))
                    .toList();
        }

        request.setAttribute("materias", materias);
        request.setAttribute("query", query);
        request.getRequestDispatcher("/materias.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("crear".equals(action)) {
            String clave = request.getParameter("clave");
            String nombre = request.getParameter("nombre");
            String descripcion = request.getParameter("descripcion");

            if (materiaDao.getById(clave) != null) {
                response.sendRedirect(request.getContextPath() + "/materias?error=duplicado");
                return;
            }

            Materia materia = new Materia(clave, nombre, descripcion, "Activo");
            materiaDao.save(materia);
            response.sendRedirect(request.getContextPath() + "/materias?msg=creado");

        } else if ("toggle".equals(action)) {
            String clave = request.getParameter("clave");
            Materia m = materiaDao.getById(clave);
            if (m != null) {
                m.setEstado("Activo".equalsIgnoreCase(m.getEstado()) ? "Inactivo" : "Activo");
                materiaDao.update(m);
            }
            response.sendRedirect(request.getContextPath() + "/materias?msg=actualizado");

        } else if ("editar".equals(action)) {
            String clave = request.getParameter("clave");
            String nombre = request.getParameter("nombre");
            String descripcion = request.getParameter("descripcion");
            String estado = request.getParameter("estado");

            Materia m = materiaDao.getById(clave);
            if (m != null) {
                m.setNombre(nombre);
                m.setDescripcion(descripcion);
                if (estado != null) m.setEstado(estado);
                materiaDao.update(m);
            }
            response.sendRedirect(request.getContextPath() + "/materias?msg=actualizado");

        } else if ("eliminar".equals(action)) {
            String clave = request.getParameter("clave");
            materiaDao.delete(clave);
            response.sendRedirect(request.getContextPath() + "/materias?msg=eliminado");
        } else {
            response.sendRedirect(request.getContextPath() + "/materias");
        }
    }
}

