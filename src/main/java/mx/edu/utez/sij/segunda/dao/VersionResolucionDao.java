package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.segunda.model.VersionResolucion;

import java.util.List;

public interface VersionResolucionDao {

    VersionResolucion save(VersionResolucion version);

    List<VersionResolucion> findByResolucionId(Long resolucionId);
}


