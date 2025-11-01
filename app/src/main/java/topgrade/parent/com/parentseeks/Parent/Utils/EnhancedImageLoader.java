package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced image loader with comprehensive error handling
 * Handles 404 errors, network issues, and provides fallback mechanisms
 */
public class EnhancedImageLoader {
    
    private static final String TAG = "EnhancedImageLoader";
    private static final int DEFAULT_PLACEHOLDER = android.R.drawable.ic_menu_gallery;
    private static final int DEFAULT_ERROR = android.R.drawable.ic_menu_report_image;
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final int CONNECT_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 15000; // 15 seconds
    
    private final Context context;
    private final RequestOptions defaultOptions;
    private final ExecutorService executorService;
    
    public EnhancedImageLoader(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newFixedThreadPool(3);
        this.defaultOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .placeholder(DEFAULT_PLACEHOLDER)
                .error(DEFAULT_ERROR)
                .centerCrop()
                .timeout(30000); // 30 second timeout
    }
    
    /**
     * Load image with comprehensive error handling
     */
    public void loadImage(@NonNull ImageView imageView, @Nullable String url) {
        loadImage(imageView, url, defaultOptions, null, 0);
    }
    
    /**
     * Load image with custom options
     */
    public void loadImage(@NonNull ImageView imageView, @Nullable String url, 
                         @NonNull RequestOptions options) {
        loadImage(imageView, url, options, null, 0);
    }
    
    /**
     * Load image with callback
     */
    public void loadImage(@NonNull ImageView imageView, @Nullable String url, 
                         @Nullable ImageLoadCallback callback) {
        loadImage(imageView, url, defaultOptions, callback, 0);
    }
    
    /**
     * Load image with custom options and callback
     */
    public void loadImage(@NonNull ImageView imageView, @Nullable String url, 
                         @NonNull RequestOptions options, @Nullable ImageLoadCallback callback) {
        loadImage(imageView, url, options, callback, 0);
    }
    
