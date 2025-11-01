package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;

public class StorageUtils {
    
    private static final String TAG = "StorageUtils";
    
    /**
     * Get internal storage info
     */
    public static StorageInfo getInternalStorageInfo(Context context) {
        if (context == null) return null;
        
        try {
            File internalDir = context.getFilesDir();
            if (internalDir != null) {
                StatFs stat = new StatFs(internalDir.getPath());
                return createStorageInfo(stat, "Internal Storage");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting internal storage info", e);
        }
        
        return null;
    }
    
    /**
     * Get external storage info (app-specific)
     */
    public static StorageInfo getExternalStorageInfo(Context context) {
        if (context == null) return null;
        
        try {
            File externalDir = context.getExternalFilesDir(null);
            if (externalDir != null) {
                StatFs stat = new StatFs(externalDir.getPath());
                return createStorageInfo(stat, "External Storage (App)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting external storage info", e);
        }
        
        return null;
    }
    
    /**
     * Get cache storage info
     */
    public static StorageInfo getCacheStorageInfo(Context context) {
        if (context == null) return null;
        
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null) {
                StatFs stat = new StatFs(cacheDir.getPath());
                return createStorageInfo(stat, "Cache Storage");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache storage info", e);
        }
        
        return null;
    }
    
    /**
     * Get external cache storage info
     */
    public static StorageInfo getExternalCacheStorageInfo(Context context) {
        if (context == null) return null;
        
        try {
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null) {
                StatFs stat = new StatFs(externalCacheDir.getPath());
                return createStorageInfo(stat, "External Cache Storage");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting external cache storage info", e);
        }
        
        return null;
    }
    
    /**
     * Get public external storage info (if available)
     */
    public static StorageInfo getPublicExternalStorageInfo() {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File publicDir = Environment.getExternalStorageDirectory();
                if (publicDir != null) {
                    StatFs stat = new StatFs(publicDir.getPath());
                    return createStorageInfo(stat, "Public External Storage");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting public external storage info", e);
        }
        
        return null;
    }
    
    /**
     * Create storage info from StatFs
     */
    private static StorageInfo createStorageInfo(StatFs stat, String name) {
        long blockSize;
        long totalBlocks;
        long availableBlocks;
        
        // minSdk is 26, so we can use the long methods directly
        blockSize = stat.getBlockSizeLong();
        totalBlocks = stat.getBlockCountLong();
        availableBlocks = stat.getAvailableBlocksLong();
        
        long totalSize = totalBlocks * blockSize;
        long availableSize = availableBlocks * blockSize;
        long usedSize = totalSize - availableSize;
        
        return new StorageInfo(name, totalSize, usedSize, availableSize);
    }
    
    /**
     * Check if external storage is available
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    
    /**
     * Check if external storage is read-only
     */
    public static boolean isExternalStorageReadOnly() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }
    
