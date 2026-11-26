package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.segunda.model.HistorialToca;

import java.util.List;

/**
 * Acceso a los datos del historial de la toca (DAO).
 *
 * Es inalterable: solo hay guardar y listar. No hay update ni delete.
 */
public interface HistorialTocaDao {

    HistorialToca save(HistorialToca historial);

    List<HistorialToca> findByTocaId(Long tocaId);
}


