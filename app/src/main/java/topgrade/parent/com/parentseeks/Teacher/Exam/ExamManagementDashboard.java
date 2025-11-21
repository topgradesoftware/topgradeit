package topgrade.parent.com.parentseeks.Teacher.Exam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class ExamManagementDashboard extends AppCompatActivity {

    private static final String TAG = "ExamManagementDashboard";
    
    // Header elements
    private ImageView backIcon;
    private TextView headerTitle;

    // Main sections
    private CardView progressReportCard;
    private CardView examMenuCard;
    private CardView examSelectionCard;
    private CardView studentMarksCard;

    // Progress report button
    private MaterialButton seeProgressReportButton;

    // Exam menu buttons
    private MaterialButton submitExamButton;
    private MaterialButton updateExamButton;
    private MaterialButton viewResultsButton;

    // Exam selection elements
    private LinearLayout examSelector;
    private TextView showAdvancedFilter;

    // Student marks elements
    private TextView attendanceDate;
    private TextView totalRecords;

    // Bottom navigation
    private LinearLayout bottomNavigation;
    private MaterialButton submitMarksButton;
    private MaterialButton backToMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() - ExamManagementDashboard started");
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_exam_management_dashboard);
        
        // Configure status bar and navigation bar
        setupSystemBars();
        
        // Setup window insets to respect system bars
        setupWindowInsets();

        // Initialize Paper DB and load constants
        initializeData();
        
        initViews();
        setupListeners();
        showMainMenu();
    }
    
    /**
     * Setup status bar and navigation bar configuration
     */
    private void setupSystemBars() {
        // Configure status bar for navy blue background with white icons
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
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the footer is visible above the navigation bar, not hidden behind it
     */
    private void setupWindowInsets() {
        try {
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
                            android.view.ViewGroup.LayoutParams layoutParams = footerContainer.getLayoutParams();
                            if (layoutParams instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = 
                                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) layoutParams;
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                                Log.d(TAG, "Footer bottom margin set to: " + bottomMargin + " (ConstraintLayout)");
                            } else if (layoutParams instanceof android.view.ViewGroup.MarginLayoutParams) {
                                android.view.ViewGroup.MarginLayoutParams params = 
                                    (android.view.ViewGroup.MarginLayoutParams) layoutParams;
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                                Log.d(TAG, "Footer bottom margin set to: " + bottomMargin + " (MarginLayoutParams)");
                            }
                        } else {
                            Log.w(TAG, "footer_container not found - cannot set bottom margin");
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in window insets listener: " + e.getMessage(), e);
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
                
                // Force initial application of insets
                androidx.core.view.ViewCompat.requestApplyInsets(rootLayout);
            } else {
                Log.e(TAG, "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize data and load constants from Paper DB
     */
    private void initializeData() {
        try {
            // Initialize Paper DB
            Paper.init(this);
            
            // Load constants from Paper DB
            Constant.loadFromPaper();
            
            Log.d(TAG, "=== EXAM UNIFIED DATA INITIALIZATION ===");
            Log.d(TAG, "staff_id: " + Constant.staff_id);
            Log.d(TAG, "campus_id: " + Constant.campus_id);
            Log.d(TAG, "current_session: " + Constant.current_session);
            Log.d(TAG, "parent_id: " + Constant.parent_id);
            Log.d(TAG, "==========================================");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing data", e);
        }
    }

    private void initViews() {
        // Header
        backIcon = findViewById(R.id.back_icon);
        headerTitle = findViewById(R.id.header_title);

        // Main sections
        progressReportCard = findViewById(R.id.progress_report_card);
        examMenuCard = findViewById(R.id.exam_menu_card);
        examSelectionCard = findViewById(R.id.exam_selection_card);
        studentMarksCard = findViewById(R.id.student_marks_card);

        // Progress report button
        seeProgressReportButton = findViewById(R.id.see_progress_report_button);

        // Exam menu buttons
        submitExamButton = findViewById(R.id.submit_exam_button);
        updateExamButton = findViewById(R.id.update_exam_button);
        viewResultsButton = findViewById(R.id.view_results_button);

        // Exam selection elements
        examSelector = findViewById(R.id.exam_selector);
        showAdvancedFilter = findViewById(R.id.show_advanced_filter);

        // Student marks elements
        attendanceDate = findViewById(R.id.attendence_date);
        totalRecords = findViewById(R.id.total_records);

        // Bottom navigation
        bottomNavigation = findViewById(R.id.bottom_navigation);
        submitMarksButton = findViewById(R.id.Submit_Marks);
        backToMenuButton = findViewById(R.id.back_to_menu_button);
    }

    private void setupListeners() {
        // ic_arrow_back
        backIcon.setOnClickListener(v -> finish());

        // Exam menu buttons
        submitExamButton.setOnClickListener(v -> {
            Log.d(TAG, "Submit Exam button clicked");
            if (validateUserData()) {
                Intent intent = new Intent(ExamManagementDashboard.this, ExamSubmit.class);
                intent.putExtra("MODE", "SUBMIT");
                intent.putExtra("LOAD_SUBMITTED_DATA", false);
                startActivity(intent);
            }
        });

        updateExamButton.setOnClickListener(v -> {
            Log.d(TAG, "Update Exam button clicked");
            if (validateUserData()) {
                Intent intent = new Intent(ExamManagementDashboard.this, ExamSubmit.class);
                intent.putExtra("MODE", "UPDATE"); // Indicate this is update mode
                intent.putExtra("LOAD_SUBMITTED_DATA", true); // Load already submitted data
                startActivity(intent);
            }
        });

        // Progress report button
        seeProgressReportButton.setOnClickListener(v -> {
            Log.d(TAG, "See Your Progress Report button clicked");
            if (validateUserData()) {
                startActivity(new Intent(ExamManagementDashboard.this, topgrade.parent.com.parentseeks.Teacher.Activites.Activity.StaffProgress.class));
            }
        });

        viewResultsButton.setOnClickListener(v -> {
            Log.d(TAG, "View Results button clicked");
            if (validateUserData()) {
                Intent intent = new Intent(ExamManagementDashboard.this, ExamSubmit.class);
                intent.putExtra("MODE", "VIEW_RESULTS"); // Indicate this is view results mode
                intent.putExtra("LOAD_SUBMITTED_DATA", true); // Load submitted data for viewing
                startActivity(intent);
            }
        });

        // Exam selector
        examSelector.setOnClickListener(v -> {
            // TODO: Show exam selection dialog
            showAdvancedFilter.setText("Mid Term Exam");
            showStudentMarks();
        });

        // Bottom navigation buttons
        submitMarksButton.setOnClickListener(v -> {
            Log.d(TAG, "Submit Marks button clicked");
            if (validateUserData()) {
                Intent intent = new Intent(ExamManagementDashboard.this, ExamSubmit.class);
                intent.putExtra("MODE", "SUBMIT"); // Indicate this is submit mode
                intent.putExtra("LOAD_SUBMITTED_DATA", false); // Don't load submitted data
                startActivity(intent);
            }
        });

        backToMenuButton.setOnClickListener(v -> {
            showMainMenu();
        });
    }

    private void showMainMenu() {
        progressReportCard.setVisibility(View.VISIBLE);
        examMenuCard.setVisibility(View.VISIBLE);
        examSelectionCard.setVisibility(View.GONE);
        studentMarksCard.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.GONE);
        headerTitle.setText("Exam Management");
    }

    private void showExamSelection(String role) {
        progressReportCard.setVisibility(View.GONE);
        examMenuCard.setVisibility(View.GONE);
        examSelectionCard.setVisibility(View.VISIBLE);
        studentMarksCard.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.GONE);
        headerTitle.setText("Select Exam - " + role);
    }

    private void showStudentMarks() {
        progressReportCard.setVisibility(View.GONE);
        examMenuCard.setVisibility(View.GONE);
        examSelectionCard.setVisibility(View.GONE);
        studentMarksCard.setVisibility(View.VISIBLE);
        bottomNavigation.setVisibility(View.VISIBLE);
        headerTitle.setText("Student Marks");
        
        // Show bottom navigation buttons
        submitMarksButton.setVisibility(View.VISIBLE);
        backToMenuButton.setVisibility(View.VISIBLE);
    }
    
    /**
     * Validate that user data is properly loaded
     */
    private boolean validateUserData() {
        try {
            if (Constant.staff_id == null || Constant.staff_id.isEmpty()) {
                Log.w(TAG, "staff_id is empty - user may need to login again");
                showErrorDialog("User data not found. Please login again.");
                return false;
            }
            
            if (Constant.campus_id == null || Constant.campus_id.isEmpty()) {
                Log.w(TAG, "campus_id is empty - user may need to login again");
                showErrorDialog("Campus data not found. Please login again.");
                return false;
            }
            
            Log.d(TAG, "User data validation passed");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating user data", e);
            showErrorDialog("Error validating user data. Please try again.");
            return false;
        }
    }
    
    /**
     * Show error dialog to user
     */
    private void showErrorDialog(String message) {
        try {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing error dialog", e);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() - refreshing constants");
        
        // Refresh constants when returning to activity
        try {
            Constant.loadFromPaper();
            Log.d(TAG, "Constants refreshed on resume");
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing constants on resume", e);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() - ExamManagementDashboard destroyed");
    }
}
