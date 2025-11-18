package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Servicios.AutenticacionService;
import co.uniquindio.edu.co.Servicios.ReproductorService;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnRegistro;
    @FXML private Label lblError;

    private AutenticacionService autenticacionService;
    private ViewFactory viewFactory;

    @FXML
    public void initialize() {
        this.autenticacionService = new AutenticacionService();
        this.viewFactory = ViewFactory.getInstancia();

        lblError.setVisible(false);
        lblError.setStyle("-fx-text-fill: red;");

        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        // ⭐ Limpiar audio completamente cuando se carga esta vista (logout)
        // ✅ USAR SINGLETON
        ReproductorService reproductor = ReproductorService.getInstancia();
        reproductor.detener();
        reproductor.limpiar();
        System.out.println("🔄 Limpieza de audio al inicializar LoginController");
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, complete todos los campos.");
            return;
        }

        Usuario usuario = autenticacionService.iniciarSesion(username, password);

        if (usuario != null) {
            lblError.setVisible(false);

            if (autenticacionService.esAdministrador()) {
                viewFactory.mostrarAdminMain();
            } else {
                viewFactory.mostrarUsuarioMain();
            }
        } else {
            mostrarError("Usuario o contraseña incorrectos.");
        }
    }

    @FXML
    private void handleRegistro() {
        viewFactory.mostrarRegistro();
    }

    /**
     * Abre la ventana de login para administradores
     */
    @FXML
    private void handleLoginAdmin() {
        System.out.println("🔐 Abriendo panel de login del administrador...");
        viewFactory.mostrarAdminLogin();
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}