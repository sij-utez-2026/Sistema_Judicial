package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.EstadoSolicitud;
import mx.edu.utez.sij.copias.model.SolicitudCopia;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a los datos de solicitudes de copias certificadas (DAO).
 *
 * Para pasar a MySQL (cop_solicitud): crear SolicitudDaoMySql usando ConexionBD
 * y PreparedStatement (docs/ARQUITECTURA.md, sección 11).
 */
public interface SolicitudDao {

    Optional<SolicitudCopia> buscarPorId(Long id);

    Optional<SolicitudCopia> buscarPorFolio(String folioSolicitud);

    List<SolicitudCopia> listarTodas();

    SolicitudCopia guardar(SolicitudCopia solicitud);

    void actualizarEstado(Long id, EstadoSolicitud nuevoEstado);

    long contar();
}
