package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Adaptor.HomeAdaptorParent;
import topgrade.parent.com.parentseeks.Parent.Model.HomeModel;
import topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.MemoryLeakDetector;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Interface.OnMenuCLick;
import topgrade.parent.com.parentseeks.Parent.Activity.StudentDateSheet;

public class AcademicsDashboard extends AppCompatActivity implements OnMenuCLick {
    
    private static final String TAG = "AcademicsDashboard";
    
    // UI Components
    private RecyclerView academicsRecyclerView;
    private ImageView backButton;
    private ImageView moreOption;
    private ProgressBar progressBar;
    private TextView dashboardTitle;
    
    // Data
    private String parent_id;
    private String campus_id;
    private List<HomeModel> academicsList;
    private HomeAdaptorParent academicsAdapter;
    
    // Lifecycle management
    private boolean isActivityDestroyed = false;
    private boolean isActivityPaused = false;
    
    // Background operations
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Custom popup menu
    private CustomPopupMenu customPopupMenu;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() - AcademicsDashboard activity started");
        
        // Register activity for memory leak detection
        MemoryLeakDetector.registerActivity(this);
        
        try {
            // Apply anti-flickering flags
            ActivityTransitionHelper.applyAntiFlickeringFlags(this);
            ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white);
            
            // Set edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            
            setContentView(R.layout.activity_parent_academics_dashboard);
            
            // Set navigation bar color to dark_brown to match login screen (like StaffMainDashboard)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
                
