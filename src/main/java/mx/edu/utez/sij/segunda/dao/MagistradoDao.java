package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.segunda.model.Magistrado;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a los datos de los magistrados (DAO).
 *
 * La tabla real es magistrado_Seg y se une con user_Seg por id_usuario. id_magistrado
 * no debe confundirse con user_Seg.id_usuario; la sala vive en magistrado_Seg.id_sala.
 * La implementación JDBC aún debe respetar esta separación.
 */
public interface MagistradoDao {

    Optional<Magistrado> findById(Long id);

    Magistrado save(Magistrado magistrado);

    long count();

    long countByActivo(boolean activo);

    boolean existsByCorreoIgnoreCase(String correo);

    /** ¿Ya hay un magistrado asignado a esa sala? (uno a uno). */
    boolean existsBySalaId(Long salaId, Long excluirId);

    /** ¿Ese usuario ya está registrado como magistrado? */
    boolean existsByUsuarioId(Long usuarioId);

    Pagina<Magistrado> buscar(String texto, Boolean activo, int pagina, int tamano);

    /** Todos los activos, para selectores. */
    List<Magistrado> activos();
}

