package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Configuracion.AppConfig;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Servicios.AutenticacionService;
import co.uniquindio.edu.co.Servicios.UsuarioService;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.LinkedList;


public class PerfilController {

    @FXML
    private Label lblUsername;
    @FXML private TextField txtNombre;
    @FXML private PasswordField txtPasswordActual;
    @FXML private PasswordField txtPasswordNueva;
    @FXML private PasswordField txtConfirmarPassword;
    @FXML private Button btnActualizar;
    @FXML private Button btnExportar;
    @FXML private Button btnVolver;
    @FXML private Label lblTotalFavoritos;
    @FXML private Label lblTotalSeguidos;
    @FXML private Label lblMensaje;

    private AutenticacionService autenticacionService;
    private UsuarioService usuarioService;
    private ViewFactory viewFactory;

    /**
     * Inicializa el controlador
     */
    @FXML
    public void initialize() {
        this.autenticacionService = new AutenticacionService();
        this.usuarioService = new UsuarioService();
        this.viewFactory = ViewFactory.getInstancia();

        lblMensaje.setVisible(false);
        cargarDatosUsuario();
    }

    /**
     * Carga los datos del usuario actual
     */
    private void cargarDatosUsuario() {
        Usuario usuario = autenticacionService.getUsuarioActual();

        if (usuario != null) {
            lblUsername.setText("@" + usuario.getUsername());
            txtNombre.setText(usuario.getNombre());
            lblTotalFavoritos.setText(String.valueOf(usuario.getCantidadFavoritos()));
            lblTotalSeguidos.setText(String.valueOf(usuario.getCantidadSeguidos()));
        }
    }

    /**
     * Maneja la actualización del perfil
     */
    @FXML
    private void handleActualizar() {
        String nombre = txtNombre.getText().trim();
        String passwordActual = txtPasswordActual.getText();
        String passwordNueva = txtPasswordNueva.getText();
        String confirmarPassword = txtConfirmarPassword.getText();

        if (nombre.isEmpty()) {
            mostrarError("El nombre no puede estar vacío.");
            return;
        }

        // Si se quiere cambiar la contraseña
        if (!passwordNueva.isEmpty() || !confirmarPassword.isEmpty()) {
            if (passwordActual.isEmpty()) {
                mostrarError("Debe ingresar la contraseña actual para cambiarla.");
                return;
            }

            if (!passwordNueva.equals(confirmarPassword)) {
                mostrarError("Las contraseñas nuevas no coinciden.");
                return;
            }

            try {
                boolean cambioPassword = autenticacionService.cambiarPassword(passwordActual, passwordNueva);

                if (!cambioPassword) {
                    mostrarError("No se pudo cambiar la contraseña.");
                    return;
                }
            } catch (IllegalArgumentException e) {
                mostrarError(e.getMessage());
                return;
            }
        }

        // Actualizar nombre
        boolean actualizado = usuarioService.actualizarPerfil(nombre, null);

        if (actualizado) {
            mostrarExito("Perfil actualizado exitosamente.");
            limpiarCamposPassword();
            cargarDatosUsuario();
        } else {
            mostrarError("No se pudo actualizar el perfil.");
        }
    }

    /**
     * Maneja la exportación de favoritos
     */
    @FXML
    private void handleExportar() {
        Usuario usuario = autenticacionService.getUsuarioActual();

        if (usuario == null) {
            mostrarError("No hay usuario en sesión.");
            return;
        }

        LinkedList<Cancion> favoritos = usuario.getListaFavoritos();

        if (favoritos.isEmpty()) {
            mostrarAlerta("Sin favoritos", "No tiene canciones favoritas para exportar.", Alert.AlertType.INFORMATION);
            return;
        }

        // Abrir diálogo para seleccionar ubicación
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Favoritos");
        fileChooser.setInitialDirectory(new File(AppConfig.DIRECTORIO_EXPORTACION_DEFAULT));
        fileChooser.setInitialFileName("favoritos_" + usuario.getUsername() + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo CSV", "*.csv")
        );

        File archivo = fileChooser.showSaveDialog(viewFactory.getStage());

        if (archivo != null) {
            boolean exportado = usuarioService.exportarFavoritos(archivo.getAbsolutePath());

            if (exportado) {
                mostrarAlerta("Éxito", "Favoritos exportados correctamente a:\n" + archivo.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } else {
                mostrarError("Error al exportar favoritos.");
            }
        }
    }

    /**
     * Maneja el evento de volver
     */
    @FXML
    private void handleVolver() {
        viewFactory.mostrarUsuarioMain();
    }

    /**
     * Limpia los campos de contraseña
     */
    private void limpiarCamposPassword() {
        txtPasswordActual.clear();
        txtPasswordNueva.clear();
        txtConfirmarPassword.clear();
    }

    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-text-fill: red;");
        lblMensaje.setVisible(true);
    }

    /**
     * Muestra un mensaje de éxito
     */
    private void mostrarExito(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-text-fill: green;");
        lblMensaje.setVisible(true);
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
