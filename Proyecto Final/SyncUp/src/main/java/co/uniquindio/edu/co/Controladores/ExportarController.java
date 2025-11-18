package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Servicios.UsuarioService;
import co.uniquindio.edu.co.Utils.CSVExporter;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

/**
 * Controlador para exportación de datos a CSV
 * RF-009: Descargar un reporte de sus canciones favoritas en formato CSV
 */
public class ExportarController {

    @FXML private Label lblUsuario;
    @FXML private Label lblTotalFavoritos;
    @FXML private Label lblEstado;

    @FXML private TableView<Cancion> tableFavoritos;
    @FXML private TableColumn<Cancion, String> colTitulo;
    @FXML private TableColumn<Cancion, String> colArtista;
    @FXML private TableColumn<Cancion, String> colGenero;
    @FXML private TableColumn<Cancion, Integer> colAño;
    @FXML private TableColumn<Cancion, String> colDuracion;

    @FXML private Button btnSeleccionarRuta;
    @FXML private TextField txtRuta;
    @FXML private Button btnExportar;
    @FXML private Button btnVolver;

    @FXML private TextArea txtPreview;
    @FXML private CheckBox chkAbrirArchivo;

    private UsuarioService usuarioService;
    private ViewFactory viewFactory;
    private Usuario usuarioActual;
    private LinkedList<Cancion> favoritos;
    private String rutaSeleccionada;

    /**
     * Inicializa el controlador
     * RF-009: Exportar favoritos a CSV
     */
    @FXML
    public void initialize() {
        this.usuarioService = new UsuarioService();
        this.viewFactory = ViewFactory.getInstancia();

        cargarDatos();
        configurarTabla();
        generarPreview();

        System.out.println("✅ ExportarController inicializado (RF-009)");
    }

    /**
     * Carga datos del usuario actual
     */
    private void cargarDatos() {
        usuarioActual = usuarioService.obtenerUsuarioActual();

        if (usuarioActual == null) {
            lblEstado.setText("❌ Error: No hay usuario logueado");
            lblEstado.setStyle("-fx-text-fill: #e74c3c;");
            btnExportar.setDisable(true);
            return;
        }

        favoritos = usuarioService.obtenerFavoritos();

        // Actualizar labels
        lblUsuario.setText("👤 " + usuarioActual.getNombre() + " (@" + usuarioActual.getUsername() + ")");
        lblTotalFavoritos.setText("📊 Total de canciones favoritas: " + favoritos.size());

        if (favoritos.isEmpty()) {
            lblEstado.setText("⚠️ No tienes canciones favoritas para exportar");
            lblEstado.setStyle("-fx-text-fill: #f39c12;");
            btnExportar.setDisable(true);
        } else {
            lblEstado.setText("✅ Listo para exportar");
            lblEstado.setStyle("-fx-text-fill: #27ae60;");
        }

        System.out.println("📂 Datos cargados: " + favoritos.size() + " favoritos");
    }

