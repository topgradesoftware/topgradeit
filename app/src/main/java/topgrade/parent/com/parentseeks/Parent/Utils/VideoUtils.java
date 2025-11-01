package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaMetadataRetriever;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VideoUtils {
    
    private static final String TAG = "VideoUtils";
    
    /**
     * Save video to app's private directory
     */
    public static File saveVideoToAppDir(Context context, byte[] videoData, String fileName) {
        if (context == null || videoData == null) return null;
        
        try {
            // Use app's private external files directory
            File videosDir = FileUtils.getAppExternalFilesDir(context, Environment.DIRECTORY_MOVIES);
            if (videosDir == null) {
                Log.e(TAG, "Cannot access app external files directory");
                return null;
            }
            
            File videoFile = new File(videosDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(videoFile)) {
                fos.write(videoData);
                fos.flush();
                Log.d(TAG, "Video saved to app directory: " + videoFile.getAbsolutePath());
                return videoFile;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving video to app directory", e);
            return null;
        }
    }
    
    /**
     * Save video to gallery using modern API
     */
    public static Uri saveVideoToGallery(Context context, byte[] videoData, String fileName) {
        if (context == null || videoData == null) return null;
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore API for Android 10+
                return saveVideoToMediaStore(context, videoData, fileName);
            } else {
                // Use external storage for older versions
                return saveVideoToExternalStorage(context, videoData, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving video to gallery", e);
            return null;
        }
    }
    
    /**
     * Save video using MediaStore API (Android 10+)
     */
    private static Uri saveVideoToMediaStore(Context context, byte[] videoData, String fileName) {
        ContentResolver resolver = context.getContentResolver();
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/ParentSeeks");
        
        Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(videoData);
                    outputStream.flush();
                    Log.d(TAG, "Video saved to MediaStore: " + uri);
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
     * Save video to external storage (pre-Android 10)
     */
    private static Uri saveVideoToExternalStorage(Context context, byte[] videoData, String fileName) {
        // Check if external storage is available
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "External storage not available");
            return null;
        }
        
        // Create directory if it doesn't exist
        File moviesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "ParentSeeks");
        if (!moviesDir.exists() && !moviesDir.mkdirs()) {
            Log.e(TAG, "Failed to create movies directory");
            return null;
        }
        
        // Create file
        File videoFile = new File(moviesDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(videoFile)) {
            fos.write(videoData);
            fos.flush();
            
            // Notify media scanner
            Uri uri = Uri.fromFile(videoFile);
            Log.d(TAG, "Video saved to external storage: " + uri);
            return uri;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to external storage", e);
            return null;
        }
    }
    
    /**
     * Copy video file from one location to another
     */
    public static boolean copyVideoFile(File sourceFile, File destFile) {
        return FileUtils.copyFile(sourceFile, destFile);
    }
    
    /**
     * Get video duration in milliseconds
     */
    public static long getVideoDuration(String videoPath) {
        if (videoPath == null) return -1;
        
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                return Long.parseLong(durationStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting video duration: " + videoPath, e);
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e);
            }
        }
        return -1;
    }
    
    /**
     * Get video duration in formatted string (MM:SS)
     */
    public static String getVideoDurationString(String videoPath) {
        long duration = getVideoDuration(videoPath);
        if (duration <= 0) return "00:00";
        
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    
    /**
     * Get video file size in human readable format
     */
    public static String getVideoFileSize(String videoPath) {
        if (videoPath == null) return "0 B";
        
        try {
            File videoFile = new File(videoPath);
            if (videoFile.exists()) {
                return FileUtils.getFileSizeString(videoFile.length());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting video file size", e);
        }
        return "0 B";
    }
    
    /**
     * Generate unique video filename
     */
    public static String generateVideoFileName(String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return prefix + "_" + timeStamp + ".mp4";
    }
    
    /**
     * Check if file is a valid video
     */
    public static boolean isValidVideoFile(String filePath) {
        if (filePath == null) return false;
        
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            return duration != null && Long.parseLong(duration) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if file is valid video", e);
            return false;
        }
    }
    
    /**
     * Get video resolution
     */
    public static String getVideoResolution(String videoPath) {
        if (videoPath == null) return "Unknown";
        
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            
            if (width != null && height != null) {
                return width + "x" + height;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting video resolution", e);
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e);
            }
        }
        return "Unknown";
    }
    
    /**
     * Get video bitrate
     */
    public static String getVideoBitrate(String videoPath) {
        if (videoPath == null) return "Unknown";
        
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            
            if (bitrate != null) {
                long bitrateLong = Long.parseLong(bitrate);
                if (bitrateLong > 1000000) {
                    return String.format(Locale.getDefault(), "%.1f Mbps", bitrateLong / 1000000.0);
                } else if (bitrateLong > 1000) {
                    return String.format(Locale.getDefault(), "%.0f Kbps", bitrateLong / 1000.0);
                } else {
                    return bitrateLong + " bps";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting video bitrate", e);
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e);
            }
        }
        return "Unknown";
    }
} 