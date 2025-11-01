package topgrade.parent.com.parentseeks.Parent.Utils

/**
 * Consolidated Menu Action Constants for the Topgrade Software App
 * This file contains all menu action constants, labels, and related strings
 * to eliminate duplication across the codebase.
 */
object MenuActionConstants {
    
    // ==================== MENU ACTION LABELS ====================
    object Labels {
        // Share Actions
        const val SHARE_APP = "Share App"
        const val SHARE_APPLICATION = "Share Application"
        
        // Rate Actions
        const val RATE = "Rate"
        const val RATE_US = "Rate Us"
        
        // Password Actions
        const val CHANGE_PASSWORD = "Change Password"
        const val CHANGE_LOGIN_PASSWORD = "Change Login Password"
        
        // Logout Actions
        const val LOGOUT = "Logout"
        const val SIGN_OUT = "Sign Out"
    }
    
    // ==================== MENU ACTION KEYS ====================
    object Actions {
        // Share Actions
        const val ACTION_SHARE_APP = "Share App"
        const val MENU_SHARE = "Share Application"
        const val MENU_SHARE_APP = "Share App"
        
        // Rate Actions
        const val ACTION_RATE_US = "Rate Us"
        const val MENU_RATE = "Rate"
        const val MENU_RATE_US = "Rate Us"
        
        // Password Actions
        const val ACTION_CHANGE_PASSWORD = "Change Password"
        const val MENU_CHANGE_PASSWORD = "Change Login Password"
        
        // Logout Actions
        const val ACTION_LOGOUT = "Logout"
        const val MENU_LOGOUT = "Logout"
    }
    
    // ==================== MENU DESCRIPTIONS ====================
    object Descriptions {
        const val SHARE_WITH_FRIENDS = "Share with friends"
        const val RATE_OUR_APP = "Rate our app"
        const val UPDATE_PASSWORD = "Update password"
        const val SIGN_OUT_DESC = "Sign Out"
    }
    
    // ==================== ERROR MESSAGES ====================
    object ErrorMessages {
        const val ERROR_SHARE_APP = "Unable to share app"
        const val ERROR_RATE_APP = "Unable to open rating page"
        const val ERROR_PASSWORD_CHANGE = "Unable to open password change"
        const val ERROR_LOGOUT = "Error during logout"
    }
    
    // ==================== SUCCESS MESSAGES ====================
    object SuccessMessages {
        const val SUCCESS_LOGOUT = "Logged out successfully"
    }
    
    // ==================== DIALOG MESSAGES ====================
    object DialogMessages {
        const val LOGOUT_TITLE = "Logout"
        const val LOGOUT_MESSAGE = "Are you sure you want to logout?"
        const val LOGOUT_POSITIVE = "Yes"
        const val LOGOUT_NEGATIVE = "No"
    }
    
    // ==================== API ENDPOINTS ====================
    object ApiEndpoints {
        const val API_LOGOUT = "logout"
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get all available menu actions for a specific user type
     */
    fun getMenuActionsForUserType(userType: String): List<String> {
        return when (userType.lowercase()) {
            "parent" -> listOf(
                Actions.ACTION_SHARE_APP,
                Actions.ACTION_RATE_US,
                Actions.ACTION_CHANGE_PASSWORD,
                Actions.ACTION_LOGOUT
            )
            "teacher", "staff" -> listOf(
                Actions.ACTION_SHARE_APP,
                Actions.ACTION_RATE_US,
                Actions.ACTION_CHANGE_PASSWORD,
                Actions.ACTION_LOGOUT
            )
            "student" -> listOf(
                Actions.ACTION_SHARE_APP,
                Actions.ACTION_RATE_US,
                Actions.ACTION_CHANGE_PASSWORD,
                Actions.ACTION_LOGOUT
            )
            else -> listOf(
                Actions.ACTION_SHARE_APP,
                Actions.ACTION_RATE_US,
                Actions.ACTION_LOGOUT
            )
        }
    }
    
