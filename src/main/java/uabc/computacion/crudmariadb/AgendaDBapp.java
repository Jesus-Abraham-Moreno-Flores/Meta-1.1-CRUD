package uabc.computacion.crudmariadb;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class AgendaDBapp extends Application {

    private static final String URL = "jdbc:mariadb://localhost:3307/agenda";
    private static final String USER = "usuario1";
    private static final String PASS = "superpassword";

    private TableView<Persona> tablaPersonas = new TableView<>();
    private TextField txtNombre = new TextField();
    private TextField txtDireccion = new TextField();
    private ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();

    private TableView<Telefono> tablaTelefonos = new TableView<>();
    private TextField txtNumeroTel = new TextField();
    private ObservableList<Telefono> listaTelefonos = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CRUD Personas y Teléfonos");

        TableColumn<Persona, Integer> colPerId = new TableColumn<>("ID");
        colPerId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Persona, String> colPerNombre = new TableColumn<>("Nombre");
        colPerNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Persona, String> colPerDir = new TableColumn<>("Dirección");
        colPerDir.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        tablaPersonas.getColumns().addAll(colPerId, colPerNombre, colPerDir);
        tablaPersonas.setItems(listaPersonas);
        tablaPersonas.setPrefHeight(180);

        tablaPersonas.getSelectionModel().selectedItemProperty().addListener((obs, viejo, personaSeleccionada) -> {
            if (personaSeleccionada != null) {
                txtNombre.setText(personaSeleccionada.getNombre());
                txtDireccion.setText(personaSeleccionada.getDireccion());
                cargarTelefonos(personaSeleccionada.getId());
            } else {
                listaTelefonos.clear();
            }
        });

        GridPane gridPersona = new GridPane();
        gridPersona.setHgap(10); gridPersona.setVgap(8);
        gridPersona.add(new Label("Nombre:"), 0, 0); gridPersona.add(txtNombre, 1, 0);
        gridPersona.add(new Label("Dirección:"), 0, 1); gridPersona.add(txtDireccion, 1, 1);

        Button btnAddPer = new Button("Agregar Persona");
        Button btnUpdPer = new Button("Modificar Persona");
        Button btnDelPer = new Button("Eliminar Persona");

        btnAddPer.setOnAction(e -> agregarPersona());
        btnUpdPer.setOnAction(e -> actualizarPersona());
        btnDelPer.setOnAction(e -> eliminarPersona());

        HBox btnsPersona = new HBox(10, btnAddPer, btnUpdPer, btnDelPer);

        VBox boxPersonas = new VBox(10, new Label("=== PERSONAS ==="), gridPersona, btnsPersona, tablaPersonas);
        boxPersonas.setPadding(new Insets(10));
        boxPersonas.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");

        TableColumn<Telefono, Integer> colTelId = new TableColumn<>("ID Tel");
        colTelId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Telefono, String> colTelNum = new TableColumn<>("Número de Teléfono");
        colTelNum.setCellValueFactory(new PropertyValueFactory<>("numero"));

        tablaTelefonos.getColumns().addAll(colTelId, colTelNum);
        tablaTelefonos.setItems(listaTelefonos);
        tablaTelefonos.setPrefHeight(150);

        tablaTelefonos.getSelectionModel().selectedItemProperty().addListener((obs, viejo, telSeleccionado) -> {
            if (telSeleccionado != null) {
                txtNumeroTel.setText(telSeleccionado.getNumero());
            }
        });

        GridPane gridTel = new GridPane();
        gridTel.setHgap(10); gridTel.setVgap(8);
        gridTel.add(new Label("Número Teléfono:"), 0, 0); gridTel.add(txtNumeroTel, 1, 0);

        Button btnAddTel = new Button("Agregar Teléfono");
        Button btnUpdTel = new Button("Modificar Teléfono");
        Button btnDelTel = new Button("Eliminar Teléfono");

        btnAddTel.setOnAction(e -> agregarTelefono());
        btnUpdTel.setOnAction(e -> actualizarTelefono());
        btnDelTel.setOnAction(e -> eliminarTelefono());

        HBox btnsTel = new HBox(10, btnAddTel, btnUpdTel, btnDelTel);

        VBox boxTelefonos = new VBox(10, new Label("=== TELÉFONOS DE LA PERSONA ==="), gridTel, btnsTel, tablaTelefonos);
        boxTelefonos.setPadding(new Insets(10));
        boxTelefonos.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");

        VBox root = new VBox(15, boxPersonas, boxTelefonos);
        root.setPadding(new Insets(15));

        cargarPersonas();

        Scene scene = new Scene(root, 600, 680);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public void cargarPersonas() {
        listaPersonas.clear();
        String sql = "SELECT * FROM Personas";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                listaPersonas.add(new Persona(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("direccion")
                ));
            }
        } catch (SQLException ex) {
            mostrarAlerta("Error", "Error al cargar personas: " + ex.getMessage());
        }
    }

    public void agregarPersona() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Advertencia", "Escribe un nombre para la persona.");
            return;
        }
        String sql = "INSERT INTO Personas (nombre, direccion) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, txtNombre.getText());
            stmt.setString(2, txtDireccion.getText());
            stmt.executeUpdate();

            limpiarCamposPersona();
            cargarPersonas();
        } catch (SQLException ex) {
            mostrarAlerta("Error", "Error al agregar persona: " + ex.getMessage());
        }
    }

    public void actualizarPersona() {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Atención", "Selecciona una persona de la tabla para actualizar.");
            return;
        }

        String sql = "UPDATE Personas SET nombre = ?, direccion = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, txtNombre.getText());
            stmt.setString(2, txtDireccion.getText());
            stmt.setInt(3, seleccionada.getId());
            stmt.executeUpdate();

            limpiarCamposPersona();
            cargarPersonas();
        } catch (SQLException ex) {
            mostrarAlerta("Error", "Error al actualizar persona: " + ex.getMessage());
        }
    }

    public void eliminarPersona() {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Atención", "Selecciona una persona para eliminar.");
            return;
        }

        try (Connection conn = getConnection()) {
            // 1. Eliminar telefonos
            try (PreparedStatement stmtTel = conn.prepareStatement("DELETE FROM Telefonos WHERE personaId = ?")) {
                stmtTel.setInt(1, seleccionada.getId());
                stmtTel.executeUpdate();
            }
            // 2. Eliminar la persona
            try (PreparedStatement stmtPer = conn.prepareStatement("DELETE FROM Personas WHERE id = ?")) {
                stmtPer.setInt(1, seleccionada.getId());
                stmtPer.executeUpdate();
            }

            limpiarCamposPersona();
            cargarPersonas();
        } catch (SQLException ex) {
            mostrarAlerta("Error", "Error al eliminar persona: " + ex.getMessage());
        }
    }

    public void cargarTelefonos(int idPersona) {
        listaTelefonos.clear();
        String sql = "SELECT * FROM Telefonos WHERE personaId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPersona);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    listaTelefonos.add(new Telefono(
                            rs.getInt("id"),
                            rs.getInt("personaId"),
                            rs.getString("telefono")
                    ));
                }
            }
        } catch (SQLException ex) {
            mostrarAlerta("Error", "Error al cargar teléfonos: " + ex.getMessage());
        }
    }

    public void agregarTelefono() {
        Persona personaSel = tablaPersonas.getSelectionModel().getSelectedItem();
        if (personaSel == null) {
            mostrarAlerta("Atención", "Debes seleccionar primero una persona para asignarle un teléfono.");
            return;
        }

        if (txtNumeroTel.getText().trim().isEmpty()) {
            mostrarAlerta("Advertencia", "Escribe un número de teléfono.");
            return;
        }

        String sql = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, personaSel.getId());
            stmt.setString(2, txtNumeroTel.getText());
            stmt.executeUpdate();

            txtNumeroTel.clear();
            cargarTelefonos(personaSel.getId());
        } catch (SQLException ex) {
            mostrarAlerta("Error", "Error al agregar teléfono: " + ex.getMessage());
        }
    }

    public void actualizarTelefono() {
        Telefono telSel = tablaTelefonos.getSelectionModel().getSelectedItem();
        Persona personaSel = tablaPersonas.getSelectionModel().getSelectedItem();

        if (telSel == null) {
            mostrarAlerta("Atención", "Selecciona un teléfono de la tabla inferior para actualizar.");
            return;
        }

        String sql = "UPDATE Telefonos SET telefono = ? WHERE personaId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, txtNumeroTel.getText());
            stmt.setInt(2, telSel.getId());
            stmt.executeUpdate();

            txtNumeroTel.clear();
            cargarTelefonos(personaSel.getId());
        } catch (SQLException ex) {
            mostrarAlerta("Error", "Error al actualizar teléfono: " + ex.getMessage());
        }
    }

    public void eliminarTelefono() {
        Telefono telSel = tablaTelefonos.getSelectionModel().getSelectedItem();
        Persona personaSel = tablaPersonas.getSelectionModel().getSelectedItem();

        if (telSel == null) {
            mostrarAlerta("Atención", "Selecciona un teléfono de la tabla inferior para eliminar.");
            return;
        }

        String sql = "DELETE FROM Telefonos WHERE personaId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, telSel.getId());
            stmt.executeUpdate();

            txtNumeroTel.clear();
            cargarTelefonos(personaSel.getId());
        } catch (SQLException ex) {
            mostrarAlerta("Error", "Error al eliminar teléfono: " + ex.getMessage());
        }
    }

    public void limpiarCamposPersona() {
        txtNombre.clear();
        txtDireccion.clear();
        txtNumeroTel.clear();
        tablaPersonas.getSelectionModel().clearSelection();
        listaTelefonos.clear();
    }

    public void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static class Persona {
        private final int id;
        private final String nombre;
        private final String direccion;

        public Persona(int id, String nombre, String direccion) {
            this.id = id;
            this.nombre = nombre;
            this.direccion = direccion;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getDireccion() { return direccion; }
    }

    public static class Telefono {
        private final int id;
        private final int idPersona;
        private final String numero;

        public Telefono(int id, int idPersona, String numero) {
            this.id = id;
            this.idPersona = idPersona;
            this.numero = numero;
        }

        public int getId() { return id; }
        public int getIdPersona() { return idPersona; }
        public String getNumero() { return numero; }
    }
}