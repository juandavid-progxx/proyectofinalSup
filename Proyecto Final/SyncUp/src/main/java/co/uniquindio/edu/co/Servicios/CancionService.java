package co.uniquindio.edu.co.Servicios;

import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import co.uniquindio.edu.co.Repositorio.CancionRepository;
import java.util.List;
import java.util.Map;

public class CancionService {

    private CancionRepository cancionRepo;

    public CancionService() {
        this.cancionRepo = CancionRepository.getInstancia();
    }

    /**
     * Obtiene todas las canciones del catálogo
     */
    public List<Cancion> obtenerTodasLasCanciones() {
        return cancionRepo.obtenerTodas();
    }

    /**
     * Busca una canción por ID
     */
    public Cancion buscarPorId(String id) {
        return cancionRepo.buscarPorId(id);
    }

    /**
     * Busca canciones por título
     */
    public List<Cancion> buscarPorTitulo(String titulo) {
        return cancionRepo.buscarPorTitulo(titulo);
    }

    /**
     * Busca canciones por artista
     */
    public List<Cancion> buscarPorArtista(String artista) {
        return cancionRepo.buscarPorArtista(artista);
    }

    /**
     * Busca canciones por género
     */
    public List<Cancion> buscarPorGenero(GeneroMusical genero) {
        return cancionRepo.buscarPorGenero(genero);
    }

    /**
     * Busca canciones por año
     */
    public List<Cancion> buscarPorAño(int año) {
        return cancionRepo.buscarPorAño(año);
    }

    /**
     * Busca canciones por rango de años
     */
    public List<Cancion> buscarPorRangoAños(int añoInicio, int añoFin) {
        return cancionRepo.buscarPorRangoAños(añoInicio, añoFin);
    }

    /**
     * Obtiene estadísticas de canciones por género
     */
    public Map<GeneroMusical, Integer> obtenerEstadisticasPorGenero() {
        return cancionRepo.obtenerEstadisticasPorGenero();
    }

    /**
     * Obtiene los artistas más populares
     */
    public Map<String, Integer> obtenerArtistasMasPopulares() {
        return cancionRepo.obtenerArtistasMasPopulares();
    }

    /**
     * Obtiene el número total de canciones
     */
    public int contarCanciones() {
        return cancionRepo.contarCanciones();
    }

    /**
     * ⭐ NUEVO - Agrega una canción completamente inicializada
     * Se usa para agregar canciones desde Jamendo con URL
     *
     * @param cancion La canción a agregar (debe tener todos los datos incluyendo URL)
     * @return La canción agregada si fue exitosa, null si ya existía
     */
    public Cancion agregarCancion(Cancion cancion) {
        if (cancion == null) {
            System.err.println("✗ Error: Intento de agregar canción nula");
            return null;
        }

        // Verificar si ya existe
        Cancion existente = cancionRepo.buscarPorId(cancion.getId());
        if (existente != null) {
            System.out.println("⚠ Canción ya existe: " + cancion.getTitulo());
            return null;
        }

        // Agregar la canción al repositorio
        boolean agregada = cancionRepo.agregarCancion(cancion);

        if (agregada) {
            System.out.println("✓ Canción agregada: " + cancion.getTitulo());
            if (cancion.tieneUrlAudio()) {
                System.out.println("  URL: " + cancion.getUrlAudio());
            }
            return cancion;
        } else {
            System.err.println("✗ Error al agregar canción: " + cancion.getTitulo());
            return null;
        }
    }

    /**
     * ⭐ NUEVO - Actualiza una canción existente
     *
     * @param cancion La canción con datos actualizados
     * @return true si se actualizó exitosamente, false en caso contrario
     */
    public boolean actualizarCancion(Cancion cancion) {
        if (cancion == null) {
            System.err.println("✗ Error: Intento de actualizar canción nula");
            return false;
        }

        boolean actualizada = cancionRepo.actualizarCancion(cancion);

        if (actualizada) {
            System.out.println("✓ Canción actualizada: " + cancion.getTitulo());
        } else {
            System.err.println("✗ Error al actualizar canción: " + cancion.getTitulo());
        }

        return actualizada;
    }

    /**
     * ⭐ NUEVO - Elimina una canción por ID
     *
     * @param id El ID de la canción a eliminar
     * @return true si se eliminó exitosamente, false en caso contrario
     */
    public boolean eliminarCancion(String id) {
        if (id == null || id.trim().isEmpty()) {
            System.err.println("✗ Error: ID de canción inválido");
            return false;
        }

        boolean eliminada = cancionRepo.eliminarCancion(id);

        if (eliminada) {
            System.out.println("✓ Canción eliminada: " + id);
        } else {
            System.err.println("✗ Error al eliminar canción: " + id);
        }

        return eliminada;
    }
}
