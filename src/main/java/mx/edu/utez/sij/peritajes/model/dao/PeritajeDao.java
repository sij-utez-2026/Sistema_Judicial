package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.peritajes.model.CandidatoAsignacion;
import mx.edu.utez.sij.peritajes.model.EstadoPeritaje;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Peritajes (tablas peritaje_Per y asignacion_Per): recepción de las solicitudes de Audiencias y asignación
// automática (RF 2.1 y 2.2), reasignación manual (RF 2.3), lista de casos, "Mis peritajes" y apertura del
// expediente (RF 4.1 y 4.2). Reglas en docs/modulos/peritajes.md 6.1 a 6.5.
// La demanda y la materia salen de solicitud_perito_Aud; el perito, de la asignación vigente (la de mayor id).
// Los métodos que cambian algo regresan el mensaje de error, o null si todo salió bien.
public class PeritajeDao {

    // Peritajes pagina de 25 en 25 (docs/ARQUITECTURA.md, sección 8)
    public static final int TAMANO_PAGINA = 25;

    public static final String NO_EXISTE = "El peritaje no existe";
    public static final String SIN_ACCESO = "No tiene acceso a este peritaje: no está asignado a su cuenta";

    // La asignación vigente del peritaje p: la de mayor id_asignacion
    static final String ASIGNACION_VIGENTE =
            "(SELECT MAX(a2.id_asignacion) FROM asignacion_Per a2 WHERE a2.id_peritaje = p.id_peritaje)";

    // Carga activa del perito u (peritajes.md 6.1): Asignado + En Proceso. La usan PeritajeDao y PeritoDao.
    static final String CARGA_ACTIVA = carga("'Asignado', 'En Proceso'");
    static final String CARGA_ASIGNADOS = carga("'Asignado'");
    static final String CARGA_EN_PROCESO = carga("'En Proceso'");

    // Datos de cada peritaje con su demanda, su materia y su perito vigente
    private static final String DESDE = " FROM peritaje_Per p"
            + " JOIN solicitud_perito_Aud s ON s.id_solicitud_perito = p.id_solicitud_perito"
            + " JOIN demanda_Ofc d ON d.folio = s.folio"
            + " JOIN materia_Ofc m ON m.clave_materia = s.clave_materia"
            + " LEFT JOIN expediente_Ofc e ON e.folio = d.folio"
            + " LEFT JOIN oficialia_Ofc o ON o.clave_oficialia = d.clave_oficialia"
            + " LEFT JOIN juzgado_Ofc j ON j.clave_juzgado = o.clave_juzgado"
            + " LEFT JOIN asignacion_Per a ON a.id_asignacion = " + ASIGNACION_VIGENTE
            + " LEFT JOIN user_Per u ON u.id_usuario = a.id_perito_asignado";

    private static final String SELECT = "SELECT p.id_peritaje, p.id_solicitud_perito, p.estado_peritaje,"
            + " p.fecha_creacion, p.fecha_apertura, p.fecha_entrega, p.fecha_cancelacion,"
            + " s.folio, s.clave_materia, m.nombre AS materia, d.descripcion, e.num_expediente,"
            + " j.nombre AS juzgado, a.id_perito_asignado, a.fecha_hora_asignacion,"
            + " CONCAT_WS(' ', u.nombre, u.apellido_paterno, u.apellido_materno) AS perito,"
            + " (SELECT GROUP_CONCAT(es.descripcion_especialidad ORDER BY es.id_especialidad SEPARATOR ', ')"
            + "    FROM educacion_especialidad_Per es WHERE es.id_usuario = a.id_perito_asignado) AS especialidades"
            + DESDE;

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    private static final Random AZAR = new Random();

    // Solo una recepción de solicitudes a la vez (dos usuarios que abren el inicio al mismo tiempo)
    private static final Object CANDADO = new Object();

    private final BitacoraDao bitacoraDao = new BitacoraDao();
    private final NotificacionDao notificacionDao = new NotificacionDao();
    private final DictamenDao dictamenDao = new DictamenDao();

    private static String carga(String estados) {
        return "(SELECT COUNT(*) FROM peritaje_Per p JOIN asignacion_Per a ON a.id_asignacion = " + ASIGNACION_VIGENTE
                + " WHERE a.id_perito_asignado = u.id_usuario AND p.estado_peritaje IN (" + estados + "))";
    }

