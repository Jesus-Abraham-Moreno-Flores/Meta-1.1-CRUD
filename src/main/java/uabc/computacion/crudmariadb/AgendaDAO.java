package uabc.computacion.crudmariadb;

import java.util.List;

public interface AgendaDAO {
    List<Persona> obtenerPersonas() throws Exception;
    void agregarPersona(Persona persona) throws Exception;
    void actualizarPersona(Persona persona) throws Exception;
    void eliminarPersona(int idPersona) throws Exception;

    List<Direccion> obtenerDireccionesDePersona(int idPersona) throws Exception;
    void agregarDireccionYAsociar(int idPersona, String calle, String ciudad) throws Exception;
    void eliminarDireccion(int idDireccion) throws Exception;

    List<Telefono> obtenerTelefonosDePersona(int idPersona) throws Exception;
    void agregarTelefono(int personaId, String numeroTelefono) throws Exception;
    void eliminarTelefono(int idTelefono) throws Exception;
}