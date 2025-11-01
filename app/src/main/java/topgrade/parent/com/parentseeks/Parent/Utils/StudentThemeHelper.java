package topgrade.parent.com.parentseeks.Parent.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import topgrade.parent.com.parentseeks.R;

/**
 * Unified Student Theme Helper
 * Applies consistent header, footer, status bar, and navigation bar theming to all student pages
 */
public class StudentThemeHelper {
    
    private static final String TAG = "StudentThemeHelper";
    
    /**
     * Apply complete student theme to any activity
     * @param activity The activity to theme
     * @param headerHeight Height of header in dp (default: 100dp)
     */
    public static void applyStudentTheme(Activity activity, int headerHeight) {
        try {
            Log.d(TAG, "Applying student theme with header height: " + headerHeight + "dp");
            
            // Apply student theme colors - cast to AppCompatActivity if possible
            if (activity instanceof androidx.appcompat.app.AppCompatActivity) {
                ThemeHelper.applyStudentTheme((androidx.appcompat.app.AppCompatActivity) activity);
            } else {
                // For regular Activity, apply basic theme manually
                applyBasicStudentTheme(activity);
            }
            
            // Apply system bars theme (status bar and navigation bar)
            applySystemBarsTheme(activity);
            
            // Apply window flags for proper status bar coverage
            applyWindowFlags(activity);
            
            Log.d(TAG, "Student theme applied successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying student theme", e);
        }
    }
    
    /**
     * Apply student theme with default header height (100dp)
     */
    public static void applyStudentTheme(Activity activity) {
        applyStudentTheme(activity, 100);
    }
    
    /**
     * Apply basic student theme for regular Activity (not AppCompatActivity)
     */
    private static void applyBasicStudentTheme(Activity activity) {
        try {
            // Apply basic student theme colors
            int tealColor = ContextCompat.getColor(activity, R.color.student_primary);
            
            // Set action bar color if available
            if (activity.getActionBar() != null) {
                activity.getActionBar().setBackgroundDrawable(
                    ContextCompat.getDrawable(activity, R.drawable.bg_wave_teal)
                );
            }
            
            Log.d(TAG, "Basic student theme applied for regular Activity");
        } catch (Exception e) {
            Log.e(TAG, "Error applying basic student theme", e);
        }
    }
    
    /**
     * Apply system bars theme (status bar and navigation bar)
     */
    private static void applySystemBarsTheme(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int tealColor = ContextCompat.getColor(activity, R.color.student_primary);
            activity.getWindow().setStatusBarColor(tealColor);
            activity.getWindow().setNavigationBarColor(tealColor);
        }
    }
    
    /**
     * Apply window flags for proper status bar coverage
     */
    private static void applyWindowFlags(Activity activity) {
        // Set flags for proper status bar coverage
        activity.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        // Force dark navigation bar icons (prevent light appearance)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (activity.getWindow().getInsetsController() != null) {
                activity.getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // 0 = do NOT use light icons
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For API 26-29, set navigation bar icon color
            // Use a simpler approach that doesn't rely on the problematic constant
            try {
                // Set basic system UI visibility without light navigation bar
                activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
                Log.d(TAG, "Applied system UI visibility for API 26-29");
            } catch (Exception e) {
                Log.d(TAG, "Using fallback system UI visibility");
            }
        }
    }
    
    /**
     * Set header height dynamically
     * @param activity The activity
     * @param heightDp Height in dp
     */
    public static void setHeaderHeight(Activity activity, int heightDp) {
        try {
            View headerWave = activity.findViewById(R.id.header_wave);
            if (headerWave != null) {
                // Convert dp to pixels
                float density = activity.getResources().getDisplayMetrics().density;
                int heightPx = (int) (heightDp * density);
                
                // Set height
                headerWave.getLayoutParams().height = heightPx;
                headerWave.requestLayout();
                
                Log.d(TAG, "Header height set to " + heightDp + "dp (" + heightPx + "px)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting header height", e);
        }
    }
    
    /**
     * Show/hide header icon (for dashboard pages)
     */
    public static void setHeaderIconVisibility(Activity activity, boolean visible) {
        try {
            View headerIcon = activity.findViewById(R.id.header_icon);
            if (headerIcon != null) {
                headerIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting header icon visibility", e);
        }
    }
    
    /**
     * Show/hide more options button (for dashboard pages)
     */
    public static void setMoreOptionsVisibility(Activity activity, boolean visible) {
        try {
            View moreOptions = activity.findViewById(R.id.more_option);
            if (moreOptions != null) {
                moreOptions.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting more options visibility", e);
        }
    }
    
    /**
     * Set header title
     */
    public static void setHeaderTitle(Activity activity, String title) {
        try {
            View headerTitle = activity.findViewById(R.id.header_title);
            if (headerTitle instanceof android.widget.TextView) {
                ((android.widget.TextView) headerTitle).setText(title);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting header title", e);
        }
    }
    
    /**
     * Show/hide footer
     */
    public static void setFooterVisibility(Activity activity, boolean visible) {
        try {
            View footer = activity.findViewById(R.id.footer_container);
            if (footer != null) {
                footer.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting footer visibility", e);
        }
    }
}
