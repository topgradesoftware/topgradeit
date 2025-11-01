package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.Teacher.Adapter.LeaveApplicationsAdapter;
import topgrade.parent.com.parentseeks.Teacher.Activity.ApplicationDetailActivity;

public class LeaveApplicationsListActivity extends AppCompatActivity implements LeaveApplicationsAdapter.OnApplicationClickListener {
    
    private static final String TAG = "LeaveApplicationsList";
    
    // UI Components
    private MaterialToolbar topAppBar;
    private RecyclerView applicationRcv;
    private SwipeRefreshLayout swipeRefresh;
    private CircularProgressIndicator progressBar;
    private View emptyView;
    
    // Data
    private List<StaffApplicationModel.Application> applicationsList = new ArrayList<>();
    private LeaveApplicationsAdapter adapter;
    private Context context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_leave_applications_list);
        
        // Configure status bar for navy blue background with white icons
        setupStatusBar();
        
        // Initialize context and Paper
        context = this;
        Paper.init(context);
        
        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup swipe refresh
        setupSwipeRefresh();
        
        // Load applications
        loadApplications();
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
        topAppBar = findViewById(R.id.topAppBar);
        applicationRcv = findViewById(R.id.application_rcv);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);
        
        // Set dynamic title from intent
        String titleText = getIntent().getStringExtra("HEADER_TITLE");
        if (titleText != null && !titleText.isEmpty()) {
            topAppBar.setTitle(titleText);
            Log.d(TAG, "Header title set from intent: " + titleText);
        } else {
            topAppBar.setTitle("Leave Applications");
            Log.d(TAG, "Header title set to default: Leave Applications");
        }
    }
    
    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        adapter = new LeaveApplicationsAdapter(applicationsList, this);
        applicationRcv.setLayoutManager(new LinearLayoutManager(this));
        applicationRcv.setAdapter(adapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadApplications();
        });
        
        // Set refresh colors to match theme
        swipeRefresh.setColorSchemeResources(R.color.navy_blue);
    }
    
    private void loadApplications() {
        // Check if constants are properly initialized
        if (Constant.staff_id == null || Constant.staff_id.isEmpty()) {
            Log.e(TAG, "staff_id is null or empty!");
            Toast.makeText(this, "Staff ID not found. Please login again.", Toast.LENGTH_LONG).show();
            showEmptyState();
            return;
        }
        
        if (Constant.campus_id == null || Constant.campus_id.isEmpty()) {
            Log.e(TAG, "campus_id is null or empty!");
            Toast.makeText(this, "Campus ID not found. Please login again.", Toast.LENGTH_LONG).show();
            showEmptyState();
            return;
        }
        
        showLoading(true);
        
        // Create API request
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
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    StaffApplicationModel result = response.body();
                    if (result.getStatus() != null) {
                        if (result.getStatus().getCode().equals("1000")) {
                            // Success - update data
                            applicationsList.clear();
                            if (result.getApplications() != null) {
                                applicationsList.addAll(result.getApplications());
                            }
                            adapter.notifyDataSetChanged();
                            
                            // Show/hide empty state
                            if (applicationsList.isEmpty()) {
                                showEmptyState();
                            } else {
                                hideEmptyState();
                            }
                            
                            Log.d(TAG, "Applications loaded successfully: " + applicationsList.size());
                        } else {
                            // API error
                            Toast.makeText(context, result.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }
                    } else {
                        // No status in response
                        Toast.makeText(context, "Invalid response from server", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                } else {
                    // HTTP error
                    Toast.makeText(context, "Failed to load applications", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<StaffApplicationModel> call, Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Network error loading applications", t);
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showEmptyState() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
        if (applicationRcv != null) {
            applicationRcv.setVisibility(View.GONE);
        }
    }
    
    private void hideEmptyState() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
        if (applicationRcv != null) {
            applicationRcv.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload applications when returning to this activity
        loadApplications();
    }
    
    // Click listener methods
    @Override
    public void onApplicationClick(StaffApplicationModel.Application application) {
        // Handle application click - open detail view
        Log.d(TAG, "Application clicked: " + application.getTitle());
        
        Intent intent = new Intent(this, ApplicationDetailActivity.class);
        intent.putExtra("APPLICATION_ID", application.getTitleId());
        intent.putExtra("HEADER_TITLE", "Application Details");
        startActivity(intent);
    }
    
    @Override
    public void onApplicationLongClick(StaffApplicationModel.Application application) {
        // Handle long click - you can show a context menu or delete option
        Log.d(TAG, "Application long clicked: " + application.getTitle());
        
        // TODO: Implement context menu or delete functionality
        // showDeleteConfirmationDialog(application);
    }
}
