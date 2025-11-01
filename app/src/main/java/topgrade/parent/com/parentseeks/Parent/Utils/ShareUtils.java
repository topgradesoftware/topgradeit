package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

public class ShareUtils {
    
    private static final String TAG = "ShareUtils";
    private static final String FILE_PROVIDER_AUTHORITY = "topgrade.parent.com.parentseeks.fileprovider";
    
    /**
     * Share file using FileProvider (modern approach)
     */
    public static void shareFile(Context context, File file, String mimeType, String title) {
        if (context == null || file == null || !file.exists()) {
            Log.e(TAG, "Invalid parameters for sharing file");
            return;
        }
        
        try {
            // Get content URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Add title if provided
            if (title != null && !title.isEmpty()) {
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                shareIntent.putExtra(Intent.EXTRA_TEXT, title);
            }
            
            // Start sharing activity
            Intent chooser = Intent.createChooser(shareIntent, "Share via");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
            
            Log.d(TAG, "File shared successfully: " + file.getName());
            
        } catch (Exception e) {
            Log.e(TAG, "Error sharing file: " + file.getName(), e);
        }
    }
    
    /**
     * Share text content
     */
    public static void shareText(Context context, String text, String title) {
        if (context == null || text == null) {
            Log.e(TAG, "Invalid parameters for sharing text");
            return;
        }
        
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            
            // Add title if provided
            if (title != null && !title.isEmpty()) {
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            }
            
            // Start sharing activity
            Intent chooser = Intent.createChooser(shareIntent, "Share via");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
            
            Log.d(TAG, "Text shared successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error sharing text", e);
        }
    }
    
    /**
     * Share multiple files
     */
    public static void shareMultipleFiles(Context context, File[] files, String mimeType, String title) {
        if (context == null || files == null || files.length == 0) {
            Log.e(TAG, "Invalid parameters for sharing multiple files");
            return;
        }
        
        try {
            Uri[] contentUris = new Uri[files.length];
            for (int i = 0; i < files.length; i++) {
                if (files[i] != null && files[i].exists()) {
                    contentUris[i] = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, files[i]);
                }
            }
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType(mimeType);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(java.util.Arrays.asList(contentUris)));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Add title if provided
            if (title != null && !title.isEmpty()) {
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                shareIntent.putExtra(Intent.EXTRA_TEXT, title);
            }
            
            // Start sharing activity
            Intent chooser = Intent.createChooser(shareIntent, "Share via");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
            
            Log.d(TAG, "Multiple files shared successfully: " + files.length + " files");
            
        } catch (Exception e) {
            Log.e(TAG, "Error sharing multiple files", e);
        }
    }
    
    /**
     * Share image file
     */
    public static void shareImage(Context context, File imageFile, String title) {
        shareFile(context, imageFile, "image/*", title);
    }
    
    /**
     * Share video file
     */
    public static void shareVideo(Context context, File videoFile, String title) {
        shareFile(context, videoFile, "video/*", title);
    }
    
    /**
     * Share audio file
     */
    public static void shareAudio(Context context, File audioFile, String title) {
        shareFile(context, audioFile, "audio/*", title);
    }
    
    /**
     * Share document file
     */
    public static void shareDocument(Context context, File documentFile, String title) {
        shareFile(context, documentFile, "application/*", title);
    }
    
    /**
     * Share PDF file
     */
    public static void sharePDF(Context context, File pdfFile, String title) {
        shareFile(context, pdfFile, "application/pdf", title);
    }
    
    /**
     * Share CSV file
     */
    public static void shareCSV(Context context, File csvFile, String title) {
        shareFile(context, csvFile, "text/csv", title);
    }
    
    /**
     * Share JSON file
     */
    public static void shareJSON(Context context, File jsonFile, String title) {
        shareFile(context, jsonFile, "application/json", title);
    }
    
    /**
     * Share XML file
     */
    public static void shareXML(Context context, File xmlFile, String title) {
        shareFile(context, xmlFile, "application/xml", title);
    }
    
    /**
     * Share Excel file
     */
    public static void shareExcel(Context context, File excelFile, String title) {
        shareFile(context, excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", title);
    }
    
    /**
     * Share Word document
     */
    public static void shareWord(Context context, File wordFile, String title) {
        shareFile(context, wordFile, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", title);
    }
    
    /**
     * Share PowerPoint presentation
     */
    public static void sharePowerPoint(Context context, File pptFile, String title) {
        shareFile(context, pptFile, "application/vnd.openxmlformats-officedocument.presentationml.presentation", title);
    }
    
    /**
     * Share ZIP archive
     */
    public static void shareZIP(Context context, File zipFile, String title) {
        shareFile(context, zipFile, "application/zip", title);
    }
    
    /**
     * Share backup file
     */
    public static void shareBackup(Context context, File backupFile, String title) {
        shareFile(context, backupFile, "application/zip", title);
    }
    
    /**
     * Share log file
     */
    public static void shareLog(Context context, File logFile, String title) {
        shareFile(context, logFile, "text/plain", title);
    }
    
    /**
     * Share export file
     */
    public static void shareExport(Context context, File exportFile, String title) {
        // Determine MIME type based on file extension
        String fileName = exportFile.getName().toLowerCase();
        String mimeType = "application/octet-stream";
        
        if (fileName.endsWith(".csv")) {
            mimeType = "text/csv";
        } else if (fileName.endsWith(".json")) {
            mimeType = "application/json";
        } else if (fileName.endsWith(".xml")) {
            mimeType = "application/xml";
        } else if (fileName.endsWith(".pdf")) {
            mimeType = "application/pdf";
        } else if (fileName.endsWith(".xlsx")) {
            mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".docx")) {
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileName.endsWith(".pptx")) {
            mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (fileName.endsWith(".txt")) {
            mimeType = "text/plain";
        }
        
        shareFile(context, exportFile, mimeType, title);
    }
    
    /**
     * Share app data summary
     */
    public static void shareAppDataSummary(Context context, String summary) {
        if (context == null || summary == null) {
            Log.e(TAG, "Invalid parameters for sharing app data summary");
            return;
        }
        
        try {
            // Create a temporary file with the summary
            File tempFile = CacheUtils.createTempFile(context, "summary_", ".txt");
            if (tempFile != null) {
                // Write summary to file
                try (java.io.FileWriter writer = new java.io.FileWriter(tempFile)) {
                    writer.write(summary);
                    writer.flush();
                }
                
                // Share the file
                shareFile(context, tempFile, "text/plain", "App Data Summary");
                
                // Clean up temp file after sharing
                tempFile.deleteOnExit();
            } else {
                // Fallback to sharing text directly
                shareText(context, summary, "App Data Summary");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error sharing app data summary", e);
            // Fallback to sharing text directly
            shareText(context, summary, "App Data Summary");
        }
    }
    
    /**
     * Share error report
     */
    public static void shareErrorReport(Context context, String errorReport) {
        if (context == null || errorReport == null) {
            Log.e(TAG, "Invalid parameters for sharing error report");
            return;
        }
        
        try {
            // Create a temporary file with the error report
            File tempFile = CacheUtils.createTempFile(context, "error_report_", ".txt");
            if (tempFile != null) {
                // Write error report to file
                try (java.io.FileWriter writer = new java.io.FileWriter(tempFile)) {
                    writer.write(errorReport);
                    writer.flush();
                }
                
                // Share the file
                shareFile(context, tempFile, "text/plain", "Error Report");
                
                // Clean up temp file after sharing
                tempFile.deleteOnExit();
            } else {
                // Fallback to sharing text directly
                shareText(context, errorReport, "Error Report");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error sharing error report", e);
            // Fallback to sharing text directly
            shareText(context, errorReport, "Error Report");
        }
    }
} 