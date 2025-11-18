package co.uniquindio.edu.co.Vista;

import co.uniquindio.edu.co.Controladores.RadioController;
import co.uniquindio.edu.co.Modelo.Cancion;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ViewFactory {

    private static ViewFactory instancia;
    private Stage stage;

    private ViewFactory() {
    }

    /**
     * Obtiene la instancia única del ViewFactory
     */
    public static ViewFactory getInstancia() {
        if (instancia == null) {
            instancia = new ViewFactory();
        }
        return instancia;
    }

    /**
     * Establece el Stage principal
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Muestra la ventana de login
     */
    public void mostrarLogin() {
        cargarVista("/login.fxml", "SyncUp - Iniciar Sesión");
    }

    /**
     * Muestra la ventana de registro
     */
    public void mostrarRegistro() {
        cargarVista("/registro.fxml", "SyncUp - Registro");
    }

    /**
     * Muestra la ventana principal del usuario
     */
    public void mostrarUsuarioMain() {
        cargarVista("/usuario_main.fxml", "SyncUp - Inicio");
    }

    /**
     * Muestra la ventana de perfil
     */
    public void mostrarPerfil() {
        cargarVista("/perfil.fxml", "SyncUp - Mi Perfil");
    }

    /**
     * Muestra la ventana de búsqueda
     */
    public void mostrarBusqueda() {
        cargarVista("/busqueda.fxml", "SyncUp - Búsqueda");
    }

    /**
     * Muestra la ventana de recomendaciones
     */
    public void mostrarRecomendaciones() {
        cargarVista("/recomendaciones.fxml", "SyncUp - Recomendaciones");
    }

    /**
     * Muestra la ventana social
     */
    public void mostrarSocial() {
        cargarVista("/social.fxml", "SyncUp - Red Social");
    }

    /**
     * Muestra la ventana principal del administrador
     */
    public void mostrarAdminMain() {
        cargarVista("/admin_main.fxml", "SyncUp - Panel de Administración");
    }

    /**
     * Muestra la ventana de gestión de canciones
     */
    public void mostrarGestionCanciones() {
        cargarVista("/gestion_canciones.fxml", "SyncUp - Gestión de Canciones");
    }

    /**
     * Muestra la ventana de gestión de usuarios
     */
    public void mostrarGestionUsuarios() {
        cargarVista("/gestion_usuarios.fxml", "SyncUp - Gestión de Usuarios");
    }

    /**
     * ⭐ ACTUALIZADO: Muestra la ventana de radio con canción seleccionada
     */
    public void mostrarRadio(Cancion cancionInicial) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/radio.fxml"));
            Parent root = loader.load();

            // ⭐ Obtener el controlador y pasar la canción
            RadioController radioController = loader.getController();
            radioController.iniciarRadio(cancionInicial);

            Scene scene = new Scene(root);
            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.setTitle("SyncUp - Radio");
            stage.show();

            System.out.println("📻 Radio mostrada con canción: " + cancionInicial.getTitulo());

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de radio: " + e.getMessage());
        }
    }

    /**
     * Versión anterior sin parámetros (para compatibilidad)
     */
    public void mostrarRadio() {
        cargarVista("/radio.fxml", "SyncUp - Radio");
    }

    /**
     * Muestra la ventana de métricas
     */
    public void mostrarMetricas() {
        cargarVista("/metricas.fxml", "SyncUp - Métricas del Sistema");
    }

    /**
     * Carga una vista desde un archivo FXML
     */
    private void cargarVista(String rutaFXML, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // Cargar CSS si existe
            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista: " + rutaFXML);
        }
    }

    /**
     * Carga una vista y retorna su controlador
     */
    public <T> T cargarVistaConControlador(String rutaFXML, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();

            return loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista: " + rutaFXML);
            return null;
        }
    }

    /**
     * Cierra la aplicación
     */
    public void cerrarAplicacion() {
        if (stage != null) {
            stage.close();
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void mostrarAdminLogin() {
        cargarVista("/admin_login.fxml", "SyncUp - Login Administrador");
    }

    public void mostrarExportar() {
        cargarVista("/exportar.fxml", "SyncUp - Exportar Favoritos a CSV");
    }
}