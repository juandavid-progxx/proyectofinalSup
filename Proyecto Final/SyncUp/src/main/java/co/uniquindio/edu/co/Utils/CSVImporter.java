package co.uniquindio.edu.co.Utils;

import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {

    /**
     * Importa canciones desde un archivo CSV
     * Formato esperado: id,titulo,artista,genero,año,duracion
     */
    public static List<Cancion> importarCanciones(String rutaArchivo) {
        List<Cancion> canciones = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            boolean primeraLinea = true;

            while ((linea = reader.readLine()) != null) {
                // Saltar encabezados si existen
                if (primeraLinea) {
                    primeraLinea = false;
                    if (linea.toLowerCase().contains("titulo") ||
                            linea.toLowerCase().contains("id")) {
                        continue;
                    }
                }

                // Saltar líneas vacías
                if (linea.trim().isEmpty()) {
                    continue;
                }

                try {
                    Cancion cancion = parsearLineaCancion(linea);
                    if (cancion != null) {
                        canciones.add(cancion);
                    }
                } catch (Exception e) {
                    System.err.println("Error al parsear línea: " + linea);
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + rutaArchivo);
            e.printStackTrace();
        }

        return canciones;
    }

    /**
     * Parsea una línea CSV y crea una Cancion
     */
    private static Cancion parsearLineaCancion(String linea) {
        String[] datos = parsearLineaCSV(linea);

        if (datos.length < 6) {
            System.err.println("Línea con formato inválido (faltan campos): " + linea);
            return null;
        }

        try {
            String id = datos[0].trim();
            String titulo = datos[1].trim();
            String artista = datos[2].trim();
            String generoStr = datos[3].trim();
            int año = Integer.parseInt(datos[4].trim());
            int duracion = Integer.parseInt(datos[5].trim());

            GeneroMusical genero = GeneroMusical.fromString(generoStr);

            return new Cancion();

        } catch (NumberFormatException e) {
            System.err.println("Error al convertir números en la línea: " + linea);
            return null;
        }
    }

    /**
     * Parsea una línea CSV respetando comillas y escapado
     */
    private static String[] parsearLineaCSV(String linea) {
        List<String> resultado = new ArrayList<>();
        StringBuilder campoActual = new StringBuilder();
        boolean dentroDeComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);

            if (c == '"') {
                // Verificar si es una comilla escapada ("")
                if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    campoActual.append('"');
                    i++; // Saltar la siguiente comilla
                } else {
                    dentroDeComillas = !dentroDeComillas;
                }
            } else if (c == ',' && !dentroDeComillas) {
                resultado.add(campoActual.toString());
                campoActual.setLength(0);
            } else {
                campoActual.append(c);
            }
        }

        // Agregar el último campo
        resultado.add(campoActual.toString());

        return resultado.toArray(new String[0]);
    }

    /**
     * Importa canciones desde un archivo de texto plano
     * Formato flexible: cada línea puede tener diferentes separadores
     */
    public static List<Cancion> importarCancionesTextoPlano(String rutaArchivo) {
        List<Cancion> canciones = new ArrayList<>();
        int idContador = 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;

            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                try {
                    Cancion cancion = parsearLineaTextoPlano(linea, String.valueOf(idContador));
                    if (cancion != null) {
                        canciones.add(cancion);
                        idContador++;
                    }
                } catch (Exception e) {
                    System.err.println("Error al parsear línea: " + linea);
                }
            }

        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + rutaArchivo);
            e.printStackTrace();
        }

        return canciones;
    }

    /**
     * Parsea una línea de texto plano con formato flexible
     * Soporta separadores: | ; , o tabulación
     */
    private static Cancion parsearLineaTextoPlano(String linea, String idDefault) {
        String[] datos = null;

        // Intentar con diferentes separadores
        if (linea.contains("|")) {
            datos = linea.split("\\|");
        } else if (linea.contains(";")) {
            datos = linea.split(";");
        } else if (linea.contains("\t")) {
            datos = linea.split("\t");
        } else if (linea.contains(",")) {
            datos = parsearLineaCSV(linea);
        }

        if (datos == null || datos.length < 4) {
            return null;
        }

        try {
            // Formato esperado: titulo, artista, genero, año, [duracion]
            String id = datos.length > 5 ? datos[0].trim() : idDefault;
            int offset = datos.length > 5 ? 1 : 0;

            String titulo = datos[offset].trim();
            String artista = datos[offset + 1].trim();
            String generoStr = datos[offset + 2].trim();
            int año = Integer.parseInt(datos[offset + 3].trim());
            int duracion = datos.length > offset + 4 ?
                    Integer.parseInt(datos[offset + 4].trim()) : 180; // default 3 min

            GeneroMusical genero = GeneroMusical.fromString(generoStr);

            return new Cancion();

        } catch (Exception e) {
            System.err.println("Error al parsear datos: " + linea);
            return null;
        }
    }

    /**
     * Valida el formato de un archivo CSV
     */
    public static boolean validarFormatoCSV(String rutaArchivo) {
        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            String primeraLinea = reader.readLine();

            if (primeraLinea == null) {
                return false;
            }

            // Contar columnas
            String[] columnas = parsearLineaCSV(primeraLinea);
            return columnas.length >= 4; // Mínimo: titulo, artista, genero, año

        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Obtiene el número de líneas en un archivo
     */
    public static int contarLineas(String rutaArchivo) {
        int contador = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            while (reader.readLine() != null) {
                contador++;
            }
        } catch (IOException e) {
            return -1;
        }

        return contador;
    }
}
