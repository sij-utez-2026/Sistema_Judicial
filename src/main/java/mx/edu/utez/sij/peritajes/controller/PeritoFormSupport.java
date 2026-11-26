package mx.edu.utez.sij.peritajes.controller;

import jakarta.servlet.http.HttpServletRequest;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.peritajes.model.Perito;
import mx.edu.utez.sij.peritajes.model.UsuarioPeritajes;
import mx.edu.utez.sij.peritajes.model.dao.MateriaDao;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Lectura y validación del formulario de perito (registro y edición).
// Lo comparten PeritoRegistrarServlet y PeritoEditarServlet. Orden en el servlet:
// 1. leer(request)  2. Validador.validar(perito) (anotaciones del model)  3. validar(perito, alta) (reglas del DFR)
// 4. PeritoDao.create / update (revisa en la base el correo repetido y las materias).
final class PeritoFormSupport {

    private static final MateriaDao materiaDao = new MateriaDao();

    private PeritoFormSupport() {
    }

    // Arma el Perito con lo que llegó del navegador, tal cual (las validaciones van después)
    static Perito leer(HttpServletRequest request) {
        Perito perito = new Perito();
        perito.setNombre(Parametros.crudo(request, "nombre"));
        perito.setApellidoPaterno(Parametros.crudo(request, "apellidoPaterno"));
        perito.setApellidoMaterno(Parametros.crudo(request, "apellidoMaterno"));
        perito.setCorreo(Parametros.crudo(request, "correo"));
        perito.setRolSistema(Parametros.texto(request, "rol"));
        // especialidades[0], especialidades[1], ... (peritos.js agrega y quita renglones)
        List<String> especialidades = Parametros.lista(request, "especialidades");
        perito.setEspecialidades(especialidades.isEmpty() ? new ArrayList<>(List.of("")) : especialidades);
        // ids de materia_Ofc; los que no son número se ignoran (PeritoDao revisa que existan)
        perito.setMaterias(Parametros.numeros(request, "materias"));
        return perito;
    }

    // Formulario de registro vacío, con un renglón de especialidad
    static Perito nuevo() {
        Perito perito = new Perito();
        perito.setEspecialidades(new ArrayList<>(List.of("")));
        return perito;
    }

    // Reglas que no caben en las anotaciones (RF 3.1 y 3.3). Deja los datos limpios para guardarlos.
    // alta = true en el registro (revisa el rol y normaliza el correo); en la edición esos dos no cambian.
    // Regresa el mensaje de error, o null si todo está bien.
    static String validar(Perito perito, boolean alta) {
        perito.setNombre(perito.getNombre().trim());
        perito.setApellidoPaterno(perito.getApellidoPaterno().trim());
        perito.setApellidoMaterno(vacioANull(perito.getApellidoMaterno()));

        if (alta) {
            perito.setCorreo(perito.getCorreo().trim().toLowerCase());
            // N1: el rol se elige en el registro (Perito o Perito Administrador)
            if (!UsuarioPeritajes.esRolDePerito(perito.getRolSistema())) {
                return "Seleccione si el perito será Perito o Perito Administrador";
            }
        }

        // Especialidades: sin espacios de más, sin renglones vacíos y sin repetidas (sin importar mayúsculas)
        List<String> limpias = new ArrayList<>();
        Set<String> vistas = new LinkedHashSet<>();
        for (String especialidad : perito.getEspecialidades()) {
            if (especialidad != null && !especialidad.isBlank()) {
                String texto = especialidad.trim().replaceAll("\\s+", " ");
                if (texto.length() > 100) {
                    return "Cada especialidad debe tener máximo 100 caracteres";
                }
                if (vistas.add(texto.toLowerCase())) {
                    limpias.add(texto);
                }
            }
        }
        if (limpias.isEmpty()) {
            return "La especialidad es un campo obligatorio: capture al menos una";
        }
        perito.setEspecialidades(limpias);
        return null;
    }

    // Copia del perito para llenar el formulario de edición (para no cambiar el perito guardado mientras se edita)
    static Perito copiaParaEditar(Perito perito) {
        Perito copia = new Perito();
        copia.setId(perito.getId());
        copia.setNombre(perito.getNombre());
        copia.setApellidoPaterno(perito.getApellidoPaterno());
        copia.setApellidoMaterno(perito.getApellidoMaterno());
        copia.setCorreo(perito.getCorreo());
        copia.setRolSistema(perito.getRolSistema());
        copia.setEspecialidades(new ArrayList<>(perito.getEspecialidades()));
        copia.setMaterias(new LinkedHashSet<>(perito.getMaterias()));
        return copia;
    }

    // Datos que el formulario necesita además del perito
    static void datosDelFormulario(HttpServletRequest request) {
        request.setAttribute("materias", materiaDao.listarActivas());
        request.setAttribute("rolPerito", UsuarioPeritajes.PERITO);
        request.setAttribute("rolPeritoAdmin", UsuarioPeritajes.PERITO_ADMIN);
    }

    private static String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
