package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.segunda.dao.PonenciaDao;
import mx.edu.utez.sij.segunda.model.EstadoPonencia;
import mx.edu.utez.sij.segunda.model.EstadoToca;
import mx.edu.utez.sij.segunda.model.Ponencia;
import mx.edu.utez.sij.segunda.model.Toca;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Ponencias (RF 5.2). Reglas en docs/modulos/segunda-instancia.md, sección 6.4.
 *
 * Reglas de negocio:
 * - La fecha y hora deben ser futuras.
 * - No puede chocar con otra ponencia del mismo magistrado (ventana ±1 hora).
 * - Solo se registran ponencias en tocas NO finales.
 * - La primera ponencia cambia la toca a EN_PONENCIA.
 */
public class PonenciaService {

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    private static final int MARGEN_CHOQUE_HORAS = 1;

    private final PonenciaDao ponenciaDao;
    private final TocaService tocaService;

    public PonenciaService(PonenciaDao ponenciaDao, TocaService tocaService) {
        this.ponenciaDao = ponenciaDao;
        this.tocaService = tocaService;
    }

    /** Registra una nueva ponencia en la toca. */
    public Ponencia registrar(Long tocaId, LocalDateTime fechaHora) {
        Toca toca = tocaService.buscar(tocaId)
                .orElseThrow(() -> new ReglaNegocioException("La toca no existe"));

        if (toca.getEstado().esFinal()) {
            throw new ReglaNegocioException(
                    "No se puede registrar una ponencia en una toca " + toca.getEstado().getEtiqueta());
        }
        if (toca.getMagistradoId() == null) {
            throw new ReglaNegocioException("La toca no tiene un magistrado de Segunda Instancia asignado");
        }
        if (fechaHora == null) {
            throw new ReglaNegocioException("La fecha y hora de la ponencia son obligatorias");
        }
        if (!fechaHora.isAfter(LocalDateTime.now(ZONA))) {
            throw new ReglaNegocioException("La fecha y hora de la ponencia deben ser futuras");
        }
        if (!fechaHora.toLocalTime().plusHours(Ponencia.DURACION_HORAS).isAfter(fechaHora.toLocalTime())) {
            throw new ReglaNegocioException("La ponencia de una hora debe terminar el mismo día; seleccione un horario anterior a las 23:00");
        }

        // Choque de horario: mismo magistrado, ventana ±MARGEN_CHOQUE_HORAS.
        // Si findByMagistradoEntre devuelve algo, es un choque real: no hay que filtrar más.
        Long magistrado = toca.getMagistradoId();
        LocalDateTime desde = fechaHora.minusHours(MARGEN_CHOQUE_HORAS);
        LocalDateTime hasta = fechaHora.plusHours(MARGEN_CHOQUE_HORAS);
        List<Ponencia> choques = ponenciaDao.findByMagistradoEntre(magistrado, desde, hasta);
        if (!choques.isEmpty()) {
            throw new ReglaNegocioException(
                    "El magistrado ya tiene una ponencia en ese horario (±"
                            + MARGEN_CHOQUE_HORAS + " h). Elija otra fecha u hora.");
        }

        Ponencia p = new Ponencia();
        p.setTocaId(tocaId);
        p.setMagistradoId(magistrado);
        p.setFechaHora(fechaHora);
        p.setEstado(EstadoPonencia.PROGRAMADA);
        boolean primera = toca.getEstado() == EstadoToca.ASIGNADA;
        String detalle = (primera ? "Primera ponencia registrada" : "Ponencia registrada")
                + " · programada para el " + p.getFechaTexto() + " a las " + p.getHoraTexto();
        if (!ponenciaDao.registrarAtomico(p, primera, detalle)) {
            ponenciaDao.save(p);
            if (primera) tocaService.cambiarEstado(tocaId, EstadoToca.EN_PONENCIA, detalle);
            else tocaService.registrarHistorial(tocaId, detalle, null);
        }

        return p;
    }

    public List<Ponencia> deToca(Long tocaId) {
        List<Ponencia> lista = ponenciaDao.findByTocaId(tocaId);
        for (Ponencia p : lista) {
            tocaService.buscar(p.getTocaId())
                    .ifPresent(t -> p.setTocaFolio(t.getFolioToca()));
        }
        return lista;
    }

    public Optional<Ponencia> buscar(Long id) {
        return ponenciaDao.findById(id);
    }

    public boolean tienePonenciaHoy(Long tocaId) {
        return ponenciaDao.tienePonenciaHoy(tocaId);
    }

    public List<Ponencia> entre(LocalDateTime desde, LocalDateTime hasta) {
        return ponenciaDao.entre(desde, hasta);
    }
}
