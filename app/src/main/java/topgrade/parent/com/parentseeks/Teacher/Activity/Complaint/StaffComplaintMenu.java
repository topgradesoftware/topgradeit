package topgrade.parent.com.parentseeks.Teacher.Activity.Complaint;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffComplaintMenu extends AppCompatActivity {

    private static final String TAG = "StaffComplaintMenu";
    
    private Context context;
    private MaterialButton btnSubmitComplaint, btnAllComplaints, btnPendingComplaints, btnUnderDiscussionComplaints, btnSolvedComplaints;
    private TextView badgeAllComplaints, badgePendingComplaints, badgeUnderDiscussionComplaints, badgeSolvedComplaints;
    
    private int allCount = 0;
    private int pendingCount = 0;
    private int underDiscussionCount = 0;
    private int solvedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_complaint_menu);
        
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

        // Additional fix for older Android versions - Force white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Ensure status bar icons are light (white) on dark background
            getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility() & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        
        // Setup window insets
        setupWindowInsets();
        
        // Initialize Paper database
        Paper.init(this);
        
        // Load constants from Paper
        Constant.loadFromPaper();
        
        // Initialize views
        btnSubmitComplaint = findViewById(R.id.btn_submit_complaint);
        btnAllComplaints = findViewById(R.id.btn_all_complaints);
        btnPendingComplaints = findViewById(R.id.btn_pending_complaints);
        btnUnderDiscussionComplaints = findViewById(R.id.btn_under_discussion_complaints);
        btnSolvedComplaints = findViewById(R.id.btn_solved_complaints);
        
        // Initialize badges
        badgeAllComplaints = findViewById(R.id.badge_all_complaints);
        badgePendingComplaints = findViewById(R.id.badge_pending_complaints);
        badgeUnderDiscussionComplaints = findViewById(R.id.badge_under_discussion_complaints);
        badgeSolvedComplaints = findViewById(R.id.badge_solved_complaints);
        
        // Set click listeners
        btnSubmitComplaint.setOnClickListener(v -> openSubmitComplaint());
        btnAllComplaints.setOnClickListener(v -> openComplaintList("all"));
        btnPendingComplaints.setOnClickListener(v -> openComplaintList("pending"));
        btnUnderDiscussionComplaints.setOnClickListener(v -> openComplaintList("under_discussion"));
        btnSolvedComplaints.setOnClickListener(v -> openComplaintList("solved"));
        
        // Initialize back button
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());
        
        // Load complaint counts
        loadComplaintCounts();
    }
    
    /**
     * Setup window insets to respect system bars - EXACT COPY FROM WORKING StaffApplicationMenu
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
                        android.view.View footerContainer = findViewById(R.id.footer_container);
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
                        Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e(TAG, "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
        }
    }
    
    /**
     * Open submit complaint page
     */
    private void openSubmitComplaint() {
        Intent intent = new Intent(this, StaffSubmitComplaint.class);
        startActivity(intent);
    }
    
    /**
     * Open complaint list with specific filter
     */
    private void openComplaintList(String filterType) {
        Intent intent = new Intent(this, StaffComplaintList.class);
        intent.putExtra("filter_type", filterType);
        startActivity(intent);
    }
    
    /**
     * Load complaint counts from API (using mock data for now)
     */
    private void loadComplaintCounts() {
        try {
            // Use mock data for now
            updateBadgeCountsWithMockData();
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading complaint counts: " + e.getMessage());
        }
    }
    
    /**
     * Update badge counts with mock data
     */
    private void updateBadgeCountsWithMockData() {
        try {
            // Set mock counts
            allCount = 5;
            pendingCount = 2;
            underDiscussionCount = 1;
            solvedCount = 2;
            
            // Update UI on main thread
            runOnUiThread(() -> {
                badgeAllComplaints.setText(String.valueOf(allCount));
                badgePendingComplaints.setText(String.valueOf(pendingCount));
                badgeUnderDiscussionComplaints.setText(String.valueOf(underDiscussionCount));
                badgeSolvedComplaints.setText(String.valueOf(solvedCount));
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating badge counts: " + e.getMessage());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload counts when returning to this screen
        loadComplaintCounts();
    }
}
