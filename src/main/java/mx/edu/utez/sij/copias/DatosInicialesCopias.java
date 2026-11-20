package mx.edu.utez.sij.copias;

import mx.edu.utez.sij.copias.dto.PersonalForm;
import mx.edu.utez.sij.copias.dto.SolicitudForm;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.service.EntregaService;
import mx.edu.utez.sij.copias.service.PagoService;
import mx.edu.utez.sij.copias.service.PersonalCopiasService;
import mx.edu.utez.sij.copias.service.SolicitudCopiaService;
import mx.edu.utez.sij.copias.service.TarifaService;
import mx.edu.utez.sij.copias.temporal.UsuarioServiceTemporal;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos de prueba del módulo (docs/ARQUITECTURA.md, sección 11). Solo se cargan si no hay tarifas.
 * Nombres ficticios y correos de example.com (dominio reservado para pruebas).
 * Se usan los services para que pasen por las mismas reglas que una captura normal.
 *
 * Usuarios (TEMPORAL, mientras no hay login; se eligen en el "Modo de prueba" del menú):
 * - Equipo 5: un administrador de Copias y dos trabajadores.
 * - Oficialía (simulados): admin_op → entra como administrador; usuario_op → entra como trabajador.
 *   Sirven para probar que Copias funciona como sección de Oficialía. Con el login del núcleo,
 *   estos usuarios serán las cuentas reales de Oficialía y se borran de aquí.
 *
 * Mientras no está la parte de pagos, dos solicitudes se marcan como pagadas directamente
 * para que Entregas tenga datos (TEMPORAL).
 */
public class DatosInicialesCopias {

    private final TarifaService tarifaService;
    private final UsuarioServiceTemporal usuarios;
    private final PersonalCopiasService personalService;
    private final SolicitudCopiaService solicitudService;
    private final PagoService pagoService;
    private final EntregaService entregaService;

    public DatosInicialesCopias(TarifaService tarifaService, UsuarioServiceTemporal usuarios,
                                PersonalCopiasService personalService, SolicitudCopiaService solicitudService,
                                PagoService pagoService, EntregaService entregaService) {
        this.tarifaService = tarifaService;
        this.usuarios = usuarios;
        this.personalService = personalService;
        this.solicitudService = solicitudService;
        this.pagoService = pagoService;
        this.entregaService = entregaService;
    }

    public void cargar() {
        if (tarifaService.total() > 0) {
            return;
        }

        // Usuarios del Equipo 5
        Long admin = usuarios.crear("Ana Sofía Paredes Gil", "admin.copias@example.com", RolesCopias.ADMIN).getId();
        Long trabajador = trabajador("María Fernanda López Díaz", "personal1@example.com");
        trabajador("Carlos Alberto Núñez Vela", "personal2@example.com");

        // Usuarios de Oficialía de Partes (simulados)
        usuarios.crear("Admin de usuarios de OP (prueba)", "admin.op@example.com", RolesCopias.OFICIALIA_ADMIN);
        usuarios.crear("Usuario de OP (prueba)", "usuario.op@example.com", RolesCopias.OFICIALIA_USUARIO);

        // Pendiente 17: tarifa fija de prueba
        tarifaService.registrar(new BigDecimal("320.00"), LocalDate.of(2026, 1, 1), admin);

        // Pública recién capturada: lista para pago
        solicitud("EXP-2026-001", "Laura Méndez Ortiz", "MEOL850312AB1", "laura.mendez@example.com", trabajador);

        // Pública pagada, la copia aún no está: queda en preparación
        SolicitudCopia enPreparacion = solicitud("EXP-2026-003", "Jorge Luis Vega Ramos", "VERJ791110TT6",
                "jorge.vega@example.com", trabajador);
        pagadaDePrueba(enPreparacion, trabajador);

        // Pública pagada y entregada al momento
        SolicitudCopia entregada = solicitud("EXP-2026-001", "Roberto Salas Méndez", "SAMR800101AB2",
                "roberto.salas@example.com", trabajador);
        pagadaDePrueba(entregada, trabajador);
        entregaService.entregar(entregada.getId(), trabajador);
    }

    private Long trabajador(String nombre, String correo) {
        PersonalForm form = new PersonalForm();
        form.setNombre(nombre);
        form.setCorreo(correo);
        return personalService.registrar(form).getId();
    }

    private SolicitudCopia solicitud(String expediente, String nombre, String rfc, String correo, Long usuarioId) {
        SolicitudForm form = new SolicitudForm();
        form.setNumeroExpediente(expediente);
        form.setSolicitanteNombre(nombre);
        form.setSolicitanteRfc(rfc);
        form.setSolicitanteCorreo(correo);
        return solicitudService.registrar(form, null, usuarioId);
    }

    private void pagadaDePrueba(SolicitudCopia solicitud, Long usuarioId) {
        pagoService.registrarPago(solicitud.getId(), usuarioId);
    }
}
