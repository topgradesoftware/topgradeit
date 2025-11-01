package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import topgrade.parent.com.parentseeks.Parent.Model.ChildModel
import topgrade.parent.com.parentseeks.Parent.Model.timetable.StudentTimetableResponse
import topgrade.parent.com.parentseeks.Teacher.Model.Timetable
import topgrade.parent.com.parentseeks.Teacher.Model.TimetableModel
import java.io.IOException

/**
 * DataStore implementation for Timetable Data Storage
 * Modern replacement for Paper DB timetable storage
 * Supports both Student and Staff timetables with caching and offline access
 */
class TimetableDataStore(private val context: Context) {
    
    companion object {
        private val Context.timetableDataStore: DataStore<Preferences> by preferencesDataStore(name = "timetable_data")
        
        // Student Timetable Keys
        private val CURRENT_CHILD_MODEL_JSON = stringPreferencesKey("current_child_model_json")
        private val STUDENT_LIST_JSON = stringPreferencesKey("student_list_json")
        private val STUDENT_TIMETABLE_CACHE_JSON = stringPreferencesKey("student_timetable_cache_json")
        private val STUDENT_TIMETABLE_CACHE_TIME = longPreferencesKey("student_timetable_cache_time")
        private val STUDENT_TIMETABLE_SESSION_ID = stringPreferencesKey("student_timetable_session_id")
        private val STUDENT_TIMETABLE_SESSIONS_JSON = stringPreferencesKey("student_timetable_sessions_json")
        
        // Staff Timetable Keys
        private val STAFF_TIMETABLE_CACHE_JSON = stringPreferencesKey("staff_timetable_cache_json")
        private val STAFF_TIMETABLE_CACHE_TIME = longPreferencesKey("staff_timetable_cache_time")
        private val STAFF_TIMETABLE_SESSION_ID = stringPreferencesKey("staff_timetable_session_id")
        private val STAFF_TIMETABLE_SESSIONS_JSON = stringPreferencesKey("staff_timetable_sessions_json")
        
        // General Timetable Keys
        private val PARENT_ID = stringPreferencesKey("parent_id")
        private val CAMPUS_ID = stringPreferencesKey("campus_id")
        private val STAFF_ID = stringPreferencesKey("staff_id")
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        private val CACHE_DURATION = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
    }
    
    private val gson = Gson()
    
    // ==================== STUDENT TIMETABLE OPERATIONS ====================
    
    /**
     * Save current child model
     */
    suspend fun saveCurrentChildModel(child: ChildModel?) {
        context.timetableDataStore.edit { preferences ->
            if (child != null) {
                preferences[CURRENT_CHILD_MODEL_JSON] = gson.toJson(child)
            } else {
                preferences.remove(CURRENT_CHILD_MODEL_JSON)
            }
        }
    }
    
