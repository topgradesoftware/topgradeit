package topgrade.parent.com.parentseeks.Utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ConsolidatedANRPreventionHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.LogoutManager;
import topgrade.parent.com.parentseeks.Parent.Utils.UserDataManager;
import topgrade.parent.com.parentseeks.Parent.Utils.AnalyticsManager;
import topgrade.parent.com.parentseeks.Parent.Utils.ScreenTrackingHelper;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;
import topgrade.parent.com.parentseeks.Parent.Activity.SelectRole;
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange;

public abstract class BaseMainDashboard extends AppCompatActivity {

    // Common UI components (all dashboards have these)
    protected TextView headerTitle;
    protected CustomPopupMenu customPopupMenu;
    protected ImageView moreOption;
    protected int primaryColor;

    // Abstract methods - you'll implement these in each dashboard
    protected abstract int getPrimaryColor();
    protected abstract String getUserType();
    protected abstract String getLogoutAPI();
    protected abstract String getUserNameKey();
    protected abstract String getUserIDKey();
    protected abstract String getDisplayName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            Log.d(getClass().getSimpleName(), "onCreate started");
            super.onCreate(savedInstanceState);
            
            // Start tracking screen load time
            ScreenTrackingHelper.startLoadTimeTracking(this);
            
            // Set the layout (each dashboard will override this)
            setContentView(getLayoutResource());
            Log.d(getClass().getSimpleName(), "Layout set successfully");

            // Apply theme based on user type
            applyTheme();

            // Get the primary color for this user type
            primaryColor = ContextCompat.getColor(this, getPrimaryColor());
            Log.d(getClass().getSimpleName(), "Primary color loaded: " + primaryColor);

            initUIComponents();
            setupListeners();
            applyUIStylingAsync();
            loadDataAsync();
            
