package topgrade.parent.com.parentseeks.Teacher.Activity.Application;

import android.app.DatePickerDialog;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.MenuItem;

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
import topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.Teacher.Utils.Util;
import topgrade.parent.com.parentseeks.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import components.searchablespinnerlibrary.SearchableSpinner;

public class StaffAddApplication extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StaffAddApplication";

    private EditText application_subject;
    private EditText application_body;
    private androidx.cardview.widget.CardView submit_application;
    private com.google.android.material.button.MaterialButton submit_button;
    private ProgressBar progress_bar;
    private Context context;
    
    // Date picker components
    private TextView start_date_text;
    private TextView end_date_text;
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    // Category loading components
    private SearchableSpinner select_application_spinner;
    private ArrayAdapter<String> application_adapter;
    private List<String> category_name_list = new ArrayList<>();
    private String selected_application_category = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_application);
        
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
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();

        initialization();
        
        // Load application categories
        loadApplicationCategories();
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
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

                        // Add bottom margin to submit leave card to push it above navigation bar
                        androidx.cardview.widget.CardView submitCard = findViewById(R.id.submit_leave_card);
                        if (submitCard != null) {
                            // Set bottom margin to navigation bar height to ensure card is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) submitCard.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 16; // 16dp original margin + navigation bar height
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

    private void initialization() {
        context = StaffAddApplication.this;
        Paper.init(this);
        
        // Load constants from Paper database (CRITICAL FIX)
        Constant.loadFromPaper();
        Log.d(TAG, "Constants loaded - staff_id: " + Constant.staff_id + ", campus_id: " + Constant.campus_id);
        
        progress_bar = findViewById(R.id.progress_bar);
        application_subject = findViewById(R.id.application_reason);
        application_body = findViewById(R.id.application_body);
        submit_application = findViewById(R.id.submit_leave_card);
        submit_button = findViewById(R.id.btn_submit_leave_application);
        select_application_spinner = findViewById(R.id.select_application_spinner);
        
        // Initialize header title
        TextView headerTitle = findViewById(R.id.header_title);
        if (headerTitle != null) {
            // Set dynamic title from intent or use default
            String titleText = getIntent().getStringExtra("HEADER_TITLE");
            if (titleText != null && !titleText.isEmpty()) {
                headerTitle.setText(titleText);
                Log.d(TAG, "Header title set from intent: " + titleText);
            } else {
                headerTitle.setText("Leave Application");
                Log.d(TAG, "Header title set to default: Leave Application");
            }
        } else {
            Log.e(TAG, "header_title not found in layout!");
        }
        
        // Initialize date picker components
        start_date_text = findViewById(R.id.start_date);
        end_date_text = findViewById(R.id.end_date);
        
        // Set default dates
        start_date_text.setText(dateFormat.format(startDate.getTime()));
        end_date_text.setText(dateFormat.format(endDate.getTime()));
        
        // Setup date picker click listeners
        start_date_text.setOnClickListener(v -> showDatePicker(true));
        end_date_text.setOnClickListener(v -> showDatePicker(false));
        
        // Setup SearchableSpinner
        if (select_application_spinner != null) {
            select_application_spinner.setTitle("Select Application Category");
            // Removed setPositiveButton("OK") to enable auto-dismiss behavior like staff timetable
            select_application_spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < category_name_list.size()) {
                        selected_application_category = category_name_list.get(position);
                        Log.d(TAG, "Selected category: " + selected_application_category);
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    selected_application_category = "";
                }
            });
            Log.d(TAG, "SearchableSpinner initialized successfully");
        } else {
            Log.e(TAG, "SearchableSpinner is null!");
        }
        
        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // Set click listener on the actual button, not the CardView
        if (submit_button != null) {
            submit_button.setOnClickListener(this);
            Log.d(TAG, "Submit button click listener set");
        } else {
            Log.e(TAG, "Submit button is null!");
        }
        
        // Disable CardView click to prevent conflicts
        if (submit_application != null) {
            submit_application.setClickable(false);
            submit_application.setFocusable(false);
        }
    }
    
    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                if (isStartDate) {
                    start_date_text.setText(dateFormat.format(calendar.getTime()));
                    // Ensure end date is not before start date
                    if (endDate.before(startDate)) {
                        endDate.setTime(startDate.getTime());
                        end_date_text.setText(dateFormat.format(endDate.getTime()));
                    }
                } else {
                    end_date_text.setText(dateFormat.format(calendar.getTime()));
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void loadApplicationCategories() {
        Log.d(TAG, "loadApplicationCategories() - Starting to load categories from API");
        
        // Load categories from backend API
        BaseApiService apiService = API.getAPIService();
        
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("campus_id", Constant.campus_id);
            
            Log.d(TAG, "Fetching categories for campus: " + Constant.campus_id);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for categories", e);
            return;
        }
        
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
        Call<topgrade.parent.com.parentseeks.Teacher.Model.LeaveApplicationResponse> call = apiService.load_leave_application_categories(requestBody);
        
        call.enqueue(new Callback<topgrade.parent.com.parentseeks.Teacher.Model.LeaveApplicationResponse>() {
            @Override
            public void onResponse(Call<topgrade.parent.com.parentseeks.Teacher.Model.LeaveApplicationResponse> call, 
                                 Response<topgrade.parent.com.parentseeks.Teacher.Model.LeaveApplicationResponse> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    topgrade.parent.com.parentseeks.Teacher.Model.LeaveApplicationResponse result = response.body();
                    
                    if (result.getCategories() != null && !result.getCategories().isEmpty()) {
                        category_name_list.clear();
                        for (topgrade.parent.com.parentseeks.Teacher.Model.CategoryModel category : result.getCategories()) {
                            category_name_list.add(category.getFullName());
                        }
                        
                        // Set up the adapter
                        if (select_application_spinner != null) {
                            application_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, category_name_list);
                            select_application_spinner.setAdapter(application_adapter);
                            Log.d(TAG, "Categories loaded successfully: " + category_name_list.size() + " items");
                        }
                    } else {
                        Log.w(TAG, "No categories returned from API");
                        loadDefaultCategories(); // Fallback
                    }
                } else {
                    Log.e(TAG, "Failed to load categories from API");
                    loadDefaultCategories(); // Fallback
                }
            }
            
            @Override
            public void onFailure(Call<topgrade.parent.com.parentseeks.Teacher.Model.LeaveApplicationResponse> call, Throwable t) {
                Log.e(TAG, "Error loading categories", t);
                loadDefaultCategories(); // Fallback
            }
        });
    }
    
    private void loadDefaultCategories() {
        Log.d(TAG, "Loading default categories as fallback");
        category_name_list.clear();
        category_name_list.add("Leave Application");
        
        if (select_application_spinner != null) {
            application_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, category_name_list);
            select_application_spinner.setAdapter(application_adapter);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_submit_leave_application) {
            Log.d(TAG, "Submit button clicked");
            showSubmitOptions(v);
        }
    }
    
    private void showSubmitOptions(View view) {
        Log.d(TAG, "showSubmitOptions() called");
        
        // Validate inputs first
        String subject = application_subject.getText().toString().trim();
        String body = application_body.getText().toString().trim();

        Log.d(TAG, "Subject: '" + subject + "', Body: '" + body + "', Category: '" + selected_application_category + "'");

        if (subject.isEmpty()) {
            application_subject.setError("Please enter application subject");
            Log.w(TAG, "Subject is empty");
            return;
        }

        if (body.isEmpty()) {
            application_body.setError("Please enter application body");
            Log.w(TAG, "Body is empty");
            return;
        }

        if (selected_application_category.isEmpty()) {
            Toast.makeText(context, "Please select an application category", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Category not selected");
            return;
        }
        
        Log.d(TAG, "Validation passed, showing popup menu");
        
        try {
            // Show popup menu with options
            PopupMenu popup = new PopupMenu(context, view);
            popup.getMenu().add(0, 1, 0, "Submit to System");
            popup.getMenu().add(0, 2, 0, "Local SMS");
            popup.getMenu().add(0, 3, 0, "Whatsapp");
            popup.getMenu().add(0, 4, 0, "Whatsapp(Business)");
            popup.getMenu().add(0, 5, 0, "Other");
            
            Log.d(TAG, "Popup menu created with 5 options");
            
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    String title = item.getTitle().toString();
                    Log.d(TAG, "Menu item clicked: " + title);
                    switch (title) {
                        case "Submit to System":
                            Log.d(TAG, "Submitting to system");
                            submitApplication();
                            break;
                        case "Whatsapp":
                            String phone = Paper.book().read("phone", "");
                            Log.d(TAG, "Sharing to WhatsApp: " + phone);
                            Util.shareToWhatsAppWithNumber(context, getApplicationMessage(), phone, "com.whatsapp");
                            break;
                        case "Whatsapp(Business)":
                            String phone_business = Paper.book().read("phone", "");
                            Log.d(TAG, "Sharing to WhatsApp Business: " + phone_business);
                            Util.shareToWhatsAppWithNumber(context, getApplicationMessage(), phone_business, "com.whatsapp.w4b");
                            break;
                        case "Local SMS":
                            String phone_sms = Paper.book().read("phone", "");
                            Log.d(TAG, "Sharing to SMS: " + phone_sms);
                            Util.showSmsIntent(context, getApplicationMessage(), phone_sms);
                            break;
                        case "Other":
                            String phone_other = Paper.book().read("phone", "");
                            Log.d(TAG, "Sharing to other apps: " + phone_other);
                            Util.shareWithPhoneNumber(context, getApplicationMessage(), phone_other);
                            break;
                    }
                    return true;
                }
            });
            
            Log.d(TAG, "Calling popup.show()");
            popup.show();
            Log.d(TAG, "Popup menu shown");
        } catch (Exception e) {
            Log.e(TAG, "Error showing popup menu", e);
            Toast.makeText(context, "Error showing menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getApplicationMessage() {
        String subject = application_subject.getText().toString().trim();
        String body = application_body.getText().toString().trim();
        String startDate = start_date_text.getText().toString();
        String endDate = end_date_text.getText().toString();
        String staffName = Paper.book().read("full_name", "Staff");
        
        String message = "Leave Application\n\n";
        message += "From: " + staffName + "\n";
        message += "Category: " + selected_application_category + "\n";
        message += "Subject: " + subject + "\n";
        message += "Start Date: " + startDate + "\n";
        message += "End Date: " + endDate + "\n\n";
        message += "Details:\n" + body;
        
        return message;
    }

    private void submitApplication() {
        // Validation already done in showSubmitOptions
        String subject = application_subject.getText().toString().trim();
        String body = application_body.getText().toString().trim();

        progress_bar.setVisibility(View.VISIBLE);
        submit_application.setEnabled(false);

        // Get selected dates from date pickers
        String startDateStr = start_date_text.getText().toString();
        String endDateStr = end_date_text.getText().toString();

        BaseApiService apiService = API.getAPIService();
        
        // Create JSON body for the request matching PHP API parameters
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("operation", "add_application");
            jsonBody.put("campus_id", Constant.campus_id);
            jsonBody.put("staff_id", Constant.staff_id);
            jsonBody.put("application_title", subject);
            jsonBody.put("applictaion_body", body);
            jsonBody.put("start_date", startDateStr);
            jsonBody.put("end_date", endDateStr);
            
            // Debug: Log request parameters
            Log.d(TAG, "=== SUBMITTING APPLICATION ===");
            Log.d(TAG, "campus_id: " + Constant.campus_id);
            Log.d(TAG, "staff_id: " + Constant.staff_id);
            Log.d(TAG, "application_title: " + subject);
            Log.d(TAG, "start_date: " + startDateStr);
            Log.d(TAG, "end_date: " + endDateStr);
            Log.d(TAG, "Request JSON: " + jsonBody.toString());
            Log.d(TAG, "==============================");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body", e);
            e.printStackTrace();
            progress_bar.setVisibility(View.GONE);
            submit_application.setEnabled(true);
            return;
        }
        
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
        Call<StaffApplicationModel> call = apiService.leave_applicaton(requestBody);
        
        call.enqueue(new Callback<StaffApplicationModel>() {
            @Override
            public void onResponse(Call<StaffApplicationModel> call, Response<StaffApplicationModel> response) {
                progress_bar.setVisibility(View.GONE);
                submit_application.setEnabled(true);
                
                Log.d(TAG, "API Response Code: " + response.code());
                Log.d(TAG, "API Response isSuccessful: " + response.isSuccessful());
                Log.d(TAG, "API Response body is null: " + (response.body() == null));
                
                // Log the raw response for debugging
                try {
                    if (response.body() != null) {
                        String rawResponse = new com.google.gson.Gson().toJson(response.body());
                        Log.d(TAG, "Raw Response Body: " + rawResponse);
                    }
                    if (response.errorBody() != null) {
                        String errorResponse = response.errorBody().string();
                        Log.d(TAG, "Error Response Body: " + errorResponse);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error logging response", e);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    StaffApplicationModel result = response.body();
                    Log.d(TAG, "Response status is null: " + (result.getStatus() == null));
                    
                    if (result.getStatus() != null) {
                        String statusCode = result.getStatus().getCode();
                        String statusMessage = result.getStatus().getMessage();
                        
                        Log.d(TAG, "Status Code: " + statusCode);
                        Log.d(TAG, "Status Message: " + statusMessage);
                        
                        if (statusCode != null && (statusCode.equals("1000") || statusCode.equalsIgnoreCase("success"))) {
                            Toast.makeText(context, statusMessage != null ? statusMessage : "Application submitted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(context, statusMessage != null ? statusMessage : "Failed to submit application", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Status object is null in response — probably custom backend format");
                        try {
                            // Try to read raw JSON manually from response
                            String rawJson = new com.google.gson.Gson().toJson(response.body());
                            Log.d(TAG, "Raw backend response: " + rawJson);
                            Toast.makeText(context, "Response received from server", Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing fallback response", e);
                            Toast.makeText(context, "Leave submitted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                } else {
                    Log.e(TAG, "Response not successful or body is null");
                    String errorMessage = "Failed to submit application";
                    
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            if (errorBody != null && !errorBody.isEmpty()) {
                                errorMessage = "Server error: " + errorBody.substring(0, Math.min(100, errorBody.length()));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    
                    // Show HTTP error code for debugging
                    if (response.code() >= 400) {
                        errorMessage = "Server error (" + response.code() + "): " + 
                            (response.code() == 500 ? "Please contact administrator" : "Please try again");
                        Log.e(TAG, "HTTP Error Code: " + response.code());
                    }
                    
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<StaffApplicationModel> call, Throwable t) {
                progress_bar.setVisibility(View.GONE);
                submit_application.setEnabled(true);
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
