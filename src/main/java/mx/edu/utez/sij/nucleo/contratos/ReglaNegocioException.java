package mx.edu.utez.sij.nucleo.contratos;

/**
 * Error esperado de una regla de negocio compartida por los módulos.
 * Los servlets deben capturar esta excepción y presentar su mensaje sin exponer
 * detalles técnicos al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
