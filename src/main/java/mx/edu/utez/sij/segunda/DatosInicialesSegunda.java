package mx.edu.utez.sij.segunda;

import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.segunda.dto.MagistradoForm;
import mx.edu.utez.sij.segunda.dto.ResolucionForm;
import mx.edu.utez.sij.segunda.dto.SalaForm;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.segunda.service.*;

import java.time.LocalDateTime;

public class DatosInicialesSegunda {

    private final SalaService salaService;
    private final MagistradoService magistradoService;
    private final TocaService tocaService;
    private final PonenciaService ponenciaService;
    private final ResolucionService resolucionService;
    private final RecepcionTocaService recepcionTocaService;

    public DatosInicialesSegunda(SalaService salaService,
                                 MagistradoService magistradoService,
                                 TocaService tocaService,
                                 PonenciaService ponenciaService,
                                 ResolucionService resolucionService,
                                 RecepcionTocaService recepcionTocaService) {
        this.salaService = salaService;
        this.magistradoService = magistradoService;
        this.tocaService = tocaService;
        this.ponenciaService = ponenciaService;
        this.resolucionService = resolucionService;
        this.recepcionTocaService = recepcionTocaService;
    }

    public void cargar() {
        if (salaService.totalRegistradas() == 0) {
            registrarSala("Sala 1 Civil", "D1", "Av. Morelos 100, Cuernavaca");
            registrarSala("Sala 2 Familiar", "D1", "Av. Morelos 200, Cuernavaca");
            registrarSala("Sala 3 Mercantil", "D2", "Calle Juárez 50, Cuautla");
            registrarSala("Sala 4 Penal", "D2", "Calle Juárez 60, Cuautla");
        }

        if (magistradoService.totalRegistrados() == 0) {
            registrarMagistrado("María Elena", "Vázquez Ruiz", "maria.vazquez@example.com", 1L);
            registrarMagistrado("Juan Carlos", "Hernández López", "juan.hernandez@example.com", 2L);
        }

        if (tocaService.totalRegistradas() == 0) {
            Long id1 = crearToca("EXP-2026-00001", "M1", "J1", "D1", 1L, null);
            Long id2 = crearToca("EXP-2026-00002", "M2", "J2", "D1", 2L, null);
            Long id3 = crearToca("EXP-2026-00003", "M3", "J3", "D2", 3L, null);

            // Ponencias de prueba: una futura, una de hoy, y otra toca sin ponencia
            registrarPonencia(id1, LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));
            registrarPonencia(id2, LocalDateTime.now().withHour(12).withMinute(0));
            // id3 queda sin ponencia

            // Resolución de prueba para la toca 2 (que ya tiene ponencia hoy)
            registrarResolucion(id2, "CONFIRMA",
                    "Se confirma la sentencia recurrida en todas sus partes.");
        }
    }

    private void registrarSala(String nombre, String distritoId, String direccion) {
        SalaForm form = new SalaForm();
        form.setNombre(nombre);
        form.setDistritoId(distritoId);
        form.setDireccion(direccion);
        salaService.registrar(form);
    }

    private void registrarMagistrado(String nombre, String apellidos, String correo, Long salaId) {
        MagistradoForm form = new MagistradoForm();
        form.setNombre(nombre);
        form.setApellidos(apellidos);
        form.setCorreo(correo);
        form.setSalaId(salaId);
        magistradoService.registrar(form);
    }

    private Long crearToca(String numeroExpediente, String materiaId, String juzgadoOrigenId,
                           String distritoId, Long salaId, Long documentoApelacionArchivoId) {
        try {
            Toca toca = tocaService.recibirDeOficialia(numeroExpediente, materiaId, juzgadoOrigenId,
                    distritoId, salaId, documentoApelacionArchivoId);
            System.out.println("[DatosInicialesSegunda] Toca creada: " + toca.getFolioToca()
                    + " → " + numeroExpediente);
            return toca.getId();
        } catch (ReglaNegocioException e) {
            System.out.println("[DatosInicialesSegunda] No se pudo crear toca " + numeroExpediente
                    + ": " + e.getMessage());
            return null;
        }
    }

    private void registrarPonencia(Long tocaId, LocalDateTime fechaHora) {
        if (tocaId == null) return;
        try {
            ponenciaService.registrar(tocaId, fechaHora);
            System.out.println("[DatosInicialesSegunda] Ponencia registrada para toca " + tocaId
                    + " · " + fechaHora);
        } catch (ReglaNegocioException e) {
            System.out.println("[DatosInicialesSegunda] No se pudo registrar ponencia: "
                    + e.getMessage());
        }
    }

    private void registrarResolucion(Long tocaId, String sentido, String comentarios) {
        if (tocaId == null) return;
        try {
            ResolucionForm form = new ResolucionForm();
            form.setSentido(sentido);
            form.setComentarios(comentarios);
            resolucionService.registrar(tocaId, form);
            System.out.println("[DatosInicialesSegunda] Resolución registrada para toca " + tocaId);
        } catch (ReglaNegocioException e) {
            System.out.println("[DatosInicialesSegunda] No se pudo registrar resolución: "
                    + e.getMessage());
        }
    }
}


