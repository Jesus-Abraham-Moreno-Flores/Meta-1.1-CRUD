package uabc.computacion.crudmariadb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AgendaDBUnitTest {

    private Persona persona;

    @BeforeEach
    void setUp() {
        persona = new Persona(1, "Carlos Lopez", "Calle Falsa 123");
    }

    @Test
    void testEncapsulamientoPersona() {
        assertEquals("Carlos Lopez", persona.getNombre());
        persona.setNombre("Carlos A. Lopez");
        assertEquals("Carlos A. Lopez", persona.getNombre());
    }

    @Test
    void testRelacionNMVariasDireccionesEnMemoria() {
        Direccion dir1 = new Direccion(10, "Calle Primera", "Mexicali");
        Direccion dir2 = new Direccion(11, "Calle Segunda", "Ensenada");

        persona.getDirecciones().add(dir1);
        persona.getDirecciones().add(dir2);

        assertEquals(2, persona.getDirecciones().size());
        assertEquals("Mexicali", persona.getDirecciones().get(0).getCiudad());
    }

    @Test
    void testPolimorfismoResumen() {
        assertTrue(persona.obtenerResumen().contains("Carlos Lopez"));
    }
}