package topgrade.parent.com.parentseeks.Teacher.Utils

import topgrade.parent.com.parentseeks.Parent.Utils.MenuActionConstants

/**
 * Constants for Staff Dashboard
 * Replaces magic strings with proper constants
 * Now uses consolidated MenuActionConstants for menu actions
 */
object DashboardConstants {
    
    // Menu item titles
    const val MENU_ATTENDANCE = "Attendance"
    const val MENU_ACADEMICS = "Academics"
    const val MENU_APPLICATIONS = "Applications"
    const val MENU_COMPLAINTS = "Complaints"
    const val MENU_FEEDBACK = "Feedback"
    const val MENU_ASSIGN_TASK = "Assign Task"
    const val MENU_SALARY = "Salary"
    const val MENU_PROFILE = "Profile"
    
    // Drawer menu items
    const val DRAWER_HOME = "Home"
    const val DRAWER_PROFILE = "Profile"
    const val DRAWER_SETTINGS = "Settings"
    
    // Action items (using consolidated MenuActionConstants)
    val ACTION_SHARE_APP = MenuActionConstants.Actions.ACTION_SHARE_APP
    val ACTION_RATE_US = MenuActionConstants.Actions.ACTION_RATE_US
    val ACTION_CHANGE_PASSWORD = MenuActionConstants.Actions.ACTION_CHANGE_PASSWORD
    val ACTION_LOGOUT = MenuActionConstants.Actions.ACTION_LOGOUT
    
    // Navigation destinations
    const val NAV_ATTENDANCE_MENU = "attendance_menu"
    const val NAV_ACADEMICS_DASHBOARD = "academics_dashboard"
    const val NAV_APPLICATIONS = "applications"
    const val NAV_COMPLAINTS = "complaints"
    const val NAV_FEEDBACK = "feedback"
    const val NAV_ASSIGN_TASK = "assign_task"
    const val NAV_SALARY = "salary"
    const val NAV_PROFILE = "profile"
    
    // Error messages
    const val ERROR_NETWORK_LOST = "Network connection lost"
    const val ERROR_NETWORK_UNAVAILABLE = "No network connection available"
    const val ERROR_NETWORK_GENERAL = "Network error occurred"
    // Menu action error messages (using consolidated MenuActionConstants)
    val ERROR_SHARE_APP = MenuActionConstants.ErrorMessages.ERROR_SHARE_APP
    val ERROR_RATE_APP = MenuActionConstants.ErrorMessages.ERROR_RATE_APP
    val ERROR_PASSWORD_CHANGE = MenuActionConstants.ErrorMessages.ERROR_PASSWORD_CHANGE
    val ERROR_LOGOUT = MenuActionConstants.ErrorMessages.ERROR_LOGOUT
    
    // Success messages
    val SUCCESS_LOGOUT = MenuActionConstants.SuccessMessages.SUCCESS_LOGOUT
    
    // Default values
    const val DEFAULT_PROFILE_IMAGE = "default_profile"
    const val DEFAULT_USER_NAME = "User"
    const val DEFAULT_LOCATION = "Location"
    
    // API endpoints
    val API_LOGOUT = MenuActionConstants.ApiEndpoints.API_LOGOUT
    const val API_PROFILE_IMAGE_BASE = "profile_image_base_url"
    
    // PaperDB keys
    const val KEY_STAFF_ID = "staff_id"
    const val KEY_CAMPUS_ID = "campus_id"
    const val KEY_FULL_NAME = "full_name"
    const val KEY_PROFILE_IMAGE = "profile_image"
    const val KEY_IS_LOGIN = "is_login"
    
    // UI constants
    const val GRID_SPAN_COUNT = 2
    const val ANIMATION_DURATION = 300L
    const val SNACKBAR_DURATION = 3000L
    
    // Performance constants
    const val OPTIMAL_THREAD_MULTIPLIER = 0.75
    const val MIN_THREAD_COUNT = 1
}
