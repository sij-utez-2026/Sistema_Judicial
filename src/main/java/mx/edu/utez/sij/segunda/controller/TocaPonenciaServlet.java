package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.model.Ponencia;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.segunda.service.PonenciaService;
import mx.edu.utez.sij.segunda.service.TocaService;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Registra una ponencia (RF 5.2).
 * URL: POST /segunda/tocas/ponencias  con  id (toca), fecha, hora
 * Vuelve al detalle de la toca.
 */
@WebServlet("/segunda/tocas/ponencias")
public class TocaPonenciaServlet extends HttpServlet {

    private final TocaService tocaService = Segunda.tocaService();
    private final PonenciaService ponenciaService = Segunda.ponenciaService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long tocaId = Parametros.numero(request, "id");
        if (tocaId == null) {
            Vistas.noEncontrado(response);
            return;
        }
        Optional<Toca> toca = tocaService.buscar(tocaId);
        if (toca.isEmpty()) {
            Vistas.noEncontrado(response);
            return;
        }

        // Verificar que el magistrado sea dueño de la toca (o admin)
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (SegundaPermisos.esMagistrado(usuario)
                && !usuario.getId().equals(toca.get().getMagistradoUsuarioId())) {
            Vistas.noEncontrado(response);
            return;
        }

        String fechaStr = Parametros.texto(request, "fecha");
        String horaStr = Parametros.texto(request, "hora");

        if (fechaStr == null || horaStr == null) {
            FlashMessage.error(request, "Indique la fecha y la hora de la ponencia.");
            Vistas.redirigir(request, response, "/segunda/tocas/detalle?id=" + tocaId);
            return;
        }

        try {
            LocalDate fecha = LocalDate.parse(fechaStr);
            LocalTime hora = LocalTime.parse(horaStr.length() == 5 ? horaStr + ":00" : horaStr);
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

            Ponencia p = ponenciaService.registrar(tocaId, fechaHora);
            FlashMessage.exito(request, "Ponencia registrada",
                    "Programada para el " + p.getFechaTexto() + " a las " + p.getHoraTexto() + ".");
        } catch (java.time.format.DateTimeParseException e) {
            FlashMessage.error(request, "La fecha u hora tienen un formato inválido.");
        } catch (ReglaNegocioException e) {
            FlashMessage.error(request, e.getMessage());
        }

        Vistas.redirigir(request, response, "/segunda/tocas/detalle?id=" + tocaId);
    }
}

