package mx.edu.utez.sij.copias.service;

import mx.edu.utez.sij.copias.dto.ColoresForm;
import mx.edu.utez.sij.copias.model.ColorArea;
import mx.edu.utez.sij.copias.model.Formatos;
import mx.edu.utez.sij.copias.dao.ColorAreaDao;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Colores de las pantallas de Copias que configura COPIAS_ADMIN (tabla cop_color).
 * Solo cambian las pantallas de este módulo: tags/copias/colores.tag pone estas variables CSS encima de las del núcleo.
 * Los valores iniciales son la paleta institucional que acordó el grupo.
 */
public class ColoresCopiasService {

    // Variable CSS → color inicial. --sij-* son del núcleo; --cop-* son propias de recursos/copias/css/copias.css
    public static final String PRIMARIO = "--sij-primario";
    public static final String TITULOS = "--cop-titulos";
    public static final String BOTONES = "--cop-boton";
    public static final String ACENTO = "--sij-acento";
    public static final String BORDES = "--sij-borde";
    public static final String AVISOS = "--cop-aviso";

    private static final Map<String, String> INICIALES = new LinkedHashMap<>();

    static {
        INICIALES.put(PRIMARIO, "#35483B");   // Bosque: menú lateral
        INICIALES.put(TITULOS, "#76513D");    // Tierra: títulos
        INICIALES.put(BOTONES, "#651747");    // Bugambilia: botones principales
        INICIALES.put(ACENTO, "#B49A5A");     // Dorado: detalles
        INICIALES.put(BORDES, "#B8B0A1");     // Piedra: bordes
        INICIALES.put(AVISOS, "#D8C39A");     // Caña: avisos
    }

    private final ColorAreaDao colorDao;

    public ColoresCopiasService(ColorAreaDao colorDao) {
        this.colorDao = colorDao;
    }

    /** Colores actuales (los guardados o, si no hay, los iniciales), en orden fijo. */
    public Map<String, String> actuales() {
        Map<String, String> colores = new LinkedHashMap<>(INICIALES);
        for (ColorArea color : colorDao.findAll()) {
            if (colores.containsKey(color.getVariable())) {
                colores.put(color.getVariable(), color.getValor());
            }
        }
        return colores;
    }

    public Map<String, String> iniciales() {
        return new LinkedHashMap<>(INICIALES);
    }

    public ColoresForm formulario() {
        return aFormulario(actuales());
    }

    public ColoresForm formularioInicial() {
        return aFormulario(INICIALES);
    }

    /** usuarioId: el administrador que hace el cambio (lo pasa el servlet). */
    public void guardar(ColoresForm form, Long usuarioId) {
        guardarColor(PRIMARIO, form.getPrimario(), usuarioId);
        guardarColor(TITULOS, form.getTitulos(), usuarioId);
        guardarColor(BOTONES, form.getBotones(), usuarioId);
        guardarColor(ACENTO, form.getAcento(), usuarioId);
        guardarColor(BORDES, form.getBordes(), usuarioId);
        guardarColor(AVISOS, form.getAvisos(), usuarioId);
    }

    public void restablecer(Long usuarioId) {
        INICIALES.forEach((variable, valor) -> guardarColor(variable, valor, usuarioId));
    }

    private ColoresForm aFormulario(Map<String, String> colores) {
        ColoresForm form = new ColoresForm();
        form.setPrimario(colores.get(PRIMARIO));
        form.setTitulos(colores.get(TITULOS));
        form.setBotones(colores.get(BOTONES));
        form.setAcento(colores.get(ACENTO));
        form.setBordes(colores.get(BORDES));
        form.setAvisos(colores.get(AVISOS));
        return form;
    }

    private void guardarColor(String variable, String valor, Long usuarioId) {
        // También se revisa aquí: el valor se escribe dentro de una etiqueta <style>
        if (valor == null || !valor.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new ReglaNegocioException("Use un color con formato #RRGGBB");
        }
        ColorArea color = colorDao.findByVariable(variable).orElseGet(ColorArea::new);
        color.setVariable(variable);
        color.setValor(valor.toUpperCase(Locale.ROOT));
        color.setActualizadoPor(usuarioId);
        color.setActualizadoEn(Formatos.ahora());
        colorDao.save(color);
    }
}
