package co.uniquindio.edu.co.Servicios;

import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Repositorio.CancionRepository;
import co.uniquindio.edu.co.Repositorio.SesionRepository;
import co.uniquindio.edu.co.Repositorio.UsuarioRepository;
import co.uniquindio.edu.co.Utils.CSVExporter;
import co.uniquindio.edu.co.Utils.Validaciones;
import java.util.LinkedList;
import java.util.List;

public class UsuarioService {

    private UsuarioRepository usuarioRepo;
    private CancionRepository cancionRepo;
    private SesionRepository sesionRepo;

    public UsuarioService() {
        this.usuarioRepo = UsuarioRepository.getInstancia();
        this.cancionRepo = CancionRepository.getInstancia();
        this.sesionRepo = SesionRepository.getInstancia();
    }

    /**
     * Actualiza el perfil del usuario actual
     */
    public boolean actualizarPerfil(String nombre, String password) {
        Usuario usuario = sesionRepo.getUsuarioActual();

        if (usuario == null) {
            return false;
        }

        if (Validaciones.noEsVacio(nombre)) {
            usuario.setNombre(nombre);
        }

        if (Validaciones.noEsVacio(password)) {
            usuario.setPassword(password);
        }

        return usuarioRepo.actualizarUsuario(usuario);
    }

    /**
     * Agrega una canción a favoritos del usuario actual
     */
    public boolean agregarAFavoritos(String idCancion) {
        Usuario usuario = sesionRepo.getUsuarioActual();

        if (usuario == null) {
            return false;
        }

        Cancion cancion = cancionRepo.buscarPorId(idCancion);

        if (cancion == null) {
            return false;
        }

        boolean agregado = usuario.agregarFavorito(cancion);

        if (agregado) {
            usuarioRepo.actualizarUsuario(usuario);
        }

        return agregado;
    }

    /**
     * Elimina una canción de favoritos del usuario actual
     */
    public boolean eliminarDeFavoritos(String idCancion) {
        Usuario usuario = sesionRepo.getUsuarioActual();

        if (usuario == null) {
            return false;
        }

        Cancion cancion = cancionRepo.buscarPorId(idCancion);

        if (cancion == null) {
            return false;
        }

        boolean eliminado = usuario.eliminarFavorito(cancion);

        if (eliminado) {
            usuarioRepo.actualizarUsuario(usuario);
        }

        return eliminado;
    }

    /**
     * Verifica si una canción está en favoritos
     */
    public boolean esFavorito(String idCancion) {
        Usuario usuario = sesionRepo.getUsuarioActual();

        if (usuario == null) {
            return false;
        }

        Cancion cancion = cancionRepo.buscarPorId(idCancion);

        if (cancion == null) {
            return false;
        }

        return usuario.esFavorito(cancion);
    }

    /**
     * Obtiene la lista de favoritos del usuario actual
     */
    public LinkedList<Cancion> obtenerFavoritos() {
        Usuario usuario = sesionRepo.getUsuarioActual();

        if (usuario == null) {
            return new LinkedList<>();
        }

        return usuario.getListaFavoritos();
    }

    /**
     * Exporta los favoritos del usuario a CSV
     */
    public boolean exportarFavoritos(String rutaArchivo) {
        Usuario usuario = sesionRepo.getUsuarioActual();

        if (usuario == null) {
            return false;
        }

        return CSVExporter.exportarFavoritos(usuario, rutaArchivo);
    }

    /**
     * Obtiene información del usuario actual
     */
    public Usuario obtenerUsuarioActual() {
        return sesionRepo.getUsuarioActual();
    }

    /**
     * Busca usuarios por nombre
     */
    public List<Usuario> buscarUsuarios(String nombre) {
        return usuarioRepo.buscarPorNombre(nombre);
    }

    /**
     * Obtiene todos los usuarios (excepto el actual)
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        Usuario usuarioActual = sesionRepo.getUsuarioActual();
        List<Usuario> todos = usuarioRepo.obtenerUsuariosRegulares();

        if (usuarioActual != null) {
            todos.removeIf(u -> u.getUsername().equals(usuarioActual.getUsername()));
        }

        return todos;
    }

    /**
     * Obtiene un usuario por username
     */
    public Usuario obtenerUsuarioPorUsername(String username) {
        return usuarioRepo.buscarPorUsername(username);
    }
}
