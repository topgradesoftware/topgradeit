package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong
import topgrade.parent.com.parentseeks.BuildConfig

/**
 * Performance monitoring utility
 * Tracks app performance metrics and provides optimization suggestions
 */
object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    
    // Performance metrics
    private val appStartTime = AtomicLong(0)
    private val screenLoadTimes = mutableMapOf<String, Long>()
    private val memoryUsageHistory = mutableListOf<Long>()
    private val cpuUsageHistory = mutableListOf<Float>()
    
    // Monitoring intervals
    private const val MEMORY_MONITOR_INTERVAL = 30000L // 30 seconds
    private const val CPU_MONITOR_INTERVAL = 60000L // 1 minute
    private const val MAX_MEMORY_HISTORY = 100
    private const val MAX_CPU_HISTORY = 50
    
    private var isMonitoring = false
    private val handler = Handler(Looper.getMainLooper())
    
    /**
     * Start performance monitoring
     */
    @JvmStatic
    fun startMonitoring(_context: Context) {
        // DISABLED: Performance monitoring is causing high CPU usage
        // Only enable in debug mode and only for manual monitoring
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Performance monitoring disabled to prevent high CPU usage")
        }
        return
        
        // Original code commented out to prevent CPU issues
        /*
        if (isMonitoring) return
        
        isMonitoring = true
        appStartTime.set(System.currentTimeMillis())
        
        Log.d(TAG, "Performance monitoring started")
        
        // Start periodic monitoring
        startMemoryMonitoring()
        startCpuMonitoring()
        */
    }
    
    /**
     * Stop performance monitoring
     */
    @JvmStatic
    fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Performance monitoring stopped")
    }
    
    /**
     * Record screen load time
     */
    fun recordScreenLoadTime(screenName: String, loadTime: Long) {
        screenLoadTimes[screenName] = loadTime
        Log.d(TAG, "Screen '$screenName' loaded in ${loadTime}ms")
        
        // Alert if load time is too high
        if (loadTime > 2000) {
            Log.w(TAG, "Slow screen load detected: $screenName took ${loadTime}ms")
        }
    }
    
    /**
     * Get app startup time
     */
    fun getAppStartupTime(): Long {
        return System.currentTimeMillis() - appStartTime.get()
    }
    
    /**
     * Get average screen load time
     */
    fun getAverageScreenLoadTime(): Long {
        return if (screenLoadTimes.isNotEmpty()) {
            screenLoadTimes.values.average().toLong()
        } else {
            0L
        }
    }
    
    /**
     * Get current memory usage
     */
    fun getCurrentMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
    
    /**
     * Get memory usage in MB
     */
    fun getMemoryUsageMB(): Long {
        return getCurrentMemoryUsage() / 1024 / 1024
    }
    
    /**
     * Get max memory in MB
     */
    fun getMaxMemoryMB(): Long {
        return Runtime.getRuntime().maxMemory() / 1024 / 1024
    }
    
    /**
     * Get memory usage percentage
     */
    fun getMemoryUsagePercentage(): Int {
        val used = getCurrentMemoryUsage()
        val max = Runtime.getRuntime().maxMemory()
        return ((used * 100) / max).toInt()
    }
    
    /**
     * Start memory monitoring
     */
    private fun startMemoryMonitoring() {
        val memoryMonitorRunnable = object : Runnable {
            override fun run() {
                if (!isMonitoring) return
                
                CoroutineScope(Dispatchers.IO).launch {
                    val memoryUsage = getCurrentMemoryUsage()
                    memoryUsageHistory.add(memoryUsage)
                    
                    // Keep only recent history
                    if (memoryUsageHistory.size > MAX_MEMORY_HISTORY) {
                        memoryUsageHistory.removeAt(0)
                    }
                    
                    val usageMB = memoryUsage / 1024 / 1024
                    val maxMB = getMaxMemoryMB()
                    val percentage = getMemoryUsagePercentage()
                    
                    Log.d(TAG, "Memory usage: ${usageMB}MB / ${maxMB}MB ($percentage%)")
                    
                    // Alert if memory usage is high
                    if (percentage > 80) {
                        Log.w(TAG, "High memory usage detected: $percentage%")
                        // Note: We can't call optimizeMemory here because we don't have context
                        // The memory optimization will be handled by the system
                    }
                }
                
                // Schedule next check
                if (isMonitoring) {
                    handler.postDelayed(this, MEMORY_MONITOR_INTERVAL)
                }
            }
        }
        
        handler.postDelayed(memoryMonitorRunnable, MEMORY_MONITOR_INTERVAL)
    }
    
    /**
     * Start CPU monitoring
     */
    private fun startCpuMonitoring() {
        val cpuMonitorRunnable = object : Runnable {
            override fun run() {
                if (!isMonitoring) return
                
                CoroutineScope(Dispatchers.IO).launch {
                    val cpuUsage = getCpuUsage()
                    cpuUsageHistory.add(cpuUsage)
                    
                    // Keep only recent history
                    if (cpuUsageHistory.size > MAX_CPU_HISTORY) {
                        cpuUsageHistory.removeAt(0)
                    }
                    
                    Log.d(TAG, "CPU usage: ${cpuUsage}%")
                    
                    // Alert if CPU usage is high
                    if (cpuUsage > 80) {
                        Log.w(TAG, "High CPU usage detected: ${cpuUsage}%")
                    }
                }
                
                // Schedule next check
                if (isMonitoring) {
                    handler.postDelayed(this, CPU_MONITOR_INTERVAL)
                }
            }
        }
        
        handler.postDelayed(cpuMonitorRunnable, CPU_MONITOR_INTERVAL)
    }
    
    /**
     * Get CPU usage (simplified implementation)
     */
    private fun getCpuUsage(): Float {
        return try {
            // This is a simplified CPU usage calculation
            // In a real implementation, you'd read from /proc/stat
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            
            // Estimate CPU usage based on memory pressure
            val memoryPressure = (usedMemory.toFloat() / totalMemory.toFloat()) * 100
            memoryPressure.coerceIn(0f, 100f)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating CPU usage", e)
            0f
        }
    }
    
    /**
     * Optimize memory usage
     */
    private suspend fun optimizeMemory(context: Context) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting memory optimization")
            
            // Clear Glide cache if it's too large
            clearGlideCache(context)
            
            // Clear other caches
            clearOtherCaches()
            
            // Trigger garbage collection
            System.gc()
            
            Log.d(TAG, "Memory optimization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during memory optimization", e)
        }
    }
    
    /**
     * Clear Glide cache
     */
    private fun clearGlideCache(context: Context) {
        try {
            com.bumptech.glide.Glide.get(context).clearMemory()
            Log.d(TAG, "Glide memory cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing Glide cache", e)
        }
    }
    
    /**
     * Clear other caches
     */
    private fun clearOtherCaches() {
        try {
            // Clear string pool
            System.runFinalization()
            
            Log.d(TAG, "Other caches cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing other caches", e)
        }
    }
    
    /**
     * Get performance report
     */
    fun getPerformanceReport(): String {
        val report = StringBuilder()
        report.append("=== Performance Report ===\n")
        report.append("App startup time: ${getAppStartupTime()}ms\n")
        report.append("Average screen load time: ${getAverageScreenLoadTime()}ms\n")
        report.append("Current memory usage: ${getMemoryUsageMB()}MB / ${getMaxMemoryMB()}MB (${getMemoryUsagePercentage()}%)\n")
        
        if (screenLoadTimes.isNotEmpty()) {
            report.append("\nScreen Load Times:\n")
            screenLoadTimes.forEach { (screen, time) ->
                report.append("  $screen: ${time}ms\n")
            }
        }
        
        if (memoryUsageHistory.isNotEmpty()) {
            val avgMemory = memoryUsageHistory.average() / 1024 / 1024
            report.append("\nAverage memory usage: ${avgMemory.toLong()}MB\n")
        }
        
        return report.toString()
    }
    
    /**
     * Log performance report
     */
    fun logPerformanceReport() {
        Log.i(TAG, getPerformanceReport())
    }
    
    /**
     * Check if performance is acceptable
     */
    fun isPerformanceAcceptable(): Boolean {
        val startupTime = getAppStartupTime()
        val avgLoadTime = getAverageScreenLoadTime()
        val memoryPercentage = getMemoryUsagePercentage()
        
        return startupTime < 5000 && // App should start within 5 seconds
               avgLoadTime < 1000 && // Screens should load within 1 second
               memoryPercentage < 80 // Memory usage should be below 80%
    }
    
    /**
     * Get performance suggestions
     */
    fun getPerformanceSuggestions(): List<String> {
        val suggestions = mutableListOf<String>()
        
        val startupTime = getAppStartupTime()
        if (startupTime > 5000) {
            suggestions.add("App startup time is slow (${startupTime}ms). Consider lazy loading and background initialization.")
        }
        
        val avgLoadTime = getAverageScreenLoadTime()
        if (avgLoadTime > 1000) {
            suggestions.add("Screen load times are slow (${avgLoadTime}ms). Consider optimizing layouts and reducing view hierarchy.")
        }
        
        val memoryPercentage = getMemoryUsagePercentage()
        if (memoryPercentage > 80) {
            suggestions.add("Memory usage is high (${memoryPercentage}%). Consider implementing memory management and cache clearing.")
        }
        
        return suggestions
    }
}
