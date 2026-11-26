package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;
import mx.edu.utez.sij.segunda.controller.SegundaPermisos;
import mx.edu.utez.sij.segunda.dto.EventoAgenda;
import mx.edu.utez.sij.segunda.model.Ponencia;
import mx.edu.utez.sij.segunda.model.Toca;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AgendaService {

    private final PonenciaService ponenciaService;
    private final TocaService tocaService;

    public AgendaService(PonenciaService ponenciaService, TocaService tocaService) {
        this.ponenciaService = ponenciaService;
        this.tocaService = tocaService;
    }

    public static LocalDate inicioSemana(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate finSemana(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public static LocalDate inicioMes(LocalDate fecha) {
        return fecha.withDayOfMonth(1);
    }

    public static LocalDate finMes(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.lastDayOfMonth());
    }

    public List<EventoAgenda> eventos(UsuarioSesion usuario,
                                      LocalDate desde,
                                      LocalDate hasta,
                                      Long salaIdFiltro,
                                      Long magistradoFiltro) {
        LocalDateTime ini = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(23, 59, 59);

        Long soloDeMagistrado = SegundaPermisos.esMagistrado(usuario)
                ? usuario.getId() : magistradoFiltro;

        List<EventoAgenda> eventos = new ArrayList<>();

        for (Ponencia p : ponenciaService.entre(ini, fin)) {
            Toca toca = tocaService.buscar(p.getTocaId()).orElse(null);
            if (toca == null) continue;

            if (soloDeMagistrado != null
                    && !soloDeMagistrado.equals(toca.getMagistradoUsuarioId())) {
                continue;
            }
            if (salaIdFiltro != null && !salaIdFiltro.equals(toca.getSalaId())) {
                continue;
            }

            eventos.add(new EventoAgenda(
                    p.getId(),
                    toca.getId(),
                    toca.getFolioToca(),
                    toca.getNumeroExpediente(),
                    toca.getSalaNombre(),
                    toca.getMagistradoNombre(),
                    p.getFechaHora(),
                    p.getEstado() == null ? "" : p.getEstado().name()
            ));
        }

        eventos.sort(Comparator.comparing(EventoAgenda::fechaHora));
        return eventos;
    }
}