    /**
     * Get current child model
     */
    val currentChildModel: Flow<ChildModel?> = context.timetableDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CURRENT_CHILD_MODEL_JSON]?.let { json ->
                try {
                    gson.fromJson(json, ChildModel::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }
    
    /**
     * Save student list
     */
    suspend fun saveStudentList(students: List<ChildModel>?) {
        context.timetableDataStore.edit { preferences ->
            if (students != null) {
                preferences[STUDENT_LIST_JSON] = gson.toJson(students)
            } else {
                preferences.remove(STUDENT_LIST_JSON)
            }
        }
    }
    
    /**
     * Get student list
     */
    val studentList: Flow<List<ChildModel>> = context.timetableDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[STUDENT_LIST_JSON]?.let { json ->
                try {
                    val type = object : TypeToken<List<ChildModel>>() {}.type
                    gson.fromJson(json, type)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }
    
    /**
     * Save student timetable with caching
     */
    suspend fun saveStudentTimetable(
        studentId: String,
        sessionId: String,
        timetableResponse: StudentTimetableResponse
    ) {
        val cacheKey = "student_timetable_${studentId}_${sessionId}"
        context.timetableDataStore.edit { preferences ->
            preferences[stringPreferencesKey(cacheKey)] = gson.toJson(timetableResponse)
            preferences[longPreferencesKey("${cacheKey}_time")] = System.currentTimeMillis()
            preferences[STUDENT_TIMETABLE_SESSION_ID] = sessionId
        }
    }
    
    /**
     * Get student timetable from cache
     */
    suspend fun getStudentTimetable(studentId: String, sessionId: String): StudentTimetableResponse? {
        val cacheKey = "student_timetable_${studentId}_${sessionId}"
        val timeKey = "${cacheKey}_time"
        
        return context.timetableDataStore.data.map { preferences ->
            val json = preferences[stringPreferencesKey(cacheKey)]
            val cacheTime = preferences[longPreferencesKey(timeKey)] ?: 0L
            
            if (json != null && (System.currentTimeMillis() - cacheTime) < CACHE_DURATION) {
                try {
                    gson.fromJson(json, StudentTimetableResponse::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }.first()
    }
    
    /**
     * Check if student timetable is cached and valid
     */
    suspend fun isStudentTimetableCached(studentId: String, sessionId: String): Boolean {
        val cacheKey = "student_timetable_${studentId}_${sessionId}"
        val timeKey = "${cacheKey}_time"
        
        return context.timetableDataStore.data.map { preferences ->
            val cacheTime = preferences[longPreferencesKey(timeKey)] ?: 0L
            (System.currentTimeMillis() - cacheTime) < CACHE_DURATION
        }.first()
    }
    
    /**
     * Save student timetable sessions
     */
    suspend fun saveStudentTimetableSessions(sessions: List<Any>?) {
        context.timetableDataStore.edit { preferences ->
            if (sessions != null) {
                preferences[STUDENT_TIMETABLE_SESSIONS_JSON] = gson.toJson(sessions)
            } else {
                preferences.remove(STUDENT_TIMETABLE_SESSIONS_JSON)
            }
        }
    }
    
    /**
     * Get student timetable sessions
     */
    val studentTimetableSessions: Flow<List<Any>> = context.timetableDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[STUDENT_TIMETABLE_SESSIONS_JSON]?.let { json ->
                try {
                    val type = object : TypeToken<List<Any>>() {}.type
                    gson.fromJson(json, type)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }
    
    // ==================== STAFF TIMETABLE OPERATIONS ====================
    
    /**
     * Save staff timetable with caching
     */
    suspend fun saveStaffTimetable(
        staffId: String,
        sessionId: String,
        timetableModel: TimetableModel
    ) {
        val cacheKey = "staff_timetable_${staffId}_${sessionId}"
        context.timetableDataStore.edit { preferences ->
            preferences[stringPreferencesKey(cacheKey)] = gson.toJson(timetableModel)
            preferences[longPreferencesKey("${cacheKey}_time")] = System.currentTimeMillis()
            preferences[STAFF_TIMETABLE_SESSION_ID] = sessionId
        }
    }
    
    /**
     * Get staff timetable from cache
     */
    suspend fun getStaffTimetable(staffId: String, sessionId: String): TimetableModel? {
        val cacheKey = "staff_timetable_${staffId}_${sessionId}"
        val timeKey = "${cacheKey}_time"
        
        return context.timetableDataStore.data.map { preferences ->
            val json = preferences[stringPreferencesKey(cacheKey)]
            val cacheTime = preferences[longPreferencesKey(timeKey)] ?: 0L
            
            if (json != null && (System.currentTimeMillis() - cacheTime) < CACHE_DURATION) {
                try {
                    gson.fromJson(json, TimetableModel::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }.first()
    }
    
    /**
     * Check if staff timetable is cached and valid
     */
    suspend fun isStaffTimetableCached(staffId: String, sessionId: String): Boolean {
        val cacheKey = "staff_timetable_${staffId}_${sessionId}"
        val timeKey = "${cacheKey}_time"
        
        return context.timetableDataStore.data.map { preferences ->
            val cacheTime = preferences[longPreferencesKey(timeKey)] ?: 0L
            (System.currentTimeMillis() - cacheTime) < CACHE_DURATION
        }.first()
    }
    
    /**
     * Save staff timetable sessions
     */
    suspend fun saveStaffTimetableSessions(sessions: List<Any>?) {
        context.timetableDataStore.edit { preferences ->
            if (sessions != null) {
                preferences[STAFF_TIMETABLE_SESSIONS_JSON] = gson.toJson(sessions)
            } else {
                preferences.remove(STAFF_TIMETABLE_SESSIONS_JSON)
            }
        }
    }
    
    /**
     * Get staff timetable sessions
     */
    val staffTimetableSessions: Flow<List<Any>> = context.timetableDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[STAFF_TIMETABLE_SESSIONS_JSON]?.let { json ->
                try {
                    val type = object : TypeToken<List<Any>>() {}.type
                    gson.fromJson(json, type)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }
    
    // ==================== GENERAL TIMETABLE OPERATIONS ====================
    
    /**
     * Save parent ID
     */
    suspend fun saveParentId(parentId: String) {
        context.timetableDataStore.edit { preferences ->
            preferences[PARENT_ID] = parentId
        }
    }
    
    /**
     * Get parent ID
     */
    val parentId: Flow<String> = context.timetableDataStore.data
        .map { preferences ->
            preferences[PARENT_ID] ?: ""
        }
    
    /**
     * Save campus ID
     */
    suspend fun saveCampusId(campusId: String) {
        context.timetableDataStore.edit { preferences ->
            preferences[CAMPUS_ID] = campusId
        }
    }
    
    /**
     * Get campus ID
     */
    val campusId: Flow<String> = context.timetableDataStore.data
        .map { preferences ->
            preferences[CAMPUS_ID] ?: ""
        }
    
    /**
     * Save staff ID
     */
    suspend fun saveStaffId(staffId: String) {
        context.timetableDataStore.edit { preferences ->
            preferences[STAFF_ID] = staffId
        }
    }
    
    /**
     * Get staff ID
     */
    val staffId: Flow<String> = context.timetableDataStore.data
        .map { preferences ->
            preferences[STAFF_ID] ?: ""
        }
    
    /**
     * Save last sync time
     */
    suspend fun saveLastSyncTime(timestamp: Long) {
        context.timetableDataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = timestamp
        }
    }
    
    /**
     * Get last sync time
     */
    val lastSyncTime: Flow<Long> = context.timetableDataStore.data
        .map { preferences ->
            preferences[LAST_SYNC_TIME] ?: 0L
        }
    
    // ==================== CACHE MANAGEMENT ====================
    
    /**
     * Clear expired cache entries
     */
    suspend fun clearExpiredCache() {
        val currentTime = System.currentTimeMillis()
        context.timetableDataStore.edit { preferences ->
            val keysToRemove = mutableListOf<Preferences.Key<*>>()
            
            preferences.asMap().forEach { (key, value) ->
                if (key.name.endsWith("_time")) {
                    val cacheTime = value as? Long ?: 0L
                    if (currentTime - cacheTime > CACHE_DURATION) {
                        // Remove both the cache data and its timestamp
                        val dataKeyName = key.name.removeSuffix("_time")
                        keysToRemove.add(key)
                        keysToRemove.add(stringPreferencesKey(dataKeyName))
                    }
                }
            }
            
            keysToRemove.forEach { key ->
                preferences.remove(key)
            }
        }
    }
    
    /**
     * Clear all timetable cache
     */
    suspend fun clearAllTimetableCache() {
        context.timetableDataStore.edit { preferences ->
            val keysToRemove = mutableListOf<Preferences.Key<*>>()
            
            preferences.asMap().forEach { (key, _) ->
                if (key.name.contains("timetable") || key.name.endsWith("_time")) {
                    keysToRemove.add(key)
                }
            }
            
            keysToRemove.forEach { key ->
                preferences.remove(key)
            }
        }
    }
    
    /**
     * Clear all data
     */
    suspend fun clearAllData() {
        context.timetableDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // ==================== CONVENIENCE METHODS ====================
    
    /**
     * Auto-select child if only one child exists
     */
    suspend fun autoSelectChildIfSingle(): ChildModel? {
        val students = studentList.first()
        return if (students.size == 1) {
            val child = students[0]
            saveCurrentChildModel(child)
            child
        } else {
            null
        }
    }
    
    /**
     * Get cached timetable for current child
     */
    suspend fun getCurrentChildTimetable(sessionId: String): StudentTimetableResponse? {
        val child = currentChildModel.first()
        return child?.students?.firstOrNull()?.let { student ->
            getStudentTimetable(student.uniqueId, sessionId)
        }
    }
    
    /**
     * Check if current child timetable is cached
     */
    suspend fun isCurrentChildTimetableCached(sessionId: String): Boolean {
        val child = currentChildModel.first()
        return child?.students?.firstOrNull()?.let { student ->
            isStudentTimetableCached(student.uniqueId, sessionId)
        } ?: false
    }
} 