    // null si no existe.
    public Peritaje getById(Long id) {
        if (id == null) {
            return null;
        }
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE p.id_peritaje = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo consultar el peritaje", e);
        }
    }

    // El perito solo ve y modifica sus peritajes (DFR 2.4 Restricciones 3; RF 4.2).
    // Regresa el mensaje de acceso denegado, o null si el peritaje es del usuario.
    public static String validarAcceso(Peritaje peritaje, UsuarioPeritajes usuario) {
        if (peritaje == null) {
            return NO_EXISTE;
        }
        if (usuario.getPeritoId() == null || !usuario.getPeritoId().equals(peritaje.getPeritoId())) {
            return SIN_ACCESO;
        }
        return null;
    }

    // ===================== Administradores: Solicitudes y peritajes =====================

    // Lista de casos con filtros opcionales (null = no filtra): folio, demanda o expediente; materia; estado; perito.
    public Pagina<Peritaje> buscar(String texto, String claveMateria, EstadoPeritaje estado, Long peritoId, int pagina) {
        return buscar(texto, claveMateria, estado == null ? null : EnumSet.of(estado), peritoId, pagina);
    }

    // Cuántos peritajes hay en cada estado (pestañas de la lista y tarjetas del inicio).
    // Llaves de texto ("ASIGNADO") para leerlas en la JSP: ${porEstado['ASIGNADO']}
    public Map<String, Long> contarPorEstado() {
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (EstadoPeritaje estado : EstadoPeritaje.values()) {
            conteo.put(estado.name(), 0L);
        }
        String sql = "SELECT estado_peritaje, COUNT(*) FROM peritaje_Per GROUP BY estado_peritaje";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conteo.put(EstadoPeritaje.deBaseDatos(rs.getString(1)).name(), rs.getLong(2));
            }
            return conteo;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron contar los peritajes", e);
        }
    }

    public long contar() {
        return contarPorEstado().values().stream().mapToLong(Long::longValue).sum();
    }

    // ===================== Peritos: Mis peritajes =====================

    // RF 4.1: solo los peritajes del perito en sesión.
    // filtro: "activos" (Asignado y En Proceso), el nombre de un estado, o "todos".
    public Pagina<Peritaje> misPeritajes(UsuarioPeritajes usuario, String texto, String filtro, int pagina) {
        if (usuario.getPeritoId() == null) {
            return new Pagina<>(List.of(), Math.max(pagina, 0), TAMANO_PAGINA, 0);
        }
        Collection<EstadoPeritaje> estados;
        EstadoPeritaje estado = EstadoPeritaje.deTexto(filtro);
        if ("todos".equals(filtro)) {
            estados = null;
        } else if (estado != null) {
            estados = EnumSet.of(estado);
        } else {
            estados = EnumSet.of(EstadoPeritaje.ASIGNADO, EstadoPeritaje.EN_PROCESO);
        }
        return buscar(texto, null, estados, usuario.getPeritoId(), pagina);
    }

    // Cuántos peritajes del perito hay en un estado (tarjetas de "Mis peritajes" y del inicio)
    public long contarDelPerito(UsuarioPeritajes usuario, EstadoPeritaje estado) {
        if (usuario.getPeritoId() == null) {
            return 0;
        }
        return buscar(null, null, EnumSet.of(estado), usuario.getPeritoId(), 0).getTotalElements();
    }

    // RF 4.2: al abrir el expediente por primera vez en Asignado, pasa a En Proceso, se guarda la fecha
    // de apertura y se crea el borrador del dictamen. En aperturas posteriores el estado no cambia.
    // Regresa el error (acceso denegado o cancelado), o null. Si cambia, actualiza también el objeto peritaje.
    public String abrirExpediente(Peritaje peritaje, UsuarioPeritajes usuario) {
        String acceso = validarAcceso(peritaje, usuario);
        if (acceso != null) {
            return acceso;
        }
        if (peritaje.getEstado() == EstadoPeritaje.CANCELADO) {
            return "El peritaje " + peritaje.getFolio() + " fue cancelado";
        }
        if (peritaje.getEstado() != EstadoPeritaje.ASIGNADO) {
            return null;
        }

        LocalDateTime ahora = LocalDateTime.now(ZONA);
        String sql = "UPDATE peritaje_Per SET estado_peritaje = 'En Proceso', fecha_apertura = COALESCE(fecha_apertura, ?)"
                + " WHERE id_peritaje = ? AND estado_peritaje = 'Asignado'";
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                int filas;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setObject(1, ahora);
                    ps.setLong(2, peritaje.getId());
                    filas = ps.executeUpdate();
                }
                if (filas == 1) {
                    dictamenDao.crearSiNoExiste(con, peritaje.getId());
                    bitacoraDao.registrar(con, usuario, BitacoraDao.APERTURA_EXPEDIENTE, peritaje.getFolio(),
                            "Primera apertura por el perito asignado. Asignado → En Proceso.");
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo abrir el expediente", e);
        }
        peritaje.setEstado(EstadoPeritaje.EN_PROCESO);
        if (peritaje.getFechaApertura() == null) {
            peritaje.setFechaApertura(ahora);
        }
        return null;
    }

    // ===================== Solicitud y asignación (DFR 3.3.4) =====================

    // Peritos elegibles: activos, con rol de perito y con la materia. De menor a mayor carga activa.
    // excluirPeritoId: el perito actual, en la reasignación (null = no excluye).
    public List<CandidatoAsignacion> candidatos(String claveMateria, Long excluirPeritoId) {
        String sql = "SELECT u.id_usuario, CONCAT_WS(' ', u.nombre, u.apellido_paterno, u.apellido_materno) AS nombre,"
                + " (SELECT GROUP_CONCAT(mt.nombre ORDER BY mt.nombre SEPARATOR ' · ') FROM perito_materia_Per pm2"
                + "    JOIN materia_Ofc mt ON mt.clave_materia = pm2.clave_materia WHERE pm2.id_usuario = u.id_usuario) AS materias,"
                + " " + CARGA_ACTIVA + " AS carga"
                + " FROM user_Per u JOIN Rol r ON r.id_rol = u.id_rol"
                + " WHERE u.Status = TRUE AND r.rol IN (?, ?)"
                + " AND EXISTS (SELECT 1 FROM perito_materia_Per pm WHERE pm.id_usuario = u.id_usuario AND pm.clave_materia = ?)"
                + " AND (? IS NULL OR u.id_usuario <> ?)"
                + " ORDER BY carga, nombre";
        List<CandidatoAsignacion> candidatos = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, UsuarioPeritajes.PERITO);
            ps.setString(2, UsuarioPeritajes.PERITO_ADMIN);
            ps.setString(3, claveMateria);
            ps.setObject(4, excluirPeritoId);
            ps.setObject(5, excluirPeritoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    candidatos.add(new CandidatoAsignacion(rs.getLong("id_usuario"), rs.getString("nombre"),
                            rs.getString("materias"), rs.getLong("carga")));
                }
            }
            return candidatos;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron consultar los peritos elegibles", e);
        }
    }

    // RF 2.1 y 2.2 (P1, decisión del equipo): Audiencias guarda sus solicitudes en solicitud_perito_Aud.
    // Cada vez que se abre el inicio, la lista de casos o "Mis peritajes":
    // 1. las solicitudes que todavía no tienen peritaje se convierten en peritaje "Pendiente de Asignación",
    // 2. a cada peritaje pendiente se le asigna el perito elegible con menor carga (empate: al azar).
    // Si no hay perito con la materia, el peritaje se queda pendiente y se vuelve a intentar la siguiente vez
    // (por ejemplo, cuando se registre o reactive un perito de esa materia).
    public void recibirSolicitudes() {
        synchronized (CANDADO) {
            for (Peritaje solicitud : solicitudesSinPeritaje()) {
                crearPeritaje(solicitud);
            }
            for (Peritaje pendiente : buscar(null, null, EnumSet.of(EstadoPeritaje.PENDIENTE_ASIGNACION), null, 0,
                    Integer.MAX_VALUE).getContent()) {
                asignarAutomaticamente(pendiente);
            }
        }
    }

    // RF 2.3 Reasignación manual. Solo en Asignado o En Proceso y el estado no cambia (decisión del equipo).
    // El borrador del dictamen se queda con el peritaje: el perito nuevo lo continúa (decisión del equipo).
    // La carga no es restricción (DFR 2.4 Restricciones 4). Regresa el error, o null si se reasignó.
    public String reasignar(Long peritajeId, Long nuevoPeritoId, String motivo, UsuarioPeritajes usuario) {
        Peritaje peritaje = getById(peritajeId);
        if (peritaje == null) {
            return NO_EXISTE;
        }
        if (!peritaje.getEstado().isActivo()) {
            return "Solo se pueden reasignar peritajes en estado Asignado o En Proceso";
        }
        if (motivo == null || motivo.isBlank()) {
            return "La justificación de la reasignación es obligatoria";
        }
        if (nuevoPeritoId == null) {
            return "Seleccione el perito al que se reasignará el peritaje";
        }
        if (nuevoPeritoId.equals(peritaje.getPeritoId())) {
            return "Seleccione un perito distinto al que tiene asignado el peritaje";
        }
        // DFR 2.3: el nuevo perito debe estar activo y tener la materia
        CandidatoAsignacion nuevo = candidatos(peritaje.getClaveMateria(), peritaje.getPeritoId()).stream()
                .filter(c -> c.getPeritoId().equals(nuevoPeritoId))
                .findFirst().orElse(null);
        if (nuevo == null) {
            return "El perito elegido no está activo o no tiene la materia del peritaje";
        }

        String sql = "INSERT INTO asignacion_Per (id_peritaje, id_perito_asignado, id_perito_saliente, id_usuario_cambio,"
                + " motivo_reasignacion, fecha_hora_asignacion) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setLong(1, peritajeId);
                    ps.setLong(2, nuevoPeritoId);
                    ps.setObject(3, peritaje.getPeritoId());
                    ps.setObject(4, usuario.getId());   // null = administrador general (no está en user_Per)
                    ps.setString(5, motivo.trim());
                    ps.setObject(6, LocalDateTime.now(ZONA));
                    ps.executeUpdate();
                }
                // RF 2.3 y 5.2: administrador, fecha y hora, perito saliente y entrante, y el motivo
                bitacoraDao.registrar(con, usuario, BitacoraDao.REASIGNACION_MANUAL, peritaje.getFolio(),
                        peritaje.getPeritoNombreTexto() + " → " + nuevo.getNombre() + ". Estado conservado: "
                                + peritaje.getEstado().getTexto() + ". Motivo: " + motivo.trim());
                // RF 5.1: aviso al perito entrante y al saliente (textos de peritajes.md 6.3)
                notificacionDao.avisar(con, nuevoPeritoId, peritajeId, NotificacionDao.REASIGNACION_ENTRANTE,
                        "Se le asignó el peritaje " + peritaje.getFolio() + " (" + peritaje.getMateria() + ", "
                                + peritaje.getReferenciaDemanda() + ").");
                if (peritaje.getPeritoId() != null) {
                    notificacionDao.avisar(con, peritaje.getPeritoId(), peritajeId,
                            NotificacionDao.REASIGNACION_SALIENTE,
                            "Ya no tiene asignado el peritaje " + peritaje.getFolio() + ".");
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo reasignar el peritaje", e);
        }
        return null;
    }

    // Folios de los peritajes Asignados y En Proceso de un perito (pantalla de edición y baja del perito)
    public List<Peritaje> activosDelPerito(Long peritoId) {
        return buscar(null, null, EnumSet.of(EstadoPeritaje.ASIGNADO, EstadoPeritaje.EN_PROCESO), peritoId, 0,
                Integer.MAX_VALUE).getContent();
    }

    // ===================== Privados =====================

    // Solicitudes de Audiencias que todavía no tienen peritaje (con su demanda y materia para la bitácora)
    private List<Peritaje> solicitudesSinPeritaje() {
        String sql = "SELECT s.id_solicitud_perito, s.folio, m.nombre AS materia FROM solicitud_perito_Aud s"
                + " JOIN materia_Ofc m ON m.clave_materia = s.clave_materia"
                + " WHERE NOT EXISTS (SELECT 1 FROM peritaje_Per p WHERE p.id_solicitud_perito = s.id_solicitud_perito)"
                + " ORDER BY s.id_solicitud_perito";
        List<Peritaje> solicitudes = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Peritaje solicitud = new Peritaje();
                solicitud.setSolicitudId(rs.getLong("id_solicitud_perito"));
                solicitud.setFolioDemanda(rs.getString("folio"));
                solicitud.setMateria(rs.getString("materia"));
                solicitudes.add(solicitud);
            }
            return solicitudes;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron consultar las solicitudes de Audiencias", e);
        }
    }

    // RF 2.1: el peritaje queda en "Pendiente de Asignación" con su fecha de creación
    private void crearPeritaje(Peritaje solicitud) {
        String sql = "INSERT INTO peritaje_Per (id_solicitud_perito, estado_peritaje, fecha_creacion) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, solicitud.getSolicitudId());
                    ps.setString(2, EstadoPeritaje.PENDIENTE_ASIGNACION.getTexto());
                    ps.setObject(3, LocalDateTime.now(ZONA));
                    ps.executeUpdate();
                    try (ResultSet llaves = ps.getGeneratedKeys()) {
                        llaves.next();
                        id = llaves.getLong(1);
                    }
                }
                bitacoraDao.registrar(con, UsuarioPeritajes.SISTEMA, BitacoraDao.RECEPCION_SOLICITUD,
                        String.valueOf(id), "Solicitud " + solicitud.getSolicitudId() + " de Audiencias. Demanda "
                                + solicitud.getFolioDemanda() + ", materia " + solicitud.getMateria()
                                + ". Pendiente de Asignación.");
                con.commit();
            } catch (SQLIntegrityConstraintViolationException e) {
                // Otra petición ya creó el peritaje de esta solicitud (id_solicitud_perito es UNIQUE)
                con.rollback();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo registrar la solicitud de peritaje", e);
        }
    }

    // RF 2.2 Asignación automática: el perito elegible con menor carga activa; si hay empate, uno al azar.
    // El peritaje pasa a Asignado, queda en asignacion_Per (sin usuario = automática), en la bitácora
    // y se avisa al perito (RF 5.1).
    private void asignarAutomaticamente(Peritaje peritaje) {
        List<CandidatoAsignacion> candidatos = candidatos(peritaje.getClaveMateria(), null);
        if (candidatos.isEmpty()) {
            return;   // sin peritos de la materia: se queda pendiente
        }
        long menorCarga = candidatos.get(0).getCarga();
        List<CandidatoAsignacion> empatados = candidatos.stream().filter(c -> c.getCarga() == menorCarga).toList();
        CandidatoAsignacion elegido = empatados.get(AZAR.nextInt(empatados.size()));

        String insertar = "INSERT INTO asignacion_Per (id_peritaje, id_perito_asignado, fecha_hora_asignacion) VALUES (?, ?, ?)";
        String actualizar = "UPDATE peritaje_Per SET estado_peritaje = 'Asignado'"
                + " WHERE id_peritaje = ? AND estado_peritaje = 'Pendiente de Asignación'";
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                int filas;
                try (PreparedStatement ps = con.prepareStatement(actualizar)) {
                    ps.setLong(1, peritaje.getId());
                    filas = ps.executeUpdate();
                }
                if (filas == 0) {
                    con.rollback();   // ya no estaba pendiente
                    return;
                }
                try (PreparedStatement ps = con.prepareStatement(insertar)) {
                    ps.setLong(1, peritaje.getId());
                    ps.setLong(2, elegido.getPeritoId());
                    ps.setObject(3, LocalDateTime.now(ZONA));
                    ps.executeUpdate();
                }
                // RF 2.2: la asignación se registra en la bitácora (perito, fecha y resultado)
                bitacoraDao.registrar(con, UsuarioPeritajes.SISTEMA, BitacoraDao.ASIGNACION_AUTOMATICA,
                        peritaje.getFolio(), elegido.getNombre() + ": activo; materia " + peritaje.getMateria()
                                + "; menor carga previa: " + elegido.getCarga() + " (" + candidatos.size()
                                + (candidatos.size() == 1 ? " perito elegible" : " peritos elegibles")
                                + "). Pendiente de Asignación → Asignado.");
                notificacionDao.avisar(con, elegido.getPeritoId(), peritaje.getId(), NotificacionDao.ASIGNACION,
                        "Se le asignó el peritaje " + peritaje.getFolio() + " (" + peritaje.getMateria() + ", "
                                + peritaje.getReferenciaDemanda() + ").");
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo asignar el peritaje", e);
        }
    }

    private Pagina<Peritaje> buscar(String texto, String claveMateria, Collection<EstadoPeritaje> estados,
                                    Long peritoId, int pagina) {
        return buscar(texto, claveMateria, estados, peritoId, pagina, TAMANO_PAGINA);
    }

    // Cada filtro es opcional (null = no filtra). La página empieza en 0. Del más reciente al más antiguo.
    private Pagina<Peritaje> buscar(String texto, String claveMateria, Collection<EstadoPeritaje> estados,
                                    Long peritoId, int pagina, int tamano) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (texto != null && !texto.isBlank()) {
            String t = texto.trim().toLowerCase();
            where.append(" AND (CAST(p.id_peritaje AS CHAR) = ? OR LOWER(s.folio) LIKE ? OR LOWER(e.num_expediente) LIKE ?)");
            parametros.add(t);
            parametros.add("%" + t + "%");
            parametros.add("%" + t + "%");
        }
        if (claveMateria != null && !claveMateria.isBlank()) {
            where.append(" AND s.clave_materia = ?");
            parametros.add(claveMateria);
        }
        if (estados != null && !estados.isEmpty()) {
            where.append(" AND p.estado_peritaje IN (");
            int i = 0;
            for (EstadoPeritaje estado : estados) {
                where.append(i++ == 0 ? "?" : ", ?");
                parametros.add(estado.getTexto());
            }
            where.append(")");
        }
        if (peritoId != null) {
            where.append(" AND a.id_perito_asignado = ?");
            parametros.add(peritoId);
        }

        int numero = Math.max(pagina, 0);
        List<Peritaje> peritajes = new ArrayList<>();
        try (Connection con = ConexionBD.obtener()) {
            long total;
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*)" + DESDE + where)) {
                Jdbc.asignar(ps, parametros);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getLong(1);
                }
            }
            String sql = SELECT + where + " ORDER BY p.fecha_creacion DESC, p.id_peritaje DESC LIMIT ? OFFSET ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int i = Jdbc.asignar(ps, parametros);
                ps.setInt(i++, tamano);
                ps.setLong(i, (long) numero * tamano);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        peritajes.add(leer(rs));
                    }
                }
            }
            return new Pagina<>(peritajes, numero, tamano, total);
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron consultar los peritajes", e);
        }
    }

    private Peritaje leer(ResultSet rs) throws SQLException {
        Peritaje p = new Peritaje();
        p.setId(rs.getLong("id_peritaje"));
        p.setSolicitudId(rs.getLong("id_solicitud_perito"));
        p.setEstado(EstadoPeritaje.deBaseDatos(rs.getString("estado_peritaje")));
        p.setFechaSolicitud(Jdbc.fecha(rs, "fecha_creacion"));
        p.setFechaApertura(Jdbc.fecha(rs, "fecha_apertura"));
        p.setFechaEntrega(Jdbc.fecha(rs, "fecha_entrega"));
        p.setFechaCancelacion(Jdbc.fecha(rs, "fecha_cancelacion"));
        p.setFolioDemanda(rs.getString("folio"));
        p.setClaveMateria(rs.getString("clave_materia"));
        p.setMateria(rs.getString("materia"));
        p.setDescripcionDemanda(rs.getString("descripcion"));
        p.setNumeroExpediente(rs.getString("num_expediente"));
        p.setJuzgado(rs.getString("juzgado"));
        p.setPeritoId(Jdbc.largo(rs, "id_perito_asignado"));
        p.setFechaAsignacion(Jdbc.fecha(rs, "fecha_hora_asignacion"));
        String perito = rs.getString("perito");
        p.setPeritoNombre(perito == null || perito.isBlank() ? null : perito);
        p.setEspecialidadesPerito(rs.getString("especialidades"));
        return p;
    }
}
