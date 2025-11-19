@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import topgrade.parent.com.parentseeks.R

/**
 * ThemeHelper utility class for applying user-type specific themes
 * Supports Parent (Dark Brown), Student (Teal), and Staff (Navy Blue) themes
 */
object ThemeHelper {
    
    // Theme types
    const val THEME_PARENT = "PARENT"
    const val THEME_STUDENT = "STUDENT"
    const val THEME_STAFF = "STAFF"
    
    /**
     * Apply Parent theme (Dark Brown)
     */
    @JvmStatic
    fun applyParentTheme(activity: AppCompatActivity) {
        applySimpleTheme(activity, THEME_PARENT)
    }
    
    /**
     * Apply Student theme (Teal)
     */
    @JvmStatic
    fun applyStudentTheme(activity: AppCompatActivity) {
        applySimpleTheme(activity, THEME_STUDENT)
    }
    
    /**
     * Apply Staff/Teacher theme (Navy Blue)
     */
    @JvmStatic
    fun applyStaffTheme(activity: AppCompatActivity) {
        applySimpleTheme(activity, THEME_STAFF)
    }
    
    /**
     * Apply theme based on user type string
     */
    @JvmStatic
    fun applyThemeByUserType(activity: AppCompatActivity, userType: String) {
        applySimpleTheme(activity, userType)
    }
    
