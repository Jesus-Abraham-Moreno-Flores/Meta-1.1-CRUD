package uabc.computacion.crudmariadb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AgendaDB implements AgendaDAO {
    private final Connection connection;

    public AgendaDB(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Persona> obtenerPersonas() throws SQLException {
        List<Persona> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, direccion FROM personas";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Persona(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("direccion")
                ));
            }
        }
        return lista;
    }

    @Override
    public void agregarPersona(Persona persona) throws SQLException {
        String sql = "INSERT INTO personas (nombre, direccion) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getDireccion() != null ? persona.getDireccion() : "");
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) persona.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public void actualizarPersona(Persona persona) throws SQLException {
        String sql = "UPDATE personas SET nombre = ?, direccion = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getDireccion());
            stmt.setInt(3, persona.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminarPersona(int idPersona) throws SQLException {
        String sql = "DELETE FROM personas WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPersona);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Direccion> obtenerDireccionesDePersona(int idPersona) throws SQLException {
        List<Direccion> lista = new ArrayList<>();
        String sql = "SELECT d.id, d.calle, d.ciudad FROM direcciones d " +
                "JOIN persona_direccion pd ON d.id = pd.id_direccion " +
                "WHERE pd.id_persona = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPersona);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Direccion(
                            rs.getInt("id"),
                            rs.getString("calle"),
                            rs.getString("ciudad")
                    ));
                }
            }
        }
        return lista;
    }

    @Override
    public void agregarDireccionYAsociar(int idPersona, String calle, String ciudad) throws SQLException {
        String sqlDir = "INSERT INTO direcciones (calle, ciudad) VALUES (?, ?)";
        try (PreparedStatement stmtDir = connection.prepareStatement(sqlDir, Statement.RETURN_GENERATED_KEYS)) {
            stmtDir.setString(1, calle);
            stmtDir.setString(2, ciudad);
            stmtDir.executeUpdate();

            int idDireccion = -1;
            try (ResultSet rs = stmtDir.getGeneratedKeys()) {
                if (rs.next()) idDireccion = rs.getInt(1);
            }

            if (idDireccion != -1) {
                String sqlRel = "INSERT INTO persona_direccion (id_persona, id_direccion) VALUES (?, ?)";
                try (PreparedStatement stmtRel = connection.prepareStatement(sqlRel)) {
                    stmtRel.setInt(1, idPersona);
                    stmtRel.setInt(2, idDireccion);
                    stmtRel.executeUpdate();
                }
            }
        }
    }

    @Override
    public void eliminarDireccion(int idDireccion) throws SQLException {
        String sql = "DELETE FROM direcciones WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idDireccion);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Telefono> obtenerTelefonosDePersona(int idPersona) throws SQLException {
        List<Telefono> lista = new ArrayList<>();
        String sql = "SELECT id, personaId, telefono FROM telefonos WHERE personaId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPersona);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Telefono(
                            rs.getInt("id"),
                            rs.getInt("personaId"),
                            rs.getString("telefono")
                    ));
                }
            }
        }
        return lista;
    }

    @Override
    public void agregarTelefono(int personaId, String numeroTelefono) throws SQLException {
        String sql = "INSERT INTO telefonos (personaId, telefono) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, personaId);
            stmt.setString(2, numeroTelefono);
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminarTelefono(int idTelefono) throws SQLException {
        String sql = "DELETE FROM telefonos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idTelefono);
            stmt.executeUpdate();
        }
    }
}