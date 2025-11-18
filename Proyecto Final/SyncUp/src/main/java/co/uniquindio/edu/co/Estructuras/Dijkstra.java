package co.uniquindio.edu.co.Estructuras;

import co.uniquindio.edu.co.Modelo.Cancion;
import java.util.*;

public class Dijkstra {

    public static List<Cancion> encontrarCancionesSimilares(GrafoDeSimilitud grafo, Cancion inicio, int limite) {
        if (!grafo.contieneCancion(inicio)) {
            return new ArrayList<>();
        }

        // Mapa de distancias (usamos similitud invertida: 1 - similitud para usar min-heap)
        Map<Cancion, Double> distancias = new HashMap<>();
        Map<Cancion, Double> similitudes = new HashMap<>();
        Set<Cancion> visitados = new HashSet<>();

        // Priority Queue (min-heap por distancia, max similitud)
        PriorityQueue<NodoDijkstra> cola = new PriorityQueue<>();

        // Inicializar distancias
        for (Cancion cancion : grafo.obtenerVertices()) {
            if (cancion.equals(inicio)) {
                distancias.put(cancion, 0.0);
                similitudes.put(cancion, 1.0);
            } else {
                distancias.put(cancion, Double.MAX_VALUE);
                similitudes.put(cancion, 0.0);
            }
        }

        cola.offer(new NodoDijkstra(inicio, 0.0));

        while (!cola.isEmpty()) {
            NodoDijkstra nodoActual = cola.poll();
            Cancion cancionActual = nodoActual.cancion;

            if (visitados.contains(cancionActual)) {
                continue;
            }

            visitados.add(cancionActual);

            // Explorar vecinos
            List<Arista> vecinos = grafo.obtenerVecinos(cancionActual);
            for (Arista arista : vecinos) {
                Cancion vecino = arista.getDestino();

                if (visitados.contains(vecino)) {
                    continue;
                }

                // Calcular nueva distancia (invertimos la similitud)
                double nuevaDistancia = distancias.get(cancionActual) + (1.0 - arista.getPeso());
                double nuevaSimilitud = similitudes.get(cancionActual) * arista.getPeso();

                // Si encontramos una mejor ruta
                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    similitudes.put(vecino, arista.getPeso());
                    cola.offer(new NodoDijkstra(vecino, nuevaDistancia));
                }
            }
        }

        // Crear lista de resultados ordenada por similitud
        List<Map.Entry<Cancion, Double>> resultados = new ArrayList<>(similitudes.entrySet());
        resultados.removeIf(entry -> entry.getKey().equals(inicio)); // Excluir la canción de inicio

        // Ordenar por similitud descendente
        resultados.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        // Retornar solo las N canciones más similares
        List<Cancion> cancionesSimilares = new ArrayList<>();
        int count = Math.min(limite, resultados.size());
        for (int i = 0; i < count; i++) {
            cancionesSimilares.add(resultados.get(i).getKey());
        }

        return cancionesSimilares;
    }

    /**
     * Clase auxiliar para representar un nodo en la cola de prioridad
     */
    private static class NodoDijkstra implements Comparable<NodoDijkstra> {
        Cancion cancion;
        double distancia;

        NodoDijkstra(Cancion cancion, double distancia) {
            this.cancion = cancion;
            this.distancia = distancia;
        }

        @Override
        public int compareTo(NodoDijkstra otro) {
            return Double.compare(this.distancia, otro.distancia);
        }
    }

    /**
     * Encuentra el camino de mayor similitud entre dos canciones
     */
    public static List<Cancion> encontrarCamino(GrafoDeSimilitud grafo, Cancion inicio, Cancion destino) {
        if (!grafo.contieneCancion(inicio) || !grafo.contieneCancion(destino)) {
            return new ArrayList<>();
        }

        Map<Cancion, Double> distancias = new HashMap<>();
        Map<Cancion, Cancion> predecesores = new HashMap<>();
        Set<Cancion> visitados = new HashSet<>();
        PriorityQueue<NodoDijkstra> cola = new PriorityQueue<>();

        // Inicializar
        for (Cancion cancion : grafo.obtenerVertices()) {
            distancias.put(cancion, cancion.equals(inicio) ? 0.0 : Double.MAX_VALUE);
        }

        cola.offer(new NodoDijkstra(inicio, 0.0));

        while (!cola.isEmpty()) {
            NodoDijkstra nodoActual = cola.poll();
            Cancion cancionActual = nodoActual.cancion;

            if (cancionActual.equals(destino)) {
                break; // Llegamos al destino
            }

            if (visitados.contains(cancionActual)) {
                continue;
            }

            visitados.add(cancionActual);

            for (Arista arista : grafo.obtenerVecinos(cancionActual)) {
                Cancion vecino = arista.getDestino();

                if (visitados.contains(vecino)) {
                    continue;
                }

                double nuevaDistancia = distancias.get(cancionActual) + (1.0 - arista.getPeso());

                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    predecesores.put(vecino, cancionActual);
                    cola.offer(new NodoDijkstra(vecino, nuevaDistancia));
                }
            }
        }

        // Reconstruir el camino
        List<Cancion> camino = new ArrayList<>();
        Cancion actual = destino;

        while (actual != null) {
            camino.add(0, actual);
            actual = predecesores.get(actual);
        }

        // Verificar que el camino sea válido
        if (camino.isEmpty() || !camino.get(0).equals(inicio)) {
            return new ArrayList<>();
        }

        return camino;
    }
}
