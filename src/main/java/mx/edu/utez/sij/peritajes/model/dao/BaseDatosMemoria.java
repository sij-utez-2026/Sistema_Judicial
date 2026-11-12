package mx.edu.utez.sij.peritajes.model.dao;

import mx.edu.utez.sij.peritajes.model.Dictamen;
import mx.edu.utez.sij.peritajes.model.HistorialAsignacion;
import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.RegistroBitacora;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

// TEMPORAL: hace las veces de la base de datos mientras no hay MySQL (los datos se pierden al reiniciar).
// Todos los DAO del módulo guardan aquí, así cualquier "new PeritoDao()" ve los mismos datos.
// Cuando exista la base de datos: cada DAO usa ConexionBD (núcleo) con la consulta de su comentario "SQL:"
// y esta clase se borra.
public final class BaseDatosMemoria {

    static final Map<Long, Perito> PERITOS = new ConcurrentHashMap<>();
    static final Map<Long, Peritaje> PERITAJES = new ConcurrentHashMap<>();
    static final Map<Long, Dictamen> DICTAMENES = new ConcurrentHashMap<>();
    static final List<HistorialAsignacion> HISTORIAL = new CopyOnWriteArrayList<>();
    static final List<RegistroBitacora> BITACORA = new CopyOnWriteArrayList<>();

    // Como el AUTO_INCREMENT de cada tabla
    static final AtomicLong ID_PERITO = new AtomicLong(1);
    static final AtomicLong ID_PERITAJE = new AtomicLong(1);
    static final AtomicLong ID_DICTAMEN = new AtomicLong(1);
    static final AtomicLong ID_HISTORIAL = new AtomicLong(1);
    static final AtomicLong ID_ARCHIVO = new AtomicLong(1);

    private BaseDatosMemoria() {
    }

    // Deja todo vacío. Lo usan las pruebas antes de cada caso.
    public static void limpiar() {
        PERITOS.clear();
        PERITAJES.clear();
        DICTAMENES.clear();
        HISTORIAL.clear();
        BITACORA.clear();
        ID_PERITO.set(1);
        ID_PERITAJE.set(1);
        ID_DICTAMEN.set(1);
        ID_HISTORIAL.set(1);
        ID_ARCHIVO.set(1);
    }
}
