package co.uniquindio.edu.co.Utils;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AudioUtils {

    private static final String CARPETA_CANCIONES = "recursos/canciones";

    /**
     * Obtiene la ruta URI de una canción basado en su ID
     * Esperado: recursos/canciones/{id}.mp3
     */
    public static String obtenerRutaCancion(String idCancion) {
        try {
            // Construir la ruta del archivo
            String rutaLocal = CARPETA_CANCIONES + File.separator + idCancion + ".mp3";

            // Verificar si el archivo existe en el sistema de archivos
            if (Files.exists(Paths.get(rutaLocal))) {
                File archivo = new File(rutaLocal);
                return archivo.toURI().toString();
            }

            // Si no existe localmente, intentar desde recursos
            ClassLoader classLoader = AudioUtils.class.getClassLoader();
            URI recurso = classLoader.getResource(rutaLocal).toURI();
            return recurso.toString();

        } catch (Exception e) {
            System.err.println("Error al obtener ruta de canción: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la ruta URI desde una ruta de archivo específica
     */
    public static String obtenerRutaDesdeArchivo(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (archivo.exists()) {
                return archivo.toURI().toString();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error al obtener ruta: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si una canción existe en la carpeta de recursos
     */
    public static boolean existeCancion(String idCancion) {
        try {
            String rutaLocal = CARPETA_CANCIONES + File.separator + idCancion + ".mp3";
            return Files.exists(Paths.get(rutaLocal));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene la carpeta de canciones
     */
    public static String getCarpetaCanciones() {
        return CARPETA_CANCIONES;
    }

    /**
     * Obtiene una lista de canciones disponibles
     */
    public static String[] obtenerCancionesDisponibles() {
        try {
            File carpeta = new File(CARPETA_CANCIONES);
            if (carpeta.exists() && carpeta.isDirectory()) {
                File[] archivos = carpeta.listFiles((dir, name) -> name.endsWith(".mp3"));
                if (archivos != null) {
                    String[] ids = new String[archivos.length];
                    for (int i = 0; i < archivos.length; i++) {
                        ids[i] = archivos[i].getName().replace(".mp3", "");
                    }
                    return ids;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener canciones disponibles: " + e.getMessage());
        }
        return new String[0];
    }

    /**
     * Convierte segundos a formato MM:SS
     */
    public static String formatearDuracion(int segundos) {
        int minutos = segundos / 60;
        int segs = segundos % 60;
        return String.format("%d:%02d", minutos, segs);
    }

    /**
     * Convierte formato MM:SS a segundos
     */
    public static int convertirASegundos(String duracionFormateada) {
        try {
            String[] partes = duracionFormateada.split(":");
            if (partes.length == 2) {
                int minutos = Integer.parseInt(partes[0]);
                int segundos = Integer.parseInt(partes[1]);
                return (minutos * 60) + segundos;
            }
        } catch (Exception e) {
            System.err.println("Error al convertir duración: " + e.getMessage());
        }
        return 0;
    }
}
