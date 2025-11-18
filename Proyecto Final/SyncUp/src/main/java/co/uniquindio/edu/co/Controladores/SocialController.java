package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Servicios.SocialService;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;


public class SocialController {

    @FXML private TextField txtBuscarUsuario;
    @FXML private Button btnBuscar;
    @FXML private Button btnSugerencias;
    @FXML private Button btnVolver;

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, Integer> colFavoritos;
    @FXML private TableColumn<Usuario, Integer> colSeguidos;

    @FXML private Button btnSeguir;
    @FXML private Button btnDejarDeSeguir;

    @FXML private ListView<Usuario> listSeguidos;
    @FXML private Label lblTotalSeguidos;
    @FXML private Label lblTitulo;

    private SocialService socialService;
    private ViewFactory viewFactory;
    private DataInitializer dataInitializer;

    private ObservableList<Usuario> usuariosObservable;
    private ObservableList<Usuario> seguidosObservable;

    /**
     * Inicializa el controlador
     */
    @FXML
    public void initialize() {
        this.dataInitializer = new DataInitializer();
        dataInitializer.inicializar();
        this.socialService = new SocialService(dataInitializer);
        this.viewFactory = ViewFactory.getInstancia();

        configurarTabla();
        configurarListaSeguidos();
        cargarDatos();
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
    }

