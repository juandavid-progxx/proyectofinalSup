package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Servicios.AdminService;
import co.uniquindio.edu.co.Servicios.AutenticacionService;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AdminMainController {

    @FXML private Label lblBienvenida;
    @FXML private Button btnGestionCanciones;
    @FXML private Button btnGestionUsuarios;
    @FXML private Button btnMetricas;
    @FXML private Button btnCerrarSesion;
    @FXML private Label lblTotalCanciones;
    @FXML private Label lblTotalUsuarios;

    private AutenticacionService autenticacionService;
    private AdminService adminService;
    private ViewFactory viewFactory;
    private DataInitializer dataInitializer;

    /**
     * Inicializa el controlador
     */
    @FXML
    public void initialize() {
        this.autenticacionService = new AutenticacionService();
        this.dataInitializer = new DataInitializer();
        dataInitializer.inicializar();
        this.adminService = new AdminService(dataInitializer);
        this.viewFactory = ViewFactory.getInstancia();

        actualizarDatos();
    }

    /**
     * Actualiza los datos mostrados
     */
    private void actualizarDatos() {
        lblBienvenida.setText("Panel de Administración - " +
                autenticacionService.getUsuarioActual().getNombre());

        lblTotalCanciones.setText(String.valueOf(adminService.obtenerTotalCanciones()));
        lblTotalUsuarios.setText(String.valueOf(adminService.obtenerTotalUsuarios()));
    }

    /**
     * Maneja la navegación a gestión de canciones
     */
    @FXML
    private void handleGestionCanciones() {
        viewFactory.mostrarGestionCanciones();
    }

    /**
     * Maneja la navegación a gestión de usuarios
     */
    @FXML
    private void handleGestionUsuarios() {
        viewFactory.mostrarGestionUsuarios();
    }

    /**
     * Maneja la navegación a métricas
     */
    @FXML
    private void handleMetricas() {
        viewFactory.mostrarMetricas();
    }

    /**
     * Maneja el cierre de sesión
     */
    @FXML
    private void handleCerrarSesion() {
        autenticacionService.cerrarSesion();
        viewFactory.mostrarLogin();
    }
}
