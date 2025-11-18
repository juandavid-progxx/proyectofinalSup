package co.uniquindio.edu.co.Repositorio;

import co.uniquindio.edu.co.Modelo.Usuario;

public class SesionRepository {

    private static SesionRepository instancia;
    private Usuario usuarioActual;

    /**
     * Constructor privado (Singleton)
     */
    private SesionRepository() {
        this.usuarioActual = null;
    }

    /**
     * Obtiene la instancia única del repositorio
     */
    public static SesionRepository getInstancia() {
        if (instancia == null) {
            instancia = new SesionRepository();
        }
        return instancia;
    }

    /**
     * Inicia sesión con un usuario
     */
    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    /**
     * Cierra la sesión actual
     */
    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    /**
     * Obtiene el usuario actual
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Verifica si hay una sesión activa
     */
    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    /**
     * Obtiene el username del usuario actual
     */
    public String getUsernameActual() {
        return usuarioActual != null ? usuarioActual.getUsername() : null;
    }

    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean esAdministrador() {
        return usuarioActual != null &&
                usuarioActual instanceof co.uniquindio.edu.co.Modelo.Administrador;
    }
}