    /**
     * ⭐ Configura la lista de seguidos (ListView de objetos Usuario)
     */
    private void configurarListaSeguidos() {
        seguidosObservable = FXCollections.observableArrayList();
        listSeguidos.setItems(seguidosObservable);

        // ⭐ Personalizar cómo se muestran los usuarios en la lista
        listSeguidos.setCellFactory(lv -> new ListCell<Usuario>() {
            @Override
            protected void updateItem(Usuario usuario, boolean empty) {
                super.updateItem(usuario, empty);
                if (empty || usuario == null) {
                    setText(null);
                } else {
                    setText(usuario.getNombre() + " (@" + usuario.getUsername() + ")");
                }
            }
        });

        // ⭐ IMPORTANTE: Permitir selección SINGLE
        listSeguidos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // ⭐ NUEVO: Limpiar la selección de la tabla cuando se selecciona en el ListView
        listSeguidos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tableUsuarios.getSelectionModel().clearSelection();
                System.out.println("✓ Usuario seleccionado en lista: " + newVal.getUsername());
            }
        });

        // ⭐ NUEVO: Limpiar la selección del ListView cuando se selecciona en la tabla
        tableUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                listSeguidos.getSelectionModel().clearSelection();
                System.out.println("✓ Usuario seleccionado en tabla: " + newVal.getUsername());
            }
        });

        // ⭐ TEST: Verificar clics en el ListView (para debugging)
        listSeguidos.setOnMouseClicked(event -> {
            Usuario seleccionado = listSeguidos.getSelectionModel().getSelectedItem();
            System.out.println("Click en ListView - Usuario: " +
                    (seleccionado != null ? seleccionado.getUsername() : "null"));
        });
    }

    /**
     * Carga los datos iniciales
     */
    private void cargarDatos() {
        cargarSeguidos();
        handleSugerencias(); // Mostrar sugerencias por defecto
    }

    /**
     * ⭐ Carga la lista de usuarios seguidos (como objetos Usuario)
     */
    private void cargarSeguidos() {
        List<Usuario> seguidos = socialService.obtenerUsuariosSeguidos();
        seguidosObservable.setAll(seguidos);
        lblTotalSeguidos.setText("Total: " + seguidos.size());

        System.out.println("✓ Seguidos cargados: " + seguidos.size());
    }

    /**
     * Maneja la búsqueda de usuarios
     */
    @FXML
    private void handleBuscar() {
        String nombre = txtBuscarUsuario.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("Campo vacío", "Por favor, ingrese un nombre para buscar.", Alert.AlertType.WARNING);
            return;
        }

        List<Usuario> usuarios = socialService.buscarUsuariosParaSeguir(nombre);
        usuariosObservable.setAll(usuarios);

        lblTitulo.setText("Resultados de búsqueda");

        if (usuarios.isEmpty()) {
            mostrarAlerta("Sin resultados", "No se encontraron usuarios con ese nombre.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Maneja las sugerencias de usuarios
     */
    @FXML
    private void handleSugerencias() {
        List<Usuario> sugerencias = socialService.obtenerSugerenciasUsuarios();
        usuariosObservable.setAll(sugerencias);

        lblTitulo.setText("Usuarios sugeridos para seguir");

        if (sugerencias.isEmpty()) {
            mostrarAlerta("Sin sugerencias", "No hay sugerencias disponibles en este momento.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Maneja seguir a un usuario
     */
    @FXML
    private void handleSeguir() {
        Usuario seleccionado = tableUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Seleccione un usuario", "Por favor, seleccione un usuario para seguir.", Alert.AlertType.WARNING);
            return;
        }

        boolean seguido = socialService.seguirUsuario(seleccionado.getUsername());

        if (seguido) {
            mostrarAlerta("Éxito", "Ahora sigues a " + seleccionado.getNombre(), Alert.AlertType.INFORMATION);
            cargarSeguidos();
            handleSugerencias(); // Actualizar sugerencias
        } else {
            mostrarAlerta("Información", "Ya sigues a este usuario o no se pudo completar la acción.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * ⭐ MEJORADO: Maneja dejar de seguir a un usuario (desde la tabla o desde la lista de seguidos)
     */
    @FXML
    private void handleDejarDeSeguir() {
        // ⭐ Primero intentar obtener selección de la lista de seguidos (derecha)
        Usuario seleccionado = listSeguidos.getSelectionModel().getSelectedItem();
        String origen = "lista";

        // Si no hay selección en la lista, intentar de la tabla (izquierda)
        if (seleccionado == null) {
            seleccionado = tableUsuarios.getSelectionModel().getSelectedItem();
            origen = "tabla";
        }

        // ⭐ DEBUG: Imprimir información para verificar
        System.out.println("══════════════════════════════════════");
        System.out.println("Intento de dejar de seguir:");
        System.out.println("Selección desde: " + origen);
        System.out.println("Usuario seleccionado: " + (seleccionado != null ? seleccionado.getUsername() : "null"));
        System.out.println("══════════════════════════════════════");

        if (seleccionado == null) {
            mostrarAlerta("Seleccione un usuario",
                    "Por favor, seleccione un usuario de la lista de seguidos (derecha) o de la tabla para dejar de seguir.",
                    Alert.AlertType.WARNING);
            return;
        }

        if (!socialService.estaSiguiendo(seleccionado.getUsername())) {
            mostrarAlerta("Información", "No sigues a este usuario.", Alert.AlertType.INFORMATION);
            return;
        }

        // ⭐ SOLUCIÓN: Crear variable final para usar en el lambda
        final Usuario usuarioADejarDeSeguir = seleccionado;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Estás seguro de dejar de seguir a " + usuarioADejarDeSeguir.getNombre() + "?");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean dejoDeSeguir = socialService.dejarDeSeguir(usuarioADejarDeSeguir.getUsername());

                if (dejoDeSeguir) {
                    mostrarAlerta("Éxito", "Dejaste de seguir a " + usuarioADejarDeSeguir.getNombre(), Alert.AlertType.INFORMATION);
                    cargarSeguidos();
                    handleSugerencias(); // Actualizar sugerencias

                    // ⭐ Limpiar ambas selecciones
                    listSeguidos.getSelectionModel().clearSelection();
                    tableUsuarios.getSelectionModel().clearSelection();

                    System.out.println("✓ Dejó de seguir exitosamente");
                }
            }
        });
    }

    /**
     * Maneja el evento de volver
     */
    @FXML
    private void handleVolver() {
        viewFactory.mostrarUsuarioMain();
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