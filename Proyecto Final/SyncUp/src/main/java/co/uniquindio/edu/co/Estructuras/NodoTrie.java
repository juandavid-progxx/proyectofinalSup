package co.uniquindio.edu.co.Estructuras;

import java.util.HashMap;
import java.util.Map;

public class NodoTrie {

    private Map<Character, NodoTrie> hijos;
    private boolean esFinDePalabra;
    private String palabraCompleta;

    //Constructor del nodo

    public NodoTrie() {
        this.hijos = new HashMap<>();
        this.esFinDePalabra = false;
        this.palabraCompleta = null;
    }

    public Map<Character, NodoTrie> getHijos() {
        return hijos;
    }

    public void setHijos(Map<Character, NodoTrie> hijos) {
        this.hijos = hijos;
    }

    public boolean esFinDePalabra() {
        return esFinDePalabra;
    }

    public void setFinDePalabra(boolean esFinDePalabra) {
        this.esFinDePalabra = esFinDePalabra;
    }

    public String getPalabraCompleta() {
        return palabraCompleta;
    }

    public void setPalabraCompleta(String palabraCompleta) {
        this.palabraCompleta = palabraCompleta;
    }

    //Verifica si el nodo tiene un hijo con el carácter especificado

    public boolean tieneHijo(char c) {
        return hijos.containsKey(c);
    }

    //Obtiene el hijo correspondiente al carácter

    public NodoTrie getHijo(char c) {
        return hijos.get(c);
    }

    //Agrega un hijo con el carácter especificado

    public void agregarHijo(char c, NodoTrie nodo) {
        hijos.put(c, nodo);
    }

    //Verifica si el nodo es hoja (no tiene hijos)
    public boolean esHoja() {
        return hijos.isEmpty();
    }
}
