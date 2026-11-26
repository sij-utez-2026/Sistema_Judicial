package mx.edu.utez.sij.nucleo.contratos;

import mx.edu.utez.sij.nucleo.contratos.segunda.RecepcionTocaApi;
import mx.edu.utez.sij.nucleo.contratos.segunda.SalaConsultaApi;

import java.util.Objects;

/**
 * Registro compartido de implementaciones de contratos entre módulos.
 * Cada módulo proveedor registra su implementación durante el arranque; los
 * módulos consumidores solicitan el contrato cuando lo necesitan.
 */
public final class Contratos {

    private static volatile SalaConsultaApi salaConsulta;
    private static volatile RecepcionTocaApi recepcionToca;

    private Contratos() {
    }

    public static void registrar(SalaConsultaApi implementacion) {
        salaConsulta = Objects.requireNonNull(implementacion, "SalaConsultaApi no puede ser null");
    }

    public static SalaConsultaApi salaConsulta() {
        SalaConsultaApi implementacion = salaConsulta;
        if (implementacion == null) {
            throw new IllegalStateException("Segunda Instancia no ha registrado SalaConsultaApi");
        }
        return implementacion;
    }

    public static void registrar(RecepcionTocaApi implementacion) {
        recepcionToca = Objects.requireNonNull(implementacion, "RecepcionTocaApi no puede ser null");
    }

    public static RecepcionTocaApi recepcionToca() {
        RecepcionTocaApi implementacion = recepcionToca;
        if (implementacion == null) {
            throw new IllegalStateException("Segunda Instancia no ha registrado RecepcionTocaApi");
        }
        return implementacion;
    }
}
