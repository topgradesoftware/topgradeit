package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * DataStore implementation for User Authentication Data
 * Modern replacement for Paper DB authentication storage
 */
class AuthDataStore(private val context: Context) {
    
    companion object {
        private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_data")
        
        // Authentication preference keys
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_TYPE = stringPreferencesKey("user_type")
        private val CAMPUS_ID = stringPreferencesKey("campus_id")
        private val FULL_NAME = stringPreferencesKey("full_name")
        private val EMAIL = stringPreferencesKey("email")
        private val PHONE = stringPreferencesKey("phone")
        private val LANDLINE = stringPreferencesKey("landline")
        private val ADDRESS = stringPreferencesKey("address")
        private val PICTURE = stringPreferencesKey("picture")
        private val PASSWORD = stringPreferencesKey("password")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val CURRENT_SESSION = stringPreferencesKey("current_session")
        private val CAMPUS_NAME = stringPreferencesKey("campus_name")
        private val STAFF_ID = stringPreferencesKey("staff_id")
        private val STAFF_PASSWORD = stringPreferencesKey("staff_password")
        private val CAMPUS_ADDRESS = stringPreferencesKey("campus_address")
        private val CAMPUS_PHONE = stringPreferencesKey("campus_phone")
    }
    
    /**
     * Save complete user authentication data
     */
    suspend fun saveUserAuthData(
        userId: String,
        userType: String,
        campusId: String,
        fullName: String,
        email: String,
        phone: String,
        landline: String = "",
        address: String = "",
        picture: String = "",
        password: String = "",
        currentSession: String = "",
        campusName: String = ""
    ) {
        context.authDataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_TYPE] = userType
            preferences[CAMPUS_ID] = campusId
            preferences[FULL_NAME] = fullName
            preferences[EMAIL] = email
            preferences[PHONE] = phone
            preferences[LANDLINE] = landline
            preferences[ADDRESS] = address
            preferences[PICTURE] = picture
            preferences[PASSWORD] = password
            preferences[IS_LOGGED_IN] = true
            preferences[CURRENT_SESSION] = currentSession
            preferences[CAMPUS_NAME] = campusName
        }
    }
    
    /**
     * Save staff-specific authentication data
     */
    suspend fun saveStaffAuthData(
        staffId: String,
        userType: String,
        campusId: String,
        fullName: String,
        email: String,
        phone: String,
        picture: String = "",
        password: String = "",
        campusName: String = "",
        campusAddress: String = "",
        campusPhone: String = "",
        currentSession: String = ""
    ) {
        context.authDataStore.edit { preferences ->
            preferences[STAFF_ID] = staffId
            preferences[USER_TYPE] = userType
            preferences[CAMPUS_ID] = campusId
            preferences[FULL_NAME] = fullName
            preferences[EMAIL] = email
            preferences[PHONE] = phone
            preferences[PICTURE] = picture
            preferences[STAFF_PASSWORD] = password
            preferences[IS_LOGGED_IN] = true
            preferences[CAMPUS_NAME] = campusName
            preferences[CAMPUS_ADDRESS] = campusAddress
            preferences[CAMPUS_PHONE] = campusPhone
            preferences[CURRENT_SESSION] = currentSession
        }
    }
    
    /**
     * Get user authentication data as Flow
     */
    val userAuthData: Flow<UserAuthData> = context.authDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserAuthData(
                userId = preferences[USER_ID] ?: "",
                userType = preferences[USER_TYPE] ?: "",
                campusId = preferences[CAMPUS_ID] ?: "",
                fullName = preferences[FULL_NAME] ?: "",
                email = preferences[EMAIL] ?: "",
                phone = preferences[PHONE] ?: "",
                landline = preferences[LANDLINE] ?: "",
                address = preferences[ADDRESS] ?: "",
                picture = preferences[PICTURE] ?: "",
                password = preferences[PASSWORD] ?: "",
                isLoggedIn = preferences[IS_LOGGED_IN] ?: false,
                currentSession = preferences[CURRENT_SESSION] ?: "",
                campusName = preferences[CAMPUS_NAME] ?: "",
                staffId = preferences[STAFF_ID] ?: "",
                staffPassword = preferences[STAFF_PASSWORD] ?: "",
                campusAddress = preferences[CAMPUS_ADDRESS] ?: "",
                campusPhone = preferences[CAMPUS_PHONE] ?: ""
            )
        }
    
    /**
     * Get specific authentication values
     */
    val isLoggedIn: Flow<Boolean> = context.authDataStore.data
        .map { preferences -> preferences[IS_LOGGED_IN] ?: false }
    
    val userType: Flow<String> = context.authDataStore.data
        .map { preferences -> preferences[USER_TYPE] ?: "" }
    
    val userId: Flow<String> = context.authDataStore.data
        .map { preferences -> preferences[USER_ID] ?: "" }
    
    val campusId: Flow<String> = context.authDataStore.data
        .map { preferences -> preferences[CAMPUS_ID] ?: "" }
    
    val fullName: Flow<String> = context.authDataStore.data
        .map { preferences -> preferences[FULL_NAME] ?: "" }
    
    val currentSession: Flow<String> = context.authDataStore.data
        .map { preferences -> preferences[CURRENT_SESSION] ?: "" }
    
    /**
     * Update specific fields
     */
    suspend fun updatePassword(password: String) {
        context.authDataStore.edit { preferences ->
            preferences[PASSWORD] = password
        }
    }
    
    suspend fun updateProfile(
        fullName: String,
        email: String,
        phone: String,
        landline: String = "",
        address: String = ""
    ) {
        context.authDataStore.edit { preferences ->
            preferences[FULL_NAME] = fullName
            preferences[EMAIL] = email
            preferences[PHONE] = phone
            preferences[LANDLINE] = landline
            preferences[ADDRESS] = address
        }
    }
    
    suspend fun updatePicture(picture: String) {
        context.authDataStore.edit { preferences ->
            preferences[PICTURE] = picture
        }
    }
    
    /**
     * Clear all authentication data (logout)
     */
    suspend fun clearAuthData() {
        context.authDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Set login state
     */
    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        context.authDataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }
    
    /**
     * Check if user is logged in (synchronous for compatibility)
     */
    suspend fun isUserLoggedIn(): Boolean {
        return context.authDataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }.first()
    }
    
    /**
     * Get user type (synchronous for compatibility)
     */
    suspend fun getUserType(): String {
        return context.authDataStore.data.map { preferences ->
            preferences[USER_TYPE] ?: ""
        }.first()
    }
}

/**
 * Data class for user authentication data
 */
data class UserAuthData(
    val userId: String,
    val userType: String,
    val campusId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val landline: String,
    val address: String,
    val picture: String,
    val password: String,
    val isLoggedIn: Boolean,
    val currentSession: String,
    val campusName: String,
    val staffId: String,
    val staffPassword: String,
    val campusAddress: String,
    val campusPhone: String
) 