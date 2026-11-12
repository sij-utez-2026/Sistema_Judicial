package mx.edu.utez.sij.peritajes;

import mx.edu.utez.sij.peritajes.model.Peritaje;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.DictamenDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritajeDao;
import mx.edu.utez.sij.peritajes.model.dao.PeritoDao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Datos de prueba del módulo (docs/ARQUITECTURA.md, sección 11). Solo se insertan si no hay peritos.
// - Peritos: nombres ficticios de los bocetos de Figma; correos de example.com (dominio reservado para
//   pruebas, para no escribirle a nadie real).
// - Peritajes SGPJ-2026-0137 a 0142: folio, expediente, juzgado, materia, perito y estado del boceto
//   "Peritajes · Listado" (docs/modulos/Nathaly/). Las fechas del boceto se usan donde las hay (0142 y 0138);
//   las demás se completaron para que la asignación automática dé el mismo perito que el boceto.
// Todo pasa por los DAO, así la asignación automática, el historial y la bitácora quedan como en uso real.
// Lo llama ArranquePeritajes al iniciar la aplicación.
// Materias de prueba: 1 Civil, 2 Familiar, 3 Mercantil, 4 Penal (MateriaDao).
public class DatosInicialesPeritajes {

    private static final UsuarioPeritajes DATOS_INICIALES =
            new UsuarioPeritajes("datos", "Sistema", "Datos iniciales", "SISTEMA", null);

    private static final long CIVIL = 1L;
    private static final long FAMILIAR = 2L;
    private static final long MERCANTIL = 3L;
    private static final long PENAL = 4L;

    private final PeritoDao peritoDao = new PeritoDao();
    private final PeritajeDao peritajeDao = new PeritajeDao();
    private final DictamenDao dictamenDao = new DictamenDao();

    public void cargar() {
        if (peritoDao.contar() > 0) {
            return;
        }

        // ---------- Peritos ----------
        Perito elena = registrar("PER-0048", "Elena", "Salgado", "Ruiz", "7775550148",
                List.of("Valuación inmobiliaria"), List.of(CIVIL, FAMILIAR), true);
        Perito daniel = registrar("PER-0051", "Daniel", "Ríos", "Vega", "7775550151",
                List.of("Valuación inmobiliaria", "Arquitectura"), List.of(CIVIL, MERCANTIL), true);
        Perito lucia = registrar("PER-0032", "Lucía", "Torres", "Mora", "7775550132",
                List.of("Criminalística", "Balística"), List.of(CIVIL, PENAL), true);
        Perito oscar = registrar("PER-0041", "Óscar", "Medina", "Sol", "7775550141",
                List.of("Contabilidad"), List.of(MERCANTIL), true);
        registrar("PER-0056", "Marina", "León", "Soto", "7775550186",
                List.of("Psicología", "Psicología forense"), List.of(FAMILIAR), true);
        registrar("PER-0027", "Arturo", "Luna", "Gil", "7775550127",
                List.of("Documentoscopía", "Grafología"), List.of(CIVIL, PENAL), false);
        registrar("PER-0063", "Patricia", "Ramos", null, "7775550163",
                List.of("Medicina legal"), List.of(FAMILIAR, PENAL), true);
        registrar("PER-0070", "Jorge", "Castillo", "Peña", "7775550170",
                List.of("Contabilidad", "Auditoría financiera"), List.of(CIVIL, MERCANTIL), false);

        // ---------- Peritajes (en orden de solicitud) ----------
        // 0137: Civil, Daniel, ya entregado antes de que llegara 0141
        Peritaje p0137 = solicitar("SGPJ-2026-0137", "365/2026", CIVIL, null, fecha(13, 9, 0), daniel);
        peritajeDao.abrirExpediente(p0137, como(daniel), fecha(13, 11, 0));
        dictamenDao.guardar(p0137, como(daniel),
                "<p>Contenido de prueba del dictamen SGPJ-2026-0137 (el boceto no trae su texto).</p>", null, false,
                fecha(16, 11, 50));
        dictamenDao.finalizar(p0137, como(daniel), null, null, fecha(16, 12, 0));

        // 0138: Familiar, Elena, Asignado (boceto: asignado el 16 oct a las 12:30 h)
        solicitar("SGPJ-2026-0138", "391/2026", FAMILIAR, null, fecha(16, 12, 30), elena);

        // 0139: Mercantil, Óscar, En Proceso
        Peritaje p0139 = solicitar("SGPJ-2026-0139", "184/2026", MERCANTIL, null, fecha(17, 10, 0), oscar);
        peritajeDao.abrirExpediente(p0139, como(oscar), fecha(17, 12, 0));

        // 0140: Penal, Lucía, En Proceso
        Peritaje p0140 = solicitar("SGPJ-2026-0140", "207/2026", PENAL, null, fecha(17, 13, 0), lucia);
        peritajeDao.abrirExpediente(p0140, como(lucia), fecha(18, 9, 30));

        // 0141: Civil, Daniel, Asignado
        solicitar("SGPJ-2026-0141", "512/2026", CIVIL, null, fecha(18, 11, 0), daniel);

        // 0142: Civil, Elena, En Proceso con borrador (textos y horas del boceto)
        Peritaje p0142 = solicitar("SGPJ-2026-0142", "438/2026", CIVIL,
                "Determinar el valor comercial de un inmueble urbano para la integración de una prueba pericial "
                        + "en materia civil.", fecha(19, 9, 0), elena);
        peritajeDao.abrirExpediente(p0142, como(elena), fecha(19, 10, 2));
        dictamenDao.guardar(p0142, como(elena), BORRADOR_0142, null, false, fecha(20, 11, 30));
    }

