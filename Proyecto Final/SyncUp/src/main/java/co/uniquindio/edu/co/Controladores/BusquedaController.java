package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import co.uniquindio.edu.co.Servicios.BusquedaService;
import co.uniquindio.edu.co.Servicios.ReproductorService;
import co.uniquindio.edu.co.Servicios.UsuarioService;
import co.uniquindio.edu.co.Threads.BusquedaAvanzadaTask;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class BusquedaController implements ReproductorService.ReproductorListener {

    @FXML private TextField txtBusquedaSimple;
    @FXML private ListView<String> listSugerencias;
    @FXML private Button btnBuscar;

    // Búsqueda avanzada
    @FXML private TextField txtArtista;
    @FXML private ComboBox<GeneroMusical> cmbGenero;
    @FXML private TextField txtAnioInicio;
    @FXML private TextField txtAnioFin;
    @FXML private RadioButton rbAND;
    @FXML private RadioButton rbOR;
    @FXML private ToggleGroup logicaGroup;
    @FXML private Button btnBusquedaAvanzada;
    @FXML private ProgressBar progressBarBusqueda;
    @FXML private Label lblEstado;

    @FXML private TableView<Cancion> tableResultados;
    @FXML private TableColumn<Cancion, String> colTitulo;
    @FXML private TableColumn<Cancion, String> colArtista;
    @FXML private TableColumn<Cancion, String> colGenero;
    @FXML private TableColumn<Cancion, Integer> colAnio;
    @FXML private TableColumn<Cancion, String> colDuracion;

    @FXML private Button btnAgregarFavorito;
    @FXML private Button btnVolver;

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

    private BusquedaService busquedaService;
    private UsuarioService usuarioService;
    private ViewFactory viewFactory;
    private DataInitializer dataInitializer;
    private ReproductorService reproductorService;

    private ObservableList<Cancion> resultadosObservable;
    private BusquedaAvanzadaTask tareaActual;

    // ⭐ Variables de control
    private int modoRepetir = 0;
    private AnimationTimer timerActualizacion;

    @FXML
    public void initialize() {
        this.dataInitializer = new DataInitializer();
        dataInitializer.inicializar();
        this.busquedaService = new BusquedaService(dataInitializer);
        this.usuarioService = new UsuarioService();
        this.viewFactory = ViewFactory.getInstancia();

        // ⭐ USAR SINGLETON
        this.reproductorService = ReproductorService.getInstancia();

        reproductorService.setReproductorListener(this);

        configurarComponentes();
        configurarTabla();
        configurarAutocompletado();
        configurarBusquedaAvanzada();
        configurarReproduccion();
        inicializarControlesSpotify();

        System.out.println("✅ BusquedaController inicializado");
    }

    private void configurarComponentes() {
        cmbGenero.setItems(FXCollections.observableArrayList(GeneroMusical.values()));
        logicaGroup = new ToggleGroup();
        rbAND.setToggleGroup(logicaGroup);
        rbOR.setToggleGroup(logicaGroup);
        rbAND.setSelected(true);
        progressBarBusqueda.setVisible(false);
        lblEstado.setText("");
        lblReproduciendo.setText("");
    }

    /**
     * ⭐ Configura la reproducción
     */
    private void configurarReproduccion() {
        tableResultados.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.tieneUrlAudio()) {
                reproductorService.reproducirDesdeURL(newVal);
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

    /**
     * ⭐ Maneja siguiente
     */
    @FXML
    private void handleSiguiente() {
        int indiceActual = tableResultados.getSelectionModel().getSelectedIndex();
        if (indiceActual < tableResultados.getItems().size() - 1) {
            tableResultados.getSelectionModel().select(indiceActual + 1);
            Cancion siguiente = tableResultados.getSelectionModel().getSelectedItem();
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
     * ⭐ Maneja anterior
     */
    @FXML
    private void handleAnterior() {
        int indiceActual = tableResultados.getSelectionModel().getSelectedIndex();
        if (indiceActual > 0) {
            tableResultados.getSelectionModel().select(indiceActual - 1);
            Cancion anterior = tableResultados.getSelectionModel().getSelectedItem();
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

    private void configurarAutocompletado() {
        txtBusquedaSimple.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                actualizarSugerencias(newValue.trim());
                listSugerencias.setVisible(true);
                listSugerencias.setManaged(true);
            } else {
                listSugerencias.getItems().clear();
                listSugerencias.setVisible(false);
                listSugerencias.setManaged(false);
            }
        });

        txtBusquedaSimple.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN && !listSugerencias.getItems().isEmpty()) {
                listSugerencias.requestFocus();
                listSugerencias.getSelectionModel().selectFirst();
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                handleBuscar();
                event.consume();
            }
        });

        listSugerencias.setOnMouseClicked(event -> {
            String seleccionada = listSugerencias.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                txtBusquedaSimple.setText(seleccionada);
                listSugerencias.getItems().clear();
                listSugerencias.setVisible(false);
                listSugerencias.setManaged(false);
                handleBuscar();
            }
        });

        listSugerencias.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String seleccionada = listSugerencias.getSelectionModel().getSelectedItem();
                if (seleccionada != null) {
                    txtBusquedaSimple.setText(seleccionada);
                    listSugerencias.getItems().clear();
                    listSugerencias.setVisible(false);
                    listSugerencias.setManaged(false);
                    handleBuscar();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                listSugerencias.getItems().clear();
                listSugerencias.setVisible(false);
                listSugerencias.setManaged(false);
                txtBusquedaSimple.requestFocus();
                event.consume();
            }
        });
    }

    private void configurarBusquedaAvanzada() {
        txtAnioInicio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("\\d+")) {
                txtAnioInicio.setText(oldVal);
            }
        });

        txtAnioFin.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("\\d+")) {
                txtAnioFin.setText(oldVal);
            }
        });

        txtAnioFin.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleBusquedaAvanzada();
            }
        });
    }

    private void configurarTabla() {
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGenero().getNombre()));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("año"));
        colDuracion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDuracionFormateada()));

        resultadosObservable = FXCollections.observableArrayList();
        tableResultados.setItems(resultadosObservable);
    }

    private void actualizarSugerencias(String prefijo) {
        try {
            List<String> sugerencias = busquedaService.autocompletarTitulo(prefijo);
            List<String> sugerenciasLimitadas = sugerencias.size() > 10
                    ? sugerencias.subList(0, 10)
                    : sugerencias;
            listSugerencias.setItems(FXCollections.observableArrayList(sugerenciasLimitadas));
        } catch (Exception e) {
            System.err.println("❌ Error en autocompletado: " + e.getMessage());
        }
    }

    @FXML
    private void handleBuscar() {
        String termino = txtBusquedaSimple.getText().trim();

        if (termino.isEmpty()) {
            mostrarAlerta("Campo vacío", "Por favor, ingrese un término de búsqueda.", Alert.AlertType.WARNING);
            return;
        }

        List<Cancion> resultados = busquedaService.busquedaGlobal(termino);
        resultadosObservable.setAll(resultados);

        lblEstado.setText("Se encontraron " + resultados.size() + " resultados para: '" + termino + "'");
        listSugerencias.getItems().clear();
        listSugerencias.setVisible(false);
        listSugerencias.setManaged(false);
    }

    @FXML
    private void handleBusquedaAvanzada() {
        String artista = txtArtista.getText().trim();
        GeneroMusical genero = cmbGenero.getValue();
        String anioInicioStr = txtAnioInicio.getText().trim();
        String anioFinStr = txtAnioFin.getText().trim();
        boolean usarAND = rbAND.isSelected();

        if (artista.isEmpty() && genero == null && anioInicioStr.isEmpty() && anioFinStr.isEmpty()) {
            mostrarAlerta("Sin criterios", "Por favor, especifique al menos un criterio de búsqueda.", Alert.AlertType.WARNING);
            return;
        }

        Integer anioInicio = null;
        Integer anioFin = null;

        try {
            if (!anioInicioStr.isEmpty()) {
                anioInicio = Integer.parseInt(anioInicioStr);
            }
            if (!anioFinStr.isEmpty()) {
                anioFin = Integer.parseInt(anioFinStr);
            }

            if (anioInicio != null && anioFin != null && anioInicio > anioFin) {
                mostrarAlerta("Error", "El año inicio no puede ser mayor que el año fin.", Alert.AlertType.ERROR);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Los años deben ser números válidos.", Alert.AlertType.ERROR);
            return;
        }

        if (tareaActual != null && tareaActual.isRunning()) {
            tareaActual.cancel();
        }

        tareaActual = busquedaService.crearBusquedaAvanzada(
                artista.isEmpty() ? null : artista,
                genero,
                anioInicio,
                anioFin,
                usarAND
        );

        progressBarBusqueda.progressProperty().bind(tareaActual.progressProperty());
        lblEstado.textProperty().bind(tareaActual.messageProperty());
        progressBarBusqueda.setVisible(true);

        tareaActual.setOnSucceeded(event -> {
            List<Cancion> resultados = tareaActual.getValue();
            resultadosObservable.setAll(resultados);
            progressBarBusqueda.setVisible(false);
            lblEstado.textProperty().unbind();
            lblEstado.setText("✅ Búsqueda completada. " + resultados.size() + " resultados encontrados.");
        });

        tareaActual.setOnFailed(event -> {
            progressBarBusqueda.setVisible(false);
            lblEstado.textProperty().unbind();
            lblEstado.setText("❌ Error en la búsqueda.");
            mostrarAlerta("Error", "Ocurrió un error durante la búsqueda: " + tareaActual.getException().getMessage(),
                    Alert.AlertType.ERROR);
        });

        Thread thread = new Thread(tareaActual);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleAgregarFavorito() {
        Cancion seleccionada = tableResultados.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Seleccione una canción", "Por favor, seleccione una canción para agregar a favoritos.", Alert.AlertType.WARNING);
            return;
        }

        boolean agregado = usuarioService.agregarAFavoritos(seleccionada.getId());

        if (agregado) {
            mostrarAlerta("Éxito", "✅ Canción agregada a favoritos: " + seleccionada.getTitulo(), Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Información", "⚠️ La canción ya está en favoritos.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleVolver() {
        if (tareaActual != null && tareaActual.isRunning()) {
            tareaActual.cancel();
        }
        if (timerActualizacion != null) {
            timerActualizacion.stop();
        }
        reproductorService.detener();
        reproductorService.limpiar();
        viewFactory.mostrarUsuarioMain();
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
            reproductorService.reproducirDesdeURL(cancion);
        } else {
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