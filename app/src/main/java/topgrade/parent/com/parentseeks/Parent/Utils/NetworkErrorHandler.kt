@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Network Error Handler for managing system-level network issues
 * Handles BPF errors, tethering issues, and network stack problems
 */
class NetworkErrorHandler private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkErrorHandler"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L
        private const val NETWORK_TIMEOUT_MS = 10000L
        
        @Volatile
        private var INSTANCE: NetworkErrorHandler? = null
        
        @JvmStatic
        fun getInstance(context: Context): NetworkErrorHandler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkErrorHandler(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val isNetworkCallbackRegistered = AtomicBoolean(false)
    private val networkCallbacks = mutableListOf<NetworkCallback>()
    private val errorHandlers = mutableListOf<NetworkErrorCallback>()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available: $network")
            notifyNetworkAvailable(network)
        }
        
        override fun onLost(network: Network) {
            Log.w(TAG, "Network lost: $network")
            notifyNetworkLost(network)
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            Log.d(TAG, "Network capabilities changed: $networkCapabilities")
            notifyCapabilitiesChanged(network, networkCapabilities)
        }
        
        override fun onUnavailable() {
            Log.e(TAG, "Network unavailable")
            notifyNetworkUnavailable()
        }
    }
    
    /**
     * Start monitoring network state
     */
    fun startMonitoring() {
        if (isNetworkCallbackRegistered.compareAndSet(false, true)) {
            try {
                val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                    .build()
                
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
                Log.d(TAG, "Network monitoring started")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start network monitoring", e)
                isNetworkCallbackRegistered.set(false)
            }
        }
    }
    
    /**
     * Stop monitoring network state
     */
    fun stopMonitoring() {
        if (isNetworkCallbackRegistered.compareAndSet(true, false)) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
                Log.d(TAG, "Network monitoring stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop network monitoring", e)
            }
        }
    }
    
    /**
     * Check if network is available
     */
    fun isNetworkAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }
    
    /**
     * Handle network errors with retry logic
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = MAX_RETRY_ATTEMPTS,
        delayMs: Long = RETRY_DELAY_MS,
        block: suspend () -> T
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                if (!isNetworkAvailable()) {
                    throw IOException("Network not available")
                }
                
                return Result.success(block())
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Network operation failed (attempt ${attempt + 1}/$maxRetries): ${e.message}")
                
                // Check if it's a system-level network error
                if (isSystemNetworkError(e)) {
                    Log.w(TAG, "System-level network error detected: ${e.message}")
                    notifySystemNetworkError(e)
                }
                
                if (attempt < maxRetries - 1) {
                    delay(delayMs * (attempt + 1)) // Exponential backoff
                }
            }
        }
        
        return Result.failure(lastException ?: IOException("Network operation failed after $maxRetries attempts"))
    }
    
    /**
     * Check if exception is a system-level network error
     */
    private fun isSystemNetworkError(exception: Exception): Boolean {
        val message = exception.message ?: ""
        return message.contains("E2BIG") ||
               message.contains("BPF") ||
               message.contains("tethering") ||
               message.contains("networkstack") ||
               message.contains("Argument list too long") ||
               exception is SocketTimeoutException
    }
    
    /**
     * Add network callback
     */
    fun addNetworkCallback(callback: NetworkCallback) {
        networkCallbacks.add(callback)
    }
    
    /**
     * Remove network callback
     */
    fun removeNetworkCallback(callback: NetworkCallback) {
        networkCallbacks.remove(callback)
    }
    
    /**
     * Add error handler
     */
    fun addErrorHandler(handler: NetworkErrorCallback) {
        errorHandlers.add(handler)
    }
    
    /**
     * Remove error handler
     */
    fun removeErrorHandler(handler: NetworkErrorCallback) {
        errorHandlers.remove(handler)
    }
    
    private fun notifyNetworkAvailable(network: Network) {
        networkCallbacks.forEach { callback ->
            try {
                callback.onNetworkAvailable(network)
            } catch (e: Exception) {
                Log.e(TAG, "Error in network callback", e)
            }
        }
    }
    
    private fun notifyNetworkLost(network: Network) {
        networkCallbacks.forEach { callback ->
            try {
                callback.onNetworkLost(network)
            } catch (e: Exception) {
                Log.e(TAG, "Error in network callback", e)
            }
        }
    }
    
    private fun notifyCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
        networkCallbacks.forEach { callback ->
            try {
                callback.onCapabilitiesChanged(network, capabilities)
            } catch (e: Exception) {
                Log.e(TAG, "Error in network callback", e)
            }
        }
    }
    
    private fun notifyNetworkUnavailable() {
        networkCallbacks.forEach { callback ->
            try {
                callback.onNetworkUnavailable()
            } catch (e: Exception) {
                Log.e(TAG, "Error in network callback", e)
            }
        }
    }
    
    private fun notifySystemNetworkError(exception: Exception) {
        errorHandlers.forEach { handler ->
            try {
                handler.onSystemNetworkError(exception)
            } catch (e: Exception) {
                Log.e(TAG, "Error in error handler", e)
            }
        }
    }
    
    /**
     * Network callback interface
     */
    interface NetworkCallback {
        fun onNetworkAvailable(network: Network) {}
        fun onNetworkLost(network: Network) {}
        fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {}
        fun onNetworkUnavailable() {}
    }
    
    /**
     * Network error callback interface
     */
    interface NetworkErrorCallback {
        fun onSystemNetworkError(exception: Exception) {}
    }
}
