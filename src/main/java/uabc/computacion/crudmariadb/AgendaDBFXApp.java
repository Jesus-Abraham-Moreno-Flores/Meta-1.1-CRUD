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
import java.sql.DriverManager;

public class AgendaDBFXApp extends Application {

    private AgendaDAO dao;

    private TableView<Persona> tablaPersonas = new TableView<>();
    private TextField txtNombre = new TextField();
    private TextField txtDireccionPer = new TextField();
    private ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();

    private TableView<Direccion> tablaDirecciones = new TableView<>();
    private TextField txtCalle = new TextField();
    private TextField txtCiudad = new TextField();
    private ObservableList<Direccion> listaDirecciones = FXCollections.observableArrayList();

    private TableView<Telefono> tablaTelefonos = new TableView<>();
    private TextField txtNumeroTel = new TextField();
    private ObservableList<Telefono> listaTelefonos = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Agenda DB - MariaDB CRUD");
        inicializarConexion();

        TableColumn<Persona, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Persona, String> colNom = new TableColumn<>("Nombre");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<Persona, String> colDir = new TableColumn<>("Dirección General");
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

        VBox boxDirecciones = new VBox(8, new Label("=== DIRECCIONES ASOCIADAS ==="), gridDir, new HBox(10, btnAddDir, btnDelDir), tablaDirecciones);

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

        cargarPersonas();

        Scene scene = new Scene(root, 650, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void inicializarConexion() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3307/agenda", "usuario1", "superpassword");
            dao = new AgendaDB(conn);
        } catch (Exception e) {
            mostrarAlerta("Error de Conexión", e.getMessage());
        }
    }

    private void cargarPersonas() {
        try {
            listaPersonas.setAll(dao.obtenerPersonas());
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void cargarDetalles(int idPersona) {
        try {
            listaDirecciones.setAll(dao.obtenerDireccionesDePersona(idPersona));
            listaTelefonos.setAll(dao.obtenerTelefonosDePersona(idPersona));
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
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