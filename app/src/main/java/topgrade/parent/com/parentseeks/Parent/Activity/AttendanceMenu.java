package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.R;

public class AttendanceMenu extends AppCompatActivity {
    
    private static final String TAG = "AttendanceMenu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AttendanceMenu onCreate - Loading layout");
        
        // Check user type and load appropriate layout
        String userType = Paper.book().read("User_Type", "");
        if ("STUDENT".equalsIgnoreCase(userType)) {
            setContentView(R.layout.activity_student_attendance_menu);
            Log.d(TAG, "AttendanceMenu onCreate - Student layout loaded");
            // Apply student theme
            ThemeHelper.applyStudentTheme(this);
            Toast.makeText(this, "STUDENT Attendance Menu Loaded", Toast.LENGTH_LONG).show();
        } else {
            // Set edge-to-edge display for parent layout
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            
            setContentView(R.layout.activity_view_attendance_parent);
            Log.d(TAG, "AttendanceMenu onCreate - Parent layout loaded");
            
            // Configure status bar for dark brown background with white icons
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Set transparent status bar to allow header wave to cover it
                getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
                
                // For Android M and above, ensure white status bar icons on dark background
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int flags = getWindow().getDecorView().getSystemUiVisibility();
                    // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
                    flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    getWindow().getDecorView().setSystemUiVisibility(flags);
                }
            }

            // Configure status bar and navigation bar icons for Android R and above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (getWindow().getInsetsController() != null) {
                    getWindow().getInsetsController().setSystemBarsAppearance(
                        0, // No light icons for status bar (white icons on dark background)
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                }
            }
            
            // Setup window insets
            setupWindowInsets();
            
            // Initialize Paper database
            Paper.init(this);
            
            // Initialize back button
            ImageView backIcon = findViewById(R.id.back_icon);
            if (backIcon != null) {
                backIcon.setOnClickListener(v -> finish());
            }
            
            // Initialize header title
            TextView headerTitle = findViewById(R.id.header_title);
            if (headerTitle != null) {
                headerTitle.setText("Attendance Menu");
            }
            
            // For parent theme, don't use ParentThemeHelper as it overwrites navigation bar color
            // System bars are already configured above to match child list
            Log.d(TAG, "Parent theme - system bars already configured in onCreate()");
        }
        
        Log.d(TAG, "AttendanceMenu onCreate - Layout loaded successfully");
    }

    public void onBackPressed(View view) {
        Log.d(TAG, "Back button clicked");
        finish();
    }

    public void View_Attendance_Class(View view) {
        Log.d(TAG, "View_Attendance_Class clicked");
        try {
            Toast.makeText(this, "Opening Class Attendance...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AttendanceMenu.this, AttendanceClassWise.class));
        } catch (Exception e) {
            Log.e(TAG, "Error opening Class Attendance", e);
            Toast.makeText(this, "Error opening Class Attendance", Toast.LENGTH_SHORT).show();
        }
    }


    public void View_Attendance_Subject_Improved(View view) {
        Log.d(TAG, "View_Attendance_Subject_Improved clicked");
        try {
            Toast.makeText(this, "Opening Improved Subject Attendance...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AttendanceMenu.this, topgrade.parent.com.parentseeks.Parent.Activity.AttendanceSubjectWiseImproved.class));
        } catch (Exception e) {
            Log.e(TAG, "Error opening Improved Subject Attendance", e);
            Toast.makeText(this, "Error opening Improved Subject Attendance", Toast.LENGTH_SHORT).show();
        }
    }

    // Student-specific attendance methods (matching parent layout structure exactly)
    public void StudentClass(View view) {
        Log.d(TAG, "StudentClass clicked");
        try {
            Toast.makeText(this, "Opening Student Class Attendance...", Toast.LENGTH_SHORT).show();
            // Use the same AttendanceClassWise activity as parent - it will work for students too
            Intent intent = new Intent(AttendanceMenu.this, AttendanceClassWise.class);
            // Pass student context flag if needed
            intent.putExtra("USER_TYPE", "STUDENT");
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening Student Class Attendance", e);
            Toast.makeText(this, "Error opening Student Class Attendance", Toast.LENGTH_SHORT).show();
        }
    }

    public void StudentSection(View view) {
        Log.d(TAG, "StudentSection clicked");
        try {
            Toast.makeText(this, "Opening Student Subject Attendance...", Toast.LENGTH_SHORT).show();
            // Use the same AttendanceSubjectWiseImproved activity as parent - it will work for students too
            Intent intent = new Intent(AttendanceMenu.this, topgrade.parent.com.parentseeks.Parent.Activity.AttendanceSubjectWiseImproved.class);
            // Pass student context flag if needed
            intent.putExtra("USER_TYPE", "STUDENT");
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening Student Subject Attendance", e);
            Toast.makeText(this, "Error opening Student Subject Attendance", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * Uses margin approach like child list - footer pushed above navigation bar,
     * navigation bar's dark_brown color creates transparent/blended appearance
     */
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            
            if (rootLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                        );

                        android.view.View footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                            }
                        }
                        
                        view.setPadding(0, 0, 0, 0);
                        return WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e(TAG, "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
        }
    }

}