            Log.d(getClass().getSimpleName(), "onCreate completed successfully");

        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error in onCreate", e);
            Toast.makeText(this, "Error initializing dashboard", Toast.LENGTH_SHORT).show();
        }
    }

    // Abstract method for layout resource
    protected abstract int getLayoutResource();

    // Common methods that work the same for all dashboards
    protected void initUIComponents() {
        try {
            Log.d(getClass().getSimpleName(), "initUIComponents started");
            headerTitle = findViewById(R.id.header_title);
            moreOption = findViewById(R.id.more_option);
            
            Log.d(getClass().getSimpleName(), "Views found - headerTitle: " + (headerTitle != null) + ", moreOption: " + (moreOption != null));

            setupCardClickListeners();
            Log.d(getClass().getSimpleName(), "initUIComponents completed successfully");
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error initializing UI components", e);
        }
    }

    protected void setupListeners() {
        try {
            Log.d(getClass().getSimpleName(), "setupListeners started");
            if (moreOption != null) {
                moreOption.setOnClickListener(this::showMoreOptions);
                Log.d(getClass().getSimpleName(), "More option click listener set successfully");
            } else {
                Log.e(getClass().getSimpleName(), "moreOption is null, cannot set click listener");
            }
            Log.d(getClass().getSimpleName(), "setupListeners completed successfully");
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error setting up listeners", e);
        }
    }

    protected void applyUIStylingAsync() {
        // Add a small delay to prevent blocking the main thread
        getWindow().getDecorView().postDelayed(() -> {
            try {
                // Make status bar transparent to allow header wave to cover it
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
                    getWindow().setNavigationBarColor(primaryColor);
                }

                // Configure status bar and navigation bar icons - NO LIGHT ICONS to prevent shadows
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R &&
                        getWindow().getInsetsController() != null) {
                    getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // No light icons for status bar (prevents shadows)
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                }

                setupWindowInsets();
                
                Log.d(getClass().getSimpleName(), "Navy blue status bar and navigation bar applied successfully");
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error in UI styling", e);
            }
        }, 50); // 50ms delay to prevent blocking
    }

    /**
     * Load user data asynchronously
     * REFACTORED: Uses centralized UserDataManager
     */
    protected void loadDataAsync() {
        ConsolidatedANRPreventionHelper.executeInBackground(
            () -> {
                // Get user name using centralized manager
                String userName = UserDataManager.INSTANCE.getCurrentUserName(getDisplayName());
                return userName;
            },
            userName -> {
                ConsolidatedANRPreventionHelper.safeUIUpdate(this, () -> {
                    if (headerTitle != null) {
                        headerTitle.setText(userName != null && !userName.isEmpty() ? userName : getDisplayName());
                    }
                    return null;
                });
                return null;
            },
            exception -> {
                Log.e(getClass().getSimpleName(), "Error loading data", exception);
                ConsolidatedANRPreventionHelper.safeUIUpdate(this, () -> {
                    if (headerTitle != null) {
                        headerTitle.setText(getDisplayName());
                    }
                    return null;
                });
                return null;
            }
        );
    }

    protected void showMoreOptions(View view) {
        try {
            Log.d(getClass().getSimpleName(), "showMoreOptions called");
            
            if (customPopupMenu == null) {
                Log.d(getClass().getSimpleName(), "Creating new CustomPopupMenu");
                customPopupMenu = new CustomPopupMenu(this, view, getUserType());
                customPopupMenu.setOnMenuItemClickListener(title -> {
                    Log.d(getClass().getSimpleName(), "Menu item clicked: " + title);
                    switch (title) {
                        case CustomPopupMenu.MENU_SHARE:
                            AnalyticsManager.INSTANCE.logShare("app", "menu");
                            share();
                            break;
                        case CustomPopupMenu.MENU_RATE:
                            AnalyticsManager.INSTANCE.logEvent("rate_us_clicked");
                            rateUs();
                            break;
                        case CustomPopupMenu.MENU_CHANGE_PASSWORD:
                            AnalyticsManager.INSTANCE.logEvent("change_password_clicked");
                            changePassword();
                            break;
                        case CustomPopupMenu.MENU_LOGOUT:
                            AnalyticsManager.INSTANCE.logLogout(getUserType());
                            logout();
                            break;
                        default:
                            Log.w(getClass().getSimpleName(), "Unknown menu item: " + title);
                            break;
                    }
                    return true;
                });
                Log.d(getClass().getSimpleName(), "CustomPopupMenu created successfully");
            }

            if (customPopupMenu.isShowing()) {
                Log.d(getClass().getSimpleName(), "Dismissing popup menu");
                customPopupMenu.dismiss();
            } else {
                Log.d(getClass().getSimpleName(), "Showing popup menu");
                customPopupMenu.show();
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error showing popup menu", e);
            showFallbackOptions();
        }
    }
    
    protected void showFallbackOptions() {
        try {
            // Show a simple toast first
            Toast.makeText(this, "More options: Share, Rate, Change Password, Logout", Toast.LENGTH_LONG).show();
            
            // Then show a simple dialog with options
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("More Options")
                .setItems(new String[]{"Share Application", "Rate Us", "Change Password", "Logout"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            share();
                            break;
                        case 1:
                            rateUs();
                            break;
                        case 2:
                            changePassword();
                            break;
                        case 3:
                            logout();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error showing fallback options", e);
            Toast.makeText(this, "More options: Share, Rate, Change Password, Logout", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Logout method - now uses centralized LogoutManager
     * REFACTORED: Eliminates duplicate logout code across all dashboard classes
     */
    protected void logout() {
        try {
            Log.d(getClass().getSimpleName(), "Logout requested");
            
            // Use centralized LogoutManager for consistent logout behavior
            LogoutManager.INSTANCE.performLogout(
                this,
                topgrade.parent.com.parentseeks.Parent.Utils.API.getAPIService(),
                () -> {
                    // Navigate to role selection after logout
                    LogoutManager.INSTANCE.navigateAfterLogout(this);
                    return null;
                },
                (Boolean show) -> {
                    showLoading(show);
                    return null;
                }
            );
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error during logout", e);
            showError("Error during logout: " + e.getMessage());
        }
    }
    
    protected void showLoading(boolean show) {
        // Note: This dashboard doesn't have a progress bar, so we'll use a simple approach
        if (show) {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        }
    }
    
    protected void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void changePassword() {
        try {
            startActivity(new Intent(this, PasswordsChange.class)
                    .putExtra("User_TYpe", getUserType()));
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error opening password change", e);
            Toast.makeText(this, "Error opening password change", Toast.LENGTH_SHORT).show();
        }
    }

    protected void share() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));

            String shareMessage = "\nLet me recommend you this application to view student attendance, fee Challan and Reports.\n\n"
                    + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Choose one"));
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error sharing app", e);
            Toast.makeText(this, "Error sharing application", Toast.LENGTH_SHORT).show();
        }
    }

    protected void rateUs() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
        } catch (android.content.ActivityNotFoundException e) {
            // fallback if Play Store not installed
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error opening rate page", e);
            Toast.makeText(this, "Error opening rate page", Toast.LENGTH_SHORT).show();
        }
    }

    protected void setupCardClickListeners() {
        // This will be implemented by each dashboard class
        // as they have different card configurations
    }

    protected void setupWindowInsets() {
        try {
            ConstraintLayout rootLayout = findViewById(R.id.rootLayout);
            if (rootLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        int bottomPadding = systemInsets.bottom > 20 ? systemInsets.bottom : 0;
                        view.setPadding(0, 0, 0, bottomPadding);
                        return WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "Error in window insets listener: " + e.getMessage());
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e(getClass().getSimpleName(), "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error setting up window insets", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(getClass().getSimpleName(), "onResume called");
        
        // Track screen view with Firebase Analytics
        ScreenTrackingHelper.trackScreenView(this);
        
        // Set user properties for analytics
        try {
            String userId = UserDataManager.INSTANCE.getCurrentUserId();
            String userType = getUserType();
            
            if (userId != null) {
                AnalyticsManager.INSTANCE.setUserId(userId);
            }
            if (userType != null) {
                AnalyticsManager.INSTANCE.setUserType(userType);
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error setting analytics properties", e);
        }
        
        // Ensure activity stays visible
        ensureActivityVisibility();
        
        // Keep status bar transparent to allow header wave to cover it
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(primaryColor);
        }
        
        // Configure status bar icons - NO LIGHT ICONS to prevent shadows
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R &&
                getWindow().getInsetsController() != null) {
            getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar (prevents shadows)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            );
        }
        
        verifyTouchEvents();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(getClass().getSimpleName(), "onPause called");
    }
    
    @Override
    protected void onStop() {
        Log.d(getClass().getSimpleName(), "onStop called");
        
        // Simple visibility check for all cases
        if (!isFinishing() && !isDestroyed()) {
            Log.d(getClass().getSimpleName(), "Activity stopped but not finishing, ensuring visibility");
            ensureActivityVisibility();
        }
        
        super.onStop();
    }

    protected void ensureActivityVisibility() {
        try {
            // Simple visibility check and fix
            if (getWindow().getDecorView().getVisibility() != View.VISIBLE) {
                Log.w(getClass().getSimpleName(), "Window was not visible, fixing visibility");
                getWindow().getDecorView().setVisibility(View.VISIBLE);
            }
            
            // Simple layout update
            getWindow().getDecorView().requestLayout();
            
            Log.d(getClass().getSimpleName(), "Activity visibility ensured");
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error ensuring activity visibility", e);
        }
    }

    protected void verifyTouchEvents() {
        try {
            if (moreOption != null) {
                moreOption.setClickable(true);
                moreOption.setFocusable(true);
                Log.d(getClass().getSimpleName(), "Touch events verified for more option");
            } else {
                Log.w(getClass().getSimpleName(), "moreOption is null in verifyTouchEvents");
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error verifying touch events", e);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(getClass().getSimpleName(), "onDestroy called");
        super.onDestroy();
        if (customPopupMenu != null) {
            if (customPopupMenu.isShowing()) {
                customPopupMenu.dismiss();
            }
            customPopupMenu = null;
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(getClass().getSimpleName(), "onWindowFocusChanged: " + hasFocus);
        
        if (hasFocus) {
            // Window gained focus, ensure visibility
            ensureActivityVisibility();
        }
    }
    
    /**
     * Apply theme based on user type
     */
    private void applyTheme() {
        try {
            String userType = getUserType();
            Log.d(getClass().getSimpleName(), "Applying theme for user type: " + userType);
            
            // Apply unified parent theme for parent dashboards
            if (ThemeHelper.THEME_PARENT.equals(userType)) {
                ParentThemeHelper.applyParentTheme(this, 140); // 140dp for dashboard pages
                ParentThemeHelper.setHeaderIconVisibility(this, true); // Show icon for dashboards
                ParentThemeHelper.setMoreOptionsVisibility(this, true); // Show more options for dashboards
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, getDisplayName());
            } else {
                // Apply theme using ThemeHelper for non-parent users
                ThemeHelper.applySimpleTheme(this, userType);
            }
            
            Log.d(getClass().getSimpleName(), "Theme applied successfully");
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error applying theme", e);
        }
    }

    @Override
    @Deprecated
    public void onBackPressed() {
        Log.d(getClass().getSimpleName(), "onBackPressed called");
        super.onBackPressed();
    }
}
