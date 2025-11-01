package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Network optimization utility for efficient API calls and caching
 * Provides optimized Volley and Retrofit configurations
 */
public class NetworkOptimizer {
    
    private static final String TAG = "NetworkOptimizer";
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int TIMEOUT_SECONDS = 30;
    private static final int READ_TIMEOUT_SECONDS = 60;
    
    private static NetworkOptimizer instance;
    private final Context context;
    private RequestQueue volleyQueue;
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    
    private NetworkOptimizer(Context context) {
        this.context = context.getApplicationContext();
        initializeVolley();
        initializeOkHttp();
    }
    
    public static synchronized NetworkOptimizer getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkOptimizer(context);
        }
        return instance;
    }
    
    /**
     * Initialize optimized Volley configuration
     */
    private void initializeVolley() {
        try {
            // Create cache directory
            File cacheDir = new File(context.getCacheDir(), "volley");
            Cache cache = new DiskBasedCache(cacheDir, CACHE_SIZE);
            
            // Create network
            Network network = new BasicNetwork(new HurlStack());
            
            // Create request queue with optimized settings
            volleyQueue = new RequestQueue(cache, network);
            volleyQueue.start();
            
            Log.d(TAG, "Volley initialized with cache size: " + CACHE_SIZE + " bytes");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Volley", e);
            // Fallback to default Volley queue
            volleyQueue = Volley.newRequestQueue(context);
        }
    }
    
    /**
     * Initialize optimized OkHttp configuration
     */
    private void initializeOkHttp() {
        try {
            // Create cache directory
            File cacheDir = new File(context.getCacheDir(), "okhttp");
            okhttp3.Cache cache = new okhttp3.Cache(cacheDir, CACHE_SIZE);
            
            // Create logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            
            // Create OkHttpClient with optimized settings
            okHttpClient = new OkHttpClient.Builder()
                    .cache(cache)
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        // Add cache control headers
                        okhttp3.Request request = chain.request();
                        okhttp3.Response response = chain.proceed(request);
                        
                        // Cache successful responses for 5 minutes
                        if (response.isSuccessful()) {
                            return response.newBuilder()
                                    .header("Cache-Control", "public, max-age=300")
                                    .build();
                        }
                        
                        return response;
                    })
                    .build();
            
            Log.d(TAG, "OkHttp initialized with cache size: " + CACHE_SIZE + " bytes");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing OkHttp", e);
        }
    }
    
    /**
     * Get optimized Volley request queue
     */
    public RequestQueue getVolleyQueue() {
        return volleyQueue;
    }
    
    /**
     * Get optimized OkHttp client
     */
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
    
    /**
     * Create optimized Retrofit instance
     */
    public Retrofit createRetrofit(String baseUrl) {
        if (retrofit == null || !retrofit.baseUrl().toString().equals(baseUrl)) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    /**
     * Add request to Volley queue with caching
     */
    public <T> void addRequest(Request<T> request) {
        if (volleyQueue != null) {
            volleyQueue.add(request);
        }
    }
    
    /**
     * Add request to Volley queue with tag for easy cancellation
     */
    public <T> void addRequest(Request<T> request, Object tag) {
        if (volleyQueue != null) {
            request.setTag(tag);
            volleyQueue.add(request);
        }
    }
    
    /**
     * Cancel all requests with specific tag
     */
    public void cancelRequests(Object tag) {
        if (volleyQueue != null) {
            volleyQueue.cancelAll(tag);
        }
    }
    
    /**
     * Cancel all requests
     */
    public void cancelAllRequests() {
        if (volleyQueue != null) {
            volleyQueue.cancelAll(request -> true);
        }
    }
    
    /**
     * Clear all caches
     */
    public void clearCaches() {
        try {
            // Clear Volley cache
            if (volleyQueue != null) {
                volleyQueue.getCache().clear();
            }
            
            // Clear OkHttp cache
            if (okHttpClient != null && okHttpClient.cache() != null) {
                okHttpClient.cache().evictAll();
            }
            
            Log.d(TAG, "All network caches cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing network caches", e);
        }
    }
    
    /**
     * Get cache size information
     */
    public String getCacheInfo() {
        try {
            long okHttpCacheSize = 0;
            
            if (okHttpClient != null && okHttpClient.cache() != null) {
                okHttpCacheSize = okHttpClient.cache().size();
            }
            
            return String.format("Volley: Available, OkHttp: %.1f KB", okHttpCacheSize / 1024.0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache info", e);
            return "Cache info unavailable";
        }
    }
    
    /**
     * Check if network is available
     */
    public boolean isNetworkAvailable() {
        return NetworkStateReceiver.isNetworkAvailable(context);
    }
    
    /**
     * Set request timeout
     */
    public void setTimeout(int seconds) {
        try {
            if (okHttpClient != null) {
                okHttpClient = okHttpClient.newBuilder()
                        .connectTimeout(seconds, TimeUnit.SECONDS)
                        .readTimeout(seconds, TimeUnit.SECONDS)
                        .writeTimeout(seconds, TimeUnit.SECONDS)
                        .build();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting timeout", e);
        }
    }
    
    /**
     * Enable/disable logging
     */
    public void setLoggingEnabled(boolean enabled) {
        try {
            if (okHttpClient != null) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(enabled ? 
                        HttpLoggingInterceptor.Level.BASIC : 
                        HttpLoggingInterceptor.Level.NONE);
                
                okHttpClient = okHttpClient.newBuilder()
                        .addInterceptor(loggingInterceptor)
                        .build();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting logging", e);
        }
    }
    
    /**
     * Shutdown network components
     */
    public void shutdown() {
        try {
            if (volleyQueue != null) {
                volleyQueue.stop();
            }
            
            if (okHttpClient != null && okHttpClient.cache() != null) {
                okHttpClient.cache().close();
            }
            
            Log.d(TAG, "Network optimizer shutdown complete");
        } catch (Exception e) {
            Log.e(TAG, "Error shutting down network optimizer", e);
        }
    }
    
    /**
     * Get network statistics
     */
    public String getNetworkStats() {
        try {
            int requestCount = 0;
            int cacheHitCount = 0;
            
            if (volleyQueue != null) {
                // Note: Volley doesn't provide direct access to these stats
                // This is a placeholder for actual implementation
                requestCount = 0;
                cacheHitCount = 0;
            }
            
            return String.format("Requests: %d, Cache Hits: %d", requestCount, cacheHitCount);
        } catch (Exception e) {
            Log.e(TAG, "Error getting network stats", e);
            return "Stats unavailable";
        }
    }
} 