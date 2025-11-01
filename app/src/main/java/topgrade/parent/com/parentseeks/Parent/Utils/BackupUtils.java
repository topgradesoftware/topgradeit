package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupUtils {
    
    private static final String TAG = "BackupUtils";
    
    /**
     * Create backup in app's private directory
     */
    public static File createBackupInAppDir(Context context, String backupName) {
        if (context == null || backupName == null) return null;
        
        try {
            // Use app's private external files directory
            File backupDir = FileUtils.getAppExternalFilesDir(context, "backups");
            if (backupDir == null) {
                Log.e(TAG, "Cannot access app external files directory");
                return null;
            }
            
            String fileName = generateBackupFileName(backupName);
            File backupFile = new File(backupDir, fileName);
            
            // Create backup
            if (createBackupArchive(context, backupFile)) {
                Log.d(TAG, "Backup created in app directory: " + backupFile.getAbsolutePath());
                return backupFile;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating backup in app directory", e);
        }
        
        return null;
    }
    
    /**
     * Save backup to downloads using modern API
     */
    public static Uri saveBackupToDownloads(Context context, String backupName) {
        if (context == null || backupName == null) return null;
        
        try {
            // First create backup in app directory
            File backupFile = createBackupInAppDir(context, backupName);
            if (backupFile == null || !backupFile.exists()) {
                Log.e(TAG, "Failed to create backup file");
                return null;
            }
            
            // Read backup data
            byte[] backupData = readFileToByteArray(backupFile);
            if (backupData == null) {
                Log.e(TAG, "Failed to read backup data");
                return null;
            }
            
            String fileName = backupFile.getName();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore API for Android 10+
                return saveBackupToMediaStore(context, backupData, fileName);
            } else {
                // Use external storage for older versions
                return saveBackupToExternalStorage(context, backupData, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving backup to downloads", e);
            return null;
        }
    }
    
    /**
     * Save backup using MediaStore API (Android 10+)
     */
    private static Uri saveBackupToMediaStore(Context context, byte[] backupData, String fileName) {
        ContentResolver resolver = context.getContentResolver();
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/zip");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ParentSeeks/Backups");
        
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (FileOutputStream outputStream = (FileOutputStream) resolver.openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(backupData);
                    outputStream.flush();
                    Log.d(TAG, "Backup saved to MediaStore: " + uri);
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
     * Save backup to external storage (pre-Android 10)
     */
    private static Uri saveBackupToExternalStorage(Context context, byte[] backupData, String fileName) {
        // Check if external storage is available
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "External storage not available");
            return null;
        }
        
        // Create directory if it doesn't exist
        File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ParentSeeks/Backups");
        if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
            Log.e(TAG, "Failed to create downloads directory");
            return null;
        }
        
        // Create file
        File backupFile = new File(downloadsDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(backupFile)) {
            fos.write(backupData);
            fos.flush();
            
            // Notify media scanner
            Uri uri = Uri.fromFile(backupFile);
            Log.d(TAG, "Backup saved to external storage: " + uri);
            return uri;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to external storage", e);
            return null;
        }
    }
    
    /**
     * Create backup archive
     */
    private static boolean createBackupArchive(Context context, File backupFile) {
        if (context == null || backupFile == null) return false;
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFile))) {
            
            // Add database files
            addDatabaseToBackup(context, zos);
            
            // Add shared preferences
            addSharedPreferencesToBackup(context, zos);
            
            // Add cache files (optional)
            addCacheToBackup(context, zos);
            
            // Add app files
            addAppFilesToBackup(context, zos);
            
            Log.d(TAG, "Backup archive created successfully");
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Error creating backup archive", e);
            return false;
        }
    }
    
    /**
     * Add database files to backup
     */
    private static void addDatabaseToBackup(Context context, ZipOutputStream zos) throws IOException {
        File dbDir = context.getDatabasePath("").getParentFile();
        if (dbDir != null && dbDir.exists()) {
            File[] dbFiles = dbDir.listFiles();
            if (dbFiles != null) {
                for (File dbFile : dbFiles) {
                    if (dbFile.isFile() && dbFile.getName().endsWith(".db")) {
                        addFileToZip(zos, dbFile, "databases/" + dbFile.getName());
                    }
                }
            }
        }
    }
    
    /**
     * Add shared preferences to backup
     */
    private static void addSharedPreferencesToBackup(Context context, ZipOutputStream zos) throws IOException {
        File prefsDir = new File(context.getApplicationInfo().dataDir, "shared_prefs");
        if (prefsDir.exists()) {
            File[] prefFiles = prefsDir.listFiles();
            if (prefFiles != null) {
                for (File prefFile : prefFiles) {
                    if (prefFile.isFile() && prefFile.getName().endsWith(".xml")) {
                        addFileToZip(zos, prefFile, "shared_prefs/" + prefFile.getName());
                    }
                }
            }
        }
    }
    
    /**
     * Add cache files to backup
     */
    private static void addCacheToBackup(Context context, ZipOutputStream zos) throws IOException {
        File cacheDir = CacheUtils.getInternalCacheDir(context);
        if (cacheDir != null && cacheDir.exists()) {
            addDirectoryToZip(zos, cacheDir, "cache");
        }
    }
    
    /**
     * Add app files to backup
     */
    private static void addAppFilesToBackup(Context context, ZipOutputStream zos) throws IOException {
        File filesDir = context.getFilesDir();
        if (filesDir.exists()) {
            addDirectoryToZip(zos, filesDir, "files");
        }
    }
    
    /**
     * Add file to zip archive
     */
    private static void addFileToZip(ZipOutputStream zos, File file, String entryName) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
            
            zos.closeEntry();
        }
    }
    
    /**
     * Add directory to zip archive recursively
     */
    private static void addDirectoryToZip(ZipOutputStream zos, File directory, String basePath) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String entryPath = basePath + "/" + file.getName();
                if (file.isDirectory()) {
                    addDirectoryToZip(zos, file, entryPath);
                } else {
                    addFileToZip(zos, file, entryPath);
                }
            }
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
     * Generate backup filename with timestamp
     */
    public static String generateBackupFileName(String backupName) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return backupName + "_" + timeStamp + ".zip";
    }
    
    /**
     * Get backup file size
     */
    public static String getBackupFileSize(File backupFile) {
        if (backupFile == null || !backupFile.exists()) return "0 B";
        
        return FileUtils.getFileSizeString(backupFile.length());
    }
    
    /**
     * List available backups
     */
    public static File[] listBackups(Context context) {
        if (context == null) return new File[0];
        
        try {
            File backupDir = FileUtils.getAppExternalFilesDir(context, "backups");
            if (backupDir != null && backupDir.exists()) {
                File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".zip"));
                return files != null ? files : new File[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing backups", e);
        }
        
        return new File[0];
    }
    
    /**
     * Delete backup file
     */
    public static boolean deleteBackup(Context context, String backupFileName) {
        if (context == null || backupFileName == null) return false;
        
        try {
            File backupDir = FileUtils.getAppExternalFilesDir(context, "backups");
            if (backupDir != null) {
                File backupFile = new File(backupDir, backupFileName);
                if (backupFile.exists()) {
                    return backupFile.delete();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting backup: " + backupFileName, e);
        }
        
        return false;
    }
} 