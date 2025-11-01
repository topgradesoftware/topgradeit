package topgrade.parent.com.parentseeks.Parent.Utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.jvm.JvmStatic

/**
 * Consolidated ANR Prevention Helper
 * 
 * This class combines functionality from both Teacher and Parent ANRPreventionHelper classes
 * to provide comprehensive ANR prevention across the entire application.
 * 
 * Features:
 * - Background operation execution with timeout
 * - Safe UI updates with activity lifecycle checks
 * - Coroutine-based operations with timeout and fallback
 * - DataStore safe operations
 * - Performance monitoring
 * - Toast message handling
 * - Async initialization
 * - Thread management utilities
 */
object ConsolidatedANRPreventionHelper {
    
    private const val TAG = "ConsolidatedANRPreventionHelper"
    
    // Timeout constants
    private const val DEFAULT_TIMEOUT_MS = 5000L // 5 seconds
    private const val BACKGROUND_TIMEOUT_MS = 10000L // 10 seconds
    private const val UI_TIMEOUT_MS = 3000L // 3 seconds for UI operations
    
    // Background executor for heavy operations
    private val backgroundExecutor: ExecutorService = Executors.newFixedThreadPool(3)
    
    // Main thread handler for UI updates
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // Coroutine scope for async operations
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // ==================== BACKGROUND OPERATIONS ====================
    
