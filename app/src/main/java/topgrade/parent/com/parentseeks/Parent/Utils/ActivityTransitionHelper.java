package topgrade.parent.com.parentseeks.Parent.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import androidx.core.content.ContextCompat;

/**
 * Utility class to handle smooth activity transitions and prevent flickering
 */
public class ActivityTransitionHelper {
    
    /**
     * Apply anti-flickering window flags to an activity
     * @param activity The activity to apply flags to
     */
    public static void applyAntiFlickeringFlags(Activity activity) {
        // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
// Set proper window flags to prevent flickering
            activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            );
            
            // Enable hardware acceleration
            activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            );
            
            // Set background to prevent white flash
            activity.getWindow().setBackgroundDrawableResource(android.R.color.white);
            
            // Disable enter/exit transitions that can cause flickering
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.getWindow().setEnterTransition(null);
                activity.getWindow().setExitTransition(null);
        }
    }
    
    /**
     * Start activity with smooth transition flags
     * @param activity Current activity
     * @param intent Intent to start new activity
     */
    public static void startActivitySmooth(Activity activity, Intent intent) {
        // Add proper transition flags
        // Use custom transition animation
        activity.startActivity(intent);
        // Apply custom animation
        activity.overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        );
    }
    
    /**
     * Start activity and finish current with smooth transition
     * @param activity Current activity
     * @param intent Intent to start new activity
     */
    public static void startActivityAndFinishSmooth(Activity activity, Intent intent) {
        // Add proper transition flags
        // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            activity.startActivity(intent);
            // Apply custom animation before finishing
            activity.overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            );
            activity.finish();
    }
    
    /**
     * Set background color to prevent white flash
     * @param activity The activity
     * @param colorResId Color resource ID
     */
    public static void setBackgroundColor(Activity activity, int colorResId) {
        activity.getWindow().getDecorView().setBackgroundColor(
            ContextCompat.getColor(activity, colorResId)
        );
    }
    
    /**
     * Set status bar color
     * @param activity The activity
     * @param colorResId Color resource ID
     */
    public static void setStatusBarColor(Activity activity, int colorResId) {
        // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
activity.getWindow().setStatusBarColor(
                ContextCompat.getColor(activity, colorResId)
            );
    }
    
    /**
     * Optimize window for better performance and prevent flickering
     * @param activity The activity to optimize
     */
    public static void optimizeWindow(Activity activity) {
        // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
// Set proper window attributes
            activity.getWindow().setAttributes(
                new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            );
            
            // Enable hardware acceleration
            activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            );
            
            // Set proper background
            activity.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }
} 