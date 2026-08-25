package uabc.computacion.crudmariadb;

public class Direccion extends AgendaDBBase {
    private String calle;
    private String ciudad;

    public Direccion(int id, String calle, String ciudad) {
        super(id);
        this.calle = calle;
        this.ciudad = ciudad;
    }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    @Override
    public String obtenerResumen() {
        return calle + ", " + ciudad;
    }
}