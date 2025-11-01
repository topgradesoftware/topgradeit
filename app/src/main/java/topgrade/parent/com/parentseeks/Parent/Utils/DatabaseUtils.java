package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DatabaseUtils {
    
    private static final String TAG = "DatabaseUtils";
    
    /**
     * Get database file
     */
    public static File getDatabaseFile(Context context, String databaseName) {
        if (context == null || databaseName == null) return null;
        
        try {
            return context.getDatabasePath(databaseName);
        } catch (Exception e) {
            Log.e(TAG, "Error getting database file: " + databaseName, e);
            return null;
        }
    }
    
    /**
     * Get database directory
     */
    public static File getDatabaseDirectory(Context context) {
        if (context == null) return null;
        
        try {
            File dbFile = context.getDatabasePath("temp.db");
            return dbFile.getParentFile();
        } catch (Exception e) {
            Log.e(TAG, "Error getting database directory", e);
            return null;
        }
    }
    
    /**
     * List all database files
     */
    public static File[] listDatabaseFiles(Context context) {
        if (context == null) return new File[0];
        
        try {
            File dbDir = getDatabaseDirectory(context);
            if (dbDir != null && dbDir.exists()) {
                File[] files = dbDir.listFiles((dir, name) -> name.endsWith(".db"));
                return files != null ? files : new File[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing database files", e);
        }
        
        return new File[0];
    }
    
    /**
     * Get database size
     */
    public static long getDatabaseSize(Context context, String databaseName) {
        if (context == null || databaseName == null) return 0;
        
        try {
            File dbFile = getDatabaseFile(context, databaseName);
            if (dbFile != null && dbFile.exists()) {
                return dbFile.length();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting database size: " + databaseName, e);
        }
        
        return 0;
    }
    
    /**
     * Get database size in human readable format
     */
    public static String getDatabaseSizeString(Context context, String databaseName) {
        long size = getDatabaseSize(context, databaseName);
        return FileUtils.getFileSizeString(size);
    }
    
    /**
     * Get total database size
     */
    public static long getTotalDatabaseSize(Context context) {
        if (context == null) return 0;
        
        try {
            File[] dbFiles = listDatabaseFiles(context);
            long totalSize = 0;
            
            for (File dbFile : dbFiles) {
                totalSize += dbFile.length();
            }
            
            return totalSize;
        } catch (Exception e) {
            Log.e(TAG, "Error getting total database size", e);
        }
        
        return 0;
    }
    
    /**
     * Get total database size in human readable format
     */
    public static String getTotalDatabaseSizeString(Context context) {
        long size = getTotalDatabaseSize(context);
        return FileUtils.getFileSizeString(size);
    }
    
    /**
     * Backup database to app's private directory
     */
    public static File backupDatabase(Context context, String databaseName, String backupName) {
        if (context == null || databaseName == null || backupName == null) return null;
        
        try {
            File dbFile = getDatabaseFile(context, databaseName);
            if (dbFile == null || !dbFile.exists()) {
                Log.e(TAG, "Database file not found: " + databaseName);
                return null;
            }
            
            // Use app's private external files directory
            File backupDir = FileUtils.getAppExternalFilesDir(context, "database_backups");
            if (backupDir == null) {
                Log.e(TAG, "Cannot access backup directory");
                return null;
            }
            
            File backupFile = new File(backupDir, backupName);
            
            // Copy database file
            if (FileUtils.copyFile(dbFile, backupFile)) {
                Log.d(TAG, "Database backed up successfully: " + backupFile.getAbsolutePath());
                return backupFile;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error backing up database: " + databaseName, e);
        }
        
        return null;
    }
    
    /**
     * Restore database from backup
     */
    public static boolean restoreDatabase(Context context, String databaseName, File backupFile) {
        if (context == null || databaseName == null || backupFile == null || !backupFile.exists()) {
            return false;
        }
        
        try {
            File dbFile = getDatabaseFile(context, databaseName);
            if (dbFile == null) {
                Log.e(TAG, "Cannot get database file: " + databaseName);
                return false;
            }
            
            // Copy backup file to database location
            if (FileUtils.copyFile(backupFile, dbFile)) {
                Log.d(TAG, "Database restored successfully: " + databaseName);
                return true;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error restoring database: " + databaseName, e);
        }
        
        return false;
    }
    
    /**
     * Export database to downloads
     */
    public static boolean exportDatabase(Context context, String databaseName, String exportName) {
        if (context == null || databaseName == null || exportName == null) return false;
        
        try {
            File dbFile = getDatabaseFile(context, databaseName);
            if (dbFile == null || !dbFile.exists()) {
                Log.e(TAG, "Database file not found: " + databaseName);
                return false;
            }
            
            // Read database file
            byte[] dbData = FileUtils.readFileToByteArray(dbFile);
            if (dbData == null) {
                Log.e(TAG, "Failed to read database file");
                return false;
            }
            
            // Export to downloads
            Uri uri = DocumentUtils.saveDocumentToDownloads(context, dbData, exportName, "application/x-sqlite3");
            
            return uri != null;
            
        } catch (Exception e) {
            Log.e(TAG, "Error exporting database: " + databaseName, e);
        }
        
        return false;
    }
    
    /**
     * Import database from file
     */
    public static boolean importDatabase(Context context, String databaseName, File importFile) {
        if (context == null || databaseName == null || importFile == null || !importFile.exists()) {
            return false;
        }
        
        try {
            File dbFile = getDatabaseFile(context, databaseName);
            if (dbFile == null) {
                Log.e(TAG, "Cannot get database file: " + databaseName);
                return false;
            }
            
            // Copy import file to database location
            if (FileUtils.copyFile(importFile, dbFile)) {
                Log.d(TAG, "Database imported successfully: " + databaseName);
                return true;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error importing database: " + databaseName, e);
        }
        
        return false;
    }
    
    /**
     * Delete database
     */
    public static boolean deleteDatabase(Context context, String databaseName) {
        if (context == null || databaseName == null) return false;
        
        try {
            return context.deleteDatabase(databaseName);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting database: " + databaseName, e);
            return false;
        }
    }
    
    /**
     * Check if database exists
     */
    public static boolean databaseExists(Context context, String databaseName) {
        if (context == null || databaseName == null) return false;
        
        try {
            File dbFile = getDatabaseFile(context, databaseName);
            return dbFile != null && dbFile.exists();
        } catch (Exception e) {
            Log.e(TAG, "Error checking database existence: " + databaseName, e);
            return false;
        }
    }
    
    /**
     * Get database file size
     */
    public static String getDatabaseFileSize(File dbFile) {
        if (dbFile == null || !dbFile.exists()) return "0 B";
        
        return FileUtils.getFileSizeString(dbFile.length());
    }
    
    /**
     * List database backups
     */
    public static File[] listDatabaseBackups(Context context) {
        if (context == null) return new File[0];
        
        try {
            File backupDir = FileUtils.getAppExternalFilesDir(context, "database_backups");
            if (backupDir != null && backupDir.exists()) {
                File[] files = backupDir.listFiles();
                return files != null ? files : new File[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing database backups", e);
        }
        
        return new File[0];
    }
    
    /**
     * Delete database backup
     */
    public static boolean deleteDatabaseBackup(Context context, String backupName) {
        if (context == null || backupName == null) return false;
        
        try {
            File backupDir = FileUtils.getAppExternalFilesDir(context, "database_backups");
            if (backupDir != null) {
                File backupFile = new File(backupDir, backupName);
                if (backupFile.exists()) {
                    return backupFile.delete();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting database backup: " + backupName, e);
        }
        
        return false;
    }
    
    /**
     * Clear all database backups
     */
    public static boolean clearAllDatabaseBackups(Context context) {
        if (context == null) return false;
        
        try {
            File backupDir = FileUtils.getAppExternalFilesDir(context, "database_backups");
            if (backupDir != null && backupDir.exists()) {
                File[] backupFiles = listDatabaseBackups(context);
                boolean allDeleted = true;
                
                for (File backupFile : backupFiles) {
                    if (!backupFile.delete()) {
                        allDeleted = false;
                    }
                }
                
                return allDeleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all database backups", e);
        }
        
        return false;
    }
    
    /**
     * Get total database backups size
     */
    public static String getTotalDatabaseBackupsSize(Context context) {
        if (context == null) return "0 B";
        
        try {
            File[] backupFiles = listDatabaseBackups(context);
            long totalSize = 0;
            
            for (File backupFile : backupFiles) {
                totalSize += backupFile.length();
            }
            
            return FileUtils.getFileSizeString(totalSize);
        } catch (Exception e) {
            Log.e(TAG, "Error getting total database backups size", e);
        }
        
        return "0 B";
    }
} 