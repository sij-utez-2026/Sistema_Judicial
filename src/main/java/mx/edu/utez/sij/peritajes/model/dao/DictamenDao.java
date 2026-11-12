package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.peritajes.model.ArchivoDictamen;
import mx.edu.utez.sij.peritajes.model.Dictamen;
import mx.edu.utez.sij.peritajes.model.EstadoDictamen;
import mx.edu.utez.sij.peritajes.model.EstadoPeritaje;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static mx.edu.utez.sij.peritajes.model.dao.BaseDatosMemoria.DICTAMENES;

// Dictamen del perito: borrador con autoguardado (RF 4.3), archivos y entrega definitiva (RF 4.4).
// docs/modulos/peritajes.md 6.6. Tablas per_dictamen y per_dictamen_archivo.
// Los métodos que cambian algo regresan el mensaje de error, o null si todo salió bien.
// Pendiente con el núcleo y la base de datos:
// - limpiar el contenido con Jsoup.clean(html, Safelist.relaxed()) antes de guardarlo (jsoup aún no está en el pom.xml).
//   Mientras tanto el HTML nunca se escribe sin escapar en una JSP: lo vuelve a cargar el editor Quill,
// - archivos con archivoService.guardar("PERITAJES", archivo, tipos, 25),
// - confirmación al perito con notificacionService (RF 4.4 y 5.1).
public class DictamenDao {

    // El autoguardado responde 409 con este mensaje (otra ventana guardó antes)
    public static final String CAMBIOS_EN_OTRA_VENTANA = "Hay cambios más recientes en otra ventana";

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    private static final long MAXIMO_BYTES = 25L * 1024 * 1024;
    private static final List<String> EXTENSIONES = List.of(".pdf", ".doc", ".docx", ".png", ".jpg", ".jpeg");

    private final BitacoraDao bitacoraDao = new BitacoraDao();

    // El dictamen que ve el perito en su expediente:
    // - Dictamen Entregado: el finalizado (solo lectura),
    // - En Proceso: su borrador (se crea vacío la primera vez; solo hay uno por peritaje, RF 4.3),
    // - otro estado: null.
    public Dictamen paraElPerito(Peritaje peritaje, UsuarioPeritajes usuario) {
        if (peritaje.getEstado() == EstadoPeritaje.DICTAMEN_ENTREGADO) {
            return buscar(peritaje.getId(), null, EstadoDictamen.FINALIZADO);
        }
        if (peritaje.getEstado() == EstadoPeritaje.EN_PROCESO) {
            return borrador(peritaje, usuario.getPeritoId());
        }
        return null;
    }

    // Solo hay un borrador por peritaje y perito; se crea la primera vez.
    // SQL: SELECT ... FROM per_dictamen WHERE peritaje_id = ? AND perito_id = ? AND estado = 'BORRADOR'
    //      y si no hay, INSERT INTO per_dictamen (peritaje_id, perito_id, contenido, estado, version) VALUES (?, ?, '', 'BORRADOR', 0)
    public Dictamen borrador(Peritaje peritaje, Long peritoId) {
        Dictamen dictamen = buscar(peritaje.getId(), peritoId, EstadoDictamen.BORRADOR);
        if (dictamen == null) {
            dictamen = new Dictamen();
            dictamen.setId(BaseDatosMemoria.ID_DICTAMEN.getAndIncrement());
            dictamen.setPeritajeId(peritaje.getId());
            dictamen.setPeritoId(peritoId);
            DICTAMENES.put(dictamen.getId(), dictamen);
        }
        return dictamen;
    }

    // RF 4.3: solo se redacta en peritajes propios y En Proceso. Regresa el error, o null si se puede.
    public String validarEnProceso(Peritaje peritaje, UsuarioPeritajes usuario) {
        String acceso = PeritajeDao.validarAcceso(peritaje, usuario);
        if (acceso != null) {
            return acceso;
        }
        if (peritaje.getEstado() == EstadoPeritaje.DICTAMEN_ENTREGADO) {
            return "El dictamen ya fue entregado: no se puede modificar";
        }
        if (peritaje.getEstado() != EstadoPeritaje.EN_PROCESO) {
            return "El dictamen solo se puede redactar cuando el peritaje está En Proceso";
        }
        return null;
    }

