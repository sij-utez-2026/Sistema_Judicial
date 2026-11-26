package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.EstadoResolucion;
import mx.edu.utez.sij.segunda.model.Resolucion;

import java.util.List;
import java.util.Optional;

public interface ResolucionDao {

    Optional<Resolucion> findById(Long id);

    Resolucion save(Resolucion resolucion);

    /**
     * Persist resolution, optional PDF, final toca state and history atomically when supported.
     * Returns false for simple/in-memory implementations so the service can use its fallback path.
     */
    default boolean registrarConCierreAtomico(Resolucion resolucion, String descripcionHistorial) {
        return false;
    }

    long count();

    /** Resoluciones de una toca. */
    List<Resolucion> findByTocaId(Long tocaId);

    /** Resolución vigente de una toca (solo puede haber una). */
    Optional<Resolucion> findVigenteByTocaId(Long tocaId);

    /**
     * Listado con filtros.
     * - texto: folio de la toca o número de expediente
     * - estado: null = todos
     * - salaId: null = todas
     * - soloDeMagistradoUsuarioId: si no es null, filtra a las resoluciones de las tocas del magistrado
     */
    Pagina<Resolucion> buscar(String texto, EstadoResolucion estado, Long salaId,
                              Long soloDeMagistradoUsuarioId, int pagina, int tamano);
}