    /**
     * Execute operation in background thread
     * @param operation The operation to execute
     * @param onComplete Callback to execute when operation completes
     * @param onError Callback to execute when operation fails
     */
    @JvmStatic
    fun <T> executeInBackground(
        operation: () -> T,
        onComplete: ((T) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        backgroundExecutor.execute {
            try {
                val result = operation()
                onComplete?.let { callback ->
                    runOnMainThread {
                        try {
                            callback(result)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in onComplete callback: ${e.message}", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in background operation: ${e.message}", e)
                onError?.let { callback ->
                    runOnMainThread {
                        try {
                            callback(e)
                        } catch (callbackError: Exception) {
                            Log.e(TAG, "Error in onError callback: ${callbackError.message}", callbackError)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Execute operation in background with timeout
     * @param operation The operation to execute
     * @param timeoutMs Timeout in milliseconds
     * @param onComplete Callback to execute when operation completes
     * @param onError Callback to execute when operation fails
     * @param onTimeout Callback to execute when operation times out
     */
    @JvmStatic
    fun <T> executeInBackgroundWithTimeout(
        operation: () -> T,
        timeoutMs: Long = BACKGROUND_TIMEOUT_MS,
        onComplete: ((T) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null,
        onTimeout: (() -> Unit)? = null
    ) {
        val future = backgroundExecutor.submit {
            try {
                operation()
            } catch (e: Exception) {
                throw e
            }
        }
        
        try {
            @Suppress("UNCHECKED_CAST")
            val result = future.get(timeoutMs, TimeUnit.MILLISECONDS) as T
            onComplete?.let { callback ->
                runOnMainThread {
                    try {
                        callback(result)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onComplete callback: ${e.message}", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in background operation with timeout: ${e.message}", e)
            if (e is java.util.concurrent.TimeoutException) {
                onTimeout?.let { callback ->
                    runOnMainThread {
                        try {
                            callback()
                        } catch (callbackError: Exception) {
                            Log.e(TAG, "Error in onTimeout callback: ${callbackError.message}", callbackError)
                        }
                    }
                }
            } else {
                onError?.let { callback ->
                    runOnMainThread {
                        try {
                            callback(e)
                        } catch (callbackError: Exception) {
                            Log.e(TAG, "Error in onError callback: ${callbackError.message}", callbackError)
                        }
                    }
                }
            }
        }
    }
    
    // ==================== COROUTINE OPERATIONS ====================
    
    /**
     * Execute coroutine with timeout
     * @param timeoutMs Timeout in milliseconds
     * @param operation The operation to execute
     * @return Result of the operation
     */
    @JvmStatic
    suspend fun <T> executeWithTimeout(
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        operation: suspend () -> T
    ): T {
        return withTimeout(timeoutMs) {
            operation()
        }
    }
    
    /**
     * Execute operation with timeout and fallback
     * @param timeoutMs Timeout in milliseconds
     * @param operation The operation to execute
     * @param fallback The fallback operation
     * @return Result of the operation or fallback
     */
    @JvmStatic
    suspend fun <T> executeWithTimeoutAndFallback(
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        operation: suspend () -> T,
        fallback: suspend () -> T
    ): T {
        return try {
            withTimeout(timeoutMs) {
                operation()
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Operation timed out, using fallback", e)
            fallback()
        } catch (e: Exception) {
            Log.e(TAG, "Operation failed, using fallback", e)
            fallback()
        }
    }
    
    /**
     * Safe DataStore operation with timeout
     * @param operation The DataStore operation
     * @param fallback The fallback value
     * @param timeoutMs Timeout in milliseconds
     * @return Result of the operation or fallback
     */
    @JvmStatic
    suspend fun <T> safeDataStoreOperation(
        operation: suspend () -> T,
        fallback: T,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS
    ): T {
        return executeWithTimeoutAndFallback(
            timeoutMs = timeoutMs,
            operation = operation,
            fallback = { fallback }
        )
    }
    
    // ==================== UI OPERATIONS ====================
    
    /**
     * Run code on main thread
     * @param runnable The code to run
     */
    @JvmStatic
    fun runOnMainThread(runnable: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Already on main thread
            runnable()
        } else {
            // Post to main thread
            mainHandler.post {
                try {
                    runnable()
                } catch (e: Exception) {
                    Log.e(TAG, "Error running on main thread: ${e.message}", e)
                }
            }
        }
    }
    
    /**
     * Run code on main thread with delay
     * @param runnable The code to run
     * @param delayMs Delay in milliseconds
     */
    @JvmStatic
    fun runOnMainThreadDelayed(runnable: () -> Unit, delayMs: Long) {
        mainHandler.postDelayed({
            try {
                runnable()
            } catch (e: Exception) {
                Log.e(TAG, "Error running on main thread delayed: ${e.message}", e)
            }
        }, delayMs)
    }
    
    /**
     * Safe UI update with activity lifecycle check
     * @param activity The activity context
     * @param update The UI update to perform
     */
    @JvmStatic
    fun safeUIUpdate(activity: Activity?, update: () -> Unit) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity is null, finishing, or destroyed - skipping UI update")
            return
        }
        
        runOnMainThread {
            try {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    update()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in safe UI update: ${e.message}", e)
            }
        }
    }
    
    /**
     * Update UI safely (alias for runOnMainThread)
     * @param update The UI update to perform
     */
    @JvmStatic
    fun updateUISafely(update: () -> Unit) {
        runOnMainThread(update)
    }
    
    /**
     * Show toast message safely
     * @param context The context
     * @param message The message to show
     * @param duration The duration (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
     */
    @JvmStatic
    fun showToastSafely(context: Context?, message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (context == null) {
            Log.w(TAG, "Context is null - cannot show toast")
            return
        }
        
        runOnMainThread {
            try {
                Toast.makeText(context, message, duration).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error showing toast: ${e.message}", e)
            }
        }
    }
    
    // ==================== DATASTORE OPERATIONS ====================
    
    /**
     * Initialize DataStore safely
     * @param onComplete Callback when initialization completes
     * @param onError Callback when initialization fails
     */
    @JvmStatic
    fun initializeDataStoreSafely(
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        executeInBackground(
            operation = {
                // Initialize DataStore in background
                Log.d(TAG, "Initializing DataStore in background")
                // Add any heavy initialization here
            },
            onComplete = {
                Log.d(TAG, "DataStore initialized successfully")
                onComplete()
            },
            onError = { exception ->
                Log.e(TAG, "DataStore initialization failed", exception)
                onError(exception)
            }
        )
    }
    
    /**
     * Load user data safely
     * @return UserData object
     */
    @JvmStatic
    suspend fun loadUserDataSafely(): UserData {
        return safeDataStoreOperation(
            operation = {
                // Simulate loading user data from DataStore
                delay(100) // Simulate network delay
                UserData(
                    userId = "user123",
                    userType = "parent",
                    campusId = "campus1",
                    fullName = "John Doe",
                    email = "john@example.com",
                    phone = "+1234567890",
                    picture = "",
                    isLoggedIn = true
                )
            },
            fallback = UserData(
                userId = "",
                userType = "",
                campusId = "",
                fullName = "User",
                email = "",
                phone = "",
                picture = "",
                isLoggedIn = false
            ),
            timeoutMs = 2000L // 2 seconds for user data
        )
    }
    
    /**
     * Load dashboard configuration safely
     * @return Dashboard configuration map
     */
    @JvmStatic
    suspend fun loadDashboardConfigSafely(): Map<String, Any> {
        return safeDataStoreOperation(
            operation = {
                // Simulate loading dashboard config
                delay(150) // Simulate processing delay
                mapOf(
                    "layoutType" to "grid",
                    "gridColumns" to 3,
                    "showCategories" to true,
                    "showSearch" to true,
                    "showFavorites" to true,
                    "enableAnimations" to true,
                    "refreshInterval" to 300,
                    "theme" to "light"
                )
            },
            fallback = mapOf(
                "layoutType" to "grid",
                "gridColumns" to 2,
                "showCategories" to false,
                "showSearch" to false,
                "showFavorites" to false,
                "enableAnimations" to false,
                "refreshInterval" to 300,
                "theme" to "light"
            ),
            timeoutMs = 1500L // 1.5 seconds for config
        )
    }
    
    /**
     * Load user preferences safely
     * @return User preferences map
     */
    @JvmStatic
    suspend fun loadUserPreferencesSafely(): Map<String, Any> {
        return safeDataStoreOperation(
            operation = {
                // Simulate loading user preferences
                delay(100) // Simulate processing delay
                mapOf(
                    "userId" to "user123",
                    "layoutPreference" to "grid",
                    "gridColumns" to 3,
                    "themePreference" to "light",
                    "showNotifications" to true,
                    "autoRefresh" to true,
                    "refreshInterval" to 300,
                    "showBadges" to true,
                    "compactMode" to false,
                    "lastUpdated" to System.currentTimeMillis()
                )
            },
            fallback = mapOf(
                "userId" to "",
                "layoutPreference" to "grid",
                "gridColumns" to 2,
                "themePreference" to "light",
                "showNotifications" to false,
                "autoRefresh" to false,
                "refreshInterval" to 300,
                "showBadges" to false,
                "compactMode" to false,
                "lastUpdated" to 0L
            ),
            timeoutMs = 1000L // 1 second for preferences
        )
    }
    
    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize heavy components asynchronously
     * @param activity The activity
     * @param initOperations List of initialization operations
     * @param onComplete Callback when all operations complete
     */
    @JvmStatic
    fun initializeAsync(
        activity: Activity?,
        initOperations: List<() -> Unit>,
        onComplete: (() -> Unit)? = null
    ) {
        if (initOperations.isEmpty()) {
            onComplete?.invoke()
            return
        }
        
        var completedCount = 0
        val totalOperations = initOperations.size
        
        initOperations.forEach { operation ->
            executeInBackground(
                operation = operation,
                onComplete = {
                    completedCount++
                    if (completedCount >= totalOperations) {
                        safeUIUpdate(activity) {
                            onComplete?.invoke()
                        }
                    }
                },
                onError = { exception ->
                    Log.e(TAG, "Error in initialization operation: ${exception.message}", exception)
                    completedCount++
                    if (completedCount >= totalOperations) {
                        safeUIUpdate(activity) {
                            onComplete?.invoke()
                        }
                    }
                }
            )
        }
    }
    
    // ==================== PERFORMANCE MONITORING ====================
    
    /**
     * Check if operation is taking too long
     * @param operationName Name of the operation for logging
     * @param timeoutMs Timeout in milliseconds
     * @param onTimeout Callback when timeout occurs
     * @return Function to cancel the timeout check
     */
    @JvmStatic
    fun checkOperationTimeout(
        operationName: String,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        onTimeout: () -> Unit
    ): () -> Unit {
        val startTime = System.currentTimeMillis()
        val timeoutRunnable = Runnable {
            val duration = System.currentTimeMillis() - startTime
            Log.w(TAG, "Operation '$operationName' took ${duration}ms (timeout: ${timeoutMs}ms)")
            onTimeout()
        }
        
        mainHandler.postDelayed(timeoutRunnable, timeoutMs)
        
        return {
            mainHandler.removeCallbacks(timeoutRunnable)
        }
    }
    
    /**
     * Monitor performance of an operation
     * @param operationName Name of the operation for logging
     * @param operation The operation to monitor
     * @param warningThresholdMs Warning threshold in milliseconds
     * @return Result of the operation
     */
    @JvmStatic
    fun <T> monitorPerformance(
        operationName: String,
        operation: () -> T,
        warningThresholdMs: Long = 100L
    ): T {
        val startTime = System.currentTimeMillis()
        val result = operation()
        val duration = System.currentTimeMillis() - startTime
        
        if (duration > warningThresholdMs) {
            Log.w(TAG, "Performance warning: $operationName took ${duration}ms (threshold: ${warningThresholdMs}ms)")
        }
        
        return result
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check if current thread is main thread
     * @return true if on main thread, false otherwise
     */
    @JvmStatic
    fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
    
    /**
     * Cleanup resources
     */
    @JvmStatic
    fun cleanup() {
        try {
            coroutineScope.cancel()
            if (!backgroundExecutor.isShutdown) {
                backgroundExecutor.shutdown()
                if (!backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    backgroundExecutor.shutdownNow()
                }
            }
            Log.d(TAG, "ConsolidatedANRPreventionHelper cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
            backgroundExecutor.shutdownNow()
        }
    }
    
    /**
     * Shutdown background executor (alias for cleanup)
     */
    @JvmStatic
    fun shutdown() {
        cleanup()
    }
    
    // ==================== DATA CLASSES ====================
    
    /**
     * Data class for user information
     */
    data class UserData(
        val userId: String,
        val userType: String,
        val campusId: String,
        val fullName: String,
        val email: String,
        val phone: String,
        val picture: String,
        val isLoggedIn: Boolean
    )
}
