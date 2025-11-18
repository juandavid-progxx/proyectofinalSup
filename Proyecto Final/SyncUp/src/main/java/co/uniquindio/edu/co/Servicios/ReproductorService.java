package co.uniquindio.edu.co.Servicios;

import co.uniquindio.edu.co.Modelo.Cancion;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Servicio de reproducción de música - SINGLETON
 * Soporta tanto streaming (Jamendo) como archivos MP3 locales
 */
public class ReproductorService {

    // ⭐ SINGLETON - Una sola instancia
    private static ReproductorService instancia;

    private MediaPlayer mediaPlayer;
    private Cancion cancionActual;
    private ReproductorListener listener;

    // ⭐ Control de estado
    private boolean estaReproduciendo = false;
    private boolean shuffleActivado = false;

    public interface ReproductorListener {
        void onReproduccionIniciada(Cancion cancion);
        void onReproduccionFinalizada(Cancion cancion);
        void onErrorReproduccion(String mensaje);
    }

    /**
     * ⭐ Constructor privado para Singleton
     */
    private ReproductorService() {
        this.mediaPlayer = null;
        this.cancionActual = null;
    }

    /**
     * ⭐ Obtiene la instancia única del servicio
     */
    public static synchronized ReproductorService getInstancia() {
        if (instancia == null) {
            instancia = new ReproductorService();
        }
        return instancia;
    }

    /**
     * ⭐ Configura el listener para eventos
     */
    public void setReproductorListener(ReproductorListener listener) {
        this.listener = listener;
    }

