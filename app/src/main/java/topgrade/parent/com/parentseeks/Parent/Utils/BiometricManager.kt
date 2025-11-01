package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class BiometricManager(private val context: Context) {
    
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "BiometricKey"
        private const val PREFS_NAME = "BiometricPrefs"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_USER_CREDENTIALS = "user_credentials"
        private const val KEY_IV = "encryption_iv"
    }
    
    // Use fully-qualified name to avoid any ambiguity with this class name
    private val biometricManagerAndroidX = androidx.biometric.BiometricManager.from(context)
    private var encryptedPrefs: SharedPreferences? = null
    private var masterKey: MasterKey? = null
    private var isInitialized = false
    
    init {
        initializeEncryptedPreferences()
    }
    
    private fun initializeEncryptedPreferences() {
        if (isInitialized) {
            return // Prevent multiple initialization attempts
        }
        
        try {
            // First, try to clear any potentially corrupted encrypted preferences
            try {
                val regularPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                if (regularPrefs.contains(KEY_USER_CREDENTIALS) || regularPrefs.contains(KEY_BIOMETRIC_ENABLED)) {
                    regularPrefs.edit().clear().apply()
                    android.util.Log.w("BiometricManager", "Cleared potentially corrupted biometric preferences.")
                }
            } catch (e: Exception) {
                android.util.Log.w("BiometricManager", "Could not clear regular preferences: ${e.message}")
            }

            // Build the master key first
            val builtMasterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Create encrypted shared preferences using the built key
            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                builtMasterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            masterKey = builtMasterKey
            isInitialized = true
            android.util.Log.d("BiometricManager", "Encrypted preferences initialized successfully")
        } catch (e: Throwable) {
            // Catch all Throwables to avoid crashes on devices/environments where
            // Android Keystore or Tink may be unavailable or misconfigured
            android.util.Log.e("BiometricManager", "Failed to initialize encrypted preferences: ${e.message}")
            e.printStackTrace()
            
            // Fallback to regular SharedPreferences if encrypted preferences fail
            try {
                // Clear any existing data in regular preferences to ensure clean state
                val regularPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                regularPrefs.edit().clear().apply()
                
                encryptedPrefs = regularPrefs
                isInitialized = true
                android.util.Log.w("BiometricManager", "Fallback to regular SharedPreferences due to encryption error.")
            } catch (ex: Throwable) {
                android.util.Log.e("BiometricManager", "Failed to initialize any SharedPreferences for biometric data: ${ex.message}")
                ex.printStackTrace()
                isInitialized = false
            }
        }
    }
    
    private fun ensureInitialized(): Boolean {
        if (!isInitialized) {
            initializeEncryptedPreferences()
        }
        return isInitialized && encryptedPrefs != null
    }
    
    /**
     * Check if biometric authentication is available on the device
     */
    fun isBiometricAvailable(): Boolean {
        return when (biometricManagerAndroidX.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> true
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else -> false
        }
    }
    
    /**
     * Check if biometric authentication is enabled for the app
     */
    fun isBiometricEnabled(): Boolean {
        return if (ensureInitialized()) {
            encryptedPrefs?.getBoolean(KEY_BIOMETRIC_ENABLED, false) ?: false
        } else {
            false
        }
    }
    
    /**
     * Enable biometric authentication and store user credentials (simplified version for backward compatibility)
     */
    fun enableBiometric(email: String, password: String, campusId: String) {
        if (!ensureInitialized()) return
        
        val credentials = "$email|$password|$campusId"
        encryptedPrefs?.edit()
            ?.putString(KEY_USER_CREDENTIALS, credentials)
            ?.putBoolean(KEY_BIOMETRIC_ENABLED, true)
            ?.apply()
    }

    /**
     * Enable biometric authentication and store user credentials for a specific user and type
     */
    fun enableBiometric(userType: String, userId: String, email: String, password: String, campusId: String) {
        if (!ensureInitialized()) return
        
        val credentials = "$email|$password|$campusId"
        val key = "user_credentials_${userType}_${userId}"
        encryptedPrefs?.edit()
            ?.putString(key, credentials)
            ?.putBoolean("biometric_enabled_${userType}_${userId}", true)
            ?.apply()
    }

    /**
     * Disable biometric authentication (simplified version for backward compatibility)
     */
    fun disableBiometric() {
        if (!ensureInitialized()) return
        
        encryptedPrefs?.edit()
            ?.remove(KEY_USER_CREDENTIALS)
            ?.putBoolean(KEY_BIOMETRIC_ENABLED, false)
            ?.apply()
    }

    /**
     * Disable biometric authentication for a specific user and type
     */
    fun disableBiometric(userType: String, userId: String) {
        if (!ensureInitialized()) return
        
        val key = "user_credentials_${userType}_${userId}"
        encryptedPrefs?.edit()
            ?.remove(key)
            ?.putBoolean("biometric_enabled_${userType}_${userId}", false)
            ?.apply()
    }

    /**
     * Get stored user credentials (simplified version for backward compatibility)
     */
    fun getStoredCredentials(): Triple<String, String, String>? {
        if (!ensureInitialized()) return null
        
        val credentials = encryptedPrefs?.getString(KEY_USER_CREDENTIALS, null)
        return credentials?.let {
            val parts = it.split("|")
            if (parts.size == 3) {
                Triple(parts[0], parts[1], parts[2])
            } else null
        }
    }

    /**
     * Get stored user credentials for a specific user and type
     */
    fun getStoredCredentials(userType: String, userId: String): Triple<String, String, String>? {
        if (!ensureInitialized()) return null
        
        val key = "user_credentials_${userType}_${userId}"
        val credentials = encryptedPrefs?.getString(key, null)
        return credentials?.let {
            val parts = it.split("|")
            if (parts.size == 3) {
                Triple(parts[0], parts[1], parts[2])
            } else null
        }
    }

    /**
     * Get all saved user IDs for a given user type
     */
    fun getAllSavedUserIds(userType: String): List<String> {
        if (!ensureInitialized()) return emptyList()
        
        return encryptedPrefs?.all?.keys?.filter { it.startsWith("user_credentials_${userType}_") }
            ?.map { it.removePrefix("user_credentials_${userType}_") } ?: emptyList()
    }
    
    /**
     * Clear all biometric data (for debugging/reset purposes)
     */
    fun clearAllBiometricData() {
        if (!ensureInitialized()) return
        
        encryptedPrefs?.edit()?.clear()?.apply()
    }
    
    /**
     * Check if device supports biometric authentication
     */
    fun getBiometricStatus(): BiometricStatus {
        return when (biometricManagerAndroidX.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HW_UNAVAILABLE
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NONE_ENROLLED
            else -> BiometricStatus.UNKNOWN
        }
    }
    
    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        HW_UNAVAILABLE,
        NONE_ENROLLED,
        UNKNOWN
    }
    
    /**
     * Show biometric authentication prompt for a specific user and type
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        userType: String,
        userId: String,
        onSuccess: (Triple<String, String, String>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!ensureInitialized()) {
            onError("Biometric manager not initialized")
            return
        }
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val credentials = getStoredCredentials(userType, userId)
                    if (credentials != null) {
                        onSuccess(credentials)
                    } else {
                        onError("No stored credentials found")
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed")
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Show biometric authentication prompt (simplified version for backward compatibility)
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: (Triple<String, String, String>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!ensureInitialized()) {
            onError("Biometric manager not initialized")
            return
        }
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val credentials = getStoredCredentials()
                    if (credentials != null) {
                        onSuccess(credentials)
                    } else {
                        onError("No stored credentials found")
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed")
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }
} 