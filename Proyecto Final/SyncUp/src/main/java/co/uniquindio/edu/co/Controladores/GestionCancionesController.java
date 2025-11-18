package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Configuracion.AppConfig;
import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import co.uniquindio.edu.co.Servicios.AdminService;
import co.uniquindio.edu.co.Servicios.CancionService;
import co.uniquindio.edu.co.Servicios.JamendoAPI;
import co.uniquindio.edu.co.Threads.CargaMasivaTask;
import co.uniquindio.edu.co.Utils.MP3Reader;  // ⭐ NUEVO IMPORT
import co.uniquindio.edu.co.Utils.Validaciones;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class GestionCancionesController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtArtista;
    @FXML private ComboBox<GeneroMusical> cmbGenero;
    @FXML private TextField txtAño;
    @FXML private TextField txtDuracion;
    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnCargarMasivo;
    @FXML private Button btnCargarJamendo;
    @FXML private Button btnVolver;

    @FXML private TableView<Cancion> tableCanciones;
    @FXML private TableColumn<Cancion, String> colId;
    @FXML private TableColumn<Cancion, String> colTitulo;
    @FXML private TableColumn<Cancion, String> colArtista;
    @FXML private TableColumn<Cancion, String> colGenero;
    @FXML private TableColumn<Cancion, Integer> colAño;
    @FXML private TableColumn<Cancion, String> colDuracion;

    @FXML private ProgressBar progressBar;
    @FXML private Label lblEstado;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private ComboBox<String> cmbFiltroGenero;
    @FXML private Button btnLimpiarFiltros;

    private AdminService adminService;
    private CancionService cancionService;
    private ViewFactory viewFactory;
    private DataInitializer dataInitializer;

    private ObservableList<Cancion> cancionesObservable;
    private List<Cancion> todasLasCanciones;
    private CargaMasivaTask tareaActual;

    @FXML
    public void initialize() {
        this.dataInitializer = new DataInitializer();
        dataInitializer.inicializar();
        this.adminService = new AdminService(dataInitializer);
        this.cancionService = new CancionService();
        this.viewFactory = ViewFactory.getInstancia();

        configurarComponentes();
        configurarTabla();
        cargarCanciones();
    }

    private void configurarComponentes() {
        cmbGenero.setItems(FXCollections.observableArrayList(GeneroMusical.values()));

        ObservableList<String> opcionesFiltro = FXCollections.observableArrayList();
        opcionesFiltro.add("Todos los géneros");
        for (GeneroMusical genero : GeneroMusical.values()) {
            opcionesFiltro.add(genero.getNombre());
        }
        cmbFiltroGenero.setItems(opcionesFiltro);
        cmbFiltroGenero.setValue("Todos los géneros");

        cmbFiltroGenero.setOnAction(e -> aplicarFiltros());
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                aplicarFiltros();
            }
        });

        progressBar.setVisible(false);
        lblEstado.setText("");

        tableCanciones.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarCancionEnFormulario(newSelection);
            }
        });
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getGenero() != null ?
                                cellData.getValue().getGenero().getNombre() : "N/A"));
        colAño.setCellValueFactory(new PropertyValueFactory<>("año"));
        colDuracion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDuracionFormateada()));

        cancionesObservable = FXCollections.observableArrayList();
        tableCanciones.setItems(cancionesObservable);
    }

    private void cargarCanciones() {
        todasLasCanciones = cancionService.obtenerTodasLasCanciones();
        cancionesObservable.setAll(todasLasCanciones);
        lblEstado.setText("Total de canciones: " + todasLasCanciones.size());
    }

    private void aplicarFiltros() {
        if (todasLasCanciones == null) {
            return;
        }

        String terminoBusqueda = txtBuscar.getText().trim().toLowerCase();
        String generoSeleccionado = cmbFiltroGenero.getValue();

        List<Cancion> resultados = todasLasCanciones.stream()
                .filter(cancion -> {
                    boolean coincideBusqueda = terminoBusqueda.isEmpty() ||
                            cancion.getTitulo().toLowerCase().contains(terminoBusqueda) ||
                            cancion.getArtista().toLowerCase().contains(terminoBusqueda);

                    boolean coincideGenero = generoSeleccionado.equals("Todos los géneros") ||
                            (cancion.getGenero() != null &&
                                    cancion.getGenero().getNombre().equals(generoSeleccionado));

                    return coincideBusqueda && coincideGenero;
                })
                .collect(Collectors.toList());

        cancionesObservable.setAll(resultados);
        lblEstado.setText("Resultados: " + resultados.size() + " de " + todasLasCanciones.size());
    }

    @FXML
    private void handleLimpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroGenero.setValue("Todos los géneros");
        cargarCanciones();
    }

    /**
     * ⭐ Muestra confirmación antes de cargar desde Jamendo
     */
    @FXML
    private void handleCargarJamendo() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cargar desde Jamendo");
        confirmacion.setHeaderText("¿Desea cargar canciones populares desde Jamendo?");
        confirmacion.setContentText("Se descargarán las 10 canciones más populares de la semana.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cargarCancionesJamendo();
            }
        });
    }

    /**
     * ⭐ MÉTODO ÚNICO - Carga las canciones desde Jamendo en hilo separado
     */
    private void cargarCancionesJamendo() {
        progressBar.setVisible(true);
        lblEstado.setText("Conectando con Jamendo API...");
        btnCargarJamendo.setDisable(true);

        new Thread(() -> {
            try {
                List<JamendoAPI.CancionAPI> cancionesAPI = JamendoAPI.obtenerCancionesPopulares();

                Platform.runLater(() -> {
                    if (cancionesAPI.isEmpty()) {
                        progressBar.setVisible(false);
                        lblEstado.setText("No se pudieron obtener canciones de Jamendo");
                        btnCargarJamendo.setDisable(false);
                        mostrarAlerta("Error", "No se pudieron cargar canciones desde Jamendo. Verifica tu conexión.",
                                Alert.AlertType.ERROR);
                        return;
                    }

                    int cargadas = 0;
                    int duplicadas = 0;
                    int contador = 1;

                    // ⭐ LOOP PARA AGREGAR CANCIONES CON URL DE JAMENDO
                    for (JamendoAPI.CancionAPI cancionAPI : cancionesAPI) {
                        try {
                            // Inferir género basado en palabras clave
                            GeneroMusical genero = inferirGenero(cancionAPI.getTitulo(), cancionAPI.getArtista());

                            // ⭐ CREAR LA CANCIÓN CON URL DE JAMENDO
                            Cancion nueva = new Cancion(
                                    "jamendo_" + contador,
                                    cancionAPI.getTitulo(),
                                    cancionAPI.getArtista(),
                                    genero,
                                    cancionAPI.getYear(),
                                    cancionAPI.getDuracion(),
                                    cancionAPI.getUrlAudio()  // ⭐ URL CRÍTICA
                            );

                            // ⭐ Guardar en repositorio (se persiste automáticamente)
                            Cancion agregada = cancionService.agregarCancion(nueva);

                            if (agregada != null) {
                                cargadas++;
                                System.out.println("✓ Agregada: " + nueva.getTitulo() + " - URL: " + cancionAPI.getUrlAudio());
                            } else {
                                duplicadas++;
                                System.out.println("⚠ Duplicada: " + nueva.getTitulo());
                            }

                            contador++;

                        } catch (Exception e) {
                            System.err.println("✗ Error al agregar: " + cancionAPI.getTitulo());
                            e.printStackTrace();
                        }
                    }

                    // Actualizar vista
                    cargarCanciones();
                    progressBar.setVisible(false);
                    btnCargarJamendo.setDisable(false);

                    String mensaje = String.format(
                            "Carga completada desde Jamendo:\n" +
                                    "• Canciones nuevas: %d\n" +
                                    "• Ya existentes: %d\n" +
                                    "• Total en catálogo: %d\n\n" +
                                    "✅ Todas guardadas en persistencia",
                            cargadas, duplicadas, todasLasCanciones.size()
                    );

                    lblEstado.setText("✅ Cargadas " + cargadas + " canciones desde Jamendo");
                    mostrarAlerta("Éxito", mensaje, Alert.AlertType.INFORMATION);

                    System.out.println("\n" + "=".repeat(60));
                    System.out.println("✅ CARGA JAMENDO COMPLETADA");
                    System.out.println("   Canciones nuevas: " + cargadas);
                    System.out.println("   Duplicadas: " + duplicadas);
                    System.out.println("   Total: " + todasLasCanciones.size());
                    System.out.println("   ✅ Todas guardadas en: ~/SyncUp/data/canciones.json");
                    System.out.println("=".repeat(60) + "\n");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    btnCargarJamendo.setDisable(false);
                    lblEstado.setText("Error al conectar con Jamendo");
                    mostrarAlerta("Error", "Error al conectar con Jamendo: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    /**
     * ⭐ Infiere el género musical basado en palabras clave
     */
    private GeneroMusical inferirGenero(String titulo, String artista) {
        String textoCompleto = (titulo + " " + artista).toLowerCase();

        if (textoCompleto.contains("rock") || textoCompleto.contains("metal")) return GeneroMusical.ROCK;
        if (textoCompleto.contains("jazz")) return GeneroMusical.JAZZ;
        if (textoCompleto.contains("electronic") || textoCompleto.contains("techno") || textoCompleto.contains("house"))
            return GeneroMusical.ELECTRONICA;
        if (textoCompleto.contains("hip hop") || textoCompleto.contains("rap")) return GeneroMusical.HIP_HOP;
        if (textoCompleto.contains("classical") || textoCompleto.contains("symphony")) return GeneroMusical.CLASICA;
        if (textoCompleto.contains("blues")) return GeneroMusical.BLUES;
        if (textoCompleto.contains("country")) return GeneroMusical.COUNTRY;
        if (textoCompleto.contains("reggae")) return GeneroMusical.REGGAE;
        if (textoCompleto.contains("folk")) return GeneroMusical.FOLK;
        if (textoCompleto.contains("indie")) return GeneroMusical.INDIE;
        if (textoCompleto.contains("punk")) return GeneroMusical.PUNK;

        return GeneroMusical.POP;
    }

    private void cargarCancionEnFormulario(Cancion cancion) {
        txtTitulo.setText(cancion.getTitulo());
        txtArtista.setText(cancion.getArtista());
        cmbGenero.setValue(cancion.getGenero());
        txtAño.setText(String.valueOf(cancion.getAño()));
        txtDuracion.setText(String.valueOf(cancion.getDuracion()));
    }

    @FXML
    private void handleAgregar() {
        if (!validarCampos()) return;

        try {
            String titulo = txtTitulo.getText().trim();
            String artista = txtArtista.getText().trim();
            GeneroMusical genero = cmbGenero.getValue();
            int año = Integer.parseInt(txtAño.getText().trim());
            int duracion = Integer.parseInt(txtDuracion.getText().trim());

            Cancion nueva = adminService.agregarCancion(titulo, artista, genero, año, duracion);

            if (nueva != null) {
                cargarCanciones();
                limpiarFormulario();
                mostrarAlerta("Éxito", "Canción agregada exitosamente.", Alert.AlertType.INFORMATION);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Año y duración deben ser números válidos.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleActualizar() {
        Cancion seleccionada = tableCanciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Seleccione una canción", "Por favor, seleccione una canción para actualizar.", Alert.AlertType.WARNING);
            return;
        }

        if (!validarCampos()) return;

        try {
            String titulo = txtTitulo.getText().trim();
            String artista = txtArtista.getText().trim();
            GeneroMusical genero = cmbGenero.getValue();
            int año = Integer.parseInt(txtAño.getText().trim());
            int duracion = Integer.parseInt(txtDuracion.getText().trim());

            boolean actualizado = adminService.actualizarCancion(
                    seleccionada.getId(), titulo, artista, genero, año, duracion
            );

            if (actualizado) {
                cargarCanciones();
                limpiarFormulario();
                mostrarAlerta("Éxito", "Canción actualizada exitosamente.", Alert.AlertType.INFORMATION);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Año y duración deben ser números válidos.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEliminar() {
        Cancion seleccionada = tableCanciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Seleccione una canción", "Por favor, seleccione una canción para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Está seguro de eliminar la canción '" + seleccionada.getTitulo() + "'?");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean eliminado = adminService.eliminarCancion(seleccionada.getId());

                    if (eliminado) {
                        cargarCanciones();
                        limpiarFormulario();
                        mostrarAlerta("Éxito", "Canción eliminada exitosamente.", Alert.AlertType.INFORMATION);
                    }
                } catch (Exception e) {
                    mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    // ⭐ MÉTODO MODIFICADO - Ahora soporta MP3, CSV y TXT
    // ═══════════════════════════════════════════════════════════════════
    @FXML
    private void handleCargarMasivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivos de canciones");
        fileChooser.setInitialDirectory(new File(AppConfig.DIRECTORIO_IMPORTACION_DEFAULT));

        // ⭐ AGREGAR MP3 a las extensiones
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de Audio MP3", "*.mp3"),
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"),
                new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        // ⭐ PERMITIR SELECCIÓN MÚLTIPLE (para MP3)
        List<File> archivos = fileChooser.showOpenMultipleDialog(viewFactory.getStage());

        if (archivos != null && !archivos.isEmpty()) {
            File primerArchivo = archivos.get(0);
            String nombreArchivo = primerArchivo.getName().toLowerCase();

            // ⭐ DETECTAR TIPO DE ARCHIVO Y PROCESAR
            if (nombreArchivo.endsWith(".mp3")) {
                // Procesar archivos MP3
                cargarArchivosMP3(archivos);
            } else if (nombreArchivo.endsWith(".csv") || nombreArchivo.endsWith(".txt")) {
                // Procesar CSV/TXT (código original)
                cargarArchivoCSV(primerArchivo);
            } else {
                mostrarAlerta("Formato no soportado",
                        "Por favor seleccione archivos MP3, CSV o TXT",
                        Alert.AlertType.WARNING);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ⭐ MÉTODO NUEVO - Carga archivos MP3
    // ═══════════════════════════════════════════════════════════════════
    private void cargarArchivosMP3(List<File> archivos) {
        progressBar.setVisible(true);
        lblEstado.setText("Procesando archivos MP3...");
        btnCargarMasivo.setDisable(true);

        new Thread(() -> {
            try {
                System.out.println("\n" + "=".repeat(60));
                System.out.println("INICIANDO CARGA DE ARCHIVOS MP3");
                System.out.println("Archivos seleccionados: " + archivos.size());
                System.out.println("=".repeat(60));

                // Leer metadatos de todos los MP3
                List<Cancion> canciones = MP3Reader.leerMultiplesMP3(archivos);

                Platform.runLater(() -> {
                    if (canciones.isEmpty()) {
                        progressBar.setVisible(false);
                        lblEstado.setText("No se pudieron procesar los archivos MP3");
                        btnCargarMasivo.setDisable(false);
                        mostrarAlerta("Error",
                                "No se pudieron leer los archivos MP3.\nVerifique que sean archivos válidos.",
                                Alert.AlertType.ERROR);
                        return;
                    }

                    int cargadas = 0;
                    int duplicadas = 0;

                    // Agregar cada canción al repositorio
                    for (Cancion cancion : canciones) {
                        Cancion agregada = cancionService.agregarCancion(cancion);
                        if (agregada != null) {
                            cargadas++;
                            System.out.println("✓ Agregada: " + cancion.getTitulo());
                        } else {
                            duplicadas++;
                            System.out.println("⚠ Duplicada: " + cancion.getTitulo());
                        }
                    }

                    // Actualizar vista
                    cargarCanciones();
                    progressBar.setVisible(false);
                    btnCargarMasivo.setDisable(false);

                    String mensaje = String.format(
                            "Carga de archivos MP3 completada:\n\n" +
                                    "• Archivos procesados: %d\n" +
                                    "• Canciones nuevas: %d\n" +
                                    "• Ya existentes: %d\n" +
                                    "• Total en catálogo: %d\n\n" +
                                    "✅ Archivos copiados a ~/SyncUp/music/\n" +
                                    "✅ Metadatos guardados en persistencia",
                            archivos.size(), cargadas, duplicadas, todasLasCanciones.size()
                    );

                    lblEstado.setText("✅ Cargadas " + cargadas + " canciones desde MP3");
                    mostrarAlerta("Éxito", mensaje, Alert.AlertType.INFORMATION);

                    System.out.println("\n" + "=".repeat(60));
                    System.out.println("✅ CARGA MP3 COMPLETADA");
                    System.out.println("   Archivos procesados: " + archivos.size());
                    System.out.println("   Canciones nuevas: " + cargadas);
                    System.out.println("   Duplicadas: " + duplicadas);
                    System.out.println("   Total en catálogo: " + todasLasCanciones.size());
                    System.out.println("   Archivos MP3 en: ~/SyncUp/music/");
                    System.out.println("   Metadatos en: ~/SyncUp/data/canciones.json");
                    System.out.println("=".repeat(60) + "\n");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    btnCargarMasivo.setDisable(false);
                    lblEstado.setText("Error al procesar MP3");
                    mostrarAlerta("Error",
                            "Error al procesar archivos MP3:\n" + e.getMessage(),
                            Alert.AlertType.ERROR);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    // ═══════════════════════════════════════════════════════════════════
    // ⭐ MÉTODO REFACTORIZADO - Código original de CSV/TXT separado
    // ═══════════════════════════════════════════════════════════════════
    private void cargarArchivoCSV(File archivo) {
        boolean esCSV = archivo.getName().toLowerCase().endsWith(".csv");

        try {
            tareaActual = adminService.crearTareaCargaMasiva(archivo.getAbsolutePath(), esCSV);

            progressBar.progressProperty().bind(tareaActual.progressProperty());
            lblEstado.textProperty().bind(tareaActual.messageProperty());
            progressBar.setVisible(true);

            tareaActual.setOnSucceeded(event -> {
                List<Cancion> canciones = tareaActual.getValue();
                progressBar.setVisible(false);
                lblEstado.textProperty().unbind();

                if (canciones != null && !canciones.isEmpty()) {
                    int cargadas = adminService.procesarCancionesCargadas(canciones);
                    cargarCanciones();
                    lblEstado.setText("Se cargaron " + cargadas + " canciones exitosamente.");
                    mostrarAlerta("Éxito", "Se cargaron " + cargadas + " canciones.", Alert.AlertType.INFORMATION);
                }
            });

            tareaActual.setOnFailed(event -> {
                progressBar.setVisible(false);
                lblEstado.textProperty().unbind();
                lblEstado.setText("Error en la carga.");
                mostrarAlerta("Error", "Ocurrió un error al cargar el archivo: " + tareaActual.getException().getMessage(), Alert.AlertType.ERROR);
            });

            Thread thread = new Thread(tareaActual);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscar() {
        aplicarFiltros();
    }

    @FXML
    private void handleVolver() {
        if (tareaActual != null && tareaActual.isRunning()) {
            tareaActual.cancel();
        }

        viewFactory.mostrarAdminMain();
    }

    private boolean validarCampos() {
        if (!Validaciones.noEsVacio(txtTitulo.getText())) {
            mostrarAlerta("Campo vacío", "El título no puede estar vacío.", Alert.AlertType.WARNING);
            return false;
        }

        if (!Validaciones.noEsVacio(txtArtista.getText())) {
            mostrarAlerta("Campo vacío", "El artista no puede estar vacío.", Alert.AlertType.WARNING);
            return false;
        }

        if (cmbGenero.getValue() == null) {
            mostrarAlerta("Campo vacío", "Debe seleccionar un género.", Alert.AlertType.WARNING);
            return false;
        }

        try {
            int año = Integer.parseInt(txtAño.getText().trim());
            if (!Validaciones.esAñoValido(año)) {
                mostrarAlerta("Año inválido", "El año debe estar entre 1900 y " + (java.time.Year.now().getValue() + 1), Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Año inválido", "El año debe ser un número válido.", Alert.AlertType.WARNING);
            return false;
        }

        try {
            int duracion = Integer.parseInt(txtDuracion.getText().trim());
            if (!Validaciones.esDuracionValida(duracion)) {
                mostrarAlerta("Duración inválida", "La duración debe ser un número positivo (máximo 7200 segundos).", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Duración inválida", "La duración debe ser un número válido.", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void limpiarFormulario() {
        txtTitulo.clear();
        txtArtista.clear();
        cmbGenero.setValue(null);
        txtAño.clear();
        txtDuracion.clear();
        tableCanciones.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}