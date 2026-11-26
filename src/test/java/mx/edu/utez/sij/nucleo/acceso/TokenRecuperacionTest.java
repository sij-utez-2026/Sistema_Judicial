package mx.edu.utez.sij.nucleo.acceso;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Enlaces de recuperación de contraseña: de un solo uso y solo el último de cada correo (docs/ARQUITECTURA.md 7.1)
class TokenRecuperacionTest {

    @Test
    void elTokenDiceDeQuienEs() {
        String token = TokenRecuperacion.generar("ana@pj.mx");
        assertEquals("ana@pj.mx", TokenRecuperacion.correoDe(token));
        // Solo letras, números, "-" y "_": va tal cual en el enlace
        assertTrue(token.matches("[A-Za-z0-9_-]{40,}"));
    }

    @Test
    void seUsaUnaSolaVez() {
        String token = TokenRecuperacion.generar("luis@pj.mx");
        TokenRecuperacion.usar(token);
        assertNull(TokenRecuperacion.correoDe(token));
    }

    @Test
    void pedirOtroEnlaceInvalidaElAnterior() {
        String primero = TokenRecuperacion.generar("eva@pj.mx");
        String segundo = TokenRecuperacion.generar("eva@pj.mx");
        assertNotEquals(primero, segundo);
        assertNull(TokenRecuperacion.correoDe(primero));
        assertEquals("eva@pj.mx", TokenRecuperacion.correoDe(segundo));
    }

    @Test
    void tokenInexistenteOVacio() {
        assertNull(TokenRecuperacion.correoDe("no-existe"));
        assertNull(TokenRecuperacion.correoDe(""));
        assertNull(TokenRecuperacion.correoDe(null));
    }
}
