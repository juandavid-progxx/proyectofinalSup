package co.uniquindio.edu.co.Servicios;

import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Estructuras.GrafoDeSimilitud;
import co.uniquindio.edu.co.Modelo.Cancion;

import java.util.*;

public class RadioService {

    private GrafoDeSimilitud grafoDeSimilitud;
    private CancionService cancionService;
    private DataInitializer dataInitializer;

    // Cola de reproducción actual
    private LinkedList<Cancion> colaReproduccion;
    private Set<Cancion> reproducidas;
    private Cancion cancionActual;
    private int indiceActual;

    // Configuración de la radio
    private static final int CANCIONES_SIMILARES_POR_CICLO = 5;
    private static final int TAMANIO_INICIAL_COLA = 20;

    public RadioService(DataInitializer dataInitializer) {
        this.dataInitializer = dataInitializer;
        this.grafoDeSimilitud = dataInitializer.getGrafoDeSimilitud();
        this.cancionService = new CancionService();
        this.colaReproduccion = new LinkedList<>();
        this.reproducidas = new HashSet<>();
        this.indiceActual = 0;
    }

    /**
     * ⭐ ACTUALIZADO: Inicia una radio a partir de una canción
     * RF-006: Genera cola de reproducción con temas similares
     * @param cancionInicial Canción base para la radio
     */
    public void iniciarRadio(Cancion cancionInicial) {
        if (cancionInicial == null) {
            System.err.println("❌ Error: Canción inicial no puede ser nula");
            return;
        }

        // Limpiar cola anterior
        colaReproduccion.clear();
        reproducidas.clear();
        indiceActual = 0;

        // ⭐ Agregar la canción seleccionada primero
        cancionActual = cancionInicial;
        colaReproduccion.add(cancionInicial);
        reproducidas.add(cancionInicial);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("📻 RADIO INICIADA (RF-006)");
        System.out.println("=".repeat(60));
        System.out.println("🎵 Canción base: " + cancionInicial.getTitulo() + " - " + cancionInicial.getArtista());

        // Llenar la cola con canciones similares
        llenarColaReproduccion(cancionInicial);

        System.out.println("📊 Tamaño de cola: " + colaReproduccion.size() + " canciones");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Llena la cola de reproducción con canciones similares
     */
    private void llenarColaReproduccion(Cancion cancionBase) {
        Set<Cancion> enCola = new HashSet<>(colaReproduccion);

        // Agregar canciones similares a la canción base
        List<Cancion> similares = grafoDeSimilitud.obtenerCancionesSimilares(
                cancionBase,
                CANCIONES_SIMILARES_POR_CICLO
        );

        for (Cancion similar : similares) {
            if (!enCola.contains(similar) && colaReproduccion.size() < TAMANIO_INICIAL_COLA) {
                colaReproduccion.add(similar);
                enCola.add(similar);
            }
        }

        // Si aún no se llena la cola, agregar aleatorias
        if (colaReproduccion.size() < TAMANIO_INICIAL_COLA) {
            List<Cancion> todasLasCanciones = cancionService.obtenerTodasLasCanciones();
            Collections.shuffle(todasLasCanciones);

            for (Cancion cancion : todasLasCanciones) {
                if (!enCola.contains(cancion) && colaReproduccion.size() < TAMANIO_INICIAL_COLA) {
                    colaReproduccion.add(cancion);
                    enCola.add(cancion);
                }
            }
        }
    }

    /**
     * ⭐ NUEVO: Agrega una canción a la cola de reproducción
     */
    public void agregarALaCola(Cancion cancion) {
        if (cancion != null && !colaReproduccion.contains(cancion)) {
            colaReproduccion.add(cancion);
            System.out.println("✅ Canción agregada a la cola: " + cancion.getTitulo());
        }
    }

    /**
     * Obtiene la siguiente canción en la cola
     * Si se llega al final, regenera la cola
     */
    public Cancion obtenerSiguiente() {
        if (colaReproduccion.isEmpty()) {
            System.err.println("❌ Cola de reproducción vacía");
            return null;
        }

        indiceActual++;

        // Si llegamos al final, regenerar cola
        if (indiceActual >= colaReproduccion.size()) {
            regenerarCola();
        }

        if (indiceActual < colaReproduccion.size()) {
            cancionActual = colaReproduccion.get(indiceActual);
            reproducidas.add(cancionActual);
            System.out.println("🎵 Siguiente: " + cancionActual.getTitulo());
            return cancionActual;
        }

        return null;
    }

    /**
     * Obtiene la canción anterior en la cola
     */
    public Cancion obtenerAnterior() {
        if (colaReproduccion.isEmpty()) {
            System.err.println("❌ Cola de reproducción vacía");
            return null;
        }

        if (indiceActual > 0) {
            indiceActual--;
            cancionActual = colaReproduccion.get(indiceActual);
            System.out.println("⏮️  Anterior: " + cancionActual.getTitulo());
            return cancionActual;
        } else {
            System.out.println("⚠️  Ya estás en la primera canción");
            return cancionActual;
        }
    }

    /**
     * Obtiene la canción actual
     */
    public Cancion obtenerActual() {
        return cancionActual;
    }

    /**
     * Regenera la cola cuando se llega al final
     * Mantiene variedad usando el grafo de similitud
     */
    private void regenerarCola() {
        System.out.println("🔄 Regenerando cola de reproducción...");

        colaReproduccion.clear();
        indiceActual = 0;

        // Seleccionar una canción aleatoria del historial de reproducidas
        List<Cancion> reproduciadasList = new ArrayList<>(reproducidas);
        if (!reproduciadasList.isEmpty()) {
            Cancion cancionBase = reproduciadasList.get(
                    new Random().nextInt(reproduciadasList.size())
            );
            llenarColaReproduccion(cancionBase);
        }
    }

    /**
     * Obtiene toda la cola de reproducción
     */
    public List<Cancion> obtenerCola() {
        return new ArrayList<>(colaReproduccion);
    }

    /**
     * Obtiene el índice actual en la cola
     */
    public int obtenerIndiceActual() {
        return indiceActual;
    }

    /**
     * Obtiene el tamaño total de la cola
     */
    public int obtenerTamañoCola() {
        return colaReproduccion.size();
    }

    /**
     * Obtiene las canciones reproducidas
     */
    public Set<Cancion> obtenerReproducidas() {
        return new HashSet<>(reproducidas);
    }

    /**
     * Verifica si la radio está activa
     */
    public boolean estaActiva() {
        return !colaReproduccion.isEmpty() && cancionActual != null;
    }

    /**
     * Obtiene la similitud entre dos canciones
     */
    public double obtenerSimilitud(Cancion c1, Cancion c2) {
        return grafoDeSimilitud.obtenerSimilitud(c1, c2);
    }

    /**
     * Obtiene canciones similares a la actual
     */
    public List<Cancion> obtenerCancionesSimilares(int cantidad) {
        if (cancionActual == null) {
            return new ArrayList<>();
        }
        return grafoDeSimilitud.obtenerCancionesSimilares(cancionActual, cantidad);
    }

    /**
     * Cambia la canción actual por una de la cola
     */
    public Cancion saltarA(int indice) {
        if (indice >= 0 && indice < colaReproduccion.size()) {
            indiceActual = indice;
            cancionActual = colaReproduccion.get(indice);
            reproducidas.add(cancionActual);
            System.out.println("⏭️  Saltando a: " + cancionActual.getTitulo());
            return cancionActual;
        }
        System.err.println("❌ Índice fuera de rango");
        return null;
    }

    /**
     * Detiene la radio
     */
    public void detenerRadio() {
        colaReproduccion.clear();
        reproducidas.clear();
        cancionActual = null;
        indiceActual = 0;
        System.out.println("⏹️  Radio detenida");
    }

    /**
     * Obtiene información de la radio actual
     */
    public String obtenerInfo() {
        if (!estaActiva()) {
            return "No hay radio activa";
        }

        return "📻 Radio Activa\n" +
                "🎵 Actual: " + cancionActual.getTitulo() + "\n" +
                "📊 Posición: " + (indiceActual + 1) + "/" + colaReproduccion.size() + "\n" +
                "🔄 Reproducidas: " + reproducidas.size() + " canciones";
    }
}