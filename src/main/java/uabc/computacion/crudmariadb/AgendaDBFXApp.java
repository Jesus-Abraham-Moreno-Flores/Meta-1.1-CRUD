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

import java.sql.Connection;

public class AgendaDBFXApp extends Application {

    private AgendaDAO dao;

    private final TableView<Persona> tablaPersonas = new TableView<>();
    private final TextField txtNombre = new TextField();
    private final TextField txtDireccionPer = new TextField();
    private final ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();

    private final TableView<Direccion> tablaDirecciones = new TableView<>();
    private final TextField txtCalle = new TextField();
    private final TextField txtCiudad = new TextField();
    private final ObservableList<Direccion> listaDirecciones = FXCollections.observableArrayList();

    private final TableView<Telefono> tablaTelefonos = new TableView<>();
    private final TextField txtNumeroTel = new TextField();
    private final ObservableList<Telefono> listaTelefonos = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Agenda DB - Aplicación SOLID con JavaFX");

        if (!inicializarConexion()) {
            return; // Se detiene la construcción de la UI si no hay conexión para evitar NullPointerException
        }

        configurarUI(primaryStage);
        cargarPersonas();
    }

    private boolean inicializarConexion() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            this.dao = new AgendaDB(conn);
            return true;
        } catch (Exception e) {
            mostrarAlerta("Error de Conexión", "No se pudo establecer conexión a MariaDB:\n" + e.getMessage());
            return false;
        }
    }

    private void configurarUI(Stage primaryStage) {
        // Sección Personas
        TableColumn<Persona, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Persona, String> colNom = new TableColumn<>("Nombre");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<Persona, String> colDir = new TableColumn<>("Dirección Principal");
        colDir.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        tablaPersonas.getColumns().addAll(colId, colNom, colDir);
        tablaPersonas.setItems(listaPersonas);
        tablaPersonas.setPrefHeight(150);

        tablaPersonas.getSelectionModel().selectedItemProperty().addListener((obs, viejo, sel) -> {
            if (sel != null) {
                txtNombre.setText(sel.getNombre());
                txtDireccionPer.setText(sel.getDireccion());
                cargarDetalles(sel.getId());
            } else {
                listaDirecciones.clear();
                listaTelefonos.clear();
            }
        });

        Button btnAddPer = new Button("Agregar Persona");
        Button btnUpdPer = new Button("Modificar Persona");
        Button btnDelPer = new Button("Eliminar Persona");

        btnAddPer.setOnAction(e -> agregarPersona());
        btnUpdPer.setOnAction(e -> modificarPersona());
        btnDelPer.setOnAction(e -> eliminarPersona());

        GridPane gridPer = new GridPane();
        gridPer.setHgap(10); gridPer.setVgap(5);
        gridPer.add(new Label("Nombre:"), 0, 0); gridPer.add(txtNombre, 1, 0);
        gridPer.add(new Label("Dirección principal:"), 0, 1); gridPer.add(txtDireccionPer, 1, 1);

        VBox boxPersonas = new VBox(8, new Label("=== PERSONAS ==="), gridPer, new HBox(10, btnAddPer, btnUpdPer, btnDelPer), tablaPersonas);

        // Sección Direcciones N:M
        TableColumn<Direccion, String> colCalle = new TableColumn<>("Calle");
        colCalle.setCellValueFactory(new PropertyValueFactory<>("calle"));
        TableColumn<Direccion, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));

        tablaDirecciones.getColumns().addAll(colCalle, colCiudad);
        tablaDirecciones.setItems(listaDirecciones);
        tablaDirecciones.setPrefHeight(120);

        Button btnAddDir = new Button("Agregar Dirección");
        Button btnDelDir = new Button("Eliminar Dirección");
        btnAddDir.setOnAction(e -> agregarDireccion());
        btnDelDir.setOnAction(e -> eliminarDireccion());

        GridPane gridDir = new GridPane();
        gridDir.setHgap(5); gridDir.setVgap(5);
        gridDir.add(new Label("Calle:"), 0, 0); gridDir.add(txtCalle, 1, 0);
        gridDir.add(new Label("Ciudad:"), 0, 1); gridDir.add(txtCiudad, 1, 1);

        VBox boxDirecciones = new VBox(8, new Label("=== DIRECCIONES ASOCIADAS (N:M) ==="), gridDir, new HBox(10, btnAddDir, btnDelDir), tablaDirecciones);

        // Sección Teléfonos
        TableColumn<Telefono, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        tablaTelefonos.getColumns().addAll(colTel);
        tablaTelefonos.setItems(listaTelefonos);
        tablaTelefonos.setPrefHeight(120);

        Button btnAddTel = new Button("Agregar Teléfono");
        Button btnDelTel = new Button("Eliminar Teléfono");
        btnAddTel.setOnAction(e -> agregarTelefono());
        btnDelTel.setOnAction(e -> eliminarTelefono());

        GridPane gridTel = new GridPane();
        gridTel.setHgap(5); gridTel.setVgap(5);
        gridTel.add(new Label("Número:"), 0, 0); gridTel.add(txtNumeroTel, 1, 0);

        VBox boxTelefonos = new VBox(8, new Label("=== TELÉFONOS ==="), gridTel, new HBox(10, btnAddTel, btnDelTel), tablaTelefonos);

        HBox panelInferior = new HBox(15, boxDirecciones, boxTelefonos);
        VBox root = new VBox(15, boxPersonas, panelInferior);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 650, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void cargarPersonas() {
        try {
            listaPersonas.setAll(dao.obtenerPersonas());
        } catch (Exception e) {
            mostrarAlerta("Error al Cargar Personas", e.getMessage());
        }
    }

    private void cargarDetalles(int idPersona) {
        try {
            listaDirecciones.setAll(dao.obtenerDireccionesDePersona(idPersona));
            listaTelefonos.setAll(dao.obtenerTelefonosDePersona(idPersona));
        } catch (Exception e) {
            mostrarAlerta("Error al Cargar Detalles", e.getMessage());
        }
    }

    private void agregarPersona() {
        if (txtNombre.getText().trim().isEmpty()) return;
        try {
            dao.agregarPersona(new Persona(0, txtNombre.getText(), txtDireccionPer.getText()));
            txtNombre.clear(); txtDireccionPer.clear();
            cargarPersonas();
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void modificarPersona() {
        Persona sel = tablaPersonas.getSelectionModel().getSelectedItem();
        if (sel == null || txtNombre.getText().trim().isEmpty()) return;
        try {
            sel.setNombre(txtNombre.getText());
            sel.setDireccion(txtDireccionPer.getText());
            dao.actualizarPersona(sel);
            cargarPersonas();
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void eliminarPersona() {
        Persona sel = tablaPersonas.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            dao.eliminarPersona(sel.getId());
            cargarPersonas();
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void agregarDireccion() {
        Persona sel = tablaPersonas.getSelectionModel().getSelectedItem();
        if (sel == null || txtCalle.getText().isEmpty() || txtCiudad.getText().isEmpty()) return;
        try {
            dao.agregarDireccionYAsociar(sel.getId(), txtCalle.getText(), txtCiudad.getText());
            txtCalle.clear(); txtCiudad.clear();
            cargarDetalles(sel.getId());
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void eliminarDireccion() {
        Direccion dir = tablaDirecciones.getSelectionModel().getSelectedItem();
        Persona per = tablaPersonas.getSelectionModel().getSelectedItem();
        if (dir == null || per == null) return;
        try {
            dao.eliminarDireccion(dir.getId());
            cargarDetalles(per.getId());
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void agregarTelefono() {
        Persona sel = tablaPersonas.getSelectionModel().getSelectedItem();
        if (sel == null || txtNumeroTel.getText().isEmpty()) return;
        try {
            dao.agregarTelefono(sel.getId(), txtNumeroTel.getText());
            txtNumeroTel.clear();
            cargarDetalles(sel.getId());
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void eliminarTelefono() {
        Telefono tel = tablaTelefonos.getSelectionModel().getSelectedItem();
        Persona per = tablaPersonas.getSelectionModel().getSelectedItem();
        if (tel == null || per == null) return;
        try {
            dao.eliminarTelefono(tel.getId());
            cargarDetalles(per.getId());
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}