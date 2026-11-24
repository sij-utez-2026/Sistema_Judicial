package mx.edu.utez.sij.oficialia.model.dao;


import java.util.List;

/**
 * Interfaz genérica DAO para estandarizar las operaciones de acceso a datos.
 * Esta interfaz permite que hoy se use almacenamiento en memoria y en el futuro
 * se sustituya directamente por persistencia en Base de Datos (JDBC) sin afectar a los Servlets.
 *
 * @param <T> Entidad del modelo
 */
public interface Dao<T> {
    List<T> getAll();
    T getById(String id);
    boolean save(T item);
    boolean update(T item);
    boolean delete(String id);
}
