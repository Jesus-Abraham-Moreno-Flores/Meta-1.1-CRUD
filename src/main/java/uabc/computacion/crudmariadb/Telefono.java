package uabc.computacion.crudmariadb;

public class Telefono extends AgendaDBBase {
    private int personaId;
    private String telefono;

    public Telefono(int id, int personaId, String telefono) {
        super(id);
        this.personaId = personaId;
        this.telefono = telefono;
    }

    public int getPersonaId() { return personaId; }
    public void setPersonaId(int personaId) { this.personaId = personaId; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public String obtenerResumen() {
        return "Teléfono: " + telefono;
    }
}