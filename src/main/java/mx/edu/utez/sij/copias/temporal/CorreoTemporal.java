package mx.edu.utez.sij.copias.temporal;

import java.util.logging.Logger;

/**
 * TEMPORAL: en lugar de enviar el correo, lo imprime en la consola de Tomcat (como pide ARQUITECTURA 7.1 en desarrollo).
 * Cuando el núcleo tenga el envío de correos a destinatarios sin cuenta, se usa ese servicio y se borra esta clase.
 */
public class CorreoTemporal {

    private static final Logger LOG = Logger.getLogger(CorreoTemporal.class.getName());

    /** Devuelve true si se "envió". El servicio real podrá devolver false si falla el servidor de correo. */
    public boolean enviar(String destinatario, String asunto, String cuerpo) {
        LOG.info("""

                ===== CORREO (simulado) =====
                Para:   %s
                Asunto: %s
                %s
                =============================""".formatted(destinatario, asunto, cuerpo));
        return true;
    }
}
