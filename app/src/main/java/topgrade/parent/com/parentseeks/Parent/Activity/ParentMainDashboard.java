package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.LinearLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;

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
import topgrade.parent.com.parentseeks.Parent.Activity.PersonalDashboard;
import topgrade.parent.com.parentseeks.Parent.Activity.OtherOptionsDashboard;
import topgrade.parent.com.parentseeks.Parent.Activity.AcademicsDashboard;

public class ParentMainDashboard extends BaseMainDashboard {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_parent_main_dashboard;
    }

    @Override
    protected int getPrimaryColor() {
        return R.color.parent_primary;
    }

    @Override
    protected String getUserType() {
        return ThemeHelper.THEME_PARENT;
    }

    @Override
    protected String getLogoutAPI() {
        return "logout_parent";
    }

    @Override
    protected String getUserNameKey() {
        return "full_name";
    }

    @Override
    protected String getUserIDKey() {
        return "parent_id";
    }

    @Override
    protected String getDisplayName() {
        return "Parent Member";
    }
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure navigation bar for dark_brown background with white icons - MATCH STAFF DASHBOARD APPROACH
        // Set this DIRECTLY and SYNCHRONOUSLY like StaffMainDashboard does, not in async callbacks
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            // Set navigation bar to dark_brown to match login screen footer
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
            
            // For Android M and above, ensure white status bar icons on dark background
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }

        // Configure status bar and navigation bar icons for Android R and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar and navigation bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | 
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();
    }
    

    // REFACTORED: loadDataAsync() and logout() are now inherited from BaseMainDashboard
    // which uses centralized UserDataManager and LogoutManager
    // This eliminates ~100 lines of duplicate code!

    @Override
    protected void setupCardClickListeners() {
        setClick(R.id.card_your_dashboard, PersonalDashboard.class, "Personal Dashboard");
        setClick(R.id.card_academics_dashboard, AcademicsDashboard.class, "Child Academics");
        setClick(R.id.card_other_options, OtherOptionsDashboard.class, "More Options");
    }
    
    /**
     * Override applyUIStylingAsync to ensure dark_brown navigation bar is maintained
     * Base class sets it to primaryColor (parent_primary), we override to dark_brown
     */
    @Override
    protected void applyUIStylingAsync() {
        // Call parent first to do status bar setup
        getWindow().getDecorView().postDelayed(() -> {
            try {
                // Make status bar transparent to allow header wave to cover it
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
                    // Override: Set navigation bar to dark_brown instead of primaryColor
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
                    
                    // Ensure white icons on dark background for Android M+
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        int flags = getWindow().getDecorView().getSystemUiVisibility();
                        flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                        getWindow().getDecorView().setSystemUiVisibility(flags);
                    }
                }

                // Configure status bar and navigation bar icons for Android R and above
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // No light icons for status bar and navigation bar (white icons on dark background)
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | 
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }

                setupWindowInsets();
                
                Log.d("ParentMainDashboard", "Dark brown navigation bar applied successfully in applyUIStylingAsync");
            } catch (Exception e) {
                Log.e("ParentMainDashboard", "Error in UI styling", e);
            }
        }, 50); // 50ms delay to prevent blocking
    }
    
    /**
     * Override setupWindowInsets to handle footer margins like parent login screen
     * This ensures the footer is visible above the navigation bar, not hidden behind it
     */
    @Override
    protected void setupWindowInsets() {
        try {
            // Use android.R.id.content like other parent activities for consistency
            android.view.View rootLayout = findViewById(android.R.id.content);
            if (rootLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                        // Extend footer card to bottom of screen and add padding inside for navigation bar
                        // This fills the navigation bar area with dark_brown color, eliminating white gap
                        LinearLayout footerContainer = findViewById(R.id.footer_container);
                        androidx.cardview.widget.CardView footerCard = null;
                        
                        // Find the CardView inside footer_container
                        if (footerContainer != null && footerContainer.getChildCount() > 0) {
                            android.view.View child = footerContainer.getChildAt(0);
                            if (child instanceof androidx.cardview.widget.CardView) {
                                footerCard = (androidx.cardview.widget.CardView) child;
                            }
                        }
                        
                        if (footerCard != null) {
                            // Get navigation bar height
                            int bottomPadding = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            
                            // Find the inner LinearLayout with the content
                            LinearLayout contentLayout = null;
                            if (footerCard.getChildCount() > 0) {
                                android.view.View child = footerCard.getChildAt(0);
                                if (child instanceof LinearLayout) {
                                    contentLayout = (LinearLayout) child;
                                }
                            }
                            
                            if (contentLayout != null) {
                                // Add bottom padding to content layout to push content above navigation bar
                                // The CardView background will fill the navigation bar space with dark_brown
                                int currentPadding = contentLayout.getPaddingBottom();
                                if (currentPadding != bottomPadding) {
                                    contentLayout.setPadding(
                                        contentLayout.getPaddingLeft(),
                                        contentLayout.getPaddingTop(),
                                        contentLayout.getPaddingRight(),
                                        bottomPadding
                                    );
                                    Log.d("ParentMainDashboard", "Footer card padding set to: " + bottomPadding + "px");
                                }
                            }
                            
                            // Ensure footer container extends to bottom with no margin
                            android.view.ViewGroup.LayoutParams layoutParams = footerContainer.getLayoutParams();
                            if (layoutParams instanceof LayoutParams) {
                                LayoutParams params = (LayoutParams) layoutParams;
                                params.bottomMargin = 0; // No margin - extend to bottom
                                footerContainer.setLayoutParams(params);
                            } else if (layoutParams instanceof android.view.ViewGroup.MarginLayoutParams) {
                                android.view.ViewGroup.MarginLayoutParams params = 
                                    (android.view.ViewGroup.MarginLayoutParams) layoutParams;
                                params.bottomMargin = 0; // No margin - extend to bottom
                                footerContainer.setLayoutParams(params);
                            }
                        } else {
                            Log.w("ParentMainDashboard", "footer_card not found - cannot set padding");
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "Error in window insets listener: " + e.getMessage(), e);
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
                
                // Force initial application of insets
                ViewCompat.requestApplyInsets(rootLayout);
            } else {
                Log.e(getClass().getSimpleName(), "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error setting up window insets", e);
        }
    }

    private void setClick(int id, Class<?> targetClass, String title) {
        try {
            View card = findViewById(id);
            if (card != null) {
                card.setOnClickListener(v -> {
                    try {
                        // Track dashboard card click
                        topgrade.parent.com.parentseeks.Parent.Utils.AnalyticsManager.INSTANCE.logDashboardCardClick(
                            title, 
                            getUserType()
                        );
                        
                        Intent intent = new Intent(this, targetClass);
                        intent.putExtra("DASHBOARD_TITLE", title);
                        
                        // Use the same simple approach for all activities, including More Options
                        if (title.equals("More Options")) {
                            Log.d("ParentMainDashboard", "Starting OtherOptionsDashboard with simple approach (like Child Academics)");
                        }
                        
                        // Use simple, reliable activity start for ALL options (including More Options)
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("ParentMainDashboard", "Error starting activity: " + targetClass.getSimpleName(), e);
                        Toast.makeText(this, "Error opening " + title, Toast.LENGTH_SHORT).show();
                        
                        // Track error
                        topgrade.parent.com.parentseeks.Parent.Utils.AnalyticsManager.INSTANCE.logError(
                            "navigation_error",
                            e.getMessage() != null ? e.getMessage() : "Unknown error",
                            "ParentMainDashboard"
                        );
                    }
                });
            } else {
                Log.w("ParentMainDashboard", "Card view not found for ID: " + getResources().getResourceEntryName(id));
            }
        } catch (Exception e) {
            Log.e("ParentMainDashboard", "Error setting up click listener for ID: " + id, e);
        }
    }
}