package topgrade.parent.com.parentseeks.Teacher.Activity.Application;

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

public class StaffApplicationMenu extends AppCompatActivity {

    private static final String TAG = "StaffApplicationMenu";
    
    private Context context;
    private MaterialButton btnSubmitApplication, btnAllApplications, btnPendingApplications, btnApprovedApplications, btnRejectedApplications;
    private TextView badgeAllApplications, badgePendingApplications, badgeApprovedApplications, badgeRejectedApplications;
    
    private int allCount = 0;
    private int pendingCount = 0;
    private int approvedCount = 0;
    private int rejectedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_leave_application_menu);
        
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
        
        // Force light status bar icons and dark navigation bar icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0,
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        // Setup window insets
        setupWindowInsets();
        
        context = StaffApplicationMenu.this;
        Paper.init(context);
        
        // Load constants from Paper database
        Constant.loadFromPaper();
        
        initViews();
        setupClickListeners();
        
        // Load application counts
        loadApplicationCounts();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload counts when returning to this screen
        loadApplicationCounts();
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
    
    private void initViews() {
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());
        
        btnSubmitApplication = findViewById(R.id.btn_submit_application);
        btnAllApplications = findViewById(R.id.btn_all_applications);
        btnPendingApplications = findViewById(R.id.btn_pending_applications);
        btnApprovedApplications = findViewById(R.id.btn_approved_applications);
        btnRejectedApplications = findViewById(R.id.btn_rejected_applications);
        
        badgeAllApplications = findViewById(R.id.badge_all_applications);
        badgePendingApplications = findViewById(R.id.badge_pending_applications);
        badgeApprovedApplications = findViewById(R.id.badge_approved_applications);
        badgeRejectedApplications = findViewById(R.id.badge_rejected_applications);
    }
    
    private void setupClickListeners() {
        btnSubmitApplication.setOnClickListener(v -> openSubmitApplication());
        btnAllApplications.setOnClickListener(v -> openApplicationList(Constant.FILTER_LEAVE_ALL));
        btnPendingApplications.setOnClickListener(v -> openApplicationList(Constant.FILTER_LEAVE_PENDING));
        btnApprovedApplications.setOnClickListener(v -> openApplicationList(Constant.FILTER_LEAVE_APPROVED));
        btnRejectedApplications.setOnClickListener(v -> openApplicationList(Constant.FILTER_LEAVE_REJECTED));
    }
    
    private void openSubmitApplication() {
        Intent intent = new Intent(context, StaffAddApplication.class);
        intent.putExtra("HEADER_TITLE", "Submit Leave Application");
        startActivity(intent);
    }
    
    private void openApplicationList(String filterType) {
        Log.d(TAG, "=== openApplicationList() DEBUG ===");
        Log.d(TAG, "Opening StaffApplicationList with filterType: '" + filterType + "'");
        Log.d(TAG, "filterType length: " + (filterType != null ? filterType.length() : "null"));
        
        Intent intent = new Intent(context, StaffApplicationList.class);
        intent.putExtra("filter_type", filterType);
        
        Log.d(TAG, "Intent created and extra added");
        Log.d(TAG, "Intent has filter_type extra: " + intent.hasExtra("filter_type"));
        Log.d(TAG, "Intent filter_type value: '" + intent.getStringExtra("filter_type") + "'");
        
        startActivity(intent);
        Log.d(TAG, "Activity started");
    }
    
    private void loadApplicationCounts() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Loading application counts...");
        }
        
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("operation", "read_application_title");
        postParam.put(Constant.KEY_STAFF_ID, Constant.staff_id);
        postParam.put(Constant.KEY_CAMPUS_ID, Constant.campus_id);
        
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());
        
        Constant.mApiService.leave_applicaton(body).enqueue(new Callback<topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel>() {
            @Override
            public void onResponse(Call<topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel> call, 
                                 Response<topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel model = response.body();
                    
                    if (model.getStatus() != null && "1000".equals(model.getStatus().getCode())) {
                        List<topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel.Application> applications = model.getApplications();
                        
                        if (applications != null && !applications.isEmpty()) {
                            categorizeApplications(applications);
                            updateBadges();
                        } else {
                            resetCounts();
                            updateBadges();
                        }
                    } else {
                        resetCounts();
                        updateBadges();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel> call, Throwable t) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Failed to load applications: " + t.getMessage());
                }
                resetCounts();
                updateBadges();
            }
        });
    }
    
    private void categorizeApplications(List<topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel.Application> applications) {
        resetCounts();
        
        for (topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel.Application app : applications) {
            String isActive = app.getIsActive();
            
            allCount++;
            
            // Categorize based on is_active status
            // Assuming: "1" = Approved, "0" = Pending, "2" = Rejected
            if ("1".equals(isActive)) {
                approvedCount++;
            } else if ("2".equals(isActive)) {
                rejectedCount++;
            } else {
                pendingCount++;
            }
        }
        
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "All: " + allCount + ", Pending: " + pendingCount + 
                      ", Approved: " + approvedCount + ", Rejected: " + rejectedCount);
        }
    }
    
    private void resetCounts() {
        allCount = 0;
        pendingCount = 0;
        approvedCount = 0;
        rejectedCount = 0;
    }
    
    private void updateBadges() {
        runOnUiThread(() -> {
            badgeAllApplications.setText(String.valueOf(allCount));
            badgePendingApplications.setText(String.valueOf(pendingCount));
            badgeApprovedApplications.setText(String.valueOf(approvedCount));
            badgeRejectedApplications.setText(String.valueOf(rejectedCount));
        });
    }
}

