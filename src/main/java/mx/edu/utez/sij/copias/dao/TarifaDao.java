package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.Tarifa;

import java.time.LocalDate;
import java.util.Optional;

/** Tarifas (tabla cop_tarifa). Para MySQL: ver SolicitudCopiaDao. */
public interface TarifaDao {

    /** SQL: INSERT INTO cop_tarifa (monto, vigente_desde, creada_por) VALUES (?, ?, ?) */
    Tarifa save(Tarifa tarifa);

    /** SQL: {@code SELECT COUNT(*) FROM cop_tarifa} */
    long count();

    /**
     * Tarifa vigente en la fecha: la más reciente que ya empezó.
     * SQL: {@code SELECT * FROM cop_tarifa WHERE vigente_desde <= ? ORDER BY vigente_desde DESC LIMIT 1}
     */
    Optional<Tarifa> findVigente(LocalDate fecha);
}
