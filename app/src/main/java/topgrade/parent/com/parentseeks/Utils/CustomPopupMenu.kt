package topgrade.parent.com.parentseeks.Utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import topgrade.parent.com.parentseeks.R
import topgrade.parent.com.parentseeks.Parent.Utils.MenuActionConstants

class CustomPopupMenu(
    private val context: Context,
    private val anchorView: View,
    private val userType: String = "student" // Default to student
) {
    
    companion object {
        // Menu action constants using consolidated constants
        const val MENU_SHARE = "Share Application"
        const val MENU_RATE = "Rate"
        const val MENU_CHANGE_PASSWORD = "Change Login Password"
        const val MENU_LOGOUT = "Logout"
    }
    
    private var popupWindow: PopupWindow? = null
    private var popupView: View? = null
    private var onMenuItemClickListener: ((String) -> Boolean)? = null
    
    init {
        Log.d("CustomPopupMenu", "Initializing CustomPopupMenu for userType: $userType")
        try {
            initializePopup()
            Log.d("CustomPopupMenu", "CustomPopupMenu initialized successfully")
        } catch (e: Exception) {
            Log.e("CustomPopupMenu", "Error during initialization", e)
        }
    }
    
    private fun initializePopup() {
        try {
            val inflater = LayoutInflater.from(context)
            popupView = inflater.inflate(R.layout.custom_popup_menu, null)
            
            if (popupView == null) {
                Log.e("CustomPopupMenu", "Failed to inflate popup layout")
                return
            }
            
            popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                isFocusable = true
                isOutsideTouchable = true
                elevation = 8f
                try {
                    animationStyle = R.style.PopupAnimation
                } catch (e: Exception) {
                    Log.w("CustomPopupMenu", "Could not set animation style, using default", e)
                    // Use default animation if custom style fails
                }
            }
            
            if (popupWindow == null) {
                Log.e("CustomPopupMenu", "Failed to create PopupWindow")
                return
            }
            
            setupClickListeners()
            applyUserTypeStyling()
            
            Log.d("CustomPopupMenu", "Popup initialized successfully")
        } catch (e: Exception) {
            Log.e("CustomPopupMenu", "Error initializing popup", e)
        }
    }
    
    private fun setupClickListeners() {
        popupView?.let { view ->
            // Share Application
            view.findViewById<View>(R.id.menu_share)?.setOnClickListener {
                onMenuItemClickListener?.invoke(MENU_SHARE) ?: false
                dismiss()
            }
            
            // Rate
            view.findViewById<View>(R.id.menu_rate)?.setOnClickListener {
                onMenuItemClickListener?.invoke(MENU_RATE) ?: false
                dismiss()
            }
            
            // Change Login Password
            view.findViewById<View>(R.id.menu_change_password)?.setOnClickListener {
                onMenuItemClickListener?.invoke(MENU_CHANGE_PASSWORD) ?: false
                dismiss()
            }
            
            // Logout
            view.findViewById<View>(R.id.menu_logout)?.setOnClickListener {
                onMenuItemClickListener?.invoke(MENU_LOGOUT) ?: false
                dismiss()
            }
        }
    }
    
    fun setOnMenuItemClickListener(listener: (String) -> Boolean) {
        onMenuItemClickListener = listener
    }
    
    fun show() {
        try {
            // Ensure we're on the main thread
            if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
                Log.w("CustomPopupMenu", "Attempting to show popup from background thread, posting to main thread")
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    show()
                }
                return
            }
            
            // Check if context is still valid
            if (context is android.app.Activity && context.isFinishing) {
                Log.w("CustomPopupMenu", "Context activity is finishing, cannot show popup")
                return
            }
            
            // Check if popup is properly initialized
            if (popupWindow == null || popupView == null) {
                Log.e("CustomPopupMenu", "Popup not initialized, attempting to reinitialize")
                initializePopup()
                if (popupWindow == null || popupView == null) {
                    Log.e("CustomPopupMenu", "Failed to initialize popup, cannot show")
                    return
                }
            }
            
            popupWindow?.let { popup ->
                if (!popup.isShowing) {
                    // Check if anchor view is still valid
                    if (!isAnchorViewValid()) {
                        Log.w("CustomPopupMenu", "Anchor view is no longer valid, cannot show popup")
                        return
                    }
                    
                    // Calculate optimal position
                    val position = calculateOptimalPosition()
                    
                    // Show with animation
                    popup.showAtLocation(
                        anchorView,
                        Gravity.NO_GRAVITY,
                        position.x,
                        position.y
                    )
                    
                    // Add entrance animation
                    animateEntrance()
                    
                    Log.d("CustomPopupMenu", "Popup shown successfully at position: $position")
                }
            }
        } catch (e: Exception) {
            Log.e("CustomPopupMenu", "Error showing popup", e)
            // Fallback: try to show at a safe default position
            showAtDefaultPosition()
        }
    }
    
    private fun isAnchorViewValid(): Boolean {
        return try {
            anchorView.windowToken != null && 
            anchorView.isAttachedToWindow && 
            anchorView.visibility == View.VISIBLE
        } catch (e: Exception) {
            Log.w("CustomPopupMenu", "Error checking anchor view validity", e)
            false
        }
    }
    
    private fun showAtDefaultPosition() {
        try {
            popupWindow?.let { popup ->
                if (!popup.isShowing) {
                    // Show at top-right corner of screen as fallback
                    val displayMetrics = context.resources.displayMetrics
                    // val screenWidth = displayMetrics.widthPixels // Unused
                    
                    popup.showAtLocation(
                        anchorView,
                        Gravity.TOP or Gravity.END,
                        16, // 16dp margin from right
                        100  // 100dp from top
                    )
                    
                    // Add entrance animation
                    animateEntrance()
                }
            }
        } catch (e: Exception) {
            Log.e("CustomPopupMenu", "Error showing popup at default position", e)
        }
    }
    
    private fun calculateOptimalPosition(): Point {
        return try {
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            
            // Get anchor view location
            val anchorLocation = IntArray(2)
            anchorView.getLocationInWindow(anchorLocation)
            
            // Measure popup size
            popupView?.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            
            val popupWidth = popupView?.measuredWidth ?: 0
            val popupHeight = popupView?.measuredHeight ?: 0
            
            // Calculate position
            var x = anchorLocation[0] + anchorView.width - popupWidth
            var y = anchorLocation[1] + anchorView.height
            
            // Ensure popup doesn't go off-screen
            if (x < 0) x = 8 // Minimum margin from left
            if (x + popupWidth > screenWidth) x = screenWidth - popupWidth - 8 // Minimum margin from right
            if (y + popupHeight > screenHeight) {
                // Show above anchor if not enough space below
                y = anchorLocation[1] - popupHeight
            }
            
            // Ensure y is not negative
            if (y < 0) y = 100 // Default to 100dp from top
            
            Point(x, y)
        } catch (e: Exception) {
            Log.e("CustomPopupMenu", "Error calculating position, using default", e)
            // Return default position (top-right)
            Point(16, 100)
        }
    }
    
    private fun animateEntrance() {
        popupView?.let { view ->
            view.alpha = 0f
            view.scaleX = 0.8f
            view.scaleY = 0.8f
            
            view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setListener(null)
        }
    }
    
    private fun animateExit(onComplete: () -> Unit) {
        popupView?.let { view ->
            view.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        onComplete()
                    }
                })
        } ?: onComplete()
    }
    
    fun dismiss() {
        popupWindow?.let { popup ->
            if (popup.isShowing) {
                animateExit {
                    popup.dismiss()
                }
            }
        }
    }
    
    fun isShowing(): Boolean {
        return popupWindow?.isShowing == true
    }
    
    private fun applyUserTypeStyling() {
        popupView?.let { view ->
            val iconColor = when (userType.lowercase()) {
                "parent" -> ContextCompat.getColor(context, R.color.dark_brown)
                "staff", "teacher" -> ContextCompat.getColor(context, R.color.navy_blue)
                else -> ContextCompat.getColor(context, R.color.student_primary)
            }
            
            // Apply color to all icons by finding them within their parent LinearLayouts
            view.findViewById<LinearLayout>(R.id.menu_share)?.let { layout ->
                layout.getChildAt(0)?.let { if (it is ImageView) it.setColorFilter(iconColor) }
            }
            view.findViewById<LinearLayout>(R.id.menu_rate)?.let { layout ->
                layout.getChildAt(0)?.let { if (it is ImageView) it.setColorFilter(iconColor) }
            }
            view.findViewById<LinearLayout>(R.id.menu_change_password)?.let { layout ->
                layout.getChildAt(0)?.let { if (it is ImageView) it.setColorFilter(iconColor) }
            }
            
            // Logout icon should remain red
            // view.findViewById<LinearLayout>(R.id.menu_logout)?.let { layout ->
            //     layout.getChildAt(0)?.let { if (it is ImageView) it.setColorFilter(ContextCompat.getColor(context, R.color.red)) }
            // }
        }
    }
} 