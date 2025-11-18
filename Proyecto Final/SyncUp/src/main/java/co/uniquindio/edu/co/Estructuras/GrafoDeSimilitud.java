package co.uniquindio.edu.co.Estructuras;

import co.uniquindio.edu.co.Modelo.Cancion;
import java.util.*;

public class GrafoDeSimilitud {

    private Map<Cancion, List<Arista>> adyacencias;
    private Set<Cancion> vertices;

    /**
     * Constructor del Grafo de Similitud
     */
    public GrafoDeSimilitud() {
        this.adyacencias = new HashMap<>();
        this.vertices = new HashSet<>();
    }

    /**
     * Agrega un vértice (canción) al grafo
     */
    public void agregarVertice(Cancion cancion) {
        if (!vertices.contains(cancion)) {
            vertices.add(cancion);
            adyacencias.put(cancion, new ArrayList<>());
        }
    }

    /**
     * Agrega una arista entre dos canciones con un peso (similitud)
     * Como es no dirigido, agrega la conexión en ambas direcciones
     */
    public void agregarArista(Cancion cancion1, Cancion cancion2, double similitud) {
        agregarVertice(cancion1);
        agregarVertice(cancion2);

        // Grafo no dirigido: agregar en ambas direcciones
        adyacencias.get(cancion1).add(new Arista(cancion1, cancion2, similitud));
        adyacencias.get(cancion2).add(new Arista(cancion2, cancion1, similitud));
    }

    /**
     * Obtiene los vecinos (canciones adyacentes) de una canción
     */
    public List<Arista> obtenerVecinos(Cancion cancion) {
        return adyacencias.getOrDefault(cancion, new ArrayList<>());
    }

    /**
     * Obtiene todas las canciones del grafo
     */
    public Set<Cancion> obtenerVertices() {
        return new HashSet<>(vertices);
    }

    /**
     * Verifica si una canción existe en el grafo
     */
    public boolean contieneCancion(Cancion cancion) {
        return vertices.contains(cancion);
    }

    /**
     * Obtiene el número de vértices (canciones)
     */
    public int numeroDeCanciones() {
        return vertices.size();
    }

    /**
     * Obtiene el número total de aristas
     */
    public int numeroDeAristas() {
        int total = 0;
        for (List<Arista> lista : adyacencias.values()) {
            total += lista.size();
        }
        return total / 2; // Dividir por 2 porque es no dirigido
    }

    /**
     * Elimina un vértice y todas sus aristas
     */
    public void eliminarVertice(Cancion cancion) {
        if (!vertices.contains(cancion)) {
            return;
        }

        // Eliminar todas las aristas que conectan con esta canción
        for (Cancion c : vertices) {
            if (!c.equals(cancion)) {
                List<Arista> aristas = adyacencias.get(c);
                aristas.removeIf(arista -> arista.getDestino().equals(cancion));
            }
        }

        // Eliminar el vértice
        vertices.remove(cancion);
        adyacencias.remove(cancion);
    }

    /**
     * Limpia todo el grafo
     */
    public void limpiar() {
        vertices.clear();
        adyacencias.clear();
    }

    /**
     * Obtiene las N canciones más similares a una canción dada
     */
    public List<Cancion> obtenerCancionesSimilares(Cancion cancion, int n) {
        List<Cancion> similares = new ArrayList<>();

        if (!contieneCancion(cancion)) {
            return similares;
        }

        List<Arista> vecinos = obtenerVecinos(cancion);

        // Ordenar por peso (similitud) de mayor a menor
        vecinos.sort((a1, a2) -> Double.compare(a2.getPeso(), a1.getPeso()));

        // Tomar las N más similares
        int limite = Math.min(n, vecinos.size());
        for (int i = 0; i < limite; i++) {
            similares.add(vecinos.get(i).getDestino());
        }

        return similares;
    }

    /**
     * Obtiene la similitud entre dos canciones
     */
    public double obtenerSimilitud(Cancion cancion1, Cancion cancion2) {
        if (!contieneCancion(cancion1) || !contieneCancion(cancion2)) {
            return 0.0;
        }

        List<Arista> vecinos = obtenerVecinos(cancion1);
        for (Arista arista : vecinos) {
            if (arista.getDestino().equals(cancion2)) {
                return arista.getPeso();
            }
        }

        return 0.0;
    }

    public Map<Cancion, List<Arista>> getAdyacencias() {
        return adyacencias;
    }
}
