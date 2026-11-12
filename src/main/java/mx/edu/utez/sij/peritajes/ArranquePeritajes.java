package mx.edu.utez.sij.peritajes;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

// Se ejecuta una vez al iniciar la aplicación: carga los datos de prueba del módulo
// (peritos, peritajes y dictámenes; solo si todavía no hay peritos).
// TEMPORAL: con la base de datos, los datos de prueba pasan a bd/30_peritajes.sql y se borra esta clase.
@WebListener
public class ArranquePeritajes implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        new DatosInicialesPeritajes().cargar();
    }
}
