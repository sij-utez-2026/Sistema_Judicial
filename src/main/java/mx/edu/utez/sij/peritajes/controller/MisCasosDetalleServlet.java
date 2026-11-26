package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.EstadoPeritaje;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.DictamenDao;
import mx.edu.utez.sij.peritajes.model.dao.ExpedienteDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;

import java.io.IOException;

// RF 4.2 Consulta de expediente (la primera apertura pasa a En Proceso) y RF 4.3 editor del dictamen.
// El expediente y el editor van en la misma pantalla, como en el boceto.
// URL: GET /peritajes/mis-casos/detalle?id=5
// Vista: WEB-INF/jsp/peritajes/mis-casos/expediente.jsp
// Las acciones del dictamen están en los servlets Dictamen...Servlet.
// Permiso: PERITAJES_PERITO y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/mis-casos/detalle")
public class MisCasosDetalleServlet extends HttpServlet {

    private final PeritajeDao dao = new PeritajeDao();
    private final DictamenDao dictamenDao = new DictamenDao();
    private final ExpedienteDao expedienteDao = new ExpedienteDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirPerito(request, response);
        if (usuario == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        Long id = Parametros.numero(request, "id");
        if (id == null) {
            Vistas.noEncontrado(response);
            return;
        }

        Peritaje peritaje = dao.getById(id);
        String error = dao.abrirExpediente(peritaje, usuario);
        if (error != null) {
            // RF 4.2: si no es suyo se deniega el acceso con el mensaje
            FlashMessage.error(request, error);
            Vistas.redirigir(request, response, "/peritajes/mis-casos");
            return;
        }

        request.setAttribute("peritaje", peritaje);
        request.setAttribute("expediente", expedienteDao.buscar(peritaje.getFolioDemanda()));
        request.setAttribute("dictamen", dictamenDao.paraElPerito(peritaje));
        request.setAttribute("soloLectura", peritaje.getEstado() == EstadoPeritaje.DICTAMEN_ENTREGADO);
        Vistas.mostrar(request, response, "peritajes/mis-casos/expediente");
    }
}
