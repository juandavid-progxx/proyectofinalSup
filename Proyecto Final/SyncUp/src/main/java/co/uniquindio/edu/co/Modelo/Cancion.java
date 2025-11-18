package co.uniquindio.edu.co.Modelo;

import java.util.Objects;

public class Cancion {

    private String id;
    private String titulo;
    private String artista;
    private GeneroMusical genero;
    private int anio;
    private int duracion; // en segundos
    private String urlAudio; // ⭐ NUEVO - URL de audio de Jamendo

    /**
     * Constructor completo de Cancion (sin URL)
     */
    public Cancion(String id, String titulo, String artista, GeneroMusical genero, int anio, int duracion) {
        this.id = id;
        this.titulo = titulo;
        this.artista = artista;
        this.genero = genero;
        this.anio = anio;
        this.duracion = duracion;
        this.urlAudio = null;
    }

    /**
     * Constructor completo de Cancion (con URL) ⭐ NUEVO
     */
    public Cancion(String id, String titulo, String artista, GeneroMusical genero, int año, int duracion, String urlAudio) {
        this.id = id;
        this.titulo = titulo;
        this.artista = artista;
        this.genero = genero;
        this.anio = año;
        this.duracion = duracion;
        this.urlAudio = urlAudio;
    }

    /**
     * Constructor alternativo con género como String
     */
    public Cancion(String id, String titulo, String artista, String genero, int año, int duracion) {
        this(id, titulo, artista, GeneroMusical.fromString(genero), año, duracion);
    }

    /**
     * Constructor alternativo con género como String y URL ⭐ NUEVO
     */
    public Cancion(String id, String titulo, String artista, String genero, int año, int duracion, String urlAudio) {
        this(id, titulo, artista, GeneroMusical.fromString(genero), año, duracion, urlAudio);
    }

    public Cancion() {
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public GeneroMusical getGenero() {
        return genero;
    }

    public void setGenero(GeneroMusical genero) {
        this.genero = genero;
    }

    public int getAño() {
        return anio;
    }

    public void setAño(int año) {
        this.anio = año;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    /**
     * ⭐ NUEVO - Obtiene la URL de audio
     */
    public String getUrlAudio() {
        return urlAudio;
    }

    /**
     * ⭐ NUEVO - Establece la URL de audio
     */
    public void setUrlAudio(String urlAudio) {
        this.urlAudio = urlAudio;
    }

    /**
     * ⭐ NUEVO - Verifica si tiene URL de audio
     */
    public boolean tieneUrlAudio() {
        return urlAudio != null && !urlAudio.trim().isEmpty();
    }

    /**
     * Retorna la duración formateada en minutos:segundos
     */
    public String getDuracionFormateada() {
        int minutos = duracion / 60;
        int segundos = duracion % 60;
        return String.format("%d:%02d", minutos, segundos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cancion cancion = (Cancion) o;
        return Objects.equals(id, cancion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return titulo + " - " + artista + " (" + anio + ")";
    }

    /**
     * Representación CSV de la canción (incluyendo URL) ⭐ MEJORADO
     */
    public String toCSV() {
        return id + "," + titulo + "," + artista + "," + genero.getNombre() + "," + anio + "," + duracion + "," + (urlAudio != null ? urlAudio : "");
    }

    /**
     * Crea una canción desde una línea CSV ⭐ MEJORADO
     */
    public static Cancion fromCSV(String linea) {
        String[] datos = linea.split(",", -1); // -1 para mantener valores vacíos
        if (datos.length >= 6) {
            String urlAudio = datos.length > 6 && !datos[6].trim().isEmpty() ? datos[6].trim() : null;
            return new Cancion(
                    datos[0].trim(),
                    datos[1].trim(),
                    datos[2].trim(),
                    datos[3].trim(),
                    Integer.parseInt(datos[4].trim()),
                    Integer.parseInt(datos[5].trim()),
                    urlAudio
            );
        }
        throw new IllegalArgumentException("Formato CSV inválido para Cancion");
    }
}