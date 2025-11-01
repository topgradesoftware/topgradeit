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
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Adaptor.HomeAdaptorStudent;
import topgrade.parent.com.parentseeks.Teacher.Interface.OnMenuCLick;
import topgrade.parent.com.parentseeks.Parent.Model.HomeModel;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Utils.BaseMainDashboard;
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange;
import topgrade.parent.com.parentseeks.Parent.Activity.Report;
import topgrade.parent.com.parentseeks.Parent.Activity.ModernStudentTimeTable;
import topgrade.parent.com.parentseeks.Parent.Activity.StudentDateSheet;
import topgrade.parent.com.parentseeks.Parent.Activity.LoadChildrenDiary;
import topgrade.parent.com.parentseeks.Parent.Activity.ParentFeedback;
import topgrade.parent.com.parentseeks.Teacher.Activity.Application.StaffAddApplication;
import topgrade.parent.com.parentseeks.Parent.Activity.AttendanceMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentAcademicsDashboard extends BaseMainDashboard implements OnMenuCLick {

    private static final String TAG = "StudentAcademicsDashboard";

    // UI Components
    private RecyclerView homeRcv;
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
    private List<HomeModel> academicsList;
    private HomeAdaptorStudent academicsAdapter;

    // Background operations
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Implement required abstract methods from BaseMainDashboard
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_student_academics_dashboard;
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
        Log.d(TAG, "onCreate() - StudentAcademicsDashboard activity started");
        
        try {
            // Initialize views
            initializeViews();
            
            // Set dynamic title from intent (IMMEDIATE - like parent logic)
            String dashboardTitleText = getIntent().getStringExtra("DASHBOARD_TITLE");
            if (headerTitle != null) {
                if (dashboardTitleText != null && !dashboardTitleText.isEmpty()) {
                    headerTitle.setText(dashboardTitleText);
                    Log.d(TAG, "Header title set from intent: " + dashboardTitleText);
                } else {
                    headerTitle.setText("Student Academics");
                    Log.d(TAG, "Header title set to default: Student Academics");
                }
            } else {
                Log.e(TAG, "headerTitle is null in onCreate!");
            }
            
            // Initialize data and setup
            initializeData();
            
            // Delay RecyclerView setup to ensure it's fully initialized
            homeRcv.post(() -> {
                setupRecyclerView();
            });
            
            setupBackButton();
            setupDrawer();
            
            // Load IDs in background
            loadIdsInBackground();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing academics dashboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initializeViews() {
        try {
            progressBar = findViewById(R.id.progress_bar);
            headerTitle = findViewById(R.id.header_title);
            homeRcv = findViewById(R.id.home_rcv);
            
            // Setup back button
            backButton = findViewById(R.id.back_button);
            if (backButton != null) {
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
            
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
            
            // Initialize academics menu items
            initializeAcademicsMenu();
            
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

    private void setupRecyclerView() {
        try {
            Log.d(TAG, "setupRecyclerView() - academicsList size: " + (academicsList != null ? academicsList.size() : "null"));
            Log.d(TAG, "setupRecyclerView() - homeRcv: " + (homeRcv != null ? "found" : "null"));
            
            if (academicsList != null && !academicsList.isEmpty() && homeRcv != null) {
                // Ensure RecyclerView is properly initialized
                homeRcv.setHasFixedSize(true);
                homeRcv.setNestedScrollingEnabled(false);
                homeRcv.setItemViewCacheSize(20);
                
                // Set up 3-column grid layout
                GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
                homeRcv.setLayoutManager(layoutManager);
                
                // Set up adapter with student color theme
                academicsAdapter = new HomeAdaptorStudent(academicsList, this, R.color.student_primary);
                homeRcv.setAdapter(academicsAdapter);
                
                // Force layout to ensure adapter is attached
                homeRcv.post(() -> {
                    if (academicsAdapter != null) {
                        academicsAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Adapter attached and data set changed");
                    }
                });
                
                Log.d(TAG, "Grid layout setup completed successfully");
            } else {
                Log.e(TAG, "academicsList is null or empty OR homeRcv is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up grid layout", e);
        }
    }

    private void initializeAcademicsMenu() {
        try {
            academicsList = new ArrayList<>();
            // Match parent academics menu structure exactly
            academicsList.add(new HomeModel(1, R.drawable.attendence, "Attendance", "View Child Attendance"));
            academicsList.add(new HomeModel(2, R.drawable.progress_report, "Progress Reports", "View Academic Reports"));
            academicsList.add(new HomeModel(3, R.drawable.schedule, "Date Sheet", "View Exam Schedule"));
            academicsList.add(new HomeModel(4, R.drawable.timetablee, "Time Table", "View Class Schedule"));
            academicsList.add(new HomeModel(5, R.drawable.ic_home, "Back to Home", "Go Home"));
            academicsList.add(new HomeModel(6, R.drawable.logout, "Logout", "Sign Out"));
            
            Log.d(TAG, "Academics menu initialized with " + academicsList.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing academics menu", e);
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

    @Override
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
            
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("application/json; charset=utf-8"), jsonString);
            
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
                    Intent intent = new Intent(StudentAcademicsDashboard.this, topgrade.parent.com.parentseeks.Parent.Activity.SelectRole.class);
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
                    Intent intent = new Intent(StudentAcademicsDashboard.this, topgrade.parent.com.parentseeks.Parent.Activity.SelectRole.class);
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

    private void handleMenuClick(String title) {
        try {
            switch (title) {
                case "Back to Home":
                    backToHome();
                    break;
                case "Attendance":
                    startActivity(new Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.AttendanceMenu.class));
                    break;
                case "Progress Reports":
                    startActivity(new Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.Report.class));
                    break;
                case "Date Sheet":
                    startActivity(new Intent(this, StudentDateSheet.class));
                    break;
                case "Time Table":
                    startActivity(new Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.ModernStudentTimeTable.class));
                    break;
                case "Logout":
                    logout();
                    break;
                default:
                    Toast.makeText(this, "Feature coming soon: " + title, Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling academics menu click", e);
            Toast.makeText(this, "Error opening " + title, Toast.LENGTH_SHORT).show();
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

    private void backToHome() {
        try {
            Intent intent = new Intent(this, StudentMainDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error going back to home", e);
            Toast.makeText(this, "Error navigating to home", Toast.LENGTH_SHORT).show();
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
} 