package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.android.material.snackbar.Snackbar;

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
import topgrade.parent.com.parentseeks.Teacher.Adaptor.TaskAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Model.API.AssigntaskModel;
import topgrade.parent.com.parentseeks.Teacher.Model.API.UpdateTaskModel;
import topgrade.parent.com.parentseeks.Teacher.Model.Task;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffTask extends AppCompatActivity implements View.OnClickListener, TaskUpdateListener {

    private static final String TAG = "StaffTask";

    RecyclerView task_rcv;
    ProgressBar progress_bar;
    Context context;
    List<Task> list = new ArrayList<>();
    TextView total_records;
    TextView header_title;
    TaskAdaptor adapter;
    View empty_view;
    SwipeRefreshLayout swipe_refresh;
    
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
        
        setContentView(R.layout.activity_staff_task);
        
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

        context = StaffTask.this;
        Paper.init(context);
        
        // Load constants from Paper database
        Constant.loadFromPaper();
        
        // Get filter type from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("filter_type")) {
            filterType = intent.getStringExtra("filter_type");
            log("Filter type: " + filterType);
        }

        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(v -> finish());

        Initlization();
        
        // Update title and colors based on filter type
        updateHeaderTitle();
        updateHeaderColors();
        
        // Load cached tasks first for instant display
        loadInitialCachedTasks();
        
        // Then load fresh data from API
        Load_Task();
    }
    
    /**
     * Setup window insets to respect system bars - EXACT COPY FROM WORKING StaffApplicationMenu
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

    private void Initlization() {
        progress_bar = findViewById(R.id.progress_bar);
        task_rcv = findViewById(R.id.task_rcv);
        total_records = findViewById(R.id.total_records);
        header_title = findViewById(R.id.header_title);
        empty_view = findViewById(R.id.empty_view);
        swipe_refresh = findViewById(R.id.swipe_refresh);
        
        // Setup RecyclerView with LinearLayoutManager
        task_rcv.setLayoutManager(new LinearLayoutManager(this));
        
        // Setup swipe refresh
        setupSwipeRefresh();
    }
    
    /**
     * Update header title based on filter type
     */
    private void updateHeaderTitle() {
        if (header_title != null) {
            String title = "View All Tasks";
            
            if (Constant.FILTER_ALL.equals(filterType)) {
                title = "View All Tasks";
            } else if (Constant.FILTER_PENDING.equals(filterType)) {
                title = "Pending Tasks";
            } else if (Constant.FILTER_INCOMPLETE.equals(filterType)) {
                title = "Incomplete";
            } else if (Constant.FILTER_COMPLETED.equals(filterType)) {
                title = "Completed";
            }
            
            header_title.setText(title);
        }
    }
    
    /**
     * Update header colors based on filter type
     * All task views (View All, Pending, Incomplete, Complete) use staff theme (navy blue)
     */
    private void updateHeaderColors() {
        ImageView headerWave = findViewById(R.id.header_wave);
        
        // All task filter types use staff theme (navy blue)
        // This includes: View All Tasks, Pending, Incomplete, and Completed
        if (headerWave != null) {
            headerWave.setImageResource(R.drawable.bg_wave_navy_blue);
        }
        
        // Always ensure status bar and navigation bar use staff theme (navy blue)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
            
            // Ensure white status bar icons on dark background
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }
        
        // Configure status bar icons for Android R and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
    }
    
    private void setupSwipeRefresh() {
        if (swipe_refresh != null) {
            // Set swipe refresh colors to match app theme
            swipe_refresh.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.navy_blue),
                ContextCompat.getColor(context, R.color.orange)
            );
            
            swipe_refresh.setOnRefreshListener(() -> {
                log("Swipe refresh triggered");
                Load_Task();
                swipe_refresh.setRefreshing(false);
            });
        }
    }

    private void Load_Task() {
        progress_bar.setVisibility(View.VISIBLE);
        
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put(Constant.KEY_OPERATION, Constant.OPERATION_READ);
        postParam.put(Constant.KEY_STAFF_ID, Constant.staff_id);
        postParam.put(Constant.KEY_CAMPUS_ID, Constant.campus_id);
        postParam.put(Constant.KEY_SESSION_ID, Constant.current_session);
        
        Log.d(TAG, "Load_Task() - staff_id: " + Constant.staff_id);
        Log.d(TAG, "Load_Task() - campus_id: " + Constant.campus_id);
        Log.d(TAG, "Load_Task() - session_id: " + Constant.current_session);
        Log.d(TAG, "Load_Task() - filterType: " + filterType);
        
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Log.d(TAG, "Load_Task() - API request body: " + (new JSONObject(postParam)).toString());

        Constant.mApiService.assign_task(body).enqueue(new Callback<AssigntaskModel>() {
            @Override
            public void onResponse(Call<AssigntaskModel> call, Response<AssigntaskModel> response) {
                Log.d(TAG, "Load_Task() - API response code: " + response.code());
                progress_bar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    AssigntaskModel taskModel = response.body();
                    Log.d(TAG, "Load_Task() - Status code: " + taskModel.getStatus().getCode());
                    Log.d(TAG, "Load_Task() - Status message: " + taskModel.getStatus().getMessage());
                    
                    if (taskModel.getStatus().getCode().equals("1000")) {
                        list = taskModel.getTask();
                        Log.d(TAG, "Load_Task() - Tasks count before filter: " + (list != null ? list.size() : "null"));
                        
                        if (list != null && !list.isEmpty()) {
                            // Filter tasks based on filter type
                            list = filterTasks(list, filterType);
                            Log.d(TAG, "Load_Task() - Tasks count after filter: " + list.size());
                            
                            if (!list.isEmpty()) {
                                // Cache the tasks for offline access
                                cacheTasks(list);
                                
                                // Reuse adapter instead of recreating
                                if (adapter == null) {
                                    adapter = new TaskAdaptor(list, context, StaffTask.this, filterType);
                                    task_rcv.setAdapter(adapter);
                                    log("Load_Task() - New adapter created and set");
                                } else {
                                    adapter.updateList(list);
                                    log("Load_Task() - Adapter list updated");
                                }
                                total_records.setText("Total Tasks: " + list.size());
                                showEmptyState(false);
                                log("Load_Task() - Adapter set successfully");
                            } else {
                                // Filtered list is empty
                                total_records.setText("Total Tasks: 0");
                                showEmptyState(true);
                                log("Load_Task() - No tasks found after filtering");
                            }
                        } else {
                            total_records.setText("Total Tasks: 0");
                            showEmptyState(true);
                            log("Load_Task() - No tasks found");
                        }
                    } else {
                        Toast.makeText(context, taskModel.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Load_Task() - API error: " + taskModel.getStatus().getMessage());
                    }
                } else {
                    Toast.makeText(context, "Failed to load tasks", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Load_Task() - Response not successful or body is null");
                }
            }

            @Override
            public void onFailure(Call<AssigntaskModel> call, Throwable t) {
                progress_bar.setVisibility(View.GONE);
                log("Load_Task() - API call failed: " + t.getMessage());
                
                // Try to load cached tasks as fallback
                List<Task> cachedTasks = loadCachedTasks();
                if (!cachedTasks.isEmpty()) {
                    log("Showing cached tasks due to network failure");
                    list = filterTasks(cachedTasks, filterType);
                    
                    if (adapter == null) {
                        adapter = new TaskAdaptor(list, context, StaffTask.this, filterType);
                        task_rcv.setAdapter(adapter);
                    } else {
                        adapter.updateList(list);
                    }
                    total_records.setText("Total Tasks: " + list.size() + " (Cached)");
                    showEmptyState(false);
                    
                    // Show info about cached data
                    Snackbar.make(task_rcv, "Showing cached data. Pull to refresh when online.", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(ContextCompat.getColor(context, R.color.orange))
                        .show();
                } else {
                    // No cached data available
                    showEmptyState(true);
                    
                    // Show retry Snackbar
                    Snackbar snackbar = Snackbar.make(task_rcv, "Network error: " + t.getMessage(), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", v -> Load_Task())
                        .setActionTextColor(ContextCompat.getColor(context, R.color.white))
                        .setBackgroundTint(ContextCompat.getColor(context, R.color.navy_blue));
                    snackbar.show();
                }
            }
        });
    }
    
    /**
     * Filter tasks based on filter type
     */
    private List<Task> filterTasks(List<Task> allTasks, String filterType) {
        if (TextUtils.isEmpty(filterType)) {
            return allTasks;
        }
        
        // If filter is "all", return all tasks without filtering
        if (Constant.FILTER_ALL.equals(filterType)) {
            Log.d(TAG, "filterTasks() - Filter type 'all', returning all tasks: " + allTasks.size());
            return allTasks;
        }
        
        List<Task> filteredList = new ArrayList<>();
        
        for (Task task : allTasks) {
            String isCompleted = task.getIsCompleted();
            String response = task.getTaskResponse();
            boolean hasResponse = !TextUtils.isEmpty(response) && !response.equals("null");
            
            switch (filterType) {
                case Constant.FILTER_PENDING:
                    // Pending: no response and not completed
                    if (!hasResponse && !"1".equals(isCompleted)) {
                        filteredList.add(task);
                    }
                    break;
                    
                case Constant.FILTER_INCOMPLETE:
                    // Incomplete: has response but not completed
                    if (hasResponse && !"1".equals(isCompleted)) {
                        filteredList.add(task);
                    }
                    break;
                    
                case Constant.FILTER_COMPLETED:
                    // Completed: marked as complete
                    if ("1".equals(isCompleted)) {
                        filteredList.add(task);
                    }
                    break;
                    
                default:
                    filteredList.add(task);
                    break;
            }
        }
        
        Log.d(TAG, "filterTasks() - Original count: " + allTasks.size() + ", Filtered count: " + filteredList.size());
        return filteredList;
    }
    
    /**
     * Update task status and response
     */
    public void updateTask(String taskId, String isCompleted, String response) {
        progress_bar.setVisibility(View.VISIBLE);
        
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put(Constant.KEY_OPERATION, Constant.OPERATION_UPDATE);
        postParam.put(Constant.KEY_STAFF_ID, Constant.staff_id);
        postParam.put(Constant.KEY_CAMPUS_ID, Constant.campus_id);
        postParam.put(Constant.KEY_SESSION_ID, Constant.current_session);
        postParam.put(Constant.KEY_TASK_ID, taskId);
        postParam.put(Constant.KEY_IS_COMPLETED, isCompleted);
        postParam.put(Constant.KEY_RESPONSE, response);
        
        Log.d(TAG, "updateTask() - taskId: " + taskId);
        Log.d(TAG, "updateTask() - isCompleted: " + isCompleted);
        Log.d(TAG, "updateTask() - response: " + response);
        
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Log.d(TAG, "updateTask() - API request body: " + (new JSONObject(postParam)).toString());

        Constant.mApiService.update_task(body).enqueue(new Callback<UpdateTaskModel>() {
            @Override
            public void onResponse(Call<UpdateTaskModel> call, Response<UpdateTaskModel> response) {
                Log.d(TAG, "updateTask() - API response code: " + response.code());
                progress_bar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    UpdateTaskModel updateModel = response.body();
                    Log.d(TAG, "updateTask() - Status code: " + updateModel.getStatus().getCode());
                    Log.d(TAG, "updateTask() - Status message: " + updateModel.getStatus().getMessage());
                    
                    if (updateModel.getStatus().getCode().equals("1000")) {
                        Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show();
                        // Refresh the task list
                        Load_Task();
                    } else {
                        Toast.makeText(context, updateModel.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "updateTask() - API error: " + updateModel.getStatus().getMessage());
                    }
                } else {
                    Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "updateTask() - Response not successful or body is null");
                }
            }

            @Override
            public void onFailure(Call<UpdateTaskModel> call, Throwable t) {
                progress_bar.setVisibility(View.GONE);
                log("updateTask() - API call failed: " + t.getMessage());
                
                // Show retry Snackbar
                Snackbar snackbar = Snackbar.make(task_rcv, "Failed to update task: " + t.getMessage(), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry", v -> updateTask(taskId, isCompleted, response))
                    .setActionTextColor(ContextCompat.getColor(context, R.color.white))
                    .setBackgroundTint(ContextCompat.getColor(context, R.color.navy_blue));
                snackbar.show();
            }
        });
    }
    
    /**
     * Show or hide empty state view
     */
    private void showEmptyState(boolean show) {
        if (empty_view != null && task_rcv != null) {
            empty_view.setVisibility(show ? View.VISIBLE : View.GONE);
            task_rcv.setVisibility(show ? View.GONE : View.VISIBLE);
            
            // Clear adapter when showing empty state
            if (show && adapter != null) {
                adapter.updateList(new ArrayList<>());
            }
        }
    }
    
    /**
     * Cache tasks to PaperDB
     */
    private void cacheTasks(List<Task> tasks) {
        try {
            if (tasks != null && !tasks.isEmpty()) {
                Paper.book().write("staff_tasks_cache", tasks);
                log("Tasks cached successfully: " + tasks.size() + " items");
            }
        } catch (Exception e) {
            log("Error caching tasks: " + e.getMessage());
        }
    }
    
    /**
     * Load cached tasks from PaperDB
     */
    private List<Task> loadCachedTasks() {
        try {
            List<Task> cachedTasks = Paper.book().read("staff_tasks_cache", new ArrayList<>());
            if (cachedTasks != null && !cachedTasks.isEmpty()) {
                log("Loaded cached tasks: " + cachedTasks.size() + " items");
                return cachedTasks;
            }
        } catch (Exception e) {
            log("Error loading cached tasks: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    /**
     * Load cached tasks on activity start
     */
    private void loadInitialCachedTasks() {
        List<Task> cachedTasks = loadCachedTasks();
        if (!cachedTasks.isEmpty()) {
            log("Loading initial cached tasks: " + cachedTasks.size() + " items");
            list = filterTasks(cachedTasks, filterType);
            
            if (adapter == null) {
                adapter = new TaskAdaptor(list, context, StaffTask.this, filterType);
                task_rcv.setAdapter(adapter);
            } else {
                adapter.updateList(list);
            }
            total_records.setText("Total Tasks: " + list.size() + " (Loading...)");
            showEmptyState(false);
        }
    }
    
    /**
     * Refresh task list
     */
    public void refreshTasks() {
        Load_Task();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Refresh when returning from response screen
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Load_Task();
        }
    }

    @Override
    public void onClick(View v) {
        // Handle click events if needed
    }
}
