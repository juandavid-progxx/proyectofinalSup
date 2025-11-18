package co.uniquindio.edu.co.Estructuras;

import java.util.ArrayList;
import java.util.List;

public class TrieAutocompletado {

    private NodoTrie raiz;

    /**
     * Constructor del Trie
     */
    public TrieAutocompletado() {
        this.raiz = new NodoTrie();
    }

    /**
     * Inserta una palabra en el Trie
     * @param palabra Palabra a insertar
     */
    public void insertar(String palabra) {
        if (palabra == null || palabra.isEmpty()) {
            return;
        }

        NodoTrie nodoActual = raiz;
        String palabraLower = palabra.toLowerCase();

        for (char c : palabraLower.toCharArray()) {
            if (!nodoActual.tieneHijo(c)) {
                nodoActual.agregarHijo(c, new NodoTrie());
            }
            nodoActual = nodoActual.getHijo(c);
        }

        nodoActual.setFinDePalabra(true);
        nodoActual.setPalabraCompleta(palabra);
    }

    /**
     * Busca todas las palabras que comienzan con el prefijo dado
     * @param prefijo Prefijo a buscar
     * @return Lista de palabras que comienzan con el prefijo
     */
    public List<String> buscarPorPrefijo(String prefijo) {
        List<String> resultados = new ArrayList<>();

        if (prefijo == null || prefijo.isEmpty()) {
            return resultados;
        }

        String prefijoLower = prefijo.toLowerCase();
        NodoTrie nodoActual = raiz;

        // Navegar hasta el nodo que representa el prefijo
        for (char c : prefijoLower.toCharArray()) {
            if (!nodoActual.tieneHijo(c)) {
                return resultados; // Prefijo no encontrado
            }
            nodoActual = nodoActual.getHijo(c);
        }

        // Recolectar todas las palabras a partir del nodo del prefijo
        recolectarPalabras(nodoActual, resultados);
        return resultados;
    }

    /**
     * Método auxiliar recursivo para recolectar palabras
     */
    private void recolectarPalabras(NodoTrie nodo, List<String> resultados) {
        if (nodo == null) {
            return;
        }

        if (nodo.esFinDePalabra()) {
            resultados.add(nodo.getPalabraCompleta());
        }

        for (NodoTrie hijo : nodo.getHijos().values()) {
            recolectarPalabras(hijo, resultados);
        }
    }

    /**
     * Verifica si una palabra existe en el Trie
     * @param palabra Palabra a verificar
     * @return true si existe, false en caso contrario
     */
    public boolean contiene(String palabra) {
        if (palabra == null || palabra.isEmpty()) {
            return false;
        }

        NodoTrie nodoActual = raiz;
        String palabraLower = palabra.toLowerCase();

        for (char c : palabraLower.toCharArray()) {
            if (!nodoActual.tieneHijo(c)) {
                return false;
            }
            nodoActual = nodoActual.getHijo(c);
        }

        return nodoActual.esFinDePalabra();
    }

    /**
     * Elimina una palabra del Trie
     * @param palabra Palabra a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean eliminar(String palabra) {
        if (palabra == null || palabra.isEmpty()) {
            return false;
        }

        String palabraLower = palabra.toLowerCase();
        return eliminarRecursivo(raiz, palabraLower, 0);
    }

    /**
     * Método auxiliar recursivo para eliminar una palabra
     */
    private boolean eliminarRecursivo(NodoTrie nodo, String palabra, int indice) {
        if (nodo == null) {
            return false;
        }

        // Si llegamos al final de la palabra
        if (indice == palabra.length()) {
            if (!nodo.esFinDePalabra()) {
                return false; // La palabra no existe
            }
            nodo.setFinDePalabra(false);
            nodo.setPalabraCompleta(null);
            return nodo.getHijos().isEmpty(); // Retorna true si se puede eliminar el nodo
        }

        char c = palabra.charAt(indice);
        NodoTrie siguienteNodo = nodo.getHijo(c);

        if (siguienteNodo == null) {
            return false; // La palabra no existe
        }

        boolean debeEliminarHijo = eliminarRecursivo(siguienteNodo, palabra, indice + 1);

        if (debeEliminarHijo) {
            nodo.getHijos().remove(c);
            return !nodo.esFinDePalabra() && nodo.getHijos().isEmpty();
        }

        return false;
    }

    /**
     * Limpia todo el Trie
     */
    public void limpiar() {
        this.raiz = new NodoTrie();
    }

    /**
     * Obtiene el número de palabras en el Trie
     */
    public int contarPalabras() {
        return contarPalabrasRecursivo(raiz);
    }

    /**
     * Método auxiliar recursivo para contar palabras
     */
    private int contarPalabrasRecursivo(NodoTrie nodo) {
        if (nodo == null) {
            return 0;
        }

        int cuenta = nodo.esFinDePalabra() ? 1 : 0;

        for (NodoTrie hijo : nodo.getHijos().values()) {
            cuenta += contarPalabrasRecursivo(hijo);
        }

        return cuenta;
    }
}
