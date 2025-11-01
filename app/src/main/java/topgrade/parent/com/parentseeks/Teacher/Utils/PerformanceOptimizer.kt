package topgrade.parent.com.parentseeks.Teacher.Utils

import android.app.Activity
import android.content.Context
import android.util.Log
import topgrade.parent.com.parentseeks.Parent.Utils.ConsolidatedPerformanceOptimizer

/**
 * Teacher Performance Optimizer - Wrapper for Consolidated Performance Optimizer
 * 
 * This class provides backward compatibility while using the consolidated
 * performance optimization functionality.
 * 
 * @deprecated Use ConsolidatedPerformanceOptimizer directly for new code
 */
@Deprecated("Use ConsolidatedPerformanceOptimizer instead", ReplaceWith("ConsolidatedPerformanceOptimizer"))
class PerformanceOptimizer(private val context: Context) {
    
    companion object {
        private const val TAG = "TeacherPerformanceOptimizer"
    }
    
    /**
     * Optimize activity performance using consolidated optimizer
     */
    fun optimizeActivity(activity: Activity) {
        try {
            ConsolidatedPerformanceOptimizer.optimizeActivity(activity)
            Log.d(TAG, "Activity optimization delegated to ConsolidatedPerformanceOptimizer")
        } catch (e: Exception) {
            Log.e(TAG, "Error optimizing activity", e)
        }
    }
    
    /**
     * Optimize RecyclerView using consolidated optimizer
     */
    fun optimizeRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        try {
            ConsolidatedPerformanceOptimizer.optimizeRecyclerView(recyclerView)
            Log.d(TAG, "RecyclerView optimization delegated to ConsolidatedPerformanceOptimizer")
        } catch (e: Exception) {
            Log.e(TAG, "Error optimizing RecyclerView", e)
        }
    }
    
    /**
     * Optimize image loading using consolidated optimizer
     */
    fun optimizeImageLoading() {
        try {
            ConsolidatedPerformanceOptimizer.optimizeImageLoading(context)
            Log.d(TAG, "Image loading optimization delegated to ConsolidatedPerformanceOptimizer")
        } catch (e: Exception) {
            Log.e(TAG, "Error optimizing image loading", e)
        }
    }
    
    /**
     * Cleanup resources using consolidated optimizer
     */
    fun cleanup() {
        try {
            ConsolidatedPerformanceOptimizer.clearCaches(context)
            Log.d(TAG, "Cleanup delegated to ConsolidatedPerformanceOptimizer")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}
