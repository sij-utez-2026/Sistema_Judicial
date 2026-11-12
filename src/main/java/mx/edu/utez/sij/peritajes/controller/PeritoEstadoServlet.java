package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.PeritoDao;

import java.io.IOException;

// RF 3.4 Baja o reactivación de perito (no se borra).
// URL: POST /peritajes/peritos/estado  con  id=5 y activo=true|false
// Regresa a la pantalla de edición con el resultado.
// Permiso: PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN (hoy con el usuario de "Ver como", PeritajesAuth).
@WebServlet("/peritajes/peritos/estado")
public class PeritoEstadoServlet extends HttpServlet {

    private final PeritoDao dao = new PeritoDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirAdministrador(request, response);
        if (usuario == null) {
            return;                                     // ya respondió 403
        }
        Long id = Parametros.numero(request, "id");
        if (dao.getById(id) == null) {
            Vistas.noEncontrado(response);
            return;
        }

        String error = dao.cambiarEstado(id, Parametros.booleano(request, "activo"), usuario);
        if (error == null) {
            Perito perito = dao.getById(id);
            // RF 3.4: confirmación de baja o reactivación
            FlashMessage.exito(request, perito.isActivo() ? "Perito reactivado" : "Perito dado de baja",
                    perito.getNombreCompleto() + " · " + perito.getNumeroRegistro()
                            + (perito.isActivo() ? " vuelve a participar en la asignación automática."
                                                 : " quedó Inactivo y ya no recibe asignaciones."));
        } else {
            FlashMessage.agregar(request, "errorEstado", error);
        }
        Vistas.redirigir(request, response, "/peritajes/peritos/editar?id=" + id);
    }
}
