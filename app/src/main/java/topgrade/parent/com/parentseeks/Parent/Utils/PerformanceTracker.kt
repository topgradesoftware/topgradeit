package topgrade.parent.com.parentseeks.Parent.Utils

import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Performance Tracker for monitoring app performance
 * Tracks API calls, screen loads, and custom operations
 * 
 * Features:
 * - API response time tracking
 * - Screen load time tracking
 * - Custom operation timing
 * - Automatic reporting to Firebase Analytics
 * 
 * @author Topgradeit Team
 * @version 1.0
 * @since 2025-10-15
 */
object PerformanceTracker {
    
    private const val TAG = "PerformanceTracker"
    
    // Thread-safe storage for ongoing operations
    private val operationStartTimes = ConcurrentHashMap<String, Long>()
    
    /**
     * Start timing an operation
     */
    fun startTracking(operationId: String) {
        operationStartTimes[operationId] = SystemClock.elapsedRealtime()
        Log.d(TAG, "Started tracking: $operationId")
    }
    
    /**
     * Stop timing and report to analytics
     */
    fun stopTracking(operationId: String, category: String = "custom") {
        val startTime = operationStartTimes.remove(operationId)
        if (startTime != null) {
            val duration = SystemClock.elapsedRealtime() - startTime
            reportPerformance(operationId, duration, category)
            Log.d(TAG, "Stopped tracking: $operationId - Duration: ${duration}ms")
        } else {
            Log.w(TAG, "No start time found for operation: $operationId")
        }
    }
    
    /**
     * Track API call performance
     */
    fun trackApiCall(endpoint: String, block: () -> Unit) {
        val startTime = SystemClock.elapsedRealtime()
        var success = true
        
        try {
            block()
        } catch (e: Exception) {
            success = false
            throw e
        } finally {
            val duration = SystemClock.elapsedRealtime() - startTime
            AnalyticsManager.logApiResponseTime(endpoint, duration, success)
        }
    }
    
    /**
     * Track screen load time
     */
    fun trackScreenLoad(screenName: String, block: () -> Unit) {
        val startTime = SystemClock.elapsedRealtime()
        
        try {
            block()
        } finally {
            val duration = SystemClock.elapsedRealtime() - startTime
            AnalyticsManager.logScreenLoadTime(screenName, duration)
        }
    }
    
    /**
     * Report performance metric to analytics
     */
    fun reportPerformance(operationId: String, durationMs: Long, category: String) {
        val params = mapOf(
            "operation_id" to operationId,
            "duration_ms" to durationMs,
            "category" to category
        )
        
        AnalyticsManager.logCustomEvent("performance_metric", params)
        
        // Log warning if operation is slow
        if (durationMs > 1000) {
            Log.w(TAG, "Slow operation detected: $operationId took ${durationMs}ms")
        }
    }
    
    /**
     * Track function execution time
     */
    inline fun <T> trackExecution(operationName: String, block: () -> T): T {
        val startTime = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            val duration = SystemClock.elapsedRealtime() - startTime
            reportPerformance(operationName, duration, "function")
        }
    }
    
    /**
     * Track suspend function execution time
     */
    suspend inline fun <T> trackSuspendExecution(operationName: String, crossinline block: suspend () -> T): T {
        val startTime = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            val duration = SystemClock.elapsedRealtime() - startTime
            reportPerformance(operationName, duration, "suspend_function")
        }
    }
    
    /**
     * Clear all tracking data (for cleanup)
     */
    fun clearAll() {
        operationStartTimes.clear()
        Log.d(TAG, "Cleared all tracking data")
    }
    
    /**
     * Get statistics about tracked operations
     */
    fun getStatistics(): Map<String, Long> {
        val stats = mutableMapOf<String, Long>()
        stats["active_operations"] = operationStartTimes.size.toLong()
        return stats
    }
}

/**
 * Extension function for easy performance tracking
 */
inline fun <T> trackPerformance(operationName: String, block: () -> T): T {
    return PerformanceTracker.trackExecution(operationName, block)
}

