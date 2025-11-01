package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtils {
    
    private static final String TAG = "SecurityUtils";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    
    /**
     * Generate secure random key
     */
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE);
        return keyGen.generateKey();
    }
    
    /**
     * Encrypt data
     */
    public static byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
    
    /**
     * Decrypt data
     */
    public static byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }
    
    /**
     * Encrypt file
     */
    public static boolean encryptFile(Context context, File inputFile, File outputFile, SecretKey key) {
        if (context == null || inputFile == null || outputFile == null || key == null) {
            return false;
        }
        
        try {
            // Read input file
            byte[] inputData = readFileToByteArray(inputFile);
            if (inputData == null) {
                Log.e(TAG, "Failed to read input file");
                return false;
            }
            
            // Encrypt data
            byte[] encryptedData = encrypt(inputData, key);
            
            // Write encrypted data to output file
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(encryptedData);
                fos.flush();
            }
            
            Log.d(TAG, "File encrypted successfully: " + outputFile.getAbsolutePath());
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting file", e);
            return false;
        }
    }
    
    /**
     * Decrypt file
     */
    public static boolean decryptFile(Context context, File inputFile, File outputFile, SecretKey key) {
        if (context == null || inputFile == null || outputFile == null || key == null) {
            return false;
        }
        
        try {
            // Read encrypted file
            byte[] encryptedData = readFileToByteArray(inputFile);
            if (encryptedData == null) {
                Log.e(TAG, "Failed to read encrypted file");
                return false;
            }
            
            // Decrypt data
            byte[] decryptedData = decrypt(encryptedData, key);
            
            // Write decrypted data to output file
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(decryptedData);
                fos.flush();
            }
            
            Log.d(TAG, "File decrypted successfully: " + outputFile.getAbsolutePath());
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting file", e);
            return false;
        }
    }
    
    /**
     * Save encrypted file to app's secure directory
     */
    public static File saveEncryptedFile(Context context, byte[] data, String fileName, SecretKey key) {
        if (context == null || data == null || key == null) return null;
        
        try {
            // Use app's private external files directory for secure storage
            File secureDir = FileUtils.getAppExternalFilesDir(context, "secure");
            if (secureDir == null) {
                Log.e(TAG, "Cannot access secure directory");
                return null;
            }
            
            File encryptedFile = new File(secureDir, fileName);
            
            // Encrypt data
            byte[] encryptedData = encrypt(data, key);
            
            // Write encrypted data
            try (FileOutputStream fos = new FileOutputStream(encryptedFile)) {
                fos.write(encryptedData);
                fos.flush();
            }
            
            Log.d(TAG, "Encrypted file saved: " + encryptedFile.getAbsolutePath());
            return encryptedFile;
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving encrypted file", e);
            return null;
        }
    }
    
    /**
     * Load and decrypt file from app's secure directory
     */
    public static byte[] loadDecryptedFile(Context context, String fileName, SecretKey key) {
        if (context == null || fileName == null || key == null) return null;
        
        try {
            // Use app's private external files directory for secure storage
            File secureDir = FileUtils.getAppExternalFilesDir(context, "secure");
            if (secureDir == null) {
                Log.e(TAG, "Cannot access secure directory");
                return null;
            }
            
            File encryptedFile = new File(secureDir, fileName);
            if (!encryptedFile.exists()) {
                Log.e(TAG, "Encrypted file not found: " + fileName);
                return null;
            }
            
            // Read encrypted data
            byte[] encryptedData = readFileToByteArray(encryptedFile);
            if (encryptedData == null) {
                Log.e(TAG, "Failed to read encrypted file");
                return null;
            }
            
            // Decrypt data
            byte[] decryptedData = decrypt(encryptedData, key);
            
            Log.d(TAG, "File decrypted successfully: " + fileName);
            return decryptedData;
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading decrypted file", e);
            return null;
        }
    }
    
    /**
     * Generate secure random bytes
     */
    public static byte[] generateRandomBytes(int length) {
        if (length <= 0) return null;
        
        try {
            SecureRandom secureRandom = new SecureRandom();
            byte[] randomBytes = new byte[length];
            secureRandom.nextBytes(randomBytes);
            return randomBytes;
        } catch (Exception e) {
            Log.e(TAG, "Error generating random bytes", e);
            return null;
        }
    }
    
    /**
     * Generate secure random string
     */
    public static String generateRandomString(int length) {
        if (length <= 0) return null;
        
        try {
            byte[] randomBytes = generateRandomBytes(length);
            if (randomBytes != null) {
                return Base64.getEncoder().encodeToString(randomBytes).substring(0, length);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating random string", e);
        }
        
        return null;
    }
    
    /**
     * Calculate SHA-256 hash
     */
    public static String calculateSHA256(String input) {
        if (input == null) return null;
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error calculating SHA-256 hash", e);
            return null;
        }
    }
    
    /**
     * Calculate SHA-256 hash of file
     */
    public static String calculateFileSHA256(File file) {
        if (file == null || !file.exists()) return null;
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] hash = digest.digest();
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error calculating file SHA-256 hash", e);
            return null;
        }
    }
    
    /**
     * Convert key to string
     */
    public static String keyToString(SecretKey key) {
        if (key == null) return null;
        
        try {
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            Log.e(TAG, "Error converting key to string", e);
            return null;
        }
    }
    
    /**
     * Convert string to key
     */
    public static SecretKey stringToKey(String keyString) {
        if (keyString == null) return null;
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            Log.e(TAG, "Error converting string to key", e);
            return null;
        }
    }
    
    /**
     * Read file to byte array
     */
    private static byte[] readFileToByteArray(File file) {
        if (file == null || !file.exists()) return null;
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return data;
        } catch (IOException e) {
            Log.e(TAG, "Error reading file to byte array", e);
            return null;
        }
    }
    
    /**
     * Clear secure directory
     */
    public static boolean clearSecureDirectory(Context context) {
        if (context == null) return false;
        
        try {
            File secureDir = FileUtils.getAppExternalFilesDir(context, "secure");
            if (secureDir != null && secureDir.exists()) {
                File[] files = secureDir.listFiles();
                if (files != null) {
                    boolean allDeleted = true;
                    for (File file : files) {
                        if (!file.delete()) {
                            allDeleted = false;
                        }
                    }
                    return allDeleted;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing secure directory", e);
        }
        
        return false;
    }
} 