package co.uniquindio.edu.co.Servicios;

import co.uniquindio.edu.co.Configuracion.AppConfig;
import co.uniquindio.edu.co.Configuracion.DataInitializer;
import co.uniquindio.edu.co.Estructuras.BFS;
import co.uniquindio.edu.co.Estructuras.GrafoSocial;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Repositorio.SesionRepository;
import co.uniquindio.edu.co.Repositorio.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SocialService {

    private UsuarioRepository usuarioRepo;
    private SesionRepository sesionRepo;
    private GrafoSocial grafoSocial;

    public SocialService(DataInitializer dataInitializer) {
        this.usuarioRepo = UsuarioRepository.getInstancia();
        this.sesionRepo = SesionRepository.getInstancia();
        this.grafoSocial = dataInitializer.getGrafoSocial();
    }

    /**
     * Sigue a un usuario
     */
    public boolean seguirUsuario(String usernameSeguir) {
        Usuario usuarioActual = sesionRepo.getUsuarioActual();

        if (usuarioActual == null) {
            return false;
        }

        // Verificar que el usuario a seguir existe
        Usuario usuarioSeguir = usuarioRepo.buscarPorUsername(usernameSeguir);
        if (usuarioSeguir == null) {
            return false;
        }

        // No puede seguirse a sí mismo
        if (usuarioActual.getUsername().equals(usernameSeguir)) {
            return false;
        }

        // Seguir en el modelo de usuario
        boolean seguido = usuarioActual.seguirUsuario(usernameSeguir);

        if (seguido) {
            // Actualizar en el repositorio
            usuarioRepo.actualizarUsuario(usuarioActual);

            // Actualizar en el grafo social
            grafoSocial.agregarConexion(usuarioActual.getUsername(), usernameSeguir);
        }

        return seguido;
    }

    /**
     * Deja de seguir a un usuario
     */
    public boolean dejarDeSeguir(String usernameDejarDeSeguir) {
        Usuario usuarioActual = sesionRepo.getUsuarioActual();

        if (usuarioActual == null) {
            return false;
        }

        boolean dejoDeSeguir = usuarioActual.dejarDeSeguir(usernameDejarDeSeguir);

        if (dejoDeSeguir) {
            // Actualizar en el repositorio
            usuarioRepo.actualizarUsuario(usuarioActual);

            // Actualizar en el grafo social
            grafoSocial.eliminarConexion(usuarioActual.getUsername(), usernameDejarDeSeguir);
        }

        return dejoDeSeguir;
    }

    /**
     * Verifica si el usuario actual está siguiendo a otro usuario
     */
    public boolean estaSiguiendo(String username) {
        Usuario usuarioActual = sesionRepo.getUsuarioActual();

        if (usuarioActual == null) {
            return false;
        }

        return usuarioActual.siguiendoA(username);
    }

    /**
     * Obtiene la lista de usuarios que el usuario actual está siguiendo
     */
    public List<Usuario> obtenerUsuariosSeguidos() {
        Usuario usuarioActual = sesionRepo.getUsuarioActual();

        if (usuarioActual == null) {
            return new ArrayList<>();
        }

        List<Usuario> seguidos = new ArrayList<>();

        for (String username : usuarioActual.getUsuariosSeguidos()) {
            Usuario usuario = usuarioRepo.buscarPorUsername(username);
            if (usuario != null) {
                seguidos.add(usuario);
            }
        }

        return seguidos;
    }

    /**
     * Obtiene sugerencias de usuarios para seguir
     */
    public List<Usuario> obtenerSugerenciasUsuarios() {
        Usuario usuarioActual = sesionRepo.getUsuarioActual();

        if (usuarioActual == null) {
            return new ArrayList<>();
        }

        // Usar BFS para encontrar sugerencias basadas en amigos en común
        List<String> sugerenciasUsernames = BFS.obtenerSugerencias(
                grafoSocial,
                usuarioActual.getUsername(),
                AppConfig.SUGERENCIAS_USUARIOS
        );

        // Convertir usernames a objetos Usuario
        List<Usuario> sugerencias = new ArrayList<>();

        for (String username : sugerenciasUsernames) {
            Usuario usuario = usuarioRepo.buscarPorUsername(username);
            if (usuario != null) {
                sugerencias.add(usuario);
            }
        }

        // Si no hay suficientes sugerencias, agregar usuarios aleatorios
        if (sugerencias.size() < AppConfig.SUGERENCIAS_USUARIOS) {
            List<Usuario> todosLosUsuarios = usuarioRepo.obtenerUsuariosRegulares();
            todosLosUsuarios.removeIf(u ->
                    u.getUsername().equals(usuarioActual.getUsername()) ||
                            usuarioActual.siguiendoA(u.getUsername()) ||
                            sugerencias.contains(u)
            );

            int faltantes = AppConfig.SUGERENCIAS_USUARIOS - sugerencias.size();
            int limite = Math.min(faltantes, todosLosUsuarios.size());

            for (int i = 0; i < limite; i++) {
                sugerencias.add(todosLosUsuarios.get(i));
            }
        }

        return sugerencias;
    }

    /**
     * Obtiene amigos de amigos (usuarios a 2 grados de separación)
     */
    public List<Usuario> obtenerAmigosDeAmigos() {
        Usuario usuarioActual = sesionRepo.getUsuarioActual();

        if (usuarioActual == null) {
            return new ArrayList<>();
        }

        Set<String> amigosDeAmigosUsernames = BFS.encontrarAmigosDeAmigos(
                grafoSocial,
                usuarioActual.getUsername()
        );

        List<Usuario> amigosDeAmigos = new ArrayList<>();

        for (String username : amigosDeAmigosUsernames) {
            Usuario usuario = usuarioRepo.buscarPorUsername(username);
            if (usuario != null) {
                amigosDeAmigos.add(usuario);
            }
        }

        return amigosDeAmigos;
    }

    /**
     * Calcula la distancia entre el usuario actual y otro usuario
     */
    public int calcularDistanciaConUsuario(String username) {
        Usuario usuarioActual = sesionRepo.getUsuarioActual();

        if (usuarioActual == null) {
            return -1;
        }

        return BFS.calcularDistancia(
                grafoSocial,
                usuarioActual.getUsername(),
                username
        );
    }

    /**
     * Obtiene el número de conexiones (amigos) del usuario actual
     */
    public int obtenerNumeroDeConexiones() {
        Usuario usuarioActual = sesionRepo.getUsuarioActual();

        if (usuarioActual == null) {
            return 0;
        }

        return usuarioActual.getCantidadSeguidos();
    }

    /**
     * Busca usuarios por nombre (excluyendo al usuario actual y ya seguidos)
     */
    public List<Usuario> buscarUsuariosParaSeguir(String nombre) {
        Usuario usuarioActual = sesionRepo.getUsuarioActual();

        if (usuarioActual == null) {
            return new ArrayList<>();
        }

        List<Usuario> usuarios = usuarioRepo.buscarPorNombre(nombre);

        // Filtrar: no incluir al usuario actual ni a los que ya sigue
        usuarios.removeIf(u ->
                u.getUsername().equals(usuarioActual.getUsername()) ||
                        usuarioActual.siguiendoA(u.getUsername())
        );

        return usuarios;
    }
}
