package mx.edu.utez.sij.copias;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Se ejecuta una vez al iniciar la aplicación: carga los datos de prueba de Copias (solo si no hay tarifas).
 */
@WebListener
public class ArranqueCopias implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        new DatosInicialesCopias(Copias.tarifaService(), Copias.usuarios(), Copias.personalService(),
                Copias.solicitudService(), Copias.historialService(), Copias.entregaService()).cargar();
    }
}
