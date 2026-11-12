package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.peritajes.model.CandidatoAsignacion;
import mx.edu.utez.sij.peritajes.model.EstadoPeritaje;
import mx.edu.utez.sij.peritajes.model.HistorialAsignacion;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.TipoAsignacion;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static mx.edu.utez.sij.peritajes.model.dao.BaseDatosMemoria.PERITAJES;

// Peritajes: recepción de solicitudes y asignación automática (RF 2.1 y 2.2), reasignación manual (RF 2.3),
// lista de casos, "Mis peritajes" y apertura del expediente (RF 4.1 y 4.2). docs/modulos/peritajes.md 6.2 y 6.3.
// Tablas per_peritaje y per_peritaje_materia (de materias solo el id).
// Los métodos que cambian algo regresan el mensaje de error, o null si todo salió bien.
// Pendiente con el núcleo: avisar con notificacionService al perito asignado, al nuevo y al que deja el caso (RF 5.1).
public class PeritajeDao {

    // Peritajes pagina de 25 en 25 (docs/ARQUITECTURA.md, sección 8)
    public static final int TAMANO_PAGINA = 25;

    public static final String NO_EXISTE = "El peritaje no existe";
    public static final String SIN_ACCESO = "No tiene acceso a este peritaje: no está asignado a su cuenta";
    public static final String SIN_PERITOS = "No hay peritos disponibles en esta materia";

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    private static final Random AZAR = new Random();

    private final PeritoDao peritoDao = new PeritoDao();
    private final MateriaDao materiaDao = new MateriaDao();
    private final ExpedienteDao expedienteDao = new ExpedienteDao();
    private final HistorialAsignacionDao historialDao = new HistorialAsignacionDao();
    private final DictamenDao dictamenDao = new DictamenDao();
    private final BitacoraDao bitacoraDao = new BitacoraDao();

