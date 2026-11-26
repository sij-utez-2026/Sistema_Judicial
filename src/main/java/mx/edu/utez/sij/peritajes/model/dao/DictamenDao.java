package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.peritajes.model.ArchivoDictamen;
import mx.edu.utez.sij.peritajes.model.Dictamen;
import mx.edu.utez.sij.peritajes.model.EstadoPeritaje;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

// Dictamen del perito: borrador con autoguardado (RF 4.3), archivos y entrega definitiva (RF 4.4).
// docs/modulos/peritajes.md 6.6. Tablas dictamen_Per (uno por peritaje) y anexo_dictamen_Per.
// Los métodos que cambian algo regresan el mensaje de error, o null si todo salió bien.
// Pendientes:
// - limpiar el contenido con Jsoup.clean(html, Safelist.relaxed()) antes de guardarlo (jsoup aún no está en el pom.xml).
//   Mientras tanto el HTML nunca se escribe sin escapar en una JSP: lo vuelve a cargar el editor Quill,
// - envío por correo de la confirmación al perito (núcleo).
public class DictamenDao {

    // El autoguardado responde 409 con este mensaje (otra ventana guardó antes)
    public static final String CAMBIOS_EN_OTRA_VENTANA = "Hay cambios más recientes en otra ventana";

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    private static final long MAXIMO_BYTES = 25L * 1024 * 1024;
    private static final List<String> EXTENSIONES = List.of("pdf", "doc", "docx", "png", "jpg", "jpeg");

    private final BitacoraDao bitacoraDao = new BitacoraDao();
    private final NotificacionDao notificacionDao = new NotificacionDao();

    // El dictamen que ve el perito en su expediente: En Proceso (borrador) o Dictamen Entregado (solo lectura).
    // En otro estado regresa null. Trae la lista de archivos (sin su contenido).
    public Dictamen paraElPerito(Peritaje peritaje) {
        if (peritaje.getEstado() != EstadoPeritaje.EN_PROCESO
                && peritaje.getEstado() != EstadoPeritaje.DICTAMEN_ENTREGADO) {
            return null;
        }
        if (peritaje.getEstado() == EstadoPeritaje.EN_PROCESO) {
            // Casos abiertos antes de que existiera el borrador
            try (Connection con = ConexionBD.obtener()) {
                crearSiNoExiste(con, peritaje.getId());
            } catch (SQLException e) {
                throw new BaseDatosException("No se pudo preparar el borrador del dictamen", e);
            }
        }
        return buscar(peritaje.getId());
    }

