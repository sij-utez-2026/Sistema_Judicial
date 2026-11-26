package mx.edu.utez.sij.nucleo.contratos.segunda;

/**
 * Datos que Oficialía envía para crear una toca (docs/CONTRATOS.md, 6.2).
 * Mismos campos que definía el DFR; sustituye a SolicitudTocaTemporalDto.
 */
public record SolicitudTocaDto(
        String numeroExpediente,
        String materiaId,
        String juzgadoOrigenId,
        String distritoId,
        Long salaId,
        Long documentoApelacionArchivoId) {
}
