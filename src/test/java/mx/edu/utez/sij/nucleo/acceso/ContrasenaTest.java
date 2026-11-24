package mx.edu.utez.sij.nucleo.acceso;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Contraseñas del sistema (cifrado PBKDF2, regla de la contraseña nueva y temporal)
class ContrasenaTest {

    @Test
    void cifraYReconoceLaContrasena() {
        String guardada = Contrasena.cifrar("Peritajes2026");

        assertTrue(guardada.startsWith("pbkdf2$"));
        assertTrue(guardada.length() <= 255);                  // cabe en user_Per.contrasena
        assertTrue(Contrasena.coincide("Peritajes2026", guardada));
        assertFalse(Contrasena.coincide("peritajes2026", guardada));
        assertFalse(Contrasena.coincide("Peritajes2026", "texto-que-no-es-un-hash"));
    }

    @Test
    void cadaCifradoUsaUnaSalDistinta() {
        assertNotEquals(Contrasena.cifrar("Peritajes2026"), Contrasena.cifrar("Peritajes2026"));
    }

    @Test
    void reglaDeLaContrasenaNueva() {
        assertNull(Contrasena.validarNueva("Peritajes2026"));
        assertNotNull(Contrasena.validarNueva("Corta1"));        // menos de 8
        assertNotNull(Contrasena.validarNueva("sinmayuscula1"));
        assertNotNull(Contrasena.validarNueva("SINMINUSCULA1"));
        assertNotNull(Contrasena.validarNueva("SinNumeros"));
    }

    @Test
    void laTemporalCumpleLaRegla() {
        for (int i = 0; i < 50; i++) {
            String temporal = Contrasena.generarTemporal();
            assertNull(Contrasena.validarNueva(temporal), temporal);
        }
    }
}
