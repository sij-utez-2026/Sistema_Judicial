package mx.edu.utez.sij.nucleo.bd;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexiones a MySQL para los DAO (INTEGRADORES). Usa un pool (HikariCP): las conexiones se reutilizan.
 *
 * Uso en un DAO, SIEMPRE con try-with-resources para devolver la conexión al pool:
 * <pre>
 * try (Connection con = ConexionBD.obtener();
 *      PreparedStatement ps = con.prepareStatement("SELECT ... WHERE id = ?")) {
 *     ps.setLong(1, id);
 *     try (ResultSet rs = ps.executeQuery()) { ... }
 * }
 * </pre>
 *
 * Datos de conexión (el primero que exista):
 * 1. Variables de entorno SIJ_BD_URL, SIJ_BD_USUARIO, SIJ_BD_PASSWORD.
 * 2. src/main/resources/bd.properties (cada quien el suyo; NO se sube a GitHub). Copiar bd.example.properties.
 *
 * El pool se crea la primera vez que un DAO pide una conexión: mientras los módulos usen DAOs en memoria,
 * el sistema arranca sin base de datos.
 */
public final class ConexionBD {

    private static HikariDataSource pool;

    private ConexionBD() {
    }

    public static Connection obtener() throws SQLException {
        return pool().getConnection();
    }

    /** Lo llama ArranqueNucleo al detener la aplicación. */
    public static synchronized void cerrar() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

    private static synchronized HikariDataSource pool() {
        if (pool == null) {
            Properties archivo = leerArchivo();
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl(valor("SIJ_BD_URL", archivo, "bd.url"));
            config.setUsername(valor("SIJ_BD_USUARIO", archivo, "bd.usuario"));
            config.setPassword(valor("SIJ_BD_PASSWORD", archivo, "bd.password"));
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(20_000);
            pool = new HikariDataSource(config);
        }
        return pool;
    }

    private static String valor(String variableEntorno, Properties archivo, String clave) {
        String valor = System.getenv(variableEntorno);
        if (valor == null || valor.isBlank()) {
            valor = archivo.getProperty(clave);
        }
        if (valor == null) {
            throw new IllegalStateException("Falta configurar la base de datos: " + clave
                    + " (copia src/main/resources/bd.example.properties como bd.properties)");
        }
        return valor;
    }

    private static Properties leerArchivo() {
        Properties propiedades = new Properties();
        try (InputStream entrada = ConexionBD.class.getClassLoader().getResourceAsStream("bd.properties")) {
            if (entrada != null) {
                propiedades.load(entrada);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer bd.properties", e);
        }
        return propiedades;
    }
}
