package co.uniquindio.edu.co.Servicios;

import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Repositorio.SesionRepository;
import co.uniquindio.edu.co.Repositorio.UsuarioRepository;
import co.uniquindio.edu.co.Utils.PasswordUtils;
import co.uniquindio.edu.co.Utils.Validaciones;

public class AutenticacionService {

    private UsuarioRepository usuarioRepo;
    private SesionRepository sesionRepo;

    public AutenticacionService() {

    this.usuarioRepo = UsuarioRepository.getInstancia();
    this.sesionRepo = SesionRepository.getInstancia();
}

/**
 * Inicia sesión con username y password
 * @return Usuario si las credenciales son correctas, null en caso contrario
 */
public Usuario iniciarSesion(String username, String password) {
    if (!Validaciones.noEsVacio(username) || !Validaciones.noEsVacio(password)) {
        return null;
    }

    Usuario usuario = usuarioRepo.buscarPorUsername(username);

    if (usuario == null) {
        return null;
    }

    // Verificar password (en este caso comparación simple, pero podría usar hash)
    if (usuario.getPassword().equals(password)) {
        sesionRepo.iniciarSesion(usuario);
        return usuario;
    }

    return null;
}

/**
 * Registra un nuevo usuario
 * @return true si se registró exitosamente, false en caso contrario
 */
public boolean registrarUsuario(String username, String password, String nombre) {
    // Validar datos
    if (!Validaciones.esUsernameValido(username)) {
        throw new IllegalArgumentException("Username inválido. Debe tener entre 3 y 20 caracteres alfanuméricos.");
    }

    if (!PasswordUtils.esPasswordValida(password)) {
        throw new IllegalArgumentException("Password inválida. Debe tener al menos 6 caracteres.");
    }

    if (!Validaciones.noEsVacio(nombre)) {
        throw new IllegalArgumentException("El nombre no puede estar vacío.");
    }

    // Verificar que el username no exista
    if (usuarioRepo.existeUsername(username)) {
        throw new IllegalArgumentException("El username ya está en uso.");
    }

    // Crear y registrar usuario
    Usuario nuevoUsuario = new Usuario(username, password, nombre);
    return usuarioRepo.registrarUsuario(nuevoUsuario);
}

/**
 * Cierra la sesión actual
 */
public void cerrarSesion() {
    sesionRepo.cerrarSesion();
}

/**
 * Obtiene el usuario actual
 */
public Usuario getUsuarioActual() {
    return sesionRepo.getUsuarioActual();
}

/**
 * Verifica si hay una sesión activa
 */
public boolean haySesionActiva() {
    return sesionRepo.haySesionActiva();
}

/**
 * Verifica si el usuario actual es administrador
 */
public boolean esAdministrador() {
    return sesionRepo.esAdministrador();
}

/**
 * Cambia la contraseña del usuario actual
 */
public boolean cambiarPassword(String passwordActual, String passwordNueva) {
    Usuario usuario = sesionRepo.getUsuarioActual();

    if (usuario == null) {
        return false;
    }

    // Verificar password actual
    if (!usuario.getPassword().equals(passwordActual)) {
        throw new IllegalArgumentException("La contraseña actual es incorrecta.");
    }

    // Validar nueva password
    if (!PasswordUtils.esPasswordValida(passwordNueva)) {
        throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
    }

    // Actualizar password
    usuario.setPassword(passwordNueva);
    return usuarioRepo.actualizarUsuario(usuario);
    }
}