    // null si no existe.
    // SQL: SELECT ... FROM per_peritaje WHERE id = ?  (+ sus materias)
    public Peritaje getById(Long id) {
        Peritaje peritaje = id == null ? null : PERITAJES.get(id);
        if (peritaje != null) {
            completar(peritaje);
        }
        return peritaje;
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

    // Lista de casos con filtros opcionales (null = no filtra): folio o expediente, materia, estado y perito.
    public Pagina<Peritaje> buscar(String texto, Long materiaId, EstadoPeritaje estado, Long peritoId, int pagina) {
        return buscar(texto, materiaId, estado == null ? null : EnumSet.of(estado), peritoId, pagina);
    }

    // Cuántos peritajes hay en cada estado (pestañas de la lista). Llaves de texto ("ASIGNADO") para la JSP.
    // SQL: SELECT estado, COUNT(*) FROM per_peritaje GROUP BY estado
    public Map<String, Long> contarPorEstado() {
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (EstadoPeritaje estado : EstadoPeritaje.values()) {
            conteo.put(estado.name(), PERITAJES.values().stream().filter(p -> p.getEstado() == estado).count());
        }
        return conteo;
    }

    // SQL: SELECT COUNT(*) FROM per_peritaje
    public long contar() {
        return PERITAJES.size();
    }

    // ===================== Peritos: Mis peritajes =====================

    // RF 4.1: solo los peritajes del perito en sesión.
    // filtro: "activos" (Asignado y En Proceso), el nombre de un estado, o "todos".
    public Pagina<Peritaje> misPeritajes(UsuarioPeritajes usuario, String texto, String filtro, int pagina) {
        if (usuario.getPeritoId() == null) {
            // Un Perito Administrador que no está en el catálogo no tiene casos
            return new Pagina<>(List.of(), Math.max(pagina, 0), TAMANO_PAGINA, 0);
        }
        Set<EstadoPeritaje> estados;
        EstadoPeritaje estado = EstadoPeritaje.deTexto(filtro);
        if ("todos".equals(filtro)) {
            estados = null;
        } else if (estado != null) {
            estados = EnumSet.of(estado);
        } else {
            // "activos" o cualquier otro valor
            estados = EnumSet.of(EstadoPeritaje.ASIGNADO, EstadoPeritaje.EN_PROCESO);
        }
        return buscar(texto, null, estados, usuario.getPeritoId(), pagina);
    }

    // Cuántos peritajes del perito hay en un estado (tarjetas de "Mis peritajes").
    // SQL: SELECT COUNT(*) FROM per_peritaje WHERE perito_id = ? AND estado = ?
    public long contarDelPerito(UsuarioPeritajes usuario, EstadoPeritaje estado) {
        Long peritoId = usuario.getPeritoId();
        if (peritoId == null) {
            return 0;
        }
        return PERITAJES.values().stream()
                .filter(p -> peritoId.equals(p.getPeritoId()) && p.getEstado() == estado)
                .count();
    }

    // RF 4.2: al abrir el expediente por primera vez en Asignado, pasa a En Proceso y se guarda la fecha.
    // En aperturas posteriores el estado no cambia. Regresa el error (acceso denegado o cancelado), o null.
    public String abrirExpediente(Peritaje peritaje, UsuarioPeritajes usuario) {
        return abrirExpediente(peritaje, usuario, LocalDateTime.now(ZONA));
    }

    // Con fecha explícita: la usan los datos de prueba.
    // SQL: UPDATE per_peritaje SET estado = 'EN_PROCESO', fecha_apertura = ? WHERE id = ? AND estado = 'ASIGNADO'
    public String abrirExpediente(Peritaje peritaje, UsuarioPeritajes usuario, LocalDateTime fecha) {
        String acceso = validarAcceso(peritaje, usuario);
        if (acceso != null) {
            return acceso;
        }
        if (peritaje.getEstado() == EstadoPeritaje.CANCELADO) {
            return "El peritaje " + peritaje.getFolio() + " fue cancelado";
        }
        if (peritaje.getEstado() == EstadoPeritaje.ASIGNADO) {
            peritaje.setEstado(EstadoPeritaje.EN_PROCESO);
            if (peritaje.getFechaApertura() == null) {
                peritaje.setFechaApertura(fecha);
            }
            bitacoraDao.registrar(fecha, usuario.getNombre(), usuario.getRol(), BitacoraDao.APERTURA_EXPEDIENTE,
                    peritaje.getFolio(), "Primera apertura por el perito asignado. Asignado → En Proceso.");
        }
        return null;
    }

    // ===================== Solicitud y asignación (DFR 3.3.4) =====================

    // Peritos elegibles: activos y con al menos una de las materias (basta con que coincida una).
    // Ordenados de menor a mayor carga activa. excluirPeritoId: el perito actual, en la reasignación.
    public List<CandidatoAsignacion> candidatos(Set<Long> materiasIds, Long excluirPeritoId) {
        return peritoDao.listarActivos().stream()
                .filter(p -> !p.getId().equals(excluirPeritoId))
                .filter(p -> p.getMateriasIds().stream().anyMatch(materiasIds::contains))
                .map(p -> new CandidatoAsignacion(p.getId(), p.getNombreCompleto(),
                        materiaDao.nombres(p.getMateriasIds()), contarCargaActiva(p.getId()), false))
                .sorted(Comparator.comparingLong(CandidatoAsignacion::getCarga)
                        .thenComparing(CandidatoAsignacion::getNombre))
                .toList();
    }

    // RF 2.1: revisa la solicitud de Audiencias antes de guardarla. Regresa el error, o null si se puede solicitar.
    // Los textos de error no están en el DFR [Confirmar], salvo el de "sin peritos" (Audiencias 6.1).
    public String validarSolicitud(String numeroExpediente, Set<Long> materiasIds) {
        if (expedienteDao.buscar(numeroExpediente) == null) {
            return "El expediente " + numeroExpediente + " no existe";
        }
        if (!materiaDao.todasExisten(materiasIds)) {
            return "La solicitud debe indicar al menos una materia válida";
        }
        if (candidatos(materiasIds, null).isEmpty()) {
            return SIN_PERITOS;
        }
        return null;
    }

    // RF 2.1 y 2.2: guarda la solicitud con folio nuevo y la asigna al perito de menor carga.
    // Antes hay que llamar a validarSolicitud. Regresa el peritaje ya Asignado, o null si no hubo peritos.
    // TEMPORAL: cuando exista nucleo/contratos/peritajes/SolicitudPeritajeApi, la clase que la implemente usa este método.
    public Peritaje solicitar(String numeroExpediente, Long audienciaId, Set<Long> materiasIds, String observaciones) {
        return solicitar(numeroExpediente, audienciaId, materiasIds, observaciones, LocalDateTime.now(ZONA), null, null);
    }

    // Con fecha, folio y desempate explícitos: solo para los datos de prueba (folios y peritos del boceto).
    // folio null = el siguiente consecutivo; desempate null = al azar.
    // SQL (en una sola transacción): INSERT INTO per_peritaje (...) VALUES (...); un INSERT por materia en
    //      per_peritaje_materia; después la asignación (UPDATE per_peritaje, historial y bitácora).
    public Peritaje solicitar(String numeroExpediente, Long audienciaId, Set<Long> materiasIds, String observaciones,
                              LocalDateTime fecha, String folio, Long desempate) {
        synchronized (PERITAJES) {
            List<CandidatoAsignacion> candidatos = candidatos(materiasIds, null);
            if (candidatos.isEmpty()) {
                return null;   // sin peritos elegibles no se guarda nada (Audiencias 6.1)
            }

            // Se guarda en Pendiente de Asignación con folio nuevo
            Peritaje peritaje = new Peritaje();
            peritaje.setId(BaseDatosMemoria.ID_PERITAJE.getAndIncrement());
            peritaje.setFolio(folio != null ? folio : siguienteFolio(fecha.getYear()));
            peritaje.setNumeroExpediente(numeroExpediente);
            peritaje.setAudienciaId(audienciaId);
            peritaje.setMateriasIds(new LinkedHashSet<>(materiasIds));
            peritaje.setObservaciones(observaciones);
            peritaje.setEstado(EstadoPeritaje.PENDIENTE_ASIGNACION);
            peritaje.setFechaSolicitud(fecha);
            PERITAJES.put(peritaje.getId(), peritaje);

            asignarAutomaticamente(peritaje, candidatos, fecha, desempate);
            completar(peritaje);
            return peritaje;
        }
    }

    // RF 2.3 Reasignación manual. Solo en Asignado o En Proceso y el estado no cambia (decisión del equipo).
    // Si estaba En Proceso, el borrador del perito anterior se archiva y el nuevo empieza su propio dictamen.
    // La carga no es restricción (DFR 2.4 Restricciones 4). Regresa el error, o null si se reasignó.
    // SQL (en una sola transacción): UPDATE per_peritaje SET perito_id = ? WHERE id = ?; archivar el borrador,
    //      INSERT en per_historial_asignacion y en la bitácora.
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
        // DFR 2.3: el nuevo perito debe estar activo y tener al menos una de las materias
        Perito nuevo = peritoDao.getById(nuevoPeritoId);
        boolean elegible = nuevo != null && nuevo.isActivo()
                && nuevo.getMateriasIds().stream().anyMatch(peritaje.getMateriasIds()::contains);
        if (!elegible) {
            return "El perito elegido no está activo o no tiene ninguna de las materias del peritaje";
        }

        LocalDateTime ahora = LocalDateTime.now(ZONA);
        Long anteriorId = peritaje.getPeritoId();
        String anteriorNombre = peritaje.getPeritoNombre();

        // El borrador del perito que deja el caso se archiva (peritajes.md 6.3)
        dictamenDao.archivarBorrador(peritajeId, anteriorId);
        peritaje.setPeritoId(nuevoPeritoId);

        HistorialAsignacion historial = new HistorialAsignacion();
        historial.setPeritajeId(peritajeId);
        historial.setPeritoAnteriorId(anteriorId);
        historial.setPeritoNuevoId(nuevoPeritoId);
        historial.setTipo(TipoAsignacion.MANUAL);
        historial.setMotivo(motivo.trim());
        historial.setRealizadoPor(usuario.getNombre());
        historial.setFecha(ahora);
        historialDao.create(historial);

        // RF 2.3: administrador, fecha y hora, perito saliente y entrante (y el motivo, RF 5.2)
        bitacoraDao.registrar(usuario, BitacoraDao.REASIGNACION_MANUAL, peritaje.getFolio(),
                anteriorNombre + " → " + nuevo.getNombreCompleto() + ". Estado conservado: "
                        + peritaje.getEstado().getTexto() + ". Motivo: " + motivo.trim());
        // TODO núcleo: avisar al nuevo perito y al que deja el caso (RF 2.3 y 5.1)
        return null;
    }