    /**
     * Load image with retry mechanism
     */
    private void loadImage(@NonNull ImageView imageView, @Nullable String url, 
                          @NonNull RequestOptions options, @Nullable ImageLoadCallback callback, 
                          int retryCount) {
        if (imageView == null) return;
        
        if (url == null || url.trim().isEmpty()) {
            Log.w(TAG, "URL is null or empty");
            imageView.setImageResource(DEFAULT_ERROR);
            if (callback != null) {
                callback.onError(new IllegalArgumentException("URL is null or empty"));
            }
            return;
        }
        
        // Validate URL format
        if (!isValidUrl(url)) {
            Log.w(TAG, "Invalid URL format: " + url);
            imageView.setImageResource(DEFAULT_ERROR);
            if (callback != null) {
                callback.onError(new IllegalArgumentException("Invalid URL format: " + url));
            }
            return;
        }
        
        try {
            Glide.with(context)
                    .load(url)
                    .apply(options)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, 
                                                   Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Failed to load image: " + url, e);
                            
                            // Log all root causes for debugging
                            if (e != null) {
                                Log.d(TAG, "GlideException root causes:");
                                for (Throwable cause : e.getRootCauses()) {
                                    Log.d(TAG, "  - " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                                }
                            }
                            
                            // Check if it's a 404 error
                            if (is404Error(e)) {
                                Log.w(TAG, "404 error detected for URL: " + url);
                                handle404Error(imageView, url, callback);
                                return false;
                            }
                            
                            // Check if it's an HTTP error (including 404)
                            if (isHttpError(e)) {
                                Log.w(TAG, "HTTP error detected for URL: " + url);
                                // Try fallback URL for HTTP errors
                                String fallbackUrl = getFallbackUrl(url);
                                if (fallbackUrl != null && !fallbackUrl.equals(url) && retryCount == 0) {
                                    Log.d(TAG, "Trying fallback URL for HTTP error: " + fallbackUrl);
                                    imageView.postDelayed(() -> 
                                        loadImage(imageView, fallbackUrl, options, callback, retryCount + 1), 
                                        500);
                                    return true; // Don't show error image yet
                                }
                            }
                            
                            // Check if it's a network error
                            if (isNetworkError(e)) {
                                Log.w(TAG, "Network error detected for URL: " + url);
                                if (retryCount < MAX_RETRY_ATTEMPTS) {
                                    Log.d(TAG, "Retrying image load, attempt: " + (retryCount + 1));
                                    // Retry after a short delay
                                    imageView.postDelayed(() -> 
                                        loadImage(imageView, url, options, callback, retryCount + 1), 
                                        1000 * (retryCount + 1));
                                    return true; // Don't show error image yet
                                }
                            }
                            
                            // For any other error, try fallback URL first
                            String fallbackUrl = getFallbackUrl(url);
                            if (fallbackUrl != null && !fallbackUrl.equals(url) && retryCount == 0) {
                                Log.d(TAG, "Trying fallback URL due to error: " + fallbackUrl);
                                imageView.postDelayed(() -> 
                                    loadImage(imageView, fallbackUrl, options, callback, retryCount + 1), 
                                    500);
                                return true; // Don't show error image yet
                            }
                            
                            // Show error image and notify callback
                            imageView.setImageResource(DEFAULT_ERROR);
                            if (callback != null) {
                                callback.onError(e);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, 
                                                     Target<Drawable> target, DataSource dataSource, 
                                                     boolean isFirstResource) {
                            Log.d(TAG, "Image loaded successfully: " + url);
                            if (callback != null) {
                                callback.onSuccess(resource);
                            }
                            return false;
                        }
                    })
                    .into(imageView);
                    
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + url, e);
            imageView.setImageResource(DEFAULT_ERROR);
            if (callback != null) {
                callback.onError(e);
            }
        }
    }
    
    /**
     * Check if the error is a 404 error
     */
    private boolean is404Error(@Nullable GlideException e) {
        if (e == null) return false;
        
        // Log the full exception for debugging
        Log.d(TAG, "Checking for 404 error in: " + e.getMessage());
        
        // Get the root cause from GlideException
        Throwable cause = e;
        while (cause != null) {
            Log.d(TAG, "Checking cause: " + cause.getClass().getSimpleName() + " - " + cause.getMessage());
            
            if (cause instanceof IOException) {
                String message = cause.getMessage();
                if (message != null && (message.contains("404") || message.contains("Not Found"))) {
                    Log.d(TAG, "404 error detected: " + message);
                    return true;
                }
            }
            
            // Also check for HttpException which Glide might throw
            if (cause.getClass().getSimpleName().contains("HttpException")) {
                String message = cause.getMessage();
                if (message != null && message.contains("404")) {
                    Log.d(TAG, "HTTP 404 error detected: " + message);
                    return true;
                }
            }
            
            cause = cause.getCause();
        }
        return false;
    }
    
    /**
     * Check if the error is a network error
     */
    private boolean isNetworkError(@Nullable GlideException e) {
        if (e == null) return false;
        
        // Get the root cause from GlideException
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof IOException) {
                String message = cause.getMessage();
                if (message != null && (message.contains("timeout") || 
                                       message.contains("connection") || 
                                       message.contains("network"))) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
    
    /**
     * Check if the error is an HTTP error (including 404)
     */
    private boolean isHttpError(@Nullable GlideException e) {
        if (e == null) return false;
        
        // Get the root cause from GlideException
        Throwable cause = e;
        while (cause != null) {
            String className = cause.getClass().getSimpleName();
            String message = cause.getMessage();
            
            // Check for HttpException
            if (className.contains("HttpException")) {
                Log.d(TAG, "HTTP Exception detected: " + message);
                return true;
            }
            
            // Check for any HTTP-related error
            if (message != null && (message.contains("status code") || 
                                   message.contains("HTTP") || 
                                   message.contains("Failed to connect"))) {
                Log.d(TAG, "HTTP-related error detected: " + message);
                return true;
            }
            
            cause = cause.getCause();
        }
        return false;
    }
    
    /**
     * Handle 404 errors with fallback logic
     */
    private void handle404Error(@NonNull ImageView imageView, @NonNull String url, 
                               @Nullable ImageLoadCallback callback) {
        // Try to find a fallback image
        String fallbackUrl = getFallbackUrl(url);
        if (fallbackUrl != null && !fallbackUrl.equals(url)) {
            Log.d(TAG, "Trying fallback URL: " + fallbackUrl);
            loadImage(imageView, fallbackUrl, defaultOptions, callback, 0);
        } else {
            // No fallback available, show default error image
            imageView.setImageResource(DEFAULT_ERROR);
            if (callback != null) {
                callback.onError(new IOException("Image not found (404): " + url));
            }
        }
    }
    
    /**
     * Generate fallback URL for 404 errors
     */
    private String getFallbackUrl(@NonNull String originalUrl) {
        try {
            Log.d(TAG, "Generating fallback URL for: " + originalUrl);
            
            // If it's a parent image, try employee image as fallback
            if (originalUrl.contains("/uploads/parent/")) {
                String fallback = originalUrl.replace("/uploads/parent/", "/uploads/employee/");
                Log.d(TAG, "Parent -> Employee fallback: " + fallback);
                return fallback;
            }
            // If it's an employee image, try staff image as fallback
            else if (originalUrl.contains("/uploads/employee/")) {
                String fallback = originalUrl.replace("/uploads/employee/", "/uploads/staff/");
                Log.d(TAG, "Employee -> Staff fallback: " + fallback);
                return fallback;
            }
            // If it's a staff image, try parent image as fallback
            else if (originalUrl.contains("/uploads/staff/")) {
                String fallback = originalUrl.replace("/uploads/staff/", "/uploads/parent/");
                Log.d(TAG, "Staff -> Parent fallback: " + fallback);
                return fallback;
            }
            // If it's a student image, try parent image as fallback
            else if (originalUrl.contains("/uploads/student/")) {
                String fallback = originalUrl.replace("/uploads/student/", "/uploads/parent/");
                Log.d(TAG, "Student -> Parent fallback: " + fallback);
                return fallback;
            }
            // If it's a general image, try different paths
            else if (originalUrl.contains("/uploads/")) {
                // Try to find any available image path
                String[] possiblePaths = {"/uploads/parent/", "/uploads/employee/", "/uploads/staff/", "/uploads/student/"};
                for (String path : possiblePaths) {
                    if (!originalUrl.contains(path)) {
                        String fallback = originalUrl.replace("/uploads/", path);
                        Log.d(TAG, "General fallback: " + fallback);
                        return fallback;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating fallback URL", e);
        }
        
        Log.d(TAG, "No fallback URL available for: " + originalUrl);
        return null;
    }
    
    /**
     * Validate URL format
     */
    private boolean isValidUrl(@NonNull String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Pre-validate image URL availability
     */
    public void validateImageUrl(@NonNull String url, @NonNull UrlValidationCallback callback) {
        executorService.execute(() -> {
            try {
                URL imageUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setRequestMethod("HEAD");
                
                int responseCode = connection.getResponseCode();
                connection.disconnect();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    callback.onValid(url);
                } else {
                    callback.onInvalid(url, "HTTP " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error validating URL: " + url, e);
                callback.onInvalid(url, e.getMessage());
            }
        });
    }
    
    /**
     * Load image with custom error drawable
     */
    public void loadImageWithCustomError(@NonNull ImageView imageView, @Nullable String url, 
                                        @DrawableRes int errorDrawable) {
        RequestOptions customOptions = defaultOptions.clone().error(errorDrawable);
        loadImage(imageView, url, customOptions);
    }
    
    /**
     * Load image with immediate fallback to default image on any error
     */
    public void loadImageWithImmediateFallback(@NonNull ImageView imageView, @Nullable String url, 
                                              @DrawableRes int fallbackDrawable) {
        if (imageView == null) return;
        
        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(fallbackDrawable);
            return;
        }
        
        try {
            Glide.with(context)
                    .load(url)
                    .apply(defaultOptions.error(fallbackDrawable))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, 
                                                   Target<Drawable> target, boolean isFirstResource) {
                            Log.w(TAG, "Image load failed, using immediate fallback: " + url);
                            imageView.setImageResource(fallbackDrawable);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, 
                                                     Target<Drawable> target, DataSource dataSource, 
                                                     boolean isFirstResource) {
                            Log.d(TAG, "Image loaded successfully: " + url);
                            return false;
                        }
                    })
                    .into(imageView);
                    
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + url, e);
            imageView.setImageResource(fallbackDrawable);
        }
    }
    
    /**
     * Load circular image with error handling
     */
    public void loadCircularImage(@NonNull ImageView imageView, @Nullable String url) {
        RequestOptions circularOptions = defaultOptions.clone().circleCrop();
        loadImage(imageView, url, circularOptions);
    }
    
    /**
     * Clear image from ImageView
     */
    public void clearImage(@NonNull ImageView imageView) {
        if (imageView == null) return;
        
        try {
            Glide.with(context).clear(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing image", e);
        }
    }
    
    /**
     * Clear all caches
     */
    public void clearCaches() {
        try {
            Glide.get(context).clearMemory();
            new Thread(() -> {
                Glide.get(context).clearDiskCache();
            }).start();
            Log.d(TAG, "All caches cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing caches", e);
        }
    }
    
    /**
     * Shutdown executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * Callback interface for image loading
     */
    public interface ImageLoadCallback {
        void onSuccess(Drawable drawable);
        void onError(Exception e);
    }
    
    /**
     * Callback interface for URL validation
     */
    public interface UrlValidationCallback {
        void onValid(String url);
        void onInvalid(String url, String reason);
    }
} 