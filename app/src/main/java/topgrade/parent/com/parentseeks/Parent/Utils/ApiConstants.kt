package topgrade.parent.com.parentseeks.Parent.Utils

import kotlin.jvm.JvmStatic

/**
 * Consolidated API Constants for the Topgrade Software App
 * This file contains all API endpoints, base URLs, and related constants
 * to eliminate duplication across the codebase.
 */
object ApiConstants {
    
    // ==================== BASE URLs ====================
    const val BASE_URL = "https://topgradesoftware.com/"
    const val CAMPUS_BASE_URL = "https://topgradeit.com/"
    const val SUPER_ADMIN_URL = "https://topgradeit.com/superadmin"
    const val PRIVACY_POLICY_URL = "https://topgradesoftware.com/privacy-policy.html"
    const val CAMPUS_LOGIN_URL = "https://topgradesoftware.com/index.php?page=campus/login_campus"
    const val SUPER_ADMIN_LOGIN_URL = "https://topgradesoftware.com/index.php?page=superadmin/login"
    const val CAMPUS_USER_LOGIN_URL = "https://topgradesoftware.com/index.php?page=campus/login_user"
    const val HELP_SUPPORT_URL = "https://topgradesoftware.com/help_support_form.php"
    
    // ==================== IMAGE BASE URLs ====================
    const val IMAGE_BASE_URL = "https://topgradesoftware.com/uploads/employee/"
    const val PARENT_IMAGE_BASE_URL = "https://topgradesoftware.com/uploads/parent/"
    const val EMPLOYEE_IMAGE_BASE_URL = "https://topgradesoftware.com/uploads/staff/"
    const val STUDENT_IMAGE_BASE_URL = "https://topgradesoftware.com/uploads/student/"
    
    // ==================== PLAY STORE URLs ====================
    const val PLAY_STORE_BASE_URL = "https://play.google.com/store/apps/details?id="
    
    // ==================== PARENT API ENDPOINTS ====================
    object Parent {
        const val LIST_CAMPUS = "api.php?page=parent/list_campus"
        const val LOGIN = "api.php?page=parent/login"
        const val LOGOUT = "api.php?page=parent/logout"
        const val LOAD_PROFILE = "api.php?page=parent/load_profile"
        const val UPDATE_PROFILE = "api.php?page=parent/update_profile"
        const val UPDATE_PROFILE_STUDENT = "api.php?page=parent/update_profile_student"
        const val LOAD_CHALLAN = "api.php?page=parent/load_challan"
        const val UPDATE_PASSWORD = "api.php?page=parent/update_password"
        const val UPDATE_PICTURE = "api.php?page=parent/update_picture"
        const val UPDATE_PICTURE_STUDENT = "api.php?page=parent/update_picture_student"
        const val LOAD_ATTENDANCE = "api.php?page=parent/load_attendance"
        const val LOAD_ATTENDANCE_SUBJECT_WISE = "api.php?page=parent/load_attendance_full"
        const val LOAD_ATTENDANCE_SUBJECTWISE = "api/parent/load_attendance_subjectwise.php"
        const val LOAD_EXAM = "api.php?page=parent/load_exam"
        const val LOAD_EXAM_SESSION = "api.php?page=parent/load_exam_session"
        const val LOAD_TIMETABLE = "api.php?page=parent/load_timetable"
        const val LOAD_TIMETABLE_SESSION = "api.php?page=parent/load_timetable_session"
        const val LOAD_DATESHEET = "api.php?page=parent/load_datesheet"
        const val LOAD_DIARY = "api.php?page=parent/load_diary"
        const val COMPLAIN = "api.php?page=parent/complain"
        const val APP_VERSION = "api.php?page=parent/app_version_2"
    }
    
    // ==================== TEACHER/STAFF API ENDPOINTS ====================
    object Teacher {
        const val LOGIN = "api.php?page=teacher/login"
        const val LOGOUT = "api.php?page=teacher/logout"
        const val UPDATE_PASSWORD = "api.php?page=teacher/update_password"
        const val UPDATE_PICTURE = "api.php?page=teacher/update_picture"
        const val LOAD_PROFILE = "api.php?page=teacher/load_profile"
        const val UPDATE_PROFILE = "api.php?page=teacher/update_profile"
        const val LOAD_ATTENDANCE = "api.php?page=teacher/load_attendance"
        const val LOAD_EXAM_SESSION = "api.php?page=teacher/load_exam_session"
        const val LOAD_ACTIVE_EXAM_SESSIONS = "api.php?page=teacher/load_active_exam_sessions"
        const val LOAD_SALARY = "api.php?page=teacher/load_salary"
        const val LOAD_ADVANCE = "api.php?page=teacher/load_advance"
        const val LOAD_LEDGER = "api.php?page=teacher/load_ledger"
        const val REPORT_PROGRESS = "api.php?page=teacher/report_progress"
        const val ASSIGN_TASK = "api.php?page=teacher/assign_task"
        const val LOAD_STUDENTS = "api.php?page=teacher/load_students"
        const val LOAD_EXAMS_RESULTS = "api.php?page=teacher/load_exams_results"
        const val SAVE_EXAM_RESULTS = "api.php?page=teacher/save_exam_results"
        const val LOAD_EXAMS = "api.php?page=teacher/load_exams"
        const val LOAD_TIMETABLE = "api.php?page=teacher/load_timetable"
        const val LOAD_TIMETABLE_SESSION = "api.php?page=teacher/load_timetable_session"
        const val LOAD_TIMETABLE_SMS = "api.php?page=teacher/load_timetable_sms"
        const val GET_STATE = "api.php?page=teacher/getstate"
        const val GET_CITY = "api.php?page=teacher/getcity"
        const val LOAD_EVENTS = "api.php?page=teacher/load_events"
        const val LOAD_NEWS = "api.php?page=teacher/load_news"
        const val ATTENDANCE_STUDENT = "api.php?page=teacher/attendence_student"
        const val ATTENDANCE_STUDENT_FULL = "api.php?page=teacher/attendence_student_full"
        const val FEEDBACK = "api.php?page=teacher/feedback"
        const val LEAVE_APPLICATION = "api.php?page=teacher/leave_applicaton"
        const val COMPLAIN = "api.php?page=teacher/complain"
        const val SEND_DIARY = "api.php?page=teacher/send_diary"
    }
    
