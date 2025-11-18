package co.uniquindio.edu.co.Servicios;

import co.uniquindio.edu.co.Modelo.Cancion;

import java.util.LinkedList;

public class ColaReproduccionService {

    private LinkedList<Cancion> cola;
    private int indiceActual;

    /**
     * Constructor de la cola
     */
    public ColaReproduccionService() {
        this.cola = new LinkedList<>();
        this.indiceActual = -1;
    }

    /**
     * Agrega una canción a la cola
     */
    public void agregarACola(Cancion cancion) {
        if (cancion != null && !cola.contains(cancion)) {
            cola.add(cancion);
        }
    }

    /**
     * Agrega varias canciones a la cola
     */
    public void agregarMultiples(LinkedList<Cancion> canciones) {
        if (canciones != null) {
            for (Cancion cancion : canciones) {
                agregarACola(cancion);
            }
        }
    }

    /**
     * Obtiene la siguiente canción en la cola
     */
    public Cancion siguienteCancion() {
        if (cola.isEmpty()) {
            return null;
        }

        indiceActual++;
        if (indiceActual >= cola.size()) {
            indiceActual = 0; // Reiniciar desde el principio (modo loop)
        }

        return cola.get(indiceActual);
    }

    /**
     * Obtiene la canción anterior en la cola
     */
    public Cancion cancionAnterior() {
        if (cola.isEmpty()) {
            return null;
        }

        indiceActual--;
        if (indiceActual < 0) {
            indiceActual = cola.size() - 1;
        }

        return cola.get(indiceActual);
    }

    /**
     * Obtiene la canción actual
     */
    public Cancion obtenerCancionActual() {
        if (indiceActual >= 0 && indiceActual < cola.size()) {
            return cola.get(indiceActual);
        }
        return null;
    }

    /**
     * Obtiene una canción por índice
     */
    public Cancion obtenerCancion(int indice) {
        if (indice >= 0 && indice < cola.size()) {
            indiceActual = indice;
            return cola.get(indice);
        }
        return null;
    }

    /**
     * Elimina una canción de la cola
     */
    public boolean eliminarDelaCola(Cancion cancion) {
        boolean removido = cola.remove(cancion);

        if (removido && indiceActual >= cola.size() && indiceActual > 0) {
            indiceActual--;
        }

        return removido;
    }

    /**
     * Limpia toda la cola
     */
    public void limpiar() {
        cola.clear();
        indiceActual = -1;
    }

    /**
     * Obtiene el tamaño de la cola
     */
    public int getTamaño() {
        return cola.size();
    }

    /**
     * Obtiene toda la cola
     */
    public LinkedList<Cancion> obtenerCola() {
        return new LinkedList<>(cola);
    }

    /**
     * Obtiene el índice actual
     */
    public int getIndiceActual() {
        return indiceActual;
    }

    /**
     * Verifica si la cola está vacía
     */
    public boolean estaVacia() {
        return cola.isEmpty();
    }

    /**
     * Verifica si hay una siguiente canción
     */
    public boolean haySiguiente() {
        return !cola.isEmpty() && (indiceActual + 1 < cola.size() || !cola.isEmpty());
    }

    /**
     * Verifica si hay una canción anterior
     */
    public boolean hayAnterior() {
        return !cola.isEmpty() && indiceActual > 0;
    }
}
