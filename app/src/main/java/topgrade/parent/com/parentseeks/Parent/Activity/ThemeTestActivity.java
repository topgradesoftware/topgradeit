package topgrade.parent.com.parentseeks.Parent.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.R;

/**
 * Test Activity to verify dynamic theme support for all user types
 * Tests Student (Teal), Staff (Navy Blue), and Parent (Brown) themes
 */
public class ThemeTestActivity extends AppCompatActivity {

    private static final String TAG = "ThemeTestActivity";

    private TextView headerTitle;
    private ImageView headerWave;
    private LinearLayout footerContainer;
    private Button testStudentTheme;
    private Button testStaffTheme;
    private Button testParentTheme;
    private Button openZoomImage;
    private TextView currentThemeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Paper DB
        try {
            Paper.init(this);
        } catch (Exception e) {
            // Paper might already be initialized, ignore the exception
            Log.d(TAG, "Paper already initialized or error: " + e.getMessage());
        }
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_theme_test);
        
        // Cache views
        headerWave = findViewById(R.id.header_wave);
        footerContainer = findViewById(R.id.footer_container);
        headerTitle = findViewById(R.id.header_title);
        testStudentTheme = findViewById(R.id.test_student_theme);
        testStaffTheme = findViewById(R.id.test_staff_theme);
        testParentTheme = findViewById(R.id.test_parent_theme);
        openZoomImage = findViewById(R.id.open_zoom_image);
        currentThemeText = findViewById(R.id.current_theme_text);
        
        // Setup click listeners
        testStudentTheme.setOnClickListener(v -> testStudentTheme());
        testStaffTheme.setOnClickListener(v -> testStaffTheme());
        testParentTheme.setOnClickListener(v -> testParentTheme());
        openZoomImage.setOnClickListener(v -> openZoomImageTest());
        
        // Apply current theme
        applyCurrentTheme();
        
        // Setup window insets
        setupWindowInsets();
    }
    
    /**
     * Test Student Theme (Teal)
     */
    private void testStudentTheme() {
        Log.d(TAG, "Testing Student Theme (Teal)");
        
        // Save user type to Paper DB
        Paper.book().write(Constants.User_Type, "STUDENT");
        
        // Apply theme
        applyTheme("STUDENT");
        
        // Update UI
        updateCurrentThemeText("Student Theme (Teal)");
        
        Toast.makeText(this, "Applied Student Theme (Teal)", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Test Staff Theme (Navy Blue)
     */
    private void testStaffTheme() {
        Log.d(TAG, "Testing Staff Theme (Navy Blue)");
        
        // Save user type to Paper DB
        Paper.book().write(Constants.User_Type, "STAFF");
        
        // Apply theme
        applyTheme("STAFF");
        
        // Update UI
        updateCurrentThemeText("Staff Theme (Navy Blue)");
        
        Toast.makeText(this, "Applied Staff Theme (Navy Blue)", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Test Parent Theme (Brown)
     */
    private void testParentTheme() {
        Log.d(TAG, "Testing Parent Theme (Brown)");
        
        // Save user type to Paper DB
        Paper.book().write(Constants.User_Type, "PARENT");
        
        // Apply theme
        applyTheme("PARENT");
        
        // Update UI
        updateCurrentThemeText("Parent Theme (Brown)");
        
        Toast.makeText(this, "Applied Parent Theme (Brown)", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Open ZoomImage activity to test theme there
     */
    private void openZoomImageTest() {
        // Get current user type to create themed test image
        String userType = Paper.book().read(Constants.User_Type, "PARENT");
        String testImageUrl;
        String testImageName = "Theme Test";
        
        // Create themed test image URL based on current theme
        if (userType != null && userType.equals("STUDENT")) {
            testImageUrl = "https://via.placeholder.com/400x300/004d40/FFFFFF?text=Student+Theme";
        } else if (userType != null && (userType.equals("STAFF") || userType.equals("TEACHER"))) {
            testImageUrl = "https://via.placeholder.com/400x300/000064/FFFFFF?text=Staff+Theme";
        } else {
            testImageUrl = "https://via.placeholder.com/400x300/693e02/FFFFFF?text=Parent+Theme";
        }
        
        // Start ZoomImage activity
        ZoomImage.start(this, testImageUrl, testImageName);
    }
    
    /**
     * Apply theme based on user type
     */
    private void applyTheme(String userType) {
        try {
            Log.d(TAG, "Applying theme for user type: " + userType);
            
            if (userType.equals("STUDENT")) {
                // Apply student theme (teal)
                ThemeHelper.applyStudentTheme(this);
                
                // Setup status bar with student theme colors
                setupStatusBar(
                    ContextCompat.getColor(this, R.color.student_primary),
                    ContextCompat.getColor(this, R.color.student_primary)
                );
                
                // Set header wave for student theme
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_teal);
                }
                
                // Set footer background for student theme
                if (footerContainer != null) {
                    footerContainer.setBackgroundResource(R.drawable.footer_background_teal);
                }
                
            } else if (userType.equals("STAFF") || userType.equals("TEACHER")) {
                // Apply staff theme (navy blue)
                // Setup status bar with transparent status bar and navy navigation bar
                setupStatusBar(
                    Color.TRANSPARENT,
                    ContextCompat.getColor(this, R.color.navy_blue)
                );
                
                // Set header wave for staff theme
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_navy_blue);
                }
                
                // Set footer background for staff theme
                if (footerContainer != null) {
                    footerContainer.setBackgroundResource(R.drawable.footer_background_staff_navy);
                }
                
            } else {
                // Apply parent theme (brown) - default fallback
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for theme test
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for theme test
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Theme Test");
                
                // Set header wave for parent theme
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_dark_brown);
                }
                
                // Set footer background for parent theme
                if (footerContainer != null) {
                    footerContainer.setBackgroundResource(R.drawable.footer_background_brown);
                }
            }
            
            Log.d(TAG, "Theme applied successfully for: " + userType);
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme for user type: " + userType, e);
            Toast.makeText(this, "Error applying theme: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Apply current theme from Paper DB
     */
    private void applyCurrentTheme() {
        try {
            String userType = Paper.book().read(Constants.User_Type, "PARENT");
            Log.d(TAG, "Current user type from Paper DB: " + userType);
            
            applyTheme(userType);
            updateCurrentThemeText(ThemeHelper.getThemeDisplayName(userType));
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying current theme", e);
            // Apply default parent theme
            applyTheme("PARENT");
            updateCurrentThemeText("Parent Theme (Brown) - Default");
        }
    }
    
    /**
     * Update current theme text display
     */
    private void updateCurrentThemeText(String themeName) {
        if (currentThemeText != null) {
            currentThemeText.setText("Current Theme: " + themeName);
        }
    }
    
    /**
     * Setup status bar and navigation bar with proper colors and icon appearance
     */
    private void setupStatusBar(int statusBarColor, int navBarColor) {
        // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
getWindow().setStatusBarColor(statusBarColor);
            getWindow().setNavigationBarColor(navBarColor);
            // minSdk is 26, so M (API 23) check is unnecessary
int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
    }
    
    /**
     * Setup window insets to respect system bars
     */
    private void setupWindowInsets() {
        View rootLayout = findViewById(R.id.root_layout);
        
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    // Apply safe padding to avoid overlapping with system bars
                    view.setPadding(
                        systemInsets.left,
                        systemInsets.top,
                        systemInsets.right,
                        systemInsets.bottom
                    );

                    return WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }
    }
    
    /**
     * Static helper method to start ThemeTestActivity
     */
    public static void start(Context context) {
        try {
            Intent intent = new Intent(context, ThemeTestActivity.class);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting ThemeTestActivity", e);
            Toast.makeText(context, "Error opening theme test", Toast.LENGTH_SHORT).show();
        }
    }
}
