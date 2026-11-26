package mx.edu.utez.sij.segunda;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mx.edu.utez.sij.nucleo.contratos.Contratos;

/**
 * Se ejecuta una vez al iniciar la aplicación:
 * 1. Registra las implementaciones de los contratos que este módulo ofrece.
 * 2. No inserta ni carga fixtures: la fuente de datos es la base configurada.
 */
@WebListener
public class ArranqueSegunda implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        // Contratos que Segunda Instancia OFRECE a otros módulos
        Contratos.registrar(Segunda.salaService());
        Contratos.registrar(Segunda.recepcionTocaService());

        // No se insertan datos ficticios ni se ejecutan escrituras en el arranque.
    }
}