    /**
     * Get total app storage size
     */
    public static long getTotalAppStorageSize(Context context) {
        if (context == null) return 0;
        
        long totalSize = 0;
        
        try {
            // Internal storage
            StorageInfo internalInfo = getInternalStorageInfo(context);
            if (internalInfo != null) {
                totalSize += internalInfo.getTotalSize();
            }
            
            // External storage (app-specific)
            StorageInfo externalInfo = getExternalStorageInfo(context);
            if (externalInfo != null) {
                totalSize += externalInfo.getTotalSize();
            }
            
            // Cache storage
            StorageInfo cacheInfo = getCacheStorageInfo(context);
            if (cacheInfo != null) {
                totalSize += cacheInfo.getTotalSize();
            }
            
            // External cache storage
            StorageInfo externalCacheInfo = getExternalCacheStorageInfo(context);
            if (externalCacheInfo != null) {
                totalSize += externalCacheInfo.getTotalSize();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting total app storage size", e);
        }
        
        return totalSize;
    }
    
    /**
     * Get available app storage size
     */
    public static long getAvailableAppStorageSize(Context context) {
        if (context == null) return 0;
        
        long availableSize = 0;
        
        try {
            // Internal storage
            StorageInfo internalInfo = getInternalStorageInfo(context);
            if (internalInfo != null) {
                availableSize += internalInfo.getAvailableSize();
            }
            
            // External storage (app-specific)
            StorageInfo externalInfo = getExternalStorageInfo(context);
            if (externalInfo != null) {
                availableSize += externalInfo.getAvailableSize();
            }
            
            // Cache storage
            StorageInfo cacheInfo = getCacheStorageInfo(context);
            if (cacheInfo != null) {
                availableSize += cacheInfo.getAvailableSize();
            }
            
            // External cache storage
            StorageInfo externalCacheInfo = getExternalCacheStorageInfo(context);
            if (externalCacheInfo != null) {
                availableSize += externalCacheInfo.getAvailableSize();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting available app storage size", e);
        }
        
        return availableSize;
    }
    
    /**
     * Get used app storage size
     */
    public static long getUsedAppStorageSize(Context context) {
        if (context == null) return 0;
        
        long usedSize = 0;
        
        try {
            // Internal storage
            StorageInfo internalInfo = getInternalStorageInfo(context);
            if (internalInfo != null) {
                usedSize += internalInfo.getUsedSize();
            }
            
            // External storage (app-specific)
            StorageInfo externalInfo = getExternalStorageInfo(context);
            if (externalInfo != null) {
                usedSize += externalInfo.getUsedSize();
            }
            
            // Cache storage
            StorageInfo cacheInfo = getCacheStorageInfo(context);
            if (cacheInfo != null) {
                usedSize += cacheInfo.getUsedSize();
            }
            
            // External cache storage
            StorageInfo externalCacheInfo = getExternalCacheStorageInfo(context);
            if (externalCacheInfo != null) {
                usedSize += externalCacheInfo.getUsedSize();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting used app storage size", e);
        }
        
        return usedSize;
    }
    
    /**
     * Check if app has sufficient storage space
     */
    public static boolean hasSufficientStorageSpace(Context context, long requiredSize) {
        if (context == null || requiredSize <= 0) return false;
        
        long availableSize = getAvailableAppStorageSize(context);
        return availableSize >= requiredSize;
    }
    
    /**
     * Get storage usage percentage
     */
    public static double getStorageUsagePercentage(Context context) {
        if (context == null) return 0.0;
        
        long totalSize = getTotalAppStorageSize(context);
        long usedSize = getUsedAppStorageSize(context);
        
        if (totalSize > 0) {
            return (double) usedSize / totalSize * 100.0;
        }
        
        return 0.0;
    }
    
    /**
     * Storage info class
     */
    public static class StorageInfo {
        private final String name;
        private final long totalSize;
        private final long usedSize;
        private final long availableSize;
        
        public StorageInfo(String name, long totalSize, long usedSize, long availableSize) {
            this.name = name;
            this.totalSize = totalSize;
            this.usedSize = usedSize;
            this.availableSize = availableSize;
        }
        
        public String getName() {
            return name;
        }
        
        public long getTotalSize() {
            return totalSize;
        }
        
        public long getUsedSize() {
            return usedSize;
        }
        
        public long getAvailableSize() {
            return availableSize;
        }
        
        public double getUsagePercentage() {
            if (totalSize > 0) {
                return (double) usedSize / totalSize * 100.0;
            }
            return 0.0;
        }
        
        public String getTotalSizeString() {
            return FileUtils.getFileSizeString(totalSize);
        }
        
        public String getUsedSizeString() {
            return FileUtils.getFileSizeString(usedSize);
        }
        
        public String getAvailableSizeString() {
            return FileUtils.getFileSizeString(availableSize);
        }
        
        @Override
        public String toString() {
            return String.format("%s: Total=%s, Used=%s, Available=%s, Usage=%.1f%%",
                    name, getTotalSizeString(), getUsedSizeString(), 
                    getAvailableSizeString(), getUsagePercentage());
        }
    }
} 