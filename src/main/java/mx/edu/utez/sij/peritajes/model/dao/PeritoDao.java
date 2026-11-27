package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.bd.ConexionBD;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Padrón de peritos (RF 3.1 – 3.4). Reglas en docs/modulos/peritajes.md, sección 6.7.
// Un perito es un usuario de user_Per con rol PERITAJES_PERITO o PERITAJES_PERITO_ADMIN; sus especialidades
// están en educacion_especialidad_Per y sus materias en perito_materia_Per.
// Los métodos que cambian algo regresan el mensaje de error, o null si todo salió bien.
public class PeritoDao {

    // RF 3.2: 25 registros por página y el administrador puede cambiar la cantidad
    public static final int TAMANO_PAGINA = 25;
    public static final List<Integer> TAMANOS_PAGINA = List.of(10, 25, 50, 100);

    private static final String SELECT = "SELECT u.id_usuario, u.nombre, u.apellido_paterno, u.apellido_materno,"
            + " u.correo, u.Status, r.rol,"
            + " (SELECT GROUP_CONCAT(mt.nombre ORDER BY mt.nombre SEPARATOR ' · ') FROM perito_materia_Per pm"
            + "    JOIN materia_Ofc mt ON mt.id_materia = pm.clave_materia WHERE pm.id_usuario = u.id_usuario) AS materias,"
            + " " + PeritajeDao.CARGA_ASIGNADOS + " AS asignados,"
            + " " + PeritajeDao.CARGA_EN_PROCESO + " AS en_proceso"
            + " FROM user_Per u JOIN Rol r ON r.id_rol = u.id_rol"
            + " WHERE r.rol IN ('" + UsuarioPeritajes.PERITO + "', '" + UsuarioPeritajes.PERITO_ADMIN + "')";

    private final MateriaDao materiaDao = new MateriaDao();
    private final PeritajeDao peritajeDao = new PeritajeDao();
    private final BitacoraDao bitacoraDao = new BitacoraDao();

