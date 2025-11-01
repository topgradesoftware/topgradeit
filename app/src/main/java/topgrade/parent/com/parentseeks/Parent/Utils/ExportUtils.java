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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportUtils {
    
    private static final String TAG = "ExportUtils";
    
    /**
     * Export data to app's private directory
     */
    public static File exportDataToAppDir(Context context, byte[] data, String fileName) {
        if (context == null || data == null) return null;
        
        try {
            // Use app's private external files directory
            File exportDir = FileUtils.getAppExternalFilesDir(context, "exports");
            if (exportDir == null) {
                Log.e(TAG, "Cannot access app external files directory");
                return null;
            }
            
            File exportFile = new File(exportDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(exportFile)) {
                fos.write(data);
                fos.flush();
                Log.d(TAG, "Data exported to app directory: " + exportFile.getAbsolutePath());
                return exportFile;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error exporting data to app directory", e);
            return null;
        }
    }
    
    /**
     * Export data to downloads using modern API
     */
    public static Uri exportDataToDownloads(Context context, byte[] data, String fileName, String mimeType) {
        if (context == null || data == null) return null;
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore API for Android 10+
                return exportDataToMediaStore(context, data, fileName, mimeType);
            } else {
                // Use external storage for older versions
                return exportDataToExternalStorage(context, data, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error exporting data to downloads", e);
            return null;
        }
    }
    
    /**
     * Export data using MediaStore API (Android 10+)
     */
    private static Uri exportDataToMediaStore(Context context, byte[] data, String fileName, String mimeType) {
        ContentResolver resolver = context.getContentResolver();
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ParentSeeks/Exports");
        
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(data);
                    outputStream.flush();
                    Log.d(TAG, "Data exported to MediaStore: " + uri);
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
     * Export data to external storage (pre-Android 10)
     */
    private static Uri exportDataToExternalStorage(Context context, byte[] data, String fileName) {
        // Check if external storage is available
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "External storage not available");
            return null;
        }
        
        // Create directory if it doesn't exist
        File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ParentSeeks/Exports");
        if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
            Log.e(TAG, "Failed to create downloads directory");
            return null;
        }
        
        // Create file
        File exportFile = new File(downloadsDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(exportFile)) {
            fos.write(data);
            fos.flush();
            
            // Notify media scanner
            Uri uri = Uri.fromFile(exportFile);
            Log.d(TAG, "Data exported to external storage: " + uri);
            return uri;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to external storage", e);
            return null;
        }
    }
    
    /**
     * Export CSV data
     */
    public static Uri exportCSV(Context context, String csvData, String fileName) {
        if (context == null || csvData == null) return null;
        
        byte[] data = csvData.getBytes();
        return exportDataToDownloads(context, data, fileName, "text/csv");
    }
    
    /**
     * Export JSON data
     */
    public static Uri exportJSON(Context context, String jsonData, String fileName) {
        if (context == null || jsonData == null) return null;
        
        byte[] data = jsonData.getBytes();
        return exportDataToDownloads(context, data, fileName, "application/json");
    }
    
    /**
     * Export XML data
     */
    public static Uri exportXML(Context context, String xmlData, String fileName) {
        if (context == null || xmlData == null) return null;
        
        byte[] data = xmlData.getBytes();
        return exportDataToDownloads(context, data, fileName, "application/xml");
    }
    
    /**
     * Export PDF data
     */
    public static Uri exportPDF(Context context, byte[] pdfData, String fileName) {
        if (context == null || pdfData == null) return null;
        
        return exportDataToDownloads(context, pdfData, fileName, "application/pdf");
    }
    
    /**
     * Export Excel data
     */
    public static Uri exportExcel(Context context, byte[] excelData, String fileName) {
        if (context == null || excelData == null) return null;
        
        return exportDataToDownloads(context, excelData, fileName, 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
    
    /**
     * Generate export filename with timestamp
     */
    public static String generateExportFileName(String prefix, String extension) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return prefix + "_" + timeStamp + "." + extension;
    }
    
    /**
     * Get export file size
     */
    public static String getExportFileSize(File exportFile) {
        if (exportFile == null || !exportFile.exists()) return "0 B";
        
        return FileUtils.getFileSizeString(exportFile.length());
    }
    
    /**
     * List available exports
     */
    public static File[] listExports(Context context) {
        if (context == null) return new File[0];
        
        try {
            File exportDir = FileUtils.getAppExternalFilesDir(context, "exports");
            if (exportDir != null && exportDir.exists()) {
                File[] files = exportDir.listFiles();
                return files != null ? files : new File[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing exports", e);
        }
        
        return new File[0];
    }
    
    /**
     * Delete export file
     */
    public static boolean deleteExport(Context context, String exportFileName) {
        if (context == null || exportFileName == null) return false;
        
        try {
            File exportDir = FileUtils.getAppExternalFilesDir(context, "exports");
            if (exportDir != null) {
                File exportFile = new File(exportDir, exportFileName);
                if (exportFile.exists()) {
                    return exportFile.delete();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting export: " + exportFileName, e);
        }
        
        return false;
    }
    
    /**
     * Clear all exports
     */
    public static boolean clearAllExports(Context context) {
        if (context == null) return false;
        
        try {
            File exportDir = FileUtils.getAppExternalFilesDir(context, "exports");
            if (exportDir != null && exportDir.exists()) {
                File[] exportFiles = listExports(context);
                boolean allDeleted = true;
                
                for (File exportFile : exportFiles) {
                    if (!exportFile.delete()) {
                        allDeleted = false;
                    }
                }
                
                return allDeleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all exports", e);
        }
        
        return false;
    }
    
    /**
     * Get total exports size
     */
    public static String getTotalExportsSize(Context context) {
        if (context == null) return "0 B";
        
        try {
            File[] exportFiles = listExports(context);
            long totalSize = 0;
            
            for (File exportFile : exportFiles) {
                totalSize += exportFile.length();
            }
            
            return FileUtils.getFileSizeString(totalSize);
        } catch (Exception e) {
            Log.e(TAG, "Error getting total exports size", e);
        }
        
        return "0 B";
    }
} 