package mx.edu.utez.sij.nucleo.acceso;

import mx.edu.utez.sij.nucleo.web.ModuloSistema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// A qué módulos entra cada rol (FiltroNucleo e InicioServlet)
class UsuarioSesionTest {

    private UsuarioSesion con(String rol) {
        return new UsuarioSesion(1L, "user_Per", "Ana Gil", "ana@example.com", rol);
    }

    @Test
    void elAdministradorGeneralEntraATodos() {
        UsuarioSesion admin = con(UsuarioSesion.ADMINISTRADOR_GENERAL);
        for (ModuloSistema modulo : ModuloSistema.values()) {
            assertTrue(admin.puedeEntrar(modulo));
        }
        assertEquals("Administrador general", admin.getRolTexto());
    }

    @Test
    void losDemasSoloEntranASuModulo() {
        UsuarioSesion perito = con("PERITAJES_PERITO");
        assertEquals(ModuloSistema.PERITAJES, perito.getModulo());
        assertTrue(perito.puedeEntrar(ModuloSistema.PERITAJES));
        assertFalse(perito.puedeEntrar(ModuloSistema.AUDIENCIAS));
        assertFalse(perito.puedeEntrar(ModuloSistema.OFICIALIA));

        assertEquals(ModuloSistema.COPIAS, con("COPIAS_JUEZ").getModulo());
    }

    @Test
    void unRolSinModuloNoEntraANinguno() {
        UsuarioSesion raro = con("INVITADO");
        assertNull(raro.getModulo());
        for (ModuloSistema modulo : ModuloSistema.values()) {
            assertFalse(raro.puedeEntrar(modulo));
        }
    }
}