    // RF 3.1 Registro. El perito siempre empieza Activo. Los datos ya deben venir validados (PeritoFormSupport).
    // contrasenaCifrada: la contraseña temporal ya cifrada (Contrasena.cifrar). Al guardar se le pone el id.
    // El trigger trg_user_Per_login_ai copia el correo y la contraseña a Login.
    public String create(Perito perito, String contrasenaCifrada, UsuarioPeritajes usuario) {
        if (!UsuarioPeritajes.esRolDePerito(perito.getRolSistema())) {
            return "Seleccione si el perito será Perito o Perito Administrador";
        }
        if (!materiaDao.todasExisten(perito.getMaterias())) {
            return "Seleccione al menos una materia válida para la asignación";
        }
        // RF 3.1: si ya existe se avisa y no se registra. Login tiene los correos de TODOS los módulos.
        String duplicado = "El perito ya existe: hay un usuario registrado con el correo " + perito.getCorreo();
        if (existeCorreo(perito.getCorreo())) {
            return duplicado;
        }

        String sql = "INSERT INTO user_Per (nombre, apellido_paterno, apellido_materno, correo, contrasena, id_rol, Status)"
                + " SELECT ?, ?, ?, ?, ?, id_rol, TRUE FROM Rol WHERE rol = ?";
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, perito.getNombre());
                    ps.setString(2, perito.getApellidoPaterno());
                    ps.setString(3, perito.getApellidoMaterno());
                    ps.setString(4, perito.getCorreo());
                    ps.setString(5, contrasenaCifrada);
                    ps.setString(6, perito.getRolSistema());
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return "El rol " + perito.getRolSistema() + " no está en la tabla Rol de la base de datos";
                    }
                    try (ResultSet llaves = ps.getGeneratedKeys()) {
                        llaves.next();
                        perito.setId(llaves.getLong(1));
                    }
                }
                guardarEspecialidadesYMaterias(con, perito);
                // RF 5.2: altas del catálogo
                bitacoraDao.registrar(con, usuario, BitacoraDao.ALTA_PERITO, perito.getNumeroRegistro(),
                        perito.getNombreCompleto() + " (" + perito.getRol() + "). Especialidades: "
                                + String.join(", ", perito.getEspecialidades()) + ". Materias: "
                                + nombresMaterias(perito.getMaterias()) + ".");
                con.commit();
                perito.setActivo(true);
                return null;
            } catch (SQLIntegrityConstraintViolationException e) {
                // Otra persona registró el mismo correo al mismo tiempo (UNIQUE de user_Per o PK de Login)
                con.rollback();
                perito.setId(null);
                return duplicado;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                perito.setId(null);
                throw e;
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo registrar el perito", e);
        }
    }

    // null si no existe o no es perito. Trae especialidades, materias y carga activa.
    public Perito getById(Long id) {
        if (id == null) {
            return null;
        }
        try (Connection con = ConexionBD.obtener()) {
            Perito perito;
            try (PreparedStatement ps = con.prepareStatement(SELECT + " AND u.id_usuario = ?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    perito = leer(rs);
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT descripcion_especialidad FROM educacion_especialidad_Per"
                    + " WHERE id_usuario = ? ORDER BY id_especialidad")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        perito.getEspecialidades().add(rs.getString(1));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT clave_materia FROM perito_materia_Per WHERE id_usuario = ?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        perito.getMaterias().add(rs.getLong(1));
                    }
                }
            }
            return perito;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo consultar el perito", e);
        }
    }

    // RF 3.3 Edición: nombre, apellidos, especialidades y materias. El correo y el rol no se cambian aquí.
    // Cambiar las materias no afecta los casos ya asignados (6.7).
    public String update(Perito datos, UsuarioPeritajes usuario) {
        if (!materiaDao.todasExisten(datos.getMaterias())) {
            return "Seleccione al menos una materia válida para la asignación";
        }
        String sql = "UPDATE user_Per SET nombre = ?, apellido_paterno = ?, apellido_materno = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, datos.getNombre());
                    ps.setString(2, datos.getApellidoPaterno());
                    ps.setString(3, datos.getApellidoMaterno());
                    ps.setLong(4, datos.getId());
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return "El perito no existe";
                    }
                }
                // Se reemplazan las especialidades y las materias
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM educacion_especialidad_Per WHERE id_usuario = ?")) {
                    ps.setLong(1, datos.getId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM perito_materia_Per WHERE id_usuario = ?")) {
                    ps.setLong(1, datos.getId());
                    ps.executeUpdate();
                }
                guardarEspecialidadesYMaterias(con, datos);
                // RF 3.3 y 5.2: cambios de datos y de especialidad
                bitacoraDao.registrar(con, usuario, BitacoraDao.EDICION_PERITO, datos.getNumeroRegistro(),
                        "Datos actualizados. Especialidades: " + String.join(", ", datos.getEspecialidades())
                                + ". Materias: " + nombresMaterias(datos.getMaterias()) + ".");
                con.commit();
                return null;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron guardar los cambios del perito", e);
        }
    }

    // RF 3.4 Baja o reactivación (no se borra el perito; columna Status). No se puede desactivar si tiene
    // peritajes Asignados o En Proceso. Un perito inactivo no inicia sesión ni recibe asignaciones.
    public String cambiarEstado(Long id, boolean activo, UsuarioPeritajes usuario) {
        Perito perito = getById(id);
        if (perito == null) {
            return "El perito no existe";
        }
        if (perito.isActivo() == activo) {
            return null;   // no hay cambio
        }
        if (!activo) {
            List<Peritaje> activos = peritajeDao.activosDelPerito(id);
            if (!activos.isEmpty()) {
                String folios = activos.stream().map(Peritaje::getFolio).collect(Collectors.joining(", "));
                // RF 3.4 y regla de negocio 8 del DFR
                return "El perito tiene " + activos.size() + " peritajes activos (" + folios
                        + "). Primero deben reasignarse para poder darlo de baja";
            }
        }

        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("UPDATE user_Per SET Status = ? WHERE id_usuario = ?")) {
                    ps.setBoolean(1, activo);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                }
                // RF 3.4 y 5.2: cada cambio de estado
                bitacoraDao.registrar(con, usuario, activo ? BitacoraDao.REACTIVACION_PERITO : BitacoraDao.BAJA_PERITO,
                        perito.getNumeroRegistro(),
                        perito.getNombreCompleto() + (activo ? ": Inactivo → Activo." : ": Activo → Inactivo."));
                con.commit();
                return null;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo cambiar el estado del perito", e);
        }
    }

    // Catálogo (RF 3.2). Cada filtro es opcional (null o vacío = no filtra). La página empieza en 0.
    // texto busca en el nombre completo y en el número de registro (PER-0005).
    // Si piden un tamaño de página que no está en TAMANOS_PAGINA, se usa el de 25.
    public Pagina<Perito> buscar(String texto, String especialidad, Long materiaId, Boolean activo,
                                 int pagina, int tamano) {
        StringBuilder where = new StringBuilder();
        List<Object> parametros = new ArrayList<>();
        if (texto != null && !texto.isBlank()) {
            String t = "%" + texto.trim().toLowerCase() + "%";
            where.append(" AND (LOWER(CONCAT_WS(' ', u.nombre, u.apellido_paterno, u.apellido_materno)) LIKE ?"
                    + " OR LOWER(CONCAT('PER-', LPAD(u.id_usuario, 4, '0'))) LIKE ?)");
            parametros.add(t);
            parametros.add(t);
        }
        if (especialidad != null && !especialidad.isBlank()) {
            where.append(" AND EXISTS (SELECT 1 FROM educacion_especialidad_Per e"
                    + " WHERE e.id_usuario = u.id_usuario AND e.descripcion_especialidad = ?)");
            parametros.add(especialidad.trim());
        }
        if (materiaId != null) {
            where.append(" AND EXISTS (SELECT 1 FROM perito_materia_Per pm2"
                    + " WHERE pm2.id_usuario = u.id_usuario AND pm2.clave_materia = ?)");
            parametros.add(materiaId);
        }
        if (activo != null) {
            where.append(" AND u.Status = ?");
            parametros.add(activo);
        }

        int numero = Math.max(pagina, 0);
        int tamanoValido = TAMANOS_PAGINA.contains(tamano) ? tamano : TAMANO_PAGINA;
        String sqlTotal = "SELECT COUNT(*) FROM user_Per u JOIN Rol r ON r.id_rol = u.id_rol"
                + " WHERE r.rol IN ('" + UsuarioPeritajes.PERITO + "', '" + UsuarioPeritajes.PERITO_ADMIN + "')" + where;
        String sql = SELECT + where + " ORDER BY u.nombre, u.apellido_paterno LIMIT ? OFFSET ?";

        List<Perito> peritos = new ArrayList<>();
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
                ps.setInt(i++, tamanoValido);
                ps.setLong(i, (long) numero * tamanoValido);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        peritos.add(leer(rs));
                    }
                }
            }
            return new Pagina<>(peritos, numero, tamanoValido, total);
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo consultar el catálogo de peritos", e);
        }
    }

    // Todos los peritos (activos e inactivos), para el filtro de perito de la lista de casos
    public List<Perito> listarTodos() {
        List<Perito> peritos = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY u.nombre, u.apellido_paterno");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                peritos.add(leer(rs));
            }
            return peritos;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron consultar los peritos", e);
        }
    }

    // Peritajes Asignados y En Proceso del perito (pantalla de edición y baja)
    public List<Peritaje> peritajesActivos(Long peritoId) {
        return peritajeDao.activosDelPerito(peritoId);
    }

    public long contar() {
        return buscar(null, null, null, null, 0, TAMANO_PAGINA).getTotalElements();
    }

    public long contarPorEstado(boolean activo) {
        return buscar(null, null, null, activo, 0, TAMANO_PAGINA).getTotalElements();
    }

    // RF 3.2: total de peritos por materia, por ejemplo {Civil=3, Familiar=2, ...}
    public Map<String, Long> totalesPorMateria() {
        String sql = "SELECT mt.nombre, COUNT(u.id_usuario) FROM materia_Ofc mt"
                + " LEFT JOIN perito_materia_Per pm ON pm.clave_materia = mt.id_materia"
                + " LEFT JOIN user_Per u ON u.id_usuario = pm.id_usuario"
                + " WHERE mt.estado = 'ACTIVO' GROUP BY mt.id_materia, mt.nombre ORDER BY mt.nombre";
        Map<String, Long> totales = new LinkedHashMap<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                totales.put(rs.getString(1), rs.getLong(2));
            }
            return totales;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron contar los peritos por materia", e);
        }
    }

    // Especialidades distintas ya registradas, para el filtro del catálogo
    public List<String> especialidadesRegistradas() {
        String sql = "SELECT DISTINCT descripcion_especialidad FROM educacion_especialidad_Per ORDER BY descripcion_especialidad";
        List<String> especialidades = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                especialidades.add(rs.getString(1));
            }
            return especialidades;
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudieron consultar las especialidades", e);
        }
    }

    // El correo ya está en Login (de cualquier módulo: el script no deja repetirlo)
    public boolean existeCorreo(String correo) {
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM Login WHERE LOWER(correo) = LOWER(?)")) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo revisar el correo", e);
        }
    }

    private void guardarEspecialidadesYMaterias(Connection con, Perito perito) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO educacion_especialidad_Per (id_usuario, descripcion_especialidad) VALUES (?, ?)")) {
            for (String especialidad : perito.getEspecialidades()) {
                ps.setLong(1, perito.getId());
                ps.setString(2, especialidad);
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO perito_materia_Per (id_usuario, clave_materia) VALUES (?, ?)")) {
            for (Long materiaId : perito.getMaterias()) {
                ps.setLong(1, perito.getId());
                ps.setLong(2, materiaId);
                ps.executeUpdate();
            }
        }
    }

    // "Civil · Familiar" a partir de los ids
    private String nombresMaterias(Set<Long> ids) {
        return materiaDao.listarActivas().stream()
                .filter(m -> ids.contains(m.getId()))
                .map(m -> m.getNombre())
                .collect(Collectors.joining(" · "));
    }

    private Perito leer(ResultSet rs) throws SQLException {
        Perito p = new Perito();
        p.setId(rs.getLong("id_usuario"));
        p.setNombre(rs.getString("nombre"));
        p.setApellidoPaterno(rs.getString("apellido_paterno"));
        p.setApellidoMaterno(rs.getString("apellido_materno"));
        p.setCorreo(rs.getString("correo"));
        p.setActivo(rs.getBoolean("Status"));
        p.setRolSistema(rs.getString("rol"));
        p.setMateriasTexto(rs.getString("materias") == null ? "" : rs.getString("materias"));
        p.setCargaAsignados(rs.getLong("asignados"));
        p.setCargaEnProceso(rs.getLong("en_proceso"));
        p.setEspecialidades(new ArrayList<>());
        p.setMaterias(new LinkedHashSet<>());
        return p;
    }
}
