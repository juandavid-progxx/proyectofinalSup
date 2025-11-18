package co.uniquindio.edu.co.Utils;

import co.uniquindio.edu.co.Modelo.Cancion;

public class SimilitudCalculator {

    // Pesos para cada factor de similitud
    private static final double PESO_GENERO = 0.4;
    private static final double PESO_ARTISTA = 0.3;
    private static final double PESO_AÑO = 0.2;
    private static final double PESO_DURACION = 0.1;

    /**
     * Calcula la similitud entre dos canciones (valor entre 0 y 1)
     * 1 = completamente similar, 0 = completamente diferente
     */
    public static double calcularSimilitud(Cancion c1, Cancion c2) {
        if (c1 == null || c2 == null) {
            return 0.0;
        }

        if (c1.equals(c2)) {
            return 1.0; // La misma canción
        }

        double similitudGenero = calcularSimilitudGenero(c1, c2);
        double similitudArtista = calcularSimilitudArtista(c1, c2);
        double similitudAño = calcularSimilitudAño(c1, c2);
        double similitudDuracion = calcularSimilitudDuracion(c1, c2);

        double similitudTotal =
                (similitudGenero * PESO_GENERO) +
                        (similitudArtista * PESO_ARTISTA) +
                        (similitudAño * PESO_AÑO) +
                        (similitudDuracion * PESO_DURACION);

        return Math.max(0.0, Math.min(1.0, similitudTotal));
    }

    /**
     * Calcula similitud por género
     */
    private static double calcularSimilitudGenero(Cancion c1, Cancion c2) {
        return c1.getGenero() == c2.getGenero() ? 1.0 : 0.0;
    }

    /**
     * Calcula similitud por artista
     */
    private static double calcularSimilitudArtista(Cancion c1, Cancion c2) {
        String artista1 = c1.getArtista().toLowerCase();
        String artista2 = c2.getArtista().toLowerCase();

        if (artista1.equals(artista2)) {
            return 1.0;
        }

        // Similitud parcial si comparten palabras
        String[] palabras1 = artista1.split("\\s+");
        String[] palabras2 = artista2.split("\\s+");

        int palabrasComunes = 0;
        for (String p1 : palabras1) {
            for (String p2 : palabras2) {
                if (p1.equals(p2) && p1.length() > 2) {
                    palabrasComunes++;
                }
            }
        }

        if (palabrasComunes > 0) {
            return 0.5;
        }

        return 0.0;
    }

    /**
     * Calcula similitud por año de lanzamiento
     */
    private static double calcularSimilitudAño(Cancion c1, Cancion c2) {
        int diferencia = Math.abs(c1.getAño() - c2.getAño());

        if (diferencia == 0) return 1.0;
        if (diferencia <= 2) return 0.8;
        if (diferencia <= 5) return 0.6;
        if (diferencia <= 10) return 0.4;
        if (diferencia <= 20) return 0.2;

        return 0.0;
    }

    /**
     * Calcula similitud por duración
     */
    private static double calcularSimilitudDuracion(Cancion c1, Cancion c2) {
        int diferencia = Math.abs(c1.getDuracion() - c2.getDuracion());

        if (diferencia == 0) return 1.0;
        if (diferencia <= 30) return 0.8;   // Menos de 30 segundos
        if (diferencia <= 60) return 0.6;   // Menos de 1 minuto
        if (diferencia <= 120) return 0.4;  // Menos de 2 minutos

        return 0.2;
    }

    /**
     * Obtiene un nivel de similitud en texto
     */
    public static String obtenerNivelSimilitud(double similitud) {
        if (similitud >= 0.8) return "Muy Similar";
        if (similitud >= 0.6) return "Similar";
        if (similitud >= 0.4) return "Algo Similar";
        if (similitud >= 0.2) return "Poco Similar";
        return "Diferente";
    }
}
