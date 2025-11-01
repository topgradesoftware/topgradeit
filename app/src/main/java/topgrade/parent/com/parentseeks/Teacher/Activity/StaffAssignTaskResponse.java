package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.HashMap;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.API.UpdateTaskModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffAssignTaskResponse extends AppCompatActivity {

    private static final String TAG = "StaffAssignTaskResponse";

    private EditText response_body;
    private SwitchCompat completed_switch;
    private MaterialButton submit_response;
    private ProgressBar progress_bar;
    private ImageView back_icon;

    // Task details from intent
    private String taskId;
    private String taskTitle;
    private String taskBody;
    private String taskTimestamp;
    private String existingResponse;
    private String isCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_assigntask_response);

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

        Paper.init(this);

        // Initialize views
        response_body = findViewById(R.id.response_body);
        completed_switch = findViewById(R.id.completed_switch);
        submit_response = findViewById(R.id.submit_response);
        progress_bar = findViewById(R.id.progress_bar);
        back_icon = findViewById(R.id.back_icon);

        // Get task details from intent
        Intent intent = getIntent();
        taskId = intent.getStringExtra("task_id");
        taskTitle = intent.getStringExtra("task_title");
        taskBody = intent.getStringExtra("task_body");
        taskTimestamp = intent.getStringExtra("task_timestamp");
        existingResponse = intent.getStringExtra("task_response");
        isCompleted = intent.getStringExtra("is_completed");

        Log.d(TAG, "Task ID: " + taskId);
        Log.d(TAG, "Is Completed: " + isCompleted);

        // Set existing data
        if (!TextUtils.isEmpty(existingResponse) && !existingResponse.equals("null")) {
            response_body.setText(existingResponse);
        }

        if ("1".equals(isCompleted)) {
            completed_switch.setChecked(true);
        }

        // Back button
        back_icon.setOnClickListener(v -> onBackPressed());

        // Submit button
        submit_response.setOnClickListener(v -> submitResponse());
    }

    private void submitResponse() {
        String response = response_body.getText().toString().trim();

        if (TextUtils.isEmpty(response)) {
            Toast.makeText(this, "Please enter a response before submitting", Toast.LENGTH_SHORT).show();
            return;
        }

        String completionStatus = completed_switch.isChecked() ? "1" : "0";
        
        // Show appropriate confirmation dialog based on status change
        if ("1".equals(completionStatus) && !"1".equals(isCompleted)) {
            // Marking as complete
            new AlertDialog.Builder(this)
                .setTitle("Mark as Completed?")
                .setMessage("Are you sure you want to mark this task as completed?")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Yes, Complete", (dialog, which) -> updateTask(completionStatus, response))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
        } else if ("0".equals(completionStatus) && "1".equals(isCompleted)) {
            // Marking as incomplete (reopening)
            new AlertDialog.Builder(this)
                .setTitle("Reopen Task?")
                .setMessage("Are you sure you want to mark this completed task as incomplete?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes, Reopen", (dialog, which) -> updateTask(completionStatus, response))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
        } else {
            // No status change or just updating response
            updateTask(completionStatus, response);
        }
    }

    private void updateTask(String completionStatus, String response) {
        progress_bar.setVisibility(View.VISIBLE);
        submit_response.setEnabled(false);

        // Read values from Paper database
        String staff_id = Paper.book().read("staff_id", "");
        String campus_id = Paper.book().read("campus_id", "");
        String current_session = Paper.book().read("current_session", "");

        if (staff_id.isEmpty() || campus_id.isEmpty() || current_session.isEmpty()) {
            Toast.makeText(this, "Error: Missing user data. Please login again.", Toast.LENGTH_SHORT).show();
            progress_bar.setVisibility(View.GONE);
            submit_response.setEnabled(true);
            return;
        }

        HashMap<String, String> postParam = new HashMap<>();
        postParam.put(Constant.KEY_OPERATION, Constant.OPERATION_UPDATE);
        postParam.put(Constant.KEY_STAFF_ID, staff_id);
        postParam.put(Constant.KEY_CAMPUS_ID, campus_id);
        postParam.put(Constant.KEY_SESSION_ID, current_session);
        postParam.put(Constant.KEY_TASK_ID, taskId);
        postParam.put(Constant.KEY_IS_COMPLETED, completionStatus);
        postParam.put(Constant.KEY_RESPONSE, response);

        Log.d(TAG, "updateTask() - taskId: " + taskId);
        Log.d(TAG, "updateTask() - isCompleted: " + completionStatus);
        Log.d(TAG, "updateTask() - response: " + response);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.update_task(body).enqueue(new Callback<UpdateTaskModel>() {
            @Override
            public void onResponse(Call<UpdateTaskModel> call, Response<UpdateTaskModel> apiResponse) {
                progress_bar.setVisibility(View.GONE);
                submit_response.setEnabled(true);

                Log.d(TAG, "updateTask() - API response code: " + apiResponse.code());

                if (apiResponse.isSuccessful() && apiResponse.body() != null) {
                    UpdateTaskModel updateModel = apiResponse.body();
                    Log.d(TAG, "updateTask() - Status code: " + updateModel.getStatus().getCode());
                    Log.d(TAG, "updateTask() - Status message: " + updateModel.getStatus().getMessage());

                    if (updateModel.getStatus().getCode().equals("1000")) {
                        Toast.makeText(StaffAssignTaskResponse.this, 
                            "Task updated successfully", Toast.LENGTH_SHORT).show();
                        
                        // Return success result
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(StaffAssignTaskResponse.this, 
                            updateModel.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "updateTask() - API error: " + updateModel.getStatus().getMessage());
                    }
                } else {
                    Toast.makeText(StaffAssignTaskResponse.this, 
                        "Failed to update task", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "updateTask() - Response not successful or body is null");
                }
            }

            @Override
            public void onFailure(Call<UpdateTaskModel> call, Throwable t) {
                progress_bar.setVisibility(View.GONE);
                submit_response.setEnabled(true);
                Log.e(TAG, "updateTask() - API call failed: " + t.getMessage());
                Toast.makeText(StaffAssignTaskResponse.this, 
                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Check if there are unsaved changes
        String currentResponse = response_body.getText().toString().trim();
        boolean hasChanges = !currentResponse.equals(existingResponse != null ? existingResponse : "");

        if (hasChanges) {
            new AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Do you want to discard them?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Discard", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
        } else {
            super.onBackPressed();
        }
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
}