    /**
     * Apply system bars theme (status bar and navigation bar)
     */
    private fun applySystemBarsTheme(activity: AppCompatActivity, colorRes: Int) {
        // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
val color = ContextCompat.getColor(activity, colorRes)
            activity.window.statusBarColor = color
            activity.window.navigationBarColor = color
        
        // Force dark navigation bar icons (prevent light appearance)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.setSystemBarsAppearance(
                0, // 0 = do NOT use light icons
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }
    }
    
    
    /**
     * Simplified theme application - only applies system bars theme
     * Use this if you want to avoid view-specific theming
     */
    @JvmStatic
    fun applySimpleTheme(activity: AppCompatActivity, themeType: String) {
        when (themeType.uppercase()) {
            THEME_PARENT -> applySystemBarsTheme(activity, R.color.parent_primary)
            THEME_STUDENT -> applySystemBarsTheme(activity, R.color.student_primary)
            THEME_STAFF, "TEACHER" -> applySystemBarsTheme(activity, R.color.staff_primary)
            else -> applySystemBarsTheme(activity, R.color.parent_primary)
        }
    }
    
    
    /**
     * Get theme colors based on theme type
     */
    private fun getThemeColors(context: Context, themeType: String): Triple<Int, Int, Int> {
        return when (themeType) {
            THEME_PARENT -> Triple(
                ContextCompat.getColor(context, R.color.parent_primary),
                ContextCompat.getColor(context, R.color.parent_primary_dark),
                ContextCompat.getColor(context, R.color.parent_accent)
            )
            THEME_STUDENT -> Triple(
                ContextCompat.getColor(context, R.color.student_primary),
                ContextCompat.getColor(context, R.color.student_primary_dark),
                ContextCompat.getColor(context, R.color.student_accent)
            )
            THEME_STAFF -> Triple(
                ContextCompat.getColor(context, R.color.staff_primary),
                ContextCompat.getColor(context, R.color.staff_primary_dark),
                ContextCompat.getColor(context, R.color.staff_accent)
            )
            else -> Triple(
                ContextCompat.getColor(context, R.color.parent_primary),
                ContextCompat.getColor(context, R.color.parent_primary_dark),
                ContextCompat.getColor(context, R.color.parent_accent)
            )
        }
    }
    
    
    
    /**
     * Get primary color for a theme type
     */
    @JvmStatic
    fun getPrimaryColor(context: Context, themeType: String): Int {
        return when (themeType.uppercase()) {
            THEME_PARENT -> ContextCompat.getColor(context, R.color.parent_primary)
            THEME_STUDENT -> ContextCompat.getColor(context, R.color.student_primary)
            THEME_STAFF, "TEACHER" -> ContextCompat.getColor(context, R.color.staff_primary)
            else -> ContextCompat.getColor(context, R.color.parent_primary)
        }
    }
    
    /**
     * Get theme name for display
     */
    @JvmStatic
    fun getThemeDisplayName(themeType: String): String {
        return when (themeType.uppercase()) {
            THEME_PARENT -> "Parent Theme (Dark Brown)"
            THEME_STUDENT -> "Student Theme (Teal)"
            THEME_STAFF, "TEACHER" -> "Staff Theme (Navy Blue)"
            else -> "Unknown Theme"
        }
    }
    
    /**
     * Apply footer theming to an activity based on user type
     */
    @JvmStatic
    fun applyFooterTheme(activity: AppCompatActivity, userType: String) {
        try {
            val footerDrawable = getFooterDrawableForUserType(userType)
            applyFooterThemeToViews(activity, footerDrawable)
        } catch (e: Exception) {
            android.util.Log.e("ThemeHelper", "Error applying footer theme", e)
        }
    }
    
    /**
     * Get footer drawable resource for user type
     */
    @JvmStatic
    fun getFooterDrawableForUserType(userType: String): Int {
        return when (userType.uppercase()) {
            THEME_STUDENT -> R.drawable.footer_background_teal
            THEME_PARENT -> R.drawable.footer_background_brown
            THEME_STAFF, "TEACHER" -> R.drawable.footer_background_staff_navy
            else -> R.drawable.footer_background_brown
        }
    }
    
    /**
     * Apply footer theme to all footer containers in the activity
     */
    private fun applyFooterThemeToViews(activity: AppCompatActivity, footerDrawable: Int) {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        if (rootView != null) {
            applyFooterThemeRecursively(rootView, footerDrawable)
        }
    }
    
    /**
     * Recursively find and apply footer theme to footer containers
     */
    private fun applyFooterThemeRecursively(parent: ViewGroup, footerDrawable: Int) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            
            // Check if this is a footer container
            if (isFooterContainer(child)) {
                if (child is MaterialCardView) {
                    val colorRes = when (footerDrawable) {
                        R.drawable.footer_background_teal -> R.color.student_primary
                        R.drawable.footer_background_staff_navy -> R.color.staff_primary
                        else -> R.color.parent_primary
                    }
                    child.setCardBackgroundColor(ContextCompat.getColor(child.context, colorRes))
                    child.setBackgroundResource(android.R.color.transparent)
                } else {
                    child.setBackgroundResource(footerDrawable)
                }
                android.util.Log.d("ThemeHelper", "Applied footer theme to: ${child.javaClass.simpleName}")
            }
            
            // Recursively check child views
            if (child is ViewGroup) {
                applyFooterThemeRecursively(child, footerDrawable)
            }
        }
    }
    
    /**
     * Check if a view is a footer container
     */
    private fun isFooterContainer(view: View): Boolean {
        return try {
            val viewId = view.id
            
            // Check if viewId is valid (not NO_ID and not invalid resource ID)
            if (viewId == View.NO_ID || viewId == 0xffffffff.toInt()) {
                return false
            }
            
            // Additional check: Resource IDs should be positive and in valid range
            // System-generated IDs (like 0x2, 0x3) are typically very small
            // Valid resource IDs are usually much larger (0x7f...)
            if (viewId < 0x01000000) {
                // This is likely a system-generated ID without a resource name
                return false
            }
            
            val viewIdName = view.context.resources.getResourceEntryName(viewId)
            
            // Check by ID name patterns
            viewIdName.contains("footer", ignoreCase = true) ||
            viewIdName.contains("footer_container", ignoreCase = true) ||
            viewIdName.contains("footer_layout", ignoreCase = true)
        } catch (e: android.content.res.Resources.NotFoundException) {
            // Resource ID doesn't have a name (system-generated ID), silently ignore
            false
        } catch (e: Exception) {
            android.util.Log.e("ThemeHelper", "Error checking footer container", e)
            false
        }
    }
}
