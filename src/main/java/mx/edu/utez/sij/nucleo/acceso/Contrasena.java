package mx.edu.utez.sij.nucleo.acceso;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Contraseñas de TODO el sistema (INTEGRADORES): cifrado con PBKDF2 (viene con Java, no necesita librerías),
 * regla de la contraseña nueva y contraseña temporal.
 *
 * Todos los módulos guardan la contraseña YA CIFRADA en su tabla user_* con Contrasena.cifrar(...):
 * el trigger la copia tal cual a la tabla Login, y el inicio de sesión del núcleo la revisa ahí.
 *
 * En la base se guarda un solo texto: pbkdf2$iteraciones$sal$hash (cabe en VARCHAR(255)).
 * Como las iteraciones van en el texto, si algún día se suben, las contraseñas viejas siguen funcionando.
 */
public final class Contrasena {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 600_000;   // docs/ARQUITECTURA.md, sección 10
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom AZAR = new SecureRandom();

    // Sin letras que se confunden (I, l, O, 0, 1) para que el administrador la pueda dictar
    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGITOS = "23456789";

    private Contrasena() {
    }

    // Lo que se guarda en user_Per.contrasena
    public static String cifrar(String contrasena) {
        byte[] sal = new byte[BYTES_SAL];
        AZAR.nextBytes(sal);
        byte[] hash = pbkdf2(contrasena, sal, ITERACIONES);
        return "pbkdf2$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    // true si la contraseña escrita corresponde a la guardada
    public static boolean coincide(String contrasena, String guardada) {
        if (contrasena == null || guardada == null) {
            return false;
        }
        String[] partes = guardada.split("\\$");
        if (partes.length != 4 || !"pbkdf2".equals(partes[0])) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            // Comparación de tiempo constante (no da pistas de cuántos bytes coinciden)
            return MessageDigest.isEqual(esperado, pbkdf2(contrasena, sal, iteraciones));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Regla común del sistema (docs/ARQUITECTURA.md 7.1): 8 caracteres con mayúscula, minúscula y número.
    // Regresa el mensaje de error, o null si es válida.
    public static String validarNueva(String contrasena) {
        if (contrasena == null || contrasena.length() < 8
                || !contrasena.matches(".*[A-ZÁÉÍÓÚÑ].*")
                || !contrasena.matches(".*[a-záéíóúñ].*")
                || !contrasena.matches(".*\\d.*")) {
            return "La contraseña debe tener al menos 8 caracteres, con una mayúscula, una minúscula y un número";
        }
        return null;
    }

    // Contraseña temporal de 10 caracteres que cumple la regla (RF 3.1: la ve el administrador una sola vez)
    public static String generarTemporal() {
        String todos = MAYUSCULAS + MINUSCULAS + DIGITOS;
        StringBuilder texto = new StringBuilder();
        texto.append(MAYUSCULAS.charAt(AZAR.nextInt(MAYUSCULAS.length())));
        texto.append(MINUSCULAS.charAt(AZAR.nextInt(MINUSCULAS.length())));
        texto.append(DIGITOS.charAt(AZAR.nextInt(DIGITOS.length())));
        while (texto.length() < 10) {
            texto.append(todos.charAt(AZAR.nextInt(todos.length())));
        }
        // Revuelve para que la mayúscula no quede siempre al inicio
        for (int i = texto.length() - 1; i > 0; i--) {
            int j = AZAR.nextInt(i + 1);
            char c = texto.charAt(i);
            texto.setCharAt(i, texto.charAt(j));
            texto.setCharAt(j, c);
        }
        return texto.toString();
    }

    private static byte[] pbkdf2(String contrasena, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, BITS_HASH);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no está disponible en esta versión de Java", e);
        }
    }
}
