package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.peritajes.model.Materia;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// TEMPORAL: materias de prueba mientras Oficialía no implementa CatalogoJudicialApi (docs/CONTRATOS.md 3.1).
// Son los datos iniciales sugeridos en docs/ARQUITECTURA.md (pendiente 2). Cuando exista el contrato, se usa
// CatalogoJudicialApi y esta clase se borra.
public class MateriaDao {

    private static final List<Materia> MATERIAS = List.of(
            new Materia(1L, "Civil"),
            new Materia(2L, "Familiar"),
            new Materia(3L, "Mercantil"),
            new Materia(4L, "Penal"));

    public List<Materia> listarActivas() {
        return MATERIAS;
    }

    public Materia getById(Long id) {
        for (Materia materia : MATERIAS) {
            if (materia.getId().equals(id)) {
                return materia;
            }
        }
        return null;
    }

    // true si hay al menos una y todas están en el catálogo (evita ids alterados en el formulario)
    public boolean todasExisten(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            if (getById(id) == null) {
                return false;
            }
        }
        return true;
    }

    // "Civil · Familiar"
    public String nombres(Collection<Long> ids) {
        return ids.stream()
                .map(this::getById)
                .filter(Objects::nonNull)
                .map(Materia::getNombre)
                .collect(Collectors.joining(" · "));
    }
}
