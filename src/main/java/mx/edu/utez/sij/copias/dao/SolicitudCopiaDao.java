package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.SolicitudCopia;
import mx.edu.utez.sij.nucleo.web.Pagina;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a las solicitudes (tabla cop_solicitud, bd/50_copias.sql).
 *
 * Hoy la implementa SolicitudCopiaDaoMemoria. Para pasar a MySQL:
 * 1. Crear SolicitudCopiaDaoMySql implements SolicitudCopiaDao con JDBC y ConexionBD (ARQUITECTURA, sección 11).
 *    Cada método con comentario "SQL:" indica la consulta.
 * 2. En Copias.java, cambiar new SolicitudCopiaDaoMemoria() por new SolicitudCopiaDaoMySql().
 * 3. Borrar la clase en memoria. Los services y servlets no cambian.
 */
public interface SolicitudCopiaDao {

    /** SQL: {@code SELECT * FROM cop_solicitud WHERE id = ?} */
    Optional<SolicitudCopia> findById(Long id);

    /** SQL: INSERT (si no tiene id, con RETURN_GENERATED_KEYS) o UPDATE ... WHERE id = ? */
    SolicitudCopia save(SolicitudCopia solicitud);

    /** SQL: {@code SELECT COUNT(*) FROM cop_solicitud} */
    long count();

    /** SQL: {@code SELECT COUNT(*) > 0 FROM cop_solicitud WHERE folio = ?} */
    boolean existsByFolio(String folio);

    /** SQL: {@code SELECT COUNT(*) FROM cop_solicitud WHERE estado = ?} */
    long countByEstado(EstadoSolicitud estado);

    /**
     * Bandejas de validación y entregas: las más antiguas primero.
     * SQL: {@code SELECT * FROM cop_solicitud WHERE estado IN (?, ?) ORDER BY fecha_solicitud}
     */
    List<SolicitudCopia> findByEstadoInOrderByFechaSolicitudAsc(Collection<EstadoSolicitud> estados);

    /**
     * Registro de solicitudes. Cada filtro es opcional (null = no filtra). Las más recientes primero.
     * SQL:
     * {@code SELECT * FROM cop_solicitud WHERE
     *     (? IS NULL OR LOWER(folio) LIKE ? OR LOWER(numero_expediente) LIKE ? OR LOWER(solicitante_nombre) LIKE ?)
     *     AND (? IS NULL OR estado = ?)
     *     ORDER BY fecha_solicitud DESC LIMIT ? OFFSET ?}
     * y otro {@code SELECT COUNT(*)} con el mismo WHERE para el total.
     */
    Pagina<SolicitudCopia> buscar(String texto, EstadoSolicitud estado, int pagina, int tamano);
}
