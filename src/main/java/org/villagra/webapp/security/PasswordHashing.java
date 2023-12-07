package org.villagra.webapp.security;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class PasswordHashing {
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";

    // Genera una contraseña hash con sal
    public static String generateHash(String password, String salt) {
        char[] passwordChars = password.toCharArray();
        byte[] saltBytes = Base64.getDecoder().decode(salt);

        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, ITERATIONS, KEY_LENGTH);

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Manejo de errores
            e.printStackTrace();
            return null;
        }
    }

    // Genera una nueva sal
    public static String generateSalt() {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Verifica si la contraseña ingresada coincide con el hash almacenado
    public static boolean verifyPassword(String enteredPassword, String storedHash, String salt) {
        String newHash = generateHash(enteredPassword, salt);
        return newHash != null && newHash.equals(storedHash);
    }

}
