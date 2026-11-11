package mx.edu.utez.sij.nucleo.web;

import java.util.List;

/**
 * Una página de resultados para las listas con paginación (lo que devuelve un DAO o un service).
 *
 * <pre>
 * Pagina&lt;Perito&gt; pagina = Pagina.de(todosLosFiltrados, numeroDePagina, 25);
 * request.setAttribute("pagina", pagina);
 * </pre>
 *
 * En la JSP: ${pagina.content} (los registros), ${pagina.totalElements}, ${pagina.number}...
 * y &lt;n:paginacion pagina="${pagina}" url="/peritajes/peritos"/&gt; dibuja los botones.
 * Los nombres en inglés son los mismos de Spring Data: así las JSP que ya existían no cambiaron.
 *
 * La página empieza en 0.
 */
public class Pagina<T> {

    private final List<T> content;
    private final int number;
    private final int size;
    private final long totalElements;

    public Pagina(List<T> content, int number, int size, long totalElements) {
        this.content = List.copyOf(content);
        this.number = number;
        this.size = size;
        this.totalElements = totalElements;
    }

    /** Recorta una lista completa (ya filtrada y ordenada) a la página pedida. Útil en DAOs en memoria. */
    public static <T> Pagina<T> de(List<T> todos, int numero, int tamano) {
        int pagina = Math.max(numero, 0);
        int desde = (int) Math.min((long) pagina * tamano, todos.size());
        int hasta = Math.min(desde + tamano, todos.size());
        return new Pagina<>(todos.subList(desde, hasta), pagina, tamano, todos.size());
    }

    /** Desde dónde empieza la página: para LIMIT ? OFFSET ? en SQL. */
    public static int offset(int numero, int tamano) {
        return Math.max(numero, 0) * tamano;
    }

    public List<T> getContent() {
        return content;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getNumberOfElements() {
        return content.size();
    }

    public int getTotalPages() {
        return size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean isFirst() {
        return number == 0;
    }

    public boolean isLast() {
        return number + 1 >= getTotalPages();
    }
}
