package topgrade.parent.com.parentseeks.Parent.Activity;

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
import androidx.core.content.ContextCompat;

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
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import components.searchablespinnerlibrary.SearchableSpinner;

public class ParentAddApplication extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ParentAddApplication";

    private EditText application_subject;
    private EditText application_body;
    private Button submit_application;
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
        setContentView(R.layout.activity_parent_application);
        
        // Apply theme based on user type
        applyTheme();
        
        initialization();
        
        // Load application categories
        loadApplicationCategories();
    }

    private void initialization() {
        context = ParentAddApplication.this;
        Paper.init(this);
        
        progress_bar = findViewById(R.id.progress_bar);
        application_subject = findViewById(R.id.application_reason);
        application_body = findViewById(R.id.application_body);
        submit_application = findViewById(R.id.submit_Leave);
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
        
        // Setup back button
        findViewById(R.id.back_icon).setOnClickListener(v -> finish());
        
        // Setup submit button
        submit_application.setOnClickListener(this);
        
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
        }
    }
    
    private void showDatePicker(boolean isStartDate) {
        Calendar currentDate = isStartDate ? startDate : endDate;
        
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            currentDate.set(Calendar.YEAR, year);
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            if (isStartDate) {
                start_date_text.setText(dateFormat.format(currentDate.getTime()));
                // Update end date minimum if it's before start date
                if (endDate.before(startDate)) {
                    endDate.setTime(startDate.getTime());
                    end_date_text.setText(dateFormat.format(endDate.getTime()));
                }
            } else {
                end_date_text.setText(dateFormat.format(currentDate.getTime()));
            }
        };
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this, dateSetListener,
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date for end date picker
        if (!isStartDate) {
            datePickerDialog.getDatePicker().setMinDate(startDate.getTimeInMillis());
        }
        
        datePickerDialog.show();
    }
    
    private void loadApplicationCategories() {
        Log.d(TAG, "loadApplicationCategories() - Starting to load leave application categories");
        
        // Create default categories for parent leave applications
        category_name_list.clear();
        category_name_list.add("Sick Leave");
        category_name_list.add("Casual Leave");
        category_name_list.add("Emergency Leave");
        category_name_list.add("Personal Leave");
        category_name_list.add("Medical Leave");
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
        if (v.getId() == R.id.submit_Leave) {
            submitApplication();
        }
    }

    private void submitApplication() {
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

        // Validate that a category is selected
        if (selected_application_category.isEmpty()) {
            Toast.makeText(context, "Please select an application category", Toast.LENGTH_SHORT).show();
            return;
        }

        progress_bar.setVisibility(View.VISIBLE);
        submit_application.setEnabled(false);

        // Get selected dates from date pickers
        String startDateStr = start_date_text.getText().toString();
        String endDateStr = end_date_text.getText().toString();

        BaseApiService apiService = API.getAPIService();
        
        // Get parent and campus IDs from Paper
        String parentId = Paper.book().read("parent_id", "");
        String campusId = Paper.book().read("campus_id", "");
        
        // Create JSON body for the request
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("operation", "add_application");
            jsonBody.put("campus_id", campusId);
            jsonBody.put("parent_id", parentId);
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
    
    /**
     * Apply theme based on user type
     */
    private void applyTheme() {
        try {
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d(TAG, "User Type: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Set system bar colors for student theme
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.student_primary));
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.student_primary));
                }
                
                Log.d(TAG, "Student theme applied successfully");
            } else {
                // Apply unified parent theme for add application page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for add application
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for add application
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Add Application");
                
                Log.d(TAG, "Parent theme applied successfully - UserType: " + userType);
            }
            
            // Apply footer theming based on user type
            ThemeHelper.applyFooterTheme(this, userType);
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme", e);
            // Fallback: apply parent theme if there's an error
            try {
                ThemeHelper.applyParentTheme(this);
                Log.d(TAG, "Fallback parent theme applied due to error");
            } catch (Exception fallbackError) {
                Log.e(TAG, "Error applying fallback theme", fallbackError);
            }
        }
    }
}
