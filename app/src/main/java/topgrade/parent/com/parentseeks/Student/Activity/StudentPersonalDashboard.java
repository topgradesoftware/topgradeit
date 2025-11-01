package topgrade.parent.com.parentseeks.Student.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Adaptor.HomeAdaptorStudent;
import topgrade.parent.com.parentseeks.Parent.Activity.ParentFeedback;
import topgrade.parent.com.parentseeks.Parent.Activity.ChildList;
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange;
import topgrade.parent.com.parentseeks.Parent.Activity.ParentProfile;
import topgrade.parent.com.parentseeks.Parent.Activity.FeeChalan;
import topgrade.parent.com.parentseeks.Parent.Activity.SelectRole;
import topgrade.parent.com.parentseeks.Parent.Model.HomeModel;
import topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.MemoryLeakDetector;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Interface.OnMenuCLick;
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;

public class StudentPersonalDashboard extends AppCompatActivity implements OnMenuCLick {

    private static final String TAG = "StudentPersonalDashboard";

    // UI Components
    private RecyclerView personalRecyclerView;
    private ImageView backButton;
    private ImageView moreOption;
    private ProgressBar progressBar;
    private TextView dashboardTitle;

    // Data
    private Context context;
    private String parent_id;
    private String campus_id;
    private List<HomeModel> personalList;
    private HomeAdaptorStudent personalAdapter;
    
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
        Log.d(TAG, "onCreate() - StudentPersonalDashboard activity started");
        
        // Register activity for memory leak detection
        MemoryLeakDetector.registerActivity(this);
        
