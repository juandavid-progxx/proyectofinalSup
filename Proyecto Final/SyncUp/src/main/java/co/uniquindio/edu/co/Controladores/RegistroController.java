package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Servicios.AutenticacionService;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class RegistroController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtNombre;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmarPassword;
    @FXML private Button btnRegistrar;
    @FXML private Button btnVolver;
    @FXML private Label lblError;
    @FXML private Label lblExito;

    private AutenticacionService autenticacionService;
    private ViewFactory viewFactory;

    /**
     * Inicializa el controlador
     */
    @FXML
    public void initialize() {
        this.autenticacionService = new AutenticacionService();
        this.viewFactory = ViewFactory.getInstancia();

        lblError.setVisible(false);
        lblExito.setVisible(false);
        lblError.setStyle("-fx-text-fill: red;");
        lblExito.setStyle("-fx-text-fill: green;");
    }

    /**
     * Maneja el evento de registro
     */
    @FXML
    private void handleRegistrar() {
        String username = txtUsername.getText().trim();
        String nombre = txtNombre.getText().trim();
        String password = txtPassword.getText();
        String confirmarPassword = txtConfirmarPassword.getText();

        // Validaciones
        if (username.isEmpty() || nombre.isEmpty() || password.isEmpty() || confirmarPassword.isEmpty()) {
            mostrarError("Por favor, complete todos los campos.");
            return;
        }

        if (!password.equals(confirmarPassword)) {
            mostrarError("Las contraseñas no coinciden.");
            return;
        }

        try {
            boolean registrado = autenticacionService.registrarUsuario(username, password, nombre);

            if (registrado) {
                mostrarExito("¡Usuario registrado exitosamente!");

                // Limpiar campos
                limpiarCampos();

                // Esperar 2 segundos y volver al login
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> viewFactory.mostrarLogin());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    /**
     * Maneja el evento de volver al login
     */
    @FXML
    private void handleVolver() {
        viewFactory.mostrarLogin();
    }

    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblExito.setVisible(false);
    }

    /**
     * Muestra un mensaje de éxito
     */
    private void mostrarExito(String mensaje) {
        lblExito.setText(mensaje);
        lblExito.setVisible(true);
        lblError.setVisible(false);
    }

    /**
     * Limpia los campos del formulario
     */
    private void limpiarCampos() {
        txtUsername.clear();
        txtNombre.clear();
        txtPassword.clear();
        txtConfirmarPassword.clear();
    }
}
