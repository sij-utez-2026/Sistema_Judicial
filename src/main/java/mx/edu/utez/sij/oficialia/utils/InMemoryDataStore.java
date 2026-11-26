package mx.edu.utez.sij.oficialia.utils;


import mx.edu.utez.sij.oficialia.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Almacén central de datos en memoria (Singleton Thread-Safe).
 * Mantiene la persistencia durante la ejecución del servidor.
 */
public class InMemoryDataStore {

    private static InMemoryDataStore instance;

    private final List<Usuario> usuarios = Collections.synchronizedList(new ArrayList<>());
    private final List<Distrito> distritos = Collections.synchronizedList(new ArrayList<>());
    private final List<Juzgado> juzgados = Collections.synchronizedList(new ArrayList<>());
    private final List<Oficialia> oficialias = Collections.synchronizedList(new ArrayList<>());
    private final List<Materia> materias = Collections.synchronizedList(new ArrayList<>());

    private InMemoryDataStore() {
        initDefaultData();
    }

    public static synchronized InMemoryDataStore getInstance() {
        if (instance == null) {
            instance = new InMemoryDataStore();
        }
        return instance;
    }

    private void initDefaultData() {
        // Usuario Superadministrador según nuevos diseños
        usuarios.add(new Usuario(1, "Elena Martínez Salgado", "elena.martinez@tsjmorelos.gob.mx", "admin123", "Superadministrador", "EM"));
        usuarios.add(new Usuario(2, "Administrador Institucional", "nombre@tsjmorelos.gob.mx", "admin123", "Superadministrador", "AI"));

        // Distritos (datos exactos de los nuevos bocetos - Imagen 2)
        distritos.add(new Distrito("D-01", "Primer Distrito Judicial", "Cuernavaca", "Cuernavaca, Huitzilac", "Activo"));
        distritos.add(new Distrito("D-02", "Segundo Distrito Judicial", "Tetecala", "Tetecala, Coatlán del Río, Miacatlán", "Activo"));
        distritos.add(new Distrito("D-06", "Sexto Distrito Judicial", "Cuautla", "Cuautla, Atlatlahucan, Yecapixtla", "Activo"));
        distritos.add(new Distrito("D-09", "Noveno Distrito Judicial", "Jiutepec", "Jiutepec", "Inactivo"));

        // Juzgados (datos exactos de los nuevos bocetos - Imagen 4)
        juzgados.add(new Juzgado("J-CIV-01", "Juzgado Primero Civil", "Primer Distrito Judicial", "Civil", "Activo"));
        juzgados.add(new Juzgado("J-FAM-02", "Juzgado Segundo Familiar", "Primer Distrito Judicial", "Familiar", "Activo"));
        juzgados.add(new Juzgado("J-MIX-01", "Juzgado Mixto de Primera Instancia", "Segundo Distrito Judicial", "Mixta", "Activo"));
        juzgados.add(new Juzgado("J-PEN-03", "Juzgado Tercero Penal", "Sexto Distrito Judicial", "Penal", "Inactivo"));
        juzgados.add(new Juzgado("J-MER-01", "Juzgado Primero Mercantil", "Noveno Distrito Judicial", "Mercantil", "Activo"));

        // Oficialías de Partes (datos exactos de los nuevos bocetos - Imagen 3)
        oficialias.add(new Oficialia("OP-001", "Oficialía de Partes Común Civil", "Juzgado Primero Civil", "Activo"));
        oficialias.add(new Oficialia("OP-002", "Oficialía de Partes Familiar", "Juzgado Segundo Familiar", "Activo"));
        oficialias.add(new Oficialia("OP-003", "Oficialía de Partes Mixta Tetecala", "Juzgado Mixto de Primera Instancia", "Inactivo"));

        // Materias (datos exactos de los nuevos bocetos - Imagen 5)
        materias.add(new Materia("MAT-CIV", "Civil", "Asuntos y controversias de carácter civil.", "Activo"));
        materias.add(new Materia("MAT-FAM", "Familiar", "Asuntos relacionados con derecho familiar.", "Activo"));
        materias.add(new Materia("MAT-PEN", "Penal", "Procesos y asuntos de materia penal.", "Activo"));
        materias.add(new Materia("MAT-MER", "Mercantil", "Controversias y procedimientos mercantiles.", "Activo"));
        materias.add(new Materia("MAT-LAB", "Laboral", "Conflictos y procedimientos laborales.", "Inactivo"));
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Distrito> getDistritos() {
        return distritos;
    }

    public List<Juzgado> getJuzgados() {
        return juzgados;
    }

    public List<Oficialia> getOficialias() {
        return oficialias;
    }

    public List<Materia> getMaterias() {
        return materias;
    }
}

