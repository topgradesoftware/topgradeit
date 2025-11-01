package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    
    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_SIZE = 1024;
    private static final int JPEG_QUALITY = 85;
    
    /**
     * Save bitmap to app's private directory
     */
    public static File saveBitmapToAppDir(Context context, Bitmap bitmap, String fileName) {
        if (context == null || bitmap == null) return null;
        
        try {
            // Use app's private external files directory
            File imagesDir = FileUtils.getAppExternalFilesDir(context, Environment.DIRECTORY_PICTURES);
            if (imagesDir == null) {
                Log.e(TAG, "Cannot access app external files directory");
                return null;
            }
            
            File imageFile = new File(imagesDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
                fos.flush();
                Log.d(TAG, "Image saved to app directory: " + imageFile.getAbsolutePath());
                return imageFile;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to app directory", e);
            return null;
        }
    }
    
    /**
     * Save bitmap to gallery using modern API
     */
    public static Uri saveBitmapToGallery(Context context, Bitmap bitmap, String fileName) {
        if (context == null || bitmap == null) return null;
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore API for Android 10+
                return saveBitmapToMediaStore(context, bitmap, fileName);
            } else {
                // Use external storage for older versions
                return saveBitmapToExternalStorage(context, bitmap, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap to gallery", e);
            return null;
        }
    }
    
    /**
     * Save bitmap using MediaStore API (Android 10+)
     */
    private static Uri saveBitmapToMediaStore(Context context, Bitmap bitmap, String fileName) {
        ContentResolver resolver = context.getContentResolver();
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ParentSeeks");
        
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream);
                    outputStream.flush();
                    Log.d(TAG, "Image saved to MediaStore: " + uri);
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
     * Save bitmap to external storage (pre-Android 10)
     */
    private static Uri saveBitmapToExternalStorage(Context context, Bitmap bitmap, String fileName) {
        // Check if external storage is available
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "External storage not available");
            return null;
        }
        
        // Create directory if it doesn't exist
        File picturesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ParentSeeks");
        if (!picturesDir.exists() && !picturesDir.mkdirs()) {
            Log.e(TAG, "Failed to create pictures directory");
            return null;
        }
        
        // Create file
        File imageFile = new File(picturesDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.flush();
            
            // Notify media scanner
            Uri uri = Uri.fromFile(imageFile);
            Log.d(TAG, "Image saved to external storage: " + uri);
            return uri;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to external storage", e);
            return null;
        }
    }
    
    /**
     * Load and resize bitmap from file
     */
    public static Bitmap loadAndResizeBitmap(String filePath, int maxSize) {
        if (filePath == null) return null;
        
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize);
            
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            
            return BitmapFactory.decodeFile(filePath, options);
        } catch (Exception e) {
            Log.e(TAG, "Error loading bitmap from file: " + filePath, e);
            return null;
        }
    }
    
    /**
     * Load and resize bitmap from input stream
     */
    public static Bitmap loadAndResizeBitmap(InputStream inputStream, int maxSize) {
        if (inputStream == null) return null;
        
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            
            // Reset stream
            inputStream.reset();
            
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize);
            
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (Exception e) {
            Log.e(TAG, "Error loading bitmap from input stream", e);
            return null;
        }
    }
    
    /**
     * Calculate inSampleSize for bitmap scaling
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
    
    /**
     * Rotate bitmap by degrees
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        if (bitmap == null) return null;
        
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            Log.e(TAG, "Error rotating bitmap", e);
            return bitmap;
        }
    }
    
    /**
     * Convert bitmap to byte array
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Error converting bitmap to byte array", e);
            return null;
        }
    }
    
    /**
     * Generate unique image filename
     */
    public static String generateImageFileName(String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return prefix + "_" + timeStamp + ".jpg";
    }
    
    /**
     * Get bitmap dimensions without loading the full image
     */
    public static int[] getBitmapDimensions(String filePath) {
        if (filePath == null) return null;
        
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            
            return new int[]{options.outWidth, options.outHeight};
        } catch (Exception e) {
            Log.e(TAG, "Error getting bitmap dimensions", e);
            return null;
        }
    }
    
    /**
     * Check if file is a valid image
     */
    public static boolean isValidImageFile(String filePath) {
        if (filePath == null) return false;
        
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
            return options.outWidth > 0 && options.outHeight > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if file is valid image", e);
            return false;
        }
    }
} 