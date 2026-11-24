package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.PeritoDao;

import java.io.IOException;
import java.util.Map;

// RF 3.3 Edición de perito (en la misma pantalla está el cambio de estado, RF 3.4).
// URL: GET /peritajes/peritos/editar?id=5 (formulario) · POST /peritajes/peritos/editar?id=5 (guardar)
// Vista: WEB-INF/jsp/peritajes/peritos/editar.jsp
// Permiso: PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/peritos/editar")
public class PeritoEditarServlet extends HttpServlet {

    private final PeritoDao dao = new PeritoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PeritajesAuth.exigirAdministrador(request, response) == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        Perito perito = dao.getById(Parametros.numero(request, "id"));
        if (perito == null) {
            Vistas.noEncontrado(response);
            return;
        }
        request.setAttribute("peritoForm", PeritoFormSupport.copiaParaEditar(perito));
        mostrar(request, response, perito);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirAdministrador(request, response);
        if (usuario == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        Perito perito = dao.getById(Parametros.numero(request, "id"));
        if (perito == null) {
            Vistas.noEncontrado(response);
            return;
        }

        Perito datos = PeritoFormSupport.leer(request);
        // El correo y el rol no se editan: se usan los guardados (también para que pase la validación)
        datos.setId(perito.getId());
        datos.setCorreo(perito.getCorreo());
        datos.setRolSistema(perito.getRolSistema());
        Map<String, String> errores = Validador.validar(datos);
        if (errores.isEmpty()) {
            String error = PeritoFormSupport.validar(datos, false);
            if (error == null) {
                error = dao.update(datos, usuario);
            }
            if (error == null) {
                FlashMessage.exito(request, "Datos actualizados correctamente", datos.getNombreCompleto() + " · "
                        + perito.getNumeroRegistro() + " actualizado. Su estado permanece "
                        + (perito.isActivo() ? "Activo." : "Inactivo."));
                Vistas.redirigir(request, response, "/peritajes/peritos");
                return;
            }
            request.setAttribute("error", error);
        }

        request.setAttribute("peritoForm", datos);
        request.setAttribute("errores", errores);
        mostrar(request, response, perito);
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response, Perito perito)
            throws ServletException, IOException {
        request.setAttribute("perito", perito);
        request.setAttribute("peritajesActivos", dao.peritajesActivos(perito.getId()));
        PeritoFormSupport.datosDelFormulario(request);
        Vistas.mostrar(request, response, "peritajes/peritos/editar");
    }
}
