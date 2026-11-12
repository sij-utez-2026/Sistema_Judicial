package mx.edu.utez.sij.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.oficialia.model.Juzgado;
import mx.edu.utez.sij.oficialia.model.dao.DistritoDao;
import mx.edu.utez.sij.oficialia.model.dao.JuzgadoDao;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "JuzgadoServlet", value = "/juzgados.jsp")
public class JuzgadoServlet extends HttpServlet {

    private final JuzgadoDao juzgadoDao = new JuzgadoDao();
    private final DistritoDao distritoDao = new DistritoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("q");
        List<Juzgado> juzgados = juzgadoDao.getAll();

        if (query != null && !query.trim().isEmpty()) {
            String qLower = query.toLowerCase().trim();
            juzgados = juzgados.stream()
                    .filter(j -> j.getClave().toLowerCase().contains(qLower)
                            || j.getNombre().toLowerCase().contains(qLower)
                            || j.getDistrito().toLowerCase().contains(qLower)
                            || (j.getEstado() != null && j.getEstado().toLowerCase().contains(qLower)))
                    .toList();
        }

        request.setAttribute("juzgados", juzgados);
        request.setAttribute("distritos", distritoDao.getAll());
        request.setAttribute("query", query);
        request.getRequestDispatcher("/juzgados.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("crear".equals(action)) {
            String clave = request.getParameter("clave");
            String nombre = request.getParameter("nombre");
            String distrito = request.getParameter("distrito");

            if (juzgadoDao.getById(clave) != null) {
                response.sendRedirect(request.getContextPath() + "/juzgados.jsp?error=duplicado");
                return;
            }

            Juzgado juzgado = new Juzgado(clave, nombre, distrito, "Activo");
            juzgadoDao.save(juzgado);
            response.sendRedirect(request.getContextPath() + "/juzgados.jsp?msg=creado");

        } else if ("toggle".equals(action)) {
            String clave = request.getParameter("clave");
            Juzgado j = juzgadoDao.getById(clave);
            if (j != null) {
                j.setEstado("Activo".equalsIgnoreCase(j.getEstado()) ? "Inactivo" : "Activo");
                juzgadoDao.update(j);
            }
            response.sendRedirect(request.getContextPath() + "/juzgados.jsp?msg=actualizado");

        } else if ("editar".equals(action)) {
            String clave = request.getParameter("clave");
            String nombre = request.getParameter("nombre");
            String distrito = request.getParameter("distrito");
            String estado = request.getParameter("estado");

            Juzgado j = juzgadoDao.getById(clave);
            if (j != null) {
                j.setNombre(nombre);
                j.setDistrito(distrito);
                if (estado != null) j.setEstado(estado);
                juzgadoDao.update(j);
            }
            response.sendRedirect(request.getContextPath() + "/juzgados.jsp?msg=actualizado");

        } else if ("eliminar".equals(action)) {
            String clave = request.getParameter("clave");
            juzgadoDao.delete(clave);
            response.sendRedirect(request.getContextPath() + "/juzgados.jsp?msg=eliminado");
        } else {
            response.sendRedirect(request.getContextPath() + "/juzgados.jsp");
        }
    }
}
