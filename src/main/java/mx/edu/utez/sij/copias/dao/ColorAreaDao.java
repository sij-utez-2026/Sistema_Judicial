package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.ColorArea;

import java.util.List;
import java.util.Optional;

/** Colores del área (tabla cop_color). Para MySQL: ver SolicitudCopiaDao. */
public interface ColorAreaDao {

    /** SQL: INSERT ... o UPDATE cop_color SET valor = ?, actualizado_por = ?, actualizado_en = ? WHERE id = ? */
    ColorArea save(ColorArea color);

    /** SQL: {@code SELECT * FROM cop_color} */
    List<ColorArea> findAll();

    /** SQL: {@code SELECT * FROM cop_color WHERE variable = ?} */
    Optional<ColorArea> findByVariable(String variable);
}
