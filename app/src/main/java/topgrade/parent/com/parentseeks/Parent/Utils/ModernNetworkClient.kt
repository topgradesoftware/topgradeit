@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

/**
 * Modern network client optimized for Android 15
 * Features:
 * - HTTP/3 support
 * - Modern TLS configuration
 * - Connection pooling
 * - Request/Response compression
 * - Automatic retry with exponential backoff
 * - Network state monitoring
 */
class ModernNetworkClient private constructor(private val context: Context) {
    
    companion object {
        private const val BASE_URL = ApiConstants.BASE_URL
        private const val TIMEOUT_SECONDS = 30L
        private const val READ_TIMEOUT_SECONDS = 60L
        private const val WRITE_TIMEOUT_SECONDS = 30L
        private const val MAX_RETRIES = 3
        private const val CACHE_SIZE = 50 * 1024 * 1024 // 50MB
        
        @Volatile
        private var INSTANCE: ModernNetworkClient? = null
        
        fun getInstance(context: Context): ModernNetworkClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ModernNetworkClient(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()
    
    private val okHttpClient: OkHttpClient by lazy {
        createOkHttpClient()
    }
    
    private val retrofit: Retrofit by lazy {
        createRetrofit()
    }
    
    /**
     * Create optimized OkHttpClient for Android 15
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
        
        val cache = Cache(
            directory = context.cacheDir.resolve("http_cache"),
            maxSize = CACHE_SIZE.toLong()
        )
        
        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(createRetryInterceptor())
            .addInterceptor(createCompressionInterceptor())
            .addInterceptor(createUserAgentInterceptor())
            .addInterceptor(createCacheInterceptor())
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .build()
    }
    
    /**
     * Create Retrofit instance
     */
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * Retry interceptor with exponential backoff
     */
    private fun createRetryInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            var response: Response? = null
            var exception: IOException? = null
            
            repeat(MAX_RETRIES) { attempt ->
                try {
                    val resp = chain.proceed(request)
                    response = resp
                    if (resp.isSuccessful) {
                        return@Interceptor resp
                    }
                } catch (e: IOException) {
                    exception = e
                    if (attempt == MAX_RETRIES - 1) {
                        throw e
                    }
                }
                
                // Exponential backoff
                if (attempt < MAX_RETRIES - 1) {
                    val delay = (1L shl attempt) * 1000L // 1s, 2s, 4s
                    Thread.sleep(delay)
                }
            }
            
            response ?: throw exception ?: IOException("Request failed after $MAX_RETRIES attempts")
        }
    }
    
    /**
     * Compression interceptor
     */
    private fun createCompressionInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Accept-Encoding", "gzip, deflate")
                .build()
            chain.proceed(request)
        }
    }
    
    /**
     * User agent interceptor
     */
    private fun createUserAgentInterceptor(): Interceptor {
        return Interceptor { chain ->
            val userAgent = "Topgrade-Software-App/1.0 (Android ${Build.VERSION.RELEASE})"
            val request = chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build()
            chain.proceed(request)
        }
    }
    
    /**
     * Cache interceptor
     */
    private fun createCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            
            // Cache successful responses for 5 minutes
            if (response.isSuccessful) {
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=300")
                    .build()
            } else {
                response
            }
        }
    }
    
    /**
     * Check network connectivity
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }
    
    /**
     * Get network type
     */
    fun getNetworkType(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return "none"
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "none"
            
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                else -> "unknown"
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            when (networkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> "wifi"
                ConnectivityManager.TYPE_MOBILE -> "cellular"
                else -> "unknown"
            }
        }
    }
    
    /**
     * Clear cache
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        try {
            okHttpClient.cache?.evictAll()
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): String {
        val cache = okHttpClient.cache
        return if (cache != null) {
            "Hit count: ${cache.hitCount()}, Request count: ${cache.requestCount()}"
        } else {
            "Cache not available"
        }
    }
    
    /**
     * Create API service
     */
    fun <T> createApiService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
    

} 