    // Crea el borrador vacío del peritaje si todavía no existe (hay uno por peritaje: id_peritaje es UNIQUE).
    // Lo llama PeritajeDao.abrirExpediente dentro de su transacción.
    void crearSiNoExiste(Connection con, Long peritajeId) throws SQLException {
        String sql = "INSERT IGNORE INTO dictamen_Per (id_peritaje, contenido_texto, fecha_ultimo_guardado, es_finalizado, version)"
                + " VALUES (?, '', ?, FALSE, 0)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, peritajeId);
            ps.setObject(2, LocalDateTime.now(ZONA));
            ps.executeUpdate();
        }
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
        String error = validarEnProceso(peritaje, usuario);
        if (error != null) {
            return error;
        }
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                crearSiNoExiste(con, peritaje.getId());
                if (!actualizarContenido(con, peritaje.getId(), contenido, version)) {
                    con.rollback();
                    return CAMBIOS_EN_OTRA_VENTANA;
                }
                // Solo el guardado manual va a la bitácora (decisión del equipo; el autoguardado de cada 30 s no)
                if (manual) {
                    bitacoraDao.registrar(con, usuario, BitacoraDao.GUARDADO_BORRADOR, peritaje.getFolio(),
                            "Cambios del dictamen guardados antes de la entrega definitiva.");
                }
                con.commit();
                return null;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo guardar el dictamen", e);
        }
    }

    // El dictamen del peritaje con sus archivos (sin el contenido de los archivos). null si no existe.
    public Dictamen buscar(Long peritajeId) {
        String sql = "SELECT id_dictamen, contenido_texto, fecha_ultimo_guardado, es_finalizado, version"
                + " FROM dictamen_Per WHERE id_peritaje = ?";
        String sqlArchivos = "SELECT id_anexo, nombre_archivo, formato_archivo, tamano_mb FROM anexo_dictamen_Per"
                + " WHERE id_dictamen = ? ORDER BY id_anexo";
        try (Connection con = ConexionBD.obtener()) {
            Dictamen dictamen;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, peritajeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    dictamen = new Dictamen();
                    dictamen.setId(rs.getLong("id_dictamen"));
                    dictamen.setPeritajeId(peritajeId);
                    String contenido = rs.getString("contenido_texto");
                    dictamen.setContenido(contenido == null ? "" : contenido);
                    dictamen.setActualizadoEn(Jdbc.fecha(rs, "fecha_ultimo_guardado"));
                    dictamen.setFinalizado(rs.getBoolean("es_finalizado"));
                    dictamen.setVersion(rs.getInt("version"));
                }
            }
            List<ArchivoDictamen> archivos = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sqlArchivos)) {
                ps.setLong(1, dictamen.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        archivos.add(new ArchivoDictamen(rs.getLong("id_anexo"), rs.getString("nombre_archivo"),
                                rs.getString("formato_archivo"), rs.getDouble("tamano_mb"), null));
                    }
                }
            }
            dictamen.setArchivos(archivos);
            return dictamen;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo consultar el dictamen", e);
        }
    }

    // RF 4.4: PDF, Word o imagen (PNG o JPEG), máximo 25 MB cada uno. Solo mientras sea borrador.
    // Recibe los datos del archivo (y no el Part del servlet). contenido es null si no se eligió archivo.
    public String subirArchivo(Peritaje peritaje, UsuarioPeritajes usuario, String nombreOriginal,
                               long tamano, InputStream contenido) {
        String error = validarEnProceso(peritaje, usuario);
        if (error != null) {
            return error;
        }
        if (contenido == null || tamano == 0 || nombreOriginal == null || nombreOriginal.isBlank()) {
            return "Seleccione un archivo para subir";
        }
        String extension = extension(nombreOriginal);
        if (!EXTENSIONES.contains(extension) || tamano > MAXIMO_BYTES) {
            // RF 4.4: formato no permitido o más de 25 MB
            return "El archivo debe ser PDF, Word o imagen (PNG o JPEG) de máximo 25 MB";
        }
        String nombre = nombreOriginal.length() > 200 ? nombreOriginal.substring(nombreOriginal.length() - 200) : nombreOriginal;

        String sql = "INSERT INTO anexo_dictamen_Per (id_dictamen, nombre_archivo, formato_archivo, tamano_mb, archivo_contenido)"
                + " SELECT id_dictamen, ?, ?, ?, ? FROM dictamen_Per WHERE id_peritaje = ? AND es_finalizado = FALSE";
        try (Connection con = ConexionBD.obtener()) {
            crearSiNoExiste(con, peritaje.getId());
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setString(2, extension.toUpperCase());
                ps.setDouble(3, ArchivoDictamen.megabytes(tamano));
                ps.setBinaryStream(4, contenido, tamano);
                ps.setLong(5, peritaje.getId());
                return ps.executeUpdate() == 1 ? null : "El dictamen ya fue entregado: no se puede modificar";
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo guardar el archivo", e);
        }
    }

    // Quitar un archivo mientras el dictamen siga en borrador (peritajes.md 6.6)
    public String quitarArchivo(Peritaje peritaje, UsuarioPeritajes usuario, Long archivoId) {
        String error = validarEnProceso(peritaje, usuario);
        if (error != null) {
            return error;
        }
        String sql = "DELETE x FROM anexo_dictamen_Per x JOIN dictamen_Per d ON d.id_dictamen = x.id_dictamen"
                + " WHERE x.id_anexo = ? AND d.id_peritaje = ? AND d.es_finalizado = FALSE";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, archivoId);
            ps.setLong(2, peritaje.getId());
            return ps.executeUpdate() == 1 ? null : "El archivo no existe";
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo quitar el archivo", e);
        }
    }

    // Un archivo del dictamen del perito (borrador o entregado) con su contenido, para descargarlo.
    // null si no existe o el peritaje no es suyo.
    public ArchivoDictamen getArchivo(Peritaje peritaje, UsuarioPeritajes usuario, Long archivoId) {
        if (PeritajeDao.validarAcceso(peritaje, usuario) != null) {
            return null;
        }
        String sql = "SELECT x.id_anexo, x.nombre_archivo, x.formato_archivo, x.tamano_mb, x.archivo_contenido"
                + " FROM anexo_dictamen_Per x JOIN dictamen_Per d ON d.id_dictamen = x.id_dictamen"
                + " WHERE x.id_anexo = ? AND d.id_peritaje = ?";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, archivoId);
            ps.setLong(2, peritaje.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ArchivoDictamen(rs.getLong("id_anexo"), rs.getString("nombre_archivo"),
                        rs.getString("formato_archivo"), rs.getDouble("tamano_mb"), rs.getBytes("archivo_contenido"));
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo leer el archivo", e);
        }
    }

    // RF 4.4 Finalizar. Si llega contenido (lo que hay en pantalla) primero se guarda.
    // En una sola transacción: dictamen finalizado y peritaje "Dictamen Entregado". Después ya no se modifica.
    public String finalizar(Peritaje peritaje, UsuarioPeritajes usuario, String contenido, Integer version) {
        String error = validarEnProceso(peritaje, usuario);
        if (error != null) {
            return error;
        }
        // Primero se valida y después se guarda, para no reemplazar el borrador con un contenido vacío
        Dictamen actual = buscar(peritaje.getId());
        String aEntregar = (contenido != null) ? contenido : (actual == null ? null : actual.getContenido());
        if (sinTexto(aEntregar)) {
            return "Debes redactar tu dictamen antes de finalizarlo";   // texto del DFR 4.4
        }

        LocalDateTime ahora = LocalDateTime.now(ZONA);
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                crearSiNoExiste(con, peritaje.getId());
                if (contenido != null && !actualizarContenido(con, peritaje.getId(), contenido, version)) {
                    con.rollback();
                    return CAMBIOS_EN_OTRA_VENTANA;
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE dictamen_Per SET es_finalizado = TRUE,"
                        + " fecha_ultimo_guardado = ? WHERE id_peritaje = ? AND es_finalizado = FALSE")) {
                    ps.setObject(1, ahora);
                    ps.setLong(2, peritaje.getId());
                    ps.executeUpdate();
                }
                int filas;
                try (PreparedStatement ps = con.prepareStatement("UPDATE peritaje_Per SET estado_peritaje = 'Dictamen Entregado',"
                        + " fecha_entrega = ? WHERE id_peritaje = ? AND estado_peritaje = 'En Proceso'")) {
                    ps.setObject(1, ahora);
                    ps.setLong(2, peritaje.getId());
                    filas = ps.executeUpdate();
                }
                if (filas == 0) {
                    con.rollback();
                    return "El dictamen solo se puede entregar cuando el peritaje está En Proceso";
                }
                // Dictamen Entregado ya no es carga activa: se le resta al perito
                PeritajeDao.actualizarCarga(con, peritaje.getPeritoId());
                bitacoraDao.registrar(con, usuario, BitacoraDao.ENTREGA_DICTAMEN, peritaje.getFolio(),
                        "Entrega definitiva confirmada por el perito. En Proceso → Dictamen Entregado. "
                                + "Contenido y archivos inmutables.");
                // RF 4.4 y 5.1: confirmación al perito
                notificacionDao.avisar(con, usuario.getId(), peritaje.getId(), NotificacionDao.ENTREGA_DICTAMEN,
                        "Su dictamen del peritaje " + peritaje.getFolio() + " quedó entregado.");
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo entregar el dictamen", e);
        }
        peritaje.setEstado(EstadoPeritaje.DICTAMEN_ENTREGADO);
        peritaje.setFechaEntrega(ahora);
        return null;
    }

    // UPDATE con la versión: si otra ventana guardó antes (la versión ya no coincide) no cambia ninguna fila.
    // version null = no se revisa. Regresa false si no se guardó.
    private boolean actualizarContenido(Connection con, Long peritajeId, String contenido, Integer version)
            throws SQLException {
        // TODO: Jsoup.clean(contenido, Safelist.relaxed()) cuando jsoup esté en el pom
        String sql = "UPDATE dictamen_Per SET contenido_texto = ?, version = version + 1, fecha_ultimo_guardado = ?"
                + " WHERE id_peritaje = ? AND es_finalizado = FALSE AND (? IS NULL OR version = ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, contenido == null ? "" : contenido);
            ps.setObject(2, LocalDateTime.now(ZONA));
            ps.setLong(3, peritajeId);
            ps.setObject(4, version);
            ps.setObject(5, version);
            return ps.executeUpdate() == 1;
        }
    }

    // "Reporte.final.PDF" → "pdf"
    private String extension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase();
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
