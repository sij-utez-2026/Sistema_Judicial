package mx.edu.utez.sij.peritajes.model;

import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Qué puede hacer cada rol dentro de Peritajes (peritajes.md, sección 3)
class UsuarioPeritajesTest {

    private UsuarioPeritajes de(String tabla, String rol) {
        return UsuarioPeritajes.de(new UsuarioSesion(7L, tabla, "Ana Gil", "ana@example.com", rol));
    }

    @Test
    void administradorGeneral_administraPeroNoVeDictamenes() {
        UsuarioPeritajes admin = de("user_Ofc", UsuarioSesion.ADMINISTRADOR_GENERAL);
        assertTrue(admin.isAdministra());
        assertFalse(admin.isAtiendeCasos());
        assertNull(admin.getId());          // no está en user_Per
        assertEquals("Administrador general", admin.getRol());
    }

    @Test
    void rolesDePeritajes() {
        UsuarioPeritajes admin = de("user_Per", UsuarioPeritajes.ADMIN);
        assertTrue(admin.isAdministra());
        assertFalse(admin.isAtiendeCasos());

        UsuarioPeritajes peritoAdmin = de("user_Per", UsuarioPeritajes.PERITO_ADMIN);
        assertTrue(peritoAdmin.isAdministra());
        assertTrue(peritoAdmin.isAtiendeCasos());
        assertEquals(7L, peritoAdmin.getPeritoId());

        UsuarioPeritajes perito = de("user_Per", UsuarioPeritajes.PERITO);
        assertFalse(perito.isAdministra());
        assertTrue(perito.isAtiendeCasos());
    }

    @Test
    void usuariosDeOtrosModulosNoEntran() {
        assertNull(de("user_Aud", "AUDIENCIAS_ADMIN"));
        assertNull(UsuarioPeritajes.de(null));
    }
}
