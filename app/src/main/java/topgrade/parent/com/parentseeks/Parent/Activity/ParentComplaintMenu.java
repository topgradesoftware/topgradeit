package topgrade.parent.com.parentseeks.Parent.Activity;

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
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class ParentComplaintMenu extends AppCompatActivity {

    private static final String TAG = "ParentComplaintMenu";
    
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
        
        setContentView(R.layout.activity_parent_complaint_menu);
        
        // Configure status bar for dark brown background with white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
            
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
    
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            
            if (rootLayout != null) {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars()
                        );

                        android.view.View footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                            }
                        }
                        
                        view.setPadding(0, 0, 0, 0);
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
    
    private void openSubmitComplaint() {
        Intent intent = new Intent(this, ParentSubmitComplaint.class);
        startActivity(intent);
    }
    
    private void openComplaintList(String filterType) {
        Intent intent = new Intent(this, ParentComplaintList.class);
        intent.putExtra("filter_type", filterType);
        startActivity(intent);
    }
    
    private void loadComplaintCounts() {
        try {
            // Get parent details from Paper
            String parentId = Paper.book().read("parent_id", "");
            String campusId = Paper.book().read("campus_id", "");
            
            if (parentId.isEmpty() || campusId.isEmpty()) {
                Log.e(TAG, "Parent ID or Campus ID not found");
                updateBadgeCountsWithDefaultData();
                return;
            }
            
            // Create request body for read_complain operation
            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("operation", "read_complain");
            requestBody.put("parent_id", parentId);
            requestBody.put("campus_id", campusId);
            requestBody.put("filter_type", "all");
            
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                new JSONObject(requestBody).toString()
            );
            
            // Make API call
            topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService apiService =
                topgrade.parent.com.parentseeks.Parent.Utils.API.getAPIService();
            
            Call<topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel> call = apiService.parent_complain(body);
            call.enqueue(new Callback<topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel>() {
                @Override
                public void onResponse(Call<topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel> call, Response<topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel model = response.body();
                        if (model.getStatus() != null && "1000".equals(model.getStatus().getCode())) {
                            if (model.getData() != null) {
                                updateBadgeCountsFromData(model.getData());
                            } else {
                                updateBadgeCountsWithDefaultData();
                            }
                        } else {
                            updateBadgeCountsWithDefaultData();
                        }
                    } else {
                        updateBadgeCountsWithDefaultData();
                    }
                }
                
                @Override
                public void onFailure(Call<topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel> call, Throwable t) {
                    Log.e(TAG, "Failed to load complaint counts: " + t.getMessage());
                    updateBadgeCountsWithDefaultData();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading complaint counts: " + e.getMessage());
            updateBadgeCountsWithDefaultData();
        }
    }
    
    private void updateBadgeCountsFromData(List<topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel.Complaint> complaints) {
        try {
            // Count complaints by status
            allCount = complaints.size();
            pendingCount = 0;
            underDiscussionCount = 0;
            solvedCount = 0;
            
            for (topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel.Complaint complaint : complaints) {
                String status = complaint.getComplaintStatus().toLowerCase();
                if (status.contains("pending")) {
                    pendingCount++;
                } else if (status.contains("discussion") || status.contains("progress")) {
                    underDiscussionCount++;
                } else if (status.contains("solved") || status.contains("resolved")) {
                    solvedCount++;
                }
            }
            
            // Update UI on main thread
            runOnUiThread(() -> {
                badgeAllComplaints.setText(String.valueOf(allCount));
                badgePendingComplaints.setText(String.valueOf(pendingCount));
                badgeUnderDiscussionComplaints.setText(String.valueOf(underDiscussionCount));
                badgeSolvedComplaints.setText(String.valueOf(solvedCount));
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating badge counts from data: " + e.getMessage());
            updateBadgeCountsWithDefaultData();
        }
    }
    
    private void updateBadgeCountsWithDefaultData() {
        try {
            // Set default counts (0 for all)
            allCount = 0;
            pendingCount = 0;
            underDiscussionCount = 0;
            solvedCount = 0;
            
            // Update UI on main thread
            runOnUiThread(() -> {
                badgeAllComplaints.setText(String.valueOf(allCount));
                badgePendingComplaints.setText(String.valueOf(pendingCount));
                badgeUnderDiscussionComplaints.setText(String.valueOf(underDiscussionCount));
                badgeSolvedComplaints.setText(String.valueOf(solvedCount));
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating default badge counts: " + e.getMessage());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadComplaintCounts();
    }
}

