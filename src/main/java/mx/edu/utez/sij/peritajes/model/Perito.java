package mx.edu.utez.sij.peritajes.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Perito del padrón (RF 3.1 – 3.4). Las anotaciones las revisa Validador (núcleo) en el registro y la edición.
// Mensajes según los criterios de aceptación del DFR (RF 3.1 y 3.3).
public class Perito {

    private Long id;

    @NotBlank(message = "El número de registro es obligatorio")
    @Pattern(regexp = "^(?i)PER-\\d{4}$", message = "El número de registro debe tener el formato PER-0000")
    private String numeroRegistro;      // único y no se cambia

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no debe pasar de 80 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 60, message = "El apellido paterno no debe pasar de 60 caracteres")
    private String apellidoPaterno;

    @Size(max = 60, message = "El apellido materno no debe pasar de 60 caracteres")
    private String apellidoMaterno;     // opcional

    @NotBlank(message = "El correo institucional es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no debe pasar de 150 caracteres")
    private String correo;              // único y no se cambia

    // 10 dígitos; en el formulario se permiten espacios (777 000 0186), se guardan solo los dígitos
    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Pattern(regexp = "^\\s*(\\d\\s*){10}$", message = "El teléfono debe tener 10 dígitos")
    private String telefono;

    // Texto libre: área o estudios del perito (al menos una). Llegan como especialidades[0], especialidades[1]...
    private List<String> especialidades = new ArrayList<>();

    // Solo los id: el catálogo de materias es de Oficialía
    @NotEmpty(message = "Seleccione al menos una materia para la asignación")
    private Set<Long> materiasIds = new LinkedHashSet<>();

    private boolean activo = true;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

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

    public String getNombreCompleto() {
        String completo = nombre + " " + apellidoPaterno;
        if (apellidoMaterno != null && !apellidoMaterno.isBlank()) {
            completo += " " + apellidoMaterno;
        }
        return completo;
    }

    // Como se muestra en pantalla: 777 000 0186
    public String getTelefonoFormateado() {
        if (telefono == null || telefono.length() != 10) {
            return telefono;
        }
        return telefono.substring(0, 3) + " " + telefono.substring(3, 6) + " " + telefono.substring(6);
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

    public String getNumeroRegistro() {
        return numeroRegistro;
    }

    public void setNumeroRegistro(String numeroRegistro) {
        this.numeroRegistro = numeroRegistro;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<String> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<String> especialidades) {
        this.especialidades = especialidades;
    }

    public Set<Long> getMateriasIds() {
        return materiasIds;
    }

    public void setMateriasIds(Set<Long> materiasIds) {
        this.materiasIds = materiasIds;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
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