    // Autoguardado y "Guardar borrador" (peritajes.md 6.6).
    // version: la que tiene la pantalla (null = no revisar). manual = true cuando el perito presiona
    // "Guardar borrador" (se registra en la bitácora); false cuando guarda el temporizador de 30 segundos.
    public String guardar(Peritaje peritaje, UsuarioPeritajes usuario, String contenido, Integer version, boolean manual) {
        return guardar(peritaje, usuario, contenido, version, manual, LocalDateTime.now(ZONA));
    }

    // Con fecha explícita: la usan los datos de prueba.
    // SQL: UPDATE per_dictamen SET contenido = ?, version = version + 1, actualizado_en = ?
    //      WHERE id = ? AND version = ?   (si no actualiza ninguna fila, otra ventana guardó antes)
    public String guardar(Peritaje peritaje, UsuarioPeritajes usuario, String contenido, Integer version,
                          boolean manual, LocalDateTime fecha) {
        String error = validarEnProceso(peritaje, usuario);
        if (error != null) {
            return error;
        }
        Dictamen dictamen = borrador(peritaje, usuario.getPeritoId());

        // Si otra ventana guardó antes, no se pisa su trabajo
        if (version != null && version != dictamen.getVersion()) {
            return CAMBIOS_EN_OTRA_VENTANA;
        }

        // TODO: dictamen.setContenido(Jsoup.clean(contenido, Safelist.relaxed())) cuando jsoup esté en el pom
        dictamen.setContenido(contenido == null ? "" : contenido);
        dictamen.setVersion(dictamen.getVersion() + 1);
        dictamen.setActualizadoEn(fecha);

        // Solo el guardado manual va a la bitácora (decisión del equipo; el autoguardado de cada 30 s no)
        if (manual) {
            bitacoraDao.registrar(fecha, usuario.getNombre(), usuario.getRol(), BitacoraDao.GUARDADO_BORRADOR,
                    peritaje.getFolio(), "Cambios del dictamen guardados antes de la entrega definitiva.");
        }
        return null;
    }

    // RF 4.4: PDF, Word o imagen (PNG o JPEG), máximo 25 MB cada uno. Solo mientras sea borrador.
    // Recibe los datos del archivo (y no el Part del servlet). contenido es null si no se eligió archivo.
    // SQL: INSERT INTO per_dictamen_archivo (dictamen_id, archivo_id) VALUES (?, ?)
    public String subirArchivo(Peritaje peritaje, UsuarioPeritajes usuario, String nombreOriginal,
                               String tipoContenido, long tamano, InputStream contenido) {
        String error = validarEnProceso(peritaje, usuario);
        if (error != null) {
            return error;
        }
        if (contenido == null || tamano == 0 || nombreOriginal == null || nombreOriginal.isBlank()) {
            return "Seleccione un archivo para subir";
        }
        boolean tipoValido = EXTENSIONES.stream().anyMatch(ext -> nombreOriginal.toLowerCase().endsWith(ext));
        if (!tipoValido || tamano > MAXIMO_BYTES) {
            // RF 4.4: formato no permitido o más de 25 MB
            return "El archivo debe ser PDF, Word o imagen (PNG o JPEG) de máximo 25 MB";
        }

        try {
            Dictamen dictamen = borrador(peritaje, usuario.getPeritoId());
            dictamen.getArchivos().add(new ArchivoDictamen(BaseDatosMemoria.ID_ARCHIVO.getAndIncrement(),
                    nombreOriginal, tipoContenido, contenido.readAllBytes(), LocalDateTime.now(ZONA)));
            return null;
        } catch (IOException e) {
            return "No se pudo leer el archivo. Intente de nuevo";
        }
    }

    // Quitar un archivo mientras el dictamen siga en borrador (peritajes.md 6.6).
    // SQL: DELETE FROM per_dictamen_archivo WHERE dictamen_id = ? AND archivo_id = ?
    public String quitarArchivo(Peritaje peritaje, UsuarioPeritajes usuario, Long archivoId) {
        String error = validarEnProceso(peritaje, usuario);
        if (error != null) {
            return error;
        }
        Dictamen dictamen = borrador(peritaje, usuario.getPeritoId());
        boolean quitado = dictamen.getArchivos().removeIf(a -> a.getId().equals(archivoId));
        return quitado ? null : "El archivo no existe";
    }

