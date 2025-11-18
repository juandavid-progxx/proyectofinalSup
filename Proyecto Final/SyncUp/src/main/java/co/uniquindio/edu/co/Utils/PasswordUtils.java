package co.uniquindio.edu.co.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {

    /**
     * Genera un hash SHA-256 de la contraseña
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback (no recomendado en producción)
        }
    }

    /**
     * Verifica si una contraseña coincide con su hash
     */
    public static boolean verificarPassword(String password, String hashedPassword) {
        String hash = hashPassword(password);
        return hash.equals(hashedPassword);
    }

    /**
     * Valida la fortaleza de una contraseña
     */
    public static boolean esPasswordValida(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }

    /**
     * Genera una contraseña aleatoria
     */
    public static String generarPasswordAleatoria(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < longitud; i++) {
            int index = random.nextInt(caracteres.length());
            password.append(caracteres.charAt(index));
        }

        return password.toString();
    }

    /**
     * Obtiene el nivel de fortaleza de una contraseña (0-3)
     * 0: Muy débil, 1: Débil, 2: Media, 3: Fuerte
     */
    public static int obtenerNivelFortaleza(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int puntos = 0;

        // Longitud
        if (password.length() >= 8) puntos++;
        if (password.length() >= 12) puntos++;

        // Contiene mayúsculas
        if (password.matches(".*[A-Z].*")) puntos++;

        // Contiene minúsculas
        if (password.matches(".*[a-z].*")) puntos++;

        // Contiene números
        if (password.matches(".*\\d.*")) puntos++;

        // Contiene caracteres especiales
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) puntos++;

        // Convertir puntos a nivel (0-3)
        if (puntos <= 2) return 0;      // Muy débil
        if (puntos <= 3) return 1;      // Débil
        if (puntos <= 4) return 2;      // Media
        return 3;                       // Fuerte
    }
}
