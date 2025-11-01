package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogUtils {
    
    private static final String TAG = "LogUtils";
    private static final String LOG_FILE_PREFIX = "app_log_";
    private static final String LOG_FILE_EXTENSION = ".txt";
    
    /**
     * Write log to app's private directory
     */
    public static void writeLogToFile(Context context, String logMessage, String logLevel) {
        if (context == null || logMessage == null) return;
        
        try {
            // Use app's private external files directory
            File logsDir = FileUtils.getAppExternalFilesDir(context, "logs");
            if (logsDir == null) {
                Log.e(TAG, "Cannot access app external files directory");
                return;
            }
            
            String fileName = generateLogFileName();
            File logFile = new File(logsDir, fileName);
            
            // Create log entry with timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String logEntry = String.format("[%s] %s: %s\n", timestamp, logLevel.toUpperCase(), logMessage);
            
            // Append to log file
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(logEntry);
                writer.flush();
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error writing log to file", e);
        }
    }
    
    /**
     * Write debug log
     */
    public static void d(Context context, String message) {
        Log.d(TAG, message);
        writeLogToFile(context, message, "DEBUG");
    }
    
    /**
     * Write info log
     */
    public static void i(Context context, String message) {
        Log.i(TAG, message);
        writeLogToFile(context, message, "INFO");
    }
    
    /**
     * Write warning log
     */
    public static void w(Context context, String message) {
        Log.w(TAG, message);
        writeLogToFile(context, message, "WARNING");
    }
    
    /**
     * Write error log
     */
    public static void e(Context context, String message) {
        Log.e(TAG, message);
        writeLogToFile(context, message, "ERROR");
    }
    
    /**
     * Write error log with exception
     */
    public static void e(Context context, String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
        String fullMessage = message + "\n" + Log.getStackTraceString(throwable);
        writeLogToFile(context, fullMessage, "ERROR");
    }
    
    /**
     * Generate log filename with timestamp
     */
    public static String generateLogFileName() {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return LOG_FILE_PREFIX + date + LOG_FILE_EXTENSION;
    }
    
    /**
     * Get log file for today
     */
    public static File getTodayLogFile(Context context) {
        if (context == null) return null;
        
        try {
            File logsDir = FileUtils.getAppExternalFilesDir(context, "logs");
            if (logsDir != null) {
                String fileName = generateLogFileName();
                File logFile = new File(logsDir, fileName);
                return logFile.exists() ? logFile : null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting today's log file", e);
        }
        
        return null;
    }
    
    /**
     * Get all log files
     */
    public static File[] getAllLogFiles(Context context) {
        if (context == null) return new File[0];
        
        try {
            File logsDir = FileUtils.getAppExternalFilesDir(context, "logs");
            if (logsDir != null && logsDir.exists()) {
                File[] files = logsDir.listFiles((dir, name) -> 
                    name.startsWith(LOG_FILE_PREFIX) && name.endsWith(LOG_FILE_EXTENSION));
                return files != null ? files : new File[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all log files", e);
        }
        
        return new File[0];
    }
    
    /**
     * Clear all log files
     */
    public static boolean clearAllLogs(Context context) {
        if (context == null) return false;
        
        try {
            File logsDir = FileUtils.getAppExternalFilesDir(context, "logs");
            if (logsDir != null && logsDir.exists()) {
                File[] logFiles = getAllLogFiles(context);
                boolean allDeleted = true;
                
                for (File logFile : logFiles) {
                    if (!logFile.delete()) {
                        allDeleted = false;
                    }
                }
                
                return allDeleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all logs", e);
        }
        
        return false;
    }
    
    /**
     * Get log file size
     */
    public static String getLogFileSize(File logFile) {
        if (logFile == null || !logFile.exists()) return "0 B";
        
        return FileUtils.getFileSizeString(logFile.length());
    }
    
    /**
     * Get total logs size
     */
    public static String getTotalLogsSize(Context context) {
        if (context == null) return "0 B";
        
        try {
            File[] logFiles = getAllLogFiles(context);
            long totalSize = 0;
            
            for (File logFile : logFiles) {
                totalSize += logFile.length();
            }
            
            return FileUtils.getFileSizeString(totalSize);
        } catch (Exception e) {
            Log.e(TAG, "Error getting total logs size", e);
        }
        
        return "0 B";
    }
    
    /**
     * Delete old log files (older than specified days)
     */
    public static boolean deleteOldLogs(Context context, int daysToKeep) {
        if (context == null || daysToKeep < 0) return false;
        
        try {
            File[] logFiles = getAllLogFiles(context);
            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L);
            boolean anyDeleted = false;
            
            for (File logFile : logFiles) {
                if (logFile.lastModified() < cutoffTime) {
                    if (logFile.delete()) {
                        anyDeleted = true;
                    }
                }
            }
            
            return anyDeleted;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting old logs", e);
        }
        
        return false;
    }
    
    /**
     * Export logs to downloads
     */
    public static boolean exportLogs(Context context) {
        if (context == null) return false;
        
        try {
            File[] logFiles = getAllLogFiles(context);
            if (logFiles.length == 0) {
                Log.w(TAG, "No log files to export");
                return false;
            }
            
            // Create a combined log file
            String combinedFileName = "combined_logs_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".txt";
            
            File combinedLogFile = new File(FileUtils.getAppExternalFilesDir(context, "logs"), combinedFileName);
            
            try (FileWriter writer = new FileWriter(combinedLogFile)) {
                for (File logFile : logFiles) {
                    writer.write("=== " + logFile.getName() + " ===\n");
                    // Read and write log file content
                    String content = FileUtils.readFileToString(logFile);
                    if (content != null) {
                        writer.write(content);
                    }
                    writer.write("\n\n");
                }
                writer.flush();
            }
            
            // Save to downloads
            Uri uri = DocumentUtils.saveDocumentToDownloads(context, 
                FileUtils.readFileToByteArray(combinedLogFile), 
                combinedFileName, 
                "text/plain");
            
            return uri != null;
            
        } catch (Exception e) {
            Log.e(TAG, "Error exporting logs", e);
        }
        
        return false;
    }
} 