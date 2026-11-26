package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.dao.MagistradoDao;
import mx.edu.utez.sij.segunda.dao.SalaDao;
import mx.edu.utez.sij.segunda.dao.TocaDao;
import mx.edu.utez.sij.segunda.dto.MagistradoForm;
import mx.edu.utez.sij.segunda.model.Magistrado;
import mx.edu.utez.sij.segunda.model.Sala;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Magistrados (RF 4.1 – 4.5). Reglas en docs/modulos/segunda-instancia.md, sección 6.2.
 *
 * Regla de negocio adoptada: UN magistrado tiene UNA sola sala a la vez.
 * Fuente de verdad: magistrado_Seg.id_sala. Magistrado.salaId se deriva de esa relación.
 */
public class MagistradoService {

    public static final int TAMANO_PAGINA = 25;
    public static final List<Integer> TAMANOS_PAGINA = List.of(10, 25, 50, 100);

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    private final MagistradoDao magistradoDao;
    private final SalaDao salaDao;
    private final TocaDao tocaDao;
    private final UsuarioServiceTemporalSegunda usuarios;

    public MagistradoService(MagistradoDao magistradoDao,
                             SalaDao salaDao,
                             TocaDao tocaDao,
                             UsuarioServiceTemporalSegunda usuarios) {
        this.magistradoDao = magistradoDao;
        this.salaDao = salaDao;
        this.tocaDao = tocaDao;
        this.usuarios = usuarios;
    }

    public Pagina<Magistrado> listar(String texto, Boolean activo, int pagina, int tamano) {
        int tamanoValido = TAMANOS_PAGINA.contains(tamano) ? tamano : TAMANO_PAGINA;
        Pagina<Magistrado> resultado = magistradoDao.buscar(vacioANull(texto), activo,
                Math.max(pagina, 0), tamanoValido);
        for (Magistrado m : resultado.getContent()) {
            enriquecerConSala(m);
        }
        return resultado;
    }

    public Optional<Magistrado> buscar(Long id) {
        Optional<Magistrado> m = magistradoDao.findById(id);
        m.ifPresent(this::enriquecerConSala);
        return m;
    }

    /** RF 4.1 Registro + RF 4.3 asignación de sala (uno a uno). */
    public Magistrado registrar(MagistradoForm form) {
        String correo = form.getCorreo().trim().toLowerCase();
        if (usuarios.existeCorreo(correo)) {
            throw new ReglaNegocioException("Ya existe una cuenta con el correo " + correo);
        }

        Sala sala = null;
        if (form.getSalaId() != null) {
            sala = salaDao.findById(form.getSalaId())
                    .orElseThrow(() -> new ReglaNegocioException("La sala seleccionada no existe"));
            if (!sala.isActiva()) {
                throw new ReglaNegocioException("La sala seleccionada está inactiva");
            }
            if (sala.getMagistradoUsuarioId() != null) {
                throw new ReglaNegocioException("La sala seleccionada ya tiene un magistrado asignado");
            }
        }

        // 1. Crear la cuenta central; el trigger de la base replica las credenciales a Login.
        UsuarioServiceTemporalSegunda.UsuarioTemporal usuario = usuarios.crearCuenta(
                form.getNombre(), form.getApellidos(), correo);

        // 2. Crear el magistrado
        Magistrado m = new Magistrado();
        m.setUsuarioId(usuario.getId());
        m.setNombre(usuario.getNombre());
        m.setApellidos(usuario.getApellidos());
        m.setCorreo(usuario.getCorreo());
        m.setSalaId(form.getSalaId());
        m.setActivo(true);
        m.setCreadoEn(LocalDateTime.now(ZONA));
        magistradoDao.save(m);

        // La asignación ya quedó guardada en magistrado_Seg.id_sala.
        // sala_Seg no almacena el id del magistrado; su ocupante se deriva
        // mediante la relación inversa consultada por SalaDaoMySql.

        enriquecerConSala(m);
        m.setContrasenaTemporal(usuario.getContrasenaTemporal());
        return m;
    }

