package co.uniquindio.edu.co.Utils;

import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utilidad para leer archivos MP3 y extraer metadatos
 */
public class MP3Reader {

    private static final String DIRECTORIO_MUSICA = System.getProperty("user.home") + "/SyncUp/music/";

    static {
        // Crear directorio de música si no existe
        try {
            Files.createDirectories(Paths.get(DIRECTORIO_MUSICA));
            System.out.println("✅ Directorio de música: " + DIRECTORIO_MUSICA);
        } catch (Exception e) {
            System.err.println("Error al crear directorio de música: " + e.getMessage());
        }
    }

    /**
     * Lee un archivo MP3 y crea una canción con sus metadatos
     *
     * @param archivoMP3 El archivo MP3 a procesar
     * @return La canción creada o null si hubo error
     */
    public static Cancion leerMP3(File archivoMP3) {
        try {
            System.out.println("\n=== Procesando MP3 ===");
            System.out.println("Archivo: " + archivoMP3.getName());

            // Leer metadatos del MP3
            AudioFile audioFile = AudioFileIO.read(archivoMP3);
            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();

            // Extraer información
            String titulo = obtenerCampo(tag, FieldKey.TITLE, archivoMP3.getName().replace(".mp3", ""));
            String artista = obtenerCampo(tag, FieldKey.ARTIST, "Artista Desconocido");
            String generoStr = obtenerCampo(tag, FieldKey.GENRE, "POP");
            String añoStr = obtenerCampo(tag, FieldKey.YEAR, "2024");

            // Convertir género
            GeneroMusical genero = convertirGenero(generoStr);

            // Obtener año
            int año = 2024;
            try {
                año = Integer.parseInt(añoStr);
                if (año < 1900 || año > 2025) año = 2024;
            } catch (NumberFormatException e) {
                System.out.println("⚠ Año inválido, usando 2024");
            }

            // Duración en segundos
            int duracion = header.getTrackLength();

            // Copiar archivo MP3 al directorio de la aplicación
            String nuevoNombre = UUID.randomUUID().toString() + ".mp3";
            Path destino = Paths.get(DIRECTORIO_MUSICA + nuevoNombre);
            Files.copy(archivoMP3.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

            // ⭐ CRÍTICO: Crear URL correcta usando File.toURI()
            String urlLocal = destino.toFile().toURI().toString();

            System.out.println("✓ Título: " + titulo);
            System.out.println("✓ Artista: " + artista);
            System.out.println("✓ Género: " + genero.getNombre());
            System.out.println("✓ Año: " + año);
            System.out.println("✓ Duración: " + duracion + "s");
            System.out.println("✓ Archivo copiado a: " + destino);
            System.out.println("✓ URL generada: " + urlLocal);

            // Crear canción con URL local
            Cancion cancion = new Cancion(
                    "mp3_" + UUID.randomUUID().toString().substring(0, 8),
                    titulo,
                    artista,
                    genero,
                    año,
                    duracion,
                    urlLocal
            );

            // ⭐ VERIFICAR que la URL se haya establecido
            if (!cancion.tieneUrlAudio()) {
                System.err.println("❌ ERROR: La canción no tiene URL después de crearla");
                System.err.println("   URL que se intentó establecer: " + urlLocal);
                return null;
            }

            System.out.println("✅ Canción creada exitosamente con URL");
            return cancion;

        } catch (Exception e) {
            System.err.println("✗ Error al leer MP3: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lee múltiples archivos MP3
     *
     * @param archivos Lista de archivos MP3
     * @return Lista de canciones creadas
     */
    public static List<Cancion> leerMultiplesMP3(List<File> archivos) {
        List<Cancion> canciones = new ArrayList<>();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("INICIANDO CARGA DE " + archivos.size() + " ARCHIVOS MP3");
        System.out.println("=".repeat(60));

        for (File archivo : archivos) {
            if (archivo.getName().toLowerCase().endsWith(".mp3")) {
                Cancion cancion = leerMP3(archivo);
                if (cancion != null) {
                    canciones.add(cancion);
                }
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("CARGA COMPLETADA: " + canciones.size() + " canciones procesadas");
        System.out.println("=".repeat(60) + "\n");

        return canciones;
    }

    /**
     * Obtiene un campo del tag MP3, retorna valor por defecto si no existe
     */
    private static String obtenerCampo(Tag tag, FieldKey key, String valorPorDefecto) {
        try {
            String valor = tag.getFirst(key);
            return (valor != null && !valor.trim().isEmpty()) ? valor.trim() : valorPorDefecto;
        } catch (Exception e) {
            return valorPorDefecto;
        }
    }

    /**
     * Convierte un string de género a GeneroMusical
     */
    private static GeneroMusical convertirGenero(String generoStr) {
        if (generoStr == null || generoStr.trim().isEmpty()) {
            return GeneroMusical.POP;
        }

        String genero = generoStr.toUpperCase().trim();

        // Mapeo de géneros comunes
        if (genero.contains("ROCK")) return GeneroMusical.ROCK;
        if (genero.contains("POP")) return GeneroMusical.POP;
        if (genero.contains("JAZZ")) return GeneroMusical.JAZZ;
        if (genero.contains("ELECTRONIC") || genero.contains("ELECTRO") || genero.contains("TECHNO") || genero.contains("HOUSE"))
            return GeneroMusical.ELECTRONICA;
        if (genero.contains("HIP") || genero.contains("RAP")) return GeneroMusical.HIP_HOP;
        if (genero.contains("CLASSIC") || genero.contains("CLASICA")) return GeneroMusical.CLASICA;
        if (genero.contains("BLUES")) return GeneroMusical.BLUES;
        if (genero.contains("COUNTRY")) return GeneroMusical.COUNTRY;
        if (genero.contains("REGGAE")) return GeneroMusical.REGGAE;
        if (genero.contains("FOLK")) return GeneroMusical.FOLK;
        if (genero.contains("INDIE")) return GeneroMusical.INDIE;
        if (genero.contains("PUNK")) return GeneroMusical.PUNK;
        if (genero.contains("METAL")) return GeneroMusical.ROCK;
        if (genero.contains("ALTERNATIVE") || genero.contains("ALT")) return GeneroMusical.INDIE;

        // Por defecto
        return GeneroMusical.POP;
    }

    /**
     * Verifica si un archivo es MP3 válido
     */
    public static boolean esMP3Valido(File archivo) {
        if (archivo == null || !archivo.exists()) {
            return false;
        }

        String nombre = archivo.getName().toLowerCase();
        return nombre.endsWith(".mp3") && archivo.length() > 0;
    }
}