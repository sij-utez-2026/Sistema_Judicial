package mx.edu.utez.sij.copias;

import mx.edu.utez.sij.copias.dao.ColorAreaDao;
import mx.edu.utez.sij.copias.dao.ColorAreaDaoMemoria;
import mx.edu.utez.sij.copias.dao.CorreoEnviadoDao;
import mx.edu.utez.sij.copias.dao.CorreoEnviadoDaoMemoria;
import mx.edu.utez.sij.copias.dao.EntregaDao;
import mx.edu.utez.sij.copias.dao.EntregaDaoMemoria;
import mx.edu.utez.sij.copias.dao.HistorialSolicitudDao;
import mx.edu.utez.sij.copias.dao.HistorialSolicitudDaoMemoria;
import mx.edu.utez.sij.copias.dao.SolicitudCopiaDao;
import mx.edu.utez.sij.copias.dao.SolicitudCopiaDaoMemoria;
import mx.edu.utez.sij.copias.dao.TarifaDao;
import mx.edu.utez.sij.copias.dao.TarifaDaoMemoria;
import mx.edu.utez.sij.copias.service.ColoresCopiasService;
import mx.edu.utez.sij.copias.service.CorreoCopiasService;
import mx.edu.utez.sij.copias.service.EntregaService;
import mx.edu.utez.sij.copias.service.FolioService;
import mx.edu.utez.sij.copias.service.HistorialService;
import mx.edu.utez.sij.copias.service.PersonalCopiasService;
import mx.edu.utez.sij.copias.service.SolicitudCopiaService;
import mx.edu.utez.sij.copias.service.TarifaService;
import mx.edu.utez.sij.copias.service.ValidacionService;
import mx.edu.utez.sij.copias.temporal.AccesoTemporal;
import mx.edu.utez.sij.copias.temporal.ArchivoServiceTemporal;
import mx.edu.utez.sij.copias.temporal.CorreoTemporal;
import mx.edu.utez.sij.copias.temporal.ExpedienteApi;
import mx.edu.utez.sij.copias.temporal.ExpedienteApiTemporal;
import mx.edu.utez.sij.copias.sesion.SesionCopias;
import mx.edu.utez.sij.copias.temporal.UsuarioServiceTemporal;

/**
 * Objetos del módulo de Copias Certificadas (EQUIPO 5). Hay UNA sola instancia de cada uno para toda la
 * aplicación, así todos los servlets comparten los mismos datos.
 *
 * Los servlets los piden así:  SolicitudCopiaService solicitudService = Copias.solicitudService();
 *
 * Aquí se decide qué implementación se usa:
 * - MySQL: cambiar cada new ...DaoMemoria() por new ...DaoMySql().
 * - Núcleo y Oficialía: cambiar las clases de temporal/ por las reales.
 * Se crean en orden: primero los que no dependen de nadie.
 */
public final class Copias {

    // DAO (hoy en memoria)
    private static final SolicitudCopiaDao SOLICITUD_DAO = new SolicitudCopiaDaoMemoria();
    private static final EntregaDao ENTREGA_DAO = new EntregaDaoMemoria();
    private static final CorreoEnviadoDao CORREO_DAO = new CorreoEnviadoDaoMemoria();
    private static final HistorialSolicitudDao HISTORIAL_DAO = new HistorialSolicitudDaoMemoria();
    private static final TarifaDao TARIFA_DAO = new TarifaDaoMemoria();
    private static final ColorAreaDao COLOR_DAO = new ColorAreaDaoMemoria();

    // TEMPORAL: lo que darán el núcleo y Oficialía
    private static final ExpedienteApi EXPEDIENTE_API = new ExpedienteApiTemporal();
    private static final ArchivoServiceTemporal ARCHIVOS = new ArchivoServiceTemporal();
    private static final CorreoTemporal CORREO = new CorreoTemporal();
    private static final UsuarioServiceTemporal USUARIOS = new UsuarioServiceTemporal();

    // Quién usa Copias y su permiso (ver SesionCopias)
    private static final SesionCopias SESION = new SesionCopias(USUARIOS);
    private static final AccesoTemporal ACCESO = new AccesoTemporal(SESION);

    // Services
    private static final TarifaService TARIFAS = new TarifaService(TARIFA_DAO);
    private static final FolioService FOLIOS = new FolioService(SOLICITUD_DAO);
    private static final HistorialService HISTORIAL = new HistorialService(SOLICITUD_DAO, HISTORIAL_DAO);
    private static final CorreoCopiasService CORREOS = new CorreoCopiasService(CORREO, CORREO_DAO);
    private static final SolicitudCopiaService SOLICITUDES = new SolicitudCopiaService(SOLICITUD_DAO, EXPEDIENTE_API,
            ARCHIVOS, TARIFAS, FOLIOS, HISTORIAL);
    private static final ValidacionService VALIDACION = new ValidacionService(SOLICITUD_DAO, SOLICITUDES, HISTORIAL,
            EXPEDIENTE_API, ARCHIVOS);
    private static final EntregaService ENTREGAS = new EntregaService(ENTREGA_DAO, SOLICITUD_DAO, SOLICITUDES,
            HISTORIAL, CORREOS);
    private static final PersonalCopiasService PERSONAL = new PersonalCopiasService(USUARIOS);
    private static final ColoresCopiasService COLORES = new ColoresCopiasService(COLOR_DAO);

    private Copias() {
    }

    /** TEMPORAL: cuentas de prueba (las crea DatosInicialesCopias). Con el núcleo, las cuentas son de core_usuario. */
    public static UsuarioServiceTemporal usuarios() {
        return USUARIOS;
    }

    public static SesionCopias sesion() {
        return SESION;
    }

    public static AccesoTemporal acceso() {
        return ACCESO;
    }

    public static TarifaService tarifaService() {
        return TARIFAS;
    }

    public static HistorialService historialService() {
        return HISTORIAL;
    }

    public static CorreoCopiasService correoService() {
        return CORREOS;
    }

    public static SolicitudCopiaService solicitudService() {
        return SOLICITUDES;
    }

    public static ValidacionService validacionService() {
        return VALIDACION;
    }

    public static EntregaService entregaService() {
        return ENTREGAS;
    }

    public static PersonalCopiasService personalService() {
        return PERSONAL;
    }

    public static ColoresCopiasService coloresService() {
        return COLORES;
    }
}
