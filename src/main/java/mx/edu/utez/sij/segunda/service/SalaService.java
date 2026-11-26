package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.contratos.segunda.SalaConsultaApi;
import mx.edu.utez.sij.nucleo.contratos.segunda.SalaDisponibleDto;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.dao.MagistradoDao;
import mx.edu.utez.sij.segunda.dao.SalaDao;
import mx.edu.utez.sij.segunda.dao.TocaDao;
import mx.edu.utez.sij.segunda.dto.SalaForm;
import mx.edu.utez.sij.segunda.model.Magistrado;
import mx.edu.utez.sij.segunda.model.Sala;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Salas (RF 3.1 – 3.4). Reglas en docs/modulos/segunda-instancia.md, sección 6.1.
 *
 * Implementa SalaConsultaApi (docs/CONTRATOS.md, 6.1) para que Oficialía pueda listar
 * las salas disponibles al asignar una toca.
 *
 * Fuente de verdad de la relación magistrado ↔ sala: magistrado_Seg.id_sala.
 * Sala.magistradoUsuarioId y Magistrado.salaId son valores derivados de esa relación.
 */
public class SalaService implements SalaConsultaApi {

    public static final int TAMANO_PAGINA = 25;
    public static final List<Integer> TAMANOS_PAGINA = List.of(10, 25, 50, 100);

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    private final SalaDao salaDao;
    private final TocaDao tocaDao;
    private final MagistradoDao magistradoDao;
    private final CatalogoTemporalSegunda catalogo;

    public SalaService(SalaDao salaDao, TocaDao tocaDao, MagistradoDao magistradoDao,
                       CatalogoTemporalSegunda catalogo) {
        this.salaDao = salaDao;
        this.tocaDao = tocaDao;
        this.magistradoDao = magistradoDao;
        this.catalogo = catalogo;
    }

    // ---------- Listados ----------

    public Pagina<Sala> listar(String texto, String distritoId, Boolean activa, int pagina, int tamano) {
        int tamanoValido = TAMANOS_PAGINA.contains(tamano) ? tamano : TAMANO_PAGINA;
        Pagina<Sala> resultado = salaDao.buscar(vacioANull(texto), distritoId, activa,
                Math.max(pagina, 0), tamanoValido);
        for (Sala sala : resultado.getContent()) {
            enriquecer(sala);
        }
        return resultado;
    }

    public Optional<Sala> buscar(Long id) {
        Optional<Sala> sala = salaDao.findById(id);
        sala.ifPresent(this::enriquecer);
        return sala;
    }

    public List<Sala> activas() {
        List<Sala> lista = salaDao.activas();
        for (Sala sala : lista) enriquecer(sala);
        return lista;
    }

    // ---------- SalaConsultaApi ----------

    @Override
    public List<SalaDisponibleDto> salasDisponibles(String distritoId) {
        return salaDao.activas().stream()
                .filter(s -> distritoId == null || distritoId.equals(s.getDistritoId()))
                .filter(s -> s.getMagistradoUsuarioId() != null)
                .map(s -> new SalaDisponibleDto(
                        s.getId(),
                        s.getNombre(),
                        s.getDistritoId(),
                        s.getDireccion(),
                        s.getMagistradoUsuarioId(),
                        nombreDeMagistrado(s.getMagistradoUsuarioId())))
                .toList();
    }

    // ---------- Altas, cambios y bajas ----------

    /** RF 3.1 Registro. */
    public Sala registrar(SalaForm form) {
        validarDistrito(form.getDistritoId());
        String nombre = form.getNombre().trim();
        if (salaDao.existsByNombreAndDistrito(nombre, form.getDistritoId(), null)) {
            throw new ReglaNegocioException(
                    "Ya existe una sala con el nombre \"" + nombre + "\" en ese distrito");
        }
        if (form.getMagistradoUsuarioId() != null) {
            validarMagistradoUnico(form.getMagistradoUsuarioId(), null);
        }

        Sala sala = new Sala();
        sala.setNombre(nombre);
        sala.setDistritoId(form.getDistritoId());
        sala.setDireccion(form.getDireccion().trim());
        sala.setMagistradoUsuarioId(form.getMagistradoUsuarioId());
        sala.setActiva(true);
        sala.setCreadaEn(LocalDateTime.now(ZONA));
        Sala guardada = salaDao.save(sala);
        sincronizarMagistrado(guardada);
        enriquecer(guardada);
        return guardada;
    }

    /** RF 3.3 Edición. No cambia el distrito si la sala tiene tocas asignadas. */
    public Sala actualizar(Long id, SalaForm form) {
        Sala sala = obtener(id);
        validarDistrito(form.getDistritoId());

        if (!sala.getDistritoId().equals(form.getDistritoId())
                && tocaDao.tieneTocasPendientesPorSala(id)) {
            throw new ReglaNegocioException(
                    "No se puede cambiar el distrito: la sala tiene tocas asignadas");
        }

        String nombre = form.getNombre().trim();
        if (salaDao.existsByNombreAndDistrito(nombre, form.getDistritoId(), id)) {
            throw new ReglaNegocioException(
                    "Ya existe una sala con el nombre \"" + nombre + "\" en ese distrito");
        }
        if (form.getMagistradoUsuarioId() != null) {
            validarMagistradoUnico(form.getMagistradoUsuarioId(), id);
        }

        Long magistradoAnterior = sala.getMagistradoUsuarioId();
        sala.setNombre(nombre);
        sala.setDistritoId(form.getDistritoId());
        sala.setDireccion(form.getDireccion().trim());
        sala.setMagistradoUsuarioId(form.getMagistradoUsuarioId());
        sala.setActualizadaEn(LocalDateTime.now(ZONA));
        Sala guardada = salaDao.save(sala);

        // Sincronizar el magistrado anterior y el nuevo con el nuevo estado
        if (magistradoAnterior != null && !magistradoAnterior.equals(guardada.getMagistradoUsuarioId())) {
            desasignarMagistradoDeSala(magistradoAnterior);
        }
        sincronizarMagistrado(guardada);
        enriquecer(guardada);
        return guardada;
    }

