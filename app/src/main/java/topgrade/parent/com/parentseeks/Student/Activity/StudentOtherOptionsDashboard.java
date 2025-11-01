package topgrade.parent.com.parentseeks.Student.Activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Adaptor.HomeAdaptorStudent;
import topgrade.parent.com.parentseeks.Teacher.Interface.OnMenuCLick;
import topgrade.parent.com.parentseeks.Parent.Model.HomeModel;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Utils.BaseMainDashboard;
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;
import topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.MemoryLeakDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentOtherOptionsDashboard extends BaseMainDashboard implements OnMenuCLick {

    private static final String TAG = "StudentOtherOptionsDashboard";

    // UI Components
    private RecyclerView otherOptionsRecyclerView;
    private ImageView backButton;
    private ProgressBar progressBar;
    private TextView headerTitle;
    private DrawerLayout drawer;
    private CircleImageView pic;
    private TextView name;
    private TextView location;
    private TextView version;

    // Data
    private String student_id;
    private String campus_id;
    private List<HomeModel> otherOptionsList;
    private HomeAdaptorStudent otherOptionsAdapter;

    // Lifecycle management
    private boolean isActivityDestroyed = false;
    private boolean isActivityPaused = false;
    
    // Background operations
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Custom popup menu
    private CustomPopupMenu customPopupMenu;

    // Implement required abstract methods from BaseMainDashboard
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_student_other_options_dashboard;
    }

    @Override
    protected int getPrimaryColor() {
        return R.color.student_primary;
    }

    @Override
    protected String getUserType() {
        return "student";
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() - StudentOtherOptionsDashboard activity started");
        
        // Register activity for memory leak detection
        MemoryLeakDetector.registerActivity(this);
        
        try {
            // Apply anti-flickering flags
            ActivityTransitionHelper.applyAntiFlickeringFlags(this);
            ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white);
            
            setContentView(R.layout.activity_student_other_options_dashboard);
            
            // Initialize views
            initializeViews();
            
            // Set dynamic title from intent (IMMEDIATE - like parent logic)
            String dashboardTitleText = getIntent().getStringExtra("DASHBOARD_TITLE");
            if (headerTitle != null) {
                if (dashboardTitleText != null && !dashboardTitleText.isEmpty()) {
                    headerTitle.setText(dashboardTitleText);
                    Log.d(TAG, "Header title set from intent: " + dashboardTitleText);
                } else {
                    headerTitle.setText("More Options");
                    Log.d(TAG, "Header title set to default: More Options");
                }
            } else {
                Log.e(TAG, "headerTitle is null in onCreate!");
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
            
            // Delay RecyclerView setup to ensure it's fully initialized
            otherOptionsRecyclerView.post(() -> {
                setupRecyclerView();
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing other options dashboard", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initializeViews() {
        try {
            progressBar = findViewById(R.id.progress_bar);
            headerTitle = findViewById(R.id.header_title);
            otherOptionsRecyclerView = findViewById(R.id.other_options_recycler_view);
            
            // Setup back button
            backButton = findViewById(R.id.back_button);
            
            // Initialize navigation drawer views
            drawer = findViewById(R.id.drawer_layout);
            pic = findViewById(R.id.pic);
            name = findViewById(R.id.name);
            location = findViewById(R.id.location);
            version = findViewById(R.id.version);

            // Set version text
            if (version != null) {
                version.setText("Version " + BuildConfig.VERSION_NAME);
            }

            // Setup Drawer
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }
            
            if (drawer != null && toolbar != null) {
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close
                );
                drawer.addDrawerListener(toggle);
                toggle.syncState();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void initializeData() {
        try {
            // Initialize Paper DB
            Paper.init(this);
            
            // Initialize other options menu items
            initializeOtherOptionsMenu();
            
            Log.d(TAG, "Data initialization completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing data", e);
        }
    }

    private void setupBackButton() {
        try {
            if (backButton != null) {
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up back button", e);
        }
    }

    private void setupDrawer() {
        try {
            if (drawer != null) {
                // Drawer is already set up in initializeViews
                Log.d(TAG, "Drawer setup completed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up drawer", e);
        }
    }

    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
     */
    protected void setupWindowInsets() {
        try {
            View rootLayout = findViewById(R.id.drawer_layout);
            if (rootLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        int bottomPadding = systemInsets.bottom > 20 ? systemInsets.bottom : 0;
                        view.setPadding(0, 0, 0, bottomPadding);
                        return WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        return insets;
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets", e);
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
    }
    
    private void updateUI(String name) {
        try {
            // Get dynamic title from intent
            String dashboardTitleText = getIntent().getStringExtra("DASHBOARD_TITLE");
            Log.d(TAG, "updateUI() - headerTitle: " + (headerTitle != null ? "not null" : "null"));
            Log.d(TAG, "updateUI() - dashboardTitleText from intent: " + dashboardTitleText);
            
            if (headerTitle != null) {
                if (dashboardTitleText != null && !dashboardTitleText.isEmpty()) {
                    headerTitle.setText(dashboardTitleText);
                    Log.d(TAG, "Header title set from intent: " + dashboardTitleText);
                } else {
                    headerTitle.setText("Utilities");
                    Log.d(TAG, "Header title set to default: Utilities");
                }
            } else {
                Log.e(TAG, "headerTitle is null! Cannot set title.");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
        }
    }

    private void setupRecyclerView() {
        try {
            Log.d(TAG, "setupRecyclerView() - otherOptionsList size: " + (otherOptionsList != null ? otherOptionsList.size() : "null"));
            Log.d(TAG, "setupRecyclerView() - otherOptionsRecyclerView: " + (otherOptionsRecyclerView != null ? "found" : "null"));
            
            if (otherOptionsList != null && !otherOptionsList.isEmpty() && otherOptionsRecyclerView != null) {
                // Ensure RecyclerView is properly initialized
                otherOptionsRecyclerView.setHasFixedSize(true);
                otherOptionsRecyclerView.setNestedScrollingEnabled(false);
                
                otherOptionsAdapter = new HomeAdaptorStudent(otherOptionsList, this, R.color.student_primary);
                otherOptionsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                otherOptionsRecyclerView.setAdapter(otherOptionsAdapter);
                
                // Force layout to ensure adapter is attached
                otherOptionsRecyclerView.post(() -> {
                    if (otherOptionsAdapter != null) {
                        otherOptionsAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Adapter attached and data set changed");
                    }
                });
                
                Log.d(TAG, "RecyclerView setup completed successfully");
            } else {
                Log.e(TAG, "otherOptionsList is null or empty OR otherOptionsRecyclerView is null");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up recycler view", e);
        }
    }
    
    private boolean isActivityValid() {
        return !isActivityDestroyed && !isActivityPaused;
    }

    private void initializeDataAsync() {
        if (!isActivityValid()) return;
        
        executor.execute(() -> {
            try {
                // Initialize Paper DB
                Paper.init(StudentOtherOptionsDashboard.this);
                
                // Read user data
                String name_str = Paper.book().read("full_name", "");
                student_id = Paper.book().read("student_id");
                campus_id = Paper.book().read("campus_id");
                
                Log.d(TAG, "initializeDataAsync() - student_id: " + student_id + ", campus_id: " + campus_id);
                
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

    private void loadIdsInBackground() {
        executor.execute(() -> {
            try {
                // Load student and campus IDs from Paper
                student_id = Paper.book().read("student_id", "");
                campus_id = Paper.book().read("campus_id", "");
                
                Log.d(TAG, "Loaded IDs - Student: " + student_id + ", Campus: " + campus_id);
                
                // Update UI on main thread
                mainHandler.post(() -> {
                    try {
                        // Load user profile data
                        loadUserProfileData();
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating UI after loading IDs", e);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading IDs in background", e);
            }
        });
    }

    private void loadUserProfileData() {
        try {
            // Load user data from Paper
            String userName = Paper.book().read("student_name", "Student");
            String userPhone = Paper.book().read("student_phone", "Phone");
            String userPicture = Paper.book().read("student_picture", "");
            
            // Update UI
            if (name != null) {
                name.setText(userName);
            }
            if (location != null) {
                location.setText(userPhone);
            }
            
            // Load profile picture
            if (pic != null && !userPicture.isEmpty()) {
                Glide.with(this)
                    .load(userPicture)
                    .placeholder(R.drawable.man)
                    .error(R.drawable.man)
                    .into(pic);
            } else if (pic != null) {
                pic.setImageResource(R.drawable.man);
            }
            
            Log.d(TAG, "User profile data loaded successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading user profile data", e);
        }
    }



    private void showChangePasswordDialog() {
        try {
            Intent intent = new Intent(this, PasswordsChange.class);
            intent.putExtra("User_TYpe", "STUDENT");
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening change password dialog", e);
            Toast.makeText(this, "Error opening change password", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAboutDialog() {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("About Topgrade Software App")
                .setMessage("Version: " + BuildConfig.VERSION_NAME + "\n\nA comprehensive school management application for students, parents, and staff.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing about dialog", e);
        }
    }


    


    protected void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnMenuCLick(View view, String title) {
        try {
            handleMenuClick(title);
        } catch (Exception e) {
            Log.e(TAG, "Error handling menu click", e);
        }
    }

    private void handleMenuClick(String title) {
        try {
            switch (title) {
                case "Settings":
                    openAppSettings();
                    break;
                case "Help":
                    openHelpDocumentation();
                    break;
                case "About":
                    showAppInfo();
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
                case "Back to Home":
                    backToHome();
                    break;
                case "Logout":
                    logout();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling menu click", e);
        }
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Topgrade Software App - Student");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing student management app!");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {
            Log.e(TAG, "Error sharing app", e);
        }
    }

    private void rateApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Log.e(TAG, "Play Store not found, using web browser", e);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening Play Store", e);
            showError("Unable to open Play Store");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Maintain student theme (teal)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.student_primary));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
                 // Shutdown executor
         if (executor != null && !executor.isShutdown()) {
             executor.shutdown();
         }
     }
     
     /**
      * Navigate back to the main student dashboard
      */
     private void backToHome() {
         try {
             Intent intent = new Intent(this, StudentMainDashboard.class);
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
             startActivity(intent);
             finish();
         } catch (Exception e) {
             Log.e(TAG, "Error going back to home", e);
             Toast.makeText(this, "Error going back to home", Toast.LENGTH_SHORT).show();
         }
     }
     
         /**
     * Logout and navigate to role selection
     */
    protected void logout() {
        try {
            Log.d(TAG, "=== LOGOUT DEBUG ===");
            Log.d(TAG, "Starting logout process...");
            
            showLoading(true);
            
            // Load student and campus IDs from Paper
            String student_id = Paper.book().read("student_id", "");
            String campus_id = Paper.book().read("campus_id", "");
            
            Log.d(TAG, "Logout - student_id: " + student_id + ", campus_id: " + campus_id);
            
            // Create request body
            java.util.HashMap<String, String> postParam = new java.util.HashMap<>();
            postParam.put("student_id", student_id);
            postParam.put("campus_id", campus_id);
            
            String jsonString = new org.json.JSONObject(postParam).toString();
            Log.d(TAG, "Logout request body: " + jsonString);
            
            okhttp3.RequestBody body = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), jsonString);
            
            // Make API call
            topgrade.parent.com.parentseeks.Parent.Utils.API.getAPIService().logout_student(body).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                    Log.d(TAG, "Logout API response received - Code: " + response.code());
                    showLoading(false);
                    
                    // Always clear local data regardless of server response (like LogoutManager.kt)
                    Log.d(TAG, "Clearing local authentication data...");
                    
                    // Clear all authentication data
                    Paper.book().write(Constants.is_login, false);
                    Paper.book().delete(Constants.User_Type);
                    Paper.book().delete("student_id");
                    Paper.book().delete("campus_id");
                    Paper.book().delete("student_name");
                    Paper.book().delete("student_phone");
                    Paper.book().delete("student_picture");
                    Paper.book().delete("campus_name");
                    
                    Log.d(TAG, "All authentication data cleared");
                    Log.d(TAG, "Paper DB after logout - is_login: " + Paper.book().read(Constants.is_login, false));
                    Log.d(TAG, "Paper DB after logout - User_Type: " + Paper.book().read(Constants.User_Type, "NOT_FOUND"));
                    
                    // Navigate to role selection with proper flags
                    Intent intent = new Intent(StudentOtherOptionsDashboard.this, topgrade.parent.com.parentseeks.Parent.Activity.SelectRole.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Logout API call failed", t);
                    showLoading(false);
                    
                    // Clear local data even if server logout fails (like LogoutManager.kt)
                    Log.d(TAG, "Server logout failed, clearing local data anyway...");
                    
                    // Clear all authentication data
                    Paper.book().write(Constants.is_login, false);
                    Paper.book().delete(Constants.User_Type);
                    Paper.book().delete("student_id");
                    Paper.book().delete("campus_id");
                    Paper.book().delete("student_name");
                    Paper.book().delete("student_phone");
                    Paper.book().delete("student_picture");
                    Paper.book().delete("campus_name");
                    
                    Log.d(TAG, "Local authentication data cleared despite server failure");
                    
                    // Navigate to role selection with proper flags
                    Intent intent = new Intent(StudentOtherOptionsDashboard.this, topgrade.parent.com.parentseeks.Parent.Activity.SelectRole.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            showError("Error during logout");
            showLoading(false);
        }
    }

    /**
     * Open App Settings - copied from main login screen (SelectRole)
     */
    private void openAppSettings() {
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
    private void openHelpDocumentation() {
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
    private void showAppInfo() {
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
} 