    /**
     * Get menu action label by action key
     */
    fun getLabelForAction(action: String): String {
        return when (action) {
            Actions.ACTION_SHARE_APP, Actions.MENU_SHARE_APP -> Labels.SHARE_APP
            Actions.MENU_SHARE -> Labels.SHARE_APPLICATION
            Actions.ACTION_RATE_US, Actions.MENU_RATE_US -> Labels.RATE_US
            Actions.MENU_RATE -> Labels.RATE
            Actions.ACTION_CHANGE_PASSWORD -> Labels.CHANGE_PASSWORD
            Actions.MENU_CHANGE_PASSWORD -> Labels.CHANGE_LOGIN_PASSWORD
            Actions.ACTION_LOGOUT, Actions.MENU_LOGOUT -> Labels.LOGOUT
            else -> action
        }
    }
    
    /**
     * Get description for menu action
     */
    fun getDescriptionForAction(action: String): String {
        return when (action) {
            Actions.ACTION_SHARE_APP, Actions.MENU_SHARE_APP -> Descriptions.SHARE_WITH_FRIENDS
            Actions.MENU_SHARE -> Descriptions.SHARE_WITH_FRIENDS
            Actions.ACTION_RATE_US, Actions.MENU_RATE_US -> Descriptions.RATE_OUR_APP
            Actions.MENU_RATE -> Descriptions.RATE_OUR_APP
            Actions.ACTION_CHANGE_PASSWORD -> Descriptions.UPDATE_PASSWORD
            Actions.MENU_CHANGE_PASSWORD -> Descriptions.UPDATE_PASSWORD
            Actions.ACTION_LOGOUT, Actions.MENU_LOGOUT -> Descriptions.SIGN_OUT_DESC
            else -> ""
        }
    }
    
    /**
     * Get error message for menu action
     */
    fun getErrorMessageForAction(action: String): String {
        return when (action) {
            Actions.ACTION_SHARE_APP, Actions.MENU_SHARE_APP -> ErrorMessages.ERROR_SHARE_APP
            Actions.MENU_SHARE -> ErrorMessages.ERROR_SHARE_APP
            Actions.ACTION_RATE_US, Actions.MENU_RATE_US -> ErrorMessages.ERROR_RATE_APP
            Actions.MENU_RATE -> ErrorMessages.ERROR_RATE_APP
            Actions.ACTION_CHANGE_PASSWORD -> ErrorMessages.ERROR_PASSWORD_CHANGE
            Actions.MENU_CHANGE_PASSWORD -> ErrorMessages.ERROR_PASSWORD_CHANGE
            Actions.ACTION_LOGOUT, Actions.MENU_LOGOUT -> ErrorMessages.ERROR_LOGOUT
            else -> "Unknown action"
        }
    }
    
    /**
     * Check if action is a share action
     */
    fun isShareAction(action: String): Boolean {
        return action in listOf(Actions.ACTION_SHARE_APP, Actions.MENU_SHARE, Actions.MENU_SHARE_APP)
    }
    
    /**
     * Check if action is a rate action
     */
    fun isRateAction(action: String): Boolean {
        return action in listOf(Actions.ACTION_RATE_US, Actions.MENU_RATE, Actions.MENU_RATE_US)
    }
    
    /**
     * Check if action is a password action
     */
    fun isPasswordAction(action: String): Boolean {
        return action in listOf(Actions.ACTION_CHANGE_PASSWORD, Actions.MENU_CHANGE_PASSWORD)
    }
    
    /**
     * Check if action is a logout action
     */
    fun isLogoutAction(action: String): Boolean {
        return action in listOf(Actions.ACTION_LOGOUT, Actions.MENU_LOGOUT)
    }
    
    /**
     * Get standardized action key for any menu action variant
     */
    fun getStandardizedActionKey(action: String): String {
        return when {
            isShareAction(action) -> Actions.ACTION_SHARE_APP
            isRateAction(action) -> Actions.ACTION_RATE_US
            isPasswordAction(action) -> Actions.ACTION_CHANGE_PASSWORD
            isLogoutAction(action) -> Actions.ACTION_LOGOUT
            else -> action
        }
    }
}
