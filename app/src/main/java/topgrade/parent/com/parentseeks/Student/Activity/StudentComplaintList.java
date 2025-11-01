package topgrade.parent.com.parentseeks.Student.Activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Student.Adaptor.StudentComplaintAdapter;
import topgrade.parent.com.parentseeks.Student.Model.StudentComplaintModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StudentComplaintList extends AppCompatActivity {

    private static final String TAG = "StudentComplaintList";

    RecyclerView complaintRcv;
    ProgressBar progressBar;
    Context context;
    List<StudentComplaintModel.Complaint> list = new ArrayList<>();
    TextView totalRecords;
    TextView headerTitle;
    StudentComplaintAdapter adapter;
    View emptyView;
    SwipeRefreshLayout swipeRefresh;
    
    // Filter type from intent
    private String filterType = "";
    
    // Debug logging wrapper
    private void log(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_student_complaint_list);
        
        // Apply unified student theme
        StudentThemeHelper.applyStudentTheme(this, 100); // 100dp for content pages
        StudentThemeHelper.setHeaderIconVisibility(this, false); // No icon for complaint list
        StudentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for complaint list
        StudentThemeHelper.setFooterVisibility(this, true); // Show footer
        StudentThemeHelper.setHeaderTitle(this, "Complaints");
        
        // Setup window insets
        setupWindowInsets();

        context = StudentComplaintList.this;
        Paper.init(context);
        
        // Load constants from Paper database
        Constant.loadFromPaper();
        
        // Get filter type from intent
        String intentFilterType = getIntent().getStringExtra("filter_type");
        if (intentFilterType != null && !intentFilterType.isEmpty()) {
            filterType = intentFilterType;
        }
        
        // Initialize views
        initViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Load initial cached complaints
        loadInitialCachedComplaints();
        
        // Then load fresh data from API
        loadComplaints();
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
        progressBar = findViewById(R.id.progress_bar);
        complaintRcv = findViewById(R.id.complaint_rcv);
        totalRecords = findViewById(R.id.total_records);
        headerTitle = findViewById(R.id.header_title);
        emptyView = findViewById(R.id.empty_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        
        // Set header title based on filter type
        setHeaderTitle();
        
        // Setup back button
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());
        
        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadComplaints);
        swipeRefresh.setColorSchemeResources(R.color.teal);
    }
    
    private void setHeaderTitle() {
        String title = "All Complaints";
        switch (filterType) {
            case "pending":
                title = "Pending Complaints";
                break;
            case "under_discussion":
                title = "Under Discussion";
                break;
            case "solved":
                title = "Solved Complaints";
                break;
        }
        headerTitle.setText(title);
    }
    
    private void setupRecyclerView() {
        try {
            complaintRcv.setLayoutManager(new LinearLayoutManager(context));
            complaintRcv.setHasFixedSize(true);
            complaintRcv.setItemViewCacheSize(20);
            complaintRcv.setNestedScrollingEnabled(false);
            
            adapter = new StudentComplaintAdapter(list, context, filterType);
            complaintRcv.setAdapter(adapter);
            
        } catch (Exception e) {
            Log.e(TAG, "setupRecyclerView: ", e);
        }
    }
    
    private void loadComplaints() {
        try {
            // Get student details from Paper
            String studentId = Paper.book().read("student_id", "");
            String campusId = Paper.book().read("campus_id", "");
            
            if (studentId.isEmpty() || campusId.isEmpty()) {
                Log.e(TAG, "Student ID or Campus ID not found in Paper");
                showEmptyState(true);
                return;
            }
            
            // Create request body
            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("operation", "read_complain");  // ADD OPERATION
            requestBody.put("student_id", studentId);
            requestBody.put("campus_id", campusId);
            requestBody.put("session_id", Constant.current_session);
            requestBody.put("filter_type", filterType);
            
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                new JSONObject(requestBody).toString()
            );
            
            // Make API call
            topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService apiService =
                topgrade.parent.com.parentseeks.Parent.Utils.API.getAPIService();
            
            Call<StudentComplaintModel> call = apiService.student_complain(body);
            call.enqueue(new Callback<StudentComplaintModel>() {
                @Override
                public void onResponse(Call<StudentComplaintModel> call, Response<StudentComplaintModel> response) {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        StudentComplaintModel studentComplaintModel = response.body();
                        // Use real API data
                        if (studentComplaintModel.getData() != null && !studentComplaintModel.getData().isEmpty()) {
                            // Filter already applied on server, but we can filter again client-side if needed
                            list = studentComplaintModel.getData();
                            updateUI();
                            cacheComplaints(studentComplaintModel.getData());
                        } else {
                            showEmptyState(true);
                        }
                    } else {
                        Log.e(TAG, "Response not successful: " + response.code());
                        showEmptyState(true);
                    }
                }
                
                @Override
                public void onFailure(Call<StudentComplaintModel> call, Throwable t) {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Failed to load complaints: " + t.getMessage());
                    showEmptyState(true);
                }
            });
            
        } catch (Exception e) {
            swipeRefresh.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error loading complaints: " + e.getMessage());
            showEmptyState(true);
        }
    }
    
    private List<StudentComplaintModel.Complaint> filterComplaints(List<StudentComplaintModel.Complaint> complaints, String filterType) {
        if (filterType == null || filterType.isEmpty() || filterType.equals("all")) {
            return complaints;
        }
        
        List<StudentComplaintModel.Complaint> filteredList = new ArrayList<>();
        for (StudentComplaintModel.Complaint complaint : complaints) {
            if (complaint.getComplaintStatus().equalsIgnoreCase(filterType)) {
                filteredList.add(complaint);
            }
        }
        return filteredList;
    }
    
    private void updateUI() {
        try {
            if (adapter != null) {
                adapter.updateData(list);
            }
            
            totalRecords.setText("Total Complaints: " + list.size());
            showEmptyState(list.isEmpty());
            
        } catch (Exception e) {
            Log.e(TAG, "updateUI: ", e);
        }
    }
    
    private void showEmptyState(boolean show) {
        try {
            if (show) {
                emptyView.setVisibility(View.VISIBLE);
                complaintRcv.setVisibility(View.GONE);
                totalRecords.setText("Total Complaints: 0");
            } else {
                emptyView.setVisibility(View.GONE);
                complaintRcv.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "showEmptyState: ", e);
        }
    }
    
    private void cacheComplaints(List<StudentComplaintModel.Complaint> complaints) {
        try {
            if (complaints != null && !complaints.isEmpty()) {
                Paper.book().write("student_complaints_cache", complaints);
                log("Complaints cached successfully: " + complaints.size() + " items");
            }
        } catch (Exception e) {
            log("Error caching complaints: " + e.getMessage());
        }
    }
    
    private List<StudentComplaintModel.Complaint> loadCachedComplaints() {
        try {
            List<StudentComplaintModel.Complaint> cachedComplaints = Paper.book().read("student_complaints_cache", new ArrayList<>());
            if (cachedComplaints != null && !cachedComplaints.isEmpty()) {
                log("Loaded cached complaints: " + cachedComplaints.size() + " items");
                return cachedComplaints;
            }
        } catch (Exception e) {
            log("Error loading cached complaints: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    private void loadInitialCachedComplaints() {
        List<StudentComplaintModel.Complaint> cachedComplaints = loadCachedComplaints();
        if (!cachedComplaints.isEmpty()) {
            log("Loading initial cached complaints: " + cachedComplaints.size() + " items");
            list = filterComplaints(cachedComplaints, filterType);
            
            if (adapter == null) {
                adapter = new StudentComplaintAdapter(list, context, filterType);
                complaintRcv.setAdapter(adapter);
            } else {
                adapter.updateData(list);
            }
            totalRecords.setText("Total Complaints: " + list.size() + " (Loading...)");
            showEmptyState(false);
        }
    }
    
    public void refreshComplaints() {
        loadComplaints();
    }
}

