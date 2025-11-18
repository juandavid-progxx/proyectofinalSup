package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Servicios.RadioService;
import co.uniquindio.edu.co.Servicios.ReproductorService;
import co.uniquindio.edu.co.Servicios.UsuarioService;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import java.util.List;

/**
 * Controlador para la vista de Radio
 * RF-006: Inicia una "Radio" a partir de una canción,
 * generando una cola de reproducción con temas similares
 */
public class RadioController implements ReproductorService.ReproductorListener {

    @FXML private Label lblCancionActual;
    @FXML private Label lblArtista;
    @FXML private Label lblGenero;
    @FXML private Label lblAnio;
    @FXML private Label lblPosicion;
    @FXML private Label lblMensaje;

    @FXML private TableView<Cancion> tableColaReproduccion;
    @FXML private TableColumn<Cancion, String> colTitulo;
    @FXML private TableColumn<Cancion, String> colArtista;
    @FXML private TableColumn<Cancion, String> colGenero;
    @FXML private TableColumn<Cancion, Integer> colAño;

    @FXML private ListView<String> listSimilares;

    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Button btnAgregarFavorito;
    @FXML private Button btnDetener;
    @FXML private Button btnVolver;

    @FXML private Button btnPlayPause;

    // ⭐ ELEMENTOS SPOTIFY
    @FXML private ProgressBar progressBar;
    @FXML private Label lblTiempo;
    @FXML private Slider sliderVolumen;
    @FXML private Label lblVolumen;
    @FXML private Button btnShuffle;
    @FXML private Button btnRepetir;
    @FXML private Label lblReproduciendo;

    private RadioService radioService;
    private UsuarioService usuarioService;
    private ViewFactory viewFactory;
    private DataInitializer dataInitializer;
    private ReproductorService reproductorService;

    private ObservableList<Cancion> colaObservable;

    // ⭐ Variables de control
    private int modoRepetir = 0;
    private AnimationTimer timerActualizacion;

    @FXML
    public void initialize() {
        this.dataInitializer = new DataInitializer();
        dataInitializer.inicializar();
        this.radioService = new RadioService(dataInitializer);
        this.usuarioService = new UsuarioService();
        this.viewFactory = ViewFactory.getInstancia();

        // ⭐ USAR SINGLETON
        this.reproductorService = ReproductorService.getInstancia();

        reproductorService.setReproductorListener(this);

        configurarTabla();
        inicializarControlesSpotify();
        actualizarUI();

        System.out.println("✅ RadioController inicializado (RF-006)");
    }

