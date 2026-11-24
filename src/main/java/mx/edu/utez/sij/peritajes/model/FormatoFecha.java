package mx.edu.utez.sij.peritajes.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FormatoFecha {

    private static final Locale ESPANOL = Locale.forLanguageTag("es-MX");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm 'h'", ESPANOL);
    private static final DateTimeFormatter FECHA_HORA_SEGUNDOS = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm:ss 'h'", ESPANOL);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d MMM yyyy", ESPANOL);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss", ESPANOL);

    private FormatoFecha() {
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "" : limpiar(fecha.format(FECHA_HORA));
    }

    public static String fechaHoraSegundos(LocalDateTime fecha) {
        return fecha == null ? "" : limpiar(fecha.format(FECHA_HORA_SEGUNDOS));
    }

    public static String fecha(LocalDateTime fecha) {
        return fecha == null ? "" : limpiar(fecha.format(FECHA));
    }

    public static String hora(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(HORA);
    }

    // Java escribe "oct." en español de México; el boceto usa "oct"
    private static String limpiar(String texto) {
        return texto.replace(".", "");
    }
}
