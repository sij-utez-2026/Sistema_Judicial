package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.peritajes.model.RegistroBitacora;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

// Bitácora del módulo (RF 5.2, tabla bitacora_Per): registra los movimientos y los consulta con filtros.
// Los registros no se editan ni se borran (no hay métodos para eso).
// registrar(...) recibe la Connection de la transacción: si la acción falla, tampoco queda en la bitácora.
// Se guarda el nombre y el rol de quien hizo el movimiento (nombre_usuario, rol_usuario): así queda también
// el administrador general, que no está en user_Per (su id_usuario va NULL, igual que el del sistema).
public class BitacoraDao {

    // Eventos que se registran (decisión del equipo: los del DFR 5.2 + recepción, apertura, borrador y entrega)
    public static final String RECEPCION_SOLICITUD = "Recepción de solicitud";
    public static final String ASIGNACION_AUTOMATICA = "Asignación automática";
    public static final String REASIGNACION_MANUAL = "Reasignación manual";
    public static final String ALTA_PERITO = "Alta de perito";
    public static final String EDICION_PERITO = "Edición de perito";
    public static final String BAJA_PERITO = "Baja de perito";
    public static final String REACTIVACION_PERITO = "Reactivación de perito";
    public static final String APERTURA_EXPEDIENTE = "Apertura de expediente";
    public static final String GUARDADO_BORRADOR = "Guardado de borrador";
    public static final String ENTREGA_DICTAMEN = "Entrega de dictamen";

    public static final List<String> EVENTOS = List.of(RECEPCION_SOLICITUD, ASIGNACION_AUTOMATICA,
            REASIGNACION_MANUAL, ALTA_PERITO, EDICION_PERITO, BAJA_PERITO, REACTIVACION_PERITO, APERTURA_EXPEDIENTE,
            GUARDADO_BORRADOR, ENTREGA_DICTAMEN);

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    // usuario: quien hace el movimiento (UsuarioPeritajes.SISTEMA para las acciones automáticas).
    // referencia: folio del peritaje o número de registro del perito.
    public void registrar(Connection con, UsuarioPeritajes usuario, String evento, String referencia, String detalle)
            throws SQLException {
        UsuarioPeritajes quien = usuario == null ? UsuarioPeritajes.SISTEMA : usuario;
        String sql = "INSERT INTO bitacora_Per (fecha_hora, id_usuario, nombre_usuario, rol_usuario, tipo_evento,"
                + " referencia, detalle_movimiento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, LocalDateTime.now(ZONA));
            if (quien.getId() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setLong(2, quien.getId());
            }
            ps.setString(3, quien.getNombre());
            ps.setString(4, quien.getId() == null && !quien.isAdministradorGeneral() ? "Automático" : quien.getRol());
            ps.setString(5, evento);
            ps.setString(6, referencia);
            ps.setString(7, detalle);
            ps.executeUpdate();
        }
    }

    // Del más reciente al más antiguo (RF 5.2). Cada filtro es opcional (null = no filtra).
    // usuario: el nombre de quien hizo el movimiento. texto: busca en la referencia (folio o número de registro).
    public Pagina<RegistroBitacora> buscar(LocalDate desde, LocalDate hasta, String usuario, String evento,
                                           String texto, int pagina, int tamano) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (desde != null) {
            where.append(" AND b.fecha_hora >= ?");
            parametros.add(desde.atStartOfDay());
        }
        if (hasta != null) {
            where.append(" AND b.fecha_hora < ?");
            parametros.add(hasta.plusDays(1).atStartOfDay());
        }
        if (usuario != null && !usuario.isBlank()) {
            where.append(" AND b.nombre_usuario = ?");
            parametros.add(usuario);
        }
        if (evento != null && !evento.isBlank()) {
            where.append(" AND b.tipo_evento = ?");
            parametros.add(evento);
        }
        if (texto != null && !texto.isBlank()) {
            where.append(" AND LOWER(b.referencia) LIKE ?");
            parametros.add("%" + texto.trim().toLowerCase() + "%");
        }

        String sql = "SELECT b.fecha_hora, b.nombre_usuario, b.rol_usuario, b.tipo_evento, b.referencia,"
                + " b.detalle_movimiento FROM bitacora_Per b"
                + where + " ORDER BY b.fecha_hora DESC, b.id_bitacora DESC LIMIT ? OFFSET ?";
        String sqlTotal = "SELECT COUNT(*) FROM bitacora_Per b" + where;

        int numero = Math.max(pagina, 0);
        List<RegistroBitacora> registros = new ArrayList<>();
        try (Connection con = ConexionBD.obtener()) {
            long total;
            try (PreparedStatement ps = con.prepareStatement(sqlTotal)) {
                Jdbc.asignar(ps, parametros);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getLong(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int i = Jdbc.asignar(ps, parametros);
                ps.setInt(i++, tamano);
                ps.setLong(i, (long) numero * tamano);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        registros.add(new RegistroBitacora(
                                rs.getObject("fecha_hora", LocalDateTime.class),
                                valor(rs.getString("nombre_usuario"), "Sistema"),
                                valor(rs.getString("rol_usuario"), ""),
                                rs.getString("tipo_evento"),
                                valor(rs.getString("referencia"), ""),
                                rs.getString("detalle_movimiento")));
                    }
                }
            }
            return new Pagina<>(registros, numero, tamano, total);
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo consultar la bitácora", e);
        }
    }

    // Nombres de quienes aparecen en la bitácora, para el filtro
    public List<String> usuarios() {
        String sql = "SELECT DISTINCT nombre_usuario FROM bitacora_Per WHERE nombre_usuario IS NOT NULL ORDER BY nombre_usuario";
        List<String> usuarios = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(rs.getString(1));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron consultar los usuarios de la bitácora", e);
        }
    }

    private String valor(String texto, String siFalta) {
        return texto == null ? siFalta : texto;
    }
}
