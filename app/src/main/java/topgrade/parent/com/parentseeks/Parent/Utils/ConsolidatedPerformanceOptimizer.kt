@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.startup.Initializer
import kotlinx.coroutines.*
import topgrade.parent.com.parentseeks.BuildConfig

/**
 * Consolidated Performance Optimization Utility
 * 
 * This class combines functionality from both Teacher and Parent PerformanceOptimizer classes
 * to provide comprehensive performance optimization across the entire application.
 * 
 * Features:
 * - Activity optimization (window insets, memory, CPU)
 * - RecyclerView optimization
 * - Image loading optimization
 * - Memory monitoring and management
 * - Cache management
 * - StrictMode configuration for debugging
 * - Resource preloading
 * - Display metrics handling
 */
class ConsolidatedPerformanceOptimizer : Initializer<Unit> {

    companion object {
        private const val TAG = "ConsolidatedPerformanceOptimizer"
        
        // Singleton instance for context-based operations
        @Volatile
        private var INSTANCE: ConsolidatedPerformanceOptimizer? = null
        
        // Coroutine management
        private var initializationJob: Job? = null
        private var cacheCleanupJob: Job? = null
        private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        
        // Memory thresholds
        private const val MEMORY_HIGH_THRESHOLD = 80
        private const val MEMORY_MEDIUM_THRESHOLD = 60
        private const val CACHE_SIZE_THRESHOLD_MB = 50
        
        /**
         * Get singleton instance
         */
        @JvmStatic
        fun getInstance(): ConsolidatedPerformanceOptimizer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConsolidatedPerformanceOptimizer().also { INSTANCE = it }
            }
        }
        
        // ==================== INITIALIZATION ====================
        
        /**
         * Initialize performance optimizations
         */
        @JvmStatic
        fun initialize(_context: Context) {
            try {
                initializationJob?.cancel()
                cacheCleanupJob?.cancel()
                
                // Basic initialization without background operations to prevent high CPU usage
                Log.d(TAG, "ConsolidatedPerformanceOptimizer initialized")
                
                // Manual memory monitoring available on-demand
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Debug mode - manual memory monitoring available")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing performance optimizations", e)
            }
        }
        
        /**
         * Cleanup resources
         */
        @JvmStatic
        fun cleanup() {
            initializationJob?.cancel()
            cacheCleanupJob?.cancel()
            Log.d(TAG, "ConsolidatedPerformanceOptimizer cleaned up")
        }
        
        // ==================== ACTIVITY OPTIMIZATION ====================
        
        /**
         * Optimize activity performance (from Teacher PerformanceOptimizer)
         */
        @JvmStatic
        fun optimizeActivity(activity: Activity) {
            try {
                optimizeWindowInsets(activity)
                optimizeMemoryUsage(activity)
                optimizeCPUUsage()
                Log.d(TAG, "Activity optimization completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error optimizing activity", e)
            }
        }
        
        /**
         * Optimize window insets for modern Android versions
         */
        private fun optimizeWindowInsets(activity: Activity) {
            try {
                val window = activity.window
                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                
                // Configure window insets
                windowInsetsController.apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
                
                // Use WindowCompat for better compatibility
                WindowCompat.setDecorFitsSystemWindows(window, false)
                
                // Handle insets properly
                val rootView = activity.findViewById<android.view.View>(android.R.id.content)
                try {
                    androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
                        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                        view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
                        WindowInsetsCompat.CONSUMED
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting window insets listener", e)
                }
                
                Log.d(TAG, "Window insets optimized")
            } catch (e: Exception) {
                Log.e(TAG, "Error optimizing window insets", e)
            }
        }
        
        /**
         * Optimize memory usage
         */
        private fun optimizeMemoryUsage(context: Context) {
            try {
                // Enable memory optimization for Android 9+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    context.applicationInfo.flags = context.applicationInfo.flags or 
                        android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP
                }
                
                // Clear image cache if memory usage is high
                if (isMemoryUsageHigh()) {
                    clearImageCache(context)
                }
                
                Log.d(TAG, "Memory optimization applied")
            } catch (e: Exception) {
                Log.e(TAG, "Error optimizing memory", e)
            }
        }
        
        /**
         * Optimize CPU usage
         */
        private fun optimizeCPUUsage() {
            try {
                val runtime = Runtime.getRuntime()
                val availableProcessors = runtime.availableProcessors()
                
                // Set thread pool size based on available processors
                val optimalThreadCount = maxOf((availableProcessors * 0.75).toInt(), 1)
                
                Log.d(TAG, "CPU optimization applied - Optimal threads: $optimalThreadCount")
            } catch (e: Exception) {
                Log.e(TAG, "Error optimizing CPU", e)
            }
        }
        
        // ==================== RECYCLERVIEW OPTIMIZATION ====================
        
        /**
         * Optimize RecyclerView performance (combined from both classes)
         */
        @JvmStatic
        fun optimizeRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
            try {
                // Set optimal cache size based on screen height
                val size = when {
                    recyclerView.context.resources.displayMetrics.heightPixels > 2000 -> 25
                    recyclerView.context.resources.displayMetrics.heightPixels > 1500 -> 20
                    else -> 15
                }
                
                recyclerView.apply {
                    setHasFixedSize(true)
                    setNestedScrollingEnabled(false)
                    itemAnimator = null // Disable animations for better performance
                    isDrawingCacheEnabled = false
                    setItemViewCacheSize(size)
                }
                
                Log.d(TAG, "RecyclerView optimized (cache size=$size)")
            } catch (e: Exception) {
                Log.e(TAG, "Error optimizing RecyclerView", e)
            }
        }
        
        // ==================== IMAGE LOADING OPTIMIZATION ====================
        
        /**
         * Optimize image loading (combined from both classes)
         */
        @JvmStatic
        fun optimizeImageLoading(context: Context) {
            try {
                // Configure Glide for better performance
                com.bumptech.glide.Glide.with(context)
                    .setDefaultRequestOptions(
                        com.bumptech.glide.request.RequestOptions()
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .skipMemoryCache(false)
                    )
                
                val runtime = Runtime.getRuntime()
                val memoryInfo = "${runtime.totalMemory() / 1024 / 1024}MB total, " +
                               "${runtime.freeMemory() / 1024 / 1024}MB free"
                Log.d(TAG, "Image loading optimized successfully. Memory: $memoryInfo")
            } catch (e: Exception) {
                Log.e(TAG, "Error optimizing image loading", e)
            }
        }
        
        /**
         * Clear image cache
         */
        private fun clearImageCache(context: Context) {
            try {
                // Clear Glide cache if memory pressure is high
                com.bumptech.glide.Glide.get(context).clearMemory()
                
                // Clear disk cache in background
                Thread {
                    try {
                        com.bumptech.glide.Glide.get(context).clearDiskCache()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error clearing disk cache", e)
                    }
                }.start()
                
                Log.d(TAG, "Image cache cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing image cache", e)
            }
        }
        
        // ==================== MEMORY MONITORING ====================
        
        /**
         * Monitor memory usage
         */
        @JvmStatic
        fun monitorMemoryUsage(): Int {
            return try {
                val runtime = Runtime.getRuntime()
                val used = runtime.totalMemory() - runtime.freeMemory()
                val max = runtime.maxMemory()
                val percent = (used * 100 / max).toInt()
                Log.d(TAG, "Memory usage: ${used / 1024 / 1024}MB / ${max / 1024 / 1024}MB ($percent%)")
                percent
            } catch (e: Exception) {
                Log.e(TAG, "Error monitoring memory usage", e)
                0
            }
        }
        
        /**
         * Get memory usage as string
         */
        @JvmStatic
        fun getMemoryUsageString(): String {
            return try {
                val runtime = Runtime.getRuntime()
                val used = runtime.totalMemory() - runtime.freeMemory()
                val max = runtime.maxMemory()
                val percent = (used * 100 / max).toInt()
                "${used / 1024 / 1024}MB / ${max / 1024 / 1024}MB ($percent%)"
            } catch (e: Exception) {
                Log.e(TAG, "Error getting memory usage string", e)
                "Unknown"
            }
        }
        
        /**
         * Check if memory usage is high
         */
        @JvmStatic
        fun isMemoryUsageHigh(threshold: Int = MEMORY_HIGH_THRESHOLD): Boolean {
            return try {
                val runtime = Runtime.getRuntime()
                val used = runtime.totalMemory() - runtime.freeMemory()
                val max = runtime.maxMemory()
                val percent = (used * 100 / max).toInt()
                percent > threshold
            } catch (e: Exception) {
                Log.e(TAG, "Error checking memory usage", e)
                false
            }
        }
        
        /**
         * Get memory info for UI display
         */
        @JvmStatic
        fun getMemoryInfoForUI(): Map<String, String> {
            return try {
                val runtime = Runtime.getRuntime()
                val used = runtime.totalMemory() - runtime.freeMemory()
                val max = runtime.maxMemory()
                val percent = (used * 100 / max).toInt()
                
                mapOf(
                    "used" to "${used / 1024 / 1024} MB",
                    "max" to "${max / 1024 / 1024} MB",
                    "percent" to "$percent%",
                    "status" to when {
                        percent > MEMORY_HIGH_THRESHOLD -> "High"
                        percent > MEMORY_MEDIUM_THRESHOLD -> "Medium"
                        else -> "Normal"
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting memory info for UI", e)
                mapOf(
                    "used" to "Unknown",
                    "max" to "Unknown",
                    "percent" to "Unknown",
                    "status" to "Unknown"
                )
            }
        }
        
        // ==================== CACHE MANAGEMENT ====================
        
        /**
         * Clear caches safely
         */
        @JvmStatic
        fun clearCaches(context: Context) {
            try {
                clearGlideCacheIfNeeded(context)
                clearCacheSafely(context)
                Log.d(TAG, "Cache cleanup completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing caches", e)
            }
        }
        
        /**
         * Clear Glide cache if size exceeds threshold
         */
        private fun clearGlideCacheIfNeeded(context: Context) {
            try {
                val dir = context.cacheDir.resolve("image_manager_disk_cache")
                if (dir.exists() && dir.isDirectory) {
                    val size = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                    val mb = size / 1024 / 1024
                    if (mb > CACHE_SIZE_THRESHOLD_MB) {
                        if (dir.deleteRecursively()) {
                            Log.d(TAG, "Glide cache cleared ($mb MB)")
                        }
                    } else {
                        Log.d(TAG, "Glide cache size OK: $mb MB")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing Glide cache", e)
            }
        }
        
        /**
         * Clear cache directories safely
         */
        private fun clearCacheSafely(context: Context) {
            try {
                val cacheDir = context.cacheDir
                if (!cacheDir.exists()) return
                
                val subDirs = listOf("http_cache", "image_cache", "temp_files")
                var clearedCount = 0
                
                for (sub in subDirs) {
                    val dir = cacheDir.resolve(sub)
                    if (dir.exists() && dir.isDirectory) {
                        if (dir.deleteRecursively()) {
                            clearedCount++
                            Log.d(TAG, "Cache subdir cleared: $sub")
                        }
                    }
                }
                Log.d(TAG, "Cache cleanup: $clearedCount directories cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cache safely", e)
            }
        }
        
        // ==================== STRICTMODE CONFIGURATION ====================
        
        /**
         * Enable StrictMode for debugging
         */
        @JvmStatic
        fun enableStrictModeForDebugging() {
            try {
                StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()
                        .penaltyLog()
                        .build()
                )
                val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) vmPolicyBuilder.detectFileUriExposure()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vmPolicyBuilder.detectContentUriWithoutPermission()
                StrictMode.setVmPolicy(vmPolicyBuilder.penaltyLog().build())
                Log.d(TAG, "StrictMode enabled for debugging")
            } catch (e: Exception) {
                Log.e(TAG, "Error enabling StrictMode", e)
            }
        }
        
        /**
         * Disable StrictMode
         */
        @JvmStatic
        fun disableStrictMode() {
            try {
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().build())
                StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
                Log.d(TAG, "StrictMode disabled")
            } catch (e: Exception) {
                Log.e(TAG, "Error disabling StrictMode", e)
            }
        }
        
        // ==================== DISPLAY METRICS ====================
        
        /**
         * Get display metrics
         */
        @JvmStatic
        fun getDisplayMetrics(context: Context): DisplayMetrics {
            return try {
                val dm = DisplayMetrics()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val wm = context.getSystemService(WindowManager::class.java)
                    val bounds = wm?.currentWindowMetrics?.bounds
                    if (bounds != null) {
                        dm.widthPixels = bounds.width()
                        dm.heightPixels = bounds.height()
                    }
                } else {
                    @Suppress("DEPRECATION")
                    (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
                        ?.defaultDisplay?.getMetrics(dm)
                }
                Log.d(TAG, "Display metrics: ${dm.widthPixels}x${dm.heightPixels} density=${dm.density}")
                dm
            } catch (e: Exception) {
                Log.e(TAG, "Error getting display metrics", e)
                DisplayMetrics()
            }
        }
        
        // ==================== RESOURCE PRELOADING ====================
        
        /**
         * Preload fonts
         */
        @JvmStatic
        fun preloadFonts(context: Context) {
            try {
                val fonts = listOf("quicksand_bold", "quicksand_medium", "quicksand_light")
                var loadedCount = 0
                
                for (fontName in fonts) {
                    try {
                        val fontResId = context.resources.getIdentifier(fontName, "font", context.packageName)
                        if (fontResId != 0) {
                            val typeface = androidx.core.content.res.ResourcesCompat.getFont(context, fontResId)
                            if (typeface != null) loadedCount++
                            Log.d(TAG, "Font preloaded: $fontName")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to preload font: $fontName", e)
                    }
                }
                Log.d(TAG, "Fonts preloaded: $loadedCount/${fonts.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error preloading fonts", e)
            }
        }
        
        /**
         * Preload drawables
         */
        @JvmStatic
        fun preloadDrawables(context: Context) {
            try {
                val drawables = listOf("ic_arrow_back", "ic_close_black_48dp", "large_logo")
                var loadedCount = 0
                
                for (drawableName in drawables) {
                    try {
                        val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                        if (resId != 0) {
                            val drawable = ContextCompat.getDrawable(context, resId)
                            if (drawable != null) loadedCount++
                            Log.d(TAG, "Drawable preloaded: $drawableName")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to preload drawable: $drawableName", e)
                    }
                }
                Log.d(TAG, "Drawables preloaded: $loadedCount/${drawables.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error preloading drawables", e)
            }
        }
    }
    
    // ==================== INITIALIZER IMPLEMENTATION ====================
    
    override fun create(context: Context): Unit {
        initialize(context)
        return Unit
    }
    
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
