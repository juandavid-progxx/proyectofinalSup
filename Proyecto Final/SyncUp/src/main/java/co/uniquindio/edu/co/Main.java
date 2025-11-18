package co.uniquindio.edu.co;

import co.uniquindio.edu.co.Configuracion.AppConfig;
import co.uniquindio.edu.co.Servicios.ReproductorService;
import co.uniquindio.edu.co.Vista.ViewFactory;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación SyncUp.
 * Motor de Recomendaciones Musicales.
 *
 * @author Equipo SyncUp
 * @version 1.0.0
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Inicializar directorios necesarios
            AppConfig.inicializarDirectorios();

            // Configurar el Stage principal
            primaryStage.setTitle(AppConfig.APP_NAME + " - " + AppConfig.APP_DESCRIPTION);
            primaryStage.setWidth(AppConfig.ANCHO_VENTANA_DEFAULT);
            primaryStage.setHeight(AppConfig.ALTO_VENTANA_DEFAULT);
            primaryStage.setMinWidth(AppConfig.ANCHO_VENTANA_MIN);
            primaryStage.setMinHeight(AppConfig.ALTO_VENTANA_MIN);

            // Configurar ViewFactory con el Stage
            ViewFactory viewFactory = ViewFactory.getInstancia();
            viewFactory.setStage(primaryStage);

            // ⭐ OBTENER LA INSTANCIA ÚNICA DEL REPRODUCTOR
            ReproductorService reproductor = ReproductorService.getInstancia();

            // Mostrar la ventana de login
            viewFactory.mostrarLogin();

            // ⭐ CRÍTICO: Listener para cuando se cierra la aplicación
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("\n🔴 Usuario cerró la ventana principal...");
                limpiarRecursos();
            });

            // Imprimir información en consola
            imprimirBienvenida();

        } catch (Exception e) {
            System.err.println("❌ Error crítico al iniciar la aplicación:");
            e.printStackTrace();

            // Mostrar diálogo de error
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Error de Inicio");
            alert.setHeaderText("No se pudo iniciar SyncUp");
            alert.setContentText("Error: " + e.getMessage() + "\n\nPor favor, verifica la configuración.");
            alert.showAndWait();

            System.exit(1);
        }
    }

    @Override
    public void stop() {
        // Este método se ejecuta cuando se cierra la aplicación (después de setOnCloseRequest)
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎵 Cerrando SyncUp...");
        System.out.println("   Guardando datos...");
        System.out.println("   Limpiando recursos...");

        // ⭐ LIMPIAR REPRODUCTOR AQUÍ TAMBIÉN (por si acaso)
        limpiarRecursos();

        System.out.println("✅ Aplicación cerrada correctamente");
        System.out.println("   ¡Hasta pronto!");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * ⭐ NUEVO: Limpia los recursos del reproductor
     * Se ejecuta cuando:
     * 1. El usuario cierra la ventana
     * 2. Se ejecuta el método stop() del Application
     */
    private void limpiarRecursos() {
        try {
            // ⭐ Obtener el singleton
            ReproductorService reproductor = ReproductorService.getInstancia();

            if (reproductor != null) {
                System.out.println("🧹 Limpiando reproductor de música...");

                // ✅ Detener reproducción
                reproductor.detener();

                // ✅ Limpiar completamente (libera MediaPlayer)
                reproductor.limpiar();

                System.out.println("   ✓ Reproductor limpiado correctamente");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al limpiar recursos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Imprime mensaje de bienvenida en consola
     */
    private void imprimirBienvenida() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎵  SYNCUP - MOTOR DE RECOMENDACIONES MUSICALES");
        System.out.println("=".repeat(60));
        System.out.println("📌 Versión:        " + AppConfig.APP_VERSION);
        System.out.println("📅 Fecha:          " + java.time.LocalDate.now());
        System.out.println("☕ Java Version:   " + System.getProperty("java.version"));
        System.out.println("🖥️  Sistema:        " + System.getProperty("os.name"));
        System.out.println("=".repeat(60));
        System.out.println("\n📋 INFORMACIÓN DE INICIO:");
        System.out.println("   • La aplicación está corriendo en modo GUI");
        System.out.println("   • Los datos se guardan en memoria (no persistente)");
        System.out.println("   • Para pruebas de consola, ejecuta MainTest.java");
        System.out.println("\n🔐 CREDENCIALES POR DEFECTO:");
        System.out.println("   ┌─────────────┬──────────────┐");
        System.out.println("   │ Usuario     │ Contraseña   │");
        System.out.println("   ├─────────────┼──────────────┤");
        System.out.println("   │ admin       │ admin123     │");
        System.out.println("   └─────────────┴──────────────┘");
        System.out.println("\n💡 FUNCIONALIDADES PRINCIPALES:");
        System.out.println("   ✓ Registro e inicio de sesión");
        System.out.println("   ✓ Búsqueda con autocompletado (Trie)");
        System.out.println("   ✓ Búsqueda avanzada con hilos");
        System.out.println("   ✓ Recomendaciones con Dijkstra");
        System.out.println("   ✓ Red social con BFS");
        System.out.println("   ✓ Gestión de canciones (Admin)");
        System.out.println("   ✓ Métricas con gráficos");
        System.out.println("   ✓ Exportación a CSV");
        System.out.println("\n✅ Aplicación iniciada correctamente");
        System.out.println("   Abriendo ventana de login...");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Método principal - Punto de entrada de la aplicación
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Configurar propiedades del sistema (opcional)
        System.setProperty("prism.lcdtext", "false"); // Mejor renderizado de texto

        // Mensaje inicial
        System.out.println("\n🚀 Iniciando SyncUp...\n");

        // Lanzar aplicación JavaFX
        launch(args);
    }
}