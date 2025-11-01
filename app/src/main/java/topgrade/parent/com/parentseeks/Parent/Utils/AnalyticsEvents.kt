package topgrade.parent.com.parentseeks.Parent.Utils

/**
 * Analytics Event Constants
 * Centralized constants for all analytics events
 * 
 * @author Topgradeit Team
 * @version 1.0
 * @since 2025-10-15
 */
object AnalyticsEvents {
    
    // ==================== AUTHENTICATION ====================
    object Auth {
        const val LOGIN = "login"
        const val LOGOUT = "logout"
        const val SIGN_UP = "sign_up"
        const val FORGOT_PASSWORD = "forgot_password"
        const val PASSWORD_RESET = "password_reset"
        const val BIOMETRIC_LOGIN = "biometric_login"
    }
    
    // ==================== DASHBOARD ====================
    object Dashboard {
        const val VIEW_PARENT_DASHBOARD = "view_parent_dashboard"
        const val VIEW_STUDENT_DASHBOARD = "view_student_dashboard"
        const val VIEW_TEACHER_DASHBOARD = "view_teacher_dashboard"
        const val CARD_CLICK = "dashboard_card_click"
        const val MENU_OPEN = "dashboard_menu_open"
    }
    
    // ==================== ACADEMIC ====================
    object Academic {
        const val VIEW_ATTENDANCE = "view_attendance"
        const val VIEW_RESULT = "view_result"
        const val VIEW_TIMETABLE = "view_timetable"
        const val VIEW_DATESHEET = "view_datesheet"
        const val SUBMIT_ASSIGNMENT = "submit_assignment"
        const val VIEW_ASSIGNMENT = "view_assignment"
        const val ENTER_EXAM_MARKS = "enter_exam_marks"
        const val VIEW_PROGRESS_REPORT = "view_progress_report"
    }
    
    // ==================== FINANCIAL ====================
    object Financial {
        const val VIEW_FEE_CHALLAN = "view_fee_challan"
        const val DOWNLOAD_FEE_CHALLAN = "download_fee_challan"
        const val VIEW_PAYMENT_HISTORY = "view_payment_history"
        const val VIEW_SALARY = "view_salary"
        const val VIEW_SALARY_SLIP = "view_salary_slip"
        const val REQUEST_ADVANCE = "request_advance"
    }
    
    // ==================== COMMUNICATION ====================
    object Communication {
        const val VIEW_DIARY = "view_diary"
        const val SEND_DIARY = "send_diary"
        const val VIEW_ANNOUNCEMENT = "view_announcement"
        const val SEND_FEEDBACK = "send_feedback"
        const val VIEW_FEEDBACK = "view_feedback"
        const val SUBMIT_COMPLAINT = "submit_complaint"
        const val VIEW_COMPLAINT = "view_complaint"
    }
    
    // ==================== PROFILE ====================
    object Profile {
        const val VIEW_PROFILE = "view_profile"
        const val EDIT_PROFILE = "edit_profile"
        const val UPDATE_PROFILE_PICTURE = "update_profile_picture"
        const val CHANGE_PASSWORD = "change_password"
    }
    
    // ==================== NAVIGATION ====================
    object Navigation {
        const val SCREEN_VIEW = "screen_view"
        const val MENU_CLICK = "menu_click"
        const val BUTTON_CLICK = "button_click"
        const val BACK_BUTTON = "back_button"
        const val DRAWER_OPEN = "drawer_open"
    }
    
    // ==================== NOTIFICATIONS ====================
    object Notification {
        const val RECEIVED = "notification_received"
        const val OPENED = "notification_opened"
        const val DISMISSED = "notification_dismissed"
        const val PERMISSION_GRANTED = "notification_permission_granted"
        const val PERMISSION_DENIED = "notification_permission_denied"
    }
    
    // ==================== ERRORS ====================
    object Error {
        const val APP_ERROR = "app_error"
        const val API_ERROR = "api_error"
        const val NETWORK_ERROR = "network_error"
        const val AUTH_ERROR = "auth_error"
        const val DATA_ERROR = "data_error"
    }
    
    // ==================== PERFORMANCE ====================
    object Performance {
        const val SCREEN_LOAD_TIME = "screen_load_time"
        const val API_RESPONSE_TIME = "api_response_time"
        const val IMAGE_LOAD_TIME = "image_load_time"
        const val APP_LAUNCH_TIME = "app_launch_time"
    }
    
    // ==================== ENGAGEMENT ====================
    object Engagement {
        const val SHARE_CONTENT = "share_content"
        const val RATE_APP = "rate_app"
        const val SEARCH = "search"
        const val FILTER_APPLIED = "filter_applied"
        const val SORT_APPLIED = "sort_applied"
    }
    
    // ==================== SETTINGS ====================
    object Settings {
        const val VIEW_SETTINGS = "view_settings"
        const val CHANGE_THEME = "change_theme"
        const val CHANGE_LANGUAGE = "change_language"
        const val ENABLE_BIOMETRIC = "enable_biometric"
        const val DISABLE_BIOMETRIC = "disable_biometric"
    }
}

/**
 * Analytics Parameter Constants
 */
object AnalyticsParams {
    const val USER_TYPE = "user_type"
    const val USER_ID = "user_id"
    const val CAMPUS_ID = "campus_id"
    const val CAMPUS_NAME = "campus_name"
    const val STUDENT_ID = "student_id"
    const val STAFF_ID = "staff_id"
    const val SCREEN_NAME = "screen_name"
    const val SCREEN_CLASS = "screen_class"
    const val CARD_NAME = "card_name"
    const val BUTTON_NAME = "button_name"
    const val MENU_ITEM = "menu_item"
    const val ERROR_TYPE = "error_type"
    const val ERROR_MESSAGE = "error_message"
    const val SUCCESS = "success"
    const val DURATION_MS = "duration_ms"
    const val ITEM_NAME = "item_name"
    const val ITEM_CATEGORY = "item_category"
}

/**
 * User Property Constants
 */
object UserProperties {
    const val USER_TYPE = "user_type"
    const val CAMPUS_ID = "campus_id"
    const val CAMPUS_NAME = "campus_name"
    const val GRADE = "grade"
    const val CLASS_NAME = "class_name"
    const val SECTION = "section"
    const val ROLE = "role"
    const val SUBJECT = "subject"
    const val FIRST_LOGIN_DATE = "first_login_date"
    const val LAST_LOGIN_DATE = "last_login_date"
}