    /** RF 4.4 Edición. El correo es la identidad central y no se cambia desde aquí. */
    public Magistrado actualizar(Long id, MagistradoForm form) {
        Magistrado m = obtener(id);
        usuarios.actualizarDatos(m.getUsuarioId(), form.getNombre(), form.getApellidos());
        m.setNombre(form.getNombre().trim());
        m.setApellidos(form.getApellidos().trim());
        m.setActualizadoEn(LocalDateTime.now(ZONA));
        return magistradoDao.findById(id).orElse(m);
    }

    /** RF 4.3 Asignar (o cambiar) la sala del magistrado. Delega la fuente de verdad a SalaDao. */
    public Magistrado asignarSala(Long magistradoId, Long salaId) {
        Magistrado m = obtener(magistradoId);
        if (!m.isActivo()) {
            throw new ReglaNegocioException("El magistrado está inactivo");
        }
        if (salaId == null) {
            throw new ReglaNegocioException("Seleccione una sala");
        }
        Sala nueva = salaDao.findById(salaId)
                .orElseThrow(() -> new ReglaNegocioException("La sala no existe"));
        if (!nueva.isActiva()) {
            throw new ReglaNegocioException("La sala está inactiva");
        }
        if (nueva.getMagistradoUsuarioId() != null
                && !Objects.equals(nueva.getMagistradoUsuarioId(), m.getUsuarioId())) {
            throw new ReglaNegocioException("La sala seleccionada ya tiene un magistrado asignado");
        }

        // Liberar la sala anterior y ocupar la nueva consiste únicamente en
        // reemplazar magistrado_Seg.id_sala.
        m.setSalaId(nueva.getId());
        m.setActualizadoEn(LocalDateTime.now(ZONA));
        magistradoDao.save(m);

        enriquecerConSala(m);
        return m;
    }

    /** RF 4.5 Desactivar (bloquea si tiene tocas pendientes). */
    public Magistrado cambiarEstado(Long id, boolean activo) {
        Magistrado m = obtener(id);

        if (!activo && m.isActivo()
                && tocaDao.tieneTocasPendientesPorMagistrado(m.getUsuarioId())) {
            throw new ReglaNegocioException(
                    "No se puede dar de baja: el magistrado tiene tocas pendientes de resolución");
        }

        m.setActivo(activo);
        m.setActualizadoEn(LocalDateTime.now(ZONA));
        magistradoDao.save(m);

        if (activo) {
            usuarios.reactivar(m.getUsuarioId());
        } else {
            usuarios.desactivar(m.getUsuarioId());
            // Al desactivar, se libera la sala poniendo id_sala en NULL.
            if (m.getSalaId() != null) {
                m.setSalaId(null);
                magistradoDao.save(m);
            }
        }

        enriquecerConSala(m);
        return m;
    }

    public long totalRegistrados() { return magistradoDao.count(); }
    public long totalActivos()     { return magistradoDao.countByActivo(true); }
    public long totalInactivos()   { return magistradoDao.countByActivo(false); }

    public List<Magistrado> activos() {
        List<Magistrado> lista = magistradoDao.activos();
        for (Magistrado m : lista) enriquecerConSala(m);
        return lista;
    }

    public MagistradoForm formularioDe(Magistrado m) {
        MagistradoForm form = new MagistradoForm();
        form.setNombre(m.getNombre());
        form.setApellidos(m.getApellidos());
        form.setCorreo(m.getCorreo());
        form.setSalaId(m.getSalaId());
        return form;
    }

    private Magistrado obtener(Long id) {
        return buscar(id).orElseThrow(() -> new ReglaNegocioException("El magistrado no existe"));
    }

    private void enriquecerConSala(Magistrado m) {
        // La fuente de verdad es la sala; el campo m.salaId se recalcula siempre.
        Long salaDelMagistrado = null;
        String nombreSala = null;
        for (Sala s : salaDao.activas()) {
            if (m.getUsuarioId() != null && m.getUsuarioId().equals(s.getMagistradoUsuarioId())) {
                salaDelMagistrado = s.getId();
                nombreSala = s.getNombre();
                break;
            }
        }
        m.setSalaId(salaDelMagistrado);
        m.setSalaNombre(nombreSala);
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
