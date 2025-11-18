package co.uniquindio.edu.co.Utils;

public class Validaciones {

    /**
     * Valida que un String no sea null o vacío
     */
    public static boolean noEsVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    /**
     * Valida formato de username (alfanumérico y guión bajo)
     */
    public static boolean esUsernameValido(String username) {
        if (!noEsVacio(username)) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    /**
     * Valida que un número sea positivo
     */
    public static boolean esPositivo(int numero) {
        return numero > 0;
    }

    /**
     * Valida que un año sea razonable (entre 1900 y año actual + 1)
     */
    public static boolean esAñoValido(int año) {
        int añoActual = java.time.Year.now().getValue();
        return año >= 1900 && año <= añoActual + 1;
    }

    /**
     * Valida que una duración sea válida (entre 1 y 7200 segundos = 2 horas)
     */
    public static boolean esDuracionValida(int duracion) {
        return duracion > 0 && duracion <= 7200;
    }

    /**
     * Valida formato de email básico
     */
    public static boolean esEmailValido(String email) {
        if (!noEsVacio(email)) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Limpia y normaliza un texto
     */
    public static String limpiarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim().replaceAll("\\s+", " ");
    }

    /**
     * Valida que un número esté en un rango
     */
    public static boolean estaEnRango(int numero, int min, int max) {
        return numero >= min && numero <= max;
    }

    /**
     * Valida que un double sea positivo
     */
    public static boolean esPositivo(double numero) {
        return numero > 0.0;
    }

    /**
     * Valida que un double esté entre 0 y 1 (para similitudes)
     */
    public static boolean esSimilitudValida(double similitud) {
        return similitud >= 0.0 && similitud <= 1.0;
    }
}
