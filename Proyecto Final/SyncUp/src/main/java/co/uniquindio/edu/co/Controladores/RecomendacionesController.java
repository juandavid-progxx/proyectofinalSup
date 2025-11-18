package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Modelo.GeneroMusical;
import co.uniquindio.edu.co.Servicios.JamendoAPI;
import co.uniquindio.edu.co.Servicios.JamendoAPI.CancionAPI;
import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Servicios.RecomendacionService;
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

public class RecomendacionesController implements ReproductorService.ReproductorListener {

    @FXML private TableView<Cancion> tableRecomendaciones;
    @FXML private TableColumn<Cancion, String> colTitulo;
    @FXML private TableColumn<Cancion, String> colArtista;
    @FXML private TableColumn<Cancion, String> colGenero;
    @FXML private TableColumn<Cancion, Integer> colAnio;
    @FXML private TableColumn<Cancion, String> colDuracion;

    @FXML private Label lblTitulo;

    // ⭐ ELEMENTOS DE REPRODUCCIÓN
    @FXML private Label lblReproduciendo;
    @FXML private Button btnPlayPause;
    @FXML private Button btnSiguiente;
    @FXML private Button btnAnterior;

    // ⭐ ELEMENTOS SPOTIFY
    @FXML private ProgressBar progressBar;
    @FXML private Label lblTiempo;
    @FXML private Slider sliderVolumen;
    @FXML private Label lblVolumen;
    @FXML private Button btnShuffle;
    @FXML private Button btnRepetir;

    private RecomendacionService recomendacionService;
    private UsuarioService usuarioService;
    private ViewFactory viewFactory;
    private DataInitializer dataInitializer;
    private ReproductorService reproductorService;

    private ObservableList<Cancion> recomendacionesObservable;
    private List<CancionAPI> cancionesAPI;

    // ⭐ Variables de control
    private int modoRepetir = 0; // 0: sin repetir, 1: repetir lista, 2: repetir canción
    private AnimationTimer timerActualizacion;

    @FXML
    public void initialize() {
        this.dataInitializer = new DataInitializer();
        dataInitializer.inicializar();
        this.recomendacionService = new RecomendacionService(dataInitializer);
        this.usuarioService = new UsuarioService();
        this.viewFactory = ViewFactory.getInstancia();

        // ⭐ USAR SINGLETON
        this.reproductorService = ReproductorService.getInstancia();

        // Registrar como listener
        reproductorService.setReproductorListener(this);

        configurarTabla();
        configurarReproduccion();
        inicializarControlesSpotify();

        handleDescubrimientoSemanal();
    }

