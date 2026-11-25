package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.nucleo.acceso.Contrasena;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.PeritoDao;

import java.io.IOException;
import java.util.Map;

// RF 3.1 Registro de perito. Se elige si será Perito o Perito Administrador (N1).
// El sistema genera una contraseña temporal y se la muestra al administrador UNA sola vez (M2),
// porque todavía no se envían correos. El perito la cambia en "Cambiar contraseña".
// URL: GET /peritajes/peritos/nuevo (formulario) · POST /peritajes/peritos/nuevo (guardar)
// Vista: WEB-INF/jsp/peritajes/peritos/registro.jsp
// Permiso: PERITAJES_ADMIN y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/peritos/nuevo")
public class PeritoRegistrarServlet extends HttpServlet {

    private final PeritoDao dao = new PeritoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PeritajesAuth.exigirAdministrador(request, response) == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        request.setAttribute("peritoForm", PeritoFormSupport.nuevo());
        PeritoFormSupport.datosDelFormulario(request);
        Vistas.mostrar(request, response, "peritajes/peritos/registro");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioPeritajes usuario = PeritajesAuth.exigirAdministrador(request, response);
        if (usuario == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        Perito perito = PeritoFormSupport.leer(request);
        Map<String, String> errores = Validador.validar(perito);

        if (errores.isEmpty()) {
            String error = PeritoFormSupport.validar(perito, true);
            String temporal = Contrasena.generarTemporal();
            if (error == null) {
                error = dao.create(perito, Contrasena.cifrar(temporal), usuario);
            }
            if (error == null) {
                // RF 3.1: confirmación de registro exitoso, en estado Activo
                FlashMessage.exito(request, "Registro exitoso", perito.getNombreCompleto() + " · "
                        + perito.getNumeroRegistro() + " (" + perito.getRol() + ") quedó registrado en estado Activo.");
                // M2: la contraseña temporal se muestra una sola vez (no se guarda en ningún lado sin cifrar)
                FlashMessage.agregar(request, "correoNuevo", perito.getCorreo());
                FlashMessage.agregar(request, "contrasenaTemporal", temporal);
                Vistas.redirigir(request, response, "/peritajes/peritos");
                return;
            }
            request.setAttribute("error", error);
        }

        // Con errores: se vuelve a mostrar el formulario con lo que capturó
        request.setAttribute("peritoForm", perito);
        request.setAttribute("errores", errores);
        PeritoFormSupport.datosDelFormulario(request);
        Vistas.mostrar(request, response, "peritajes/peritos/registro");
    }
}
