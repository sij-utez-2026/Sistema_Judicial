package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.segunda.model.VersionResolucion;

import java.util.List;

/** El esquema entregado no incluye tabla de versiones de resolución. */
public class VersionResolucionDaoMySql implements VersionResolucionDao {
    @Override public VersionResolucion save(VersionResolucion version) {
        throw new UnsupportedOperationException("El esquema vigente no tiene tabla para versiones de resolución; no se guardó ningún dato.");
    }
    @Override public List<VersionResolucion> findByResolucionId(Long resolucionId) { return List.of(); }
}
