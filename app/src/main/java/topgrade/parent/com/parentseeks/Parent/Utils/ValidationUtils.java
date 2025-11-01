package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;

import java.io.File;
import java.util.regex.Pattern;

public class ValidationUtils {
    
    private static final String TAG = "ValidationUtils";
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;
    
    // Phone number validation pattern (basic)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    
    // Password validation pattern (at least 8 characters, 1 uppercase, 1 lowercase, 1 digit)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$");
    
    // Name validation pattern (letters, spaces, hyphens, apostrophes)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s\\-']+$");
    
    // URL validation pattern
    private static final Pattern URL_PATTERN = Patterns.WEB_URL;
    
    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate phone number
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^\\d+]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Validate name (first name, last name, etc.)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        return NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validate URL
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        return URL_PATTERN.matcher(url.trim()).matches();
    }
    
    /**
     * Validate file exists and is readable
     */
    public static boolean isValidFile(File file) {
        return file != null && file.exists() && file.canRead() && file.isFile();
    }
    
    /**
     * Validate directory exists and is readable
     */
    public static boolean isValidDirectory(File directory) {
        return directory != null && directory.exists() && directory.canRead() && directory.isDirectory();
    }
    
    /**
     * Validate file size is within limits
     */
    public static boolean isValidFileSize(File file, long maxSize) {
        if (!isValidFile(file)) {
            return false;
        }
        
        return file.length() <= maxSize;
    }
    
    /**
     * Validate file extension
     */
    public static boolean isValidFileExtension(File file, String[] allowedExtensions) {
        if (!isValidFile(file) || allowedExtensions == null || allowedExtensions.length == 0) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        for (String extension : allowedExtensions) {
            if (fileName.endsWith(extension.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate image file
     */
    public static boolean isValidImageFile(File file) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
        return isValidFileExtension(file, imageExtensions);
    }
    
    /**
     * Validate video file
     */
    public static boolean isValidVideoFile(File file) {
        String[] videoExtensions = {".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".3gp"};
        return isValidFileExtension(file, videoExtensions);
    }
    
    /**
     * Validate audio file
     */
    public static boolean isValidAudioFile(File file) {
        String[] audioExtensions = {".mp3", ".wav", ".aac", ".ogg", ".flac", ".m4a"};
        return isValidFileExtension(file, audioExtensions);
    }
    
    /**
     * Validate document file
     */
    public static boolean isValidDocumentFile(File file) {
        String[] documentExtensions = {".pdf", ".doc", ".docx", ".txt", ".rtf", ".csv", ".xls", ".xlsx"};
        return isValidFileExtension(file, documentExtensions);
    }
    
    /**
     * Validate string length
     */
    public static boolean isValidStringLength(String text, int minLength, int maxLength) {
        if (text == null) {
            return minLength == 0;
        }
        
        int length = text.trim().length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * Validate numeric range
     */
    public static boolean isValidNumericRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validate numeric range for double
     */
    public static boolean isValidNumericRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validate age (assuming age is in years)
     */
    public static boolean isValidAge(int age) {
        return isValidNumericRange(age, 0, 150);
    }
    
    /**
     * Validate date format (YYYY-MM-DD)
     */
    public static boolean isValidDateFormat(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        
        Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
        return datePattern.matcher(date.trim()).matches();
    }
    
    /**
     * Validate time format (HH:MM or HH:MM:SS)
     */
    public static boolean isValidTimeFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }
        
        Pattern timePattern = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$");
        return timePattern.matcher(time.trim()).matches();
    }
    
    /**
     * Validate credit card number (Luhn algorithm)
     */
    public static boolean isValidCreditCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove spaces and dashes
        String cleaned = cardNumber.replaceAll("[\\s\\-]", "");
        
        // Check if it's all digits and has reasonable length
        if (!cleaned.matches("\\d{13,19}")) {
            return false;
        }
        
        // Luhn algorithm
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cleaned.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cleaned.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }
    
    /**
     * Validate postal code (basic US format)
     */
    public static boolean isValidPostalCode(String postalCode) {
        if (postalCode == null || postalCode.trim().isEmpty()) {
            return false;
        }
        
        Pattern postalPattern = Pattern.compile("^\\d{5}(-\\d{4})?$");
        return postalPattern.matcher(postalCode.trim()).matches();
    }
    
    /**
     * Validate social security number (US format)
     */
    public static boolean isValidSSN(String ssn) {
        if (ssn == null || ssn.trim().isEmpty()) {
            return false;
        }
        
        Pattern ssnPattern = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");
        return ssnPattern.matcher(ssn.trim()).matches();
    }
    
    /**
     * Validate URI
     */
    public static boolean isValidUri(Uri uri) {
        return uri != null && uri.toString().trim().length() > 0;
    }
    
    /**
     * Validate app-specific file path
     */
    public static boolean isValidAppFilePath(Context context, String filePath) {
        if (context == null || filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        try {
            File file = new File(filePath);
            
            // Check if file is within app's private directories
            File filesDir = context.getFilesDir();
            File externalFilesDir = context.getExternalFilesDir(null);
            File cacheDir = context.getCacheDir();
            File externalCacheDir = context.getExternalCacheDir();
            
            return file.getCanonicalPath().startsWith(filesDir.getCanonicalPath()) ||
                   (externalFilesDir != null && file.getCanonicalPath().startsWith(externalFilesDir.getCanonicalPath())) ||
                   file.getCanonicalPath().startsWith(cacheDir.getCanonicalPath()) ||
                   (externalCacheDir != null && file.getCanonicalPath().startsWith(externalCacheDir.getCanonicalPath()));
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating app file path", e);
            return false;
        }
    }
    
    /**
     * Validate required fields are not null or empty
     */
    public static boolean areRequiredFieldsValid(String... fields) {
        if (fields == null) return false;
        
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validate at least one field is not null or empty
     */
    public static boolean isAtLeastOneFieldValid(String... fields) {
        if (fields == null) return false;
        
        for (String field : fields) {
            if (field != null && !field.trim().isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get password strength level
     */
    public static PasswordStrength getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.WEAK;
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character variety checks
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[@$!%*?&].*")) score++;
        
        // Determine strength
        if (score >= 5) return PasswordStrength.STRONG;
        if (score >= 3) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }
    
    /**
     * Password strength enum
     */
    public enum PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
} 