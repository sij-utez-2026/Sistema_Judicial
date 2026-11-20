package mx.edu.utez.sij.copias.model;


import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tarifa de la copia certificada (tabla cop_tarifa). La vigente es la más reciente que ya empezó.
 * Quién la configura está pendiente (pendiente 17): por ahora DatosInicialesCopias crea una de $320.
 */
public class Tarifa {

    private Long id;

    private BigDecimal monto;

    private LocalDate vigenteDesde;

    private Long creadaPor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public void setVigenteDesde(LocalDate vigenteDesde) {
        this.vigenteDesde = vigenteDesde;
    }

    public Long getCreadaPor() {
        return creadaPor;
    }

    public void setCreadaPor(Long creadaPor) {
        this.creadaPor = creadaPor;
    }
}
