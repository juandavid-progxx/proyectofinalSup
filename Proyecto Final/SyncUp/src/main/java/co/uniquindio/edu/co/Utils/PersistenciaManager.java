package co.uniquindio.edu.co.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import co.uniquindio.edu.co.Modelo.Usuario;
import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PersistenciaManager {

    private static final String DIRECTORIO_DATOS = System.getProperty("user.home") + "/SyncUp/data/";
    private static final String ARCHIVO_USUARIOS = DIRECTORIO_DATOS + "usuarios.json";
    private static final String ARCHIVO_RELACIONES = DIRECTORIO_DATOS + "relaciones_sociales.json";
    private static final String ARCHIVO_CANCIONES = DIRECTORIO_DATOS + "canciones.json";  // ⭐ NUEVO

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Inicializa el directorio de datos
     */
    public static void inicializarDirectorio() {
        File directorio = new File(DIRECTORIO_DATOS);
        if (!directorio.exists()) {
            directorio.mkdirs();
            System.out.println("✅ Directorio de datos creado: " + DIRECTORIO_DATOS);
        }
    }

    // ==================== USUARIOS ====================

    /**
     * Guarda todos los usuarios en archivo JSON
     */
    public static boolean guardarUsuarios(HashMap<String, Usuario> usuarios) {
        try {
            inicializarDirectorio();

            Map<String, UsuarioDTO> usuariosDTO = new HashMap<>();
            for (Map.Entry<String, Usuario> entry : usuarios.entrySet()) {
                usuariosDTO.put(entry.getKey(), convertirAUsuarioDTO(entry.getValue()));
            }

            String json = gson.toJson(usuariosDTO);

            try (FileWriter writer = new FileWriter(ARCHIVO_USUARIOS)) {
                writer.write(json);
            }

            System.out.println("💾 Usuarios guardados: " + usuarios.size());
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error al guardar usuarios: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Carga usuarios desde archivo JSON
     */
    public static HashMap<String, Usuario> cargarUsuarios() {
        HashMap<String, Usuario> usuarios = new HashMap<>();
        File archivo = new File(ARCHIVO_USUARIOS);

        if (!archivo.exists()) {
            System.out.println("ℹ️ No hay datos previos de usuarios");
            return usuarios;
        }

        try (FileReader reader = new FileReader(archivo)) {
            Type type = new TypeToken<Map<String, UsuarioDTO>>(){}.getType();
            Map<String, UsuarioDTO> usuariosDTO = gson.fromJson(reader, type);

            if (usuariosDTO != null) {
                for (Map.Entry<String, UsuarioDTO> entry : usuariosDTO.entrySet()) {
                    Usuario usuario = convertirAUsuario(entry.getValue());
                    usuarios.put(entry.getKey(), usuario);
                }
            }

            System.out.println("📂 Usuarios cargados: " + usuarios.size());
            return usuarios;

        } catch (IOException e) {
            System.err.println("❌ Error al cargar usuarios: " + e.getMessage());
            return usuarios;
        }
    }

    // ==================== CANCIONES ⭐ NUEVO ====================

    /**
     * ⭐ Guarda todas las canciones en archivo JSON
     */
    public static boolean guardarCanciones(HashMap<String, Cancion> canciones) {
        try {
            inicializarDirectorio();

            Map<String, CancionDTO> cancionesDTO = new HashMap<>();
            for (Map.Entry<String, Cancion> entry : canciones.entrySet()) {
                cancionesDTO.put(entry.getKey(), convertirACancionDTO(entry.getValue()));
            }

            String json = gson.toJson(cancionesDTO);

            try (FileWriter writer = new FileWriter(ARCHIVO_CANCIONES)) {
                writer.write(json);
            }

            System.out.println("💾 Canciones guardadas: " + canciones.size());
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error al guardar canciones: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ⭐ Carga canciones desde archivo JSON
     */
    public static HashMap<String, Cancion> cargarCanciones() {
        HashMap<String, Cancion> canciones = new HashMap<>();
        File archivo = new File(ARCHIVO_CANCIONES);

        if (!archivo.exists()) {
            System.out.println("ℹ️ No hay canciones guardadas previas");
            return canciones;
        }

        try (FileReader reader = new FileReader(archivo)) {
            Type type = new TypeToken<Map<String, CancionDTO>>(){}.getType();
            Map<String, CancionDTO> cancionesDTO = gson.fromJson(reader, type);

            if (cancionesDTO != null) {
                for (Map.Entry<String, CancionDTO> entry : cancionesDTO.entrySet()) {
                    Cancion cancion = convertirACancion(entry.getValue());
                    canciones.put(entry.getKey(), cancion);
                }
            }

            System.out.println("📂 Canciones cargadas: " + canciones.size());
            return canciones;

        } catch (IOException e) {
            System.err.println("❌ Error al cargar canciones: " + e.getMessage());
            return canciones;
        }
    }

    // ==================== RELACIONES SOCIALES ====================

    /**
     * Guarda relaciones sociales (quién sigue a quién)
     */
    public static boolean guardarRelacionesSociales(HashMap<String, Usuario> usuarios) {
        try {
            inicializarDirectorio();

            Map<String, LinkedList<String>> relaciones = new HashMap<>();
            for (Map.Entry<String, Usuario> entry : usuarios.entrySet()) {
                relaciones.put(entry.getKey(), entry.getValue().getUsuariosSeguidos());
            }

            String json = gson.toJson(relaciones);

            try (FileWriter writer = new FileWriter(ARCHIVO_RELACIONES)) {
                writer.write(json);
            }

            System.out.println("💾 Relaciones sociales guardadas");
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error al guardar relaciones: " + e.getMessage());
            return false;
        }
    }

    /**
     * Carga relaciones sociales desde archivo
     */
    public static Map<String, LinkedList<String>> cargarRelacionesSociales() {
        File archivo = new File(ARCHIVO_RELACIONES);

        if (!archivo.exists()) {
            System.out.println("ℹ️ No hay datos previos de relaciones sociales");
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(archivo)) {
            Type type = new TypeToken<Map<String, LinkedList<String>>>(){}.getType();
            Map<String, LinkedList<String>> relaciones = gson.fromJson(reader, type);

            System.out.println("📂 Relaciones sociales cargadas");
            return relaciones != null ? relaciones : new HashMap<>();

        } catch (IOException e) {
            System.err.println("❌ Error al cargar relaciones: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // ==================== CONVERTIDORES DTO ====================

    /**
     * Convierte Usuario a DTO
     */
    private static UsuarioDTO convertirAUsuarioDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.username = usuario.getUsername();
        dto.password = usuario.getPassword();
        dto.nombre = usuario.getNombre();
        dto.esAdmin = usuario instanceof co.uniquindio.edu.co.Modelo.Administrador;

        dto.favoritosIds = new LinkedList<>();
        for (Cancion cancion : usuario.getListaFavoritos()) {
            dto.favoritosIds.add(cancion.getId());
        }

        dto.usuariosSeguidos = new LinkedList<>(usuario.getUsuariosSeguidos());

        return dto;
    }

    /**
     * Convierte DTO a Usuario
     */
    private static Usuario convertirAUsuario(UsuarioDTO dto) {
        Usuario usuario;

        if (dto.esAdmin) {
            usuario = new co.uniquindio.edu.co.Modelo.Administrador(
                    dto.username,
                    dto.password,
                    dto.nombre
            );
        } else {
            usuario = new Usuario(dto.username, dto.password, dto.nombre);
        }

        usuario.setUsuariosSeguidos(dto.usuariosSeguidos);

        return usuario;
    }

    /**
     * ⭐ Convierte Cancion a DTO
     */
    private static CancionDTO convertirACancionDTO(Cancion cancion) {
        CancionDTO dto = new CancionDTO();
        dto.id = cancion.getId();
        dto.titulo = cancion.getTitulo();
        dto.artista = cancion.getArtista();
        dto.genero = cancion.getGenero() != null ? cancion.getGenero().name() : null;
        dto.año = cancion.getAño();
        dto.duracion = cancion.getDuracion();
        dto.urlAudio = cancion.getUrlAudio();
        return dto;
    }

    /**
     * ⭐ Convierte DTO a Cancion
     */
    private static Cancion convertirACancion(CancionDTO dto) {
        GeneroMusical genero = null;
        if (dto.genero != null) {
            try {
                genero = GeneroMusical.valueOf(dto.genero);
            } catch (IllegalArgumentException e) {
                genero = GeneroMusical.POP;
            }
        }

        return new Cancion(
                dto.id,
                dto.titulo,
                dto.artista,
                genero,
                dto.año,
                dto.duracion,
                dto.urlAudio
        );
    }

    /**
     * Restaura favoritos de usuarios desde IDs
     */
    public static void restaurarFavoritos(HashMap<String, Usuario> usuarios,
                                          co.uniquindio.edu.co.Repositorio.CancionRepository cancionRepo) {
        File archivo = new File(ARCHIVO_USUARIOS);

        if (!archivo.exists()) return;

        try (FileReader reader = new FileReader(archivo)) {
            Type type = new TypeToken<Map<String, UsuarioDTO>>(){}.getType();
            Map<String, UsuarioDTO> usuariosDTO = gson.fromJson(reader, type);

            if (usuariosDTO != null) {
                for (Map.Entry<String, UsuarioDTO> entry : usuariosDTO.entrySet()) {
                    Usuario usuario = usuarios.get(entry.getKey());
                    if (usuario != null) {
                        UsuarioDTO dto = entry.getValue();

                        for (String idCancion : dto.favoritosIds) {
                            Cancion cancion = cancionRepo.buscarPorId(idCancion);
                            if (cancion != null) {
                                usuario.agregarFavorito(cancion);
                            }
                        }
                    }
                }
            }

            System.out.println("📂 Favoritos restaurados para todos los usuarios");

        } catch (IOException e) {
            System.err.println("❌ Error al restaurar favoritos: " + e.getMessage());
        }
    }

    // ==================== DTOs ====================

    /**
     * DTO para Usuario
     */
    private static class UsuarioDTO {
        String username;
        String password;
        String nombre;
        boolean esAdmin;
        LinkedList<String> favoritosIds;
        LinkedList<String> usuariosSeguidos;
    }

    /**
     * ⭐ DTO para Cancion
     */
    private static class CancionDTO {
        String id;
        String titulo;
        String artista;
        String genero;
        int año;
        int duracion;
        String urlAudio;
    }
}
