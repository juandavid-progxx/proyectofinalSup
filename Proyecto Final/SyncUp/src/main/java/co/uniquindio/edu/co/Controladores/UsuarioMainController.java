package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Servicios.*;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import java.util.LinkedList;
import java.util.List;

public class UsuarioMainController implements ReproductorService.ReproductorListener {

    @FXML private Label lblBienvenida;
    @FXML private TableView<Cancion> tableCanciones;
    @FXML private TableColumn<Cancion, String> colTitulo;
    @FXML private TableColumn<Cancion, String> colArtista;
    @FXML private TableColumn<Cancion, String> colGenero;
    @FXML private TableColumn<Cancion, Integer> colAño;
    @FXML private TableColumn<Cancion, String> colDuracion;

    @FXML private Button btnBusqueda;
    @FXML private Button btnRecomendaciones;
    @FXML private Button btnPerfil;
    @FXML private Button btnSocial;
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnAgregarFavorito;
    @FXML private Button btnEliminarFavorito;
    @FXML private Button btnRadio;
    @FXML private Button btnExportar;

    @FXML private TabPane tabPane;
    @FXML private Tab tabCatalogo;
    @FXML private Tab tabFavoritos;
    @FXML private TableView<Cancion> tableFavoritos;
    @FXML private TableColumn<Cancion, String> colFavTitulo;
    @FXML private TableColumn<Cancion, String> colFavArtista;
    @FXML private TableColumn<Cancion, String> colFavGenero;

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

    private AutenticacionService autenticacionService;
    private UsuarioService usuarioService;
    private CancionService cancionService;
    private RecomendacionService recomendacionService;
    private ViewFactory viewFactory;
    private DataInitializer dataInitializer;
    private ReproductorService reproductorService;

    private ObservableList<Cancion> cancionesObservable;
    private ObservableList<Cancion> favoritosObservable;

    // ⭐ Variables de control
    private boolean repetirActivado = false;
    private int modoRepetir = 0; // 0: sin repetir, 1: repetir lista, 2: repetir canción
    private AnimationTimer timerActualizacion;

    @FXML
    public void initialize() {
        this.autenticacionService = new AutenticacionService();
        this.usuarioService = new UsuarioService();
        this.cancionService = new CancionService();
        this.viewFactory = ViewFactory.getInstancia();
        this.dataInitializer = new DataInitializer();
        dataInitializer.inicializar();
        this.recomendacionService = new RecomendacionService(dataInitializer);

        // ⭐ USAR SINGLETON
        this.reproductorService = ReproductorService.getInstancia();

        // Registrar como listener
        reproductorService.setReproductorListener(this);

        configurarTablas();
        configurarReproduccion();
        inicializarControlesSpotify();
        cargarDatos();
        actualizarBienvenida();
    }

