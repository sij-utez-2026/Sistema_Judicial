package mx.edu.utez.sij.peritajes.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.peritajes.model.Perito;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a los datos de los peritos (DAO).
 *
 * Hoy la implementa PeritoDaoMemoria. Para pasar a MySQL:
 * 1. Crear PeritoDaoMySql implements PeritoDao con JDBC y ConexionBD (docs/ARQUITECTURA.md, sección 11).
 *    Cada método con comentario "SQL:" indica la consulta.
 * 2. En Peritajes.java, cambiar new PeritoDaoMemoria() por new PeritoDaoMySql().
 * 3. Borrar PeritoDaoMemoria.
 * El service y los servlets no cambian.
 */
public interface PeritoDao {

    Optional<Perito> findById(Long id);

    Perito save(Perito perito);

    long count();

    long countByActivo(boolean activo);

    /**
     * Peritos que tienen la materia (RF 3.2: total por materia).
     * SQL: {@code SELECT COUNT(*) FROM per_perito_materia WHERE materia_id = ?}
     */
    long contarPorMateria(Long materiaId);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByNumeroRegistroIgnoreCase(String numeroRegistro);

    /**
     * Búsqueda del catálogo. Cada filtro es opcional (null = no filtra). La página empieza en 0.
     * SQL: SELECT ... FROM per_perito p WHERE
     *        (? IS NULL OR LOWER(CONCAT_WS(' ', p.nombre, p.apellido_paterno, p.apellido_materno)) LIKE ?
     *                   OR LOWER(p.numero_registro) LIKE ?)
     *        AND (? IS NULL OR EXISTS (SELECT 1 FROM per_perito_especialidad e WHERE e.perito_id = p.id AND e.especialidad = ?))
     *        AND (? IS NULL OR EXISTS (SELECT 1 FROM per_perito_materia m WHERE m.perito_id = p.id AND m.materia_id = ?))
     *        AND (? IS NULL OR p.activo = ?)
     *      ORDER BY p.nombre, p.apellido_paterno LIMIT ? OFFSET ?
     *      y otro SELECT COUNT(*) con los mismos filtros para el total (Pagina.offset ayuda con el OFFSET).
     */
    Pagina<Perito> buscar(String texto, String especialidad, Long materiaId, Boolean activo, int pagina, int tamano);

    /**
     * Especialidades distintas ya registradas, para el filtro del catálogo.
     * SQL: {@code SELECT DISTINCT especialidad FROM per_perito_especialidad ORDER BY especialidad}
     */
    List<String> especialidadesRegistradas();
}