    // RF 2.2 Asignación automática: el perito elegible con menor carga activa; si hay empate, uno al azar.
    // El peritaje pasa a Asignado y queda en el historial (con los candidatos) y en la bitácora.
    // desempate: solo para los datos de prueba (debe ser uno de los empatados).
    private void asignarAutomaticamente(Peritaje peritaje, List<CandidatoAsignacion> candidatos,
                                        LocalDateTime fecha, Long desempate) {
        long menorCarga = candidatos.get(0).getCarga();
        List<CandidatoAsignacion> empatados = candidatos.stream().filter(c -> c.getCarga() == menorCarga).toList();
        CandidatoAsignacion elegido;
        if (desempate == null) {
            elegido = empatados.get(AZAR.nextInt(empatados.size()));
        } else {
            elegido = empatados.stream().filter(c -> c.getPeritoId().equals(desempate)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("El perito de desempate no tiene la menor carga"));
        }

        peritaje.setPeritoId(elegido.getPeritoId());
        peritaje.setEstado(EstadoPeritaje.ASIGNADO);
        peritaje.setFechaAsignacion(fecha);

        HistorialAsignacion historial = new HistorialAsignacion();
        historial.setPeritajeId(peritaje.getId());
        historial.setPeritoNuevoId(elegido.getPeritoId());
        historial.setTipo(TipoAsignacion.AUTOMATICA);
        historial.setRealizadoPor("Sistema");
        historial.setFecha(fecha);
        historial.setCandidatos(candidatos.stream()
                .map(c -> c.conSeleccion(c.getPeritoId().equals(elegido.getPeritoId())))
                .toList());
        historialDao.create(historial);

        // Materias del peritaje que también tiene el perito elegido
        Set<Long> delPerito = peritoDao.getById(elegido.getPeritoId()).getMateriasIds();
        Set<Long> enComun = new LinkedHashSet<>(peritaje.getMateriasIds());
        enComun.retainAll(delPerito);

        // RF 2.2: la asignación se registra en la bitácora (perito, fecha y resultado)
        bitacoraDao.registrar(fecha, "Sistema", "Asignación automática", BitacoraDao.ASIGNACION_AUTOMATICA,
                peritaje.getFolio(), elegido.getNombre() + ": activo; coincide en " + materiaDao.nombres(enComun)
                        + "; menor carga previa: " + elegido.getCarga() + ". Carga posterior: " + (elegido.getCarga() + 1) + ".");
        // TODO núcleo: notificacionService.avisar(perito, "Nuevo peritaje asignado", folio) (RF 5.1)
    }

