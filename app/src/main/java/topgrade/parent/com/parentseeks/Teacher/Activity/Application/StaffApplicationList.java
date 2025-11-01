package topgrade.parent.com.parentseeks.Teacher.Activity.Application;

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
import topgrade.parent.com.parentseeks.Teacher.Adaptor.ApplicationAdapter;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffApplicationList extends AppCompatActivity {

    private static final String TAG = "StaffApplicationList";

    RecyclerView applicationRcv;
    ProgressBar progressBar;
    Context context;
    List<StaffApplicationModel.Application> list = new ArrayList<>();
    TextView totalRecords;
    TextView headerTitle;
    ApplicationAdapter adapter;
    View emptyView;
    SwipeRefreshLayout swipeRefresh;
    
    // Filter type from intent
    private String filterType = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_leave_application_list);
        
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
        
        // Additional fix for older Android versions - Force white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Ensure status bar icons are light (white) on dark background
            getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility() & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0,
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        setupWindowInsets();
        
        // Debug: Log status bar configuration
        Log.d(TAG, "=== STATUS BAR DEBUG ===");
        Log.d(TAG, "Status bar color: " + getWindow().getStatusBarColor());
        Log.d(TAG, "Navigation bar color: " + getWindow().getNavigationBarColor());
        Log.d(TAG, "System UI visibility: " + getWindow().getDecorView().getSystemUiVisibility());
        Log.d(TAG, "WindowCompat.setDecorFitsSystemWindows: false");
        Log.d(TAG, "=== END STATUS BAR DEBUG ===");
        
        // Post-delayed status bar configuration to handle timing issues
        findViewById(android.R.id.content).post(() -> {
            Log.d(TAG, "=== POST-DELAYED STATUS BAR CONFIG ===");
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
                Log.d(TAG, "Post-delayed: Status bar set to transparent, flags: " + flags);
            }
            Log.d(TAG, "=== END POST-DELAYED CONFIG ===");
        });

        context = StaffApplicationList.this;
        Paper.init(context);
        
        Constant.loadFromPaper();
        
        // Get filter type from intent
        Log.d(TAG, "=== INTENT DEBUG START ===");
        Log.d(TAG, "getIntent() is null: " + (getIntent() == null));
        
        if (getIntent() != null) {
            Log.d(TAG, "Intent exists, checking for filter_type extra");
            Log.d(TAG, "hasExtra('filter_type'): " + getIntent().hasExtra("filter_type"));
            
            if (getIntent().hasExtra("filter_type")) {
                filterType = getIntent().getStringExtra("filter_type");
                Log.d(TAG, "✅ Filter type received from intent: '" + filterType + "'");
                Log.d(TAG, "Filter type length: " + (filterType != null ? filterType.length() : "null"));
                Log.d(TAG, "Filter type isEmpty: " + (filterType != null ? filterType.isEmpty() : "null"));
            } else {
                Log.w(TAG, "❌ No 'filter_type' extra found in intent");
                Log.d(TAG, "Available intent extras:");
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    for (String key : extras.keySet()) {
                        Log.d(TAG, "  - " + key + ": " + extras.get(key));
                    }
                } else {
                    Log.d(TAG, "  - No extras bundle found");
                }
            }
        } else {
            Log.e(TAG, "❌ getIntent() is NULL!");
        }
        Log.d(TAG, "=== INTENT DEBUG END ===");

        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        initialization();
        
        // Debug: Check header title before and after update
        Log.d(TAG, "=== HEADER TITLE DEBUG ===");
        if (headerTitle != null) {
            Log.d(TAG, "Header title before updateHeaderTitle(): '" + headerTitle.getText().toString() + "'");
        }
        
        updateHeaderTitle();
        updateHeaderColors();
        
        // Debug: Check header title after update
        if (headerTitle != null) {
            Log.d(TAG, "Header title after updateHeaderTitle(): '" + headerTitle.getText().toString() + "'");
        }
        
        loadApplications();
    }
    
    private void setupWindowInsets() {
        View rootLayout = findViewById(R.id.root_layout);
        
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    // Don't add top padding to allow header wave to cover status bar
                    // Only add bottom padding for navigation bar
                    view.setPadding(
                        systemInsets.left,
                        0, // No top padding - let header wave cover status bar
                        systemInsets.right,
                        systemInsets.bottom
                    );

                    return WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }
    }

    private void initialization() {
        Log.d(TAG, "=== INITIALIZATION DEBUG START ===");
        
        progressBar = findViewById(R.id.progress_bar);
        Log.d(TAG, "progressBar found: " + (progressBar != null));
        
        applicationRcv = findViewById(R.id.application_rcv);
        Log.d(TAG, "applicationRcv found: " + (applicationRcv != null));
        
        totalRecords = findViewById(R.id.total_records);
        Log.d(TAG, "totalRecords found: " + (totalRecords != null));
        
        headerTitle = findViewById(R.id.header_title);
        Log.d(TAG, "headerTitle found: " + (headerTitle != null));
        if (headerTitle != null) {
            Log.d(TAG, "headerTitle current text: '" + headerTitle.getText().toString() + "'");
            Log.d(TAG, "headerTitle visibility: " + headerTitle.getVisibility());
        } else {
            Log.e(TAG, "❌ headerTitle NOT FOUND! Check if R.id.header_title exists in layout");
        }
        
        emptyView = findViewById(R.id.empty_view);
        Log.d(TAG, "emptyView found: " + (emptyView != null));
        
        swipeRefresh = findViewById(R.id.swipe_refresh);
        Log.d(TAG, "swipeRefresh found: " + (swipeRefresh != null));
        
        if (applicationRcv != null) {
            applicationRcv.setLayoutManager(new LinearLayoutManager(this));
            Log.d(TAG, "RecyclerView layout manager set");
        } else {
            Log.e(TAG, "❌ Cannot set layout manager - applicationRcv is null");
        }
        
        setupSwipeRefresh();
        Log.d(TAG, "=== INITIALIZATION DEBUG END ===");
    }
    
    /**
     * Update header title based on filter type - Using the same pattern as StaffTask.java
     */
    private void updateHeaderTitle() {
        if (headerTitle != null) {
            String title = "View All Applications";
            
            if (Constant.FILTER_LEAVE_ALL.equals(filterType)) {
                title = "View All Applications";
            } else if (Constant.FILTER_LEAVE_PENDING.equals(filterType)) {
                title = "Pending Applications";
            } else if (Constant.FILTER_LEAVE_APPROVED.equals(filterType)) {
                title = "Approved Applications";
            } else if (Constant.FILTER_LEAVE_REJECTED.equals(filterType)) {
                title = "Rejected Applications";
            }
            
            headerTitle.setText(title);
        }
    }
    
    private void updateHeaderColors() {
        int headerColor = R.color.navy_blue;
        
        if (Constant.FILTER_LEAVE_ALL.equals(filterType)) {
            headerColor = R.color.navy_blue;
        } else if (Constant.FILTER_LEAVE_PENDING.equals(filterType)) {
            headerColor = R.color.orange;
        } else if (Constant.FILTER_LEAVE_APPROVED.equals(filterType)) {
            headerColor = R.color.success_500;
        } else if (Constant.FILTER_LEAVE_REJECTED.equals(filterType)) {
            headerColor = R.color.error_500;
        }
        
        View headerLayout = findViewById(R.id.header_layout);
        if (headerLayout != null) {
            headerLayout.setBackgroundColor(ContextCompat.getColor(this, headerColor));
        }
    }
    
    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.navy_blue),
                ContextCompat.getColor(context, R.color.orange)
            );
            
            swipeRefresh.setOnRefreshListener(() -> {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Swipe refresh triggered");
                }
                loadApplications();
                swipeRefresh.setRefreshing(false);
            });
        }
    }

    private void loadApplications() {
        progressBar.setVisibility(View.VISIBLE);
        
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("operation", "read_application_title");
        postParam.put(Constant.KEY_STAFF_ID, Constant.staff_id);
        postParam.put(Constant.KEY_CAMPUS_ID, Constant.campus_id);
        
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Loading applications - staff_id: " + Constant.staff_id);
            Log.d(TAG, "Loading applications - campus_id: " + Constant.campus_id);
        }
        
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.leave_applicaton(body).enqueue(new Callback<StaffApplicationModel>() {
            @Override
            public void onResponse(Call<StaffApplicationModel> call, Response<StaffApplicationModel> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    StaffApplicationModel applicationModel = response.body();
                    
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "API Status: " + (applicationModel.getStatus() != null ? applicationModel.getStatus().getCode() : "null"));
                    }
                    
                    if (applicationModel.getStatus() != null && "1000".equals(applicationModel.getStatus().getCode())) {
                        list = applicationModel.getApplications();
                        
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Applications count before filter: " + (list != null ? list.size() : "null"));
                        }
                        
                        if (list != null && !list.isEmpty()) {
                            list = filterApplications(list, filterType);
                            
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "Applications count after filter: " + list.size());
                            }
                            
                            if (!list.isEmpty()) {
                                if (adapter == null) {
                                    adapter = new ApplicationAdapter(list, context, filterType);
                                    applicationRcv.setAdapter(adapter);
                                } else {
                                    adapter.updateList(list);
                                }
                                totalRecords.setText("Total Applications: " + list.size());
                                showEmptyState(false);
                            } else {
                                totalRecords.setText("Total Applications: 0");
                                showEmptyState(true);
                            }
                        } else {
                            totalRecords.setText("Total Applications: 0");
                            showEmptyState(true);
                        }
                    } else {
                        Toast.makeText(context, 
                            applicationModel.getStatus() != null ? applicationModel.getStatus().getMessage() : "Failed to load applications", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Failed to load applications", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<StaffApplicationModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "API Failure: " + t.getMessage());
                }
                showEmptyState(true);
            }
        });
    }
    
    private List<StaffApplicationModel.Application> filterApplications(List<StaffApplicationModel.Application> applications, String filter) {
        if (Constant.FILTER_LEAVE_ALL.equals(filter)) {
            return applications;
        }
        
        List<StaffApplicationModel.Application> filtered = new ArrayList<>();
        
        for (StaffApplicationModel.Application app : applications) {
            String isActive = app.getIsActive();
            
            if (Constant.FILTER_LEAVE_PENDING.equals(filter) && "0".equals(isActive)) {
                filtered.add(app);
            } else if (Constant.FILTER_LEAVE_APPROVED.equals(filter) && "1".equals(isActive)) {
                filtered.add(app);
            } else if (Constant.FILTER_LEAVE_REJECTED.equals(filter) && "2".equals(isActive)) {
                filtered.add(app);
            }
        }
        
        return filtered;
    }
    
    private void showEmptyState(boolean show) {
        if (show) {
            emptyView.setVisibility(View.VISIBLE);
            applicationRcv.setVisibility(View.GONE);
            if (adapter != null) {
                adapter.updateList(new ArrayList<>());
            }
        } else {
            emptyView.setVisibility(View.GONE);
            applicationRcv.setVisibility(View.VISIBLE);
        }
    }
}