    private void configurarTablas() {
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
        colAño.setCellValueFactory(new PropertyValueFactory<>("año"));
        colDuracion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDuracionFormateada()));

        colFavTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colFavArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colFavGenero.setCellValueFactory(cellData -> {
            Cancion cancion = cellData.getValue();
            if (cancion.getGenero() != null) {
                return new javafx.beans.property.SimpleStringProperty(cancion.getGenero().getNombre());
            } else {
                return new javafx.beans.property.SimpleStringProperty("Sin género");
            }
        });

        cancionesObservable = FXCollections.observableArrayList();
        tableCanciones.setItems(cancionesObservable);

        favoritosObservable = FXCollections.observableArrayList();
        tableFavoritos.setItems(favoritosObservable);
    }

    /**
     * ⭐ Configura la reproducción - CON DEBUG MEJORADO
     */
    private void configurarReproduccion() {
        // Al seleccionar en catálogo
        tableCanciones.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // ⭐ DEBUG COMPLETO
                System.out.println("\n" + "=".repeat(60));
                System.out.println("📌 CANCIÓN SELECCIONADA EN CATÁLOGO");
                System.out.println("   ID: " + newVal.getId());
                System.out.println("   Título: " + newVal.getTitulo());
                System.out.println("   Artista: " + newVal.getArtista());
                System.out.println("   Tiene URL: " + newVal.tieneUrlAudio());
                System.out.println("   URL: " + (newVal.getUrlAudio() != null ? newVal.getUrlAudio() : "NULL"));
                System.out.println("=".repeat(60) + "\n");

                if (newVal.tieneUrlAudio()) {
                    System.out.println("✅ Iniciando reproducción...");
                    reproductorService.reproducirDesdeURL(newVal);
                } else {
                    System.err.println("❌ Esta canción NO tiene URL de audio");
                    lblReproduciendo.setText("❌ Esta canción no tiene audio disponible");
                    mostrarAlerta("Sin audio",
                            "Esta canción no tiene audio disponible para reproducir.",
                            Alert.AlertType.WARNING);
                }
            }
        });

        // Al seleccionar en favoritos
        tableFavoritos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // ⭐ DEBUG COMPLETO
                System.out.println("\n" + "=".repeat(60));
                System.out.println("📌 CANCIÓN SELECCIONADA EN FAVORITOS");
                System.out.println("   ID: " + newVal.getId());
                System.out.println("   Título: " + newVal.getTitulo());
                System.out.println("   Artista: " + newVal.getArtista());
                System.out.println("   Tiene URL: " + newVal.tieneUrlAudio());
                System.out.println("   URL: " + (newVal.getUrlAudio() != null ? newVal.getUrlAudio() : "NULL"));
                System.out.println("=".repeat(60) + "\n");

                if (newVal.tieneUrlAudio()) {
                    System.out.println("✅ Iniciando reproducción...");
                    reproductorService.reproducirDesdeURL(newVal);
                } else {
                    System.err.println("❌ Esta canción NO tiene URL de audio");
                    lblReproduciendo.setText("❌ Esta canción no tiene audio disponible");
                    mostrarAlerta("Sin audio",
                            "Esta canción no tiene audio disponible para reproducir.",
                            Alert.AlertType.WARNING);
                }
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

        // Inicializar label de volumen
        lblVolumen.setText("50%");

        // Inicializar label de tiempo
        lblTiempo.setText("0:00 / 0:00");

        // Inicializar progress bar
        progressBar.setProgress(0);

        // Inicializar botones
        btnShuffle.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");
        btnRepetir.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");

        // Permitir hacer clic en la barra de progreso para saltar
        progressBar.setOnMouseClicked(event -> {
            if (reproductorService.getDuracionTotal().toMillis() > 0) {
                double clickPos = event.getX() / progressBar.getWidth();
                Duration nuevaTiempo = Duration.millis(clickPos * reproductorService.getDuracionTotal().toMillis());
                reproductorService.buscar(nuevaTiempo);
            }
        });

        // Iniciar actualización de barra
        actualizarBarra();
    }

    /**
     * ⭐ Actualiza continuamente la barra de progreso y tiempo
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
        int indiceActual = tableCanciones.getSelectionModel().getSelectedIndex();
        if (indiceActual < tableCanciones.getItems().size() - 1) {
            tableCanciones.getSelectionModel().select(indiceActual + 1);
            Cancion siguiente = tableCanciones.getSelectionModel().getSelectedItem();
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
        int indiceActual = tableCanciones.getSelectionModel().getSelectedIndex();
        if (indiceActual > 0) {
            tableCanciones.getSelectionModel().select(indiceActual - 1);
            Cancion anterior = tableCanciones.getSelectionModel().getSelectedItem();
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
     * ⭐ Maneja shuffle (aleatorio)
     */
    @FXML
    private void handleShuffle() {
        boolean nuevoEstado = !reproductorService.isShuffle();
        reproductorService.activarShuffle(nuevoEstado);

        if (nuevoEstado) {
            btnShuffle.setStyle("-fx-background-color: transparent; -fx-text-fill: #1db954; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0; -fx-font-weight: bold;");
            System.out.println("🔀 SHUFFLE ACTIVADO");
        } else {
            btnShuffle.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");
            System.out.println("🔀 SHUFFLE DESACTIVADO");
        }
    }

    /**
     * ⭐ Maneja repetir (cicla entre: sin repetir → repetir lista → repetir canción)
     */
    @FXML
    private void handleRepetir() {
        modoRepetir = (modoRepetir + 1) % 3;

        switch (modoRepetir) {
            case 0:
                btnRepetir.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");
                btnRepetir.setText("🔁");
                System.out.println("🔁 SIN REPETIR");
                break;
            case 1:
                btnRepetir.setStyle("-fx-background-color: transparent; -fx-text-fill: #1db954; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0; -fx-font-weight: bold;");
                btnRepetir.setText("🔁");
                System.out.println("🔁 REPETIR LISTA");
                break;
            case 2:
                btnRepetir.setStyle("-fx-background-color: #1db954; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 3; -fx-background-radius: 12; -fx-font-weight: bold;");
                btnRepetir.setText("1");
                System.out.println("🔁 REPETIR CANCIÓN");
                break;
        }
    }

    private void cargarDatos() {
        List<Cancion> canciones = cancionService.obtenerTodasLasCanciones();
        cancionesObservable.setAll(canciones);

        // ⭐ DEBUG: Mostrar cuántas canciones tienen URL
        long conURL = canciones.stream().filter(Cancion::tieneUrlAudio).count();
        System.out.println("\n📊 ESTADÍSTICAS DE CATÁLOGO:");
        System.out.println("   Total de canciones: " + canciones.size());
        System.out.println("   Con URL de audio: " + conURL);
        System.out.println("   Sin URL de audio: " + (canciones.size() - conURL));

        cargarFavoritos();
    }

    private void cargarFavoritos() {
        LinkedList<Cancion> favoritos = usuarioService.obtenerFavoritos();
        favoritosObservable.setAll(favoritos);
    }

    private void actualizarBienvenida() {
        Usuario usuario = autenticacionService.getUsuarioActual();
        if (usuario != null) {
            lblBienvenida.setText("¡Bienvenido, " + usuario.getNombre() + "!");
        }
    }

    @FXML
    private void handleAgregarFavorito() {
        Cancion seleccionada = tableCanciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Seleccione una canción", "Por favor, seleccione una canción para agregar a favoritos.", Alert.AlertType.WARNING);
            return;
        }

        boolean agregado = usuarioService.agregarAFavoritos(seleccionada.getId());

        if (agregado) {
            cargarFavoritos();
            mostrarAlerta("Éxito", "Canción agregada a favoritos.", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Información", "La canción ya está en favoritos.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleEliminarFavorito() {
        Cancion seleccionada = tableFavoritos.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Seleccione una canción", "Por favor, seleccione una canción para eliminar de favoritos.", Alert.AlertType.WARNING);
            return;
        }

        boolean eliminado = usuarioService.eliminarDeFavoritos(seleccionada.getId());

        if (eliminado) {
            cargarFavoritos();
            mostrarAlerta("Éxito", "Canción eliminada de favoritos.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleRadio() {
        Cancion seleccionada = tableCanciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Seleccione una canción", "Por favor, seleccione una canción para iniciar la radio.", Alert.AlertType.WARNING);
            return;
        }

        System.out.println("📻 Radio iniciada con: " + seleccionada.getTitulo());
        mostrarAlerta("Radio Iniciada", "Iniciando radio con: '" + seleccionada.getTitulo() + "'", Alert.AlertType.INFORMATION);

        viewFactory.mostrarRadio(seleccionada);
    }

    @FXML
    private void handleExportar() {
        LinkedList<Cancion> favoritos = usuarioService.obtenerFavoritos();

        if (favoritos.isEmpty()) {
            mostrarAlerta("Sin favoritos", "No tienes canciones favoritas para exportar.", Alert.AlertType.WARNING);
            return;
        }

        System.out.println("📥 Abriendo ventana de exportación...");
        viewFactory.mostrarExportar();
    }

    @FXML
    private void handleBusqueda() {
        viewFactory.mostrarBusqueda();
    }

    @FXML
    private void handleRecomendaciones() {
        viewFactory.mostrarRecomendaciones();
    }

    @FXML
    private void handlePerfil() {
        viewFactory.mostrarPerfil();
    }

    @FXML
    private void handleSocial() {
        viewFactory.mostrarSocial();
    }

    /**
     * ⭐ CERRAR SESIÓN - Parar TODO completamente
     */
    @FXML
    private void handleCerrarSesion() {
        System.out.println("🔐 Cerrando sesión...");

        // ✅ Paso 1: Detener timer de actualización
        if (timerActualizacion != null) {
            timerActualizacion.stop();
        }

        // ✅ Paso 2: Detener reproducción
        reproductorService.detener();

        // ✅ Paso 3: Limpiar el reproductor completamente
        reproductorService.limpiar();

        // ✅ Paso 4: Cerrar sesión
        autenticacionService.cerrarSesion();

        // ✅ Paso 5: Volver al login
        viewFactory.mostrarLogin();

        System.out.println("✅ Sesión cerrada - Reproductor limpiado");
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

        // Si está en modo repetir canción, reproducirla de nuevo
        if (modoRepetir == 2) {
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
        lblReproduciendo.setText("❌ " + mensaje);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}