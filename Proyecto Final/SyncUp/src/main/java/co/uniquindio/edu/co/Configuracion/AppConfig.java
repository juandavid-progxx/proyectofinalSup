package co.uniquindio.edu.co.Configuracion;

public class AppConfig {

    // Información de la aplicación
    public static final String APP_NAME = "SyncUp";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_DESCRIPTION = "Motor de Recomendaciones Musicales";

    // Configuración de búsquedas
    public static final int MAX_RESULTADOS_BUSQUEDA = 100;
    public static final int MIN_CARACTERES_BUSQUEDA = 1;

    // Configuración de recomendaciones
    public static final int CANCIONES_DESCUBRIMIENTO_SEMANAL = 20;
    public static final int CANCIONES_RADIO = 30;
    public static final int SUGERENCIAS_USUARIOS = 10;

    // Configuración de similitud
    public static final double UMBRAL_SIMILITUD_MINIMA = 0.3;
    public static final double UMBRAL_SIMILITUD_ALTA = 0.7;

    // Configuración de validaciones
    public static final int MIN_LONGITUD_PASSWORD = 6;
    public static final int MAX_LONGITUD_PASSWORD = 50;
    public static final int MIN_LONGITUD_USERNAME = 3;
    public static final int MAX_LONGITUD_USERNAME = 20;

    // Configuración de UI
    public static final int ANCHO_VENTANA_DEFAULT = 1200;
    public static final int ALTO_VENTANA_DEFAULT = 700;
    public static final int ANCHO_VENTANA_MIN = 800;
    public static final int ALTO_VENTANA_MIN = 600;

    // Rutas de recursos
    public static final String RUTA_FXML = "/fxml/";
    public static final String RUTA_CSS = "/css/";
    public static final String RUTA_IMAGENES = "/images/";
    public static final String RUTA_DATA = "/data/";

    // Configuración de exportación
    public static final String DIRECTORIO_EXPORTACION_DEFAULT = System.getProperty("user.home") + "/SyncUp/exports/";
    public static final String DIRECTORIO_IMPORTACION_DEFAULT = System.getProperty("user.home") + "/SyncUp/imports/";

    // ⭐ NUEVO: Configuración de persistencia
    public static final String DIRECTORIO_DATOS = System.getProperty("user.home") + "/SyncUp/data/";
    public static final boolean AUTO_GUARDAR = true; // Guardar automáticamente los cambios

    // Configuración de concurrencia
    public static final int TIMEOUT_BUSQUEDA_SEGUNDOS = 30;
    public static final int TIMEOUT_CARGA_SEGUNDOS = 60;

    /**
     * Crea los directorios necesarios para la aplicación
     */
    public static void inicializarDirectorios() {
        crearDirectorioSiNoExiste(DIRECTORIO_EXPORTACION_DEFAULT);
        crearDirectorioSiNoExiste(DIRECTORIO_IMPORTACION_DEFAULT);
        crearDirectorioSiNoExiste(DIRECTORIO_DATOS); // ⭐ NUEVO
    }

    /**
     * Crea un directorio si no existe
     */
    private static void crearDirectorioSiNoExiste(String ruta) {
        java.io.File directorio = new java.io.File(ruta);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
    }

    /**
     * Obtiene información completa de la aplicación
     */
    public static String getInfoCompleta() {
        return APP_NAME + " v" + APP_VERSION + " - " + APP_DESCRIPTION;
    }
}
