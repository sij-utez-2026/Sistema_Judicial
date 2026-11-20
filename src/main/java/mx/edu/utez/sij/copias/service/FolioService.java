package mx.edu.utez.sij.copias.service;

import mx.edu.utez.sij.copias.dao.SolicitudCopiaDao;
import mx.edu.utez.sij.copias.model.Formatos;

import java.security.SecureRandom;

/**
 * Folio de la solicitud con una parte aleatoria para que no se pueda adivinar: CC-AAAA-XXXXXX
 * (copias-certificadas.md 6.1). Sin letras que se confunden (I, O, 0, 1).
 * El folio del comprobante de pago lo genera la parte de pagos.
 */
public class FolioService {

    private static final String CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LARGO = 6;

    private final SecureRandom aleatorio = new SecureRandom();
    private final SolicitudCopiaDao solicitudDao;

    public FolioService(SolicitudCopiaDao solicitudDao) {
        this.solicitudDao = solicitudDao;
    }

    public String folioSolicitud() {
        String folio;
        do {
            StringBuilder parte = new StringBuilder();
            for (int i = 0; i < LARGO; i++) {
                parte.append(CARACTERES.charAt(aleatorio.nextInt(CARACTERES.length())));
            }
            folio = "CC-" + Formatos.ahora().getYear() + "-" + parte;
        } while (solicitudDao.existsByFolio(folio));   // repite si por casualidad ya existe
        return folio;
    }
}
