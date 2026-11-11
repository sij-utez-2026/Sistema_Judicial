package mx.edu.utez.sij.nucleo.web;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lee parámetros de la petición sin repetir validaciones en cada servlet.
 * Todo lo que no se puede convertir regresa null (o el valor por omisión), nunca lanza excepción.
 *
 * <pre>
 * String q      = Parametros.texto(request, "q");            // sin espacios a los lados; vacío = null
 * int page      = Parametros.entero(request, "page", 0);
 * Long id       = Parametros.numero(request, "id");          // null si falta o no es número
 * boolean activo = Parametros.booleano(request, "activo");    // "true"/"on"/"1" = true
 * Set&lt;Long&gt; ids = Parametros.numeros(request, "materiasIds"); // casillas con el mismo name
 * List&lt;String&gt; e = Parametros.lista(request, "especialidades"); // especialidades[0], especialidades[1]...
 * </pre>
 */
public final class Parametros {

    private Parametros() {
    }

    public static String texto(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    /** El texto tal como llegó (sin recortar); null si no viene. Para llenar formularios. */
    public static String crudo(HttpServletRequest request, String nombre) {
        return request.getParameter(nombre);
    }

    public static Long numero(HttpServletRequest request, String nombre) {
        String valor = texto(request, nombre);
        if (valor == null) {
            return null;
        }
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int entero(HttpServletRequest request, String nombre, int porOmision) {
        Long valor = numero(request, nombre);
        return (valor == null || valor > Integer.MAX_VALUE || valor < Integer.MIN_VALUE) ? porOmision : valor.intValue();
    }

    public static boolean booleano(HttpServletRequest request, String nombre) {
        String valor = texto(request, nombre);
        return valor != null && (valor.equalsIgnoreCase("true") || valor.equalsIgnoreCase("on") || valor.equals("1"));
    }

    /** Varios valores con el mismo name (casillas de verificación). Ignora los que no son número. */
    public static Set<Long> numeros(HttpServletRequest request, String nombre) {
        String[] valores = request.getParameterValues(nombre);
        if (valores == null) {
            return new LinkedHashSet<>();
        }
        Set<Long> numeros = new LinkedHashSet<>();
        for (String valor : valores) {
            try {
                numeros.add(Long.parseLong(valor.trim()));
            } catch (NumberFormatException e) {
                // se ignora: alguien alteró el formulario
            }
        }
        return numeros;
    }

    /**
     * Campos con índice: nombre[0], nombre[1], ... (por ejemplo, renglones que se agregan con JavaScript).
     * Regresa los valores en el orden de su índice, tal como llegaron.
     */
    public static List<String> lista(HttpServletRequest request, String nombre) {
        Pattern patron = Pattern.compile(Pattern.quote(nombre) + "\\[(\\d{1,4})]");
        TreeMap<Integer, String> porIndice = new TreeMap<>();
        for (String parametro : Collections.list(request.getParameterNames())) {
            Matcher m = patron.matcher(parametro);
            if (m.matches()) {
                porIndice.put(Integer.parseInt(m.group(1)), request.getParameter(parametro));
            }
        }
        return new ArrayList<>(porIndice.values());
    }
}
