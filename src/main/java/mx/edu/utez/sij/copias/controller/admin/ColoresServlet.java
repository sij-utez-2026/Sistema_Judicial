package mx.edu.utez.sij.copias.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.copias.Copias;
import mx.edu.utez.sij.copias.RolesCopias;
import mx.edu.utez.sij.copias.sesion.UsuarioCopias;
import mx.edu.utez.sij.copias.dto.ColoresForm;
import mx.edu.utez.sij.copias.service.ColoresCopiasService;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.nucleo.web.Vistas;

import java.io.IOException;
import java.util.Map;

/**
 * Colores de las pantallas de Copias (COPIAS_ADMIN). Solo cambian este módulo.
 * URL: GET /copias/admin/colores · POST /copias/admin/colores (guardar)
 */
@WebServlet("/copias/admin/colores")
public class ColoresServlet extends HttpServlet {

    private final ColoresCopiasService coloresService = Copias.coloresService();
    private final AccesoTemporal acceso = Copias.acceso();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.ADMIN);
        if (usuario == null) {
            return;
        }
        request.setAttribute("coloresForm", coloresService.formulario());
        mostrar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioCopias usuario = acceso.exigirRol(request, response, RolesCopias.ADMIN);
        if (usuario == null) {
            return;
        }
        ColoresForm form = new ColoresForm();
        form.setPrimario(Parametros.texto(request, "primario"));
        form.setTitulos(Parametros.texto(request, "titulos"));
        form.setBotones(Parametros.texto(request, "botones"));
        form.setAcento(Parametros.texto(request, "acento"));
        form.setBordes(Parametros.texto(request, "bordes"));
        form.setAvisos(Parametros.texto(request, "avisos"));
        Map<String, String> errores = Validador.validar(form);

        if (errores.isEmpty()) {
            try {
                coloresService.guardar(form, usuario.getId());
                FlashMessage.exito(request, "Colores actualizados correctamente", null);
                Vistas.redirigir(request, response, "/copias/admin/colores");
                return;
            } catch (ReglaNegocioException e) {
                request.setAttribute("error", e.getMessage());
            }
        }
        request.setAttribute("coloresForm", form);
        request.setAttribute("errores", errores);
        mostrar(request, response);
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("coloresIniciales", coloresService.formularioInicial());
        Vistas.mostrar(request, response, "copias/admin/colores");
    }
}
