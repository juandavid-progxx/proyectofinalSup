package co.uniquindio.edu.co.Repositorio;

import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import co.uniquindio.edu.co.Utils.PersistenciaManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CancionRepository {

    private static CancionRepository instancia;
    private HashMap<String, Cancion> canciones;
    private int contadorId;

    /**
     * Obtiene la instancia única del repositorio
     */
    public static CancionRepository getInstancia() {
        if (instancia == null) {
            instancia = new CancionRepository();
        }
        return instancia;
    }

    /**
     * Constructor privado (Singleton)
     * ⭐ Ahora carga canciones guardadas automáticamente
     */
    private CancionRepository() {
        this.canciones = new HashMap<>();
        this.contadorId = 1;

        // ⭐ Cargar canciones guardadas desde persistencia
        cargarCancionesGuardadas();
    }

    /**
     * ⭐ NUEVO: Carga canciones desde archivo de persistencia
     */
    private void cargarCancionesGuardadas() {
        try {
            HashMap<String, Cancion> cancionesCargadas = PersistenciaManager.cargarCanciones();

            if (cancionesCargadas != null && !cancionesCargadas.isEmpty()) {
                this.canciones.putAll(cancionesCargadas);

                // Actualizar contadorId
                int maxId = cancionesCargadas.keySet().stream()
                        .map(id -> {
                            try {
                                // Extraer número de "jamendo_123"
                                return Integer.parseInt(id.replaceAll("[^0-9]", ""));
                            } catch (Exception e) {
                                return 0;
                            }
                        })
                        .max(Integer::compare)
                        .orElse(0);

                this.contadorId = maxId + 1;
                System.out.println("✅ " + cancionesCargadas.size() + " canciones cargadas desde persistencia");
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudieron cargar canciones guardadas: " + e.getMessage());
        }
    }

    /**
     * Genera un ID único para una nueva canción
     */
    private String generarId() {
        return String.valueOf(contadorId++);
    }

    /**
     * ⭐ ACTUALIZADO: Agrega una canción y la persiste automáticamente
     */
    public boolean agregarCancion(Cancion cancion) {
        if (canciones.containsKey(cancion.getId())) {
            return false;
        }
        canciones.put(cancion.getId(), cancion);

        // ⭐ GUARDAR AUTOMÁTICAMENTE
        PersistenciaManager.guardarCanciones(canciones);
        System.out.println("💾 Canción guardada en persistencia: " + cancion.getTitulo());

        return true;
    }

    /**
     * ⭐ ACTUALIZADO: Agrega una canción generando un ID automático y la persiste
     */
    public Cancion agregarCancionConIdAuto(String titulo, String artista, GeneroMusical genero, int año, int duracion) {
        String id = generarId();
        Cancion cancion = new Cancion(id, titulo, artista, genero, año, duracion);
        canciones.put(id, cancion);

        // ⭐ GUARDAR AUTOMÁTICAMENTE
        PersistenciaManager.guardarCanciones(canciones);

        return cancion;
    }

    /**
     * Busca una canción por ID
     */
    public Cancion buscarPorId(String id) {
        if (canciones == null) return null;
        return canciones.get(id);
    }

    /**
     * Verifica si existe una canción con el ID dado
     */
    public boolean existeId(String id) {
        if (canciones == null) return false;
        return canciones.containsKey(id);
    }

    /**
     * ⭐ ACTUALIZADO: Actualiza una canción y la persiste
     */
    public boolean actualizarCancion(Cancion cancion) {
        if (!canciones.containsKey(cancion.getId())) {
            return false;
        }
        canciones.put(cancion.getId(), cancion);

        // ⭐ GUARDAR AUTOMÁTICAMENTE
        PersistenciaManager.guardarCanciones(canciones);

        return true;
    }

    /**
     * ⭐ ACTUALIZADO: Elimina una canción y persiste
     */
    public boolean eliminarCancion(String id) {
        boolean eliminado = canciones.remove(id) != null;

        if (eliminado) {
            // ⭐ GUARDAR AUTOMÁTICAMENTE
            PersistenciaManager.guardarCanciones(canciones);
        }

        return eliminado;
    }

    /**
     * Obtiene todas las canciones
     */
    public List<Cancion> obtenerTodas() {
        if (canciones == null) return new ArrayList<>();
        return new ArrayList<>(canciones.values());
    }

    /**
     * Busca canciones por título (búsqueda parcial)
     */
    public List<Cancion> buscarPorTitulo(String titulo) {
        List<Cancion> resultados = new ArrayList<>();
        if (canciones == null) return resultados;

        String tituloLower = titulo.toLowerCase();

        for (Cancion cancion : canciones.values()) {
            if (cancion.getTitulo().toLowerCase().contains(tituloLower)) {
                resultados.add(cancion);
            }
        }

        return resultados;
    }

    /**
     * Busca canciones por artista (búsqueda parcial)
     */
    public List<Cancion> buscarPorArtista(String artista) {
        List<Cancion> resultados = new ArrayList<>();
        if (canciones == null) return resultados;

        String artistaLower = artista.toLowerCase();

        for (Cancion cancion : canciones.values()) {
            if (cancion.getArtista().toLowerCase().contains(artistaLower)) {
                resultados.add(cancion);
            }
        }

        return resultados;
    }

    /**
     * Busca canciones por género
     */
    public List<Cancion> buscarPorGenero(GeneroMusical genero) {
        List<Cancion> resultados = new ArrayList<>();
        if (canciones == null) return resultados;

        for (Cancion cancion : canciones.values()) {
            if (cancion.getGenero() == genero) {
                resultados.add(cancion);
            }
        }

        return resultados;
    }

    /**
     * Busca canciones por año
     */
    public List<Cancion> buscarPorAño(int año) {
        List<Cancion> resultados = new ArrayList<>();
        if (canciones == null) return resultados;

        for (Cancion cancion : canciones.values()) {
            if (cancion.getAño() == año) {
                resultados.add(cancion);
            }
        }

        return resultados;
    }

    /**
     * Busca canciones por rango de años
     */
    public List<Cancion> buscarPorRangoAños(int añoInicio, int añoFin) {
        List<Cancion> resultados = new ArrayList<>();
        if (canciones == null) return resultados;

        for (Cancion cancion : canciones.values()) {
            int año = cancion.getAño();
            if (año >= añoInicio && año <= añoFin) {
                resultados.add(cancion);
            }
        }

        return resultados;
    }

    /**
     * Obtiene el número total de canciones
     */
    public int contarCanciones() {
        if (canciones == null) return 0;
        return canciones.size();
    }

    /**
     * Obtiene estadísticas de canciones por género
     */
    public Map<GeneroMusical, Integer> obtenerEstadisticasPorGenero() {
        Map<GeneroMusical, Integer> estadisticas = new HashMap<>();
        if (canciones == null) return estadisticas;

        for (Cancion cancion : canciones.values()) {
            GeneroMusical genero = cancion.getGenero();
            estadisticas.put(genero, estadisticas.getOrDefault(genero, 0) + 1);
        }

        return estadisticas;
    }

    /**
     * Obtiene los artistas más populares (por cantidad de canciones)
     */
    public Map<String, Integer> obtenerArtistasMasPopulares() {
        Map<String, Integer> conteo = new HashMap<>();
        if (canciones == null) return conteo;

        for (Cancion cancion : canciones.values()) {
            String artista = cancion.getArtista();
            conteo.put(artista, conteo.getOrDefault(artista, 0) + 1);
        }

        return conteo;
    }

    /**
     * Limpia todas las canciones
     */
    public void limpiar() {
        canciones.clear();
        contadorId = 1;
        PersistenciaManager.guardarCanciones(canciones);
    }

    /**
     * Carga canciones masivamente desde una lista
     */
    public int cargarCancionesMasivas(List<Cancion> listaCanciones) {
        int cargadas = 0;
        for (Cancion cancion : listaCanciones) {
            if (agregarCancion(cancion)) {
                cargadas++;
            }
        }
        return cargadas;
    }

    public HashMap<String, Cancion> getCanciones() {
        return canciones;
    }
}