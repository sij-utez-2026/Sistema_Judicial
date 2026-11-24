package mx.edu.utez.sij.peritajes.controller;

import mx.edu.utez.sij.nucleo.web.Validador;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Reglas del formulario de perito (RF 3.1 y 3.3): anotaciones del model + PeritoFormSupport.validar.
// No usan la base de datos: el correo repetido y las materias del catálogo los revisa PeritoDao.
class PeritoFormSupportTest {

    private Perito perito(String nombre, String correo, String... materias) {
        Perito perito = new Perito();
        perito.setNombre(nombre);
        perito.setApellidoPaterno("Neri");
        perito.setApellidoMaterno("Campos");
        perito.setCorreo(correo);
        perito.setRolSistema(UsuarioPeritajes.PERITO);
        perito.setEspecialidades(new ArrayList<>(List.of("Psicología")));
        perito.setMaterias(new LinkedHashSet<>(List.of(materias)));
        return perito;
    }

    @Test
    void normalizaLosDatosDelRegistro() {
        Perito datos = perito(" Sofía ", " Sofia.Neri@PJ.mx ", "FAM");
        datos.setApellidoMaterno("  ");

        assertNull(PeritoFormSupport.validar(datos, true));

        assertEquals("Sofía", datos.getNombre());
        assertNull(datos.getApellidoMaterno());
        assertEquals("Sofía Neri", datos.getNombreCompleto());
        assertEquals("sofia.neri@pj.mx", datos.getCorreo());
    }

    @Test
    void elRegistroExigeUnRolDePerito() {
        Perito datos = perito("Ana", "ana@pj.mx", "CIV");
        datos.setRolSistema(UsuarioPeritajes.ADMIN);
        assertEquals("Seleccione si el perito será Perito o Perito Administrador",
                PeritoFormSupport.validar(datos, true));

        datos.setRolSistema(UsuarioPeritajes.PERITO_ADMIN);
        assertNull(PeritoFormSupport.validar(datos, true));
    }

    @Test
    void limpiaEspecialidadesVaciasYRepetidas() {
        Perito datos = perito("Ana", "ana@pj.mx", "CIV");
        datos.setEspecialidades(new ArrayList<>(Arrays.asList(" Psicología ", "", null, "psicología", "Grafología")));

        assertNull(PeritoFormSupport.validar(datos, true));
        assertEquals(List.of("Psicología", "Grafología"), datos.getEspecialidades());
    }

    @Test
    void exigeAlMenosUnaEspecialidad() {
        Perito datos = perito("Ana", "ana@pj.mx", "CIV");
        datos.setEspecialidades(new ArrayList<>(List.of(" ", "")));

        assertTrue(Validador.validar(datos).containsKey("especialidadCapturada"));
        assertEquals("La especialidad es un campo obligatorio: capture al menos una",
                PeritoFormSupport.validar(datos, true));
    }

    @Test
    void anotacionesDelModel() {
        Perito datos = perito("", "no-es-correo");

        var errores = Validador.validar(datos);
        assertTrue(errores.containsKey("nombre"));
        assertTrue(errores.containsKey("correo"));
        assertTrue(errores.containsKey("materias"));
    }

    @Test
    void elNumeroDeRegistroSaleDelId() {
        Perito datos = perito("Ana", "ana@pj.mx", "CIV");
        datos.setId(5L);
        assertEquals("PER-0005", datos.getNumeroRegistro());
    }
}
