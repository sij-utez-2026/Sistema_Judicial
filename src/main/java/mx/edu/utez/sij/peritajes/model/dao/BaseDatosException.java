package mx.edu.utez.sij.peritajes.model.dao;

// Error de MySQL que no es de negocio (la base no responde, una consulta mal escrita...).
// Los DAO no esconden los errores de SQL (docs/ARQUITECTURA.md, sección 11): los convierten en esta excepción,
// que llega hasta Tomcat y muestra la página de error 500. Así los servlets no tienen que atrapar SQLException.
public class BaseDatosException extends RuntimeException {

    public BaseDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
