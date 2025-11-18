package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Servicios.AdminService;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class GestionUsuariosController {

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML
    private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, Integer> colFavoritos;
    @FXML private TableColumn<Usuario, Integer> colSeguidos;

    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver;
    @FXML private Button btnRefrescar;

    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblDetalles;

    private AdminService adminService;
    private ViewFactory viewFactory;
    private DataInitializer dataInitializer;

    private ObservableList<Usuario> usuariosObservable;

    /**
     * Inicializa el controlador
     */
    @FXML
    public void initialize() {
        this.dataInitializer = new DataInitializer();
        dataInitializer.inicializar();
        this.adminService = new AdminService(dataInitializer);
        this.viewFactory = ViewFactory.getInstancia();

        configurarTabla();
        cargarUsuarios();
    }

    /**
     * Configura la tabla de usuarios
     */
    private void configurarTabla() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colFavoritos.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCantidadFavoritos()).asObject());
        colSeguidos.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCantidadSeguidos()).asObject());

        usuariosObservable = FXCollections.observableArrayList();
        tableUsuarios.setItems(usuariosObservable);

        // Listener para selección
        tableUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mostrarDetallesUsuario(newSelection);
            }
        });
    }

    /**
     * Carga todos los usuarios
     */
    private void cargarUsuarios() {
        try {
            List<Usuario> usuarios = adminService.obtenerUsuariosRegulares();
            usuariosObservable.setAll(usuarios);
            lblTotalUsuarios.setText("Total de usuarios: " + usuarios.size());
            lblDetalles.setText("Seleccione un usuario para ver detalles");
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar usuarios: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Muestra los detalles de un usuario
     */
    private void mostrarDetallesUsuario(Usuario usuario) {
        StringBuilder detalles = new StringBuilder();
        detalles.append("Usuario: ").append(usuario.getUsername()).append("\n");
        detalles.append("Nombre: ").append(usuario.getNombre()).append("\n");
        detalles.append("Canciones favoritas: ").append(usuario.getCantidadFavoritos()).append("\n");
        detalles.append("Usuarios seguidos: ").append(usuario.getCantidadSeguidos());

        lblDetalles.setText(detalles.toString());
    }

    /**
     * Maneja la búsqueda de usuarios
     */
    @FXML
    private void handleBuscar() {
        String nombre = txtBuscar.getText().trim();

        if (nombre.isEmpty()) {
            cargarUsuarios();
            return;
        }

        try {
            List<Usuario> resultados = adminService.buscarUsuarios(nombre);
            usuariosObservable.setAll(resultados);
            lblTotalUsuarios.setText("Resultados: " + resultados.size());

            if (resultados.isEmpty()) {
                mostrarAlerta("Sin resultados", "No se encontraron usuarios con ese nombre.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al buscar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Maneja la eliminación de un usuario
     */
    @FXML
    private void handleEliminar() {
        Usuario seleccionado = tableUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Seleccione un usuario", "Por favor, seleccione un usuario para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        if (seleccionado.getUsername().equals("admin")) {
            mostrarAlerta("Acción no permitida", "No se puede eliminar al administrador principal.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Está seguro de eliminar al usuario '" + seleccionado.getNombre() + "' (@" + seleccionado.getUsername() + ")?\n\nEsta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean eliminado = adminService.eliminarUsuario(seleccionado.getUsername());

                    if (eliminado) {
                        cargarUsuarios();
                        mostrarAlerta("Éxito", "Usuario eliminado exitosamente.", Alert.AlertType.INFORMATION);
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el usuario.", Alert.AlertType.ERROR);
                    }
                } catch (Exception e) {
                    mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    /**
     * Maneja el evento de refrescar
     */
    @FXML
    private void handleRefrescar() {
        txtBuscar.clear();
        cargarUsuarios();
    }

    /**
     * Maneja el evento de volver
     */
    @FXML
    private void handleVolver() {
        viewFactory.mostrarAdminMain();
    }

    /**
     * Muestra una alerta
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
