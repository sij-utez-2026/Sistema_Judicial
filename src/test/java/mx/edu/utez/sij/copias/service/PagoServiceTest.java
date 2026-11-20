package mx.edu.utez.sij.copias.service;

import mx.edu.utez.sij.copias.model.CorreoEnviado;
import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.Pago;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.model.TipoCorreo;
import mx.edu.utez.sij.copias.temporal.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias del pago simulado y comprobante (RF 4.1). */
class PagoServiceTest {

    private final CopiasPrueba p = new CopiasPrueba();

    private SolicitudCopia publica() {
        return p.solicitudService.registrar(CopiasPrueba.form("EXP-2026-001"), null, CopiasPrueba.USUARIO);
    }

    private SolicitudCopia privada() {
        return p.solicitudService.registrar(CopiasPrueba.form("EXP-2026-002"), CopiasPrueba.ine(), CopiasPrueba.USUARIO);
    }

    @Test
    void pagaSolicitudPublicaCambiaEstadoYEnviaComprobante() {
        SolicitudCopia s = publica();

        Pago pago = p.pagoService.registrarPago(s.getId(), CopiasPrueba.USUARIO);

        assertTrue(pago.getFolioPago().matches("^CP-\\d{4}-[A-Z2-9]{6}$"));
        assertTrue(pago.isSimulado());
        assertEquals(new BigDecimal("320.00"), pago.getMonto());
        assertEquals(CopiasPrueba.USUARIO, pago.getRegistradoPor());

        SolicitudCopia actualizada = p.solicitudService.obtener(s.getId());
        assertEquals(EstadoSolicitud.PAGADO_EN_PREPARACION, actualizada.getEstado());
        assertEquals(1, p.correo.enviados);
        assertEquals(TipoCorreo.COMPROBANTE, p.correoService.enviados(s.getId()).get(0).getTipo());
    }

    @Test
    void pagaSolicitudPrivadaDespuesDeAprobar() {
        SolicitudCopia s = privada();
        p.validacionService.aprobar(s.getId(), CopiasPrueba.USUARIO);

        Pago pago = p.pagoService.registrarPago(s.getId(), CopiasPrueba.USUARIO);

        assertEquals(EstadoSolicitud.PAGADO_EN_PREPARACION, p.solicitudService.obtener(s.getId()).getEstado());
        assertEquals(pago.getFolioPago(), p.pagoService.obtenerComprobante(s.getId()).getFolioPago());
    }

    @Test
    void noPermitePagarSiEstaEnEsperaOCancelada() {
        SolicitudCopia enEspera = privada();
        ReglaNegocioException exEspera = assertThrows(ReglaNegocioException.class,
                () -> p.pagoService.registrarPago(enEspera.getId(), CopiasPrueba.USUARIO));
        assertEquals("Debe esperar la aprobación antes de pagar", exEspera.getMessage());

        p.validacionService.rechazar(enEspera.getId(), "No coincide con las partes", CopiasPrueba.USUARIO);
        ReglaNegocioException exCancelada = assertThrows(ReglaNegocioException.class,
                () -> p.pagoService.registrarPago(enEspera.getId(), CopiasPrueba.USUARIO));
        assertEquals("La solicitud fue cancelada y no se puede pagar", exCancelada.getMessage());
    }

    @Test
    void impidePagosDobles() {
        SolicitudCopia s = publica();
        p.pagoService.registrarPago(s.getId(), CopiasPrueba.USUARIO);

        assertThrows(ReglaNegocioException.class,
                () -> p.pagoService.registrarPago(s.getId(), CopiasPrueba.USUARIO));
    }

    @Test
    void folioEntregadoInhabilitaComprobanteYReenvio() {
        SolicitudCopia s = publica();
        p.pagoService.registrarPago(s.getId(), CopiasPrueba.USUARIO);
        p.entregaService.entregar(s.getId(), CopiasPrueba.USUARIO);

        assertThrows(ReglaNegocioException.class, () -> p.pagoService.obtenerComprobante(s.getId()));
        assertThrows(ReglaNegocioException.class,
                () -> p.pagoService.reenviarComprobante(s.getId(), CopiasPrueba.USUARIO));
    }

    @Test
    void reenviaComprobantePorCorreo() {
        SolicitudCopia s = publica();
        p.pagoService.registrarPago(s.getId(), CopiasPrueba.USUARIO);

        CorreoEnviado reenvio = p.pagoService.reenviarComprobante(s.getId(), CopiasPrueba.USUARIO);

        assertTrue(reenvio.isExito());
        assertEquals(2, p.correo.enviados);
        assertEquals(2, p.correoService.enviados(s.getId()).size());
    }
}
