package mx.edu.utez.sij.nucleo.contratos.segunda;

import java.util.List;

/**
 * Contrato de consulta de salas (docs/CONTRATOS.md, 6.1).
 * Lo implementa Segunda Instancia; lo usa Oficialía (RF 5.2).
 */
public interface SalaConsultaApi {

    /** Salas activas del distrito con magistrado activo asignado. Lista vacía si no hay. */
    List<SalaDisponibleDto> salasDisponibles(String distritoId);
}