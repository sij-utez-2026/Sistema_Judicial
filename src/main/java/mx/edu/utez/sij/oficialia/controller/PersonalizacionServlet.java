package mx.edu.utez.sij.oficialia.controller;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.sij.oficialia.model.Usuario;
import mx.edu.utez.sij.oficialia.model.dao.UsuarioDao;

import java.io.IOException;

@WebServlet(name = "PersonalizacionServlet", value = "/personalizacion")
public class PersonalizacionServlet extends HttpServlet {

    private final UsuarioDao usuarioDao = new UsuarioDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/personalizacion.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario != null) {
                String nombre = request.getParameter("nombre");
                String contrasena = request.getParameter("contrasena");

                if (nombre != null && !nombre.trim().isEmpty()) {
                    usuario.setNombre(nombre.trim());
                    String[] parts = nombre.trim().split("\\s+");
                    if (parts.length >= 2) {
                        usuario.setIniciales(("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase());
                    }
                }
                if (contrasena != null && !contrasena.trim().isEmpty()) {
                    usuario.setContrasena(contrasena.trim());
                }

                usuarioDao.update(usuario);
                session.setAttribute("usuario", usuario);
                response.sendRedirect(request.getContextPath() + "/personalizacion?msg=ok");
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/personalizacion");
    }
}


