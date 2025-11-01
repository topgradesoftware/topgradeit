package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log

/**
 * Parent Performance Optimizer - Wrapper for Consolidated Performance Optimizer
 * 
 * This class provides backward compatibility while using the consolidated
 * performance optimization functionality.
 * 
 * @deprecated Use ConsolidatedPerformanceOptimizer directly for new code
 */
@Deprecated("Use ConsolidatedPerformanceOptimizer instead", ReplaceWith("ConsolidatedPerformanceOptimizer"))
class PerformanceOptimizer {

    companion object {
        private const val TAG = "PerformanceOptimizer"

        // 🔹 Main entry point
        @JvmStatic
        fun initialize(context: Context) {
            try {
                ConsolidatedPerformanceOptimizer.initialize(context)
                Log.d(TAG, "PerformanceOptimizer initialized via ConsolidatedPerformanceOptimizer")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing performance optimizations", e)
            }
        }

        @JvmStatic
        fun cleanup() {
            try {
                ConsolidatedPerformanceOptimizer.cleanup()
                Log.d(TAG, "PerformanceOptimizer cleaned up via ConsolidatedPerformanceOptimizer")
            } catch (e: Exception) {
                Log.e(TAG, "Error during cleanup", e)
            }
        }

        // 🔹 StrictMode
        @JvmStatic 
        fun enableStrictModeForDebugging() {
            try {
                ConsolidatedPerformanceOptimizer.enableStrictModeForDebugging()
                Log.d(TAG, "StrictMode enabled for debugging via ConsolidatedPerformanceOptimizer")
            } catch (e: Exception) {
                Log.e(TAG, "Error enabling StrictMode", e)
            }
        }
        
        @JvmStatic 
        fun disableStrictMode() {
            try {
                ConsolidatedPerformanceOptimizer.disableStrictMode()
                Log.d(TAG, "StrictMode disabled via ConsolidatedPerformanceOptimizer")
            } catch (e: Exception) {
                Log.e(TAG, "Error disabling StrictMode", e)
            }
        }

        // 🔹 RecyclerView optimization
        @JvmStatic
        fun optimizeRecyclerView(rv: androidx.recyclerview.widget.RecyclerView) {
            try {
                ConsolidatedPerformanceOptimizer.optimizeRecyclerView(rv)
                Log.d(TAG, "RecyclerView optimized via ConsolidatedPerformanceOptimizer")
            } catch (e: Exception) {
                Log.e(TAG, "Error optimizing RecyclerView", e)
            }
        }

        // 🔹 Memory Monitoring
        @JvmStatic
        fun monitorMemoryUsage(): Int {
            return try {
                ConsolidatedPerformanceOptimizer.monitorMemoryUsage()
            } catch (e: Exception) {
                Log.e(TAG, "Error monitoring memory usage", e)
                0
            }
        }

        @JvmStatic
        fun getMemoryUsageString(): String {
            return try {
                ConsolidatedPerformanceOptimizer.getMemoryUsageString()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting memory usage string", e)
                "Unknown"
            }
        }

        @JvmStatic
        fun isMemoryUsageHigh(threshold: Int = 80): Boolean {
            return try {
                ConsolidatedPerformanceOptimizer.isMemoryUsageHigh(threshold)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking memory usage", e)
                false
            }
        }

        @JvmStatic
        fun getMemoryInfoForUI(): Map<String, String> {
            return try {
                ConsolidatedPerformanceOptimizer.getMemoryInfoForUI()
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

        // 🔹 Image Loading Optimization
        @JvmStatic
        fun optimizeImageLoading(context: Context) {
            try {
                ConsolidatedPerformanceOptimizer.optimizeImageLoading(context)
                Log.d(TAG, "Image loading optimized via ConsolidatedPerformanceOptimizer")
            } catch (e: Exception) {
                Log.e(TAG, "Error optimizing image loading", e)
            }
        }

        // 🔹 Cache Management
        @JvmStatic
        fun clearCaches(context: Context) {
            try {
                ConsolidatedPerformanceOptimizer.clearCaches(context)
                Log.d(TAG, "Caches cleared via ConsolidatedPerformanceOptimizer")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing caches", e)
            }
        }

        // 🔹 Display Metrics
        @JvmStatic
        fun getDisplayMetrics(context: Context) {
            try {
                ConsolidatedPerformanceOptimizer.getDisplayMetrics(context)
                Log.d(TAG, "Display metrics retrieved via ConsolidatedPerformanceOptimizer")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting display metrics", e)
            }
        }

        // 🔹 Resource Preloading
        @JvmStatic
        fun preloadFonts(context: Context) {
            try {
                ConsolidatedPerformanceOptimizer.preloadFonts(context)
                Log.d(TAG, "Fonts preloaded via ConsolidatedPerformanceOptimizer")
            } catch (e: Exception) {
                Log.e(TAG, "Error preloading fonts", e)
            }
        }

        @JvmStatic
        fun preloadDrawables(context: Context) {
            try {
                ConsolidatedPerformanceOptimizer.preloadDrawables(context)
                Log.d(TAG, "Drawables preloaded via ConsolidatedPerformanceOptimizer")
            } catch (e: Exception) {
                Log.e(TAG, "Error preloading drawables", e)
            }
        }
    }
}
