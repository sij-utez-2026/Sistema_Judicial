package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.model.EstadoToca;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.util.List;

/** Permite a SEGUNDA_ADMIN ejecutar reasignaciones directas de tocas abiertas. */
@WebServlet("/segunda/tocas/reasignar")
public class ReasignarTocaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (usuario == null || !SegundaPermisos.esAdmin(usuario)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<Toca> tocas = Segunda.tocaService().listar(null, null, null, null, 0, 100)
                .getContent().stream()
                .filter(t -> t.getEstado() != EstadoToca.RESUELTA)
                .toList();

        request.setAttribute("usuario", usuario);
        request.setAttribute("salas", Segunda.salaService().activas());
        request.setAttribute("tocasAbiertas", tocas);
        Vistas.mostrar(request, response, "segunda/tocas/reasignar");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (usuario == null || !SegundaPermisos.esAdmin(usuario)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Long tocaId = Parametros.numero(request, "tocaId");
        Long salaId = Parametros.numero(request, "salaId");
        String motivo = Parametros.texto(request, "motivo");
        if (tocaId == null || salaId == null || motivo == null
                || motivo.isBlank() || motivo.trim().length() > 300) {
            FlashMessage.error(request,
                    "Seleccione la toca y la sala destino e indique un motivo de hasta 300 caracteres.");
            Vistas.redirigir(request, response, "/segunda/tocas/reasignar");
            return;
        }

        try {
            ReasignacionResultado resultado = ejecutarReasignacion(tocaId, salaId, motivo);
            Segunda.tocaService().registrarHistorial(resultado.tocaId(), "Reasignación administrativa",
                    "Sala " + resultado.salaOrigenId() + " → sala " + resultado.salaDestinoId());
            Segunda.registrarBitacora(request, usuario, "reasignacion_Seg", resultado.reasignacionId(),
                    "Reasignación administrativa ejecutada",
                    "Toca " + resultado.tocaId() + "; sala origen " + resultado.salaOrigenId()
                            + "; sala destino " + resultado.salaDestinoId()
                            + "; motivo: " + motivo.trim(), "gold");
            FlashMessage.exito(request, "Reasignación realizada",
                    "La sala y el magistrado de la toca fueron actualizados.");
        } catch (IllegalArgumentException e) {
            FlashMessage.error(request, e.getMessage());
        }
        Vistas.redirigir(request, response, "/segunda/tocas/reasignar");
    }

    private ReasignacionResultado ejecutarReasignacion(Long tocaId, Long salaId, String motivo) {
        var resultado = Segunda.reasignacionDao().reasignar(tocaId, salaId, motivo);
        return new ReasignacionResultado(resultado.reasignacionId(), resultado.tocaId(),
                resultado.salaOrigenId(), resultado.salaDestinoId());
    }

    private record ReasignacionResultado(long reasignacionId, long tocaId,
                                         long salaOrigenId, long salaDestinoId) {
    }
}
