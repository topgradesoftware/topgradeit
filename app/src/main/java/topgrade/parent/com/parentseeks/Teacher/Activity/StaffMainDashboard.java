package topgrade.parent.com.parentseeks.Teacher.Activity;

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
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffDashboard;
import topgrade.parent.com.parentseeks.Teacher.Activity.AcademicDashboard;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffOthersDashboard;

public class StaffMainDashboard extends BaseMainDashboard {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure status bar for navy blue background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
            
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
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();

    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the footer is visible above the navigation bar, not hidden behind it
     */
    protected void setupWindowInsets() {
        android.view.View rootLayout = findViewById(android.R.id.content);
        
        if (rootLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars()
                    );

                    // Add bottom margin to footer container to push it above navigation bar
                    LinearLayout footerContainer = findViewById(R.id.footer_container);
                    if (footerContainer != null) {
                        // Set bottom margin to navigation bar height to ensure footer is visible
                        int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                        android.view.ViewGroup.MarginLayoutParams params = 
                            (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                        if (params != null) {
                            params.bottomMargin = bottomMargin;
                            footerContainer.setLayoutParams(params);
                        }
                    }
                    
                    // No padding on root layout to avoid touch interference
                    view.setPadding(0, 0, 0, 0);

                    // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e("StaffMainDashboard", "Error in window insets listener: " + e.getMessage());
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                }
            });
        } else {
            Log.e("StaffMainDashboard", "rootLayout is null - cannot setup window insets");
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_staff_main_dashboard;
    }

    @Override
    protected int getPrimaryColor() {
        return R.color.staff_primary;
    }

    @Override
    protected String getUserType() {
        return ThemeHelper.THEME_STAFF;
    }

    @Override
    protected String getLogoutAPI() {
        return "logout_teacher";
    }

    @Override
    protected String getUserNameKey() {
        return "full_name";
    }

    @Override
    protected String getUserIDKey() {
        return "staff_id";
    }

    @Override
    protected String getDisplayName() {
        return "Staff Member";
    }

    // REFACTORED: loadDataAsync() and logout() are now inherited from BaseMainDashboard
    // which uses centralized UserDataManager and LogoutManager
    // This eliminates ~100 lines of duplicate code!

    @Override
    protected void setupCardClickListeners() {
        setClick(R.id.card_your_dashboard, StaffDashboard.class, "My Dashboard");
        setClick(R.id.card_academics_dashboard, AcademicDashboard.class, "Teaching Hub");
        setClick(R.id.card_other_options, StaffOthersDashboard.class, "More Options");
    }

    private void setClick(int id, Class<?> targetClass, String title) {
        try {
            View card = findViewById(id);
            if (card != null) {
                card.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(this, targetClass);
                        intent.putExtra("DASHBOARD_TITLE", title);
                        
                        // Use the same simple approach for all activities, including More Options
                        if (title.equals("More Options")) {
                            Log.d("StaffMainDashboard", "Starting StaffOthersDashboard with simple approach (like Teaching Hub)");
                        }
                        
                        // Use simple, reliable activity start for ALL options (including More Options)
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("StaffMainDashboard", "Error starting activity: " + targetClass.getSimpleName(), e);
                        Toast.makeText(this, "Error opening " + title, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.w("StaffMainDashboard", "Card view not found for ID: " + getResources().getResourceEntryName(id));
            }
        } catch (Exception e) {
            Log.e("StaffMainDashboard", "Error setting up click listener for ID: " + id, e);
        }
    }
}
