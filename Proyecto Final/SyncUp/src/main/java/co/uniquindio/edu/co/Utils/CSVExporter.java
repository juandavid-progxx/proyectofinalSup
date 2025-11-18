package co.uniquindio.edu.co.Utils;

import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.Usuario;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter {

    /**
     * Exporta una lista de canciones a un archivo CSV
     */
    public static boolean exportarCanciones(List<Cancion> canciones, String rutaArchivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            // Escribir encabezados
            writer.write("ID,Título,Artista,Género,Año,Duración (seg)");
            writer.newLine();

            // Escribir datos
            for (Cancion cancion : canciones) {
                writer.write(escaparCSV(cancion.getId()) + ",");
                writer.write(escaparCSV(cancion.getTitulo()) + ",");
                writer.write(escaparCSV(cancion.getArtista()) + ",");
                writer.write(escaparCSV(cancion.getGenero().getNombre()) + ",");
                writer.write(cancion.getAño() + ",");
                writer.write(String.valueOf(cancion.getDuracion()));
                writer.newLine();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exporta los favoritos de un usuario a CSV
     */
    public static boolean exportarFavoritos(Usuario usuario, String rutaArchivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            // Escribir información del usuario
            writer.write("Usuario: " + usuario.getNombre() + " (@" + usuario.getUsername() + ")");
            writer.newLine();
            writer.write("Total de favoritos: " + usuario.getCantidadFavoritos());
            writer.newLine();
            writer.newLine();

            // Escribir encabezados
            writer.write("ID,Título,Artista,Género,Año,Duración");
            writer.newLine();

            // Escribir canciones favoritas
            for (Cancion cancion : usuario.getListaFavoritos()) {
                writer.write(escaparCSV(cancion.getId()) + ",");
                writer.write(escaparCSV(cancion.getTitulo()) + ",");
                writer.write(escaparCSV(cancion.getArtista()) + ",");
                writer.write(escaparCSV(cancion.getGenero().getNombre()) + ",");
                writer.write(cancion.getAño() + ",");
                writer.write(cancion.getDuracionFormateada());
                writer.newLine();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exporta una lista de usuarios a CSV
     */
    public static boolean exportarUsuarios(List<Usuario> usuarios, String rutaArchivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            // Escribir encabezados
            writer.write("Username,Nombre,Canciones Favoritas,Usuarios Seguidos,Tipo");
            writer.newLine();

            // Escribir datos
            for (Usuario usuario : usuarios) {
                writer.write(escaparCSV(usuario.getUsername()) + ",");
                writer.write(escaparCSV(usuario.getNombre()) + ",");
                writer.write(usuario.getCantidadFavoritos() + ",");
                writer.write(usuario.getCantidadSeguidos() + ",");
                String tipo = (usuario instanceof co.uniquindio.edu.co.Modelo.Administrador) ? "Admin" : "Usuario";
                writer.write(tipo);
                writer.newLine();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exporta estadísticas generales del sistema
     */
    public static boolean exportarEstadisticas(
            int totalUsuarios,
            int totalCanciones,
            java.util.Map<co.uniquindio.edu.co.Modelo.GeneroMusical, Integer> estadisticasGenero,
            String rutaArchivo) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            writer.write("=== ESTADÍSTICAS DEL SISTEMA SYNCUP ===");
            writer.newLine();
            writer.newLine();

            writer.write("Total de Usuarios: " + totalUsuarios);
            writer.newLine();
            writer.write("Total de Canciones: " + totalCanciones);
            writer.newLine();
            writer.newLine();

            writer.write("=== DISTRIBUCIÓN POR GÉNERO ===");
            writer.newLine();
            writer.write("Género,Cantidad,Porcentaje");
            writer.newLine();

            for (var entry : estadisticasGenero.entrySet()) {
                double porcentaje = (entry.getValue() * 100.0) / totalCanciones;
                writer.write(entry.getKey().getNombre() + ",");
                writer.write(entry.getValue() + ",");
                writer.write(String.format("%.2f%%", porcentaje));
                writer.newLine();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Escapa caracteres especiales en CSV (comas, comillas, saltos de línea)
     */
    private static String escaparCSV(String texto) {
        if (texto == null) {
            return "";
        }

        // Si contiene comas, comillas o saltos de línea, envolver en comillas
        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n")) {
            // Duplicar comillas internas
            texto = texto.replace("\"", "\"\"");
            // Envolver en comillas
            return "\"" + texto + "\"";
        }

        return texto;
    }

    /**
     * Verifica si un archivo es escribible
     */
    public static boolean esRutaValida(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            File directorio = archivo.getParentFile();

            if (directorio != null && !directorio.exists()) {
                return directorio.mkdirs();
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene la extensión CSV si no la tiene
     */
    public static String asegurarExtensionCSV(String rutaArchivo) {
        if (!rutaArchivo.toLowerCase().endsWith(".csv")) {
            return rutaArchivo + ".csv";
        }
        return rutaArchivo;
    }
}