                // Ensure white icons on dark background
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int flags = getWindow().getDecorView().getSystemUiVisibility();
                    flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    getWindow().getDecorView().setSystemUiVisibility(flags);
                }
            }
            
            // Configure for Android R and above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (getWindow().getInsetsController() != null) {
                    getWindow().getInsetsController().setSystemBarsAppearance(
                        0, // No light icons (white icons on dark background)
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | 
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                }
            }
            
            // Apply parent theme
            applyTheme();
            
            // Setup window insets for footer positioning
            setupWindowInsets();
            
            // Initialize views
            initializeViews();
            
            // Title will be set in updateUI() after data loading
            
            // Setup click listeners
            setupClickListeners();
            
            // Initialize data in background
            mainHandler.postDelayed(() -> {
                if (!isActivityDestroyed) {
                    initializeDataAsync();
                }
            }, 50);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, getString(R.string.error_initializing_dashboard), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initializeViews() {
        try {
            academicsRecyclerView = findViewById(R.id.academics_recycler_view);
            backButton = findViewById(R.id.back_button);
            moreOption = findViewById(R.id.more_option);
            progressBar = findViewById(R.id.content_progress_bar);
            dashboardTitle = findViewById(R.id.header_title);
            
            Log.d(TAG, "initializeViews() - dashboardTitle: " + (dashboardTitle != null ? "found" : "not found"));
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }
    
    
    private void setupClickListeners() {
        // Back Button - consistent with "Back to Home" behavior
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (isActivityValid()) {
                    Log.d(TAG, "Back button clicked");
                    backToHome(); // Use same navigation as "Back to Home" menu
                }
            });
        } else {
            Log.e(TAG, "backButton is null!");
        }
        
        // More Options Button
        if (moreOption != null) {
            moreOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMoreOptions(view);
                }
            });
        } else {
            Log.e(TAG, "moreOption is null!");
        }
    }
    
    private void showMoreOptions(View view) {
        try {
            if (customPopupMenu == null) {
                customPopupMenu = new CustomPopupMenu(this, view, "parent");
                customPopupMenu.setOnMenuItemClickListener(title -> {
                    switch (title) {
                        case "Share Application":
                            shareApp();
                            break;
                        case "Rate":
                            rateApp();
                            break;
                        case "Change Login Password":
                            showChangePasswordDialog();
                            break;
                        case "Logout":
                            performLogout();
                            break;
                    }
                    return true;
                });
            }
            
            if (customPopupMenu.isShowing()) {
                customPopupMenu.dismiss();
            } else {
                customPopupMenu.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing popup menu", e);
        }
    }
    
    private void showChangePasswordDialog() {
        startActivity(new Intent(this, PasswordsChange.class)
                .putExtra("User_TYpe", "Parent"));
    }
    
    private void performLogout() {
        logout(); // Use the comprehensive logout method
    }
    
    private void initializeDataAsync() {
        if (!isActivityValid()) return;
        
        executor.execute(() -> {
            try {
                // Initialize Paper DB
                Paper.init(AcademicsDashboard.this);
                
                // Read user data with offline support
                parent_id = Paper.book().read("parent_id");
                campus_id = Paper.book().read("campus_id");
                
                Log.d(TAG, "initializeDataAsync() - parent_id: " + parent_id + ", campus_id: " + campus_id);
                
                // Check if data is available
                if (parent_id == null || campus_id == null) {
                    Log.w(TAG, "User data not found - user may need to login again");
                    mainHandler.post(() -> {
                        if (isActivityValid()) {
                            showErrorWithRetry(getString(R.string.data_load_error), () -> {
                                // Navigate to login
                                Intent intent = new Intent(AcademicsDashboard.this, SelectRole.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });
                        }
                    });
                    return;
                }
                
                // Initialize academics menu items
                initializeAcademicsMenu();
                
                // Update UI on main thread
                mainHandler.post(() -> {
                    if (isActivityValid()) {
                        updateUI();
                        setupRecyclerView();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error in initializeDataAsync", e);
            }
        });
    }
    
    private void initializeAcademicsMenu() {
        try {
            academicsList = new ArrayList<>();
            
            // Academics-related menu items
            academicsList.add(new HomeModel(1, R.drawable.attendence, getString(R.string.attendance), getString(R.string.attendance_description)));
            academicsList.add(new HomeModel(2, R.drawable.progress_report, getString(R.string.progress_reports), getString(R.string.progress_reports_description)));
            academicsList.add(new HomeModel(3, R.drawable.schedule, getString(R.string.date_sheet), getString(R.string.date_sheet_description)));
            academicsList.add(new HomeModel(4, R.drawable.timetablee, getString(R.string.time_table), getString(R.string.time_table_description)));
            // Unified Diary Menu
            academicsList.add(new HomeModel(7, R.drawable.diary, getString(R.string.diary), getString(R.string.diary_description)));
            academicsList.add(new HomeModel(5, R.drawable.ic_home, getString(R.string.back_to_home), getString(R.string.back_to_home_description)));
            academicsList.add(new HomeModel(6, R.drawable.logout, getString(R.string.logout), getString(R.string.logout_description)));
            
            Log.d(TAG, "initializeAcademicsMenu() - Added " + academicsList.size() + " items to academicsList");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing academics menu", e);
        }
    }
    
    private void setupRecyclerView() {
        try {
            Log.d(TAG, "setupRecyclerView() - academicsList size: " + (academicsList != null ? academicsList.size() : "null"));
            
            // Null safety check for RecyclerView
            if (academicsRecyclerView == null) {
                Log.e(TAG, "RecyclerView is null! Cannot setup.");
                return;
            }
            
            if (academicsList != null && !academicsList.isEmpty()) {
                academicsAdapter = new HomeAdaptorParent(academicsList, this, R.color.parent_primary);
                
                // Performance optimizations (removed deprecated APIs)
                academicsRecyclerView.setHasFixedSize(true);
                academicsRecyclerView.setItemViewCacheSize(10); // Reduced for better memory usage
                
                academicsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                academicsRecyclerView.setAdapter(academicsAdapter);
                
                Log.d(TAG, "RecyclerView setup completed successfully");
            } else {
                Log.e(TAG, "academicsList is null or empty");
                showEmptyState();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up recycler view", e);
        }
    }
    
    @Override
    public void OnMenuCLick(View view, String title) {
        try {
            if (getString(R.string.back_to_home).equals(title)) {
                backToHome();
            } else if (getString(R.string.attendance).equals(title)) {
                startActivity(new Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.AttendanceMenu.class));
            } else if (getString(R.string.progress_reports).equals(title)) {
                startActivity(new Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.Report.class));
            } else if (getString(R.string.date_sheet).equals(title)) {
                startActivity(new Intent(this, StudentDateSheet.class));
            } else if (getString(R.string.time_table).equals(title)) {
                startActivity(new Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.ModernStudentTimeTable.class));
            } else if (getString(R.string.diary).equals(title)) {
                startActivity(new Intent(this, ParentDiaryMenu.class));
            } else if (getString(R.string.logout).equals(title)) {
                logout();
            } else {
                Toast.makeText(this, getString(R.string.feature_coming_soon, title), Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling academics menu click", e);
            Toast.makeText(this, getString(R.string.error_opening_feature, title), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String appUrl = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
            String shareMessage = getString(R.string.share_app_message, appUrl);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_chooser)));
        } catch (Exception e) {
            Log.e(TAG, "Error sharing app", e);
            Toast.makeText(this, getString(R.string.error_sharing_application), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void rateApp() {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
            startActivity(rateIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening rate page", e);
            Toast.makeText(this, getString(R.string.error_opening_rate_page), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void backToHome() {
        try {
            // Navigate back to the main parent dashboard
            Intent intent = new Intent(AcademicsDashboard.this, ParentMainDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "Error going back to home", e);
            Toast.makeText(this, getString(R.string.error_going_back_home), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void logout() {
        try {
            showLoading(true);
            
            // Load parent and campus IDs from Paper
            String parent_id = Paper.book().read("parent_id", "");
            String campus_id = Paper.book().read("campus_id", "");
            
            // Create request body
            java.util.HashMap<String, String> postParam = new java.util.HashMap<>();
            postParam.put("parent_id", parent_id);
            postParam.put("campus_id", campus_id);
            
            String jsonString = new org.json.JSONObject(postParam).toString();
            okhttp3.RequestBody body = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), jsonString);
            
            // Make API call
            topgrade.parent.com.parentseeks.Parent.Utils.API.getAPIService().logout_parent(body).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<okhttp3.ResponseBody> call, @NonNull retrofit2.Response<okhttp3.ResponseBody> response) {
                    showLoading(false);
                    okhttp3.ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        try {
                            // Read response body (can only be called once)
                            String responseString = responseBody.string();
                            Log.d(TAG, "Logout response: " + responseString);
                            
                            // Clear all authentication data
                            Paper.book().write(Constants.is_login, false);
                            Paper.book().delete(Constants.User_Type);
                            Paper.book().delete("parent_id");
                            Paper.book().delete("campus_id");
                            Paper.book().delete("full_name");
                            Paper.book().delete("phone");
                            Paper.book().delete("picture");
                            Paper.book().delete("campus_name");
                            
                            // Navigate to role selection with proper flags
                            Intent intent = new Intent(AcademicsDashboard.this, SelectRole.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing logout response", e);
                            if (!isFinishing()) {
                                showErrorWithRetry(getString(R.string.logout_failed), () -> logout());
                            }
                        } finally {
                            responseBody.close();
                        }
                    } else {
                        if (!isFinishing()) {
                            String errorMessage = response.raw().message();
                            String message = errorMessage != null && !errorMessage.isEmpty() ? errorMessage : getString(R.string.logout_failed);
                            showErrorWithRetry(message, () -> logout());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                    Log.e(TAG, "Logout failed", t);
                    showLoading(false);
                    if (!isFinishing()) {
                        showErrorWithRetry(getString(R.string.logout_failed) + ": " + t.getMessage(), () -> logout());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            showLoading(false);
            if (!isFinishing()) {
                showErrorWithRetry(getString(R.string.error_during_logout), this::clearLocalDataAndLogout);
            }
        }
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private void showErrorWithRetry(String message, Runnable retryAction) {
        // DISABLED: No retry dialogs - just continue loading automatically
        Log.w(TAG, "Retry dialog disabled - continuing automatic loading: " + message);
        
        // Automatically retry after a short delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                try {
                    if (retryAction != null) {
                        retryAction.run();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in auto-retry action", e);
                }
            }
        }, 3000); // 3 second delay before auto-retry
    }
    
    private void showEmptyState() {
        try {
            // Show a friendly message when no data is available
            Toast.makeText(this, getString(R.string.no_academics_data), Toast.LENGTH_LONG).show();
            Log.d(TAG, "Showing empty state message");
        } catch (Exception e) {
            Log.e(TAG, "Error showing empty state", e);
        }
    }
    
    private void clearLocalDataAndLogout() {
        try {
            // Clear all authentication data even if logout API fails
            Paper.book().write(Constants.is_login, false);
            Paper.book().delete(Constants.User_Type);
            Paper.book().delete("parent_id");
            Paper.book().delete("campus_id");
            Paper.book().delete("full_name");
            Paper.book().delete("phone");
            Paper.book().delete("picture");
            Paper.book().delete("campus_name");
            
            Log.d(TAG, "Local data cleared due to logout failure");
            
            // Navigate to role selection
            Intent intent = new Intent(AcademicsDashboard.this, SelectRole.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing local data", e);
        }
    }
    
    private void updateUI() {
        try {
            // Get dynamic title from intent
            String dashboardTitleText = getIntent().getStringExtra("DASHBOARD_TITLE");
            Log.d(TAG, "updateUI() - dashboardTitle: " + (dashboardTitle != null ? "not null" : "null"));
            Log.d(TAG, "updateUI() - dashboardTitleText from intent: " + dashboardTitleText);
            
            if (dashboardTitle != null) {
                if (dashboardTitleText != null && !dashboardTitleText.isEmpty()) {
                    dashboardTitle.setText(dashboardTitleText);
                    Log.d(TAG, "Header title set from intent: " + dashboardTitleText);
                } else {
                    dashboardTitle.setText(getString(R.string.academics_dashboard));
                    Log.d(TAG, "Header title set to default: Academics Dashboard");
                }
            } else {
                Log.e(TAG, "dashboardTitle is null! Cannot set title.");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
        }
    }
    
    private boolean isActivityValid() {
        return !isActivityDestroyed && !isFinishing() && !isActivityPaused;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        isActivityPaused = true;
        
        // Don't hide progress bar in onPause() as it might cancel important operations
        // Let the operation completion handle progress bar visibility
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        isActivityPaused = false;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;
        
        // Unregister activity from memory leak detection
        MemoryLeakDetector.unregisterActivity(this);
        
        try {
            // Shutdown executor to prevent memory leaks
            if (!executor.isShutdown()) {
                executor.shutdownNow(); // Direct shutdown for lightweight tasks
                Log.d(TAG, "Executor shutdown completed");
            }
            
            // Check for memory leaks before destroying
            MemoryLeakDetector.checkMemoryLeaks(this);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }
    
    /**
     * Apply theme based on user type
     */
    private void applyTheme() {
        try {
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d(TAG, "User Type: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Set system bar colors for student theme
                try {
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.student_primary));
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.student_primary));
                } catch (Exception e) {
                    Log.e(TAG, "Error setting system bar colors for student theme", e);
                }
                
                Log.d(TAG, "Student theme applied successfully");
            } else {
                // Apply unified parent theme for academics dashboard
                ParentThemeHelper.applyParentTheme(this, 140); // 140dp for dashboard pages
                ParentThemeHelper.setHeaderIconVisibility(this, true); // Show icon for dashboards
                ParentThemeHelper.setMoreOptionsVisibility(this, true); // Show more options for dashboards
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Child Academics");
                
                Log.d(TAG, "Parent theme applied successfully - UserType: " + userType);
            }
            
            // Apply footer theming based on user type
            ThemeHelper.applyFooterTheme(this, userType != null ? userType : "");
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme", e);
            // Fallback: apply parent theme if there's an error
            try {
                ThemeHelper.applyParentTheme(this);
                Log.d(TAG, "Fallback parent theme applied due to error");
            } catch (Exception fallbackError) {
                Log.e(TAG, "Error applying fallback theme", fallbackError);
            }
        }
    }
    
    /**
     * Setup window insets to handle footer margins like parent login screen
     * This ensures the footer is visible above the navigation bar, not hidden behind it
     */
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            if (rootLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                        // Extend footer card to bottom of screen and add padding inside for navigation bar
                        // This fills the navigation bar area with dark_brown color, eliminating white gap
                        LinearLayout footerContainer = findViewById(R.id.footer_container);
                        CardView footerCard = null;
                        
                        // Find the CardView inside footer_container
                        if (footerContainer != null && footerContainer.getChildCount() > 0) {
                            android.view.View child = footerContainer.getChildAt(0);
                            if (child instanceof CardView) {
                                footerCard = (CardView) child;
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
                                    Log.d(TAG, "Footer card padding set to: " + bottomPadding + "px");
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
                            Log.w(TAG, "footer_card not found - cannot set padding");
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding
                        return WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in window insets listener: " + e.getMessage(), e);
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
                
                // Force initial application of insets
                ViewCompat.requestApplyInsets(rootLayout);
            } else {
                Log.e(TAG, "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets", e);
        }
    }
}