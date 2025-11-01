package topgrade.parent.com.parentseeks.Teacher.Activity.Attendance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffAttendanceSubmitClass;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffAttendanceSubmitSection;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffAttendanceSubmitSubject;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffAttendanceUpdateClass;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffAttendanceUpdateSection;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffAttendanceUpdateSubject;

public class StaffAttendanceMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_all_attendance_menu);
        
        // Configure status bar for navy blue background with white icons - COMPREHENSIVE FIX
        // Set transparent status bar to allow header wave to cover it
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
        
        // Ensure white status bar icons on dark background
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
        flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        
        // Configure status bar and navigation bar icons for Android R and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();
        
        // Load staff credentials from Paper database
        topgrade.parent.com.parentseeks.Teacher.Utils.Constant.loadFromPaper();

        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(v -> finish());
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
     */
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);

            if (rootLayout != null) {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars()
                        );

                        // Add bottom margin to main content to push it above navigation bar
                        LinearLayout mainContent = findViewById(R.id.layout);
                        if (mainContent != null) {
                            // Set bottom margin to navigation bar height to ensure content is visible
                            int bottomMargin = Math.max(systemInsets.bottom, 0);
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) mainContent.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 16; // 16dp extra padding
                                mainContent.setLayoutParams(params);
                            }
                        }

                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("StaffAttendanceMenu", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e("StaffAttendanceMenu", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e("StaffAttendanceMenu", "Error setting up window insets: " + e.getMessage(), e);
        }
    }

    public void MYAttendance(View view) {
        startActivity(new Intent(StaffAttendanceMenu.this, StaffAttendance.class));
    }

    public void StudentAttendance(View view) {
        startActivity(new Intent(StaffAttendanceMenu.this, StaffAttendanceSubmitSubject.class));
    }

    public void StudentAttendanceEdit(View view) {
        startActivity(new Intent(StaffAttendanceMenu.this, StaffAttendanceUpdateSubject.class));
    }

    public void StudentAttendance_CLass(View view) {
        startActivity(new Intent(StaffAttendanceMenu.this, StaffAttendanceSubmitClass.class));
    }

    public void StudentAttendanceEdit_CLass(View view) {
        startActivity(new Intent(StaffAttendanceMenu.this, StaffAttendanceUpdateClass.class));
    }

    public void StudentAttendance_Section(View view) {
        startActivity(new Intent(StaffAttendanceMenu.this, StaffAttendanceSubmitSection.class));
    }

    public void StudentAttendanceEdit_Section(View view) {
        startActivity(new Intent(StaffAttendanceMenu.this, StaffAttendanceUpdateSection.class));
    }

}