    /**
     * ⭐⭐⭐ NUEVO - Normaliza la URL para que MediaPlayer la acepte
     * MediaPlayer necesita URLs en formato correcto:
     * - file:/// (con 3 barras) para archivos locales
     * - http:// o https:// para streaming
     */
    private String normalizarURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            System.err.println("❌ URL vacía o nula");
            return null;
        }

        // Si ya es una URL HTTP/HTTPS válida (Jamendo), devolverla tal cual
        if (url.startsWith("http://") || url.startsWith("https://")) {
            System.out.println("✓ URL de streaming detectada (Jamendo)");
            return url;
        }

        // Si ya tiene formato file:/// correcto, devolverla
        if (url.startsWith("file:///")) {
            System.out.println("✓ URL file:/// correcta detectada");
            return url;
        }

        try {
            java.io.File archivo;

            // Caso 1: URL con formato file:/ (pero sin las 3 barras)
            if (url.startsWith("file:/")) {
                System.out.println("🔧 Convirtiendo file:/ a file:///");
                java.net.URI uri = new java.net.URI(url);
                archivo = new java.io.File(uri);
            }
            // Caso 2: Ruta absoluta de Windows (C:\... o C:/...)
            else if (url.matches("^[A-Za-z]:[/\\\\].*")) {
                System.out.println("🔧 Convirtiendo ruta Windows a file:///");
                archivo = new java.io.File(url);
            }
            // Caso 3: Ruta Unix/Linux
            else if (url.startsWith("/")) {
                System.out.println("🔧 Convirtiendo ruta Unix a file:///");
                archivo = new java.io.File(url);
            }
            // Caso 4: Ruta relativa
            else {
                System.out.println("🔧 Procesando ruta relativa");
                archivo = new java.io.File(url);
            }

            // Verificar si el archivo existe
            if (!archivo.exists()) {
                System.err.println("❌ Archivo no existe: " + archivo.getAbsolutePath());
                System.err.println("   Intentando buscar en ubicaciones alternativas...");

                // Intentar buscar en el directorio de música de la app
                String directorioMusica = System.getProperty("user.home") + "/SyncUp/music/";
                java.io.File archivoAlternativo = new java.io.File(directorioMusica + archivo.getName());

                if (archivoAlternativo.exists()) {
                    System.out.println("✓ Archivo encontrado en ubicación alternativa");
                    archivo = archivoAlternativo;
                } else {
                    return null;
                }
            }

            // Convertir a URL correcta para MediaPlayer
            // MediaPlayer necesita file:/// con 3 barras
            String urlCorrecta = archivo.toURI().toString();

            System.out.println("✓ URL original:     " + url);
            System.out.println("✓ URL normalizada:  " + urlCorrecta);
            System.out.println("✓ Archivo existe:   " + archivo.getAbsolutePath());
            System.out.println("✓ Tamaño archivo:   " + (archivo.length() / 1024) + " KB");

            return urlCorrecta;

        } catch (Exception e) {
            System.err.println("❌ Error al normalizar URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ⭐ Reproduce una canción desde URL - VERSIÓN MEJORADA
     * Funciona con:
     * - URLs de Jamendo (http/https)
     * - Archivos MP3 locales (file:// o rutas absolutas)
     */
    public void reproducirDesdeURL(Cancion cancion) {
        if (cancion == null) {
            notificarError("Canción nula");
            return;
        }

        if (!cancion.tieneUrlAudio()) {
            System.err.println("❌ La canción NO tiene URL de audio");
            System.err.println("   ID: " + cancion.getId());
            System.err.println("   Título: " + cancion.getTitulo());
            System.err.println("   URL actual: " + cancion.getUrlAudio());
            notificarError("Canción sin URL de audio: " + cancion.getTitulo());
            return;
        }

        try {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("🎵 REPRODUCIENDO CANCIÓN");
            System.out.println("   Título:   " + cancion.getTitulo());
            System.out.println("   Artista:  " + cancion.getArtista());
            System.out.println("   ID:       " + cancion.getId());
            System.out.println("   Duración: " + cancion.getDuracionFormateada());
            System.out.println("=".repeat(70));

            // Detener cualquier reproducción anterior
            System.out.println("🛑 Deteniendo reproductor anterior...");
            detenerInterno();

            // Pequeña pausa para liberar recursos
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // ⭐⭐⭐ CAMBIO IMPORTANTE - Normalizar URL ⭐⭐⭐
            String urlOriginal = cancion.getUrlAudio();
            System.out.println("\n📥 PROCESANDO URL DE AUDIO:");
            System.out.println("   URL original: " + urlOriginal);

            // Normalizar la URL para que MediaPlayer la acepte
            String urlAudio = normalizarURL(urlOriginal);

            if (urlAudio == null) {
                notificarError("No se pudo procesar la URL del audio. Archivo no encontrado.");
                return;
            }

            System.out.println("   URL procesada: " + urlAudio);

            // ⭐ Detectar tipo de fuente
            if (urlAudio.startsWith("file:")) {
                System.out.println("   Tipo: 📁 ARCHIVO MP3 LOCAL");
            } else if (urlAudio.startsWith("http")) {
                System.out.println("   Tipo: 🌐 STREAMING (Jamendo)");
            }

            // Crear Media con la URL normalizada
            System.out.println("\n📦 Creando Media object...");
            Media media = new Media(urlAudio);
            mediaPlayer = new MediaPlayer(media);
            this.cancionActual = cancion;

            // Configurar eventos del MediaPlayer
            mediaPlayer.setOnReady(() -> {
                System.out.println("✅ MediaPlayer LISTO");
                Duration duracion = mediaPlayer.getTotalDuration();
                System.out.println("   Duración detectada: " +
                        String.format("%.2f minutos", duracion.toMinutes()));
            });

            mediaPlayer.setOnPlaying(() -> {
                System.out.println("▶️  REPRODUCIENDO: " + cancion.getTitulo());
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                System.out.println("✅ REPRODUCCIÓN FINALIZADA: " + cancion.getTitulo());
                estaReproduciendo = false;
                if (listener != null) {
                    listener.onReproduccionFinalizada(cancion);
                }
            });

            mediaPlayer.setOnError(() -> {
                String errorMsg = "Error desconocido";
                String errorTipo = "Desconocido";

                try {
                    if (mediaPlayer.getError() != null) {
                        errorMsg = mediaPlayer.getError().getMessage();
                        errorTipo = mediaPlayer.getError().getClass().getSimpleName();
                    }
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                }

                System.err.println("\n" + "=".repeat(70));
                System.err.println("❌ ERROR EN MEDIAPLAYER");
                System.err.println("   Canción:       " + cancion.getTitulo());
                System.err.println("   Tipo de error: " + errorTipo);
                System.err.println("   Mensaje:       " + errorMsg);
                System.err.println("   URL original:  " + urlOriginal);
                System.err.println("   URL procesada: " + urlAudio);
                System.err.println("=".repeat(70) + "\n");

                notificarError("Error al reproducir: " + errorMsg);
            });

            mediaPlayer.setOnPaused(() -> {
                System.out.println("⏸️  PAUSADO");
            });

            mediaPlayer.setOnStopped(() -> {
                System.out.println("⏹️  DETENIDO");
            });

            // Iniciar reproducción
            System.out.println("\n▶️  INICIANDO REPRODUCCIÓN...");
            mediaPlayer.play();
            estaReproduciendo = true;

            // Notificar al listener
            if (listener != null) {
                listener.onReproduccionIniciada(cancion);
            }

            System.out.println("✅ REPRODUCCIÓN INICIADA EXITOSAMENTE");
            System.out.println("=".repeat(70) + "\n");

        } catch (Exception e) {
            System.err.println("\n" + "=".repeat(70));
            System.err.println("❌ EXCEPCIÓN AL REPRODUCIR");
            System.err.println("   Canción:  " + cancion.getTitulo());
            System.err.println("   URL:      " + cancion.getUrlAudio());
            System.err.println("   Error:    " + e.getMessage());
            System.err.println("   Tipo:     " + e.getClass().getName());
            System.err.println("   Stack trace:");
            e.printStackTrace();
            System.err.println("=".repeat(70) + "\n");

            notificarError("Error al cargar canción: " + e.getMessage());
        }
    }

    /**
     * ⭐ Pausa la reproducción actual
     */
    public void pausar() {
        if (mediaPlayer != null && estaReproduciendo) {
            try {
                mediaPlayer.pause();
                estaReproduciendo = false;
                System.out.println("⏸️  Pausada: " +
                        (cancionActual != null ? cancionActual.getTitulo() : "canción"));
            } catch (Exception e) {
                System.err.println("❌ Error al pausar: " + e.getMessage());
            }
        }
    }

    /**
     * ⭐ Reanuda la reproducción
     */
    public void reanudar() {
        if (mediaPlayer != null && !estaReproduciendo) {
            try {
                mediaPlayer.play();
                estaReproduciendo = true;
                System.out.println("▶️  Reanudada: " +
                        (cancionActual != null ? cancionActual.getTitulo() : "canción"));
            } catch (Exception e) {
                System.err.println("❌ Error al reanudar: " + e.getMessage());
            }
        }
    }

    /**
     * ⭐ Detiene la reproducción
     */
    public void detener() {
        detenerInterno();
    }

    /**
     * ⭐ Método interno para detener
     */
    private void detenerInterno() {
        if (mediaPlayer != null) {
            try {
                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING ||
                        status == MediaPlayer.Status.PAUSED) {
                    mediaPlayer.stop();
                }
            } catch (Exception e) {
                System.err.println("❌ Error al detener: " + e.getMessage());
            }
        }
        estaReproduciendo = false;
        cancionActual = null;
    }

    /**
     * ⭐ Limpia el reproductor COMPLETAMENTE
     */
    public void limpiar() {
        try {
            if (mediaPlayer != null) {
                try {
                    MediaPlayer.Status status = mediaPlayer.getStatus();
                    if (status == MediaPlayer.Status.PLAYING ||
                            status == MediaPlayer.Status.PAUSED) {
                        mediaPlayer.stop();
                    }
                } catch (Exception e) {
                    System.err.println("  Error al detener: " + e.getMessage());
                }

                try {
                    mediaPlayer.dispose();
                } catch (Exception e) {
                    System.err.println("  Error en dispose: " + e.getMessage());
                }

                mediaPlayer = null;
            }
        } catch (Exception e) {
            System.err.println("❌ Error al limpiar: " + e.getMessage());
            mediaPlayer = null;
        }

        cancionActual = null;
        estaReproduciendo = false;
        System.out.println("🧹 Reproductor limpiado");
    }

    /**
     * ⭐ Cambia el volumen (0.0 a 1.0)
     */
    public void cambiarVolumen(double volumen) {
        if (mediaPlayer != null) {
            try {
                double vol = Math.max(0.0, Math.min(1.0, volumen));
                mediaPlayer.setVolume(vol);
            } catch (Exception e) {
                System.err.println("❌ Error al cambiar volumen: " + e.getMessage());
            }
        }
    }

    /**
     * ⭐ Busca una posición en la canción
     */
    public void buscar(Duration duracion) {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.getTotalDuration().toMillis() > 0) {
                    mediaPlayer.seek(duracion);
                }
            } catch (Exception e) {
                System.err.println("❌ Error al buscar: " + e.getMessage());
            }
        }
    }

    /**
     * ⭐ Obtiene el tiempo actual
     */
    public Duration getTiempoActual() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentTime();
            } catch (Exception e) {
                return Duration.ZERO;
            }
        }
        return Duration.ZERO;
    }

    /**
     * ⭐ Obtiene la duración total
     */
    public Duration getDuracionTotal() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getTotalDuration();
            } catch (Exception e) {
                return Duration.ZERO;
            }
        }
        return Duration.ZERO;
    }

    /**
     * ⭐ Verifica si está reproduciendo
     */
    public boolean estaReproduciendo() {
        return estaReproduciendo && mediaPlayer != null;
    }

    /**
     * ⭐ Activa/desactiva shuffle
     */
    public void activarShuffle(boolean activado) {
        this.shuffleActivado = activado;
        System.out.println(activado ? "🔀 SHUFFLE ACTIVADO" : "🔀 SHUFFLE DESACTIVADO");
    }

    /**
     * ⭐ Obtiene estado de shuffle
     */
    public boolean isShuffle() {
        return shuffleActivado;
    }

    /**
     * ⭐ Obtiene la canción actual
     */
    public Cancion getCancionActual() {
        return cancionActual;
    }

    /**
     * ⭐ Notifica errores
     */
    private void notificarError(String mensaje) {
        System.err.println("❌ " + mensaje);
        if (listener != null) {
            listener.onErrorReproduccion(mensaje);
        }
    }
}