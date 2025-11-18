package co.uniquindio.edu.co.Servicios;

import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Estructuras.TrieAutocompletado;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import co.uniquindio.edu.co.Repositorio.CancionRepository;
import co.uniquindio.edu.co.Threads.BusquedaAvanzadaTask;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BusquedaService {

    private CancionRepository cancionRepo;
    private TrieAutocompletado trie;

    public BusquedaService(DataInitializer dataInitializer) {
        this.cancionRepo = CancionRepository.getInstancia();
        this.trie = dataInitializer.getTrie();
    }

    /**
     * Busca canciones por autocompletado de título
     */
    public List<String> autocompletarTitulo(String prefijo) {
        if (prefijo == null || prefijo.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return trie.buscarPorPrefijo(prefijo);
    }

    /**
     * Busca canciones cuyo título coincida con las sugerencias del autocompletado
     */
    public List<Cancion> buscarPorAutocompletado(String prefijo) {
        List<String> titulosSugeridos = autocompletarTitulo(prefijo);
        List<Cancion> resultados = new ArrayList<>();

        for (String titulo : titulosSugeridos) {
            List<Cancion> canciones = cancionRepo.buscarPorTitulo(titulo);
            resultados.addAll(canciones);
        }

        return resultados;
    }

    /**
     * Búsqueda simple por título
     */
    public List<Cancion> buscarPorTitulo(String titulo) {
        return cancionRepo.buscarPorTitulo(titulo);
    }

    /**
     * Búsqueda simple por artista
     */
    public List<Cancion> buscarPorArtista(String artista) {
        return cancionRepo.buscarPorArtista(artista);
    }

    /**
     * Búsqueda simple por género
     */
    public List<Cancion> buscarPorGenero(GeneroMusical genero) {
        return cancionRepo.buscarPorGenero(genero);
    }

    /**
     * Búsqueda simple por año
     */
    public List<Cancion> buscarPorAño(int año) {
        return cancionRepo.buscarPorAño(año);
    }

    /**
     * Crea una tarea de búsqueda avanzada (con hilos)
     */
    public BusquedaAvanzadaTask crearBusquedaAvanzada(
            String artista,
            GeneroMusical genero,
            Integer añoInicio,
            Integer añoFin,
            boolean usarLogicaAND) {

        List<Cancion> catalogo = cancionRepo.obtenerTodas();

        return new BusquedaAvanzadaTask(
                catalogo,
                artista,
                genero,
                añoInicio,
                añoFin,
                usarLogicaAND
        );
    }

    /**
     * Búsqueda global por cualquier campo
     */
    public List<Cancion> busquedaGlobal(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Cancion> todas = cancionRepo.obtenerTodas();
        String terminoLower = termino.toLowerCase();

        return todas.stream()
                .filter(cancion ->
                        cancion.getTitulo().toLowerCase().contains(terminoLower) ||
                                cancion.getArtista().toLowerCase().contains(terminoLower) ||
                                cancion.getGenero().getNombre().toLowerCase().contains(terminoLower) ||
                                String.valueOf(cancion.getAño()).contains(terminoLower)
                )
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las canciones (sin filtro)
     */
    public List<Cancion> obtenerTodas() {
        return cancionRepo.obtenerTodas();
    }
}
