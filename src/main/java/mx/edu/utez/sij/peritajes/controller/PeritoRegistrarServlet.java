package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.PeritoDao;

import java.io.IOException;
import java.util.Map;

// RF 3.1 Registro de perito.
// URL: GET /peritajes/peritos/nuevo (formulario) · POST /peritajes/peritos/nuevo (guardar)
// Vista: WEB-INF/jsp/peritajes/peritos/registro.jsp
// Permiso: PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN (hoy con el usuario de "Ver como", PeritajesAuth).
@WebServlet("/peritajes/peritos/nuevo")
public class PeritoRegistrarServlet extends HttpServlet {

    private final PeritoDao dao = new PeritoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PeritajesAuth.exigirAdministrador(request, response) == null) {
            return;                                     // ya respondió 403
        }
        request.setAttribute("peritoForm", PeritoFormSupport.nuevo());
        PeritoFormSupport.datosDelFormulario(request);
        Vistas.mostrar(request, response, "peritajes/peritos/registro");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirAdministrador(request, response);
        if (usuario == null) {
            return;                                     // ya respondió 403
        }
        Perito perito = PeritoFormSupport.leer(request);
        Map<String, String> errores = Validador.validar(perito);

        if (errores.isEmpty()) {
            String error = PeritoFormSupport.validar(perito, true);
            if (error == null && dao.create(perito, usuario)) {
                // RF 3.1: confirmación de registro exitoso, en estado Activo
                FlashMessage.exito(request, "Registro exitoso", perito.getNombreCompleto() + " · "
                        + perito.getNumeroRegistro() + " quedó registrado en estado Activo.");
                Vistas.redirigir(request, response, "/peritajes/peritos");
                return;
            }
            request.setAttribute("error", error != null ? error : "No se pudo registrar el perito. Intente de nuevo");
        }

        // Con errores: se vuelve a mostrar el formulario con lo que capturó
        request.setAttribute("peritoForm", perito);
        request.setAttribute("errores", errores);
        PeritoFormSupport.datosDelFormulario(request);
        Vistas.mostrar(request, response, "peritajes/peritos/registro");
    }
}
