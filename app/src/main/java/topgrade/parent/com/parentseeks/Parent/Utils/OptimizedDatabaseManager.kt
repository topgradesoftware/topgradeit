@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent
import topgrade.parent.com.parentseeks.Parent.Model.Subject
import java.util.concurrent.Executors

/**
 * Optimized Database Manager
 * Replaces Paper DB with Room database for better performance and type safety
 */
class OptimizedDatabaseManager(context: Context) {
    
    companion object {
        private const val TAG = "OptimizedDatabaseManager"
        private const val DATABASE_NAME = "topgrade_database"
        private const val DATABASE_VERSION = 1
    }
    
    private val database: AppDatabase
    private val userDao: UserDao
    private val studentDao: StudentDao
    private val subjectDao: SubjectDao
    private val sessionDao: SessionDao
    
    // Background executor for database operations
    private val databaseExecutor = Executors.newFixedThreadPool(4)
    
    init {
        database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(TAG, "Database created successfully")
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(TAG, "Database opened successfully")
            }
        })
        .fallbackToDestructiveMigration()
        .build()
        
        userDao = database.userDao()
        studentDao = database.studentDao()
        subjectDao = database.subjectDao()
        sessionDao = database.sessionDao()
    }
    
    // ==================== USER OPERATIONS ====================
    
    /**
     * Save user data with optimized batch operations
     */
    suspend fun saveUserData(
        userId: String,
        userType: String,
        campusId: String,
        fullName: String,
        email: String,
        phone: String,
        landline: String,
        address: String,
        picture: String,
        password: String
    ) = withContext(Dispatchers.IO) {
        try {
            val user = UserEntity(
                userId = userId,
                userType = userType,
                campusId = campusId,
                fullName = fullName,
                email = email,
                phone = phone,
                landline = landline,
                address = address,
                picture = picture,
                password = password,
                isLoggedIn = true,
                lastUpdated = System.currentTimeMillis()
            )
            
            userDao.insertOrUpdateUser(user)
            Log.d(TAG, "User data saved successfully: $userId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user data", e)
            throw e
        }
    }
    
    /**
     * Get current user data
     */
    suspend fun getCurrentUser(): UserEntity? = withContext(Dispatchers.IO) {
        try {
            userDao.getCurrentUser()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            null
        }
    }
    
    /**
     * Check if user is logged in
     */
    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            userDao.getCurrentUser() != null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status", e)
            false
        }
    }
    
    /**
     * Clear user data (logout)
     */
    suspend fun clearUserData() = withContext(Dispatchers.IO) {
        try {
            userDao.clearAllUsers()
            studentDao.clearAllStudents()
            subjectDao.clearAllSubjects()
            sessionDao.clearAllSessions()
            Log.d(TAG, "All user data cleared successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user data", e)
            throw e
        }
    }
    
    // ==================== STUDENT OPERATIONS ====================
    
    /**
     * Save students data with optimized batch operations
     */
    suspend fun saveStudents(students: List<SharedStudent>) = withContext(Dispatchers.IO) {
        try {
            val studentEntities = students.map { student ->
                StudentEntity(
                    studentId = student.uniqueId,
                    fullName = student.fullName,
                    className = student.className ?: "",
                    sectionName = student.sectionId ?: "",
                    parentId = student.parentId,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            
            // Batch insert for better performance
            studentDao.insertStudents(studentEntities)
            
            // Note: Subjects are not available in SharedStudent model
            // If subjects are needed, they should be handled separately
            
            Log.d(TAG, "Students data saved successfully: ${students.size} students")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving students data", e)
            throw e
        }
    }
    
    /**
     * Get all students
     */
    suspend fun getStudents(): List<StudentEntity> = withContext(Dispatchers.IO) {
        try {
            studentDao.getAllStudents()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting students", e)
            emptyList()
        }
    }
    
    /**
     * Get student with subjects
     */
    suspend fun getStudentWithSubjects(studentId: String): StudentWithSubjects? = withContext(Dispatchers.IO) {
        try {
            studentDao.getStudentWithSubjects(studentId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting student with subjects", e)
            null
        }
    }
    
    // ==================== SESSION OPERATIONS ====================
    
    /**
     * Save session data
     */
    suspend fun saveSession(sessionId: String, sessionData: String) = withContext(Dispatchers.IO) {
        try {
            val session = SessionEntity(
                sessionId = sessionId,
                sessionData = sessionData,
                lastUpdated = System.currentTimeMillis()
            )
            sessionDao.insertOrUpdateSession(session)
            Log.d(TAG, "Session data saved successfully: $sessionId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving session data", e)
            throw e
        }
    }
    
    /**
     * Get current session
     */
    suspend fun getCurrentSession(): SessionEntity? = withContext(Dispatchers.IO) {
        try {
            sessionDao.getCurrentSession()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current session", e)
            null
        }
    }
    
    // ==================== MIGRATION FROM PAPER DB ====================
    
    /**
     * Migrate data from Paper DB to Room database
     */
    suspend fun migrateFromPaperDB(): MigrationResult = withContext(Dispatchers.IO) {
        val result = MigrationResult()
        
        try {
            Log.d(TAG, "Starting migration from Paper DB to Room database...")
            
            // Migrate user data
            migrateUserData(result)
            
            // Migrate students data
            migrateStudentsData(result)
            
            // Migrate session data
            migrateSessionData(result)
            
            result.success = true
            Log.d(TAG, "Migration completed successfully: ${result.migratedItems.size} items")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during migration", e)
            result.success = false
            result.errorMessage = e.message
        }
        
        result
    }
    
    private suspend fun migrateUserData(result: MigrationResult) {
        try {
            val userId = Paper.book().read<String>("parent_id")
            val userType = Paper.book().read<String>("User_Type")
            val campusId = Paper.book().read<String>("campus_id")
            val fullName = Paper.book().read<String>("full_name")
            val email = Paper.book().read<String>("email")
            val phone = Paper.book().read<String>("phone")
            val landline = Paper.book().read<String>("landline")
            val address = Paper.book().read<String>("address")
            val picture = Paper.book().read<String>("picture")
            val password = Paper.book().read<String>("password")
            
            if (userId != null && fullName != null) {
                saveUserData(
                    userId = userId,
                    userType = userType ?: "PARENT",
                    campusId = campusId ?: "",
                    fullName = fullName,
                    email = email ?: "",
                    phone = phone ?: "",
                    landline = landline ?: "",
                    address = address ?: "",
                    picture = picture ?: "",
                    password = password ?: ""
                )
                result.migratedItems.add("User data")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating user data", e)
            result.errors.add("User data: ${e.message}")
        }
    }
    
    private suspend fun migrateStudentsData(result: MigrationResult) {
        try {
            val students = Paper.book().read<List<SharedStudent>>("students")
            if (students != null && students.isNotEmpty()) {
                saveStudents(students)
                result.migratedItems.add("Students data (${students.size} students)")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating students data", e)
            result.errors.add("Students data: ${e.message}")
        }
    }
    
    private suspend fun migrateSessionData(result: MigrationResult) {
        try {
            val sessionId = Paper.book().read<String>("current_session")
            if (sessionId != null) {
                saveSession(sessionId, "migrated_session")
                result.migratedItems.add("Session data")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating session data", e)
            result.errors.add("Session data: ${e.message}")
        }
    }
    
    /**
     * Close database connection
     */
    fun close() {
        try {
            database.close()
            databaseExecutor.shutdown()
            Log.d(TAG, "Database connection closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing database", e)
        }
    }
    
    /**
     * Migration result data class
     */
    data class MigrationResult(
        var success: Boolean = false,
        val migratedItems: MutableList<String> = mutableListOf(),
        val errors: MutableList<String> = mutableListOf(),
        var errorMessage: String? = null
    ) {
        val totalMigrated: Int get() = migratedItems.size
        val totalErrors: Int get() = errors.size
    }
}

// ==================== ROOM DATABASE ENTITIES ====================

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
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
    val lastUpdated: Long
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val studentId: String,
    val fullName: String,
    val className: String,
    val sectionName: String,
    val parentId: String,
    val lastUpdated: Long
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val subjectId: String,
    val subjectName: String,
    val studentId: String,
    val lastUpdated: Long
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val sessionData: String,
    val lastUpdated: Long
)

// ==================== ROOM RELATIONSHIPS ====================

data class StudentWithSubjects(
    @Embedded val student: StudentEntity,
    @Relation(
        parentColumn = "studentId",
        entityColumn = "studentId"
    )
    val subjects: List<SubjectEntity>
)

// ==================== ROOM DAOs ====================

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)
    
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
    
    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)
    
    @Query("SELECT * FROM students ORDER BY fullName")
    suspend fun getAllStudents(): List<StudentEntity>
    
    @Transaction
    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getStudentWithSubjects(studentId: String): StudentWithSubjects?
    
    @Query("DELETE FROM students")
    suspend fun clearAllStudents()
}

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)
    
    @Query("SELECT * FROM subjects WHERE studentId = :studentId")
    suspend fun getSubjectsForStudent(studentId: String): List<SubjectEntity>
    
    @Query("DELETE FROM subjects")
    suspend fun clearAllSubjects()
}

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: SessionEntity)
    
    @Query("SELECT * FROM sessions ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun getCurrentSession(): SessionEntity?
    
    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()
}

// ==================== ROOM DATABASE ====================

@Database(
    entities = [
        UserEntity::class,
        StudentEntity::class,
        SubjectEntity::class,
        SessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun sessionDao(): SessionDao
} 