        try {
            // Apply anti-flickering flags
            ActivityTransitionHelper.applyAntiFlickeringFlags(this);
            
            setContentView(R.layout.activity_student_personal_dashboard);
            
            // Apply unified student theme
            StudentThemeHelper.applyStudentTheme(this, 140); // 140dp for dashboard pages
            StudentThemeHelper.setHeaderIconVisibility(this, true); // Show icon for dashboards
            StudentThemeHelper.setMoreOptionsVisibility(this, true); // Show more options for dashboards
            StudentThemeHelper.setFooterVisibility(this, true); // Show footer
            StudentThemeHelper.setHeaderTitle(this, "Personal Dashboard");
            
            // Initialize context
            context = this;
            
            // Initialize views
            initializeViews();
            
            // Set dynamic title from intent (IMMEDIATE - like parent logic)
            String dashboardTitleText = getIntent().getStringExtra("DASHBOARD_TITLE");
            if (dashboardTitle != null) {
                if (dashboardTitleText != null && !dashboardTitleText.isEmpty()) {
                    dashboardTitle.setText(dashboardTitleText);
                    Log.d(TAG, "Header title set from intent: " + dashboardTitleText);
                } else {
                    dashboardTitle.setText("Personal Dashboard");
                    Log.d(TAG, "Header title set to default: Personal Dashboard");
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
            initializePersonalMenu();
            setupRecyclerView();
            
            // Apply navigation bar theme
            applyNavigationBarTheme();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing personal dashboard", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initializeViews() {
        try {
            personalRecyclerView = findViewById(R.id.home_rcv);
            backButton = findViewById(R.id.back_button);
            moreOption = findViewById(R.id.more_option);
            progressBar = findViewById(R.id.content_progress_bar);
            dashboardTitle = findViewById(R.id.header_title);
            
            Log.d(TAG, "initializeViews() - dashboardTitle: " + (dashboardTitle != null ? "found" : "not found"));
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }
    
    private void applyNavigationBarTheme() {
        try {
            // Get user type from Paper DB
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d(TAG, "=== ANDROID SYSTEM NAVIGATION BAR THEME DEBUG ===");
            Log.d(TAG, "User Type from Paper: '" + userType + "'");
            
            // Apply theme to Android system navigation bar
            int navigationBarColor;
            if (userType != null && userType.equals("STUDENT")) {
                navigationBarColor = getResources().getColor(R.color.teal);
                Log.d(TAG, "Applying teal theme to Android system navigation bar");
            } else {
                navigationBarColor = getResources().getColor(R.color.dark_brown);
                Log.d(TAG, "Applying dark brown theme to Android system navigation bar");
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
                
                Log.d(TAG, "Android system navigation bar color set to: " + navigationBarColor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying navigation bar theme", e);
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
                customPopupMenu = new CustomPopupMenu(this, view, "student");
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
                .putExtra("User_TYpe", "Student"));
    }
    
    private void performLogout() {
        logout(); // Use the comprehensive logout method
    }
    
    private void initializeDataAsync() {
        if (!isActivityValid()) return;
        
        executor.execute(() -> {
            try {
                // Initialize Paper DB
                Paper.init(StudentPersonalDashboard.this);
                
                // Read user data (same as parent)
                String name_str = Paper.book().read("full_name", "");
                parent_id = Paper.book().read("parent_id");
                campus_id = Paper.book().read("campus_id");
                
                Log.d(TAG, "initializeDataAsync() - parent_id: " + parent_id + ", campus_id: " + campus_id);
                
                // Initialize personal menu items
                initializePersonalMenu();
                
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

    private void setupRecyclerView() {
        try {
            Log.d(TAG, "setupRecyclerView() - personalList size: " + (personalList != null ? personalList.size() : "null"));
            Log.d(TAG, "setupRecyclerView() - personalRecyclerView: " + (personalRecyclerView != null ? "found" : "null"));
            
            if (personalList != null && !personalList.isEmpty()) {
                personalAdapter = new HomeAdaptorStudent(personalList, this, R.color.student_primary);
                personalRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                personalRecyclerView.setAdapter(personalAdapter);
                
                Log.d(TAG, "RecyclerView setup completed successfully");
            } else {
                Log.e(TAG, "personalList is null or empty");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up recycler view", e);
        }
    }

    private void initializePersonalMenu() {
        try {
            personalList = new ArrayList<>();
            // Student personal dashboard features (same as parent)
            personalList.add(new HomeModel(1, R.drawable.man, "Parent Profile", "View Profile"));
            personalList.add(new HomeModel(2, R.drawable.children, "Child List", "View Children"));
            personalList.add(new HomeModel(3, R.drawable.chalan, "Fee Challan", "View Challan"));
            personalList.add(new HomeModel(4, R.drawable.feedback, "Remarks By Teachers", "View Teacher Remarks"));
            personalList.add(new HomeModel(5, R.drawable.ic_home, "Back to Home", "Go Home"));
            personalList.add(new HomeModel(6, R.drawable.logout, "Logout", "Sign Out"));
            
            Log.d(TAG, "Personal menu initialized with " + personalList.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing personal menu", e);
        }
    }







    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
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
                    dashboardTitle.setText("Personal Dashboard");
                    Log.d(TAG, "Header title set to default: Personal Dashboard");
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
    
    protected void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnMenuCLick(View view, String title) {
        try {
            switch (title) {
                case "Back to Home":
                    backToHome();
                    break;
                case "Parent Profile":
                    startActivity(new Intent(StudentPersonalDashboard.this, ParentProfile.class));
                    break;
                case "Child List":
                    startActivity(new Intent(StudentPersonalDashboard.this, ChildList.class));
                    break;
                case "Fee Challan":
                    startActivity(new Intent(StudentPersonalDashboard.this, FeeChalan.class));
                    break;
                case "Remarks By Teachers":
                    startActivity(new Intent(StudentPersonalDashboard.this, ParentFeedback.class));
                    break;
                case "Logout":
                    logout();
                    break;
                default:
                    Toast.makeText(this, "Feature coming soon: " + title, Toast.LENGTH_SHORT).show();
                    break;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling personal menu click", e);
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
        
        // Reapply navigation bar theme in case it was overridden
        getWindow().getDecorView().postDelayed(() -> {
            applyNavigationBarTheme();
        }, 50); // Small delay to ensure it overrides system settings
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
    
    private void backToHome() {
        try {
            // Navigate back to the main student dashboard
            Intent intent = new Intent(StudentPersonalDashboard.this, StudentMainDashboard.class);
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
            
            // Load student and campus IDs from Paper (correct for student context)
            String student_id = Paper.book().read("student_id", "");
            String campus_id = Paper.book().read("campus_id", "");
            
            Log.d(TAG, "Logout - student_id: " + student_id + ", campus_id: " + campus_id);
            
            // Create request body
            java.util.HashMap<String, String> postParam = new java.util.HashMap<>();
            postParam.put("student_id", student_id);
            postParam.put("campus_id", campus_id);
            
            String jsonString = new org.json.JSONObject(postParam).toString();
            okhttp3.RequestBody body = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), jsonString);
            
            // Make API call
            topgrade.parent.com.parentseeks.Parent.Utils.API.getAPIService().logout_student(body).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                    Log.d(TAG, "Logout API response received - Code: " + response.code());
                    showLoading(false);
                    
                    // Always clear local data regardless of server response
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
                    Intent intent = new Intent(StudentPersonalDashboard.this, SelectRole.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Logout API failed", t);
                    showLoading(false);
                    
                    // Even if API fails, clear local data and logout
                    Log.d(TAG, "API failed, but clearing local data anyway...");
                    
                    // Clear all authentication data
                    Paper.book().write(Constants.is_login, false);
                    Paper.book().delete(Constants.User_Type);
                    Paper.book().delete("student_id");
                    Paper.book().delete("campus_id");
                    Paper.book().delete("student_name");
                    Paper.book().delete("student_phone");
                    Paper.book().delete("student_picture");
                    Paper.book().delete("campus_name");
                    
                    // Navigate to role selection with proper flags
                    Intent intent = new Intent(StudentPersonalDashboard.this, SelectRole.class);
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
    

} 