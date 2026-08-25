package uabc.computacion.crudmariadb;

public abstract class AgendaDBBase {
    protected int id;

    public AgendaDBBase(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public abstract String obtenerResumen();
}