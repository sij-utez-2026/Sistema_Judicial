package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.peritajes.model.EstadoPeritaje;
import mx.edu.utez.sij.peritajes.model.Materia;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static mx.edu.utez.sij.peritajes.model.dao.BaseDatosMemoria.PERITAJES;
import static mx.edu.utez.sij.peritajes.model.dao.BaseDatosMemoria.PERITOS;

// Padrón de peritos (RF 3.1 – 3.4). Reglas en docs/modulos/peritajes.md, sección 6.7.
// Tablas: per_perito, per_perito_especialidad y per_perito_materia (de materias solo el id).
// Pendiente con el núcleo: crear la cuenta con usuarioService.crear(nombre, correo, "PERITAJES_PERITO") y la baja
// o reactivación con usuarioService.quitarRol(...) / agregarRol(...).
public class PeritoDao {

    // RF 3.2: 25 registros por página y el administrador puede cambiar la cantidad
    public static final int TAMANO_PAGINA = 25;
    public static final List<Integer> TAMANOS_PAGINA = List.of(10, 25, 50, 100);

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    private final MateriaDao materiaDao = new MateriaDao();
    private final BitacoraDao bitacoraDao = new BitacoraDao();

    // RF 3.1 Registro. El perito siempre empieza Activo. Los datos ya deben venir validados (PeritoFormSupport).
    // SQL (en una transacción): INSERT INTO per_perito (...) VALUES (...); un INSERT por especialidad
    //      en per_perito_especialidad y uno por materia en per_perito_materia.
    public boolean create(Perito perito, UsuarioPeritajes usuario) {
        perito.setId(BaseDatosMemoria.ID_PERITO.getAndIncrement());
        perito.setActivo(true);
        perito.setCreadoEn(LocalDateTime.now(ZONA));
        PERITOS.put(perito.getId(), perito);

        // RF 5.2: altas del catálogo
        bitacoraDao.registrar(usuario, BitacoraDao.ALTA_PERITO, perito.getNumeroRegistro(),
                perito.getNombreCompleto() + ". Especialidades: " + String.join(", ", perito.getEspecialidades())
                        + ". Materias: " + materiaDao.nombres(perito.getMateriasIds()) + ".");
        return true;
    }

    // null si no existe.
    // SQL: SELECT ... FROM per_perito WHERE id = ?  (+ sus especialidades y materias)
    public Perito getById(Long id) {
        Perito perito = id == null ? null : PERITOS.get(id);
        if (perito != null) {
            completar(perito);
        }
        return perito;
    }

    // RF 3.3 Edición. El correo y el número de registro no se cambian aunque lleguen en el formulario.
    // Cambiar las materias no afecta los casos ya asignados (6.7).
    // SQL: UPDATE per_perito SET nombre = ?, apellido_paterno = ?, apellido_materno = ?, telefono = ?,
    //      actualizado_en = ? WHERE id = ?; y se reemplazan sus especialidades y materias (DELETE + INSERT).
    public boolean update(Perito datos, UsuarioPeritajes usuario) {
        Perito perito = PERITOS.get(datos.getId());
        if (perito == null) {
            return false;
        }
        perito.setNombre(datos.getNombre());
        perito.setApellidoPaterno(datos.getApellidoPaterno());
        perito.setApellidoMaterno(datos.getApellidoMaterno());
        perito.setTelefono(datos.getTelefono());
        perito.setEspecialidades(new ArrayList<>(datos.getEspecialidades()));
        perito.setMateriasIds(new LinkedHashSet<>(datos.getMateriasIds()));
        perito.setActualizadoEn(LocalDateTime.now(ZONA));

        // RF 3.3 y 5.2: cambios de datos y de especialidad
        bitacoraDao.registrar(usuario, BitacoraDao.EDICION_PERITO, perito.getNumeroRegistro(),
                "Datos actualizados. Especialidades: " + String.join(", ", perito.getEspecialidades())
                        + ". Materias: " + materiaDao.nombres(perito.getMateriasIds()) + ".");
        return true;
    }

    // RF 3.4 Baja o reactivación (no se borra el perito). No se puede desactivar si tiene peritajes
    // Asignados o En Proceso. Regresa el mensaje de error, o null si se aplicó.
    // SQL: UPDATE per_perito SET activo = ?, actualizado_en = ? WHERE id = ?
    public String cambiarEstado(Long id, boolean activo, UsuarioPeritajes usuario) {
        Perito perito = id == null ? null : PERITOS.get(id);
        if (perito == null) {
            return "El perito no existe";
        }

        if (!activo && perito.isActivo()) {
            List<Peritaje> activos = peritajesActivos(id);
            if (!activos.isEmpty()) {
                String folios = activos.stream().map(Peritaje::getFolio).collect(Collectors.joining(", "));
                // RF 3.4 y regla de negocio 8 del DFR
                return "El perito tiene " + activos.size() + " peritajes activos (" + folios
                        + "). Primero deben reasignarse para poder darlo de baja";
            }
        }

        boolean cambia = perito.isActivo() != activo;
        perito.setActivo(activo);
        perito.setActualizadoEn(LocalDateTime.now(ZONA));

        // RF 3.4 y 5.2: cada cambio de estado
        if (cambia) {
            bitacoraDao.registrar(usuario, activo ? BitacoraDao.REACTIVACION_PERITO : BitacoraDao.BAJA_PERITO,
                    perito.getNumeroRegistro(),
                    perito.getNombreCompleto() + (activo ? ": Inactivo → Activo." : ": Activo → Inactivo."));
        }
        return null;
    }

