package mx.edu.utez.sij.peritajes.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Perito del padrón (RF 3.1 – 3.4): un usuario de user_Per con rol PERITAJES_PERITO o PERITAJES_PERITO_ADMIN,
// sus especialidades (educacion_especialidad_Per) y sus materias (perito_materia_Per).
// Las anotaciones las revisa Validador (núcleo) en el registro y la edición.
// Los tamaños máximos son los de las columnas de user_Per.
public class Perito {

    private Long id;                    // id_usuario; el número de registro sale de aquí (PER-0005)

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no debe pasar de 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 50, message = "El apellido paterno no debe pasar de 50 caracteres")
    private String apellidoPaterno;

    @Size(max = 50, message = "El apellido materno no debe pasar de 50 caracteres")
    private String apellidoMaterno;     // opcional

    @NotBlank(message = "El correo institucional es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no debe pasar de 150 caracteres")
    private String correo;              // único en todo el sistema (tabla Login) y no se cambia

    // PERITAJES_PERITO o PERITAJES_PERITO_ADMIN; se elige en el registro
    private String rolSistema = UsuarioPeritajes.PERITO;

    // Texto libre: área o estudios del perito (al menos una). Llegan como especialidades[0], especialidades[1]...
    private List<String> especialidades = new ArrayList<>();

    // Ids de materia_Ofc (id_materia)
    @NotEmpty(message = "Seleccione al menos una materia para la asignación")
    private Set<Long> materias = new LinkedHashSet<>();

    private boolean activo = true;      // columna Status

    // Campos de solo lectura que llena PeritoDao para las pantallas
    private String materiasTexto;       // "Civil · Familiar"
    private long cargaAsignados;
    private long cargaEnProceso;

    // RF 3.1: "la especialidad es un campo obligatorio"
    @AssertTrue(message = "La especialidad es un campo obligatorio: capture al menos una")
    public boolean isEspecialidadCapturada() {
        return especialidades != null
                && especialidades.stream().anyMatch(e -> e != null && !e.isBlank());
    }

    // Q1: el número de registro es el id con formato PER-0005
    public static String numeroRegistro(Long id) {
        return id == null ? "" : String.format("PER-%04d", id);
    }

    public String getNumeroRegistro() {
        return numeroRegistro(id);
    }

    public String getNombreCompleto() {
        String completo = nombre + " " + apellidoPaterno;
        if (apellidoMaterno != null && !apellidoMaterno.isBlank()) {
            completo += " " + apellidoMaterno;
        }
        return completo;
    }

    public String getRol() {
        return UsuarioPeritajes.textoRol(rolSistema);
    }

    // Carga activa: Asignados + En Proceso (regla de negocio 3 del DFR)
    public long getCargaTotal() {
        return cargaAsignados + cargaEnProceso;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRolSistema() {
        return rolSistema;
    }

    public void setRolSistema(String rolSistema) {
        this.rolSistema = rolSistema;
    }

    public List<String> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<String> especialidades) {
        this.especialidades = especialidades;
    }

    public Set<Long> getMaterias() {
        return materias;
    }

    public void setMaterias(Set<Long> materias) {
        this.materias = materias;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getMateriasTexto() {
        return materiasTexto;
    }

    public void setMateriasTexto(String materiasTexto) {
        this.materiasTexto = materiasTexto;
    }

    public long getCargaAsignados() {
        return cargaAsignados;
    }

    public void setCargaAsignados(long cargaAsignados) {
        this.cargaAsignados = cargaAsignados;
    }

    public long getCargaEnProceso() {
        return cargaEnProceso;
    }

    public void setCargaEnProceso(long cargaEnProceso) {
        this.cargaEnProceso = cargaEnProceso;
    }
}
