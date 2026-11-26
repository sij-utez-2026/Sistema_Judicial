package mx.edu.utez.sij.nucleo.contratos.segunda;

/**
 * Contrato de recepción de tocas (docs/CONTRATOS.md, 6.2).
 * Lo implementa Segunda Instancia; lo usa Oficialía (RF 4.1 y 5.2).
 */
public interface RecepcionTocaApi {

    /** Crea la toca y devuelve su folio. El magistrado es el de la sala elegida. */
    String recibir(SolicitudTocaDto solicitud);
}