    // Catálogo (RF 3.2). Cada filtro es opcional (null o vacío = no filtra). La página empieza en 0.
    // Si piden un tamaño de página que no está en TAMANOS_PAGINA, se usa el de 25.
    // SQL: SELECT ... FROM per_perito p WHERE
    //        (? IS NULL OR LOWER(CONCAT_WS(' ', p.nombre, p.apellido_paterno, p.apellido_materno)) LIKE ?
    //                   OR LOWER(p.numero_registro) LIKE ?)
    //        AND (? IS NULL OR EXISTS (SELECT 1 FROM per_perito_especialidad e WHERE e.perito_id = p.id AND e.especialidad = ?))
    //        AND (? IS NULL OR EXISTS (SELECT 1 FROM per_perito_materia m WHERE m.perito_id = p.id AND m.materia_id = ?))
    //        AND (? IS NULL OR p.activo = ?)
    //      ORDER BY p.nombre, p.apellido_paterno LIMIT ? OFFSET ?
    //      y otro SELECT COUNT(*) con los mismos filtros para el total (Pagina.offset ayuda con el OFFSET).
    public Pagina<Perito> buscar(String texto, String especialidad, Long materiaId, Boolean activo,
                                 int pagina, int tamano) {
        String t = vacioANull(texto) == null ? null : texto.trim().toLowerCase();
        String esp = vacioANull(especialidad);
        int tamanoValido = TAMANOS_PAGINA.contains(tamano) ? tamano : TAMANO_PAGINA;

        List<Perito> filtrados = PERITOS.values().stream()
                .filter(p -> t == null
                        || p.getNombreCompleto().toLowerCase().contains(t)
                        || p.getNumeroRegistro().toLowerCase().contains(t))
                .filter(p -> esp == null || p.getEspecialidades().contains(esp))
                .filter(p -> materiaId == null || p.getMateriasIds().contains(materiaId))
                .filter(p -> activo == null || p.isActivo() == activo)
                .sorted(Comparator.comparing(Perito::getNombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Perito::getApellidoPaterno, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Pagina<Perito> resultado = Pagina.de(filtrados, Math.max(pagina, 0), tamanoValido);
        resultado.getContent().forEach(this::completar);
        return resultado;
    }

    // Candidatos a asignación (RF 2.2 y 2.3) y el "Ver como" temporal.
    // SQL: SELECT ... FROM per_perito WHERE activo = TRUE
    public List<Perito> listarActivos() {
        return PERITOS.values().stream().filter(Perito::isActivo).toList();
    }

    // Peritajes Asignados y En Proceso del perito, del más antiguo al más reciente (pantalla de edición y baja).
    // SQL: SELECT ... FROM per_peritaje WHERE perito_id = ? AND estado IN ('ASIGNADO','EN_PROCESO') ORDER BY fecha_solicitud
    public List<Peritaje> peritajesActivos(Long peritoId) {
        return PERITAJES.values().stream()
                .filter(p -> peritoId.equals(p.getPeritoId()) && p.getEstado().isActivo())
                .sorted(Comparator.comparing(Peritaje::getFechaSolicitud))
                .toList();
    }

    // SQL: SELECT COUNT(*) FROM per_perito
    public long contar() {
        return PERITOS.size();
    }

    // SQL: SELECT COUNT(*) FROM per_perito WHERE activo = ?
    public long contarPorEstado(boolean activo) {
        return PERITOS.values().stream().filter(p -> p.isActivo() == activo).count();
    }

    // RF 3.2: total de peritos por materia, por ejemplo {Civil=3, Familiar=2, ...}
    // SQL: SELECT COUNT(*) FROM per_perito_materia WHERE materia_id = ?  (una vez por materia)
    public Map<String, Long> totalesPorMateria() {
        Map<String, Long> totales = new LinkedHashMap<>();
        for (Materia materia : materiaDao.listarActivas()) {
            long total = PERITOS.values().stream().filter(p -> p.getMateriasIds().contains(materia.getId())).count();
            totales.put(materia.getNombre(), total);
        }
        return totales;
    }

    // Especialidades distintas ya registradas, para el filtro del catálogo.
    // SQL: SELECT DISTINCT especialidad FROM per_perito_especialidad ORDER BY especialidad
    public List<String> especialidadesRegistradas() {
        return PERITOS.values().stream()
                .flatMap(p -> p.getEspecialidades().stream())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    // SQL: SELECT COUNT(*) FROM per_perito WHERE LOWER(numero_registro) = LOWER(?)
    public boolean existeNumeroRegistro(String numeroRegistro) {
        return PERITOS.values().stream().anyMatch(p -> p.getNumeroRegistro().equalsIgnoreCase(numeroRegistro));
    }

    // SQL: SELECT COUNT(*) FROM per_perito WHERE LOWER(correo) = LOWER(?)
    public boolean existeCorreo(String correo) {
        return PERITOS.values().stream().anyMatch(p -> p.getCorreo().equalsIgnoreCase(correo));
    }

    // Llena los campos de solo lectura: nombres de las materias y carga activa (6.1).
    // SQL: SELECT COUNT(*) FROM per_peritaje WHERE perito_id = ? AND estado = ?  (Asignado y En Proceso)
    private void completar(Perito perito) {
        perito.setMateriasTexto(materiaDao.nombres(perito.getMateriasIds()));
        perito.setCargaAsignados(contarPeritajes(perito.getId(), EstadoPeritaje.ASIGNADO));
        perito.setCargaEnProceso(contarPeritajes(perito.getId(), EstadoPeritaje.EN_PROCESO));
    }

    private long contarPeritajes(Long peritoId, EstadoPeritaje estado) {
        return PERITAJES.values().stream()
                .filter(p -> peritoId.equals(p.getPeritoId()) && p.getEstado() == estado)
                .count();
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
