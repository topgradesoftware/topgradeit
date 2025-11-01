package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class ImportUtils {
    
    private static final String TAG = "ImportUtils";
    
    /**
     * Import file to app's private directory
     */
    public static File importFileToAppDir(Context context, Uri sourceUri, String fileName) {
        if (context == null || sourceUri == null) return null;
        
        try {
            // Use app's private external files directory
            File importDir = FileUtils.getAppExternalFilesDir(context, "imports");
            if (importDir == null) {
                Log.e(TAG, "Cannot access app external files directory");
                return null;
            }
            
            File importFile = new File(importDir, fileName);
            
            // Copy file from source URI to app directory
            try (InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
                 FileOutputStream outputStream = new FileOutputStream(importFile)) {
                
                if (inputStream != null) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                    
                    Log.d(TAG, "File imported to app directory: " + importFile.getAbsolutePath());
                    return importFile;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error importing file to app directory", e);
        }
        
        return null;
    }
    
    /**
     * Import file from app's cache directory
     */
    public static File importFileFromCache(Context context, String cacheFileName, String importFileName) {
        if (context == null || cacheFileName == null) return null;
        
        try {
            // Get cache file
            File cacheFile = CacheUtils.getCacheFile(context, cacheFileName);
            if (cacheFile == null || !cacheFile.exists()) {
                Log.e(TAG, "Cache file not found: " + cacheFileName);
                return null;
            }
            
            // Use app's private external files directory
            File importDir = FileUtils.getAppExternalFilesDir(context, "imports");
            if (importDir == null) {
                Log.e(TAG, "Cannot access app external files directory");
                return null;
            }
            
            File importFile = new File(importDir, importFileName);
            
            // Copy file from cache to import directory
            if (FileUtils.copyFile(cacheFile, importFile)) {
                Log.d(TAG, "File imported from cache: " + importFile.getAbsolutePath());
                return importFile;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error importing file from cache", e);
        }
        
        return null;
    }
    
    /**
     * Copy file to import directory
     */
    public static File copyToImportDir(Context context, File sourceFile, String importFileName) {
        if (context == null || sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        
        try {
            // Use app's private external files directory
            File importDir = FileUtils.getAppExternalFilesDir(context, "imports");
            if (importDir == null) {
                Log.e(TAG, "Cannot access app external files directory");
                return null;
            }
            
            File importFile = new File(importDir, importFileName);
            
            // Copy file
            if (FileUtils.copyFile(sourceFile, importFile)) {
                Log.d(TAG, "File copied to import directory: " + importFile.getAbsolutePath());
                return importFile;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error copying file to import directory", e);
        }
        
        return null;
    }
    
    /**
     * Read file content as string
     */
    public static String readFileContent(Context context, Uri fileUri) {
        if (context == null || fileUri == null) return null;
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
            if (inputStream != null) {
                byte[] data = new byte[inputStream.available()];
                inputStream.read(data);
                return new String(data);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading file content", e);
        }
        
        return null;
    }
    
    /**
     * Read file content as byte array
     */
    public static byte[] readFileContentAsBytes(Context context, Uri fileUri) {
        if (context == null || fileUri == null) return null;
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
            if (inputStream != null) {
                byte[] data = new byte[inputStream.available()];
                inputStream.read(data);
                return data;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading file content as bytes", e);
        }
        
        return null;
    }
    
    /**
     * Validate import file
     */
    public static boolean validateImportFile(Context context, Uri fileUri, String expectedExtension) {
        if (context == null || fileUri == null) return false;
        
        try {
            String fileName = getFileNameFromUri(context, fileUri);
            if (fileName == null) return false;
            
            // Check file extension
            if (expectedExtension != null && !fileName.toLowerCase().endsWith(expectedExtension.toLowerCase())) {
                Log.w(TAG, "File extension mismatch. Expected: " + expectedExtension + ", Got: " + fileName);
                return false;
            }
            
            // Check file size (optional validation)
            long fileSize = getFileSize(context, fileUri);
            if (fileSize <= 0) {
                Log.w(TAG, "File size is invalid: " + fileSize);
                return false;
            }
            
            // Check if file is too large (e.g., > 100MB)
            if (fileSize > 100 * 1024 * 1024) {
                Log.w(TAG, "File is too large: " + fileSize + " bytes");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error validating import file", e);
            return false;
        }
    }
    
    /**
     * Get file name from URI
     */
    public static String getFileNameFromUri(Context context, Uri uri) {
        if (context == null || uri == null) return null;
        
        try {
            // Try to get display name from content resolver
            String[] projection = {android.provider.OpenableColumns.DISPLAY_NAME};
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        return cursor.getString(nameIndex);
                    }
                }
            }
            
            // Fallback: get from URI path
            String path = uri.getPath();
            if (path != null) {
                return new File(path).getName();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name from URI", e);
        }
        
        return null;
    }
    
    /**
     * Get file size from URI
     */
    public static long getFileSize(Context context, Uri uri) {
        if (context == null || uri == null) return -1;
        
        try {
            // Try to get size from content resolver
            String[] projection = {android.provider.OpenableColumns.SIZE};
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                    if (sizeIndex >= 0) {
                        return cursor.getLong(sizeIndex);
                    }
                }
            }
            
            // Fallback: get from file
            String path = uri.getPath();
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    return file.length();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file size from URI", e);
        }
        
        return -1;
    }
    
    /**
     * List imported files
     */
    public static File[] listImportedFiles(Context context) {
        if (context == null) return new File[0];
        
        try {
            File importDir = FileUtils.getAppExternalFilesDir(context, "imports");
            if (importDir != null && importDir.exists()) {
                File[] files = importDir.listFiles();
                return files != null ? files : new File[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing imported files", e);
        }
        
        return new File[0];
    }
    
    /**
     * Delete imported file
     */
    public static boolean deleteImportedFile(Context context, String fileName) {
        if (context == null || fileName == null) return false;
        
        try {
            File importDir = FileUtils.getAppExternalFilesDir(context, "imports");
            if (importDir != null) {
                File importFile = new File(importDir, fileName);
                if (importFile.exists()) {
                    return importFile.delete();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting imported file: " + fileName, e);
        }
        
        return false;
    }
    
    /**
     * Clear all imported files
     */
    public static boolean clearAllImportedFiles(Context context) {
        if (context == null) return false;
        
        try {
            File importDir = FileUtils.getAppExternalFilesDir(context, "imports");
            if (importDir != null && importDir.exists()) {
                File[] importFiles = listImportedFiles(context);
                boolean allDeleted = true;
                
                for (File importFile : importFiles) {
                    if (!importFile.delete()) {
                        allDeleted = false;
                    }
                }
                
                return allDeleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all imported files", e);
        }
        
        return false;
    }
    
    /**
     * Get total imported files size
     */
    public static String getTotalImportedFilesSize(Context context) {
        if (context == null) return "0 B";
        
        try {
            File[] importFiles = listImportedFiles(context);
            long totalSize = 0;
            
            for (File importFile : importFiles) {
                totalSize += importFile.length();
            }
            
            return FileUtils.getFileSizeString(totalSize);
        } catch (Exception e) {
            Log.e(TAG, "Error getting total imported files size", e);
        }
        
        return "0 B";
    }
} 