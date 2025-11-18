package co.uniquindio.edu.co.Controladores;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Reproductor de música personalizado con controles
 */
public class ReproductorMusica {

    private MediaPlayer mediaPlayer;
    private Stage stage;
    private Label lblTiempo;
    private Label lblDuracion;
    private Slider sliderProgreso;
    private Slider sliderVolumen;
    private Button btnPlayPause;
    private boolean reproduciendo = false;

    public ReproductorMusica(String urlAudio, String titulo, String artista) {
        crearVentanaReproductor(urlAudio, titulo, artista);
    }

    private void crearVentanaReproductor(String urlAudio, String titulo, String artista) {
        stage = new Stage();
        stage.setTitle("Reproductor - " + titulo);
        stage.setWidth(500);
        stage.setHeight(350);
        stage.setResizable(false);

        // Contenedor principal
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #f5f5f5;");

        // Información de la canción
        VBox infoCancion = crearInfoCancion(titulo, artista);
        contenedor.getChildren().add(infoCancion);

        // Línea separadora
        Separator separador = new Separator();
        contenedor.getChildren().add(separador);

        // Barra de progreso
        VBox barra = crearBarraProgreso();
        contenedor.getChildren().add(barra);

        // Controles
        HBox controles = crearControles();
        contenedor.getChildren().add(controles);

        // Control de volumen
        VBox volumen = crearControlVolumen();
        contenedor.getChildren().add(volumen);

        // Inicializar reproductor
        try {
            Media media = new Media(urlAudio);
            mediaPlayer = new MediaPlayer(media);

            // Actualizar duración cuando esté lista
            mediaPlayer.setOnReady(() -> {
                javafx.application.Platform.runLater(() -> {
                    double duracionTotal = mediaPlayer.getTotalDuration().toSeconds();
                    lblDuracion.setText(formatearTiempo((int) duracionTotal));
                    sliderProgreso.setMax(duracionTotal);
                });
            });

            // Actualizar tiempo actual
            mediaPlayer.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
                javafx.application.Platform.runLater(() -> {
                    double tiempoActual = newVal.toSeconds();
                    lblTiempo.setText(formatearTiempo((int) tiempoActual));
                    if (!sliderProgreso.isPressed()) {
                        sliderProgreso.setValue(tiempoActual);
                    }
                });
            });

            // Cuando termina la canción
            mediaPlayer.setOnEndOfMedia(() -> {
                reproduciendo = false;
                btnPlayPause.setText("▶ Reproducir");
            });

        } catch (Exception e) {
            mostrarError("Error al cargar la canción: " + e.getMessage());
            return;
        }

        // Escena
        javafx.scene.Scene scene = new javafx.scene.Scene(contenedor);
        stage.setScene(scene);
        stage.show();
    }

    private VBox crearInfoCancion(String titulo, String artista) {
        VBox vbox = new VBox(8);
        vbox.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 8; -fx-padding: 15; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

        Label lblTitulo = new Label("♫ " + titulo);
        lblTitulo.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #5B6FD6;");
        lblTitulo.setWrapText(true);

        Label lblArtista = new Label("por " + artista);
        lblArtista.setStyle("-fx-font-size: 12; -fx-text-fill: #666666;");
        lblArtista.setWrapText(true);

        vbox.getChildren().addAll(lblTitulo, lblArtista);
        return vbox;
    }

    private VBox crearBarraProgreso() {
        VBox vbox = new VBox(8);

        // Slider de progreso
        sliderProgreso = new Slider();
        sliderProgreso.setStyle("-fx-control-inner-background: #5B6FD6;");
        sliderProgreso.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(javafx.util.Duration.seconds(sliderProgreso.getValue()));
            }
        });

        // Tiempos
        HBox tiempos = new HBox(10);
        tiempos.setPrefHeight(20);

        lblTiempo = new Label("0:00");
        lblTiempo.setStyle("-fx-font-size: 10; -fx-text-fill: #666666;");
        lblTiempo.setPrefWidth(40);

        lblDuracion = new Label("0:00");
        lblDuracion.setStyle("-fx-font-size: 10; -fx-text-fill: #666666;");
        lblDuracion.setPrefWidth(40);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        tiempos.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        tiempos.getChildren().addAll(lblTiempo, spacer, lblDuracion);

        vbox.getChildren().addAll(sliderProgreso, tiempos);
        return vbox;
    }

    private HBox crearControles() {
        HBox hbox = new HBox(10);
        hbox.setStyle("-fx-alignment: center;");

        // Botón Anterior
        Button btnAnterior = new Button("⏮ Anterior");
        btnAnterior.setPrefWidth(100);
        btnAnterior.setStyle("-fx-font-size: 11; -fx-padding: 8 15;");
        btnAnterior.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(javafx.util.Duration.seconds(0));
            }
        });

        // Botón Play/Pause
        btnPlayPause = new Button("⏸ Pausar");
        btnPlayPause.setPrefWidth(120);
        btnPlayPause.setStyle("-fx-font-size: 12; -fx-padding: 10 20; -fx-background-color: #5B6FD6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnPlayPause.setOnAction(e -> alternarPlayPause());

        // Botón Siguiente
        Button btnSiguiente = new Button("Siguiente ⏭");
        btnSiguiente.setPrefWidth(100);
        btnSiguiente.setStyle("-fx-font-size: 11; -fx-padding: 8 15;");
        btnSiguiente.setOnAction(e -> {
            if (mediaPlayer != null) {
                double duracion = mediaPlayer.getTotalDuration().toSeconds();
                mediaPlayer.seek(javafx.util.Duration.seconds(duracion));
            }
        });

        hbox.getChildren().addAll(btnAnterior, btnPlayPause, btnSiguiente);
        return hbox;
    }

    private VBox crearControlVolumen() {
        VBox vbox = new VBox(8);

        Label lblVolumen = new Label("Volumen:");
        lblVolumen.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");

        sliderVolumen = new Slider(0, 1, 0.5);
        sliderVolumen.setStyle("-fx-control-inner-background: #5B6FD6;");
        sliderVolumen.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(sliderVolumen.getValue());
            }
        });

        Label lblPorcentaje = new Label("50%");
        lblPorcentaje.setStyle("-fx-font-size: 10; -fx-text-fill: #666666;");

        sliderVolumen.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblPorcentaje.setText((int) (newVal.doubleValue() * 100) + "%");
        });

        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(sliderVolumen, lblPorcentaje);
        HBox.setHgrow(sliderVolumen, javafx.scene.layout.Priority.ALWAYS);

        vbox.getChildren().addAll(lblVolumen, hbox);
        return vbox;
    }

    private void alternarPlayPause() {
        if (mediaPlayer != null) {
            if (reproduciendo) {
                mediaPlayer.pause();
                btnPlayPause.setText("▶ Reproducir");
                reproduciendo = false;
            } else {
                mediaPlayer.play();
                btnPlayPause.setText("⏸ Pausar");
                reproduciendo = true;
            }
        }
    }

    private String formatearTiempo(int segundos) {
        int minutos = segundos / 60;
        int segs = segundos % 60;
        return String.format("%d:%02d", minutos, segs);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
