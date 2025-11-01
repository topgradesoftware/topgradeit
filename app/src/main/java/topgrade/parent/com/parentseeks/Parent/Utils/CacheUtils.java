package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class CacheUtils {
    
    private static final String TAG = "CacheUtils";
    
    /**
     * Get app's internal cache directory
     */
    public static File getInternalCacheDir(Context context) {
        if (context == null) return null;
        
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }
    
    /**
     * Get app's external cache directory
     */
    public static File getExternalCacheDir(Context context) {
        if (context == null) return null;
        
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir != null && !externalCacheDir.exists()) {
            externalCacheDir.mkdirs();
        }
        return externalCacheDir;
    }
    
    /**
     * Get cache directory for specific type
     */
    public static File getCacheDir(Context context, String type) {
        if (context == null) return null;
        
        File cacheDir;
        if (type != null && !type.isEmpty()) {
            cacheDir = new File(getInternalCacheDir(context), type);
        } else {
            cacheDir = getInternalCacheDir(context);
        }
        
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }
    
    /**
     * Clear internal cache directory
     */
    public static boolean clearInternalCache(Context context) {
        if (context == null) return false;
        
        try {
            File cacheDir = getInternalCacheDir(context);
            if (cacheDir != null && cacheDir.exists()) {
                return deleteDirectory(cacheDir);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing internal cache", e);
        }
        return false;
    }
    
    /**
     * Clear external cache directory
     */
    public static boolean clearExternalCache(Context context) {
        if (context == null) return false;
        
        try {
            File externalCacheDir = getExternalCacheDir(context);
            if (externalCacheDir != null && externalCacheDir.exists()) {
                return deleteDirectory(externalCacheDir);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing external cache", e);
        }
        return false;
    }
    
    /**
     * Clear all cache directories
     */
    public static boolean clearAllCache(Context context) {
        if (context == null) return false;
        
        boolean internalCleared = clearInternalCache(context);
        boolean externalCleared = clearExternalCache(context);
        
        return internalCleared || externalCleared;
    }
    
    /**
     * Get cache size in bytes
     */
    public static long getCacheSize(Context context) {
        if (context == null) return 0;
        
        long size = 0;
        
        try {
            File internalCacheDir = getInternalCacheDir(context);
            if (internalCacheDir != null && internalCacheDir.exists()) {
                size += getDirectorySize(internalCacheDir);
            }
            
            File externalCacheDir = getExternalCacheDir(context);
            if (externalCacheDir != null && externalCacheDir.exists()) {
                size += getDirectorySize(externalCacheDir);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache size", e);
        }
        
        return size;
    }
    
    /**
     * Get cache size in human readable format
     */
    public static String getCacheSizeString(Context context) {
        long size = getCacheSize(context);
        return FileUtils.getFileSizeString(size);
    }
    
    /**
     * Copy file to cache directory
     */
    public static File copyToCache(Context context, File sourceFile, String cacheFileName) {
        if (context == null || sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        
        try {
            File cacheDir = getInternalCacheDir(context);
            if (cacheDir == null) {
                Log.e(TAG, "Cannot access cache directory");
                return null;
            }
            
            File cacheFile = new File(cacheDir, cacheFileName);
            
            // Copy file
            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(cacheFile);
                 FileChannel source = fis.getChannel();
                 FileChannel destination = fos.getChannel()) {
                
                destination.transferFrom(source, 0, source.size());
                Log.d(TAG, "File copied to cache: " + cacheFile.getAbsolutePath());
                return cacheFile;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error copying file to cache", e);
        }
        
        return null;
    }
    
    /**
     * Create temporary file in cache directory
     */
    public static File createTempFile(Context context, String prefix, String suffix) throws IOException {
        if (context == null) throw new IOException("Context is null");
        
        File cacheDir = getInternalCacheDir(context);
        if (cacheDir == null) {
            throw new IOException("Cannot access cache directory");
        }
        
        return File.createTempFile(prefix, suffix, cacheDir);
    }
    
    /**
     * Delete specific cache file
     */
    public static boolean deleteCacheFile(Context context, String fileName) {
        if (context == null || fileName == null) return false;
        
        try {
            File cacheDir = getInternalCacheDir(context);
            if (cacheDir != null) {
                File cacheFile = new File(cacheDir, fileName);
                if (cacheFile.exists()) {
                    return cacheFile.delete();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting cache file: " + fileName, e);
        }
        
        return false;
    }
    
    /**
     * Check if cache file exists
     */
    public static boolean cacheFileExists(Context context, String fileName) {
        if (context == null || fileName == null) return false;
        
        try {
            File cacheDir = getInternalCacheDir(context);
            if (cacheDir != null) {
                File cacheFile = new File(cacheDir, fileName);
                return cacheFile.exists();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking cache file existence: " + fileName, e);
        }
        
        return false;
    }
    
    /**
     * Get cache file
     */
    public static File getCacheFile(Context context, String fileName) {
        if (context == null || fileName == null) return null;
        
        try {
            File cacheDir = getInternalCacheDir(context);
            if (cacheDir != null) {
                File cacheFile = new File(cacheDir, fileName);
                if (cacheFile.exists()) {
                    return cacheFile;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache file: " + fileName, e);
        }
        
        return null;
    }
    
    /**
     * Delete directory and all its contents
     */
    private static boolean deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return false;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        
        return directory.delete();
    }
    
    /**
     * Get directory size in bytes
     */
    private static long getDirectorySize(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
        }
        
        long size = 0;
        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    size += getDirectorySize(file);
                } else {
                    size += file.length();
                }
            }
        }
        
        return size;
    }
} 