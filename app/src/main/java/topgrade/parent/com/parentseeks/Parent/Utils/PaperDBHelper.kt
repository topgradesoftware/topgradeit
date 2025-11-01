package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import io.paperdb.Paper
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent

object PaperDBHelper {
    
    /**
     * Safely read data from PaperDB with error handling
     */
    inline fun <reified T> safeRead(key: String, defaultValue: T? = null): T? {
        return try {
            Paper.book().read(key, defaultValue)
        } catch (e: Exception) {
            Log.e("PaperDBHelper", "Error reading $key from PaperDB: ${e.message}")
            // Clear corrupted data
            safeDelete(key)
            defaultValue
        }
    }
    
    /**
     * Safely write data to PaperDB
     */
    fun <T : Any> safeWrite(key: String, value: T): Boolean {
        return try {
            Paper.book().write(key, value)
            true
        } catch (e: Exception) {
            Log.e("PaperDBHelper", "Error writing $key to PaperDB: ${e.message}")
            false
        }
    }
    
    /**
     * Safely delete data from PaperDB
     */
    fun safeDelete(key: String): Boolean {
        return try {
            Paper.book().delete(key)
            Log.d("PaperDBHelper", "Successfully deleted $key from PaperDB")
            true
        } catch (e: Exception) {
            Log.e("PaperDBHelper", "Error deleting $key from PaperDB: ${e.message}")
            false
        }
    }
    
    /**
     * Clear all corrupted data and reset to defaults
     */
    @JvmStatic
    fun clearCorruptedData() {
        try {
            val keysToClear = listOf(
                "students",
                "current_child_model",
                "parent_id",
                "campus_id"
            )
            
            keysToClear.forEach { key ->
                safeDelete(key)
            }
            
            Log.d("PaperDBHelper", "Cleared all potentially corrupted PaperDB data")
        } catch (e: Exception) {
            Log.e("PaperDBHelper", "Error clearing corrupted data: ${e.message}")
        }
    }
    
    /**
     * Initialize PaperDB safely
     */
    @JvmStatic
    fun init(context: Context) {
        try {
            Paper.init(context)
            Log.d("PaperDBHelper", "PaperDB initialized successfully")
        } catch (e: Exception) {
            Log.e("PaperDBHelper", "Error initializing PaperDB: ${e.message}")
        }
    }
    
    // Java-compatible wrapper methods
    @JvmStatic
    fun safeReadStudent(key: String): SharedStudent? {
        return safeRead(key, null)
    }
    
    @JvmStatic
    fun safeReadString(key: String): String? {
        return safeRead(key, null)
    }
    
    @JvmStatic
    fun safeReadStudentList(key: String): List<SharedStudent>? {
        return safeRead<List<SharedStudent>>(key, ArrayList())
    }
}
