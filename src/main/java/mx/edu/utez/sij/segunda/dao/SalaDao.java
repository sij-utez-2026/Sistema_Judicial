package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.Sala;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a los datos de las salas (DAO).
 *
 * Hoy la implementa SalaDaoMemoria. Para pasar a MySQL:
 * 1. Crear SalaDaoMySql implements SalaDao con JDBC y ConexionBD (docs/ARQUITECTURA.md, sección 11).
 * 2. En Segunda.java, cambiar new SalaDaoMemoria() por new SalaDaoMySql().
 * 3. Borrar SalaDaoMemoria.
 * El service y los servlets no cambian.
 */
public interface SalaDao {

    Optional<Sala> findById(Long id);

    Sala save(Sala sala);

    long count();

    long countByActiva(boolean activa);

    /** ¿Ya existe una sala con ese nombre en ese distrito? (RF 3.1: nombre único por distrito). */
    boolean existsByNombreAndDistrito(String nombre, String distritoId, Long excluirId);

    /** ¿El magistrado ya tiene una sala asignada? (regla: un magistrado solo puede tener una sala). */
    boolean existsByMagistradoUsuarioId(Long magistradoUsuarioId, Long excluirSalaId);

    /**
     * Listado con filtros opcionales.
     * SQL: SELECT ... FROM sala_Seg s WHERE
     *        (? IS NULL OR LOWER(s.nombre) LIKE ?)
     *        AND (? IS NULL OR s.clave_distrito = ?)
     *        AND (? IS NULL OR s.estatus = ?)
     *      ORDER BY s.nombre LIMIT ? OFFSET ?
     *      y otro SELECT COUNT(*) con los mismos filtros.
     */
    Pagina<Sala> buscar(String texto, String distritoId, Boolean activa, int pagina, int tamano);

    /** Todas las salas activas, para selectores (por ejemplo, al asignar un magistrado). */
    List<Sala> activas();

    /** ¿La sala tiene tocas pendientes de resolución? (RF 3.4). */
    boolean tieneTocasPendientes(Long salaId);
}


