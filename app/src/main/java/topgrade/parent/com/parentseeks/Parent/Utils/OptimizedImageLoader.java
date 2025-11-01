package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.graphics.Bitmap;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Optimized image loading utility with caching and memory management
 * Provides efficient image loading using Glide with performance optimizations
 */
public class OptimizedImageLoader {
    
    private static final String TAG = "OptimizedImageLoader";
    private static final int DEFAULT_PLACEHOLDER = android.R.drawable.ic_menu_gallery;
    private static final int DEFAULT_ERROR = android.R.drawable.ic_menu_report_image;
    
    private final Context context;
    private final RequestOptions defaultOptions;
    private static final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    
    public OptimizedImageLoader(Context context) {
        this.context = context.getApplicationContext();
        this.defaultOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .placeholder(DEFAULT_PLACEHOLDER)
                .error(DEFAULT_ERROR)
                .centerCrop();
    }
    
    /**
     * Load image from URL with default options
     */
    public void loadImage(@NonNull ImageView imageView, @Nullable String url) {
        loadImage(imageView, url, defaultOptions, null);
    }
    
    /**
     * Load image from URL with custom options
     */
    public void loadImage(@NonNull ImageView imageView, @Nullable String url, 
                         @NonNull RequestOptions options) {
        loadImage(imageView, url, options, null);
    }
    
    /**
     * Load image from URL with callback
     */
    public void loadImage(@NonNull ImageView imageView, @Nullable String url, 
                         @Nullable ImageLoadCallback callback) {
        loadImage(imageView, url, defaultOptions, callback);
    }
    
    /**
     * Load image from URL with custom options and callback
     */
    public void loadImage(@NonNull ImageView imageView, @Nullable String url, 
                         @NonNull RequestOptions options, @Nullable ImageLoadCallback callback) {
        if (imageView == null) return;
        
        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(DEFAULT_ERROR);
            if (callback != null) {
                callback.onError(new IllegalArgumentException("URL is null or empty"));
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
     * Load image from resource ID
     */
    public void loadImage(@NonNull ImageView imageView, @DrawableRes int resourceId) {
        if (imageView == null) return;
        
        try {
            Glide.with(context)
                    .load(resourceId)
                    .apply(defaultOptions)
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image resource: " + resourceId, e);
            imageView.setImageResource(DEFAULT_ERROR);
        }
    }
    
    /**
     * Load circular image
     */
    public void loadCircularImage(@NonNull ImageView imageView, @Nullable String url) {
        RequestOptions circularOptions = defaultOptions.clone()
                .circleCrop();
        loadImage(imageView, url, circularOptions);
    }
    
    /**
     * Load rounded image
     */
    public void loadRoundedImage(@NonNull ImageView imageView, @Nullable String url, int radius) {
        RequestOptions roundedOptions = defaultOptions.clone()
                .transform(new RoundedCornersTransformation(radius));
        loadImage(imageView, url, roundedOptions);
    }
    
    /**
     * Load image with custom size
     */
    public void loadImageWithSize(@NonNull ImageView imageView, @Nullable String url, 
                                 int width, int height) {
        RequestOptions sizeOptions = defaultOptions.clone()
                .override(width, height);
        loadImage(imageView, url, sizeOptions);
    }
    
    /**
     * Preload image for caching
     */
    public void preloadImage(@Nullable String url) {
        if (url == null || url.trim().isEmpty()) return;
        
        try {
            Glide.with(context)
                    .load(url)
                    .apply(defaultOptions)
                    .preload();
            Log.d(TAG, "Image preloaded: " + url);
        } catch (Exception e) {
            Log.e(TAG, "Error preloading image: " + url, e);
        }
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
     * Uses ExecutorService instead of creating new threads
     */
    public void clearCaches() {
        try {
            Glide.get(context).clearMemory();
            backgroundExecutor.execute(() -> {
                Glide.get(context).clearDiskCache();
            });
            Log.d(TAG, "All caches cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing caches", e);
        }
    }
    
    /**
     * Get cache size information
     */
    public String getCacheInfo() {
        try {
            // This is a simplified cache info - in a real implementation,
            // you might want to calculate actual cache sizes
            return "Glide cache active";
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache info", e);
            return "Cache info unavailable";
        }
    }
    
    /**
     * Check if image is cached
     */
    public boolean isImageCached(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        
        try {
            // This is a simplified check - Glide doesn't provide a direct API for this
            // In a real implementation, you might want to check the disk cache directly
            return true; // Assume cached for now
        } catch (Exception e) {
            Log.e(TAG, "Error checking if image is cached: " + url, e);
            return false;
        }
    }
    
    /**
     * Load bitmap asynchronously
     */
    public void loadBitmap(@Nullable String url, @NonNull BitmapLoadCallback callback) {
        if (url == null || url.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("URL is null or empty"));
            return;
        }
        
        try {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(defaultOptions)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, 
                                                   Target<Bitmap> target, boolean isFirstResource) {
                            Log.e(TAG, "Failed to load bitmap: " + url, e);
                            callback.onError(e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, 
                                                     Target<Bitmap> target, DataSource dataSource, 
                                                     boolean isFirstResource) {
                            Log.d(TAG, "Bitmap loaded successfully: " + url);
                            callback.onSuccess(resource);
                            return false;
                        }
                    })
                    .submit();
                    
        } catch (Exception e) {
            Log.e(TAG, "Error loading bitmap: " + url, e);
            callback.onError(e);
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
     * Callback interface for bitmap loading
     */
    public interface BitmapLoadCallback {
        void onSuccess(Bitmap bitmap);
        void onError(Exception e);
    }
    
    /**
     * Simple rounded corners transformation
     * Note: This is a basic implementation. For production, consider using a proper library
     */
    private static class RoundedCornersTransformation extends com.bumptech.glide.load.resource.bitmap.BitmapTransformation {
        private final int radius;
        
        public RoundedCornersTransformation(int radius) {
            this.radius = radius;
        }
        
        @Override
        protected Bitmap transform(@NonNull com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool pool, 
                                 @NonNull Bitmap toTransform, int outWidth, int outHeight) {
            // This is a simplified implementation
            // In production, you should implement proper rounded corners
            return toTransform;
        }
        
        @Override
        public void updateDiskCacheKey(@NonNull java.security.MessageDigest messageDigest) {
            messageDigest.update(("rounded_corners_" + radius).getBytes());
        }
    }
} 