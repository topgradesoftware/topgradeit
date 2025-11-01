package topgrade.parent.com.parentseeks.Teacher.Activity.Complaint;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Teacher.Model.ComplaintModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.R;

import java.util.ArrayList;
import java.util.List;

import components.searchablespinnerlibrary.SearchableSpinner;

public class StaffSubmitComplaint extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StaffSubmitComplaint";

    private EditText complaint_subject;
    private EditText complaint_description;
    private SearchableSpinner complaint_category;
    private SearchableSpinner complaint_priority;
    private Button submit_complaint_btn;
    private ProgressBar progress_bar;
    private ImageView back_icon;
    private Context context;

    private List<String> categoryList = new ArrayList<>();
    private List<String> priorityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_submit_complaint);
        
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

        context = StaffSubmitComplaint.this;
        Paper.init(context);
        
        // Load constants from Paper database
        Constant.loadFromPaper();
        
        // Initialize views
        initViews();
        
        // Setup spinners
        setupSpinners();
        
        // Set click listeners
        setClickListeners();
    }
    
    /**
     * Setup window insets to respect system bars - EXACT COPY FROM WORKING StaffAddApplication
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

                        // Add bottom margin to submit button card to push it above navigation bar
                        android.view.View submitCard = findViewById(R.id.submit_complaint_card);
                        if (submitCard != null) {
                            // Set bottom margin to navigation bar height to ensure submit button is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) submitCard.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                submitCard.setLayoutParams(params);
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
        complaint_subject = findViewById(R.id.complaint_subject);
        complaint_description = findViewById(R.id.complaint_description);
        complaint_category = findViewById(R.id.complaint_category);
        complaint_priority = findViewById(R.id.complaint_priority);
        submit_complaint_btn = findViewById(R.id.submit_complaint_btn);
        progress_bar = findViewById(R.id.progress_bar);
        back_icon = findViewById(R.id.back_icon);
    }
    
    private void setupSpinners() {
        // Setup complaint categories
        categoryList.add("General");
        categoryList.add("Academic");
        categoryList.add("Administrative");
        categoryList.add("Technical");
        categoryList.add("Facility");
        categoryList.add("Other");
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complaint_category.setAdapter(categoryAdapter);
        
        // Setup priority levels
        priorityList.add("Low");
        priorityList.add("Medium");
        priorityList.add("High");
        priorityList.add("Urgent");
        
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorityList);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complaint_priority.setAdapter(priorityAdapter);
    }
    
    private void setClickListeners() {
        submit_complaint_btn.setOnClickListener(this);
        back_icon.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.submit_complaint_btn) {
            submitComplaint();
        } else if (v.getId() == R.id.back_icon) {
            finish();
        }
    }
    
    private void submitComplaint() {
        // Validate inputs
        if (complaint_subject.getText().toString().trim().isEmpty()) {
            complaint_subject.setError("Please enter complaint subject");
            complaint_subject.requestFocus();
            return;
        }
        
        if (complaint_description.getText().toString().trim().isEmpty()) {
            complaint_description.setError("Please enter complaint description");
            complaint_description.requestFocus();
            return;
        }
        
        if (complaint_category.getSelectedItemPosition() == 0) {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (complaint_priority.getSelectedItemPosition() == 0) {
            Toast.makeText(context, "Please select a priority", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress bar
        progress_bar.setVisibility(View.VISIBLE);
        submit_complaint_btn.setEnabled(false);
        
        try {
            // Get staff details from Paper
            String staffId = Paper.book().read("staff_id", "");
            String campusId = Paper.book().read("campus_id", "");
            
            if (staffId.isEmpty() || campusId.isEmpty()) {
                Toast.makeText(context, "Staff information not found", Toast.LENGTH_SHORT).show();
                hideProgressBar();
                return;
            }
            
            // Create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("staff_id", staffId);
            requestBody.put("campus_id", campusId);
            requestBody.put("session_id", Constant.current_session);
            requestBody.put("subject", complaint_subject.getText().toString().trim());
            requestBody.put("description", complaint_description.getText().toString().trim());
            requestBody.put("category", complaint_category.getSelectedItem().toString());
            requestBody.put("priority", complaint_priority.getSelectedItem().toString());
            requestBody.put("status", "pending");
            
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                requestBody.toString()
            );
            
            // Make API call
            BaseApiService apiService = API.getAPIService();
            Call<topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel> call = apiService.complain(body);
            
            call.enqueue(new Callback<topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel>() {
                @Override
                public void onResponse(Call<topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel> call, Response<topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel> response) {
                    hideProgressBar();
                    
                    if (response.isSuccessful() && response.body() != null) {
                        topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel staffComplainModel = response.body();
                        if (staffComplainModel.getStatus() != null && "1000".equals(staffComplainModel.getStatus().getCode())) {
                            Toast.makeText(context, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String errorMsg = staffComplainModel.getStatus() != null ? staffComplainModel.getStatus().getMessage() : "Unknown error";
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Failed to submit complaint", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel> call, Throwable t) {
                    hideProgressBar();
                    Log.e(TAG, "Error submitting complaint: " + t.getMessage());
                    Toast.makeText(context, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (JSONException e) {
            hideProgressBar();
            Log.e(TAG, "Error creating request body: " + e.getMessage());
            Toast.makeText(context, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void hideProgressBar() {
        progress_bar.setVisibility(View.GONE);
        submit_complaint_btn.setEnabled(true);
    }
}