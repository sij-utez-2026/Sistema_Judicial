package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.dao.HistorialTocaDao;
import mx.edu.utez.sij.segunda.dao.MagistradoDao;
import mx.edu.utez.sij.segunda.dao.SalaDao;
import mx.edu.utez.sij.segunda.dao.TocaDao;
import mx.edu.utez.sij.segunda.model.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TocaService {

    public static final int TAMANO_PAGINA = 25;
    public static final List<Integer> TAMANOS_PAGINA = List.of(10, 25, 50, 100);

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    /** Transiciones válidas de estado de una toca. Los estados finales no salen. */
    private static final Map<EstadoToca, Set<EstadoToca>> TRANSICIONES = new EnumMap<>(EstadoToca.class);
    static {
        TRANSICIONES.put(EstadoToca.ASIGNADA,
                EnumSet.of(EstadoToca.EN_PONENCIA));
        TRANSICIONES.put(EstadoToca.EN_PONENCIA,
                EnumSet.of(EstadoToca.RESUELTA));
        TRANSICIONES.put(EstadoToca.RESUELTA, EnumSet.noneOf(EstadoToca.class));
    }

    private final TocaDao tocaDao;
    private final HistorialTocaDao historialDao;
    private final SalaDao salaDao;
    private final MagistradoDao magistradoDao;
    private final CatalogoTemporalSegunda catalogo;
    private final ExpedienteTemporalSegunda expedienteTemporal;

    public TocaService(TocaDao tocaDao, HistorialTocaDao historialDao,
                       SalaDao salaDao, MagistradoDao magistradoDao,
                       CatalogoTemporalSegunda catalogo,
                       ExpedienteTemporalSegunda expedienteTemporal) {
        this.tocaDao = tocaDao;
        this.historialDao = historialDao;
        this.salaDao = salaDao;
        this.magistradoDao = magistradoDao;
        this.catalogo = catalogo;
        this.expedienteTemporal = expedienteTemporal;
    }

    public Pagina<Toca> listar(String texto, EstadoToca estado, Long salaId,
                               Long soloDeMagistradoUsuarioId,
                               int pagina, int tamano) {
        int tamanoValido = TAMANOS_PAGINA.contains(tamano) ? tamano : TAMANO_PAGINA;
        Pagina<Toca> resultado = tocaDao.buscar(vacioANull(texto), estado, salaId,
                soloDeMagistradoUsuarioId, Math.max(pagina, 0), tamanoValido);
        for (Toca t : resultado.getContent()) enriquecer(t);
        return resultado;
    }

    public Optional<Toca> buscar(Long id) {
        Optional<Toca> t = tocaDao.findById(id);
        t.ifPresent(this::enriquecer);
        return t;
    }

    public List<HistorialToca> historial(Long tocaId) {
        return historialDao.findByTocaId(tocaId);
    }

    /** Cambia el estado de la toca (valida la transición) y deja constancia. */
    public void cambiarEstado(Long tocaId, EstadoToca nuevoEstado, String motivo) {
        Toca toca = tocaDao.findById(tocaId)
                .orElseThrow(() -> new ReglaNegocioException("La toca no existe"));

        if (toca.getEstado() == nuevoEstado) {
            // Sin cambio real: solo registra el motivo si lo hay, sin transición
            if (motivo != null && !motivo.isBlank()) {
                registrarHistorial(tocaId, "Actualización de la toca", motivo);
            }
            return;
        }

        Set<EstadoToca> permitidos = TRANSICIONES.getOrDefault(toca.getEstado(),
                EnumSet.noneOf(EstadoToca.class));
        if (!permitidos.contains(nuevoEstado)) {
            throw new ReglaNegocioException(
                    "No se puede pasar de " + toca.getEstado().getEtiqueta()
                            + " a " + nuevoEstado.getEtiqueta());
        }

        toca.setEstado(nuevoEstado);
        tocaDao.save(toca);

        HistorialToca h = new HistorialToca();
        h.setTocaId(tocaId);
        h.setFecha(LocalDateTime.now(ZONA));
        h.setAccion("Cambio de estado a " + nuevoEstado.getEtiqueta());
        h.setDetalle(motivo);
        historialDao.save(h);
    }

    /** Registra un evento en el historial sin cambiar el estado. */
    public void registrarHistorial(Long tocaId, String accion, String detalle) {
        HistorialToca h = new HistorialToca();
        h.setTocaId(tocaId);
        h.setFecha(LocalDateTime.now(ZONA));
        h.setAccion(accion);
        h.setDetalle(detalle);
        historialDao.save(h);
    }

    /** Crea la toca cuando la llama Oficialía (RecepcionTocaApi). */
    public Toca recibirDeOficialia(String numeroExpediente, String materiaId, String juzgadoOrigenId,
                                   String distritoId, Long salaId, Long documentoApelacionArchivoId) {
        return recibirDeOficialia(numeroExpediente, materiaId, juzgadoOrigenId, distritoId,
                salaId, documentoApelacionArchivoId, null);
    }

    /** Flujo propio de Segunda: el usuario selecciona la calidad de quien interpone la apelación. */
    public Toca recibirDeOficialia(String numeroExpediente, String materiaId, String juzgadoOrigenId,
                                   String distritoId, Long salaId, Long documentoApelacionArchivoId,
                                   String apelante) {
        return recibirDeOficialia(numeroExpediente, materiaId, juzgadoOrigenId, distritoId,
                salaId, documentoApelacionArchivoId, apelante, null, null);
    }

    /** Recepción propia de Segunda con PDF; el DAO lo inserta dentro de la transacción de la toca. */
    public Toca recibirDeOficialia(String numeroExpediente, String materiaId, String juzgadoOrigenId,
                                   String distritoId, Long salaId, Long documentoApelacionArchivoId,
                                   String apelante, String nombrePdf, byte[] pdf) {
        return recibirInterno(numeroExpediente, materiaId, juzgadoOrigenId, distritoId, salaId,
                documentoApelacionArchivoId, apelante, nombrePdf, pdf, true, true);
    }

    /** Recepción automática: Oficialía entrega expediente y sala; Segunda completa lo demás. */
    public Toca recibirDeContratoOficialia(String numeroExpediente, String materiaId,
                                           String juzgadoOrigenId, String distritoId, Long salaId) {
        return recibirInterno(numeroExpediente, materiaId, juzgadoOrigenId, distritoId, salaId,
                null, null, null, null, false, false);
    }

    private Toca recibirInterno(String numeroExpediente, String materiaId, String juzgadoOrigenId,
                                String distritoId, Long salaId, Long documentoApelacionArchivoId,
                                String apelante, String nombrePdf, byte[] pdf,
                                boolean validarApelante, boolean validarPdf) {
        if (numeroExpediente == null || numeroExpediente.isBlank()
                || materiaId == null || juzgadoOrigenId == null
                || distritoId == null || salaId == null) {
            throw new ReglaNegocioException("Faltan datos obligatorios para generar la toca");
        }
        if (validarApelante && (apelante == null
                || !(apelante.equals("Demandante") || apelante.equals("Demandado")))) {
            throw new ReglaNegocioException("Seleccione expresamente si apela el demandante o el demandado");
        }
        if (validarPdf && !pdfApelacionValido(pdf)) {
            throw new ReglaNegocioException("Adjunte el PDF de apelación válido (hasta 15 MB); la recepción no puede guardarse sin ese documento");
        }
        if (tocaDao.existsByNumeroExpediente(numeroExpediente)) {
            throw new ReglaNegocioException("El expediente ya tiene una toca abierta");
        }

        Sala sala = salaDao.findById(salaId)
                .orElseThrow(() -> new ReglaNegocioException("La sala no existe"));
        if (!sala.isActiva()) {
            throw new ReglaNegocioException("La sala no está activa");
        }
        if (sala.getMagistradoUsuarioId() == null) {
            throw new ReglaNegocioException("La sala no tiene magistrado asignado");
        }
        Magistrado magistrado = magistradoDao.activos().stream()
                .filter(m -> sala.getMagistradoUsuarioId().equals(m.getUsuarioId()))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontró el registro de magistrado asociado a la sala"));

        Toca toca = new Toca();
        toca.setFolioToca(generarFolio());
        toca.setNumeroExpediente(numeroExpediente);
        toca.setApelante(apelante);
        toca.setMateriaId(materiaId);
        toca.setJuzgadoOrigenId(juzgadoOrigenId);
        toca.setDistritoId(distritoId);
        toca.setSalaId(salaId);
        toca.setMagistradoUsuarioId(sala.getMagistradoUsuarioId());
        toca.setMagistradoId(magistrado.getId());
        toca.setDocumentoApelacionArchivoId(documentoApelacionArchivoId);
        toca.setNombreDocumentoApelacion(nombrePdf);
        toca.setDocumentoApelacionPdf(pdf);
        toca.setEstado(EstadoToca.ASIGNADA);
        toca.setFechaRecepcion(LocalDateTime.now(ZONA));

        HistorialToca h = new HistorialToca();
        h.setFecha(LocalDateTime.now(ZONA));
        h.setUsuarioNombre("Sistema");
        h.setAccion("Recepción de la toca");
        h.setDetalle("Recibida de Oficialía · expediente " + numeroExpediente);
        if (!tocaDao.recibirAtomico(toca, h)) {
            tocaDao.save(toca);
            h.setTocaId(toca.getId());
            historialDao.save(h);
        }
        return toca;
    }

    public long totalRegistradas()           { return tocaDao.count(); }
    public long totalPorEstado(EstadoToca e) { return tocaDao.countByEstado(e); }

    public ExpedienteTemporalSegunda.ExpedienteTemporal expedienteDe(Toca toca) {
        return expedienteTemporal.buscarExpediente(toca.getNumeroExpediente()).orElse(null);
    }

    public List<ExpedienteTemporalSegunda.AudienciaTemporalDto> audienciasDe(Toca toca) {
        return expedienteTemporal.audienciasDeExpediente(toca.getNumeroExpediente());
    }

    public ExpedienteTemporalSegunda.ResolucionPrimeraTemporalDto resolucionPrimeraDe(Toca toca) {
        return expedienteTemporal.resolucionPrimera(toca.getNumeroExpediente()).orElse(null);
    }

    public List<ExpedienteTemporalSegunda.DictamenTemporalDto> dictamenesDe(Toca toca) {
        return expedienteTemporal.dictamenesDeExpediente(toca.getNumeroExpediente());
    }

    // ---------- helpers ----------

    private void enriquecer(Toca t) {
        catalogo.materia(t.getMateriaId()).ifPresent(m -> t.setMateriaNombre(m.nombre()));
        catalogo.distrito(t.getDistritoId()).ifPresent(d -> t.setDistritoNombre(d.nombre()));
        catalogo.juzgados(null).stream()
                .filter(j -> j.clave().equals(t.getJuzgadoOrigenId()))
                .findFirst()
                .ifPresent(j -> t.setJuzgadoNombre(j.nombre()));
        if (t.getSalaId() != null) {
            salaDao.findById(t.getSalaId()).ifPresent(s -> {
                t.setSalaNombre(s.getNombre());
                if (t.getMagistradoUsuarioId() == null) {
                    t.setMagistradoUsuarioId(s.getMagistradoUsuarioId());
                }
            });
        }
        enriquecerMagistrado(t);
    }

    private void enriquecerMagistrado(Toca toca) {
        if (toca.getMagistradoId() != null) {
            magistradoDao.findById(toca.getMagistradoId()).ifPresent(m -> {
                if (toca.getMagistradoUsuarioId() == null) toca.setMagistradoUsuarioId(m.getUsuarioId());
                if (toca.getMagistradoNombre() == null) toca.setMagistradoNombre(m.getNombreCompleto());
            });
        }
        if (toca.getMagistradoUsuarioId() != null) {
            for (Magistrado m : magistradoDao.activos()) {
                if (toca.getMagistradoUsuarioId().equals(m.getUsuarioId())) {
                    if (toca.getMagistradoId() == null) toca.setMagistradoId(m.getId());
                    if (toca.getMagistradoNombre() == null) toca.setMagistradoNombre(m.getNombreCompleto());
                    break;
                }
            }
        }
    }

    private String generarFolio() {
        int anio = LocalDateTime.now(ZONA).getYear();
        for (int intento = 0; intento < 20; intento++) {
            int numero = ThreadLocalRandom.current().nextInt(10000, 100000);
            String folio = String.format("TOCA-%d-%05d", anio, numero);
            if (!tocaDao.existsByFolio(folio)) {
                return folio;
            }
        }
        throw new ReglaNegocioException("No se pudo generar un folio único para la toca");
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private boolean pdfApelacionValido(byte[] pdf) {
        return pdf != null && pdf.length >= 5 && pdf.length <= 15 * 1024 * 1024
                && pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F' && pdf[4] == '-';
    }
}
