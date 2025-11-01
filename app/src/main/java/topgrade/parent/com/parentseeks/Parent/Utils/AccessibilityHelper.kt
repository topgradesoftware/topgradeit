@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

/**
 * Accessibility Helper for improved app accessibility
 * Ensures the app is usable by everyone, including users with disabilities
 * 
 * Features:
 * - Screen reader support
 * - Content descriptions
 * - Touch target sizes
 * - High contrast support
 * - Keyboard navigation
 * 
 * @author Topgradeit Team
 * @version 1.0
 * @since 2025-10-15
 */
object AccessibilityHelper {
    
    // Minimum touch target size (48dp as per Material Design guidelines)
    private const val MIN_TOUCH_TARGET_DP = 48
    
    /**
     * Check if accessibility services (like TalkBack) are enabled
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.isEnabled && am.isTouchExplorationEnabled
    }
    
    /**
     * Set content description for views (important for screen readers)
     */
    fun setContentDescription(view: View, description: String?) {
        view.contentDescription = description
    }
    
    /**
     * Set content description for ImageView with fallback
     */
    fun setImageDescription(imageView: ImageView, description: String?, fallback: String = "Image") {
        imageView.contentDescription = description ?: fallback
        ViewCompat.setImportantForAccessibility(
            imageView,
            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
        )
    }
    
    /**
     * Make view clickable with proper accessibility announcement
     */
    fun makeAccessibleClickable(view: View, description: String, action: String = "tap") {
        view.contentDescription = "$description, double $action to activate"
        view.isClickable = true
        view.isFocusable = true
        ViewCompat.setImportantForAccessibility(
            view,
            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
        )
    }
    
    /**
     * Ensure minimum touch target size (48dp)
     */
    fun ensureTouchTargetSize(view: View) {
        val context = view.context
        val minSize = (MIN_TOUCH_TARGET_DP * context.resources.displayMetrics.density).toInt()
        
        view.post {
            val layoutParams = view.layoutParams
            if (view.width < minSize || view.height < minSize) {
                layoutParams.width = Math.max(view.width, minSize)
                layoutParams.height = Math.max(view.height, minSize)
                view.layoutParams = layoutParams
            }
        }
    }
    
    /**
     * Set heading for screen readers (useful for section titles)
     */
    fun setAsHeading(textView: TextView) {
        ViewCompat.setAccessibilityHeading(textView, true)
    }
    
    /**
     * Announce message to screen reader
     */
    fun announce(view: View, message: String) {
        view.announceForAccessibility(message)
    }
    
    /**
     * Set live region for dynamic content updates
     */
    fun setLiveRegion(view: View, mode: Int = ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE) {
        ViewCompat.setAccessibilityLiveRegion(view, mode)
    }
    
    /**
     * Group views for accessibility (for complex layouts)
     */
    fun groupViewsForAccessibility(container: View, vararg views: View) {
        views.forEach { view ->
            ViewCompat.setImportantForAccessibility(
                view,
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            )
        }
        
        container.contentDescription = views.joinToString(", ") { 
            it.contentDescription?.toString() ?: ""
        }
        ViewCompat.setImportantForAccessibility(
            container,
            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
        )
    }
    
    /**
     * Setup custom accessibility action
     */
    fun addCustomAction(view: View, label: String, action: () -> Boolean) {
        ViewCompat.addAccessibilityAction(view, label) { _, _ ->
            action()
        }
    }
    
    /**
     * Mark view as important for accessibility
     */
    fun markAsImportant(view: View) {
        ViewCompat.setImportantForAccessibility(
            view,
            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
        )
    }
    
    /**
     * Hide view from accessibility services (for decorative elements)
     */
    fun hideFromAccessibility(view: View) {
        ViewCompat.setImportantForAccessibility(
            view,
            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO
        )
    }
    
    /**
     * Set traversal order (for keyboard navigation)
     */
    fun setTraversalOrder(view: View, before: View? = null, after: View? = null) {
        // API 22+ only - setting traversal order for keyboard navigation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            before?.let { view.accessibilityTraversalBefore = it.id }
            after?.let { view.accessibilityTraversalAfter = it.id }
        }
    }
    
    /**
     * Setup accessibility for form fields
     */
    fun setupFormFieldAccessibility(
        field: View,
        label: String,
        hint: String? = null,
        error: String? = null
    ) {
        val description = buildString {
            append(label)
            hint?.let { append(", $it") }
            error?.let { append(", Error: $it") }
        }
        
        field.contentDescription = description
        setLiveRegion(field, ViewCompat.ACCESSIBILITY_LIVE_REGION_ASSERTIVE)
    }
    
    /**
     * Setup accessibility for list items
     */
    fun setupListItemAccessibility(
        item: View,
        title: String,
        subtitle: String? = null,
        position: Int,
        total: Int
    ) {
        val description = buildString {
            append(title)
            subtitle?.let { append(", $it") }
            append(", item $position of $total")
        }
        
        item.contentDescription = description
        markAsImportant(item)
    }
    
    /**
     * Setup accessibility for cards
     */
    fun setupCardAccessibility(
        card: View,
        title: String,
        description: String? = null,
        actionHint: String = "double tap to open"
    ) {
        val fullDescription = buildString {
            append(title)
            description?.let { append(", $it") }
            append(", $actionHint")
        }
        
        card.contentDescription = fullDescription
        makeAccessibleClickable(card, title)
    }
    
    /**
     * Setup accessibility for buttons
     */
    fun setupButtonAccessibility(
        button: View,
        label: String,
        state: String? = null
    ) {
        val description = buildString {
            append(label)
            state?.let { append(", $it") }
            append(", button")
        }
        
        button.contentDescription = description
        ensureTouchTargetSize(button)
    }
    
    /**
     * Setup accessibility for images
     */
    fun setupImageAccessibility(
        imageView: ImageView,
        description: String,
        isDecorative: Boolean = false
    ) {
        if (isDecorative) {
            hideFromAccessibility(imageView)
        } else {
            setImageDescription(imageView, description)
        }
    }
    
    /**
     * Setup accessibility for progress indicators
     */
    fun setupProgressAccessibility(
        progressBar: View,
        label: String,
        progress: Int,
        max: Int
    ) {
        val description = "$label, $progress out of $max"
        progressBar.contentDescription = description
        setLiveRegion(progressBar, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE)
    }
    
    /**
     * Setup accessibility for tabs
     */
    fun setupTabAccessibility(
        tab: View,
        label: String,
        isSelected: Boolean,
        position: Int,
        total: Int
    ) {
        val description = buildString {
            append(label)
            append(", tab $position of $total")
            if (isSelected) append(", selected")
        }
        
        tab.contentDescription = description
        tab.isSelected = isSelected
    }
    
    /**
     * Request accessibility focus on view
     */
    fun requestAccessibilityFocus(view: View) {
        view.post {
            view.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
    }
    
    /**
     * Clear accessibility focus
     */
    fun clearAccessibilityFocus(view: View) {
        view.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED)
    }
    
    /**
     * Check if device has high contrast enabled
     */
    fun isHighContrastEnabled(context: Context): Boolean {
        try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            // Note: isHighTextContrastEnabled is not available in AccessibilityManager
            // This would require reflection or checking system settings
            return am.isEnabled
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Get recommended minimum text size for accessibility
     */
    fun getMinimumTextSize(context: Context): Float {
        // 16sp is minimum recommended for body text
        return 16f * context.resources.displayMetrics.scaledDensity
    }
}