    private void configurarTabla() {
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));

        colGenero.setCellValueFactory(cellData -> {
            Cancion cancion = cellData.getValue();
            if (cancion.getGenero() != null) {
                return new javafx.beans.property.SimpleStringProperty(cancion.getGenero().getNombre());
            } else {
                return new javafx.beans.property.SimpleStringProperty("Sin género");
            }
        });

        colAnio.setCellValueFactory(new PropertyValueFactory<>("año"));
        colDuracion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDuracionFormateada()));

        recomendacionesObservable = FXCollections.observableArrayList();
        tableRecomendaciones.setItems(recomendacionesObservable);
    }

    /**
     * ⭐ Configura la reproducción
     */
    private void configurarReproduccion() {
        // Al seleccionar una canción en la tabla
        tableRecomendaciones.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.tieneUrlAudio()) {
                // ✅ reproducirDesdeURL pausa la anterior automáticamente
                reproductorService.reproducirDesdeURL(newVal);
            }
        });

        lblReproduciendo.setText("");
    }

    /**
     * ⭐ Inicializa los controles Spotify
     */
    private void inicializarControlesSpotify() {
        // Slider de volumen
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

        // Click en barra de progreso
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

    /**
     * ⭐ SIGUIENTE - Pausa la actual y reproduce la siguiente
     */
    @FXML
    private void handleSiguiente() {
        int indiceActual = tableRecomendaciones.getSelectionModel().getSelectedIndex();
        if (indiceActual < tableRecomendaciones.getItems().size() - 1) {
            tableRecomendaciones.getSelectionModel().select(indiceActual + 1);
            Cancion siguiente = tableRecomendaciones.getSelectionModel().getSelectedItem();
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
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        } else {
            lblReproduciendo.setText("No hay más canciones");
        }
    }

    /**
     * ⭐ ANTERIOR - Pausa la actual y reproduce la anterior
     */
    @FXML
    private void handleAnterior() {
        int indiceActual = tableRecomendaciones.getSelectionModel().getSelectedIndex();
        if (indiceActual > 0) {
            tableRecomendaciones.getSelectionModel().select(indiceActual - 1);
            Cancion anterior = tableRecomendaciones.getSelectionModel().getSelectedItem();
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
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        } else {
            lblReproduciendo.setText("No hay canciones anteriores");
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
    private void handleDescubrimientoSemanal() {
        lblTitulo.setText("Descubrimiento Semanal");

        List<Cancion> recomendaciones = recomendacionService.generarDescubrimientoSemanal();
        recomendacionesObservable.setAll(recomendaciones);

        if (recomendaciones.isEmpty()) {
            mostrarAlerta("Sin recomendaciones",
                    "No hay recomendaciones disponibles. Agrega canciones a favoritos para obtener mejores sugerencias.",
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleTendencias() {
        lblTitulo.setText("Tendencias para ti");

        List<Cancion> tendencias = recomendacionService.obtenerTendencias(30);
        recomendacionesObservable.setAll(tendencias);

        if (tendencias.isEmpty()) {
            mostrarAlerta("Sin tendencias",
                    "No hay tendencias disponibles en este momento.",
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleAgregarFavorito() {
        Cancion seleccionada = tableRecomendaciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Seleccione una canción",
                    "Por favor, seleccione una canción para agregar a favoritos.",
                    Alert.AlertType.WARNING);
            return;
        }

        boolean agregado = usuarioService.agregarAFavoritos(seleccionada.getId());

        if (agregado) {
            mostrarAlerta("Éxito", "Canción agregada a favoritos.", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Información", "La canción ya está en favoritos.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * ⭐ VOLVER - Parar TODO completamente
     */
    @FXML
    private void handleVolver() {
        System.out.println("🔙 Volviendo desde Recomendaciones...");

        // ✅ Paso 1: Detener timer de actualización
        if (timerActualizacion != null) {
            timerActualizacion.stop();
        }

        // ✅ Paso 2: Detener reproducción
        reproductorService.detener();

        // ✅ Paso 3: Limpiar el reproductor completamente
        reproductorService.limpiar();

        System.out.println("✅ Reproductor limpiado");
        viewFactory.mostrarUsuarioMain();
    }

    @FXML
    private void handleExplorarJamendo() {
        lblTitulo.setText("Explorar Jamendo 🎶");
        cancionesAPI = JamendoAPI.obtenerCancionesPopulares();
        recomendacionesObservable.clear();

        System.out.println("🎵 Obteniendo canciones de Jamendo...");

        int contador = 1;
        for (CancionAPI c : cancionesAPI) {
            try {
                GeneroMusical genero = inferirGeneroJamendo(c.getTitulo(), c.getArtista());

                Cancion cancion = new Cancion(
                        "jamendo_" + contador,
                        c.getTitulo(),
                        c.getArtista(),
                        genero,
                        c.getYear(),
                        c.getDuracion(),
                        c.getUrlAudio()
                );

                recomendacionesObservable.add(cancion);
                System.out.println("  ✓ " + c.getTitulo() + " (" + genero.getNombre() + ")");
                contador++;

            } catch (Exception e) {
                System.err.println("  ✗ Error al procesar: " + c.getTitulo());
                e.printStackTrace();
            }
        }

        if (cancionesAPI.isEmpty()) {
            mostrarAlerta("Error",
                    "No se pudieron obtener canciones desde Jamendo.\nVerifica tu conexión a internet.",
                    Alert.AlertType.ERROR);
        } else {
            System.out.println("✅ " + recomendacionesObservable.size() + " canciones cargadas");
            mostrarAlerta("Éxito",
                    "Se cargaron " + recomendacionesObservable.size() + " canciones populares de Jamendo",
                    Alert.AlertType.INFORMATION);
        }
    }

    private GeneroMusical inferirGeneroJamendo(String titulo, String artista) {
        String textoCompleto = (titulo + " " + artista).toLowerCase();

        if (textoCompleto.contains("rock")) return GeneroMusical.ROCK;
        if (textoCompleto.contains("jazz")) return GeneroMusical.JAZZ;
        if (textoCompleto.contains("electronic") || textoCompleto.contains("techno")) return GeneroMusical.ELECTRONICA;
        if (textoCompleto.contains("hip hop") || textoCompleto.contains("rap")) return GeneroMusical.HIP_HOP;
        if (textoCompleto.contains("classical") || textoCompleto.contains("symphony")) return GeneroMusical.CLASICA;
        if (textoCompleto.contains("blues")) return GeneroMusical.BLUES;
        if (textoCompleto.contains("country")) return GeneroMusical.COUNTRY;
        if (textoCompleto.contains("reggae")) return GeneroMusical.REGGAE;
        if (textoCompleto.contains("folk")) return GeneroMusical.FOLK;

        return GeneroMusical.POP;
    }

    /**
     * ⭐ Implementación de ReproductorListener
     */
    @Override
    public void onReproduccionIniciada(Cancion cancion) {
        lblReproduciendo.setText("♫ " + cancion.getTitulo() + " - " + cancion.getArtista());
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
            // ✅ Pasar a la siguiente automáticamente (en el thread de JavaFX)
            javafx.application.Platform.runLater(() -> {
                handleSiguiente();
            });
        }
    }

    @Override
    public void onErrorReproduccion(String mensaje) {
        lblReproduciendo.setText("❌ Error: " + mensaje);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}