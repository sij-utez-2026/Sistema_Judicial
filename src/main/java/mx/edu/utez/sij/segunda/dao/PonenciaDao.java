package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.segunda.model.Ponencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a los datos de las ponencias (DAO).
 *
 * SQL vigente: ponencia_Seg (id_toca, id_magistrado, fecha_sesion, hora_inicio,
 * hora_fin, tipo_sesion, fecha_creacion). id_magistrado es magistrado_Seg.id_magistrado.
 */
public interface PonenciaDao {

    Optional<Ponencia> findById(Long id);

    Ponencia save(Ponencia ponencia);

    /** Persists a ponencia and its toca transition/history in one transaction when supported. */
    default boolean registrarAtomico(Ponencia ponencia, boolean iniciarPonencia, String descripcionHistorial) {
        return false;
    }

    /** Todas las ponencias de una toca, ordenadas por fecha. */
    List<Ponencia> findByTocaId(Long tocaId);

    /**
     * Ponencias del magistrado (a través de sus tocas) cuya fecha+hora cae dentro del rango.
     * Sirve para detectar choques de horario.
     */
    List<Ponencia> findByMagistradoEntre(Long magistradoId,
                                         LocalDateTime desde, LocalDateTime hasta);

    /** ¿La toca tiene al menos una ponencia programada para HOY? */
    boolean tienePonenciaHoy(Long tocaId);

    List<Ponencia> entre(LocalDateTime desde, LocalDateTime hasta);
}
