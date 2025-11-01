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

public class DocumentUtils {
    
    private static final String TAG = "DocumentUtils";
    
    /**
     * Save document to app's private directory
     */
    public static File saveDocumentToAppDir(Context context, byte[] documentData, String fileName) {
        if (context == null || documentData == null) return null;
        
        try {
            // Use app's private external files directory
            File documentsDir = FileUtils.getAppExternalFilesDir(context, Environment.DIRECTORY_DOCUMENTS);
            if (documentsDir == null) {
                Log.e(TAG, "Cannot access app external files directory");
                return null;
            }
            
            File documentFile = new File(documentsDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(documentFile)) {
                fos.write(documentData);
                fos.flush();
                Log.d(TAG, "Document saved to app directory: " + documentFile.getAbsolutePath());
                return documentFile;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving document to app directory", e);
            return null;
        }
    }
    
    /**
     * Save document to downloads using modern API
     */
    public static Uri saveDocumentToDownloads(Context context, byte[] documentData, String fileName, String mimeType) {
        if (context == null || documentData == null) return null;
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore API for Android 10+
                return saveDocumentToMediaStore(context, documentData, fileName, mimeType);
            } else {
                // Use external storage for older versions
                return saveDocumentToExternalStorage(context, documentData, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving document to downloads", e);
            return null;
        }
    }
    
    /**
     * Save document using MediaStore API (Android 10+)
     */
    private static Uri saveDocumentToMediaStore(Context context, byte[] documentData, String fileName, String mimeType) {
        ContentResolver resolver = context.getContentResolver();
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ParentSeeks");
        
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(documentData);
                    outputStream.flush();
                    Log.d(TAG, "Document saved to MediaStore: " + uri);
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
     * Save document to external storage (pre-Android 10)
     */
    private static Uri saveDocumentToExternalStorage(Context context, byte[] documentData, String fileName) {
        // Check if external storage is available
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "External storage not available");
            return null;
        }
        
        // Create directory if it doesn't exist
        File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ParentSeeks");
        if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
            Log.e(TAG, "Failed to create downloads directory");
            return null;
        }
        
        // Create file
        File documentFile = new File(downloadsDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(documentFile)) {
            fos.write(documentData);
            fos.flush();
            
            // Notify media scanner
            Uri uri = Uri.fromFile(documentFile);
            Log.d(TAG, "Document saved to external storage: " + uri);
            return uri;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to external storage", e);
            return null;
        }
    }
    
    /**
     * Generate unique document filename
     */
    public static String generateDocumentFileName(String prefix, String extension) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return prefix + "_" + timeStamp + "." + extension;
    }
    
    /**
     * Get document file size in human readable format
     */
    public static String getDocumentFileSize(String documentPath) {
        if (documentPath == null) return "0 B";
        
        try {
            File documentFile = new File(documentPath);
            if (documentFile.exists()) {
                return FileUtils.getFileSizeString(documentFile.length());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting document file size", e);
        }
        return "0 B";
    }
    
    /**
     * Check if file is a valid document
     */
    public static boolean isValidDocumentFile(String filePath) {
        if (filePath == null) return false;
        
        try {
            File file = new File(filePath);
            return file.exists() && file.length() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if file is valid document", e);
            return false;
        }
    }
    
    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Get MIME type from file extension
     */
    public static String getMimeTypeFromExtension(String extension) {
        if (extension == null) return "application/octet-stream";
        
        switch (extension.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "rtf":
                return "application/rtf";
            case "csv":
                return "text/csv";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            case "7z":
                return "application/x-7z-compressed";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * Check if file is a supported document type
     */
    public static boolean isSupportedDocumentType(String fileName) {
        if (fileName == null) return false;
        
        String extension = getFileExtension(fileName);
        return !extension.isEmpty() && !getMimeTypeFromExtension(extension).equals("application/octet-stream");
    }
    
    /**
     * Get human readable file type name
     */
    public static String getFileTypeName(String fileName) {
        if (fileName == null) return "Unknown";
        
        String extension = getFileExtension(fileName);
        switch (extension.toLowerCase()) {
            case "pdf":
                return "PDF Document";
            case "doc":
            case "docx":
                return "Word Document";
            case "xls":
            case "xlsx":
                return "Excel Spreadsheet";
            case "ppt":
            case "pptx":
                return "PowerPoint Presentation";
            case "txt":
                return "Text File";
            case "rtf":
                return "Rich Text Document";
            case "csv":
                return "CSV File";
            case "zip":
                return "ZIP Archive";
            case "rar":
                return "RAR Archive";
            case "7z":
                return "7-Zip Archive";
            default:
                return "Document";
        }
    }
} 