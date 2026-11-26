package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.dao.ResolucionDao;
import mx.edu.utez.sij.segunda.dao.TocaDao;
import mx.edu.utez.sij.segunda.dao.VersionResolucionDao;
import mx.edu.utez.sij.segunda.dto.ResolucionForm;
import mx.edu.utez.sij.segunda.model.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public class ResolucionService {

    public static final int TAMANO_PAGINA = 25;
    public static final List<Integer> TAMANOS_PAGINA = List.of(10, 25, 50, 100);
    private static final int MAX_COMENTARIOS = 2000;
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    private final ResolucionDao resolucionDao;
    private final VersionResolucionDao versionDao;
    private final TocaDao tocaDao;
    private final TocaService tocaService;
    private final PonenciaService ponenciaService;

    public ResolucionService(ResolucionDao resolucionDao,
                             VersionResolucionDao versionDao,
                             TocaDao tocaDao,
                             TocaService tocaService,
                             PonenciaService ponenciaService) {
        this.resolucionDao = resolucionDao;
        this.versionDao = versionDao;
        this.tocaDao = tocaDao;
        this.tocaService = tocaService;
        this.ponenciaService = ponenciaService;
    }

    /** RF 7.1 Registrar resolución. */
    public Resolucion registrar(Long tocaId, ResolucionForm form) {
        return registrar(tocaId, form, null, null);
    }

    /** Registra la resolución y genera su PDF como documento binario asociado a la toca. */
    public Resolucion registrar(Long tocaId, ResolucionForm form, String nombrePdf, byte[] pdf) {
        Toca toca = tocaService.buscar(tocaId)
                .orElseThrow(() -> new ReglaNegocioException("La toca no existe"));

        if (toca.getEstado() != EstadoToca.EN_PONENCIA) {
            throw new ReglaNegocioException(
                    "La resolución solo puede registrarse cuando la toca está en ponencia");
        }
        if (toca.getMagistradoId() == null) {
            throw new ReglaNegocioException("La toca no tiene un magistrado de Segunda Instancia asignado");
        }
        if (form.getSentido() == null || form.getSentido().isBlank()) {
            throw new ReglaNegocioException("Seleccione el sentido de la resolución");
        }
        String comentarios = limpiarHtml(form.getComentarios());
        String textoPlano = comentarios.replaceAll("<[^>]+>", " ").replaceAll("&nbsp;", " ").trim();
        if (textoPlano.isBlank()) {
            throw new ReglaNegocioException("Escriba el resolutivo de la resolución");
        }
        if (textoPlano.length() > MAX_COMENTARIOS) {
            throw new ReglaNegocioException("Los comentarios no deben pasar de 2000 caracteres");
        }
        if (resolucionDao.findVigenteByTocaId(tocaId).isPresent()) {
            throw new ReglaNegocioException("La toca ya tiene una resolución vigente");
        }

        Resolucion r = new Resolucion();
        r.setTocaId(tocaId);
        r.setMagistradoId(toca.getMagistradoId());
        r.setSentido(SentidoResolucion.desdeTexto(form.getSentido()));
        r.setComentarios(comentarios);
        r.setPdfUrl(null);
        r.setTocaFolio(toca.getFolioToca());
        r.setTocaExpediente(toca.getNumeroExpediente());
        r.setNombrePdf(nombrePdfGenerado(toca.getFolioToca()));
        r.setEstado(EstadoResolucion.VIGENTE);
        r.setFecha(LocalDateTime.now(ZONA));
        r.setPdfDocumento(ResolucionPdfGenerator.generar(r));
        String detalle = "Resolución emitida · sentido: " + r.getSentido().getEtiqueta();
        if (!resolucionDao.registrarConCierreAtomico(r, detalle)) {
            resolucionDao.save(r);
            tocaService.cambiarEstado(tocaId, EstadoToca.RESUELTA, detalle);
        }
        return r;
    }

    /** Nombre estable y seguro para el documento generado en documento_Seg. */
    private String nombrePdfGenerado(String folio) {
        String base = folio == null || folio.isBlank() ? "resolucion" : folio;
        return "Resolucion-" + base.replaceAll("[^A-Za-z0-9._-]", "_") + ".pdf";
    }

    /** Conserva únicamente las etiquetas que puede producir el editor Quill. */
    private String limpiarHtml(String html) {
        if (html == null) return "";
        String limpio = html.replaceAll("(?is)<!--.*?-->|<script[^>]*>.*?</script>|<style[^>]*>.*?</style>", "");
        limpio = limpio.replaceAll("(?i)</?(?!p\\b|br\\b|strong\\b|em\\b|u\\b|ol\\b|ul\\b|li\\b)[a-z][^>]*>", "");
        limpio = limpio.replaceAll("(?i)<(p|br|strong|em|u|ol|ul|li)\\b[^>]*>", "<$1>");
        return limpio.trim();
    }

    /** RF 7.3 Actualizar solo el PDF (nunca el sentido). */
    public Resolucion actualizarPdf(Long resolucionId, String motivo) {
        throw new ReglaNegocioException("La base vigente no tiene una tabla de versiones ni almacenamiento de documentos para actualizar el PDF; no se realizó ningún cambio.");
    }

    /** RF 7.4 Anular resolución (solo admin). */
    public Resolucion anular(Long resolucionId, String motivo) {
        throw new ReglaNegocioException("La tabla resolucion_Seg no tiene campos de anulación; no se realizó ningún cambio.");
    }

    public List<Resolucion> deToca(Long tocaId) {
        List<Resolucion> lista = resolucionDao.findByTocaId(tocaId);
        for (Resolucion r : lista) enriquecer(r);
        return lista;
    }

    public Optional<Resolucion> buscar(Long id) {
        Optional<Resolucion> r = resolucionDao.findById(id);
        r.ifPresent(this::enriquecer);
        return r;
    }

    public List<VersionResolucion> versiones(Long resolucionId) {
        return versionDao.findByResolucionId(resolucionId);
    }

    public Pagina<Resolucion> listar(String texto, EstadoResolucion estado, Long salaId,
                                     Long soloDeMagistradoUsuarioId, int pagina, int tamano) {
        int tamanoValido = TAMANOS_PAGINA.contains(tamano) ? tamano : TAMANO_PAGINA;
        Pagina<Resolucion> resultado = resolucionDao.buscar(vacioANull(texto), estado, salaId,
                soloDeMagistradoUsuarioId, Math.max(pagina, 0), tamanoValido);
        for (Resolucion r : resultado.getContent()) enriquecer(r);
        return resultado;
    }

    public long totalVigentes() {
        return listar(null, EstadoResolucion.VIGENTE, null, null, 0, 1000)
                .getContent().size();
    }

    private void enriquecer(Resolucion r) {
        r.setComentarios(limpiarHtml(r.getComentarios()));
        tocaDao.findById(r.getTocaId()).ifPresent(t -> {
            r.setTocaFolio(t.getFolioToca());
            r.setTocaExpediente(t.getNumeroExpediente());
        });
    }

    private String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