    // ==================== STUDENT API ENDPOINTS ====================
    // Student uses the same APIs as Parent since they have identical features
    object Student {
        // Login and Authentication
        const val LOGIN = "api.php?page=parent/login"  // Same as parent
        const val LOGOUT = "api.php?page=parent/logout"  // Same as parent
        
        // Profile Management
        const val LOAD_PROFILE = "api.php?page=parent/load_profile"
        const val UPDATE_PROFILE = "api.php?page=parent/update_profile"
        const val UPDATE_PROFILE_STUDENT = "api.php?page=parent/update_profile_student"
        const val UPDATE_PASSWORD = "api.php?page=parent/update_password"
        const val UPDATE_PICTURE = "api.php?page=parent/update_picture"
        const val UPDATE_PICTURE_STUDENT = "api.php?page=parent/update_picture_student"
        
        // Academic Data
        const val LOAD_ATTENDANCE = "api.php?page=parent/load_attendance"
        const val LOAD_ATTENDANCE_SUBJECT_WISE = "api.php?page=parent/load_attendance_full"
        const val LOAD_ATTENDANCE_SUBJECTWISE = "api/parent/load_attendance_subjectwise.php"
        const val LOAD_EXAM = "api.php?page=parent/load_exam"
        const val LOAD_EXAM_SESSION = "api.php?page=parent/load_exam_session"
        const val LOAD_TIMETABLE = "api.php?page=parent/load_timetable"
        const val LOAD_TIMETABLE_SESSION = "api.php?page=parent/load_timetable_session"
        const val LOAD_DATESHEET = "api.php?page=parent/load_datesheet"
        const val LOAD_DIARY = "api.php?page=parent/load_diary"
        
        // Financial
        const val LOAD_CHALLAN = "api.php?page=parent/load_challan"
        
        // Campus and App
        const val LIST_CAMPUS = "api.php?page=parent/list_campus"
        const val APP_VERSION = "api.php?page=parent/app_version_2"
    }
    
    // ==================== CAMPUS API ENDPOINTS ====================
    object Campus {
        const val APP_VERSION = "api.php?page=campus/app_version"
    }
    
    // ==================== SHARED PREFERENCES KEYS ====================
    object SharedPrefs {
        const val CAMPUS_LIST = "campus_list"
        const val IS_LOGIN = "is_login_4"
        const val USER_TYPE = "User_Type"
        const val PREFERENCE_EXTRA_REGISTRATION_ID = "PREFERENCE_EXTRA_REGISTRATION_ID"
        const val APP_VERSION_NAME = "topgradeit_app_version"
        const val EXAM_SESSION = "exam_session"
        const val EXAM_KEY = "exam_key"
        const val MONTH_KEY = "month_key"
        const val CP_KEY = "cp_key"
    }
    
    // ==================== WHATSAPP INTEGRATION ====================
    const val WHATSAPP_BASE_URL = "https://wa.me/"
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Builds a complete API URL by combining base URL with endpoint
     */
    @JvmStatic
    fun buildApiUrl(endpoint: String): String {
        return BASE_URL + endpoint
    }
    
    /**
     * Builds a complete campus API URL by combining campus base URL with endpoint
     */
    @JvmStatic
    fun buildCampusApiUrl(endpoint: String): String {
        return CAMPUS_BASE_URL + endpoint
    }
    
    /**
     * Builds a complete image URL by combining image base URL with filename
     */
    fun buildImageUrl(baseUrl: String, filename: String): String {
        return baseUrl + filename
    }
    
    /**
     * Builds a complete Play Store URL for the app
     */
    fun buildPlayStoreUrl(packageName: String): String {
        return PLAY_STORE_BASE_URL + packageName
    }
    
    /**
     * Builds a WhatsApp URL with phone number and message
     */
    fun buildWhatsAppUrl(phoneNumber: String, message: String): String {
        return "$WHATSAPP_BASE_URL$phoneNumber?text=${android.net.Uri.encode(message)}"
    }
}
