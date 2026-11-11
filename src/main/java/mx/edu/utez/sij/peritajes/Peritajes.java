package mx.edu.utez.sij.peritajes;

import mx.edu.utez.sij.peritajes.dao.PeritoDao;
import mx.edu.utez.sij.peritajes.dao.PeritoDaoMemoria;
import mx.edu.utez.sij.peritajes.service.CatalogoMateriasTemporal;
import mx.edu.utez.sij.peritajes.service.PeritoService;

/**
 * Objetos del módulo de Peritajes (EQUIPO 3). Hay UNA sola instancia de cada uno para toda la aplicación,
 * así todos los servlets comparten los mismos datos.
 *
 * Los servlets los piden así:  PeritoService peritoService = Peritajes.peritoService();
 *
 * Aquí se decide qué implementación se usa: para pasar a MySQL basta cambiar
 * new PeritoDaoMemoria() por new PeritoDaoMySql().
 */
public final class Peritajes {

    private static final PeritoDao PERITO_DAO = new PeritoDaoMemoria();
    private static final CatalogoMateriasTemporal CATALOGO_MATERIAS = new CatalogoMateriasTemporal();
    private static final PeritoService PERITO_SERVICE = new PeritoService(PERITO_DAO, CATALOGO_MATERIAS);

    private Peritajes() {
    }

    public static PeritoService peritoService() {
        return PERITO_SERVICE;
    }
}