    /** RF 3.4 Baja o reactivación. */
    public Sala cambiarEstado(Long id, boolean activa) {
        Sala sala = obtener(id);
        if (!activa && sala.isActiva() && tocaDao.tieneTocasPendientesPorSala(id)) {
            throw new ReglaNegocioException(
                    "No se puede desactivar la sala: tiene tocas pendientes de resolución");
        }
        sala.setActiva(activa);
        sala.setActualizadaEn(LocalDateTime.now(ZONA));
        Sala guardada = salaDao.save(sala);
        enriquecer(guardada);
        return guardada;
    }

    /** Asigna o quita el magistrado de una sala (RF 4.3). Un magistrado = una sala. */
    public Sala asignarMagistrado(Long salaId, Long magistradoUsuarioId) {
        Sala sala = obtener(salaId);

        // Si ya estaba el mismo, no hay nada que hacer
        if (java.util.Objects.equals(sala.getMagistradoUsuarioId(), magistradoUsuarioId)) {
            enriquecer(sala);
            return sala;
        }

        if (magistradoUsuarioId != null) {
            validarMagistradoUnico(magistradoUsuarioId, salaId);
        }

        Long anterior = sala.getMagistradoUsuarioId();
        sala.setMagistradoUsuarioId(magistradoUsuarioId);
        sala.setActualizadaEn(LocalDateTime.now(ZONA));
        Sala guardada = salaDao.save(sala);

        if (anterior != null) {
            desasignarMagistradoDeSala(anterior);
        }
        sincronizarMagistrado(guardada);
        enriquecer(guardada);
        return guardada;
    }

    // ---------- Totales ----------

    public long totalRegistradas() { return salaDao.count(); }
    public long totalActivas()     { return salaDao.countByActiva(true); }
    public long totalInactivas()   { return salaDao.countByActiva(false); }

    public List<CatalogoTemporalSegunda.DistritoDto> distritosActivos() {
        return catalogo.distritosActivos();
    }

    public SalaForm formularioDe(Sala sala) {
        SalaForm form = new SalaForm();
        form.setNombre(sala.getNombre());
        form.setDistritoId(sala.getDistritoId());
        form.setDireccion(sala.getDireccion());
        form.setMagistradoUsuarioId(sala.getMagistradoUsuarioId());
        return form;
    }

    // ---------- helpers ----------

    private Sala obtener(Long id) {
        return salaDao.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La sala no existe"));
    }

    private void validarDistrito(String distritoId) {
        if (distritoId == null || catalogo.distrito(distritoId).isEmpty()) {
            throw new ReglaNegocioException("Seleccione un distrito válido");
        }
    }

    private void validarMagistradoUnico(Long magistradoUsuarioId, Long excluirSalaId) {
        if (magistradoUsuarioId != null
                && salaDao.existsByMagistradoUsuarioId(magistradoUsuarioId, excluirSalaId)) {
            throw new ReglaNegocioException(
                    "El magistrado ya tiene una sala asignada. Primero quítelo de la otra sala.");
        }
    }

    /** Refleja en el Magistrado (campo derivado salaId) la sala que tiene asignada. */
    private void sincronizarMagistrado(Sala sala) {
        if (sala.getMagistradoUsuarioId() == null) return;
        for (Magistrado m : magistradoDao.activos()) {
            if (sala.getMagistradoUsuarioId().equals(m.getUsuarioId())) {
                m.setSalaId(sala.getId());
                magistradoDao.save(m);
                return;
            }
        }
    }

    /** Deja al magistrado sin sala (cuando se le quita o se le cambia). */
    private void desasignarMagistradoDeSala(Long magistradoUsuarioId) {
        for (Magistrado m : magistradoDao.activos()) {
            if (magistradoUsuarioId.equals(m.getUsuarioId()) && m.getSalaId() != null) {
                m.setSalaId(null);
                magistradoDao.save(m);
                return;
            }
        }
    }

    private String nombreDeMagistrado(Long magistradoUsuarioId) {
        if (magistradoUsuarioId == null) return null;
        for (Magistrado m : magistradoDao.activos()) {
            if (magistradoUsuarioId.equals(m.getUsuarioId())) {
                return m.getNombreCompleto();
            }
        }
        return null;
    }

    private void enriquecer(Sala sala) {
        catalogo.distrito(sala.getDistritoId())
                .ifPresent(d -> sala.setDistritoNombre(d.nombre()));
        sala.setMagistradoNombre(nombreDeMagistrado(sala.getMagistradoUsuarioId()));
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
