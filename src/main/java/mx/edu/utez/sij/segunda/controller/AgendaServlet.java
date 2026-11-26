package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.dto.EventoAgenda;
import mx.edu.utez.sij.segunda.service.AgendaService;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/segunda/agenda")
public class AgendaServlet extends HttpServlet {

    private final AgendaService agendaService = Segunda.agendaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion usuario = Segunda.usuarioActual(request);

        String vista = Parametros.texto(request, "vista");
        if (vista == null) vista = "semanal";

        LocalDate fecha;
        try {
            String f = Parametros.texto(request, "fecha");
            fecha = (f == null) ? LocalDate.now() : LocalDate.parse(f);
        } catch (DateTimeParseException e) {
            fecha = LocalDate.now();
        }

        Long salaId = Parametros.numero(request, "salaId");
        Long magistradoId = Parametros.numero(request, "magistradoId");

        LocalDate desde, hasta;
        if ("mensual".equals(vista)) {
            desde = AgendaService.inicioSemana(AgendaService.inicioMes(fecha));
            hasta = AgendaService.finSemana(AgendaService.finMes(fecha));
        } else {
            desde = AgendaService.inicioSemana(fecha);
            hasta = AgendaService.finSemana(fecha);
        }

        List<EventoAgenda> eventos =
                agendaService.eventos(usuario, desde, hasta, salaId, magistradoId);

        Map<LocalDate, List<EventoAgenda>> porDia = new LinkedHashMap<>();
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
            porDia.put(d, new ArrayList<>());
        }
        for (EventoAgenda e : eventos) {
            List<EventoAgenda> lista = porDia.get(e.fechaHora().toLocalDate());
            if (lista != null) lista.add(e);
        }

        LocalDate anterior = "mensual".equals(vista)
                ? fecha.minusMonths(1) : fecha.minusWeeks(1);
        LocalDate siguiente = "mensual".equals(vista)
                ? fecha.plusMonths(1) : fecha.plusWeeks(1);

        request.setAttribute("usuario", usuario);
        request.setAttribute("vista", vista);
        request.setAttribute("fecha", fecha);
        request.setAttribute("desde", desde);
        request.setAttribute("hasta", hasta);
        request.setAttribute("fechaAnterior", anterior);
        request.setAttribute("fechaSiguiente", siguiente);
        request.setAttribute("mesActual", fecha.getMonthValue());
        request.setAttribute("anioActual", fecha.getYear());
        request.setAttribute("porDia", porDia);
        request.setAttribute("totalEventos", eventos.size());

        if (!SegundaPermisos.esMagistrado(usuario)) {
            request.setAttribute("salas", Segunda.salaService().activas());
            request.setAttribute("magistrados", Segunda.magistradoService().activos());
        }
        request.setAttribute("salaId", salaId);
        request.setAttribute("magistradoId", magistradoId);

        Vistas.mostrar(request, response, "segunda/agenda/lista");
    }
}


