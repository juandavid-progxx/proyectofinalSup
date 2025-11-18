package co.uniquindio.edu.co.Servicios;

import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Repositorio.CancionRepository;
import co.uniquindio.edu.co.Repositorio.SesionRepository;
import co.uniquindio.edu.co.Repositorio.UsuarioRepository;
import co.uniquindio.edu.co.Threads.CargaMasivaTask;
import co.uniquindio.edu.co.Utils.CSVExporter;
import java.util.List;
import java.util.Map;

public class AdminService {

    private CancionRepository cancionRepo;
    private UsuarioRepository usuarioRepo;
    private SesionRepository sesionRepo;
    private DataInitializer dataInitializer;

    public AdminService(DataInitializer dataInitializer) {
        this.cancionRepo = CancionRepository.getInstancia();
        this.usuarioRepo = UsuarioRepository.getInstancia();
        this.sesionRepo = SesionRepository.getInstancia();
        this.dataInitializer = dataInitializer;
    }

    /**
     * Verifica si el usuario actual es administrador
     */
    private boolean esAdministrador() {
        return sesionRepo.esAdministrador();
    }

    // ========== GESTIÓN DE CANCIONES ==========

    /**
     * Agrega una nueva canción al catálogo
     */
    public Cancion agregarCancion(String titulo, String artista, GeneroMusical genero, int año, int duracion) {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden agregar canciones.");
        }

        Cancion cancion = cancionRepo.agregarCancionConIdAuto(titulo, artista, genero, año, duracion);

        // Actualizar estructuras de datos
        dataInitializer.actualizarGrafoConCancion(cancion);
        dataInitializer.actualizarTrieConTitulo(cancion.getTitulo());

