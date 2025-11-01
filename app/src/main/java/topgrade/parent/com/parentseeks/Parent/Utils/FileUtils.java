package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {
    
    private static final String TAG = "FileUtils";
    
    /**
     * Get the app's private external files directory
     */
    public static File getAppExternalFilesDir(Context context, String type) {
        if (context == null) return null;
        
        File externalFilesDir = context.getExternalFilesDir(type);
        if (externalFilesDir != null && !externalFilesDir.exists()) {
            externalFilesDir.mkdirs();
        }
        return externalFilesDir;
    }
    
    /**
     * Get the app's private cache directory
     */
    public static File getAppCacheDir(Context context) {
        if (context == null) return null;
        
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }
    
    /**
     * Create a temporary file in the app's cache directory
     */
    public static File createTempFile(Context context, String prefix, String suffix) throws IOException {
        if (context == null) throw new IOException("Context is null");
        
        File cacheDir = getAppCacheDir(context);
        if (cacheDir == null) {
            throw new IOException("Cannot access cache directory");
        }
        
        return File.createTempFile(prefix, suffix, cacheDir);
    }
    
    /**
     * Save image to MediaStore (Android 10+) or external storage (older versions)
     */
    public static Uri saveImageToGallery(Context context, byte[] imageData, String fileName) {
        if (context == null || imageData == null) return null;
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore API for Android 10+
                return saveImageToMediaStore(context, imageData, fileName);
            } else {
                // Use external storage for older versions
                return saveImageToExternalStorage(context, imageData, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving image to gallery", e);
            return null;
        }
    }
    
    /**
     * Save image using MediaStore API (Android 10+)
     */
    private static Uri saveImageToMediaStore(Context context, byte[] imageData, String fileName) {
        ContentResolver resolver = context.getContentResolver();
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ParentSeeks");
        
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(imageData);
                    outputStream.flush();
                    Log.d(TAG, "Image saved to MediaStore: " + uri);
                    return uri;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error writing to MediaStore", e);
                resolver.delete(uri, null, null);
            }
        }
        return null;
    }
    
    /**
     * Save image to external storage (pre-Android 10)
     */
    private static Uri saveImageToExternalStorage(Context context, byte[] imageData, String fileName) {
        // Check if external storage is available
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "External storage not available");
            return null;
        }
        
        // Create directory if it doesn't exist
        File picturesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ParentSeeks");
        if (!picturesDir.exists() && !picturesDir.mkdirs()) {
            Log.e(TAG, "Failed to create pictures directory");
            return null;
        }
        
        // Create file
        File imageFile = new File(picturesDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(imageData);
            fos.flush();
            
            // Notify media scanner
            Uri uri = Uri.fromFile(imageFile);
            Log.d(TAG, "Image saved to external storage: " + uri);
            return uri;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to external storage", e);
            return null;
        }
    }
    
    /**
     * Generate unique filename with timestamp
     */
    public static String generateUniqueFileName(String prefix, String extension) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return prefix + "_" + timeStamp + "." + extension;
    }
    
    /**
     * Copy file from one location to another
     */
    public static boolean copyFile(File sourceFile, File destFile) {
        if (sourceFile == null || destFile == null || !sourceFile.exists()) {
            return false;
        }
        
        try {
            // Create parent directories if they don't exist
            File parentDir = destFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Copy file
            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(destFile);
                 FileChannel source = fis.getChannel();
                 FileChannel destination = fos.getChannel()) {
                
                destination.transferFrom(source, 0, source.size());
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error copying file", e);
            return false;
        }
    }
    
    /**
     * Copy file using streams
     */
    public static boolean copyFileStream(InputStream inputStream, File destFile) {
        if (inputStream == null || destFile == null) {
            return false;
        }
        
        try {
            // Create parent directories if they don't exist
            File parentDir = destFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Copy file
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.flush();
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error copying file stream", e);
            return false;
        }
    }
    
    /**
     * Delete file safely
     */
    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        try {
            return file.delete();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting file: " + file.getAbsolutePath(), e);
            return false;
        }
    }
    
    /**
     * Get file size in human readable format
     */
    public static String getFileSizeString(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format(Locale.getDefault(), "%.1f %s", 
                           size / Math.pow(1024, digitGroups), 
                           units[digitGroups]);
    }
    
    /**
     * Check if file is an image
     */
    public static boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || 
               lowerCaseName.endsWith(".jpeg") || 
               lowerCaseName.endsWith(".png") || 
               lowerCaseName.endsWith(".gif") || 
               lowerCaseName.endsWith(".bmp") || 
               lowerCaseName.endsWith(".webp");
    }
    
    /**
     * Check if file is a video
     */
    public static boolean isVideoFile(String fileName) {
        if (fileName == null) return false;
        
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".mp4") || 
               lowerCaseName.endsWith(".avi") || 
               lowerCaseName.endsWith(".mov") || 
               lowerCaseName.endsWith(".wmv") || 
               lowerCaseName.endsWith(".flv") || 
               lowerCaseName.endsWith(".mkv") || 
               lowerCaseName.endsWith(".3gp");
    }
    
    /**
     * Read file to byte array
     */
    public static byte[] readFileToByteArray(File file) {
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
     * Read file to string
     */
    public static String readFileToString(File file) {
        if (file == null || !file.exists()) return null;
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return new String(data, "UTF-8");
        } catch (IOException e) {
            Log.e(TAG, "Error reading file to string", e);
            return null;
        }
    }
} 