package uabc.computacion.crudmariadb;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgendaDBIntegrationTest {

    private static Connection connection;
    private static AgendaDB agendaDB;

    private static final String USER = "usuario1";
    private static final String PASS = "superpassword";

    @BeforeAll
    static void setup() throws Exception {
        connection = DriverManager.getConnection("jdbc:mariadb://localhost:3307/agenda", USER, PASS);

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS agenda");
            stmt.executeUpdate("USE agenda");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS personas (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre VARCHAR(100) NOT NULL)");
        }

        agendaDB = new AgendaDB(connection);
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Integración: Insertar persona en MariaDB y recuperar ID autogenerado")
    void testInsertarPersonaIntegracion() throws Exception {
        Persona nuevaPersona = new Persona(0, "Persona Prueba JUnit", "Hola 321");

        agendaDB.agregarPersona(nuevaPersona);

        assertTrue(nuevaPersona.getId() > 0, "El ID retornado por MariaDB debe ser mayor a 0");
    }

    @Test
    @Order(2)
    @DisplayName("Integración: Consultar personas en MariaDB")
    void testConsultarPersonasIntegracion() throws Exception {
        List<Persona> personas = agendaDB.obtenerPersonas();

        assertNotNull(personas, "La lista retornada por MariaDB no debe ser nula");
        assertFalse(personas.isEmpty(), "La base de datos debe contener al menos un registro");
        assertTrue(personas.stream().anyMatch(p -> p.getNombre().equals("Persona Prueba JUnit")),
                "El registro insertado previamente debe existir en la BD");
    }
}