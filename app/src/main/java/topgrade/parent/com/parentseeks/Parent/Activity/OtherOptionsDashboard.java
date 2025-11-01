package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Context;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange;
import topgrade.parent.com.parentseeks.Parent.Activity.ParentMainDashboard;
import topgrade.parent.com.parentseeks.Parent.Activity.SelectRole;

public class OtherOptionsDashboard extends AppCompatActivity implements OnMenuCLick {
    
    private static final String TAG = "OtherOptionsDashboard";
    
    // UI Components
    private RecyclerView otherOptionsRecyclerView;
    private ImageView backButton;
    private ImageView moreOption;
    private ProgressBar progressBar;
    private TextView dashboardTitle;
    
    // Data
    private Context context;
    private String parent_id;
    private String campus_id;
    private List<HomeModel> otherOptionsList;
    private HomeAdaptorParent otherOptionsAdapter;
    
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
        Log.d(TAG, "onCreate() - OtherOptionsDashboard activity started");
        
        // Register activity for memory leak detection
        MemoryLeakDetector.registerActivity(this);
        
        try {
            // Apply anti-flickering flags
            ActivityTransitionHelper.applyAntiFlickeringFlags(this);
            ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white);
            
            setContentView(R.layout.activity_parent_other_options_dashboard);
            
            // Apply parent theme
            applyTheme();
            
            // Initialize views
            initializeViews();
            
            // Set dynamic title from intent (IMMEDIATE - like staff logic)
            String dashboardTitleText = getIntent().getStringExtra("DASHBOARD_TITLE");
            if (dashboardTitle != null) {
                if (dashboardTitleText != null && !dashboardTitleText.isEmpty()) {
                    dashboardTitle.setText(dashboardTitleText);
                    Log.d(TAG, "Header title set from intent: " + dashboardTitleText);
                } else {
                    dashboardTitle.setText("Utilities");
                    Log.d(TAG, "Header title set to default: Utilities");
                }
            } else {
                Log.e(TAG, "dashboardTitle is null in onCreate!");
            }
            
            // Setup click listeners
            setupClickListeners();
            
            // Initialize data in background
            mainHandler.postDelayed(() -> {
                if (!isActivityDestroyed) {
                    initializeDataAsync();
                }
            }, 50);
            
