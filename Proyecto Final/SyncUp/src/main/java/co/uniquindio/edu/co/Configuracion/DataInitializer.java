package co.uniquindio.edu.co.Configuracion;

import co.uniquindio.edu.co.Estructuras.GrafoDeSimilitud;
import co.uniquindio.edu.co.Estructuras.GrafoSocial;
import co.uniquindio.edu.co.Estructuras.TrieAutocompletado;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Repositorio.CancionRepository;
import co.uniquindio.edu.co.Repositorio.UsuarioRepository;
import co.uniquindio.edu.co.Utils.SimilitudCalculator;
import java.util.List;

public class DataInitializer {

    private CancionRepository cancionRepo;
    private UsuarioRepository usuarioRepo;
    private GrafoDeSimilitud grafoDeSimilitud;
    private GrafoSocial grafoSocial;
    private TrieAutocompletado trie;

    /**
     * Constructor
     */
    public DataInitializer() {
        this.cancionRepo = CancionRepository.getInstancia();
        this.usuarioRepo = UsuarioRepository.getInstancia();
        this.grafoDeSimilitud = new GrafoDeSimilitud();
        this.grafoSocial = new GrafoSocial();
        this.trie = new TrieAutocompletado();
    }

    /**
     * Inicializa todas las estructuras de datos
     */
    public void inicializar() {
        System.out.println("Inicializando SyncUp...");

        // ⭐ NUEVO: Restaurar favoritos antes de inicializar grafos
        usuarioRepo.restaurarFavoritos(cancionRepo);

        inicializarGrafoDeSimilitud();
        inicializarTrie();
        inicializarGrafoSocial();

        System.out.println("Inicialización completada.");
    }

    /**
     * Inicializa el Grafo de Similitud con todas las canciones
     */
    private void inicializarGrafoDeSimilitud() {
        System.out.println("Construyendo Grafo de Similitud...");

        List<Cancion> canciones = cancionRepo.obtenerTodas();

        // Agregar todos los vértices
        for (Cancion cancion : canciones) {
            grafoDeSimilitud.agregarVertice(cancion);
        }

        // Calcular similitudes y agregar aristas
        int aristasCreadas = 0;
        for (int i = 0; i < canciones.size(); i++) {
            Cancion c1 = canciones.get(i);

            for (int j = i + 1; j < canciones.size(); j++) {
                Cancion c2 = canciones.get(j);

                double similitud = SimilitudCalculator.calcularSimilitud(c1, c2);

                // Solo agregar arista si la similitud supera el umbral mínimo
                if (similitud >= AppConfig.UMBRAL_SIMILITUD_MINIMA) {
                    grafoDeSimilitud.agregarArista(c1, c2, similitud);
                    aristasCreadas++;
                }
            }
        }

        System.out.println("Grafo de Similitud creado: " +
                grafoDeSimilitud.numeroDeCanciones() + " canciones, " +
                aristasCreadas + " conexiones.");
    }

    /**
     * Inicializa el Trie con todos los títulos de canciones
     */
    private void inicializarTrie() {
        System.out.println("Construyendo Trie de Autocompletado...");

        List<Cancion> canciones = cancionRepo.obtenerTodas();

        for (Cancion cancion : canciones) {
            trie.insertar(cancion.getTitulo());
        }

        System.out.println("Trie creado con " + trie.contarPalabras() + " títulos.");
    }

    /**
     * Inicializa el Grafo Social con todos los usuarios
     */
    private void inicializarGrafoSocial() {
        System.out.println("Construyendo Grafo Social...");

        List<Usuario> usuarios = usuarioRepo.obtenerTodos();

        // Agregar todos los usuarios al grafo
        for (Usuario usuario : usuarios) {
            grafoSocial.agregarUsuario(usuario.getUsername());

            // Agregar sus conexiones
            for (String seguido : usuario.getUsuariosSeguidos()) {
                if (usuarioRepo.existeUsername(seguido)) {
                    grafoSocial.agregarConexion(usuario.getUsername(), seguido);
                }
            }
        }
        System.out.println("Grafo Social creado con " + grafoSocial.numeroDeUsuarios() + " usuarios.");
    }

    /**
     * Actualiza el Grafo de Similitud (agregar nueva canción)
     */
    public void actualizarGrafoConCancion(Cancion nuevaCancion) {
        grafoDeSimilitud.agregarVertice(nuevaCancion);

        List<Cancion> todasLasCanciones = cancionRepo.obtenerTodas();

        for (Cancion cancion : todasLasCanciones) {
            if (!cancion.equals(nuevaCancion)) {
                double similitud = SimilitudCalculator.calcularSimilitud(nuevaCancion, cancion);

                if (similitud >= AppConfig.UMBRAL_SIMILITUD_MINIMA) {
                    grafoDeSimilitud.agregarArista(nuevaCancion, cancion, similitud);
                }
            }
        }
    }

    /**
     * Actualiza el Trie con un nuevo título
     */
    public void actualizarTrieConTitulo(String titulo) {
        trie.insertar(titulo);
    }

    /**
     * Elimina una canción del grafo y del trie
     */
    public void eliminarCancionDeEstructuras(Cancion cancion) {
        grafoDeSimilitud.eliminarVertice(cancion);
        trie.eliminar(cancion.getTitulo());
    }

    /**
     * Actualiza el Grafo Social con un nuevo usuario
     */
    public void actualizarGrafoConUsuario(Usuario usuario) {
        grafoSocial.agregarUsuario(usuario.getUsername());
    }

    /**
     * Elimina un usuario del grafo social
     */
    public void eliminarUsuarioDeGrafo(String username) {
        grafoSocial.eliminarUsuario(username);
    }

    /**
     * Reconstruye todas las estructuras desde cero
     */
    public void reconstruirEstructuras() {
        grafoDeSimilitud.limpiar();
        trie.limpiar();
        grafoSocial.limpiar();
        inicializar();
    }

    // Getters
    public GrafoDeSimilitud getGrafoDeSimilitud() {
        return grafoDeSimilitud;
    }

    public GrafoSocial getGrafoSocial() {
        return grafoSocial;
    }

    public TrieAutocompletado getTrie() {
        return trie;
    }
}
