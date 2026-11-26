package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.EstadoPeritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.NotificacionDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritoDao;

import java.io.IOException;
import java.util.List;

// Inicio del módulo (/peritajes): el dashboard de cada rol (DFR: "cada perfil tendrá su propio dashboard
// con solo las opciones que le corresponden"), en tarjetas:
// - Administrador: peritajes por estado, peritos activos e inactivos, y accesos.
// - Perito: sus peritajes Asignados, En Proceso y Entregados, y los que tiene activos.
// - Perito Administrador: las dos cosas.
// - Administrador general: lo mismo que el Administrador (no atiende casos ni ve dictámenes).
// - Todos los usuarios de Peritajes: sus avisos (RF 5.1).
// Antes recibe las solicitudes nuevas de Audiencias y las asigna (RF 2.1 y 2.2).
// Vista: WEB-INF/jsp/peritajes/inicio.jsp
@WebServlet("/peritajes")
public class InicioPeritajesServlet extends HttpServlet {

    private static final int AVISOS = 5;

    private final PeritajeDao peritajeDao = new PeritajeDao();
    private final PeritoDao peritoDao = new PeritoDao();
    private final NotificacionDao notificacionDao = new NotificacionDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirSesion(request, response);
        if (usuario == null) {
            return;
        }
        peritajeDao.recibirSolicitudes();

        if (usuario.isAdministra()) {
            // Llaves de texto ("ASIGNADO") para leerlas en la JSP: ${porEstado['ASIGNADO']}
            request.setAttribute("porEstado", peritajeDao.contarPorEstado());
            request.setAttribute("peritosActivos", peritoDao.contarPorEstado(true));
            request.setAttribute("peritosInactivos", peritoDao.contarPorEstado(false));
        }
        if (usuario.isAtiendeCasos()) {
            request.setAttribute("misAsignados", peritajeDao.contarDelPerito(usuario, EstadoPeritaje.ASIGNADO));
            request.setAttribute("misEnProceso", peritajeDao.contarDelPerito(usuario, EstadoPeritaje.EN_PROCESO));
            request.setAttribute("misEntregados", peritajeDao.contarDelPerito(usuario, EstadoPeritaje.DICTAMEN_ENTREGADO));
            request.setAttribute("misActivos", peritajeDao.misPeritajes(usuario, null, "activos", 0).getContent());
        }
        // El administrador general no está en user_Per: no recibe avisos de Peritajes
        request.setAttribute("avisos", usuario.getId() == null ? List.of() : notificacionDao.recientes(usuario.getId(), AVISOS));
        Vistas.mostrar(request, response, "peritajes/inicio");
    }
}
