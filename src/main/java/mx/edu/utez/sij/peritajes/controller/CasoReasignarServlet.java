package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;

import java.io.IOException;

// RF 2.3 Reasignación manual con justificación.
// URL: POST /peritajes/casos/reasignar  con  id, peritoId y motivo
// Regresa al detalle del peritaje con el resultado.
// Permiso: PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/casos/reasignar")
public class CasoReasignarServlet extends HttpServlet {

    private final PeritajeDao dao = new PeritajeDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirAdministrador(request, response);
        if (usuario == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        Long id = Parametros.numero(request, "id");
        if (dao.getById(id) == null) {
            Vistas.noEncontrado(response);
            return;
        }
        String motivo = Parametros.crudo(request, "motivo");

        String error = dao.reasignar(id, Parametros.numero(request, "peritoId"), motivo, usuario);
        if (error == null) {
            Peritaje peritaje = dao.getById(id);
            // RF 2.3: mensaje de confirmación de asignación exitosa
            FlashMessage.exito(request, "Reasignación exitosa", "El peritaje " + peritaje.getFolio()
                    + " ahora está asignado a " + peritaje.getPeritoNombreTexto() + ". Estado conservado: "
                    + peritaje.getEstado().getTexto() + ".");
        } else {
            FlashMessage.error(request, error);
            // Para volver a abrir la ventana con lo que se había escrito
            FlashMessage.agregar(request, "motivoCapturado", motivo);
            FlashMessage.agregar(request, "abrirReasignacion", true);
        }
        Vistas.redirigir(request, response, "/peritajes/casos/detalle?id=" + id);
    }
}
