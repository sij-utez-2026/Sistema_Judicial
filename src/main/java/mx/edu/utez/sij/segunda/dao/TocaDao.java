package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.EstadoToca;
import mx.edu.utez.sij.segunda.model.HistorialToca;
import mx.edu.utez.sij.segunda.model.Toca;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a los datos de las tocas (DAO).
 *
 * Mapeo SQL vigente: toca_Seg.id_toca, numero_toca, id_segunda_instancia,
 * id_sala, id_magistrado, estado y fecha_recepcion. El número de expediente
 * se obtiene enlazando toca_Seg.id_segunda_instancia con segunda_instancia_Ofc,
 * y de ahí con expediente_Ofc.num_expediente. Materia, juzgado y distrito se
 * derivan de las relaciones del expediente; no son columnas de toca_Seg.
 */
public interface TocaDao {

    Optional<Toca> findById(Long id);

    Optional<Toca> findByFolio(String folioToca);

    Toca save(Toca toca);

    /** Persists reception, PDF/state transition and history atomically when supported. */
    default boolean recibirAtomico(Toca toca, HistorialToca historial) {
        return false;
    }

    long count();

    long countByEstado(EstadoToca estado);

    boolean existsByFolio(String folioToca);

    boolean existsByNumeroExpediente(String numeroExpediente);

    boolean tieneTocasPendientesPorSala(Long salaId);

    boolean tieneTocasPendientesPorMagistrado(Long magistradoUsuarioId);

    /**
     * Listado con filtros opcionales.
     * - texto: folio o número de expediente
     * - estado: null = todos
     * - salaId: null = todas
     * - soloDeMagistradoUsuarioId: si no es null, filtra a las tocas de ese magistrado
     */
    Pagina<Toca> buscar(String texto, EstadoToca estado, Long salaId, Long soloDeMagistradoUsuarioId,
                        int pagina, int tamano);

    List<Toca> pendientesDeMagistrado(Long magistradoUsuarioId);
}
