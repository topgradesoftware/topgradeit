package topgrade.parent.com.parentseeks.Teacher.Activity.Complaint;

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
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.ComplaintAdapter;
import topgrade.parent.com.parentseeks.Teacher.Model.ComplaintModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffComplaintList extends AppCompatActivity {

    private static final String TAG = "StaffComplaintList";

    RecyclerView complaintRcv;
    ProgressBar progressBar;
    Context context;
    List<ComplaintModel.Complaint> list = new ArrayList<>();
    TextView totalRecords;
    TextView headerTitle;
    ComplaintAdapter adapter;
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
        
        setContentView(R.layout.activity_staff_complaint_list);
        
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

        context = StaffComplaintList.this;
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
    
    /**
     * Setup window insets to respect system bars - EXACT COPY FROM WORKING StaffApplicationList
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
        swipeRefresh.setColorSchemeResources(R.color.navy_blue);
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
            
            adapter = new ComplaintAdapter(list, context, filterType);
            complaintRcv.setAdapter(adapter);
            
        } catch (Exception e) {
            Log.e(TAG, "setupRecyclerView: ", e);
        }
    }
    
    /**
     * Load complaints from API
     */
    private void loadComplaints() {
        try {
            // Get staff details from Paper
            String staffId = Paper.book().read("staff_id", "");
            String campusId = Paper.book().read("campus_id", "");
            
            if (staffId.isEmpty() || campusId.isEmpty()) {
                Log.e(TAG, "Staff ID or Campus ID not found in Paper");
                showEmptyState(true);
                return;
            }
            
            // Create request body
            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("staff_id", staffId);
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
            
            Call<topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel> call = apiService.complain(body);
            call.enqueue(new Callback<topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel>() {
                @Override
                public void onResponse(Call<topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel> call, Response<topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel> response) {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel staffComplainModel = response.body();
                        
                        // Check if API response is successful
                        if (staffComplainModel.getStatus() != null && "1000".equals(staffComplainModel.getStatus().getCode())) {
                            // Use real API data
                            List<ComplaintModel.Complaint> complaints = staffComplainModel.getData();
                            if (complaints != null && !complaints.isEmpty()) {
                                list = filterComplaints(complaints, filterType);
                                updateUI();
                                cacheComplaints(complaints);
                                log("Loaded " + list.size() + " complaints from API");
                            } else {
                                log("No complaints found in API response");
                                showEmptyState(true);
                            }
                        } else {
                            String errorMsg = staffComplainModel.getStatus() != null ? 
                                staffComplainModel.getStatus().getMessage() : "Unknown error";
                            Log.e(TAG, "API error: " + errorMsg);
                            showEmptyState(true);
                        }
                    } else {
                        Log.e(TAG, "Response not successful: " + (response.body() != null ? response.code() : "null response"));
                        showEmptyState(true);
                    }
                }
                
                @Override
                public void onFailure(Call<topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel> call, Throwable t) {
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
    
    /**
     * Filter complaints based on type
     */
    private List<ComplaintModel.Complaint> filterComplaints(List<ComplaintModel.Complaint> complaints, String filterType) {
        if (filterType == null || filterType.isEmpty() || filterType.equals("all")) {
            return complaints;
        }
        
        List<ComplaintModel.Complaint> filteredList = new ArrayList<>();
        for (ComplaintModel.Complaint complaint : complaints) {
            if (complaint.getComplaintStatus().equalsIgnoreCase(filterType)) {
                filteredList.add(complaint);
            }
        }
        return filteredList;
    }
    
    /**
     * Update UI with complaint data
     */
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
    
    /**
     * Show/hide empty state
     */
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
    
    /**
     * Cache complaints to PaperDB
     */
    private void cacheComplaints(List<ComplaintModel.Complaint> complaints) {
        try {
            if (complaints != null && !complaints.isEmpty()) {
                Paper.book().write("staff_complaints_cache", complaints);
                log("Complaints cached successfully: " + complaints.size() + " items");
            }
        } catch (Exception e) {
            log("Error caching complaints: " + e.getMessage());
        }
    }
    
    /**
     * Load cached complaints from PaperDB
     */
    private List<ComplaintModel.Complaint> loadCachedComplaints() {
        try {
            List<ComplaintModel.Complaint> cachedComplaints = Paper.book().read("staff_complaints_cache", new ArrayList<>());
            if (cachedComplaints != null && !cachedComplaints.isEmpty()) {
                log("Loaded cached complaints: " + cachedComplaints.size() + " items");
                return cachedComplaints;
            }
        } catch (Exception e) {
            log("Error loading cached complaints: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    /**
     * Load cached complaints on activity start
     */
    private void loadInitialCachedComplaints() {
        List<ComplaintModel.Complaint> cachedComplaints = loadCachedComplaints();
        if (!cachedComplaints.isEmpty()) {
            log("Loading initial cached complaints: " + cachedComplaints.size() + " items");
            list = filterComplaints(cachedComplaints, filterType);
            
            if (adapter == null) {
                adapter = new ComplaintAdapter(list, context, filterType);
                complaintRcv.setAdapter(adapter);
            } else {
                adapter.updateData(list);
            }
            totalRecords.setText("Total Complaints: " + list.size() + " (Loading...)");
            showEmptyState(false);
        }
    }
    
    /**
     * Refresh complaint list
     */
    public void refreshComplaints() {
        loadComplaints();
    }
    
    /**
     * Create mock complaint data for testing
     */
    private List<ComplaintModel.Complaint> createMockComplaints() {
        List<ComplaintModel.Complaint> mockComplaints = new ArrayList<>();
        
        // Create sample complaints
        ComplaintModel.Complaint complaint1 = new ComplaintModel.Complaint();
        complaint1.setComplaintId("1");
        complaint1.setComplaintTitle("Internet Connection Issue");
        complaint1.setComplaintDescription("The internet connection in the computer lab is very slow and frequently disconnects.");
        complaint1.setComplaintStatus("Pending");
        complaint1.setComplaintDate("2024-01-15");
        complaint1.setStaffId("staff001");
        complaint1.setStaffName("John Smith");
        mockComplaints.add(complaint1);
        
        ComplaintModel.Complaint complaint2 = new ComplaintModel.Complaint();
        complaint2.setComplaintId("2");
        complaint2.setComplaintTitle("Air Conditioning Problem");
        complaint2.setComplaintDescription("The air conditioning in classroom 101 is not working properly.");
        complaint2.setComplaintStatus("Under Discussion");
        complaint2.setComplaintDate("2024-01-14");
        complaint2.setStaffId("staff002");
        complaint2.setStaffName("Jane Doe");
        mockComplaints.add(complaint2);
        
        ComplaintModel.Complaint complaint3 = new ComplaintModel.Complaint();
        complaint3.setComplaintId("3");
        complaint3.setComplaintTitle("Projector Maintenance");
        complaint3.setComplaintDescription("The projector in the main hall needs maintenance and bulb replacement.");
        complaint3.setComplaintStatus("Solved");
        complaint3.setComplaintDate("2024-01-10");
        complaint3.setStaffId("staff003");
        complaint3.setStaffName("Mike Johnson");
        complaint3.setResponse("Projector has been repaired and new bulb installed.");
        complaint3.setResponseDate("2024-01-12");
        mockComplaints.add(complaint3);
        
        return mockComplaints;
    }
}
