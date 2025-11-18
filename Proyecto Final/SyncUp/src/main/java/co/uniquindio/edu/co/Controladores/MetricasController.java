package co.uniquindio.edu.co.Controladores;

import co.uniquindio.edu.co.Configuracion.AppConfig;
import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import co.uniquindio.edu.co.Servicios.AdminService;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricasController {

    @FXML
    private Label lblTotalCanciones;
    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblGeneroMasPopular;
    @FXML private Label lblArtistaMasPopular;

    @FXML private PieChart pieChartGeneros;
    @FXML private BarChart<String, Number> barChartArtistas;

    @FXML private Button btnExportarEstadisticas;
    @FXML private Button btnRefrescar;
    @FXML private Button btnVolver;

    private AdminService adminService;
    private ViewFactory viewFactory;
    private DataInitializer dataInitializer;

    /**
     * Inicializa el controlador
     */
    @FXML
    public void initialize() {
        this.dataInitializer = new DataInitializer();
        dataInitializer.inicializar();
        this.adminService = new AdminService(dataInitializer);
        this.viewFactory = ViewFactory.getInstancia();

        cargarMetricas();
    }

    /**
     * Carga todas las métricas
     */
    private void cargarMetricas() {
        cargarMetricasGenerales();
        cargarGraficoGeneros();
        cargarGraficoArtistas();
    }

    /**
     * Carga las métricas generales
     */
    private void cargarMetricasGenerales() {
        try {
            int totalCanciones = adminService.obtenerTotalCanciones();
            int totalUsuarios = adminService.obtenerTotalUsuarios();

            lblTotalCanciones.setText(String.valueOf(totalCanciones));
            lblTotalUsuarios.setText(String.valueOf(totalUsuarios));

            // Género más popular
            Map<GeneroMusical, Integer> estadisticasGenero = adminService.obtenerEstadisticasGenero();
            if (!estadisticasGenero.isEmpty()) {
                Map.Entry<GeneroMusical, Integer> generoMasPopular = estadisticasGenero.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElse(null);

                if (generoMasPopular != null) {
                    lblGeneroMasPopular.setText(generoMasPopular.getKey().getNombre() +
                            " (" + generoMasPopular.getValue() + " canciones)");
                }
            }

            // Artista más popular
            Map<String, Integer> estadisticasArtistas = adminService.obtenerArtistasMasPopulares();
            if (!estadisticasArtistas.isEmpty()) {
                Map.Entry<String, Integer> artistaMasPopular = estadisticasArtistas.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElse(null);

                if (artistaMasPopular != null) {
                    lblArtistaMasPopular.setText(artistaMasPopular.getKey() +
                            " (" + artistaMasPopular.getValue() + " canciones)");
                }
            }

        } catch (Exception e) {
            System.err.println("Error al cargar métricas generales: " + e.getMessage());
        }
    }

    /**
     * Carga el gráfico de géneros (Pie Chart)
     */
    private void cargarGraficoGeneros() {
        try {
            Map<GeneroMusical, Integer> estadisticasGenero = adminService.obtenerEstadisticasGenero();

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            for (Map.Entry<GeneroMusical, Integer> entry : estadisticasGenero.entrySet()) {
                pieChartData.add(new PieChart.Data(
                        entry.getKey().getNombre() + " (" + entry.getValue() + ")",
                        entry.getValue()
                ));
            }

            pieChartGeneros.setData(pieChartData);
            pieChartGeneros.setTitle("Distribución de Canciones por Género");
            pieChartGeneros.setLegendVisible(true);

        } catch (Exception e) {
            System.err.println("Error al cargar gráfico de géneros: " + e.getMessage());
        }
    }

    /**
     * Carga el gráfico de artistas (Bar Chart)
     */
    private void cargarGraficoArtistas() {
        try {
            Map<String, Integer> estadisticasArtistas = adminService.obtenerArtistasMasPopulares();

            // Obtener top 10 artistas
            Map<String, Integer> top10 = estadisticasArtistas.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            java.util.LinkedHashMap::new
                    ));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Canciones");

            for (Map.Entry<String, Integer> entry : top10.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            barChartArtistas.getData().clear();
            barChartArtistas.getData().add(series);
            barChartArtistas.setTitle("Top 10 Artistas Más Populares");
            barChartArtistas.setLegendVisible(false);

        } catch (Exception e) {
            System.err.println("Error al cargar gráfico de artistas: " + e.getMessage());
        }
    }

    /**
     * Maneja la exportación de estadísticas
     */
    @FXML
    private void handleExportarEstadisticas() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Estadísticas");
        fileChooser.setInitialDirectory(new File(AppConfig.DIRECTORIO_EXPORTACION_DEFAULT));
        fileChooser.setInitialFileName("estadisticas_syncup.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo CSV", "*.csv")
        );

        File archivo = fileChooser.showSaveDialog(viewFactory.getStage());

        if (archivo != null) {
            try {
                boolean exportado = adminService.exportarEstadisticas(archivo.getAbsolutePath());

                if (exportado) {
                    mostrarAlerta("Éxito", "Estadísticas exportadas correctamente a:\n" + archivo.getAbsolutePath(), javafx.scene.control.Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "Error al exportar estadísticas.", javafx.scene.control.Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                mostrarAlerta("Error", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Maneja el evento de refrescar
     */
    @FXML
    private void handleRefrescar() {
        cargarMetricas();
    }

    /**
     * Maneja el evento de volver
     */
    @FXML
    private void handleVolver() {
        viewFactory.mostrarAdminMain();
    }

    /**
     * Muestra una alerta
     */
    private void mostrarAlerta(String titulo, String mensaje, javafx.scene.control.Alert.AlertType tipo) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
