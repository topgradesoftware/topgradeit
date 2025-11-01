package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Modern DataStore implementation for Android 15
 * Provides type-safe, efficient data storage with better performance than SharedPreferences
 */
class ModernDataStore(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "topgrade_software_preferences")
        
        // Preference keys
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_TYPE = stringPreferencesKey("user_type")
        private val CAMPUS_ID = stringPreferencesKey("campus_id")
        private val FULL_NAME = stringPreferencesKey("full_name")
        private val EMAIL = stringPreferencesKey("email")
        private val PHONE = stringPreferencesKey("phone")
        private val PICTURE = stringPreferencesKey("picture")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
        private val CURRENT_SESSION = stringPreferencesKey("current_session")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val APP_VERSION = intPreferencesKey("app_version")
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val LANGUAGE = stringPreferencesKey("language")
    }
    
    /**
     * Save user authentication data
     */
    suspend fun saveUserData(
        userId: String,
        userType: String,
        campusId: String,
        fullName: String,
        email: String,
        phone: String,
        picture: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_TYPE] = userType
            preferences[CAMPUS_ID] = campusId
            preferences[FULL_NAME] = fullName
            preferences[EMAIL] = email
            preferences[PHONE] = phone
            preferences[PICTURE] = picture
            preferences[IS_LOGGED_IN] = true
        }
    }
    
    /**
     * Get user data as Flow
     */
    val userData: Flow<UserData> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserData(
                userId = preferences[USER_ID] ?: "",
                userType = preferences[USER_TYPE] ?: "",
                campusId = preferences[CAMPUS_ID] ?: "",
                fullName = preferences[FULL_NAME] ?: "",
                email = preferences[EMAIL] ?: "",
                phone = preferences[PHONE] ?: "",
                picture = preferences[PICTURE] ?: "",
                isLoggedIn = preferences[IS_LOGGED_IN] ?: false
            )
        }
    
    /**
     * Save FCM token
     */
    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[FCM_TOKEN] = token
        }
    }
    
    /**
     * Get FCM token
     */
    val fcmToken: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[FCM_TOKEN] ?: ""
        }
    
    /**
     * Save current session
     */
    suspend fun saveCurrentSession(sessionId: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_SESSION] = sessionId
        }
    }
    
    /**
     * Get current session
     */
    val currentSession: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_SESSION] ?: ""
        }
    
    /**
     * Enable/disable biometric authentication
     */
    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED] = enabled
        }
    }
    
    /**
     * Check if biometric is enabled
     */
    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[BIOMETRIC_ENABLED] ?: false
        }
    
    /**
     * Save app version
     */
    suspend fun saveAppVersion(version: Int) {
        context.dataStore.edit { preferences ->
            preferences[APP_VERSION] = version
        }
    }
    
    /**
     * Get app version
     */
    val appVersion: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[APP_VERSION] ?: 0
        }
    
    /**
     * Save last sync time
     */
    suspend fun saveLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = timestamp
        }
    }
    
    /**
     * Get last sync time
     */
    val lastSyncTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_SYNC_TIME] ?: 0L
        }
    
    /**
     * Save theme mode
     */
    suspend fun saveThemeMode(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = theme
        }
    }
    
    /**
     * Get theme mode
     */
    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: "system"
        }
    
    /**
     * Save language preference
     */
    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
    }
    
    /**
     * Get language preference
     */
    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE] ?: "en"
        }
    
    /**
     * Clear all user data (logout)
     */
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Check if user is logged in
     */
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }
    
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