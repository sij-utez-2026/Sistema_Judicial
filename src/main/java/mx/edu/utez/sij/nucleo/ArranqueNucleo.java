package mx.edu.utez.sij.nucleo;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mx.edu.utez.sij.nucleo.bd.ConexionBD;

/**
 * Se ejecuta al iniciar y al detener la aplicación (INTEGRADORES).
 * Cada módulo tiene su propio Arranque&lt;Modulo&gt; para sus datos de prueba.
 */
@WebListener
public class ArranqueNucleo implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        ConexionBD.cerrar();
    }
}
