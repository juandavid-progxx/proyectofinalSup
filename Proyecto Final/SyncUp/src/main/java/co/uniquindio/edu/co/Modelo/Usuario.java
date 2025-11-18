package co.uniquindio.edu.co.Modelo;

import java.util.LinkedList;
import java.util.Objects;

public class Usuario {

    private String username;
    private String password;
    private String nombre;
    private LinkedList<Cancion> listaFavoritos;
    private LinkedList<String> usuariosSeguidos; // usernames de usuarios que sigue

    /**
     * Constructor completo de Usuario
     */
    public Usuario(String username, String password, String nombre) {
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.listaFavoritos = new LinkedList<>();
        this.usuariosSeguidos = new LinkedList<>();
    }

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LinkedList<Cancion> getListaFavoritos() {
        return listaFavoritos;
    }

    public void setListaFavoritos(LinkedList<Cancion> listaFavoritos) {
        this.listaFavoritos = listaFavoritos;
    }

    public LinkedList<String> getUsuariosSeguidos() {
        return usuariosSeguidos;
    }

    public void setUsuariosSeguidos(LinkedList<String> usuariosSeguidos) {
        this.usuariosSeguidos = usuariosSeguidos;
    }

    /**
     * Agrega una canción a favoritos si no existe
     */
    public boolean agregarFavorito(Cancion cancion) {
        if (!listaFavoritos.contains(cancion)) {
            listaFavoritos.add(cancion);
            return true;
        }
        return false;
    }

    /**
     * Elimina una canción de favoritos
     */
    public boolean eliminarFavorito(Cancion cancion) {
        return listaFavoritos.remove(cancion);
    }

    /**
     * Verifica si una canción está en favoritos
     */
    public boolean esFavorito(Cancion cancion) {
        return listaFavoritos.contains(cancion);
    }

    /**
     * Sigue a un usuario
     */
    public boolean seguirUsuario(String username) {
        if (!usuariosSeguidos.contains(username) && !this.username.equals(username)) {
            usuariosSeguidos.add(username);
            return true;
        }
        return false;
    }

    /**
     * Deja de seguir a un usuario
     */
    public boolean dejarDeSeguir(String username) {
        return usuariosSeguidos.remove(username);
    }

    /**
     * Verifica si sigue a un usuario
     */
    public boolean siguiendoA(String username) {
        return usuariosSeguidos.contains(username);
    }

    /**
     * Obtiene el número de usuarios que sigue
     */
    public int getCantidadSeguidos() {
        return usuariosSeguidos.size();
    }

    /**
     * Obtiene el número de canciones favoritas
     */
    public int getCantidadFavoritos() {
        return listaFavoritos.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(username, usuario.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return nombre + " (@" + username + ")";
    }
}