    // Un archivo del dictamen del perito (borrador o entregado), para descargarlo. null si no existe o no es suyo.
    public ArchivoDictamen getArchivo(Peritaje peritaje, UsuarioPeritajes usuario, Long archivoId) {
        if (PeritajeDao.validarAcceso(peritaje, usuario) != null) {
            return null;
        }
        Dictamen dictamen = paraElPerito(peritaje, usuario);
        if (dictamen == null) {
            return null;
        }
        for (ArchivoDictamen archivo : dictamen.getArchivos()) {
            if (archivo.getId().equals(archivoId)) {
                return archivo;
            }
        }
        return null;
    }

    // RF 4.4 Finalizar. Si llega contenido (lo que hay en pantalla) primero se guarda.
    // Después ya no se puede modificar ni reemplazar.
    public String finalizar(Peritaje peritaje, UsuarioPeritajes usuario, String contenido, Integer version) {
        return finalizar(peritaje, usuario, contenido, version, LocalDateTime.now(ZONA));
    }

    // Con fecha explícita: la usan los datos de prueba.
    // SQL (en una sola transacción): UPDATE per_dictamen SET estado = 'FINALIZADO', finalizado_en = ? WHERE id = ?
    //      y UPDATE per_peritaje SET estado = 'DICTAMEN_ENTREGADO', fecha_entrega = ? WHERE id = ?
    public String finalizar(Peritaje peritaje, UsuarioPeritajes usuario, String contenido, Integer version,
                            LocalDateTime fecha) {
        String error = validarEnProceso(peritaje, usuario);
        if (error != null) {
            return error;
        }
        // Primero se valida y después se guarda, para no reemplazar el borrador con un contenido vacío
        String aEntregar = (contenido != null) ? contenido : borrador(peritaje, usuario.getPeritoId()).getContenido();
        if (sinTexto(aEntregar)) {
            return "Debes redactar tu dictamen antes de finalizarlo";   // texto del DFR 4.4
        }
        if (contenido != null) {
            error = guardar(peritaje, usuario, contenido, version, false, fecha);
            if (error != null) {
                return error;
            }
        }

        Dictamen dictamen = borrador(peritaje, usuario.getPeritoId());
        dictamen.setEstado(EstadoDictamen.FINALIZADO);
        dictamen.setFinalizadoEn(fecha);
        peritaje.setEstado(EstadoPeritaje.DICTAMEN_ENTREGADO);
        peritaje.setFechaEntrega(fecha);

        bitacoraDao.registrar(fecha, usuario.getNombre(), usuario.getRol(), BitacoraDao.ENTREGA_DICTAMEN,
                peritaje.getFolio(), "Entrega definitiva confirmada por el perito. En Proceso → Dictamen Entregado. "
                        + "Contenido y archivos inmutables.");
        // TODO núcleo: notificacionService (confirmación al perito, RF 4.4 y 5.1)
        return null;
    }

    // Reasignación (peritajes.md 6.3): el borrador del perito que deja el caso se archiva.
    // SQL: UPDATE per_dictamen SET estado = 'ARCHIVADO' WHERE peritaje_id = ? AND perito_id = ? AND estado = 'BORRADOR'
    public void archivarBorrador(Long peritajeId, Long peritoId) {
        Dictamen borrador = buscar(peritajeId, peritoId, EstadoDictamen.BORRADOR);
        if (borrador != null) {
            borrador.setEstado(EstadoDictamen.ARCHIVADO);
        }
    }

    // peritoId null = de cualquier perito.
    // SQL: SELECT ... FROM per_dictamen WHERE peritaje_id = ? AND (? IS NULL OR perito_id = ?) AND estado = ?
    private Dictamen buscar(Long peritajeId, Long peritoId, EstadoDictamen estado) {
        for (Dictamen d : DICTAMENES.values()) {
            if (d.getPeritajeId().equals(peritajeId) && d.getEstado() == estado
                    && (peritoId == null || peritoId.equals(d.getPeritoId()))) {
                return d;
            }
        }
        return null;
    }

    // "<p><br></p>" (editor vacío) cuenta como sin contenido
    private boolean sinTexto(String html) {
        if (html == null) {
            return true;
        }
        String texto = html.replaceAll("<[^>]*>", "").replace("&nbsp;", " ");
        return texto.isBlank();
    }
}
