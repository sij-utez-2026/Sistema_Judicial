package mx.edu.utez.sij.peritajes.model;

// Perito que puede recibir un peritaje: activo y con la materia (RF 2.2 y 2.3), con su carga activa.
public class CandidatoAsignacion {

    private final Long peritoId;
    private final String nombre;
    private final String materias;      // "Civil · Familiar"
    private final long carga;           // Asignado + En Proceso

    public CandidatoAsignacion(Long peritoId, String nombre, String materias, long carga) {
        this.peritoId = peritoId;
        this.nombre = nombre;
        this.materias = materias;
        this.carga = carga;
    }

    public Long getPeritoId() {
        return peritoId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMaterias() {
        return materias;
    }

    public long getCarga() {
        return carga;
    }
}
