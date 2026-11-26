package mx.edu.utez.sij.oficialia.utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria para la conexión a Base de Datos (JDBC).
 *
 * NOTA DE ARQUITECTURA:
 * Actualmente el sistema opera con 'InMemoryDataStore' para persistencia en memoria
 * según los requerimientos iniciales. Cuando desees conectar una Base de Datos real
 * (MySQL, PostgreSQL, MariaDB o Oracle), solo debes:
 * 1. Agregar la dependencia del conector en el pom.xml (ej. mysql-connector-j).
 * 2. Configurar las constantes URL, USER, PASSWORD y DRIVER en esta clase.
 * 3. En los DAO (DistritoDao, JuzgadoDao, etc.) sustituir las llamadas a InMemoryDataStore
 *    por las consultas SQL preparadas (PreparedStatement) que ya dejamos estructuradas.
 */
public class SQLConnector {

    // Configuración para futura conexión (ejemplo con MySQL / MariaDB):
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "oficialia_partes";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC no encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return connection;
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}

