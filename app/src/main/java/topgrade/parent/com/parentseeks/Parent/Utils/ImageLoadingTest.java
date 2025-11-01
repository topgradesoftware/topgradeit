package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Test utility for image loading functionality
 * Helps debug image loading issues and verify fallback mechanisms
 */
public class ImageLoadingTest {
    
    private static final String TAG = "ImageLoadingTest";
    
    /**
     * Test image loading with detailed logging
     */
    public static void testImageLoading(@NonNull Context context, @NonNull ImageView imageView, 
                                       @NonNull String url, @NonNull String testName) {
        Log.d(TAG, "=== Starting image loading test: " + testName + " ===");
        Log.d(TAG, "URL: " + url);
        Log.d(TAG, "ImageView: " + imageView.getClass().getSimpleName());
        
        try {
            Glide.with(context)
                    .load(url)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, 
                                                   Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "=== Test FAILED: " + testName + " ===");
                            Log.e(TAG, "Error: " + e.getMessage());
                            
                            // Log all root causes
                            if (e != null) {
                                Log.d(TAG, "Root causes:");
                                for (Throwable cause : e.getRootCauses()) {
                                    Log.d(TAG, "  - " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                                }
                            }
                            
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, 
                                                     Target<Drawable> target, 
                                                     com.bumptech.glide.load.DataSource dataSource, 
                                                     boolean isFirstResource) {
                            Log.d(TAG, "=== Test PASSED: " + testName + " ===");
                            Log.d(TAG, "Image loaded successfully");
                            Log.d(TAG, "DataSource: " + dataSource.name());
                            return false;
                        }
                    })
                    .into(imageView);
                    
        } catch (Exception e) {
            Log.e(TAG, "=== Test ERROR: " + testName + " ===");
            Log.e(TAG, "Exception: " + e.getMessage(), e);
        }
    }
    
    /**
     * Test enhanced image loader
     */
    public static void testEnhancedImageLoader(@NonNull Context context, @NonNull ImageView imageView, 
                                              @NonNull String url, @NonNull String testName) {
        Log.d(TAG, "=== Starting EnhancedImageLoader test: " + testName + " ===");
        Log.d(TAG, "URL: " + url);
        
        EnhancedImageLoader imageLoader = new EnhancedImageLoader(context);
        imageLoader.loadImage(imageView, url, new EnhancedImageLoader.ImageLoadCallback() {
            @Override
            public void onSuccess(Drawable drawable) {
                Log.d(TAG, "=== EnhancedImageLoader test PASSED: " + testName + " ===");
            }
            
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "=== EnhancedImageLoader test FAILED: " + testName + " ===");
                Log.e(TAG, "Error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Test fallback URL generation
     */
    public static void testFallbackUrls() {
        Log.d(TAG, "=== Testing fallback URL generation ===");
        
        String[] testUrls = {
            "https://topgradesoftware.com/uploads/parent/685c1a88f2cb3_ic_student_login.png",
            "https://topgradesoftware.com/uploads/employee/test.jpg",
            "https://topgradesoftware.com/uploads/staff/avatar.png",
            "https://topgradesoftware.com/uploads/student/photo.jpg"
        };
        
        EnhancedImageLoader imageLoader = new EnhancedImageLoader(null);
        
        for (String url : testUrls) {
            try {
                // Use reflection to access private method for testing
                java.lang.reflect.Method method = EnhancedImageLoader.class.getDeclaredMethod("getFallbackUrl", String.class);
                method.setAccessible(true);
                String fallback = (String) method.invoke(imageLoader, url);
                
                Log.d(TAG, "Original: " + url);
                Log.d(TAG, "Fallback: " + (fallback != null ? fallback : "null"));
                Log.d(TAG, "---");
            } catch (Exception e) {
                Log.e(TAG, "Error testing fallback for: " + url, e);
            }
        }
    }
} 