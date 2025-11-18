package co.uniquindio.edu.co.Repositorio;

import co.uniquindio.edu.co.Modelo.Administrador;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Utils.PersistenciaManager;
import java.util.*;

public class UsuarioRepository {

    private static UsuarioRepository instancia;
    private HashMap<String, Usuario> usuarios;

    /**
     * Constructor privado (Singleton)
     */
    private UsuarioRepository() {
        this.usuarios = new HashMap<>();
        cargarDatosPersistentes(); // ⭐ CARGAR DATOS AL INICIAR
    }

    /**
     * Obtiene la instancia única del repositorio
     */
    public static UsuarioRepository getInstancia() {
        if (instancia == null) {
            instancia = new UsuarioRepository();
        }
        return instancia;
    }

    /**
     * ⭐ NUEVO: Carga datos persistentes desde archivo
     */
    private void cargarDatosPersistentes() {
        HashMap<String, Usuario> usuariosCargados = PersistenciaManager.cargarUsuarios();

        if (usuariosCargados.isEmpty()) {
            // Si no hay datos guardados, inicializar con admin por defecto
            System.out.println("ℹ️ No hay usuarios guardados, creando administrador por defecto");
            inicializarAdministrador();
        } else {
            // Cargar usuarios desde archivo
            this.usuarios = usuariosCargados;
            System.out.println("✅ Usuarios cargados desde archivo: " + usuarios.size());

            // Verificar que exista el admin
            if (!usuarios.containsKey("admin")) {
                System.out.println("⚠️ Admin no encontrado, creando...");
                inicializarAdministrador();
            }

            // Cargar relaciones sociales
            cargarRelacionesSociales();
        }
    }

    /**
     * ⭐ NUEVO: Carga relaciones sociales
     */
    private void cargarRelacionesSociales() {
        Map<String, LinkedList<String>> relaciones = PersistenciaManager.cargarRelacionesSociales();

        for (Map.Entry<String, LinkedList<String>> entry : relaciones.entrySet()) {
            Usuario usuario = usuarios.get(entry.getKey());
            if (usuario != null) {
                usuario.setUsuariosSeguidos(entry.getValue());
            }
        }
    }

    /**
     * ⭐ NUEVO: Guarda todos los datos
     */
    public void guardarDatos() {
        PersistenciaManager.guardarUsuarios(usuarios);
        PersistenciaManager.guardarRelacionesSociales(usuarios);
    }

    /**
     * ⭐ NUEVO: Restaura favoritos desde IDs guardados
     */
    public void restaurarFavoritos(CancionRepository cancionRepo) {
        PersistenciaManager.restaurarFavoritos(usuarios, cancionRepo);
    }

    /**
     * Inicializa un administrador por defecto
     */
    private void inicializarAdministrador() {
        Administrador admin = new Administrador("admin", "admin123", "Administrador del Sistema");
        usuarios.put(admin.getUsername(), admin);
        guardarDatos(); // ⭐ GUARDAR
    }

    /**
     * Registra un nuevo usuario
     * @return true si se registró exitosamente, false si el username ya existe
     */
    public boolean registrarUsuario(Usuario usuario) {
        if (usuarios.containsKey(usuario.getUsername())) {
            return false;
        }
        usuarios.put(usuario.getUsername(), usuario);
        guardarDatos(); // ⭐ GUARDAR AUTOMÁTICAMENTE
        return true;
    }

    /**
     * Busca un usuario por username
     * @return Usuario encontrado o null
     */
    public Usuario buscarPorUsername(String username) {
        return usuarios.get(username);
    }

    /**
     * Verifica si un username existe
     */
    public boolean existeUsername(String username) {
        return usuarios.containsKey(username);
    }

    /**
     * Actualiza los datos de un usuario
     */
    public boolean actualizarUsuario(Usuario usuario) {
        if (!usuarios.containsKey(usuario.getUsername())) {
            return false;
        }
        usuarios.put(usuario.getUsername(), usuario);
        guardarDatos(); // ⭐ GUARDAR AUTOMÁTICAMENTE
        return true;
    }

    /**
     * Elimina un usuario por username
     */
    public boolean eliminarUsuario(String username) {
        if (username.equals("admin")) {
            return false; // No se puede eliminar al administrador
        }
        boolean eliminado = usuarios.remove(username) != null;
        if (eliminado) {
            guardarDatos(); // ⭐ GUARDAR AUTOMÁTICAMENTE
        }
        return eliminado;
    }

    /**
     * Obtiene todos los usuarios
     */
    public List<Usuario> obtenerTodos() {
        return new ArrayList<>(usuarios.values());
    }

    /**
     * Obtiene todos los usuarios excepto administradores
     */
    public List<Usuario> obtenerUsuariosRegulares() {
        List<Usuario> regulares = new ArrayList<>();
        for (Usuario usuario : usuarios.values()) {
            if (!(usuario instanceof Administrador)) {
                regulares.add(usuario);
            }
        }
        return regulares;
    }

    /**
     * Obtiene todos los administradores
     */
    public List<Usuario> obtenerAdministradores() {
        List<Usuario> admins = new ArrayList<>();
        for (Usuario usuario : usuarios.values()) {
            if (usuario instanceof Administrador) {
                admins.add(usuario);
            }
        }
        return admins;
    }

    /**
     * Busca usuarios por nombre (búsqueda parcial)
     */
    public List<Usuario> buscarPorNombre(String nombre) {
        List<Usuario> resultados = new ArrayList<>();
        String nombreLower = nombre.toLowerCase();

        for (Usuario usuario : usuarios.values()) {
            if (usuario.getNombre().toLowerCase().contains(nombreLower)) {
                resultados.add(usuario);
            }
        }

        return resultados;
    }

    /**
     * Obtiene el número total de usuarios
     */
    public int contarUsuarios() {
        return usuarios.size();
    }

    /**
     * Obtiene el número de usuarios regulares
     */
    public int contarUsuariosRegulares() {
        return obtenerUsuariosRegulares().size();
    }

    /**
     * Limpia todos los usuarios excepto el administrador
     */
    public void limpiar() {
        usuarios.clear();
        inicializarAdministrador();
        guardarDatos(); // ⭐ GUARDAR
    }

    /**
     * Verifica si un usuario es administrador
     */
    public boolean esAdministrador(String username) {
        Usuario usuario = buscarPorUsername(username);
        return usuario instanceof Administrador;
    }

    public HashMap<String, Usuario> getUsuarios() {
        return usuarios;
    }
}
