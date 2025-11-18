package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Servicios.AutenticacionService;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class AdminLoginController {

    @FXML
    private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnAtras;
    @FXML private Label lblError;

    private AutenticacionService autenticacionService;
    private ViewFactory viewFactory;

    @FXML
    public void initialize() {
        this.autenticacionService = new AutenticacionService();
        this.viewFactory = ViewFactory.getInstancia();

        lblError.setVisible(false);
        lblError.setStyle("-fx-text-fill: red;");

        // Permitir login con Enter
        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        // Label informativo
        System.out.println("🔐 Ventana de Login del Administrador cargada");
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, complete todos los campos.");
            return;
        }

        // Validar credenciales de administrador
        Usuario usuario = autenticacionService.iniciarSesion(username, password);

        if (usuario != null && autenticacionService.esAdministrador()) {
            lblError.setVisible(false);
            System.out.println("✅ Administrador autenticado: " + username);
            viewFactory.mostrarAdminMain();
        } else {
            mostrarError("Acceso denegado. Solo administradores pueden iniciar sesión aquí.");
            System.out.println("❌ Intento de acceso fallido desde login admin");
        }
    }

    @FXML
    private void handleAtras() {
        System.out.println("🔙 Volviendo al login principal...");
        viewFactory.mostrarLogin();
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}
