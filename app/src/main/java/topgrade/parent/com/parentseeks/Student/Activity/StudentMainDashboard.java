package topgrade.parent.com.parentseeks.Student.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Parent.Utils.ConsolidatedANRPreventionHelper;
import topgrade.parent.com.parentseeks.Utils.BaseMainDashboard;
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;
import topgrade.parent.com.parentseeks.Parent.Activity.SelectRole;
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange;
import topgrade.parent.com.parentseeks.Student.Activity.StudentPersonalDashboard;
import topgrade.parent.com.parentseeks.Student.Activity.StudentOtherOptionsDashboard;
import topgrade.parent.com.parentseeks.Student.Activity.StudentAcademicsDashboard;

public class StudentMainDashboard extends BaseMainDashboard {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_student_main_dashboard;
    }

    @Override
    protected int getPrimaryColor() {
        return R.color.student_primary;
    }

    @Override
    protected String getUserType() {
        return ThemeHelper.THEME_STUDENT;
    }

    @Override
    protected String getLogoutAPI() {
        return "logout_student";
    }

    @Override
    protected String getUserNameKey() {
        return "student_name";
    }

    @Override
    protected String getUserIDKey() {
        return "student_id";
    }

    @Override
    protected String getDisplayName() {
        return "Student Member";
    }

    @Override
    protected void applyUIStylingAsync() {
        // Call parent method first
        super.applyUIStylingAsync();
        
        // Apply theme to navigation bar (footer) with a small delay
        getWindow().getDecorView().postDelayed(() -> {
            applyNavigationBarTheme();
        }, 100); // 100ms delay to ensure UI is ready
    }
    
    private void applyNavigationBarTheme() {
        try {
            // Get user type from Paper DB
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("StudentMainDashboard", "=== ANDROID SYSTEM NAVIGATION BAR THEME DEBUG ===");
            Log.d("StudentMainDashboard", "User Type from Paper: '" + userType + "'");
            
            // Apply theme to Android system navigation bar
            int navigationBarColor;
            if (userType != null && userType.equals("STUDENT")) {
                navigationBarColor = getResources().getColor(R.color.teal);
                Log.d("StudentMainDashboard", "Applying teal theme to Android system navigation bar");
            } else {
                navigationBarColor = getResources().getColor(R.color.dark_brown);
                Log.d("StudentMainDashboard", "Applying dark brown theme to Android system navigation bar");
            }
            
            // Set Android system navigation bar color
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(navigationBarColor);
                
                // Set navigation bar appearance for better visibility
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        // Make navigation bar icons dark for better contrast
                        getWindow().getInsetsController().setSystemBarsAppearance(
                                0, // Clear light navigation bar flag
                                android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }
                
                Log.d("StudentMainDashboard", "Android system navigation bar color set to: " + navigationBarColor);
            }
            
            // Also apply theme to app footer container
            LinearLayout footerContainer = findViewById(R.id.footer_container);
            if (footerContainer != null) {
                footerContainer.setBackgroundColor(navigationBarColor);
                Log.d("StudentMainDashboard", "App footer container theme applied successfully");
            } else {
                Log.e("StudentMainDashboard", "Footer container not found - ID: footer_container");
            }
        } catch (Exception e) {
            Log.e("StudentMainDashboard", "Error applying navigation bar theme", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Reapply navigation bar theme in case it was overridden
        getWindow().getDecorView().postDelayed(() -> {
            applyNavigationBarTheme();
        }, 50); // Small delay to ensure it overrides system settings
    }

    // REFACTORED: loadDataAsync() and logout() are now inherited from BaseMainDashboard
    // which uses centralized UserDataManager and LogoutManager
    // This eliminates ~120 lines of duplicate code!

    @Override
    protected void setupCardClickListeners() {
        Log.d("StudentMainDashboard", "setupCardClickListeners() called");
        setClick(R.id.card_your_dashboard, StudentPersonalDashboard.class, "Personal Dashboard");
        setClick(R.id.card_academics_dashboard, StudentAcademicsDashboard.class, "Student Academics");
        setClick(R.id.card_other_options, StudentOtherOptionsDashboard.class, "More Options");
        Log.d("StudentMainDashboard", "setupCardClickListeners() completed");
    }

    private void setClick(int id, Class<?> targetClass, String title) {
        try {
            String resourceName = getResources().getResourceEntryName(id);
            Log.d("StudentMainDashboard", "Setting up click listener for: " + resourceName + " -> " + targetClass.getSimpleName());
            
            View card = findViewById(id);
            if (card != null) {
                Log.d("StudentMainDashboard", "Card found for " + resourceName + ", setting click listener");
                card.setOnClickListener(v -> {
                    try {
                        Log.d("StudentMainDashboard", "Card clicked: " + resourceName + " -> " + title);
                        Intent intent = new Intent(this, targetClass);
                        intent.putExtra("DASHBOARD_TITLE", title);
                        
                        // Use the same simple approach for all activities, including More Options
                        if (title.equals("More Options")) {
                            Log.d("StudentMainDashboard", "Starting StudentOtherOptionsDashboard with simple approach (like Student Academics)");
                        }
                        
                        // Use simple, reliable activity start for ALL options (including More Options)
                        startActivity(intent);
                        Log.d("StudentMainDashboard", "Activity started successfully: " + targetClass.getSimpleName());
                    } catch (Exception e) {
                        Log.e("StudentMainDashboard", "Error starting activity: " + targetClass.getSimpleName(), e);
                        Toast.makeText(this, "Error opening " + title, Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d("StudentMainDashboard", "Click listener set successfully for " + resourceName);
            } else {
                Log.w("StudentMainDashboard", "Card view not found for ID: " + resourceName);
            }
        } catch (Exception e) {
            Log.e("StudentMainDashboard", "Error setting up click listener for ID: " + id, e);
        }
    }
} 