    private void configurarTabla() {
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGenero().getNombre()));
        colAño.setCellValueFactory(new PropertyValueFactory<>("año"));

        colaObservable = FXCollections.observableArrayList();
        tableColaReproduccion.setItems(colaObservable);

        // ⭐ Al hacer clic en la tabla, salta a esa canción
        tableColaReproduccion.setOnMouseClicked(event -> {
            int indice = tableColaReproduccion.getSelectionModel().getSelectedIndex();
            if (indice >= 0) {
                radioService.saltarA(indice);
                Cancion cancion = radioService.obtenerActual();

                if (cancion != null && cancion.tieneUrlAudio()) {
                    // ✅ reproducirDesdeURL pausa la anterior automáticamente
                    reproductorService.reproducirDesdeURL(cancion);
                }

                actualizarUI();
            }
        });
    }

    /**
     * ⭐ Inicializa los controles Spotify
     */
    private void inicializarControlesSpotify() {
        sliderVolumen.setMin(0);
        sliderVolumen.setMax(100);
        sliderVolumen.setValue(50);
        sliderVolumen.valueProperty().addListener((obs, oldVal, newVal) -> {
            double volumen = newVal.doubleValue() / 100.0;
            reproductorService.cambiarVolumen(volumen);
            lblVolumen.setText((int)(volumen * 100) + "%");
        });

        lblVolumen.setText("50%");
        lblTiempo.setText("0:00 / 0:00");
        progressBar.setProgress(0);

        btnShuffle.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");
        btnRepetir.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");

        progressBar.setOnMouseClicked(event -> {
            if (reproductorService.getDuracionTotal().toMillis() > 0) {
                double clickPos = event.getX() / progressBar.getWidth();
                Duration nuevaTiempo = Duration.millis(clickPos * reproductorService.getDuracionTotal().toMillis());
                reproductorService.buscar(nuevaTiempo);
            }
        });

        actualizarBarra();
    }

    /**
     * ⭐ Actualiza la barra de progreso
     */
    private void actualizarBarra() {
        timerActualizacion = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (reproductorService.estaReproduciendo()) {
                    Duration actual = reproductorService.getTiempoActual();
                    Duration total = reproductorService.getDuracionTotal();

                    if (total.toMillis() > 0) {
                        double progreso = actual.toMillis() / total.toMillis();
                        progressBar.setProgress(progreso);

                        long minActual = (long) actual.toMinutes();
                        long segActual = (long) actual.toSeconds() % 60;
                        long minTotal = (long) total.toMinutes();
                        long segTotal = (long) total.toSeconds() % 60;

                        lblTiempo.setText(String.format("%d:%02d / %d:%02d",
                                minActual, segActual, minTotal, segTotal));
                    }
                }
            }
        };
        timerActualizacion.start();
    }

    public void iniciarRadio(Cancion cancion) {
        if (cancion == null) {
            mostrarAlerta("Error", "Seleccione una canción para iniciar la radio", Alert.AlertType.ERROR);
            return;
        }

        radioService.iniciarRadio(cancion);

        if (cancion.tieneUrlAudio()) {
            reproductorService.reproducirDesdeURL(cancion);
        }

        actualizarUI();

        System.out.println("🎵 Radio iniciada con: " + cancion.getTitulo());
    }

    /**
     * ⭐ SIGUIENTE - Pausa la anterior y reproduce la nueva
     */
    @FXML
    private void handleSiguiente() {
        Cancion siguiente = radioService.obtenerSiguiente();
        if (siguiente != null && siguiente.tieneUrlAudio()) {
            System.out.println("🎵 Siguiente: " + siguiente.getTitulo());

            // ✅ Detener completamente la anterior
            reproductorService.detener();

            // ✅ Esperar un poco para liberar completamente
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    javafx.application.Platform.runLater(() -> {
                        reproductorService.reproducirDesdeURL(siguiente);
                        actualizarUI();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } else {
            mostrarAlerta("Info", "No hay más canciones en la cola", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * ⭐ ANTERIOR - Pausa la actual y reproduce la anterior
     */
    @FXML
    private void handleAnterior() {
        Cancion anterior = radioService.obtenerAnterior();
        if (anterior != null && anterior.tieneUrlAudio()) {
            System.out.println("🎵 Anterior: " + anterior.getTitulo());

            // ✅ Detener completamente la actual
            reproductorService.detener();

            // ✅ Esperar un poco para liberar completamente
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    javafx.application.Platform.runLater(() -> {
                        reproductorService.reproducirDesdeURL(anterior);
                        actualizarUI();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     * ⭐ Maneja pausar/reanudar
     */
    @FXML
    private void handlePlayPause() {
        if (reproductorService.estaReproduciendo()) {
            reproductorService.pausar();
            btnPlayPause.setText("▶");
        } else {
            reproductorService.reanudar();
            btnPlayPause.setText("⏸");
        }
    }

    /**
     * ⭐ Maneja shuffle
     */
    @FXML
    private void handleShuffle() {
        boolean nuevoEstado = !reproductorService.isShuffle();
        reproductorService.activarShuffle(nuevoEstado);

        if (nuevoEstado) {
            btnShuffle.setStyle("-fx-background-color: transparent; -fx-text-fill: #1db954; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0; -fx-font-weight: bold;");
        } else {
            btnShuffle.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");
        }
    }

    /**
     * ⭐ Maneja repetir
     */
    @FXML
    private void handleRepetir() {
        modoRepetir = (modoRepetir + 1) % 3;

        switch (modoRepetir) {
            case 0:
                btnRepetir.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");
                btnRepetir.setText("🔁");
                break;
            case 1:
                btnRepetir.setStyle("-fx-background-color: transparent; -fx-text-fill: #1db954; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0; -fx-font-weight: bold;");
                btnRepetir.setText("🔁");
                break;
            case 2:
                btnRepetir.setStyle("-fx-background-color: #1db954; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 3; -fx-background-radius: 12; -fx-font-weight: bold;");
                btnRepetir.setText("1");
                break;
        }
    }

    @FXML
    private void handleAgregarFavorito() {
        Cancion actual = radioService.obtenerActual();

        if (actual == null) {
            mostrarAlerta("Error", "No hay canción actual", Alert.AlertType.ERROR);
            return;
        }

        boolean agregado = usuarioService.agregarAFavoritos(actual.getId());

        if (agregado) {
            mostrarAlerta("Éxito", "✅ Canción agregada a favoritos: " + actual.getTitulo(),
                    Alert.AlertType.INFORMATION);
            System.out.println("❤️  Agregada a favoritos: " + actual.getTitulo());
        } else {
            mostrarAlerta("Info", "⚠️ La canción ya está en favoritos", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * ⭐ DETENER - Limpia la cola y detiene reproducción
     */
    @FXML
    private void handleDetener() {
        // ✅ Paso 1: Limpiar la cola de la radio
        radioService.detenerRadio();

        // ✅ Paso 2: Detener reproducción
        reproductorService.detener();

        // ✅ Paso 3: Limpiar completamente el reproductor
        reproductorService.limpiar();

        // ✅ Paso 4: Actualizar UI
        actualizarUI();
        lblMensaje.setText("🔴 Radio detenida");

        System.out.println("⏹️  Radio detenida completamente");
    }

    /**
     * ⭐ VOLVER - Parar todo y volver al menú
     */
    @FXML
    private void handleVolver() {
        // ✅ Paso 1: Detener timer de actualización
        if (timerActualizacion != null) {
            timerActualizacion.stop();
        }

        // ✅ Paso 2: Limpiar la radio
        radioService.detenerRadio();

        // ✅ Paso 3: Detener reproducción
        reproductorService.detener();

        // ✅ Paso 4: Limpiar el reproductor completamente
        reproductorService.limpiar();

        System.out.println("🔙 Volviendo al menú principal...");
        viewFactory.mostrarUsuarioMain();
    }

    private void actualizarUI() {
        Cancion actual = radioService.obtenerActual();

        if (actual == null || !radioService.estaActiva()) {
            lblCancionActual.setText("No hay canción");
            lblArtista.setText("-");
            lblGenero.setText("-");
            lblAnio.setText("-");
            lblPosicion.setText("0/0");
            lblMensaje.setText("Inicia una radio desde la búsqueda");
            colaObservable.clear();
            listSimilares.getItems().clear();
            btnSiguiente.setDisable(true);
            btnAnterior.setDisable(true);
            btnAgregarFavorito.setDisable(true);
            btnPlayPause.setDisable(true);
            return;
        }

        lblCancionActual.setText("♫ " + actual.getTitulo());
        lblArtista.setText("👤 " + actual.getArtista());
        lblGenero.setText("🎸 " + actual.getGenero().getNombre());
        lblAnio.setText("📅 " + actual.getAño());
        lblPosicion.setText((radioService.obtenerIndiceActual() + 1) + "/" +
                radioService.obtenerTamañoCola());
        lblMensaje.setText("📻 Radio activa");

        List<Cancion> cola = radioService.obtenerCola();
        colaObservable.setAll(cola);
        tableColaReproduccion.getSelectionModel().select(radioService.obtenerIndiceActual());

        actualizarSimilares();

        btnSiguiente.setDisable(false);
        btnAnterior.setDisable(false);
        btnAgregarFavorito.setDisable(false);
        btnPlayPause.setDisable(false);

        if (reproductorService.estaReproduciendo()) {
            btnPlayPause.setText("⏸");
        } else {
            btnPlayPause.setText("▶");
        }
    }

    private void actualizarSimilares() {
        List<Cancion> similares = radioService.obtenerCancionesSimilares(5);

        ObservableList<String> items = FXCollections.observableArrayList();

        for (Cancion cancion : similares) {
            String similitud = String.format("%.2f",
                    radioService.obtenerSimilitud(radioService.obtenerActual(), cancion));
            items.add("♫ " + cancion.getTitulo() + " - " + cancion.getArtista() +
                    " (" + similitud + "% similar)");
        }

        listSimilares.setItems(items);
    }

    /**
     * ⭐ Implementación de ReproductorListener
     */
    @Override
    public void onReproduccionIniciada(Cancion cancion) {
        lblCancionActual.setText("♫ " + cancion.getTitulo());
        lblMensaje.setText("📻 Reproduciendo...");
        btnPlayPause.setText("⏸");
        btnPlayPause.setStyle("-fx-background-color: #1db954; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 25; -fx-cursor: hand;");
    }

    @Override
    public void onReproduccionFinalizada(Cancion cancion) {
        System.out.println("✓ Finalizada: " + cancion.getTitulo());

        if (modoRepetir == 2) {
            // Repetir canción actual
            reproductorService.reproducirDesdeURL(cancion);
        } else {
            // ✅ Pasar a la siguiente automáticamente
            javafx.application.Platform.runLater(() -> {
                handleSiguiente();
            });
        }
    }

    @Override
    public void onErrorReproduccion(String mensaje) {
        lblMensaje.setText("❌ Error: " + mensaje);
    }

    public RadioService getRadioService() {
        return radioService;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}