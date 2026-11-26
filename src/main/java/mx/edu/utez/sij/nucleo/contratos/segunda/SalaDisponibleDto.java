package mx.edu.utez.sij.nucleo.contratos.segunda;

public record SalaDisponibleDto(
        Long salaId,
        String nombre,
        String distritoId,
        String direccion,
        Long magistradoUsuarioId,
        String magistradoNombre) {
}