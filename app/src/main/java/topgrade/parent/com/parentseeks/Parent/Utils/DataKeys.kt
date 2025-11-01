package topgrade.parent.com.parentseeks.Parent.Utils

/**
 * Centralized data keys for PaperDB and SharedPreferences
 * This eliminates hardcoded strings scattered throughout the codebase
 */
object DataKeys {
    
    // ==================== AUTHENTICATION KEYS ====================
    const val IS_LOGIN = "islogin"
    const val USER_TYPE = "User_Type"
    const val PASSWORD = "password"
    
    // ==================== PARENT DATA KEYS ====================
    const val PARENT_ID = "parent_id"
    const val PARENT_NAME = "full_name"
    const val PARENT_PHONE = "phone"
    const val PARENT_EMAIL = "email"
    const val PARENT_PICTURE = "picture"
    
    // ==================== STUDENT DATA KEYS ====================
    const val STUDENT_ID = "student_id"
    const val STUDENT_NAME = "student_name"
    const val STUDENT_PHONE = "student_phone"
    const val STUDENT_PICTURE = "student_picture"
    const val STUDENTS = "students"
    
    // ==================== STAFF/TEACHER DATA KEYS ====================
    const val STAFF_ID = "staff_id"
    const val STAFF_NAME = "full_name"  // Staff also uses "full_name"
    const val STAFF_PHONE = "staff_phone"
    const val STAFF_PICTURE = "staff_picture"
    const val STAFF_PASSWORD = "staff_password"
    
    // ==================== CAMPUS DATA KEYS ====================
    const val CAMPUS_ID = "campus_id"
    const val CAMPUS_NAME = "campus_name"
    const val CAMPUS_LIST = "campus_list"
    const val CURRENT_SESSION = "current_session"
    
    // ==================== EXAM DATA KEYS ====================
    const val EXAM_SESSION = "exam_session"
    const val EXAM_KEY = "exam_key"
    const val MONTH_KEY = "month_key"
    const val CP_KEY = "cp_key"
    
    // ==================== NOTIFICATION KEYS ====================
    const val PREFERENCE_EXTRA_REGISTRATION_ID = "registrationId"
    
    // ==================== APP DATA KEYS ====================
    const val APP_VERSION_NAME = "app_version_name"
    
    /**
     * Get all keys that should be cleared on logout
     */
    fun getAllUserDataKeys(): List<String> {
        return listOf(
            IS_LOGIN,
            USER_TYPE,
            PASSWORD,
            PARENT_ID,
            PARENT_NAME,
            PARENT_PHONE,
            PARENT_EMAIL,
            PARENT_PICTURE,
            STUDENT_ID,
            STUDENT_NAME,
            STUDENT_PHONE,
            STUDENT_PICTURE,
            STUDENTS,
            STAFF_ID,
            STAFF_NAME,
            STAFF_PHONE,
            STAFF_PICTURE,
            STAFF_PASSWORD,
            CAMPUS_ID,
            CAMPUS_NAME,
            CURRENT_SESSION
        )
    }
    
    /**
     * Get user ID key based on user type
     */
    fun getUserIdKey(userType: UserType): String {
        return when (userType) {
            UserType.PARENT -> PARENT_ID
            UserType.STUDENT -> STUDENT_ID
            UserType.TEACHER, UserType.STAFF -> STAFF_ID
        }
    }
    
    /**
     * Get user name key based on user type
     */
    fun getUserNameKey(userType: UserType): String {
        return when (userType) {
            UserType.PARENT -> PARENT_NAME
            UserType.STUDENT -> STUDENT_NAME
            UserType.TEACHER, UserType.STAFF -> STAFF_NAME
        }
    }
    
    /**
     * Get user phone key based on user type
     */
    fun getUserPhoneKey(userType: UserType): String {
        return when (userType) {
            UserType.PARENT -> PARENT_PHONE
            UserType.STUDENT -> STUDENT_PHONE
            UserType.TEACHER, UserType.STAFF -> STAFF_PHONE
        }
    }
    
    /**
     * Get user picture key based on user type
     */
    fun getUserPictureKey(userType: UserType): String {
        return when (userType) {
            UserType.PARENT -> PARENT_PICTURE
            UserType.STUDENT -> STUDENT_PICTURE
            UserType.TEACHER, UserType.STAFF -> STAFF_PICTURE
        }
    }
}

