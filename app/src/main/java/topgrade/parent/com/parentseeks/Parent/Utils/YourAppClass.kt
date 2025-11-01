@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Utils

import android.app.Application
import android.util.Log
import topgrade.parent.com.parentseeks.BuildConfig
import topgrade.parent.com.parentseeks.Parent.Utils.NotificationUtils

class TopgradeApplication : Application() {
    
    companion object {
        private const val TAG = "TopgradeApplication"
    }
    

    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Disable StrictMode for main thread to prevent performance issues
            if (BuildConfig.DEBUG) {
                disableStrictModeForMainThread()
            }
            
            // Initialize Paper DB (ONLY HERE - Don't call Paper.init() in activities!)
            io.paperdb.Paper.init(this)
            
            // Initialize UserDataManager (uses PaperDB, so must be after Paper.init())
            UserDataManager.init(this)
            Log.d(TAG, "UserDataManager initialized successfully")
            
            // Load constants from Paper DB
            topgrade.parent.com.parentseeks.Teacher.Utils.Constant.loadFromPaper()
            Log.d(TAG, "Constants loaded from Paper DB")
            
            // Initialize only critical components immediately
            initializeNotificationChannels()
            
            // Defer non-critical initializations to background
            initializeNonCriticalComponentsInBackground()
            
            // Simple initialization without complex operations
            Log.d(TAG, "Application started - Debug mode: ${BuildConfig.DEBUG}")
            
            // Set up basic uncaught exception handler
            if (BuildConfig.DEBUG) {
                Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                    Log.e(TAG, "Uncaught exception in thread: ${thread.name}", throwable)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during application initialization", e)
        }
    }
    
    
    /**
     * Initialize non-critical components in background to avoid blocking startup
     */
    private fun initializeNonCriticalComponentsInBackground() {
        try {
            // Use a background thread for non-critical initializations
            Thread {
                try {
                    // Initialize Analytics Manager in background
                    AnalyticsManager.initialize(this@TopgradeApplication)
                    AnalyticsManager.setAnalyticsEnabled(true)
                    Log.d(TAG, "AnalyticsManager initialized successfully in background")
                    
                    // Log app open event in background
                    AnalyticsManager.logAppOpen()
                    
                    // Initialize performance optimizations in background
                    initializePerformanceOptimizations()
                    
                    // Memory optimization in background
                    initializeMemoryOptimization()
                    
                    // Initialize network error handler in background
                    initializeNetworkErrorHandler()
                    
                    Log.d(TAG, "All non-critical components initialized in background")
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing non-critical components in background", e)
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting background initialization", e)
        }
    }
    
    /**
     * Initialize performance optimization components
     */
    private fun initializePerformanceOptimizations() {
        try {
            // DISABLED: Performance optimizer to prevent high CPU usage
            // PerformanceOptimizer.initialize(this)
            
            // Only basic initialization without any background operations
            Log.d(TAG, "Performance optimizations disabled to prevent high CPU usage")
            
            // Manual memory monitoring available on-demand if needed
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Debug mode - manual memory monitoring available via PerformanceOptimizer.monitorMemoryUsage()")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing performance optimizations", e)
        }
    }
    
    /**
     * Initialize notification channels
     */
    private fun initializeNotificationChannels() {
        try {
            NotificationUtils.createNotificationChannels(this)
            Log.d(TAG, "Notification channels initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing notification channels", e)
        }
    }
    
    /**
     * Initialize memory optimization
     */
    private fun initializeMemoryOptimization() {
        try {
            // Set memory allocation strategy
            System.setProperty("java.vm.heapgrowthlimit", "512m")
            System.setProperty("java.vm.heapsize", "1g")
            
            // Enable memory optimization flags
            System.setProperty("android.enableR8.fullMode", "true")
            System.setProperty("android.enableJetifier", "false") // Disabled to reduce CPU usage
            
            // Set garbage collection optimization
            System.setProperty("java.vm.gc.type", "G1")
            
            // CPU optimization - reduce background processing
            System.setProperty("java.vm.threads.max", "4")
            System.setProperty("android.enableBackgroundProcessing", "false")
            
            Log.d(TAG, "Memory and CPU optimization initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing memory optimization", e)
        }
    }
    
    /**
     * Get performance optimizer instance
     */
    fun getPerformanceOptimizer(): PerformanceOptimizer? {
        return null // PerformanceOptimizer is now a static utility
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Low memory warning received")
        
        // DISABLED: Continuous monitoring to prevent high CPU usage
        // Only log the warning without additional monitoring
        Log.w(TAG, "Low memory warning - consider clearing caches")
        
        // Manual memory check only when needed
        // PerformanceOptimizer.monitorMemoryUsage()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "Trim memory level: $level")
        
        // DISABLED: Continuous monitoring to prevent high CPU usage
        // Only log the trim level without additional monitoring
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            Log.w(TAG, "Moderate memory trim requested - level: $level")
        }
        
        // Manual memory check only when needed
        // PerformanceOptimizer.monitorMemoryUsage()
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        // Memory monitoring cleanup handled by PerformanceOptimizer
        
        // Cleanup performance optimizer
        PerformanceOptimizer.cleanup()
        
        Log.d(TAG, "Application terminating - cleanup completed")
    }
    
    /**
     * Disable StrictMode for main thread to prevent performance issues
     */
    private fun disableStrictModeForMainThread() {
        try {
            android.os.StrictMode.setThreadPolicy(
                android.os.StrictMode.ThreadPolicy.Builder()
                    .build()
            )
            Log.d(TAG, "StrictMode disabled for main thread")
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling StrictMode", e)
        }
    }
    
    /**
     * Initialize network error handler
     */
    private fun initializeNetworkErrorHandler() {
        try {
            val networkErrorHandler = NetworkErrorHandler.getInstance(this)
            networkErrorHandler.startMonitoring()
            
            // Add system network error handler
            networkErrorHandler.addErrorHandler(object : NetworkErrorHandler.NetworkErrorCallback {
                override fun onSystemNetworkError(exception: Exception) {
                    Log.w(TAG, "System network error detected: ${exception.message}")
                    // Log the error but don't crash the app
                    // These are system-level errors that we can't control
                }
            })
            
            Log.d(TAG, "Network error handler initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing network error handler", e)
        }
    }
} 