    /**
     * Configura la tabla de favoritos
     */
    private void configurarTabla() {
        colTitulo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitulo()));
        colArtista.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getArtista()));
        colGenero.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGenero().getNombre()));
        colAño.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getAño()).asObject());
        colDuracion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDuracionFormateada()));

        if (favoritos != null) {
            tableFavoritos.setItems(javafx.collections.FXCollections.observableArrayList(favoritos));
        }
    }

    /**
     * Genera preview del CSV
     */
    private void generarPreview() {
        if (favoritos == null || favoritos.isEmpty()) {
            txtPreview.setText("Sin canciones para previsualizar");
            return;
        }

        StringBuilder preview = new StringBuilder();

        // Encabezados
        preview.append("Usuario: ").append(usuarioActual.getNombre())
                .append(" (@").append(usuarioActual.getUsername()).append(")\n");
        preview.append("Total de favoritos: ").append(favoritos.size()).append("\n");
        preview.append("Fecha de exportación: ").append(obtenerFechaActual()).append("\n\n");

        preview.append("ID,Título,Artista,Género,Año,Duración\n");

        // Primeras 10 canciones
        int limite = Math.min(10, favoritos.size());
        for (int i = 0; i < limite; i++) {
            Cancion cancion = favoritos.get(i);
            preview.append(cancion.getId()).append(",");
            preview.append("\"").append(cancion.getTitulo()).append("\",");
            preview.append("\"").append(cancion.getArtista()).append("\",");
            preview.append(cancion.getGenero().getNombre()).append(",");
            preview.append(cancion.getAño()).append(",");
            preview.append(cancion.getDuracionFormateada()).append("\n");
        }

        if (favoritos.size() > 10) {
            preview.append("\n... y ").append(favoritos.size() - 10).append(" canciones más\n");
        }

        txtPreview.setText(preview.toString());
    }

    /**
     * Maneja selección de ruta de archivo
     */
    @FXML
    private void handleSeleccionarRuta() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar reporte de favoritos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );

        // Ruta por defecto: Desktop
        String carpetaDefault = System.getProperty("user.home") + "/Desktop";
        File carpetaInicial = new File(carpetaDefault);
        if (carpetaInicial.exists()) {
            fileChooser.setInitialDirectory(carpetaInicial);
        }

        // Nombre por defecto: favoritos_usuario_fecha
        String nombreArchivo = "favoritos_" + usuarioActual.getUsername() + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName(nombreArchivo + ".csv");

        File archivo = fileChooser.showSaveDialog(viewFactory.getStage());

        if (archivo != null) {
            rutaSeleccionada = archivo.getAbsolutePath();
            txtRuta.setText(rutaSeleccionada);
            lblEstado.setText("✅ Ruta seleccionada: " + archivo.getName());
            lblEstado.setStyle("-fx-text-fill: #27ae60;");

            System.out.println("📁 Ruta seleccionada: " + rutaSeleccionada);
        }
    }

    /**
     * Maneja exportación a CSV
     * RF-009: Exporta favoritos en formato CSV
     */
    @FXML
    private void handleExportar() {
        if (favoritos == null || favoritos.isEmpty()) {
            mostrarAlerta("Error", "No hay canciones para exportar", Alert.AlertType.ERROR);
            return;
        }

        if (rutaSeleccionada == null || rutaSeleccionada.trim().isEmpty()) {
            mostrarAlerta("Error", "Por favor, selecciona una ruta de guardar", Alert.AlertType.ERROR);
            return;
        }

        // Asegurar extensión CSV
        String rutaFinal = CSVExporter.asegurarExtensionCSV(rutaSeleccionada);

        // Validar ruta
        if (!CSVExporter.esRutaValida(rutaFinal)) {
            mostrarAlerta("Error", "La ruta no es válida. Verifica los permisos.", Alert.AlertType.ERROR);
            lblEstado.setText("❌ Error: Ruta inválida");
            lblEstado.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Exportar
        boolean exitoso = CSVExporter.exportarFavoritos(usuarioActual, rutaFinal);

        if (exitoso) {
            lblEstado.setText("✅ Exportación completada: " + new File(rutaFinal).getName());
            lblEstado.setStyle("-fx-text-fill: #27ae60;");

            mostrarAlerta("Éxito", "✅ Favoritos exportados correctamente a:\n" + rutaFinal,
                    Alert.AlertType.INFORMATION);

            System.out.println("✅ Exportación exitosa a: " + rutaFinal);
            System.out.println("📊 Canciones exportadas: " + favoritos.size());

            // Abrir archivo si está seleccionado
            if (chkAbrirArchivo.isSelected()) {
                abrirArchivo(rutaFinal);
            }
        } else {
            lblEstado.setText("❌ Error durante la exportación");
            lblEstado.setStyle("-fx-text-fill: #e74c3c;");

            mostrarAlerta("Error", "❌ Error al exportar favoritos.\nVerifica los permisos de la carpeta.",
                    Alert.AlertType.ERROR);

            System.err.println("❌ Error en exportación");
        }
    }

    /**
     * Abre el archivo exportado con la aplicación por defecto
     */
    private void abrirArchivo(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (archivo.exists()) {
                Desktop.getDesktop().open(archivo);
                System.out.println("📂 Archivo abierto: " + rutaArchivo);
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo abrir el archivo automáticamente: " + e.getMessage());
        }
    }

    /**
     * Maneja volver
     */
    @FXML
    private void handleVolver() {
        System.out.println("🔙 Volviendo al menú principal...");
        viewFactory.mostrarUsuarioMain();
    }

    /**
     * Obtiene la fecha actual formateada
     */
    private String obtenerFechaActual() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return ahora.format(formato);
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
