package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressionUtils {
    
    private static final String TAG = "CompressionUtils";
    private static final int BUFFER_SIZE = 8192;
    
    /**
     * Compress file to ZIP
     */
    public static File compressFile(Context context, File inputFile, String outputFileName) {
        if (context == null || inputFile == null || !inputFile.exists()) {
            return null;
        }
        
        try {
            // Use app's private external files directory
            File compressedDir = FileUtils.getAppExternalFilesDir(context, "compressed");
            if (compressedDir == null) {
                Log.e(TAG, "Cannot access compressed directory");
                return null;
            }
            
            File outputFile = new File(compressedDir, outputFileName);
            
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
                addFileToZip(zos, inputFile, inputFile.getName());
            }
            
            Log.d(TAG, "File compressed successfully: " + outputFile.getAbsolutePath());
            return outputFile;
            
        } catch (IOException e) {
            Log.e(TAG, "Error compressing file", e);
            return null;
        }
    }
    
    /**
     * Compress multiple files to ZIP
     */
    public static File compressFiles(Context context, File[] inputFiles, String outputFileName) {
        if (context == null || inputFiles == null || inputFiles.length == 0) {
            return null;
        }
        
        try {
            // Use app's private external files directory
            File compressedDir = FileUtils.getAppExternalFilesDir(context, "compressed");
            if (compressedDir == null) {
                Log.e(TAG, "Cannot access compressed directory");
                return null;
            }
            
            File outputFile = new File(compressedDir, outputFileName);
            
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
                for (File inputFile : inputFiles) {
                    if (inputFile != null && inputFile.exists()) {
                        addFileToZip(zos, inputFile, inputFile.getName());
                    }
                }
            }
            
            Log.d(TAG, "Files compressed successfully: " + outputFile.getAbsolutePath());
            return outputFile;
            
        } catch (IOException e) {
            Log.e(TAG, "Error compressing files", e);
            return null;
        }
    }
    
    /**
     * Compress directory to ZIP
     */
    public static File compressDirectory(Context context, File inputDirectory, String outputFileName) {
        if (context == null || inputDirectory == null || !inputDirectory.exists() || !inputDirectory.isDirectory()) {
            return null;
        }
        
        try {
            // Use app's private external files directory
            File compressedDir = FileUtils.getAppExternalFilesDir(context, "compressed");
            if (compressedDir == null) {
                Log.e(TAG, "Cannot access compressed directory");
                return null;
            }
            
            File outputFile = new File(compressedDir, outputFileName);
            
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
                addDirectoryToZip(zos, inputDirectory, inputDirectory.getName());
            }
            
            Log.d(TAG, "Directory compressed successfully: " + outputFile.getAbsolutePath());
            return outputFile;
            
        } catch (IOException e) {
            Log.e(TAG, "Error compressing directory", e);
            return null;
        }
    }
    
    /**
     * Decompress ZIP file
     */
    public static boolean decompressFile(Context context, File zipFile, String outputDirectoryName) {
        if (context == null || zipFile == null || !zipFile.exists()) {
            return false;
        }
        
        try {
            // Use app's private external files directory
            File decompressedDir = FileUtils.getAppExternalFilesDir(context, "decompressed");
            if (decompressedDir == null) {
                Log.e(TAG, "Cannot access decompressed directory");
                return false;
            }
            
            File outputDir = new File(decompressedDir, outputDirectoryName);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    File outputFile = new File(outputDir, entry.getName());
                    
                    if (entry.isDirectory()) {
                        outputFile.mkdirs();
                    } else {
                        // Create parent directories if they don't exist
                        File parentDir = outputFile.getParentFile();
                        if (parentDir != null && !parentDir.exists()) {
                            parentDir.mkdirs();
                        }
                        
                        // Extract file
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int bytesRead;
                            while ((bytesRead = zis.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                    
                    zis.closeEntry();
                }
            }
            
            Log.d(TAG, "File decompressed successfully: " + outputDir.getAbsolutePath());
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Error decompressing file", e);
            return false;
        }
    }
    
    /**
     * Add file to ZIP
     */
    private static void addFileToZip(ZipOutputStream zos, File file, String entryName) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
            
            zos.closeEntry();
        }
    }
    
    /**
     * Add directory to ZIP recursively
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
     * Get compressed file size
     */
    public static String getCompressedFileSize(File compressedFile) {
        if (compressedFile == null || !compressedFile.exists()) return "0 B";
        
        return FileUtils.getFileSizeString(compressedFile.length());
    }
    
    /**
     * Get compression ratio
     */
    public static double getCompressionRatio(File originalFile, File compressedFile) {
        if (originalFile == null || compressedFile == null || 
            !originalFile.exists() || !compressedFile.exists()) {
            return 0.0;
        }
        
        long originalSize = originalFile.length();
        long compressedSize = compressedFile.length();
        
        if (originalSize > 0) {
            return (double) compressedSize / originalSize * 100.0;
        }
        
        return 0.0;
    }
    
    /**
     * List compressed files
     */
    public static File[] listCompressedFiles(Context context) {
        if (context == null) return new File[0];
        
        try {
            File compressedDir = FileUtils.getAppExternalFilesDir(context, "compressed");
            if (compressedDir != null && compressedDir.exists()) {
                File[] files = compressedDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".zip"));
                return files != null ? files : new File[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing compressed files", e);
        }
        
        return new File[0];
    }
    
    /**
     * List decompressed directories
     */
    public static File[] listDecompressedDirectories(Context context) {
        if (context == null) return new File[0];
        
        try {
            File decompressedDir = FileUtils.getAppExternalFilesDir(context, "decompressed");
            if (decompressedDir != null && decompressedDir.exists()) {
                File[] files = decompressedDir.listFiles(File::isDirectory);
                return files != null ? files : new File[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing decompressed directories", e);
        }
        
        return new File[0];
    }
    
    /**
     * Delete compressed file
     */
    public static boolean deleteCompressedFile(Context context, String fileName) {
        if (context == null || fileName == null) return false;
        
        try {
            File compressedDir = FileUtils.getAppExternalFilesDir(context, "compressed");
            if (compressedDir != null) {
                File compressedFile = new File(compressedDir, fileName);
                if (compressedFile.exists()) {
                    return compressedFile.delete();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting compressed file: " + fileName, e);
        }
        
        return false;
    }
    
    /**
     * Delete decompressed directory
     */
    public static boolean deleteDecompressedDirectory(Context context, String directoryName) {
        if (context == null || directoryName == null) return false;
        
        try {
            File decompressedDir = FileUtils.getAppExternalFilesDir(context, "decompressed");
            if (decompressedDir != null) {
                File directory = new File(decompressedDir, directoryName);
                if (directory.exists() && directory.isDirectory()) {
                    return deleteDirectory(directory);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting decompressed directory: " + directoryName, e);
        }
        
        return false;
    }
    
    /**
     * Clear all compressed files
     */
    public static boolean clearAllCompressedFiles(Context context) {
        if (context == null) return false;
        
        try {
            File compressedDir = FileUtils.getAppExternalFilesDir(context, "compressed");
            if (compressedDir != null && compressedDir.exists()) {
                File[] compressedFiles = listCompressedFiles(context);
                boolean allDeleted = true;
                
                for (File compressedFile : compressedFiles) {
                    if (!compressedFile.delete()) {
                        allDeleted = false;
                    }
                }
                
                return allDeleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all compressed files", e);
        }
        
        return false;
    }
    
    /**
     * Clear all decompressed directories
     */
    public static boolean clearAllDecompressedDirectories(Context context) {
        if (context == null) return false;
        
        try {
            File decompressedDir = FileUtils.getAppExternalFilesDir(context, "decompressed");
            if (decompressedDir != null && decompressedDir.exists()) {
                File[] directories = listDecompressedDirectories(context);
                boolean allDeleted = true;
                
                for (File directory : directories) {
                    if (!deleteDirectory(directory)) {
                        allDeleted = false;
                    }
                }
                
                return allDeleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all decompressed directories", e);
        }
        
        return false;
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
} 