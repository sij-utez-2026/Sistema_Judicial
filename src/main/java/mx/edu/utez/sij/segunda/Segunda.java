package mx.edu.utez.sij.segunda;

import jakarta.servlet.http.HttpServletRequest;
import mx.edu.utez.sij.nucleo.acceso.Acceso;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;
import mx.edu.utez.sij.segunda.controller.SegundaPermisos;
import mx.edu.utez.sij.segunda.dao.*;
import mx.edu.utez.sij.segunda.service.*;

/** Ensamblador de servicios y punto de acceso a la sesión común del sistema. */
public final class Segunda {
    private static final CatalogoTemporalSegunda CATALOGO = new CatalogoTemporalSegunda();
    private static final ExpedienteTemporalSegunda EXPEDIENTE = new ExpedienteTemporalSegunda();

    private static final SalaDao SALA_DAO = new SalaDaoMySql();
    private static final MagistradoDao MAGISTRADO_DAO = new MagistradoDaoMySql();
    private static final TocaDao TOCA_DAO = new TocaDaoMySql();
    private static final HistorialTocaDao HISTORIAL_DAO = new HistorialTocaDaoMySql();
    private static final PonenciaDao PONENCIA_DAO = new PonenciaDaoMySql();
    private static final ResolucionDao RESOLUCION_DAO = new ResolucionDaoMySql();
    private static final VersionResolucionDao VERSION_DAO = new VersionResolucionDaoMySql();
    private static final RecepcionPendienteDaoMySql RECEPCION_PENDIENTE_DAO = new RecepcionPendienteDaoMySql();
    private static final DocumentoSegDaoMySql DOCUMENTO_DAO = new DocumentoSegDaoMySql();
    private static final ReasignacionDaoMySql REASIGNACION_DAO = new ReasignacionDaoMySql();
    private static final BitacoraDaoMySql BITACORA_DAO = new BitacoraDaoMySql();

    private static final UsuarioServiceTemporalSegunda USUARIOS = new UsuarioServiceTemporalSegunda();
    private static final SalaService SALA_SERVICE = new SalaService(SALA_DAO, TOCA_DAO, MAGISTRADO_DAO, CATALOGO);
    private static final MagistradoService MAGISTRADO_SERVICE = new MagistradoService(MAGISTRADO_DAO, SALA_DAO, TOCA_DAO, USUARIOS);
    private static final TocaService TOCA_SERVICE = new TocaService(TOCA_DAO, HISTORIAL_DAO, SALA_DAO, MAGISTRADO_DAO, CATALOGO, EXPEDIENTE);
    private static final PonenciaService PONENCIA_SERVICE = new PonenciaService(PONENCIA_DAO, TOCA_SERVICE);
    private static final ResolucionService RESOLUCION_SERVICE = new ResolucionService(RESOLUCION_DAO, VERSION_DAO, TOCA_DAO, TOCA_SERVICE, PONENCIA_SERVICE);
    private static final RecepcionTocaService RECEPCION_TOCA_SERVICE = new RecepcionTocaService(TOCA_SERVICE);
    private static final AgendaService AGENDA_SERVICE = new AgendaService(PONENCIA_SERVICE, TOCA_SERVICE);

    private Segunda() { }

    public static SalaService salaService() { return SALA_SERVICE; }
    public static MagistradoService magistradoService() { return MAGISTRADO_SERVICE; }
    public static TocaService tocaService() { return TOCA_SERVICE; }
    public static PonenciaService ponenciaService() { return PONENCIA_SERVICE; }
    public static ResolucionService resolucionService() { return RESOLUCION_SERVICE; }
    public static RecepcionTocaService recepcionTocaService() { return RECEPCION_TOCA_SERVICE; }
    public static CatalogoTemporalSegunda catalogo() { return CATALOGO; }
    public static AgendaService agendaService() { return AGENDA_SERVICE; }
    public static RecepcionPendienteDaoMySql recepcionPendienteDao() { return RECEPCION_PENDIENTE_DAO; }
    public static DocumentoSegDaoMySql documentoDao() { return DOCUMENTO_DAO; }
    public static ReasignacionDaoMySql reasignacionDao() { return REASIGNACION_DAO; }
    public static BitacoraDaoMySql bitacoraDao() { return BITACORA_DAO; }

    /** Devuelve el usuario autenticado por el login común del sistema. */
    public static UsuarioSesion usuarioActual(HttpServletRequest request) {
        UsuarioSesion usuario = Acceso.usuario(request);
        if (usuario != null) {
            request.setAttribute("segundaUsuario", usuario);
            request.setAttribute("segundaEsAdmin", SegundaPermisos.esAdmin(usuario));
            request.setAttribute("segundaEsMagistrado", SegundaPermisos.esMagistrado(usuario));
        }
        return usuario;
    }
}
