package mx.edu.utez.sij.copias;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Inicializa los datos de prueba del módulo de Copias Certificadas al arrancar la aplicación.
 */
@WebListener
public class ArranqueCopias implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        new DatosInicialesCopias(Copias.solicitudDao(), Copias.pagoDao()).cargar();
    }
}
