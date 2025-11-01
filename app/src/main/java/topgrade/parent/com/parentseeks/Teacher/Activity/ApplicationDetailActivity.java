package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class ApplicationDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "ApplicationDetail";
    
    // UI Components
    private TextView headerTitle;
    private TextView applicationStatus;
    private TextView applicationDate;
    private TextView applicationTitle;
    private TextView dateRange;
    private TextView applicationBody;
    private MaterialButton editButton;
    private MaterialButton deleteButton;
    private ProgressBar progressBar;
    
    // Data
    private StaffApplicationModel.Application application;
    private String applicationId;
    private Context context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_application_detail);
        
        // Configure status bar for navy blue background with white icons
        setupStatusBar();
        
        // Initialize context and Paper
        context = this;
        Paper.init(context);
        
        // Get application ID from intent
        applicationId = getIntent().getStringExtra("APPLICATION_ID");
        if (applicationId == null || applicationId.isEmpty()) {
            Log.e(TAG, "Application ID not provided in intent");
            Toast.makeText(this, "Application ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        initializeViews();
        
        // Setup click listeners
        setupClickListeners();
        
        // Load application details
        loadApplicationDetails();
    }
    
    private void setupStatusBar() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
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
    }
    
    private void initializeViews() {
        // Initialize header title
        headerTitle = findViewById(R.id.header_title);
        if (headerTitle != null) {
            // Set dynamic title from intent or use default
            String titleText = getIntent().getStringExtra("HEADER_TITLE");
            if (titleText != null && !titleText.isEmpty()) {
                headerTitle.setText(titleText);
                Log.d(TAG, "Header title set from intent: " + titleText);
            } else {
                headerTitle.setText("Application Details");
                Log.d(TAG, "Header title set to default: Application Details");
            }
        } else {
            Log.e(TAG, "header_title not found in layout!");
        }
        
        // Initialize other views
        applicationStatus = findViewById(R.id.application_status);
        applicationDate = findViewById(R.id.application_date);
        applicationTitle = findViewById(R.id.application_title);
        dateRange = findViewById(R.id.date_range);
        applicationBody = findViewById(R.id.application_body);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);
        progressBar = findViewById(R.id.progress_bar);
        
        // Setup back button
        View backIcon = findViewById(R.id.back_icon);
        if (backIcon != null) {
            backIcon.setOnClickListener(v -> finish());
        }
    }
    
    private void setupClickListeners() {
        if (editButton != null) {
            editButton.setOnClickListener(v -> {
                // TODO: Implement edit functionality
                Toast.makeText(this, "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> {
                // TODO: Implement delete functionality
                Toast.makeText(this, "Delete functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void loadApplicationDetails() {
        // Check if constants are properly initialized
        if (Constant.staff_id == null || Constant.staff_id.isEmpty()) {
            Log.e(TAG, "staff_id is null or empty!");
            Toast.makeText(this, "Staff ID not found. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (Constant.campus_id == null || Constant.campus_id.isEmpty()) {
            Log.e(TAG, "campus_id is null or empty!");
            Toast.makeText(this, "Campus ID not found. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }
        
        showLoading(true);
        
        // Create API request to get application details
        String jsonBody = String.format(
            "{\"operation\":\"read_application_title\",\"campus_id\":\"%s\",\"staff_id\":\"%s\"}",
            Constant.campus_id,
            Constant.staff_id
        );
        
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody);
        Call<StaffApplicationModel> call = Constant.mApiService.leave_applicaton(requestBody);
        
        call.enqueue(new Callback<StaffApplicationModel>() {
            @Override
            public void onResponse(Call<StaffApplicationModel> call, Response<StaffApplicationModel> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    StaffApplicationModel result = response.body();
                    if (result.getStatus() != null && result.getStatus().getCode().equals("1000")) {
                        // Find the specific application by ID
                        if (result.getApplications() != null) {
                            for (StaffApplicationModel.Application app : result.getApplications()) {
                                if (applicationId.equals(app.getTitleId())) {
                                    application = app;
                                    displayApplicationDetails();
                                    return;
                                }
                            }
                        }
                        // Application not found
                        Toast.makeText(context, "Application not found", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // API error
                        Toast.makeText(context, result.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // HTTP error
                    Toast.makeText(context, "Failed to load application details", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<StaffApplicationModel> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error loading application details", t);
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayApplicationDetails() {
        if (application == null) return;
        
        // Set application title
        if (applicationTitle != null) {
            String title = application.getTitle();
            if (title != null && !title.isEmpty()) {
                applicationTitle.setText(title);
            } else {
                applicationTitle.setText("Leave Application");
            }
        }
        
        // Set application status
        if (applicationStatus != null) {
            applicationStatus.setText("Pending"); // You might want to get this from API
        }
        
        // Set application date
        if (applicationDate != null) {
            // You might want to get the submission date from API
            applicationDate.setText("2025-01-15"); // Placeholder
        }
        
        // Set date range
        if (dateRange != null) {
            String startDate = application.getStartDate();
            String endDate = application.getEndDate();
            if (startDate != null && endDate != null) {
                if (startDate.equals(endDate)) {
                    dateRange.setText(startDate);
                } else {
                    dateRange.setText(startDate + " to " + endDate);
                }
            } else {
                dateRange.setText("Date not specified");
            }
        }
        
        // Set application body
        if (applicationBody != null) {
            String body = application.getBody();
            if (body != null && !body.isEmpty()) {
                applicationBody.setText(body);
            } else {
                applicationBody.setText("No description provided");
            }
        }
        
        Log.d(TAG, "Application details displayed successfully");
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