            // Also initialize immediately for testing
            initializeOtherOptionsMenu();
            setupRecyclerView();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing other options dashboard", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initializeViews() {
        try {
            otherOptionsRecyclerView = findViewById(R.id.other_options_recycler_view);
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
        // Back Button
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (isActivityValid()) {
                    Log.d(TAG, "Back button clicked");
                    finish();
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
                Paper.init(OtherOptionsDashboard.this);
                
                // Read user data
                String name_str = Paper.book().read("full_name", "");
                parent_id = Paper.book().read("parent_id");
                campus_id = Paper.book().read("campus_id");
                
                Log.d(TAG, "initializeDataAsync() - parent_id: " + parent_id + ", campus_id: " + campus_id);
                
                // Initialize other options menu items
                initializeOtherOptionsMenu();
                
                // Update UI on main thread
                mainHandler.post(() -> {
                    if (isActivityValid()) {
                        updateUI(name_str);
                        setupRecyclerView();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error in initializeDataAsync", e);
            }
        });
    }
    
    private void initializeOtherOptionsMenu() {
        try {
            otherOptionsList = new ArrayList<>();
            
            // Other options menu items - Standardized with 8 cards for consistency
            otherOptionsList.add(new HomeModel(1, R.drawable.ic_settings, "Settings", "App configuration"));
            otherOptionsList.add(new HomeModel(2, R.drawable.ic_help, "Help", "User support"));
            otherOptionsList.add(new HomeModel(3, R.drawable.ic_info, "About", "App information"));
            otherOptionsList.add(new HomeModel(4, R.drawable.share, "Share App", "Share with friends"));
            otherOptionsList.add(new HomeModel(5, R.drawable.rate, "Rate App", "Rate our app"));
            otherOptionsList.add(new HomeModel(6, R.drawable.key, "Change Password", "Update password"));
            otherOptionsList.add(new HomeModel(7, R.drawable.ic_home, "Back to Home", "Go Home"));
            otherOptionsList.add(new HomeModel(8, R.drawable.logout, "Logout", "Sign Out"));
            
            Log.d(TAG, "initializeOtherOptionsMenu() - Added " + otherOptionsList.size() + " items to otherOptionsList");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing other options menu", e);
        }
    }
    
    private void setupRecyclerView() {
        try {
            Log.d(TAG, "setupRecyclerView() - otherOptionsList size: " + (otherOptionsList != null ? otherOptionsList.size() : "null"));
            
            if (otherOptionsList != null && !otherOptionsList.isEmpty()) {
                otherOptionsAdapter = new HomeAdaptorParent(otherOptionsList, this, R.color.parent_primary);
                otherOptionsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                otherOptionsRecyclerView.setAdapter(otherOptionsAdapter);
                
                Log.d(TAG, "RecyclerView setup completed successfully");
            } else {
                Log.e(TAG, "otherOptionsList is null or empty");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up recycler view", e);
        }
    }
    
    @Override
    public void OnMenuCLick(View view, String title) {
        try {
            switch (title) {
                case "Settings":
                    showSettings();
                    break;
                case "Help":
                    showHelp();
                    break;
                case "About":
                    showAbout();
                    break;
                case "Back to Home":
                    backToHome();
                    break;
                case "Share App":
                    shareApp();
                    break;
                case "Rate App":
                    rateApp();
                    break;
                case "Change Password":
                    showChangePasswordDialog();
                    break;
                case "Logout":
                    logout();
                    break;
                default:
                    Toast.makeText(this, "Feature coming soon: " + title, Toast.LENGTH_SHORT).show();
                    break;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling other options menu click", e);
            Toast.makeText(this, "Error opening " + title, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage = "\nLet me recommend you this application to view student attendance, fee Challan and Reports.\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            Log.e(TAG, "Error sharing app", e);
            Toast.makeText(this, "Error sharing application", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void rateApp() {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
            startActivity(rateIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening rate page", e);
            Toast.makeText(this, "Error opening rate page", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void backToHome() {
        try {
            // Navigate back to the main parent dashboard
            Intent intent = new Intent(OtherOptionsDashboard.this, ParentMainDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "Error going back to home", e);
            Toast.makeText(this, "Error going back to home", Toast.LENGTH_SHORT).show();
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
                public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                    showLoading(false);
                    if (response.body() != null) {
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
                        Intent intent = new Intent(OtherOptionsDashboard.this, SelectRole.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        showError(response.raw().message() != null ? response.raw().message() : "Logout failed");
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Logout failed", t);
                    showLoading(false);
                    showError("Logout failed: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            showError("Error during logout");
            showLoading(false);
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
    

    
    private void updateUI(String name) {
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
                    dashboardTitle.setText("Utilities");
                    Log.d(TAG, "Header title set to default: Utilities");
                }
            } else {
                Log.e(TAG, "dashboardTitle is null! Cannot set title.");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
        }
    }
    
    private boolean isActivityValid() {
        return !isActivityDestroyed && !isFinishing() && !isActivityPaused && context != null;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        isActivityPaused = true;
        
        if (progressBar != null && !isActivityDestroyed) {
            progressBar.setVisibility(View.GONE);
        }
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
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.SECONDS);
            }
            
            // Clear context reference
            context = null;
            
            // Check for memory leaks before destroying
            MemoryLeakDetector.checkMemoryLeaks(this);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }
    
    /**
     * Open App Settings - copied from main login screen (SelectRole)
     */
    private void showSettings() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening app settings", e);
            Toast.makeText(this, "Error opening settings", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Open Help Documentation - copied from main login screen (SelectRole)
     */
    private void showHelp() {
        try {
            // Directly open the TopGrade Software help and support page
            openUrl("https://topgradesoftware.com/help_support_form.php");
        } catch (Exception e) {
            Log.e(TAG, "Error opening help documentation", e);
            Toast.makeText(this, "Error opening help documentation", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show App Info - copied from main login screen (SelectRole)
     */
    private void showAbout() {
        try {
            android.content.pm.PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            
            String message = "Version: " + versionName + " (" + versionCode + ")\n\n" +
                           "Developed by Top Grade Software\n\n" +
                           "Contact Information:\n" +
                           "📱 WhatsApp: +923006616622\n" +
                           "📧 Email: support@topgradesoftware.com\n" +
                           "🌐 Website: https://topgradesoftware.com\n\n" +
                           "For support and inquiries, please contact us through any of the above channels.";
            
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("About " + getString(R.string.app_name))
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing app info", e);
            Toast.makeText(this, "Error showing app info", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to open URLs - copied from main login screen (SelectRole)
     */
    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            intent.setPackage("com.android.chrome");
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
            } catch (Exception e2) {
                Log.e(TAG, "Error opening URL: " + url, e2);
                Toast.makeText(this, "Error opening link", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening URL: " + url, e);
            Toast.makeText(this, "Error opening link", Toast.LENGTH_SHORT).show();
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
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.student_primary));
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.student_primary));
                }
                
                Log.d(TAG, "Student theme applied successfully");
            } else {
                // Apply unified parent theme for other options dashboard
                ParentThemeHelper.applyParentTheme(this, 140); // 140dp for dashboard pages
                ParentThemeHelper.setHeaderIconVisibility(this, true); // Show icon for dashboards
                ParentThemeHelper.setMoreOptionsVisibility(this, true); // Show more options for dashboards
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "More Options");
                
                Log.d(TAG, "Parent theme applied successfully - UserType: " + userType);
            }
            
            // Apply footer theming based on user type
            ThemeHelper.applyFooterTheme(this, userType);
            
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
}
