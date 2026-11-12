package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.peritajes.model.RegistroBitacora;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static mx.edu.utez.sij.peritajes.model.dao.BaseDatosMemoria.BITACORA;

// Bitácora del módulo (RF 5.2): registra los movimientos y los consulta con filtros.
// Los registros no se editan ni se borran (no hay métodos para eso).
// TEMPORAL: cuando exista BitacoraService del núcleo, registrar(...) llama a bitacoraService.registrar("PERITAJES", ...)
// dentro de la misma transacción, y buscar(...) consulta core_bitacora filtrando por el módulo.
public class BitacoraDao {

    // Eventos que se registran (decisión del equipo: los del DFR 5.2 + apertura, borrador y entrega del dictamen)
    public static final String ASIGNACION_AUTOMATICA = "Asignación automática";
    public static final String REASIGNACION_MANUAL = "Reasignación manual";
    public static final String ALTA_PERITO = "Alta de perito";
    public static final String EDICION_PERITO = "Edición de perito";
    public static final String BAJA_PERITO = "Baja de perito";
    public static final String REACTIVACION_PERITO = "Reactivación de perito";
    public static final String APERTURA_EXPEDIENTE = "Apertura de expediente";
    public static final String GUARDADO_BORRADOR = "Guardado de borrador";
    public static final String ENTREGA_DICTAMEN = "Entrega de dictamen";

    public static final List<String> EVENTOS = List.of(ASIGNACION_AUTOMATICA, REASIGNACION_MANUAL, ALTA_PERITO,
            EDICION_PERITO, BAJA_PERITO, REACTIVACION_PERITO, APERTURA_EXPEDIENTE, GUARDADO_BORRADOR,
            ENTREGA_DICTAMEN);

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    // Con la fecha y hora actuales
    public void registrar(UsuarioPeritajes usuario, String evento, String referencia, String detalle) {
        registrar(LocalDateTime.now(ZONA), usuario.getNombre(), usuario.getRol(), evento, referencia, detalle);
    }

    // Con fecha y usuario explícitos (la asignación automática y los datos de prueba)
    public void registrar(LocalDateTime fecha, String usuario, String rol, String evento,
                          String referencia, String detalle) {
        BITACORA.add(new RegistroBitacora(fecha, usuario, rol, evento, referencia, detalle));
    }

    // Del más reciente al más antiguo (RF 5.2). Cada filtro es opcional; texto busca en la referencia
    // (folio o número de registro).
    public Pagina<RegistroBitacora> buscar(LocalDate desde, LocalDate hasta, String usuario, String evento,
                                           String texto, int pagina, int tamano) {
        String t = (texto == null || texto.isBlank()) ? null : texto.trim().toLowerCase();

        // Del último registrado al primero; así, con la misma hora, queda primero el más reciente
        List<RegistroBitacora> recientesPrimero = new ArrayList<>(BITACORA);
        Collections.reverse(recientesPrimero);

        List<RegistroBitacora> filtrados = recientesPrimero.stream()
                .filter(r -> desde == null || !r.getFecha().toLocalDate().isBefore(desde))
                .filter(r -> hasta == null || !r.getFecha().toLocalDate().isAfter(hasta))
                .filter(r -> usuario == null || usuario.isBlank() || r.getUsuario().equals(usuario))
                .filter(r -> evento == null || evento.isBlank() || r.getEvento().equals(evento))
                .filter(r -> t == null || r.getReferencia().toLowerCase().contains(t))
                .sorted(Comparator.comparing(RegistroBitacora::getFecha).reversed())
                .toList();

        return Pagina.de(filtrados, pagina, tamano);
    }

    // Usuarios que aparecen en la bitácora, para el filtro
    public List<String> usuarios() {
        return BITACORA.stream().map(RegistroBitacora::getUsuario).distinct().sorted().toList();
    }
}
