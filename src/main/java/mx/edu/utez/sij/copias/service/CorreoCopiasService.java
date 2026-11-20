package mx.edu.utez.sij.copias.service;

import mx.edu.utez.sij.copias.dao.CorreoEnviadoDao;
import mx.edu.utez.sij.copias.model.CorreoEnviado;
import mx.edu.utez.sij.copias.model.Formatos;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.copias.model.TipoCorreo;
import mx.edu.utez.sij.copias.temporal.CorreoTemporal;
import mx.edu.utez.sij.copias.temporal.SesionTemporal;

import java.util.List;

/**
 * Correos al solicitante. Cada envío queda en cop_correo.
 * - Aviso de que su copia está lista para recoger (Entregas).
 * - El comprobante de pago lo envía la parte de pagos con enviar(...) y TipoCorreo.COMPROBANTE.
 *
 * TODO núcleo: enviar con el servicio de correo del núcleo y plantillas en templates/correo/copias/.
 * Hoy el correo se imprime en la consola de Tomcat.
 */
public class CorreoCopiasService {

    private final CorreoTemporal correo;
    private final CorreoEnviadoDao correoDao;
    private final SesionTemporal sesion;

    public CorreoCopiasService(CorreoTemporal correo, CorreoEnviadoDao correoDao, SesionTemporal sesion) {
        this.correo = correo;
        this.correoDao = correoDao;
        this.sesion = sesion;
    }

    public CorreoEnviado enviarListaParaRecoger(SolicitudCopia solicitud) {
        String cuerpo = """
                Estimado(a) %s:

                Su copia certificada del expediente %s ya está lista.

                Folio de la solicitud: %s
                Lugar de entrega:      %s

                Acuda con su comprobante de pago y una identificación oficial.
                """.formatted(solicitud.getSolicitanteNombre(), solicitud.getNumeroExpediente(),
                solicitud.getFolio(), solicitud.getJuzgado());
        return enviar(solicitud, TipoCorreo.LISTA_PARA_RECOGER, "Su copia certificada está lista para recoger", cuerpo);
    }

    public List<CorreoEnviado> enviados(Long solicitudId) {
        return correoDao.findBySolicitudIdOrderByEnviadoEnDesc(solicitudId);
    }

    /** Envía el correo al solicitante y lo anota en cop_correo (también para el comprobante de pago). */
    public CorreoEnviado enviar(SolicitudCopia solicitud, TipoCorreo tipo, String asunto, String cuerpo) {
        boolean exito = correo.enviar(solicitud.getSolicitanteCorreo(), asunto, cuerpo);
        CorreoEnviado registro = new CorreoEnviado();
        registro.setSolicitudId(solicitud.getId());
        registro.setTipo(tipo);
        registro.setDestinatario(solicitud.getSolicitanteCorreo());
        registro.setEnviadoPor(sesion.usuarioId());
        registro.setEnviadoEn(Formatos.ahora());
        registro.setExito(exito);
        return correoDao.save(registro);
    }
}