    private Perito registrar(String numeroRegistro, String nombre, String apellidoPaterno, String apellidoMaterno,
                             String telefono, List<String> especialidades, List<Long> materias, boolean activo) {
        Perito perito = new Perito();
        perito.setNumeroRegistro(numeroRegistro);
        perito.setNombre(nombre);
        perito.setApellidoPaterno(apellidoPaterno);
        perito.setApellidoMaterno(apellidoMaterno);
        perito.setCorreo(numeroRegistro.toLowerCase() + "@example.com");
        perito.setTelefono(telefono);
        perito.setEspecialidades(new ArrayList<>(especialidades));
        perito.setMateriasIds(new LinkedHashSet<>(materias));
        peritoDao.create(perito, DATOS_INICIALES);
        if (!activo) {
            peritoDao.cambiarEstado(perito.getId(), false, DATOS_INICIALES);
        }
        return perito;
    }

    // Solicitud como la enviaría Audiencias; el perito del boceto se usa solo para desempatar
    private Peritaje solicitar(String folio, String expediente, long materia, String observaciones,
                               LocalDateTime fecha, Perito peritoDelBoceto) {
        return peritajeDao.solicitar(expediente, null, Set.of(materia), observaciones, fecha, folio,
                peritoDelBoceto.getId());
    }

    private UsuarioPeritajes como(Perito perito) {
        return new UsuarioPeritajes("perito-" + perito.getId(), perito.getNombreCompleto(), "Perito Judicial",
                UsuarioPeritajes.PERITO, perito.getId());
    }

    private LocalDateTime fecha(int diaOctubre, int hora, int minuto) {
        return LocalDateTime.of(2026, 10, diaOctubre, hora, minuto);
    }

    // Texto del boceto "Expediente · Editor de dictamen"
    private static final String BORRADOR_0142 = """
            <h2>Dictamen de valuación inmobiliaria</h2>
            <p>Folio SGPJ-2026-0142 · Expediente 438/2026</p>
            <h3>1. Objeto del dictamen</h3>
            <p>Determinar el valor comercial del inmueble de referencia C-17, ubicado en Cuernavaca, Morelos, \
            conforme a la solicitud del Juzgado Segundo Civil.</p>
            <h3>2. Metodología</h3>
            <p>Se realizó la revisión documental del expediente y el análisis comparativo de referencias \
            inmobiliarias de la misma zona. Se consideraron superficie, conservación y características del entorno.</p>
            <h3>3. Consideraciones técnicas</h3>
            <p>La estimación se sustenta en los antecedentes documentales disponibles y en las características \
            físicas descritas en el anexo técnico. Las referencias utilizadas se integran como soporte de la valoración.</p>
            <h3>4. Conclusión</h3>
            <p>El valor comercial estimado del inmueble es de $1,850,000.00 MXN, sujeto a los alcances y \
            condiciones señalados en el presente dictamen.</p>""";
}
