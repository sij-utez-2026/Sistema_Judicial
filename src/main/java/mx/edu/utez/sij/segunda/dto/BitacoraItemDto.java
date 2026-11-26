package mx.edu.utez.sij.segunda.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BitacoraItemDto {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final Long id;
    private final LocalDateTime fecha;
    private final String tipoFuente;
    private final String nombreTabla;
    private final Long registroId;
    private final String accion;
    private final Long usuarioId;
    private final String usuarioNombre;
    private final String valoresAnteriores;
    private final String valoresNuevos;
    private final String detalle;
    private final String tono;

    public BitacoraItemDto(Long id, LocalDateTime fecha, String tipoFuente, String nombreTabla,
                           Long registroId, String accion, Long usuarioId, String usuarioNombre,
                           String valoresAnteriores, String valoresNuevos, String detalle, String tono) {
        this.id = id; this.fecha = fecha; this.tipoFuente = tipoFuente; this.nombreTabla = nombreTabla;
        this.registroId = registroId; this.accion = accion; this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre; this.valoresAnteriores = valoresAnteriores;
        this.valoresNuevos = valoresNuevos; this.detalle = detalle; this.tono = tono;
    }

    public Long getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public String getFechaTexto() { return fecha == null ? "" : fecha.format(FECHA); }
    public String getTipoFuente() { return tipoFuente; }
    public String getNombreTabla() { return nombreTabla; }
    public Long getRegistroId() { return registroId; }
    public String getAccion() { return accion; }
    public Long getUsuarioId() { return usuarioId; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public String getValoresAnteriores() { return valoresAnteriores; }
    public String getValoresNuevos() { return valoresNuevos; }
    public String getDetalle() { return detalle; }
    public String getTono() { return tono; }
}
