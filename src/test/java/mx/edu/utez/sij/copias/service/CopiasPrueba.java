package mx.edu.utez.sij.copias.service;

import mx.edu.utez.sij.copias.dao.ColorAreaDaoMemoria;
import mx.edu.utez.sij.copias.dao.CorreoEnviadoDaoMemoria;
import mx.edu.utez.sij.copias.dao.EntregaDaoMemoria;
import mx.edu.utez.sij.copias.dao.HistorialSolicitudDaoMemoria;
import mx.edu.utez.sij.copias.dao.SolicitudCopiaDaoMemoria;
import mx.edu.utez.sij.copias.dao.TarifaDaoMemoria;
import mx.edu.utez.sij.copias.dto.ArchivoSubido;
import mx.edu.utez.sij.copias.dto.SolicitudForm;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.temporal.ArchivoServiceTemporal;
import mx.edu.utez.sij.copias.temporal.CorreoTemporal;
import mx.edu.utez.sij.copias.temporal.ExpedienteApiTemporal;
import mx.edu.utez.sij.copias.temporal.UsuarioServiceTemporal;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Arma los services de Copias con los DAO en memoria, igual que Copias.java, pero nuevos para cada prueba.
 * correo.falla = true simula que el servidor de correo no responde.
 */
class CopiasPrueba {

    static class CorreoDePrueba extends CorreoTemporal {
        boolean falla;
        int enviados;

        @Override
        public boolean enviar(String destinatario, String asunto, String cuerpo) {
            if (falla) {
                return false;
            }
            enviados++;
            return true;
        }
    }

    // Id de quien hace las acciones en las pruebas (en la app lo pone el servlet con SesionCopias)
    static final Long USUARIO = 7L;

    final SolicitudCopiaDaoMemoria solicitudes = new SolicitudCopiaDaoMemoria();
    final CorreoEnviadoDaoMemoria correos = new CorreoEnviadoDaoMemoria();
    final CorreoDePrueba correo = new CorreoDePrueba();

    final TarifaService tarifaService = new TarifaService(new TarifaDaoMemoria());
    final FolioService folioService = new FolioService(solicitudes);
    final HistorialService historialService =
            new HistorialService(solicitudes, new HistorialSolicitudDaoMemoria());
    final ExpedienteApiTemporal expedienteApi = new ExpedienteApiTemporal();
    final ArchivoServiceTemporal archivoService = new ArchivoServiceTemporal();
    final SolicitudCopiaService solicitudService = new SolicitudCopiaService(solicitudes, expedienteApi,
            archivoService, tarifaService, folioService, historialService);
    final ValidacionService validacionService = new ValidacionService(solicitudes, solicitudService,
            historialService, expedienteApi, archivoService);
    final CorreoCopiasService correoService = new CorreoCopiasService(correo, correos);
    final EntregaService entregaService = new EntregaService(new EntregaDaoMemoria(), solicitudes,
            solicitudService, historialService, correoService);
    final PersonalCopiasService personalService = new PersonalCopiasService(new UsuarioServiceTemporal());
    final ColoresCopiasService coloresService = new ColoresCopiasService(new ColorAreaDaoMemoria());

    CopiasPrueba() {
        tarifaService.registrar(new BigDecimal("320.00"), LocalDate.of(2026, 1, 1), 1L);
    }

    // EXP-2026-001 y 003 son públicos; EXP-2026-002 y 004 privados (ExpedienteApiTemporal)
    static SolicitudForm form(String expediente) {
        SolicitudForm form = new SolicitudForm();
        form.setNumeroExpediente(expediente);
        form.setSolicitanteNombre("  Andrea   Castro Ruiz ");
        form.setSolicitanteRfc(" cara900721mz4 ");
        form.setSolicitanteCorreo(" Andrea@Example.com ");
        return form;
    }

    static ArchivoSubido archivo(String nombre, int bytes) {
        return new ArchivoSubido(nombre, bytes, new ByteArrayInputStream(new byte[bytes]));
    }

    static ArchivoSubido ine() {
        return archivo("ine.png", 3);
    }

    /** La parte de pagos no está en este módulo todavía: solo se pone el estado que deja el pago. */
    void marcarPagada(SolicitudCopia solicitud) {
        historialService.cambiarEstado(solicitud, EstadoSolicitud.PAGADO_EN_PREPARACION, "Pago de prueba", USUARIO);
    }
}
