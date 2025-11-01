package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.API.AssigntaskModel;
import topgrade.parent.com.parentseeks.Teacher.Model.Task;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffTaskMenu extends AppCompatActivity {

    private static final String TAG = "StaffTaskMenu";
    
    private MaterialButton btn_all_tasks;
    private MaterialButton btn_pending_tasks;
    private MaterialButton btn_incomplete_tasks;
    private MaterialButton btn_complete_tasks;
    private ImageView back_icon;
    
    // Badge counters
    private TextView badge_all_tasks;
    private TextView badge_pending_tasks;
    private TextView badge_incomplete_tasks;
    private TextView badge_complete_tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_task_menu);
        
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
        
        // Initialize Paper database
        Paper.init(this);
        
        // Load constants from Paper
        Constant.loadFromPaper();
        
        // Initialize views
        btn_all_tasks = findViewById(R.id.btn_all_tasks);
        btn_pending_tasks = findViewById(R.id.btn_pending_tasks);
        btn_incomplete_tasks = findViewById(R.id.btn_incomplete_tasks);
        btn_complete_tasks = findViewById(R.id.btn_complete_tasks);
        back_icon = findViewById(R.id.back_icon);
        
        // Initialize badges
        badge_all_tasks = findViewById(R.id.badge_all_tasks);
        badge_pending_tasks = findViewById(R.id.badge_pending_tasks);
        badge_incomplete_tasks = findViewById(R.id.badge_incomplete_tasks);
        badge_complete_tasks = findViewById(R.id.badge_complete_tasks);
        
        // Set click listeners
        back_icon.setOnClickListener(v -> finish());
        
        btn_all_tasks.setOnClickListener(v -> openTaskList(Constant.FILTER_ALL));
        btn_pending_tasks.setOnClickListener(v -> openTaskList(Constant.FILTER_PENDING));
        btn_incomplete_tasks.setOnClickListener(v -> openTaskList(Constant.FILTER_INCOMPLETE));
        btn_complete_tasks.setOnClickListener(v -> openTaskList(Constant.FILTER_COMPLETED));
        
        // Load task counts
        loadTaskCounts();
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
    
    /**
     * Open task list with specific filter
     */
    private void openTaskList(String filterType) {
        Intent intent = new Intent(this, StaffTask.class);
        intent.putExtra("filter_type", filterType);
        startActivity(intent);
    }
    
    /**
     * Load task counts from API and update badges
     */
    private void loadTaskCounts() {
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put(Constant.KEY_OPERATION, Constant.OPERATION_READ);
        postParam.put(Constant.KEY_STAFF_ID, Constant.staff_id);
        postParam.put(Constant.KEY_CAMPUS_ID, Constant.campus_id);
        postParam.put(Constant.KEY_SESSION_ID, Constant.current_session);
        
        Log.d(TAG, "loadTaskCounts() - staff_id: " + Constant.staff_id);
        
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.assign_task(body).enqueue(new Callback<AssigntaskModel>() {
            @Override
            public void onResponse(Call<AssigntaskModel> call, Response<AssigntaskModel> response) {
                Log.d(TAG, "loadTaskCounts() - API response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    AssigntaskModel taskModel = response.body();
                    
                    if (taskModel.getStatus().getCode().equals("1000")) {
                        List<Task> allTasks = taskModel.getTask();
                        
                        if (allTasks != null && !allTasks.isEmpty()) {
                            // Count tasks by category
                            int pendingCount = 0;
                            int incompleteCount = 0;
                            int completedCount = 0;
                            
                            for (Task task : allTasks) {
                                String isCompleted = task.getIsCompleted();
                                String responseText = task.getTaskResponse();
                                boolean hasResponse = !TextUtils.isEmpty(responseText) && !responseText.equals("null");
                                
                                if ("1".equals(isCompleted)) {
                                    completedCount++;
                                } else if (hasResponse) {
                                    incompleteCount++;
                                } else {
                                    pendingCount++;
                                }
                            }
                            
                            // Update badges
                            updateBadges(allTasks.size(), pendingCount, incompleteCount, completedCount);
                            
                            Log.d(TAG, "Task counts - All: " + allTasks.size() + 
                                      ", Pending: " + pendingCount + 
                                      ", Incomplete: " + incompleteCount + 
                                      ", Completed: " + completedCount);
                        } else {
                            // No tasks found
                            updateBadges(0, 0, 0, 0);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AssigntaskModel> call, Throwable t) {
                Log.e(TAG, "loadTaskCounts() - API call failed: " + t.getMessage());
                // Keep badges at 0 on error
            }
        });
    }
    
    /**
     * Update badge counters
     */
    private void updateBadges(int allCount, int pendingCount, int incompleteCount, int completedCount) {
        if (badge_all_tasks != null) {
            badge_all_tasks.setText(String.valueOf(allCount));
        }
        if (badge_pending_tasks != null) {
            badge_pending_tasks.setText(String.valueOf(pendingCount));
        }
        if (badge_incomplete_tasks != null) {
            badge_incomplete_tasks.setText(String.valueOf(incompleteCount));
        }
        if (badge_complete_tasks != null) {
            badge_complete_tasks.setText(String.valueOf(completedCount));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload counts when returning to this screen
        loadTaskCounts();
    }
}

