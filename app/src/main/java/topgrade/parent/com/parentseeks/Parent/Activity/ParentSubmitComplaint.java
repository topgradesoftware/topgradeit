package topgrade.parent.com.parentseeks.Parent.Activity;

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
import topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import components.searchablespinnerlibrary.SearchableSpinner;

public class ParentSubmitComplaint extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ParentSubmitComplaint";

    private EditText complaint_subject;
    private EditText complaint_description;
    private SearchableSpinner complaint_category;
    private SearchableSpinner complaint_priority;
    private Button submit_complaint_btn;
    private ProgressBar progress_bar;
    private ImageView back_icon;
    private Context context;

    private List<String> categoryList = new ArrayList<>();
    private List<String> categoryTitleIds = new ArrayList<>();
    private List<String> priorityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_parent_submit_complaint);
        
        // Configure status bar for dark brown background with white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }
        
        // Setup window insets
        setupWindowInsets();

        context = ParentSubmitComplaint.this;
        Paper.init(context);
        
        // Load constants from Paper database
        Constant.loadFromPaper();
        
        // Initialize views
        initViews();
        
        // Setup priority spinner (static)
        setupPrioritySpinner();
        
        // Load complaint titles from API
        loadComplaintTitles();
        
        // Set click listeners
        setClickListeners();
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
    
    private void initViews() {
        complaint_subject = findViewById(R.id.complaint_subject);
        complaint_description = findViewById(R.id.complaint_description);
        complaint_category = findViewById(R.id.complaint_category);
        complaint_priority = findViewById(R.id.complaint_priority);
        submit_complaint_btn = findViewById(R.id.submit_complaint_btn);
        progress_bar = findViewById(R.id.progress_bar);
        back_icon = findViewById(R.id.back_icon);
    }
    
    private void setupPrioritySpinner() {
        // Setup priority levels (static - not loaded from API)
        priorityList.clear();
        priorityList.add("Select Priority");
        priorityList.add("Low");
        priorityList.add("Medium");
        priorityList.add("High");
        priorityList.add("Urgent");
        
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorityList);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complaint_priority.setAdapter(priorityAdapter);
        complaint_priority.setSelection(0);
    }
    
    private void loadComplaintTitles() {
        try {
            String campusId = Paper.book().read("campus_id", "");
            
            if (campusId.isEmpty()) {
                Log.e(TAG, "Campus ID not found");
                setupDefaultCategories();
                return;
            }
            
            // Show loading indicator
            progress_bar.setVisibility(View.VISIBLE);
            complaint_category.setEnabled(false);
            
            // Create request body for read_complain_title operation
            HashMap<String, String> postParam = new HashMap<>();
            postParam.put("operation", "read_complain_title");
            postParam.put("campus_id", campusId);
            
            String requestBody = (new JSONObject(postParam)).toString();
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                requestBody
            );
            
            // Make API call
            BaseApiService apiService = API.getAPIService();
            Call<ParentComplaintModel> call = apiService.parent_complain(body);
            
            call.enqueue(new Callback<ParentComplaintModel>() {
                @Override
                public void onResponse(Call<ParentComplaintModel> call, Response<ParentComplaintModel> response) {
                    progress_bar.setVisibility(View.GONE);
                    complaint_category.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ParentComplaintModel model = response.body();
                        if (model.getStatus() != null && "1000".equals(model.getStatus().getCode())) {
                            if (model.getTitles() != null && !model.getTitles().isEmpty()) {
                                setupCategoriesFromAPI(model.getTitles());
                            } else {
                                setupDefaultCategories();
                            }
                        } else {
                            Log.e(TAG, "Error loading titles: " + model.getStatus().getMessage());
                            setupDefaultCategories();
                        }
                    } else {
                        Log.e(TAG, "Response unsuccessful");
                        setupDefaultCategories();
                    }
                }
                
                @Override
                public void onFailure(Call<ParentComplaintModel> call, Throwable t) {
                    progress_bar.setVisibility(View.GONE);
                    complaint_category.setEnabled(true);
                    Log.e(TAG, "Error loading complaint titles: " + t.getMessage());
                    setupDefaultCategories();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating request: " + e.getMessage());
            setupDefaultCategories();
        }
    }
    
    private void setupCategoriesFromAPI(List<ParentComplaintModel.ComplaintTitle> titles) {
        categoryList.clear();
        categoryTitleIds.clear();
        
        categoryList.add("Select Category");
        categoryTitleIds.add("");
        
        for (ParentComplaintModel.ComplaintTitle title : titles) {
            categoryList.add(title.getTitle());
            categoryTitleIds.add(title.getTitleId());
        }
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complaint_category.setAdapter(categoryAdapter);
        complaint_category.setSelection(0);
        
        Log.d(TAG, "Loaded " + (categoryList.size() - 1) + " complaint categories from API");
    }
    
    private void setupDefaultCategories() {
        categoryList.clear();
        categoryTitleIds.clear();
        
        categoryList.add("Select Category");
        categoryList.add("General");
        categoryList.add("Academic");
        categoryList.add("Administrative");
        categoryList.add("Technical");
        categoryList.add("Facility");
        categoryList.add("Other");
        
        for (int i = 0; i < categoryList.size(); i++) {
            categoryTitleIds.add(String.valueOf(i));
        }
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complaint_category.setAdapter(categoryAdapter);
        complaint_category.setSelection(0);
        
        Log.d(TAG, "Using default complaint categories");
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
            // Get parent details from Paper
            String parentId = Paper.book().read("parent_id", "");
            String campusId = Paper.book().read("campus_id", "");
            
            if (parentId.isEmpty() || campusId.isEmpty()) {
                Toast.makeText(context, "Parent information not found", Toast.LENGTH_SHORT).show();
                hideProgressBar();
                return;
            }
            
            // Get selected category title_id
            int selectedCategoryPosition = complaint_category.getSelectedItemPosition();
            String categoryTitleId = "";
            if (selectedCategoryPosition > 0 && selectedCategoryPosition <= categoryTitleIds.size()) {
                categoryTitleId = categoryTitleIds.get(selectedCategoryPosition);
            }
            
            // Create request body matching parent API
            HashMap<String, String> postParam = new HashMap<>();
            postParam.put("operation", "add_complain");
            postParam.put("campus_id", campusId);
            postParam.put("parent_id", parentId);
            postParam.put("complain_title", complaint_subject.getText().toString().trim());
            postParam.put("complain_body", complaint_description.getText().toString().trim());
            if (!categoryTitleId.isEmpty()) {
                postParam.put("complainant_category", categoryTitleId); // Send title_id
            }
            
            String requestBody = (new JSONObject(postParam)).toString();
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                requestBody
            );
            
            // Make API call using parent API endpoint
            BaseApiService apiService = API.getAPIService();
            Call<ParentComplaintModel> call = apiService.parent_complain(body);
            
            call.enqueue(new Callback<ParentComplaintModel>() {
                @Override
                public void onResponse(Call<ParentComplaintModel> call, Response<ParentComplaintModel> response) {
                    hideProgressBar();
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ParentComplaintModel parentComplaintModel = response.body();
                        if (parentComplaintModel.getStatus() != null && "1000".equals(parentComplaintModel.getStatus().getCode())) {
                            Toast.makeText(context, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String errorMsg = parentComplaintModel.getStatus() != null ? parentComplaintModel.getStatus().getMessage() : "Unknown error";
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Failed to submit complaint", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ParentComplaintModel> call, Throwable t) {
                    hideProgressBar();
                    Log.e(TAG, "Error submitting complaint: " + t.getMessage());
                    Toast.makeText(context, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
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

