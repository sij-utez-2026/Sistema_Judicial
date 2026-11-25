package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.peritajes.model.Dictamen;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.DictamenDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;

import java.io.IOException;

// RF 4.3 Autoguardado cada 30 s y "Guardar borrador". Lo llama dictamen.js con fetch.
// URL: POST /peritajes/mis-casos/dictamen/autoguardado  con los campos id, contenido, version y manual
//      (como formulario, sin JSON; el token CSRF va en el encabezado, ver SIJ.encabezadosCsrf).
// Responde JSON (lo lee dictamen.js):
//   200 {"version": 4, "guardadoEn": "11:30:00"}
//   409 {"mensaje": "..."} si otra ventana guardó antes; 403 si no es suyo; 400 cualquier otro error.
// Permiso: PERITAJES_PERITO y PERITAJES_PERITO_ADMIN.
@WebServlet("/peritajes/mis-casos/dictamen/autoguardado")
public class DictamenAutoguardadoServlet extends HttpServlet {

    private final PeritajeDao peritajeDao = new PeritajeDao();
    private final DictamenDao dao = new DictamenDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // dictamen.js espera JSON: si la sesión terminó responde 401 en lugar de mandar al login
        if (PeritajesAuth.actual(request) == null) {
            responderJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    mensaje("Su sesión terminó. Copie su texto y vuelva a iniciar sesión"));
            return;
        }
        UsuarioPeritajes usuario = PeritajesAuth.exigirPerito(request, response);
        if (usuario == null) {
            return;                                     // ya mandó al login o respondió 403
        }
        Long id = Parametros.numero(request, "id");
        if (id == null) {
            responderJson(response, HttpServletResponse.SC_BAD_REQUEST, mensaje(PeritajeDao.NO_EXISTE));
            return;
        }
        Peritaje peritaje = peritajeDao.getById(id);
        String acceso = PeritajeDao.validarAcceso(peritaje, usuario);
        if (acceso != null) {
            responderJson(response, HttpServletResponse.SC_FORBIDDEN, mensaje(acceso));
            return;
        }

        Long version = Parametros.numero(request, "version");
        String error = dao.guardar(peritaje, usuario, Parametros.crudo(request, "contenido"),
                version == null ? null : version.intValue(), Parametros.booleano(request, "manual"));
        if (error == null) {
            Dictamen dictamen = dao.buscar(peritaje.getId());
            responderJson(response, HttpServletResponse.SC_OK, "{\"version\": " + dictamen.getVersion()
                    + ", \"guardadoEn\": " + textoJson(dictamen.getActualizadoEnHora()) + "}");
        } else if (DictamenDao.CAMBIOS_EN_OTRA_VENTANA.equals(error)) {
            responderJson(response, HttpServletResponse.SC_CONFLICT, mensaje(error));
        } else {
            responderJson(response, HttpServletResponse.SC_BAD_REQUEST, mensaje(error));
        }
    }

    private void responderJson(HttpServletResponse response, int estado, String json) throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    private String mensaje(String texto) {
        return "{\"mensaje\": " + textoJson(texto) + "}";
    }

    // Texto entre comillas para JSON (escapa comillas, diagonales invertidas y caracteres de control)
    private String textoJson(String texto) {
        StringBuilder json = new StringBuilder("\"");
        for (char c : (texto == null ? "" : texto).toCharArray()) {
            switch (c) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (c < 0x20) {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
        return json.append('"').toString();
    }
}
