package topgrade.parent.com.parentseeks.Parent.Utils

/**
 * Enum for user types to provide type safety and avoid hardcoded strings
 */
enum class UserType(val value: String, val displayName: String) {
    PARENT("PARENT", "Parent Member"),
    STUDENT("STUDENT", "Student Member"),
    TEACHER("Teacher", "Staff Member"),  // Note: Backend uses "Teacher" for staff
    STAFF("STAFF", "Staff Member");

    companion object {
        /**
         * Get UserType from string value, returns null if not found
         */
        fun fromString(value: String?): UserType? {
            return values().firstOrNull { 
                it.value.equals(value, ignoreCase = true) || 
                it.name.equals(value, ignoreCase = true) 
            }
        }

        /**
         * Check if a string is a valid user type
         */
        fun isValid(value: String?): Boolean {
            return fromString(value) != null
        }
    }

    /**
     * Get the theme identifier for this user type
     */
    fun getThemeIdentifier(): String {
        return when (this) {
            PARENT -> ThemeHelper.THEME_PARENT
            STUDENT -> ThemeHelper.THEME_STUDENT
            TEACHER, STAFF -> ThemeHelper.THEME_STAFF
        }
    }

    /**
     * Get the primary color resource for this user type
     */
    fun getPrimaryColorRes(): Int {
        return when (this) {
            PARENT -> topgrade.parent.com.parentseeks.R.color.parent_primary
            STUDENT -> topgrade.parent.com.parentseeks.R.color.student_primary
            TEACHER, STAFF -> topgrade.parent.com.parentseeks.R.color.staff_primary
        }
    }

    /**
     * Get the logout API endpoint for this user type
     */
    fun getLogoutApiEndpoint(): String {
        return when (this) {
            PARENT -> "logout_parent"
            STUDENT -> "logout_student"
            TEACHER, STAFF -> "logout_teacher"
        }
    }
}

