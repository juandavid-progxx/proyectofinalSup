package co.uniquindio.edu.co.Servicios;

import co.uniquindio.edu.co.Configuracion.AppConfig;
import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Estructuras.Dijkstra;
import co.uniquindio.edu.co.Estructuras.GrafoDeSimilitud;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Repositorio.CancionRepository;
import co.uniquindio.edu.co.Repositorio.SesionRepository;

import java.util.*;

public class RecomendacionService {

    private CancionRepository cancionRepo;
    private SesionRepository sesionRepo;
    private DataInitializer dataInitializer;
    private GrafoDeSimilitud grafoSimilitud;

    public RecomendacionService(DataInitializer dataInitializer) {
        this.cancionRepo = CancionRepository.getInstancia();
        this.sesionRepo = SesionRepository.getInstancia();
        this.dataInitializer = dataInitializer;
        this.grafoSimilitud = dataInitializer.getGrafoDeSimilitud();
    }

    /**
     * Genera el "Descubrimiento Semanal" basado en favoritos del usuario
     */
    public List<Cancion> generarDescubrimientoSemanal() {
        Usuario usuario = sesionRepo.getUsuarioActual();

        if (usuario == null || usuario.getListaFavoritos().isEmpty()) {
            return generarRecomendacionesAleatorias(AppConfig.CANCIONES_DESCUBRIMIENTO_SEMANAL);
        }

        Set<Cancion> recomendaciones = new HashSet<>();
        LinkedList<Cancion> favoritos = usuario.getListaFavoritos();

        // Obtener canciones similares basadas en favoritos
        for (Cancion favorito : favoritos) {
            List<Cancion> similares = grafoSimilitud.obtenerCancionesSimilares(
                    favorito,
                    5
            );

            recomendaciones.addAll(similares);

            if (recomendaciones.size() >= AppConfig.CANCIONES_DESCUBRIMIENTO_SEMANAL) {
                break;
            }
        }

        // Eliminar canciones que ya están en favoritos
        recomendaciones.removeAll(favoritos);

        // Convertir a lista y limitar
        List<Cancion> resultado = new ArrayList<>(recomendaciones);

        if (resultado.size() > AppConfig.CANCIONES_DESCUBRIMIENTO_SEMANAL) {
            resultado = resultado.subList(0, AppConfig.CANCIONES_DESCUBRIMIENTO_SEMANAL);
        }

        // Si no hay suficientes, completar con aleatorias
        if (resultado.size() < AppConfig.CANCIONES_DESCUBRIMIENTO_SEMANAL) {
            int faltantes = AppConfig.CANCIONES_DESCUBRIMIENTO_SEMANAL - resultado.size();
            List<Cancion> aleatorias = generarRecomendacionesAleatorias(faltantes);
            aleatorias.removeAll(resultado);
            aleatorias.removeAll(favoritos);
            resultado.addAll(aleatorias);
        }

        return resultado;
    }

    /**
     * Genera una "Radio" basada en una canción semilla
     */
    public List<Cancion> generarRadio(String idCancion) {
        Cancion cancionSemilla = cancionRepo.buscarPorId(idCancion);

        if (cancionSemilla == null) {
            return new ArrayList<>();
        }

        // Usar Dijkstra para encontrar canciones similares
        List<Cancion> radio = Dijkstra.encontrarCancionesSimilares(
                grafoSimilitud,
                cancionSemilla,
                AppConfig.CANCIONES_RADIO
        );

        return radio;
    }

    /**
     * Obtiene recomendaciones basadas en un género
     */
    public List<Cancion> recomendarPorGenero(GeneroMusical genero, int limite) {
        List<Cancion> cancionesDelGenero = cancionRepo.buscarPorGenero(genero);

        // Mezclar aleatoriamente
        Collections.shuffle(cancionesDelGenero);

        // Limitar resultados
        if (cancionesDelGenero.size() > limite) {
            return cancionesDelGenero.subList(0, limite);
        }

        return cancionesDelGenero;
    }

    /**
     * Obtiene recomendaciones basadas en un artista
     */
    public List<Cancion> recomendarPorArtista(String artista, int limite) {
        List<Cancion> cancionesDelArtista = cancionRepo.buscarPorArtista(artista);

        // Mezclar aleatoriamente
        Collections.shuffle(cancionesDelArtista);

        // Limitar resultados
        if (cancionesDelArtista.size() > limite) {
            return cancionesDelArtista.subList(0, limite);
        }

        return cancionesDelArtista;
    }

    /**
     * Genera recomendaciones aleatorias
     */
    private List<Cancion> generarRecomendacionesAleatorias(int cantidad) {
        List<Cancion> todasLasCanciones = cancionRepo.obtenerTodas();
        Collections.shuffle(todasLasCanciones);

        if (todasLasCanciones.size() > cantidad) {
            return todasLasCanciones.subList(0, cantidad);
        }

        return todasLasCanciones;
    }

    /**
     * Obtiene las canciones más populares basadas en géneros preferidos
     */
    public List<Cancion> obtenerTendencias(int limite) {
        Usuario usuario = sesionRepo.getUsuarioActual();

        if (usuario == null || usuario.getListaFavoritos().isEmpty()) {
            return generarRecomendacionesAleatorias(limite);
        }

        // Analizar géneros favoritos
        Map<GeneroMusical, Integer> generosPreferidos = new HashMap<>();

        for (Cancion favorita : usuario.getListaFavoritos()) {
            GeneroMusical genero = favorita.getGenero();
            generosPreferidos.put(genero, generosPreferidos.getOrDefault(genero, 0) + 1);
        }

        // Obtener el género más frecuente
        GeneroMusical generoFavorito = generosPreferidos.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (generoFavorito != null) {
            return recomendarPorGenero(generoFavorito, limite);
        }

        return generarRecomendacionesAleatorias(limite);
    }
}