        return cancion;
    }

    /**
     * Actualiza una canción existente
     */
    public boolean actualizarCancion(String id, String titulo, String artista, GeneroMusical genero, int año, int duracion) {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden actualizar canciones.");
        }

        Cancion cancion = cancionRepo.buscarPorId(id);

        if (cancion == null) {
            return false;
        }

        // Guardar título anterior para actualizar el Trie
        String tituloAnterior = cancion.getTitulo();

        // Actualizar datos
        cancion.setTitulo(titulo);
        cancion.setArtista(artista);
        cancion.setGenero(genero);
        cancion.setAño(año);
        cancion.setDuracion(duracion);

        boolean actualizado = cancionRepo.actualizarCancion(cancion);

        if (actualizado) {
            // Actualizar Trie si cambió el título
            if (!tituloAnterior.equals(titulo)) {
                dataInitializer.getTrie().eliminar(tituloAnterior);
                dataInitializer.actualizarTrieConTitulo(titulo);
            }

            // Reconstruir grafo de similitud (la similitud puede haber cambiado)
            dataInitializer.reconstruirEstructuras();
        }

        return actualizado;
    }

    /**
     * Elimina una canción del catálogo
     */
    public boolean eliminarCancion(String id) {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden eliminar canciones.");
        }

        Cancion cancion = cancionRepo.buscarPorId(id);

        if (cancion == null) {
            return false;
        }

        // Eliminar de las estructuras
        dataInitializer.eliminarCancionDeEstructuras(cancion);

        // Eliminar del repositorio
        boolean eliminado = cancionRepo.eliminarCancion(id);

        if (eliminado) {
            // Eliminar de favoritos de todos los usuarios
            List<Usuario> usuarios = usuarioRepo.obtenerTodos();
            for (Usuario usuario : usuarios) {
                usuario.eliminarFavorito(cancion);
                usuarioRepo.actualizarUsuario(usuario);
            }
        }

        return eliminado;
    }

    /**
     * Crea una tarea para carga masiva de canciones
     */
    public CargaMasivaTask crearTareaCargaMasiva(String rutaArchivo, boolean esCSV) {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden cargar canciones masivamente.");
        }

        return new CargaMasivaTask(rutaArchivo, esCSV);
    }

    /**
     * Procesa las canciones cargadas masivamente
     */
    public int procesarCancionesCargadas(List<Cancion> canciones) {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden procesar canciones.");
        }

        int cargadas = cancionRepo.cargarCancionesMasivas(canciones);

        if (cargadas > 0) {
            // Reconstruir estructuras con las nuevas canciones
            dataInitializer.reconstruirEstructuras();
        }

        return cargadas;
    }

    // ========== GESTIÓN DE USUARIOS ==========

    /**
     * Obtiene todos los usuarios
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden listar usuarios.");
        }

        return usuarioRepo.obtenerTodos();
    }

    /**
     * Obtiene solo usuarios regulares (no administradores)
     */
    public List<Usuario> obtenerUsuariosRegulares() {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden listar usuarios.");
        }

        return usuarioRepo.obtenerUsuariosRegulares();
    }

    /**
     * Elimina un usuario
     */
    public boolean eliminarUsuario(String username) {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden eliminar usuarios.");
        }

        // No permitir eliminar al administrador principal
        if (username.equals("admin")) {
            return false;
        }

        boolean eliminado = usuarioRepo.eliminarUsuario(username);

        if (eliminado) {
            // Eliminar del grafo social
            dataInitializer.eliminarUsuarioDeGrafo(username);

            // Eliminar de las listas de seguidos de otros usuarios
            List<Usuario> todosLosUsuarios = usuarioRepo.obtenerTodos();
            for (Usuario usuario : todosLosUsuarios) {
                usuario.dejarDeSeguir(username);
                usuarioRepo.actualizarUsuario(usuario);
            }
        }

        return eliminado;
    }

    /**
     * Busca usuarios por nombre
     */
    public List<Usuario> buscarUsuarios(String nombre) {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden buscar usuarios.");
        }

        return usuarioRepo.buscarPorNombre(nombre);
    }

    // ========== MÉTRICAS Y ESTADÍSTICAS ==========

    /**
     * Obtiene el número total de usuarios
     */
    public int obtenerTotalUsuarios() {
        return usuarioRepo.contarUsuarios();
    }

    /**
     * Obtiene el número total de canciones
     */
    public int obtenerTotalCanciones() {
        return cancionRepo.contarCanciones();
    }

    /**
     * Obtiene estadísticas de canciones por género
     */
    public Map<GeneroMusical, Integer> obtenerEstadisticasGenero() {
        return cancionRepo.obtenerEstadisticasPorGenero();
    }

    /**
     * Obtiene los artistas más populares
     */
    public Map<String, Integer> obtenerArtistasMasPopulares() {
        return cancionRepo.obtenerArtistasMasPopulares();
    }

    /**
     * Exporta el catálogo completo de canciones
     */
    public boolean exportarCatalogo(String rutaArchivo) {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden exportar el catálogo.");
        }

        List<Cancion> canciones = cancionRepo.obtenerTodas();
        return CSVExporter.exportarCanciones(canciones, rutaArchivo);
    }

    /**
     * Exporta la lista de usuarios
     */
    public boolean exportarUsuarios(String rutaArchivo) {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden exportar usuarios.");
        }

        List<Usuario> usuarios = usuarioRepo.obtenerTodos();
        return CSVExporter.exportarUsuarios(usuarios, rutaArchivo);
    }

    /**
     * Exporta estadísticas del sistema
     */
    public boolean exportarEstadisticas(String rutaArchivo) {
        if (!esAdministrador()) {
            throw new SecurityException("Solo los administradores pueden exportar estadísticas.");
        }

        int totalUsuarios = obtenerTotalUsuarios();
        int totalCanciones = obtenerTotalCanciones();
        Map<GeneroMusical, Integer> estadisticasGenero = obtenerEstadisticasGenero();

        return CSVExporter.exportarEstadisticas(
                totalUsuarios,
                totalCanciones,
                estadisticasGenero,
                rutaArchivo
        );
    }
}
