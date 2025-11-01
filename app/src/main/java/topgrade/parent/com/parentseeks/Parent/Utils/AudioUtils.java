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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioUtils {
    
    private static final String TAG = "AudioUtils";
    
    /**
     * Save audio to app's private directory
     */
    public static File saveAudioToAppDir(Context context, byte[] audioData, String fileName) {
        if (context == null || audioData == null) return null;
        
        try {
            // Use app's private external files directory
            File audioDir = FileUtils.getAppExternalFilesDir(context, Environment.DIRECTORY_MUSIC);
            if (audioDir == null) {
                Log.e(TAG, "Cannot access app external files directory");
                return null;
            }
            
            File audioFile = new File(audioDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                fos.write(audioData);
                fos.flush();
                Log.d(TAG, "Audio saved to app directory: " + audioFile.getAbsolutePath());
                return audioFile;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving audio to app directory", e);
            return null;
        }
    }
    
    /**
     * Save audio to gallery using modern API
     */
    public static Uri saveAudioToGallery(Context context, byte[] audioData, String fileName) {
        if (context == null || audioData == null) return null;
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore API for Android 10+
                return saveAudioToMediaStore(context, audioData, fileName);
            } else {
                // Use external storage for older versions
                return saveAudioToExternalStorage(context, audioData, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving audio to gallery", e);
            return null;
        }
    }
    
    /**
     * Save audio using MediaStore API (Android 10+)
     */
    private static Uri saveAudioToMediaStore(Context context, byte[] audioData, String fileName) {
        ContentResolver resolver = context.getContentResolver();
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/ParentSeeks");
        
        Uri uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(audioData);
                    outputStream.flush();
                    Log.d(TAG, "Audio saved to MediaStore: " + uri);
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
     * Save audio to external storage (pre-Android 10)
     */
    private static Uri saveAudioToExternalStorage(Context context, byte[] audioData, String fileName) {
        // Check if external storage is available
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "External storage not available");
            return null;
        }
        
        // Create directory if it doesn't exist
        File musicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "ParentSeeks");
        if (!musicDir.exists() && !musicDir.mkdirs()) {
            Log.e(TAG, "Failed to create music directory");
            return null;
        }
        
        // Create file
        File audioFile = new File(musicDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(audioFile)) {
            fos.write(audioData);
            fos.flush();
            
            // Notify media scanner
            Uri uri = Uri.fromFile(audioFile);
            Log.d(TAG, "Audio saved to external storage: " + uri);
            return uri;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to external storage", e);
            return null;
        }
    }
    
    /**
     * Get audio duration in milliseconds
     */
    public static long getAudioDuration(String audioPath) {
        if (audioPath == null) return -1;
        
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(audioPath);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                return Long.parseLong(durationStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting audio duration: " + audioPath, e);
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
     * Get audio duration in formatted string (MM:SS)
     */
    public static String getAudioDurationString(String audioPath) {
        long duration = getAudioDuration(audioPath);
        if (duration <= 0) return "00:00";
        
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    
    /**
     * Get audio file size in human readable format
     */
    public static String getAudioFileSize(String audioPath) {
        if (audioPath == null) return "0 B";
        
        try {
            File audioFile = new File(audioPath);
            if (audioFile.exists()) {
                return FileUtils.getFileSizeString(audioFile.length());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting audio file size", e);
        }
        return "0 B";
    }
    
    /**
     * Generate unique audio filename
     */
    public static String generateAudioFileName(String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return prefix + "_" + timeStamp + ".mp3";
    }
    
    /**
     * Check if file is a valid audio
     */
    public static boolean isValidAudioFile(String filePath) {
        if (filePath == null) return false;
        
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            return duration != null && Long.parseLong(duration) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if file is valid audio", e);
            return false;
        }
    }
    
    /**
     * Get audio bitrate
     */
    public static String getAudioBitrate(String audioPath) {
        if (audioPath == null) return "Unknown";
        
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(audioPath);
            String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            
            if (bitrate != null) {
                long bitrateLong = Long.parseLong(bitrate);
                if (bitrateLong > 1000) {
                    return String.format(Locale.getDefault(), "%.0f Kbps", bitrateLong / 1000.0);
                } else {
                    return bitrateLong + " bps";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting audio bitrate", e);
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
     * Get audio sample rate
     */
    public static String getAudioSampleRate(String audioPath) {
        if (audioPath == null) return "Unknown";
        
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(audioPath);
            String sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE);
            
            if (sampleRate != null) {
                long sampleRateLong = Long.parseLong(sampleRate);
                if (sampleRateLong > 1000) {
                    return String.format(Locale.getDefault(), "%.1f kHz", sampleRateLong / 1000.0);
                } else {
                    return sampleRateLong + " Hz";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting audio sample rate", e);
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