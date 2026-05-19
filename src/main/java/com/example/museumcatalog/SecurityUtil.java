package com.example.museumcatalog;

import org.mindrot.jbcrypt.BCrypt;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // Байт (96 бит) - стандарт для gcm
    private static final int TAG_LENGTH_BIT = 128; //бит

    public static SecretKey loadKeyFromEnv(String envVarName) {
        String keyBase64 = System.getenv(envVarName);
        if (keyBase64 == null || keyBase64.isEmpty()) {
            throw new IllegalStateException("Ключ шифрования не найден в переменной окружения: " + envVarName);
        }
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public static String encrypt(String data, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] cipherText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String base64Data, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] combined = Base64.getDecoder().decode(base64Data);

        byte[] iv = new byte[IV_SIZE];
        byte[] cipherText = new byte[combined.length - iv.length];

        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    public static String encryptSafe(String data, SecretKey key) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }
        try {
            return encrypt(data, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptSafe(String base64Data, SecretKey key) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            return null;
        }
        try {
            return decrypt(base64Data, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return null;
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}