package mx.edu.utez.sij.peritajes.model;

public class CandidatoAsignacion {

    private final Long peritoId;
    private final String nombre;
    private final String materias;      // "Civil · Familiar"
    private final long carga;           // Asignado + En Proceso
    private final boolean seleccionado; // el que eligió el sistema

    public CandidatoAsignacion(Long peritoId, String nombre, String materias, long carga, boolean seleccionado) {
        this.peritoId = peritoId;
        this.nombre = nombre;
        this.materias = materias;
        this.carga = carga;
        this.seleccionado = seleccionado;
    }

    public CandidatoAsignacion conSeleccion(boolean elegido) {
        return new CandidatoAsignacion(peritoId, nombre, materias, carga, elegido);
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

    public boolean isSeleccionado() {
        return seleccionado;
    }
}
