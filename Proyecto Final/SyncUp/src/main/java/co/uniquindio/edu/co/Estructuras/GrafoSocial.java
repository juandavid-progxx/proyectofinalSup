package co.uniquindio.edu.co.Estructuras;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GrafoSocial {

    private Map<String, Set<String>> adyacencias; // username -> set de usernames seguidos

    /**
     * Constructor del Grafo Social
     */
    public GrafoSocial() {
        this.adyacencias = new HashMap<>();
    }

    /**
     * Agrega un usuario al grafo
     */
    public void agregarUsuario(String username) {
        if (!adyacencias.containsKey(username)) {
            adyacencias.put(username, new HashSet<>());
        }
    }

    /**
     * Agrega una conexión entre dos usuarios (bidireccional)
     */
    public void agregarConexion(String usuario1, String usuario2) {
        agregarUsuario(usuario1);
        agregarUsuario(usuario2);

        adyacencias.get(usuario1).add(usuario2);
        adyacencias.get(usuario2).add(usuario1);
    }

    /**
     * Elimina la conexión entre dos usuarios
     */
    public void eliminarConexion(String usuario1, String usuario2) {
        if (adyacencias.containsKey(usuario1)) {
            adyacencias.get(usuario1).remove(usuario2);
        }
        if (adyacencias.containsKey(usuario2)) {
            adyacencias.get(usuario2).remove(usuario1);
        }
    }

    /**
     * Verifica si dos usuarios están conectados
     */
    public boolean estanConectados(String usuario1, String usuario2) {
        if (!adyacencias.containsKey(usuario1)) {
            return false;
        }
        return adyacencias.get(usuario1).contains(usuario2);
    }

    /**
     * Obtiene los amigos directos de un usuario
     */
    public Set<String> obtenerAmigos(String username) {
        return new HashSet<>(adyacencias.getOrDefault(username, new HashSet<>()));
    }

    /**
     * Obtiene el número de conexiones de un usuario
     */
    public int obtenerNumeroDeAmigos(String username) {
        return adyacencias.getOrDefault(username, new HashSet<>()).size();
    }

    /**
     * Elimina un usuario y todas sus conexiones
     */
    public void eliminarUsuario(String username) {
        if (!adyacencias.containsKey(username)) {
            return;
        }

        // Eliminar de las listas de adyacencia de otros usuarios
        Set<String> amigos = new HashSet<>(adyacencias.get(username));
        for (String amigo : amigos) {
            adyacencias.get(amigo).remove(username);
        }

        // Eliminar el usuario
        adyacencias.remove(username);
    }

    /**
     * Obtiene todos los usuarios del grafo
     */
    public Set<String> obtenerTodosLosUsuarios() {
        return new HashSet<>(adyacencias.keySet());
    }

    /**
     * Obtiene el número total de usuarios
     */
    public int numeroDeUsuarios() {
        return adyacencias.size();
    }

    /**
     * Limpia todo el grafo
     */
    public void limpiar() {
        adyacencias.clear();
    }

    /**
     * Verifica si un usuario existe en el grafo
     */
    public boolean contieneUsuario(String username) {
        return adyacencias.containsKey(username);
    }

    public Map<String, Set<String>> getAdyacencias() {
        return adyacencias;
    }
}
