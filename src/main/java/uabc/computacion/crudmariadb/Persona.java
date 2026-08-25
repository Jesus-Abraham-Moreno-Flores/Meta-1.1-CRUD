package uabc.computacion.crudmariadb;

import java.util.ArrayList;
import java.util.List;

public class Persona extends AgendaDBBase {
    private String nombre;
    private String direccion; // Columna presente en la tabla personas
    private List<Direccion> direcciones = new ArrayList<>();
    private List<Telefono> telefonos = new ArrayList<>();

    public Persona(int id, String nombre, String direccion) {
        super(id);
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public List<Direccion> getDirecciones() { return direcciones; }
    public List<Telefono> getTelefonos() { return telefonos; }

    @Override
    public String obtenerResumen() {
        return "Persona #" + id + ": " + nombre;
    }
}