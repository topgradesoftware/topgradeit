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
        
        progress_bar = findViewById(R.id.progress_bar);
        application_subject = findViewById(R.id.application_reason);
        application_body = findViewById(R.id.application_body);
        submit_application = findViewById(R.id.submit_leave_card);
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
        
        submit_application.setOnClickListener(this);
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
        Log.d(TAG, "loadApplicationCategories() - Starting to load leave application categories");
        
        // Since the API doesn't provide categories, let's create some default ones
        // You can modify these based on your school's leave types
        category_name_list.clear();
        category_name_list.add("Sick Leave");
        category_name_list.add("Casual Leave");
        category_name_list.add("Annual Leave");
        category_name_list.add("Emergency Leave");
        category_name_list.add("Personal Leave");
        category_name_list.add("Other");
        
        // Set up the adapter for select_application_spinner
        if (select_application_spinner != null && !category_name_list.isEmpty()) {
            application_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, category_name_list);
            select_application_spinner.setAdapter(application_adapter);
            Log.d(TAG, "loadApplicationCategories() - Adapter set successfully with " + category_name_list.size() + " categories");
        } else {
            Log.e(TAG, "loadApplicationCategories() - SearchableSpinner is null or category list is empty");
            Toast.makeText(context, "No application categories available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.submit_leave_card) {
            showSubmitOptions(v);
        }
    }
    
    private void showSubmitOptions(View view) {
        // Validate inputs first
        String subject = application_subject.getText().toString().trim();
        String body = application_body.getText().toString().trim();

        if (subject.isEmpty()) {
            application_subject.setError("Please enter application subject");
            return;
        }

        if (body.isEmpty()) {
            application_body.setError("Please enter application body");
            return;
        }

        if (selected_application_category.isEmpty()) {
            Toast.makeText(context, "Please select an application category", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show popup menu with options
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenu().add("Submit to System");
        popup.getMenu().add("Local SMS");
        popup.getMenu().add("Whatsapp");
        popup.getMenu().add("Whatsapp(Business)");
        popup.getMenu().add("Other");
        
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                String title = item.getTitle().toString();
                switch (title) {
                    case "Submit to System":
                        submitApplication();
                        break;
                    case "Whatsapp":
                        String phone = Paper.book().read("phone", "");
                        Util.shareToWhatsAppWithNumber(context, getApplicationMessage(), phone, "com.whatsapp");
                        break;
                    case "Whatsapp(Business)":
                        String phone_business = Paper.book().read("phone", "");
                        Util.shareToWhatsAppWithNumber(context, getApplicationMessage(), phone_business, "com.whatsapp.w4b");
                        break;
                    case "Local SMS":
                        String phone_sms = Paper.book().read("phone", "");
                        Util.showSmsIntent(context, getApplicationMessage(), phone_sms);
                        break;
                    case "Other":
                        String phone_other = Paper.book().read("phone", "");
                        Util.shareWithPhoneNumber(context, getApplicationMessage(), phone_other);
                        break;
                }
                return true;
            }
        });
        popup.show();
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
        } catch (JSONException e) {
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
                
                if (response.isSuccessful() && response.body() != null) {
                    StaffApplicationModel result = response.body();
                    if (result.getStatus() != null) {
                        if (result.getStatus().getCode().equals("1000")) {
                            Toast.makeText(context, result.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(context, result.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(context, "Failed to submit application", Toast.LENGTH_SHORT).show();
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
