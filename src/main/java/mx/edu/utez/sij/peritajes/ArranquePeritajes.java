package mx.edu.utez.sij.peritajes;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Se ejecuta una vez al iniciar la aplicación: carga los datos de prueba del módulo
 * (solo si todavía no hay peritos).
 */
@WebListener
public class ArranquePeritajes implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        new DatosInicialesPeritajes(Peritajes.peritoService()).cargar();
    }
}