    // Cada filtro es opcional (null = no filtra). La página empieza en 0. Del más reciente al más antiguo.
    // SQL: SELECT ... FROM per_peritaje p WHERE
    //        (? IS NULL OR LOWER(p.folio) LIKE ? OR LOWER(p.numero_expediente) LIKE ?)
    //        AND (? IS NULL OR EXISTS (SELECT 1 FROM per_peritaje_materia m WHERE m.peritaje_id = p.id AND m.materia_id = ?))
    //        AND p.estado IN (?, ?, ...)            -- solo si estados no es null (un ? por estado)
    //        AND (? IS NULL OR p.perito_id = ?)
    //      ORDER BY p.fecha_solicitud DESC LIMIT ? OFFSET ?
    //      y otro SELECT COUNT(*) con los mismos filtros para el total.
    private Pagina<Peritaje> buscar(String texto, Long materiaId, Collection<EstadoPeritaje> estados, Long peritoId,
                                    int pagina) {
        String t = (texto == null || texto.isBlank()) ? null : texto.trim().toLowerCase();

        List<Peritaje> filtrados = PERITAJES.values().stream()
                .filter(p -> t == null
                        || p.getFolio().toLowerCase().contains(t)
                        || p.getNumeroExpediente().toLowerCase().contains(t))
                .filter(p -> materiaId == null || p.getMateriasIds().contains(materiaId))
                .filter(p -> estados == null || estados.contains(p.getEstado()))
                .filter(p -> peritoId == null || peritoId.equals(p.getPeritoId()))
                .sorted(Comparator.comparing(Peritaje::getFechaSolicitud).reversed())
                .toList();

        Pagina<Peritaje> resultado = Pagina.de(filtrados, Math.max(pagina, 0), TAMANO_PAGINA);
        resultado.getContent().forEach(this::completar);
        return resultado;
    }

    // Carga activa de un perito: Asignado + En Proceso (peritajes.md 6.1).
    // SQL: SELECT COUNT(*) FROM per_peritaje WHERE perito_id = ? AND estado IN ('ASIGNADO','EN_PROCESO')
    private long contarCargaActiva(Long peritoId) {
        return PERITAJES.values().stream()
                .filter(p -> peritoId.equals(p.getPeritoId()) && p.getEstado().isActivo())
                .count();
    }

    // SGPJ-AAAA-NNNN: el consecutivo del año sigue al último folio guardado.
    // SQL: SELECT MAX(folio) FROM per_peritaje WHERE folio LIKE CONCAT(?, '%')
    private String siguienteFolio(int anio) {
        String prefijo = "SGPJ-" + anio + "-";
        int ultimo = PERITAJES.values().stream()
                .map(Peritaje::getFolio)
                .filter(f -> f.startsWith(prefijo))
                .max(Comparator.naturalOrder())
                .map(f -> Integer.parseInt(f.substring(prefijo.length())))
                .orElse(0);
        return prefijo + String.format("%04d", ultimo + 1);
    }

    // Llena los campos de solo lectura: juzgado (Oficialía), materias y perito (como un JOIN).
    private void completar(Peritaje peritaje) {
        peritaje.setJuzgado(expedienteDao.juzgado(peritaje.getNumeroExpediente()));
        peritaje.setMateriasTexto(materiaDao.nombres(peritaje.getMateriasIds()));
        Perito perito = peritoDao.getById(peritaje.getPeritoId());
        peritaje.setPeritoNombre(perito == null ? "Sin asignar" : perito.getNombreCompleto());
        peritaje.setEspecialidadesPerito(perito == null ? "" : String.join(", ", perito.getEspecialidades()));
    }
}
