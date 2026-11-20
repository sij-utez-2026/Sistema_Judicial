package mx.edu.utez.sij.copias.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago simulado de una solicitud de copias certificadas (tabla cop_pago, RF 4.1).
 * No realiza cargos reales (simulado = true); genera un folio de comprobante CP-AAAA-XXXXXX.
 */
public class Pago {

    private Long id;
    private Long solicitudId;
    private String folioPago;
    private BigDecimal monto;
    private boolean simulado = true;
    private Long registradoPor;
    private LocalDateTime fecha;

    public String getMontoTexto() {
        return Formatos.dinero(monto);
    }

    public String getFechaTexto() {
        return Formatos.fechaHora(fecha);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }

    public String getFolioPago() {
        return folioPago;
    }

    public void setFolioPago(String folioPago) {
        this.folioPago = folioPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public boolean isSimulado() {
        return simulado;
    }

    public void setSimulado(boolean simulado) {
        this.simulado = simulado;
    }

    public Long getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Long registradoPor) {
        this.registradoPor = registradoPor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
