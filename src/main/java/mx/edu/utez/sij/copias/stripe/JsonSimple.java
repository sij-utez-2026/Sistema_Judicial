package mx.edu.utez.sij.copias.stripe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Analizador JSON ligero sin dependencias externas para procesar respuestas de la API REST de Stripe.
 */
public final class JsonSimple {

    private final String fuente;
    private int posicion;

    private JsonSimple(String fuente) {
        this.fuente = fuente;
        this.posicion = 0;
    }

    public static Map<String, Object> parsearObjeto(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("El JSON recibido está vacío");
        }
        JsonSimple analizador = new JsonSimple(json);
        Object raiz = analizador.leerValor();
        analizador.saltarEspacios();
        if (analizador.posicion < analizador.fuente.length()) {
            throw new IllegalArgumentException("Contenido inesperado al final del JSON");
        }
        if (!(raiz instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Se esperaba un objeto JSON en la raíz");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> resultado = (Map<String, Object>) raiz;
        return resultado;
    }

    public static String texto(Map<String, Object> objeto, String clave) {
        if (objeto == null) {
            return null;
        }
        Object valor = objeto.get(clave);
        return valor == null ? null : String.valueOf(valor);
    }

    public static Long enteroLargo(Map<String, Object> objeto, String clave) {
        if (objeto == null) {
            return null;
        }
        Object valor = objeto.get(clave);
        if (valor instanceof Number numero) {
            return numero.longValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> objeto(Map<String, Object> objeto, String clave) {
        if (objeto == null) {
            return null;
        }
        Object valor = objeto.get(clave);
        if (valor instanceof Map<?, ?> mapa) {
            return (Map<String, Object>) mapa;
        }
        return null;
    }

    private Object leerValor() {
        saltarEspacios();
        if (posicion >= fuente.length()) {
            throw new IllegalArgumentException("JSON incompleto en posición " + posicion);
        }
        char c = fuente.charAt(posicion);
        return switch (c) {
            case '{' -> leerObjeto();
            case '[' -> leerArreglo();
            case '"' -> leerCadena();
            case 't', 'f' -> leerBooleano();
            case 'n' -> leerNulo();
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    yield leerNumero();
                }
                throw new IllegalArgumentException("Carácter inesperado '" + c + "' en posición " + posicion);
            }
        };
    }

    private Map<String, Object> leerObjeto() {
        esperar('{');
        Map<String, Object> mapa = new LinkedHashMap<>();
        saltarEspacios();
        if (mirar('}')) {
            posicion++;
            return mapa;
        }
        while (true) {
            saltarEspacios();
            if (!mirar('"')) {
                throw new IllegalArgumentException("Se esperaba una clave de texto en posición " + posicion);
            }
            String clave = leerCadena();
            saltarEspacios();
            esperar(':');
            Object valor = leerValor();
            mapa.put(clave, valor);
            saltarEspacios();
            if (mirar(',')) {
                posicion++;
            } else if (mirar('}')) {
                posicion++;
                return mapa;
            } else {
                throw new IllegalArgumentException("Se esperaba ',' o '}' en posición " + posicion);
            }
        }
    }

    private List<Object> leerArreglo() {
        esperar('[');
        List<Object> lista = new ArrayList<>();
        saltarEspacios();
        if (mirar(']')) {
            posicion++;
            return lista;
        }
        while (true) {
            lista.add(leerValor());
            saltarEspacios();
            if (mirar(',')) {
                posicion++;
            } else if (mirar(']')) {
                posicion++;
                return lista;
            } else {
                throw new IllegalArgumentException("Se esperaba ',' o ']' en posición " + posicion);
            }
        }
    }

    private String leerCadena() {
        esperar('"');
        StringBuilder sb = new StringBuilder();
        while (posicion < fuente.length()) {
            char c = fuente.charAt(posicion++);
            if (c == '"') {
                return sb.toString();
            }
            if (c == '\\') {
                if (posicion >= fuente.length()) {
                    throw new IllegalArgumentException("Escape incompleto en cadena JSON");
                }
                char esc = fuente.charAt(posicion++);
                switch (esc) {
                    case '"', '\\', '/' -> sb.append(esc);
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (posicion + 4 > fuente.length()) {
                            throw new IllegalArgumentException("Escape unicode incompleto");
                        }
                        String hex = fuente.substring(posicion, posicion + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        posicion += 4;
                    }
                    default -> throw new IllegalArgumentException("Secuencia de escape inválida: \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
        throw new IllegalArgumentException("Cadena JSON sin cerrar");
    }

    private Boolean leerBooleano() {
        if (fuente.startsWith("true", posicion)) {
            posicion += 4;
            return Boolean.TRUE;
        }
        if (fuente.startsWith("false", posicion)) {
            posicion += 5;
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Booleano inválido en posición " + posicion);
    }

    private Object leerNulo() {
        if (fuente.startsWith("null", posicion)) {
            posicion += 4;
            return null;
        }
        throw new IllegalArgumentException("Valor nulo inválido en posición " + posicion);
    }

    private Number leerNumero() {
        int inicio = posicion;
        if (fuente.charAt(posicion) == '-') {
            posicion++;
        }
        boolean esDecimal = false;
        while (posicion < fuente.length()) {
            char c = fuente.charAt(posicion);
            if (c >= '0' && c <= '9') {
                posicion++;
            } else if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                if (c == '-' || c == '+') {
                    char anterior = fuente.charAt(posicion - 1);
                    if (anterior != 'e' && anterior != 'E') {
                        break;
                    }
                }
                esDecimal = true;
                posicion++;
            } else {
                break;
            }
        }
        String texto = fuente.substring(inicio, posicion);
        return esDecimal ? Double.parseDouble(texto) : Long.parseLong(texto);
    }

    private void saltarEspacios() {
        while (posicion < fuente.length()) {
            char c = fuente.charAt(posicion);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                posicion++;
            } else {
                break;
            }
        }
    }

    private boolean mirar(char esperado) {
        return posicion < fuente.length() && fuente.charAt(posicion) == esperado;
    }

    private void esperar(char esperado) {
        if (posicion >= fuente.length() || fuente.charAt(posicion) != esperado) {
            throw new IllegalArgumentException("Se esperaba '" + esperado + "' en posición " + posicion);
        }
        posicion++;
    }
}
