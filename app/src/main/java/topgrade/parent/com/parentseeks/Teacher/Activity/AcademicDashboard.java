package topgrade.parent.com.parentseeks.Teacher.Activity;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange;
import topgrade.parent.com.parentseeks.Parent.Activity.SelectRole;
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffMainDashboard;
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard;
import topgrade.parent.com.parentseeks.Teacher.Adapter.StaffDashboardGridAdapter;
import topgrade.parent.com.parentseeks.Parent.Utils.UserType;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

import org.json.JSONObject;

import java.util.HashMap;

public class AcademicDashboard extends AppCompatActivity {

    private static final String TAG = "AcademicDashboard";
    
    // UI Components
    private ProgressBar progress_bar;
    private ImageView more_option, backButton;
    private TextView dashboardTitle;
    
    // Custom popup menu
    private CustomPopupMenu customPopupMenu;
    
    // Lifecycle management
    private boolean isActivityDestroyed = false;
    private boolean isActivityPaused = false;
    
    // Background operations
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // ANR prevention flag
    private boolean isInitialized = false;

    private RecyclerView cardRecyclerView;
    private StaffDashboardGridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_staff_teaching_hub_dashboard);
            
            // Configure status bar for navy blue background with white icons
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
                getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.navy_blue));

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
            
            // Initialize UI components first
            initializeViews();
            
            // Initialize data in background to prevent ANR
            initializeDataInBackground();
            
            isInitialized = true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing dashboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initializeViews() {
        progress_bar = findViewById(R.id.progress_bar);
        more_option = findViewById(R.id.more_option);
        backButton = findViewById(R.id.back_button);
        dashboardTitle = findViewById(R.id.header_title);
        cardRecyclerView = findViewById(R.id.home_rcv);
        
        if (cardRecyclerView == null) {
            Log.e(TAG, "RecyclerView not found! Check if layout contains home_rcv");
        }
        
        // Set dynamic title from intent
        String dashboardTitleText = getIntent().getStringExtra("DASHBOARD_TITLE");
        if (dashboardTitleText != null && !dashboardTitleText.isEmpty() && dashboardTitle != null) {
            dashboardTitle.setText(dashboardTitleText);
            Log.d(TAG, "Header title set from intent: " + dashboardTitleText);
        } else if (dashboardTitle != null) {
            dashboardTitle.setText("Teaching Hub");
            Log.d(TAG, "Header title set to default: Teaching Hub");
        }
    }
    
    private void initializeDataInBackground() {
        // Show progress bar while loading
        if (progress_bar != null) {
            progress_bar.setVisibility(View.VISIBLE);
        }
        
        backgroundExecutor.execute(() -> {
            try {
                // Initialize Paper
                Paper.init(AcademicDashboard.this);
                
                // Update UI on main thread
                mainHandler.post(() -> {
                    try {
                        setupRecyclerView();
                        setupClickListeners();
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting up UI components", e);
                        if (progress_bar != null) {
                            progress_bar.setVisibility(View.GONE);
                        }
                        Toast.makeText(AcademicDashboard.this, "Error setting up dashboard", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error initializing data", e);
                mainHandler.post(() -> {
                    if (progress_bar != null) {
                        progress_bar.setVisibility(View.GONE);
                    }
                    Toast.makeText(AcademicDashboard.this, "Error loading dashboard data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void setupRecyclerView() {
        if (cardRecyclerView != null) {
            // Set up 3-column grid layout
            androidx.recyclerview.widget.GridLayoutManager layoutManager = 
                new androidx.recyclerview.widget.GridLayoutManager(this, 3);
            cardRecyclerView.setLayoutManager(layoutManager);
            
            // Create academic dashboard cards
            java.util.List<topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard> cards = createAcademicCards();
            
            // Set up adapter
            gridAdapter = new topgrade.parent.com.parentseeks.Teacher.Adapter.StaffDashboardGridAdapter(
                this, cards, new topgrade.parent.com.parentseeks.Teacher.Adapter.StaffDashboardGridAdapter.OnCardClickListener() {
                    @Override
                    public void onCardClick(topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard card) {
                        AcademicDashboard.this.onCardClick(card);
                    }
                });
            cardRecyclerView.setAdapter(gridAdapter);
            
            // Optimize RecyclerView performance
            cardRecyclerView.setHasFixedSize(true);
            cardRecyclerView.setItemViewCacheSize(20);
            
            // Hide progress bar after successful setup
            if (progress_bar != null) {
                progress_bar.setVisibility(View.GONE);
            }
            
            Log.d(TAG, "RecyclerView setup completed successfully");
        } else {
            Log.e(TAG, "cardRecyclerView is null - cannot setup RecyclerView");
            if (progress_bar != null) {
                progress_bar.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Error: Could not initialize dashboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    private java.util.List<topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard> createAcademicCards() {
        java.util.List<topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard> cards = new java.util.ArrayList<>();
        
        cards.add(new topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard(
            1, "SharedStudent List", "View SharedStudent Details", R.drawable.student,
            topgrade.parent.com.parentseeks.Teacher.Activity.StaffStudentList.class, null, "student_list"));
            
        cards.add(new topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard(
            2, "Attendance", "Manage Attendance", R.drawable.attendence,
            topgrade.parent.com.parentseeks.Teacher.Activity.Attendance.StaffAttendanceMenu.class, null, "attendance"));
            
        cards.add(new topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard(
            3, "Exam", "Manage Exams", R.drawable.exam,
            topgrade.parent.com.parentseeks.Teacher.Exam.ExamManagementDashboard.class, null, "exam"));
            
        cards.add(new topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard(
            4, "Progress Report", "View Progress Reports", R.drawable.progress_report,
                            topgrade.parent.com.parentseeks.Teacher.Activites.Activity.StaffProgress.class, null, "progress_report"));
            
        cards.add(new topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard(
            5, "Feedback Students", "SharedStudent Feedback", R.drawable.feedback,
            topgrade.parent.com.parentseeks.Teacher.Activity.FeedbackMenu.class, null, "feedback"));
            
        cards.add(new topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard(
            6, "Send Diary", "Class, Section or Subject Diary", R.drawable.diary,
            topgrade.parent.com.parentseeks.Teacher.Diary.DiaryMenu.class, null, "send_diary"));
            
        cards.add(new topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard(
            7, "Back to Home", "Return to Main Menu", R.drawable.ic_home,
            null, new Runnable() {
                @Override
                public void run() {
                    backToHome();
                }
            }, "back_to_home"));
            
        cards.add(new topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard(
            9, "Logout", "Sign Out", R.drawable.logout,
            null, new Runnable() {
                @Override
                public void run() {
                    logout();
                }
            }, "logout"));
        
        return cards;
    }
    
    private void onCardClick(topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard card) {
        if (card.getTargetActivity() != null) {
            startActivity(new Intent(this, card.getTargetActivity()));
        } else if (card.getAction() != null) {
            card.getAction().run();
        }
    }
    
    private void setupClickListeners() {
        if (more_option != null) {
            more_option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pop_uop_menu(view);
                }
            });
        }
        
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    backToHome();
                }
            });
        }
    }

    private void pop_uop_menu(View view) {
        try {
            if (customPopupMenu == null) {
                customPopupMenu = new CustomPopupMenu(this, view, UserType.TEACHER.getValue());
                customPopupMenu.setOnMenuItemClickListener(title -> {
                    switch (title) {
                        case "Share Application":
                            share();
                            break;
                        case "Rate":
                            rate_us();
                            break;
                        case "Change Login Password":
                            chnage_password_dialog();
                            break;
                        case "Logout":
                            logout();
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

    private void logout() {
        try {
            progress_bar.setVisibility(View.VISIBLE);
            
            BaseApiService mApiService = API.getAPIService();
            String staff_id = Paper.book().read("staff_id");
            String campus_id = Paper.book().read("campus_id");
            HashMap<String, String> postParam = new HashMap<String, String>();
            postParam.put("staff_id", staff_id);
            postParam.put("parent_id", campus_id);
            RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));
            
            mApiService.logout_teacher(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    try {
                        progress_bar.setVisibility(View.GONE);
                        
                        if (response.body() != null) {
                            // Clear all authentication data
                            Paper.book().write(Constants.is_login, false);
                            Paper.book().delete(Constants.User_Type);
                            Paper.book().delete("staff_id");
                            Paper.book().delete("campus_id");
                            Paper.book().delete("full_name");
                            Paper.book().delete("phone");
                            Paper.book().delete("picture");
                            Paper.book().delete("campus_name");
                            
                            // Navigate to role selection with consistent flags
                            Intent intent = new Intent(AcademicDashboard.this, SelectRole.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            showError(response.raw().message() != null ? response.raw().message() : "Logout failed");
                        }
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error in logout response", e);
                        progress_bar.setVisibility(View.GONE);
                        showError("Logout error");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        progress_bar.setVisibility(View.GONE);
                        Log.e(TAG, "Logout failed", t);
                        showError("Logout failed: " + t.getMessage());
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error in logout failure", e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in logout", e);
            progress_bar.setVisibility(View.GONE);
            showError("Error during logout");
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void share() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Parent Seeks");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Download Parent Seeks App: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {
            Log.e(TAG, "Error sharing app", e);
        }
    }

    private void rate_us() {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
            startActivity(rateIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error rating app", e);
        }
    }
    
    private void backToHome() {
        try {
            // Navigate back to the main staff dashboard
            Intent intent = new Intent(AcademicDashboard.this, StaffMainDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "Error going back to home", e);
            Toast.makeText(this, "Error going back to home", Toast.LENGTH_SHORT).show();
        }
    }
    
    // Add missing method for password change dialog
    private void chnage_password_dialog() {
        try {
            startActivity(new Intent(AcademicDashboard.this, PasswordsChange.class)
                    .putExtra("User_TYpe", UserType.TEACHER.getValue()));
        } catch (Exception e) {
            Log.e(TAG, "Error opening password change dialog", e);
        }
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the footer won't be hidden behind the navigation bar
     * Optimized approach: Only apply bottom padding to avoid touch interference
     */
    private void setupWindowInsets() {
        android.view.View rootLayout = findViewById(android.R.id.content);
        
        if (rootLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars()
                    );

                    // Only add bottom padding for navigation bar, no other padding to avoid touch interference
                    int bottomPadding = systemInsets.bottom > 20 ? systemInsets.bottom : 0;
                    
                    // Set padding only on the root layout, not affecting child touch events
                    view.setPadding(
                        0, // left - no padding to avoid touch interference
                        0, // top - no padding to avoid touch interference  
                        0, // right - no padding to avoid touch interference
                        bottomPadding // only bottom padding for navigation bar
                    );

                    // Return CONSUMED to prevent child views from getting default padding
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                }
            });
        } else {
            Log.w(TAG, "Root layout not found for window insets setup");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Shutdown background executor
            if (!backgroundExecutor.isShutdown()) {
                backgroundExecutor.shutdown();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }
}
