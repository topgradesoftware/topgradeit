package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

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
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.FeebackAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Model.Feedback;
import topgrade.parent.com.parentseeks.Teacher.Model.FeedbackModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.Teacher.Activity.AddFeedback;

public class FeedbackList extends AppCompatActivity implements OnClickListener {


    MaterialButton fab;
    private ProgressBar progress_bar;
    Context context;
    RecyclerView feedback_rcv;
    List<Feedback> feedback_list = new ArrayList<>();

    String feedbackId;
    String child_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_feedback_list);
        
        // Configure status bar for navy blue background with white icons - COMPREHENSIVE FIX
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

        // Additional fix for older Android versions - Force white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Ensure status bar icons are light (white) on dark background
            getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility() & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();
        context = FeedbackList.this;
        Paper.init(context);
        
        // Load constants from Paper database first
        Constant.loadFromPaper();
        
        // Debug: Log what was loaded from Paper
        Log.d("FeedbackList", "=== PAPER LOADING DEBUG ===");
        Log.d("FeedbackList", "staff_id from Paper: '" + Constant.staff_id + "'");
        Log.d("FeedbackList", "campus_id from Paper: '" + Constant.campus_id + "'");
        Log.d("FeedbackList", "current_session from Paper: '" + Constant.current_session + "'");
        Log.d("FeedbackList", "================================");
        
        // Initialize views first
        progress_bar = findViewById(R.id.progress_bar);
        feedback_rcv = findViewById(R.id.feedback_rcv);
        fab = findViewById(R.id.fab);
        
        // Initialize header title
        TextView headerTitle = findViewById(R.id.header_title);
        if (headerTitle != null) {
            // Set dynamic title from intent or use default
            String titleText = getIntent().getStringExtra("HEADER_TITLE");
            if (titleText != null && !titleText.isEmpty()) {
                headerTitle.setText(titleText);
                Log.d("FeedbackList", "Header title set from intent: " + titleText);
            } else {
                headerTitle.setText("Feedback List");
                Log.d("FeedbackList", "Header title set to default: Feedback List");
            }
        } else {
            Log.e("FeedbackList", "header_title not found in layout!");
        }
        
        // Check if views were found
        if (progress_bar == null) {
            Log.e("FeedbackList", "progress_bar not found in layout!");
        }
        if (feedback_rcv == null) {
            Log.e("FeedbackList", "feedback_rcv not found in layout!");
        }
        if (fab == null) {
            Log.e("FeedbackList", "fab not found in layout!");
        } else {
            Log.d("FeedbackList", "FAB found successfully: " + fab.getId());
        }
        
        // Check if constants are properly initialized
        if (Constant.staff_id == null || Constant.staff_id.isEmpty()) {
            Log.e("FeedbackList", "staff_id is null or empty!");
            Toast.makeText(this, "Staff ID not found. Please login again.", Toast.LENGTH_LONG).show();
            // Show fallback data instead of returning
            showFallbackFeedbackData();
            return;
        }
        
        if (Constant.campus_id == null || Constant.campus_id.isEmpty()) {
            Log.e("FeedbackList", "campus_id is null or empty!");
            Toast.makeText(this, "Campus ID not found. Please login again.", Toast.LENGTH_LONG).show();
            // Show fallback data instead of returning
            showFallbackFeedbackData();
            return;
        }
        
        if (Constant.current_session == null || Constant.current_session.isEmpty()) {
            Log.e("FeedbackList", "current_session is null or empty!");
            Toast.makeText(this, "Session not found. Please login again.", Toast.LENGTH_LONG).show();
            // Show fallback data instead of returning
            showFallbackFeedbackData();
            return;
        }
        
        Log.d("FeedbackList", "Constants initialized - staff_id: " + Constant.staff_id + ", campus_id: " + Constant.campus_id + ", session: " + Constant.current_session);
        
        // Setup click listeners only if views are found
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("FeedbackList", "FAB clicked! Starting AddFeedback activity...");
                    startActivity(new Intent(FeedbackList.this, AddFeedback.class));
                }
            });
            Log.d("FeedbackList", "FAB click listener set successfully");
        } else {
            Log.e("FeedbackList", "Cannot set click listener - FAB is null!");
        }

        ImageView back_icon = findViewById(R.id.back_icon);
        if (back_icon != null) {
            back_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        
        // Load real feedback data instead of showing fallback data
        Log.d("FeedbackList", "Loading real feedback data...");
        
        // Debug: Check if we should show fallback data
        if (Constant.staff_id == null || Constant.staff_id.isEmpty() || 
            Constant.campus_id == null || Constant.campus_id.isEmpty() || 
            Constant.current_session == null || Constant.current_session.isEmpty()) {
            Log.e("FeedbackList", "Constants still missing after loadFromPaper, showing fallback data");
            showFallbackFeedbackData();
        } else {
            read_feedback();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        
        // Only call read_feedback if views are properly initialized AND we don't already have data
        if (progress_bar != null && feedback_rcv != null && (feedback_list == null || feedback_list.isEmpty())) {
            Log.d("FeedbackList", "onResume: No existing data, calling read_feedback");
            read_feedback();
        } else if (feedback_list != null && !feedback_list.isEmpty()) {
            Log.d("FeedbackList", "onResume: Data already exists, skipping read_feedback");
        } else {
            Log.w("FeedbackList", "onResume: Views not initialized, skipping read_feedback");
        }
    }

    private void read_feedback() {
        Log.d("FeedbackList", "Starting read_feedback...");
        
        // Check if views are properly initialized
        if (progress_bar == null) {
            Log.e("FeedbackList", "progress_bar is null, cannot proceed with read_feedback");
            Toast.makeText(context, "Error: Progress bar not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (feedback_rcv == null) {
            Log.e("FeedbackList", "feedback_rcv is null, cannot proceed with read_feedback");
            Toast.makeText(context, "Error: RecyclerView not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log all constants to verify they are set
        Log.d("FeedbackList", "=== CONSTANTS CHECK ===");
        Log.d("FeedbackList", "staff_id: '" + Constant.staff_id + "'");
        Log.d("FeedbackList", "campus_id: '" + Constant.campus_id + "'");
        Log.d("FeedbackList", "session_id: '" + Constant.current_session + "'");
        Log.d("FeedbackList", "=====================");

        // Check if constants are valid
        if (Constant.staff_id == null || Constant.staff_id.isEmpty()) {
            Log.e("FeedbackList", "staff_id is null or empty!");
            showEmptyState("Staff ID not found. Please login again.");
            return;
        }

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("campus_id", Constant.campus_id);
        postParam.put("operation", "read_feedback");
        postParam.put("session_id", Constant.current_session);

        Log.d("FeedbackList", "API parameters: " + postParam.toString());

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

        Log.d("FeedbackList", "Request body: " + (new JSONObject(postParam)).toString());

        Constant.mApiService.feedback(body).enqueue(new Callback<FeedbackModel>() {
            @Override
            public void onResponse(Call<FeedbackModel> call, Response<FeedbackModel> response) {
                Log.d("FeedbackList", "=== API RESPONSE RECEIVED ===");
                Log.d("FeedbackList", "Response code: " + response.code());
                Log.d("FeedbackList", "Response message: " + response.message());
                
                if (progress_bar != null) {
                    progress_bar.setVisibility(View.GONE);
                }
                
                if (response.body() != null) {
                    Log.d("FeedbackList", "Response body is NOT null");
                    Log.d("FeedbackList", "Response body status code: " + response.body().getStatus().getCode());
                    Log.d("FeedbackList", "Response body status message: " + response.body().getStatus().getMessage());
                    
                    // Check if we have feedback data
                    List<Feedback> feedbackData = response.body().getFeedback();
                    Log.d("FeedbackList", "Raw feedback data: " + feedbackData);
                    Log.d("FeedbackList", "Feedback list size: " + (feedbackData != null ? feedbackData.size() : "null"));

                    if (response.body().getStatus().getCode().equals("1000")) {
                        feedback_list = feedbackData;
                        
                        if (feedback_list != null && !feedback_list.isEmpty()) {
                            Log.d("FeedbackList", "Setting adapter with " + feedback_list.size() + " items");
                            
                            // Ensure RecyclerView is properly configured
                            if (feedback_rcv.getLayoutManager() == null) {
                                Log.d("FeedbackList", "Setting LinearLayoutManager for RecyclerView");
                                feedback_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(FeedbackList.this));
                            }
                            
                            // Create and set adapter
                            FeebackAdaptor adapter = new FeebackAdaptor(feedback_list, FeedbackList.this);
                            feedback_rcv.setAdapter(adapter);
                            
                            // Update feedback counter
                            TextView feedbackCount = findViewById(R.id.feedback_count);
                            if (feedbackCount != null) {
                                feedbackCount.setText(String.valueOf(feedback_list.size()));
                            }
                            
                            // Force layout update
                            feedback_rcv.requestLayout();
                            feedback_rcv.invalidate();
                            
                            Log.d("FeedbackList", "Feedback adapter set successfully");
                            Log.d("FeedbackList", "RecyclerView child count: " + feedback_rcv.getChildCount());
                            Log.d("FeedbackList", "Adapter item count: " + adapter.getItemCount());
                            
                            // Additional debugging for RecyclerView state
                            Log.d("FeedbackList", "RecyclerView visibility: " + (feedback_rcv.getVisibility() == View.VISIBLE ? "VISIBLE" : "NOT VISIBLE"));
                            Log.d("FeedbackList", "RecyclerView width: " + feedback_rcv.getWidth() + ", height: " + feedback_rcv.getHeight());
                            Log.d("FeedbackList", "RecyclerView is enabled: " + feedback_rcv.isEnabled());
                            Log.d("FeedbackList", "RecyclerView has focus: " + feedback_rcv.hasFocus());
                            
                            // Log first few items for debugging
                            for (int i = 0; i < Math.min(feedback_list.size(), 3); i++) {
                                Feedback item = feedback_list.get(i);
                                Log.d("FeedbackList", "Item " + i + ": ID=" + item.getFeedbackId() + 
                                      ", Text=" + item.getFeedback() + 
                                      ", SharedStudent=" + item.getChildName());
                            }
                            
                            // Post a delayed check to see if items are displayed
                            feedback_rcv.postDelayed(() -> {
                                Log.d("FeedbackList", "=== DELAYED CHECK ===");
                                Log.d("FeedbackList", "RecyclerView child count after delay: " + feedback_rcv.getChildCount());
                                Log.d("FeedbackList", "Adapter item count after delay: " + adapter.getItemCount());
                                Log.d("FeedbackList", "RecyclerView is attached to window: " + feedback_rcv.isAttachedToWindow());
                                Log.d("FeedbackList", "=====================");
                            }, 1000);
                            
                        } else {
                            Log.w("FeedbackList", "No feedback items found in response");
                            showEmptyState("No feedback found for this session. Click the button below to add feedback about students.");
                        }

                    } else {
                        Log.e("FeedbackList", "API call failed with status: " + response.body().getStatus().getCode());
                        Log.e("FeedbackList", "API error message: " + response.body().getStatus().getMessage());
                        // Show empty state instead of fallback data for API errors
                        Log.d("FeedbackList", "API returned error status, showing empty state...");
                        showEmptyState("API Error: " + response.body().getStatus().getMessage());
                    }
                } else {
                    Log.e("FeedbackList", "Response body is NULL");
                    Log.e("FeedbackList", "Raw response: " + response.raw());
                    Log.e("FeedbackList", "Raw message: " + response.raw().message());
                    // Show empty state instead of fallback data for null response
                    Log.d("FeedbackList", "API returned empty response, showing empty state...");
                    showEmptyState("No response from server. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<FeedbackModel> call, Throwable e) {
                Log.e("FeedbackList", "=== API CALL FAILED ===");
                Log.e("FeedbackList", "Error message: " + e.getMessage());
                Log.e("FeedbackList", "Error type: " + e.getClass().getSimpleName());
                e.printStackTrace();
                
                if (progress_bar != null) {
                    progress_bar.setVisibility(View.GONE);
                }
                
                // Show fallback data only on network failures
                Log.d("FeedbackList", "API failed, showing fallback data...");
                showFallbackFeedbackData();
            }
        });
    }
    
    private void showEmptyState(String message) {
        Log.d("FeedbackList", "Showing empty state with message: " + message);
        
        // Clear the list and show empty state
        feedback_list.clear();
        if (feedback_rcv != null) {
            // Ensure RecyclerView is properly configured
            if (feedback_rcv.getLayoutManager() == null) {
                Log.d("FeedbackList", "Setting LinearLayoutManager for empty state RecyclerView");
                feedback_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(FeedbackList.this));
            }
            
            FeebackAdaptor adapter = new FeebackAdaptor(feedback_list, FeedbackList.this);
            feedback_rcv.setAdapter(adapter);
            
            // Force layout update
            feedback_rcv.requestLayout();
            feedback_rcv.invalidate();
            
            Log.d("FeedbackList", "Empty state adapter set with " + feedback_list.size() + " items");
        }
        
        // Show the no_record text with the message
        TextView noRecordText = findViewById(R.id.no_record);
        if (noRecordText != null) {
            noRecordText.setText(message);
            noRecordText.setVisibility(View.VISIBLE);
        }
        
        // Show a toast message
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Show fallback feedback data when API fails
     * This ensures users see something instead of an empty page
     */
    private void showFallbackFeedbackData() {
        Log.d("FeedbackList", "Showing fallback feedback data...");
        
        // Check if we already have real data loaded
        if (feedback_list != null && !feedback_list.isEmpty()) {
            Log.d("FeedbackList", "Real data already loaded, skipping fallback data");
            return;
        }
        
        // Create sample feedback data
        feedback_list.clear();
        
        Feedback sample1 = new Feedback();
        sample1.setFeedbackId(1);
        sample1.setChildId("101");
        sample1.setChildName("Ayesha Noor");
        sample1.setSubject_name("Biology");
        sample1.setFeedback("Excellent performance in recent biology test. Shows great understanding of concepts.");
        sample1.setTimestamp("2024-01-15 10:30:00");
        
        Feedback sample2 = new Feedback();
        sample2.setFeedbackId(2);
        sample2.setChildId("102");
        sample2.setChildName("Ahmed Khan");
        sample2.setSubject_name("Mathematics");
        sample2.setFeedback("Good progress in algebra. Needs more practice with quadratic equations.");
        sample2.setTimestamp("2024-01-14 14:20:00");
        
        Feedback sample3 = new Feedback();
        sample3.setFeedbackId(3);
        sample3.setChildId("103");
        sample3.setChildName("Fatima Ali");
        sample3.setSubject_name("English");
        sample3.setFeedback("Outstanding essay writing skills. Creative and well-structured responses.");
        sample3.setTimestamp("2024-01-13 09:15:00");
        
        feedback_list.add(sample1);
        feedback_list.add(sample2);
        feedback_list.add(sample3);
        
        // Set adapter with sample data
        if (feedback_rcv != null) {
            // Ensure RecyclerView is properly configured
            if (feedback_rcv.getLayoutManager() == null) {
                Log.d("FeedbackList", "Setting LinearLayoutManager for fallback RecyclerView");
                feedback_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(FeedbackList.this));
            }
            
            FeebackAdaptor adapter = new FeebackAdaptor(feedback_list, FeedbackList.this);
            feedback_rcv.setAdapter(adapter);
            
            // Update feedback counter
            TextView feedbackCount = findViewById(R.id.feedback_count);
            if (feedbackCount != null) {
                feedbackCount.setText(String.valueOf(feedback_list.size()));
            }
            
            // Force layout update
            feedback_rcv.requestLayout();
            feedback_rcv.invalidate();
            
            Log.d("FeedbackList", "Fallback adapter set with " + feedback_list.size() + " sample items");
            Log.d("FeedbackList", "Fallback RecyclerView child count: " + feedback_rcv.getChildCount());
            Log.d("FeedbackList", "Fallback adapter item count: " + adapter.getItemCount());
        } else {
            Log.e("FeedbackList", "feedback_rcv is null in showFallbackFeedbackData!");
        }
        
        // Hide no record text
        TextView noRecordText = findViewById(R.id.no_record);
        if (noRecordText != null) {
            noRecordText.setVisibility(View.GONE);
        }
        
        // Show info toast
        Toast.makeText(context, "Showing sample feedback data. Please check your connection and try again.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemClick(View view, int position) {


        pop_uop_menu(view, position);
    }

    private void pop_uop_menu(View view, final int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.feedback_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();
                if (id == R.id.Delete) {


                    feedbackId = "" + feedback_list.get(position).getFeedbackId();
                    child_id = feedback_list.get(position).getChildId();

                    delete_feedback();


                }


                return true;
            }
        });

        popup.show();
    }

    private void delete_feedback() {

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("campus_id", Constant.campus_id);
        postParam.put("child_id", child_id);
        postParam.put("feedback_id", feedbackId);
        postParam.put("operation", "delete_feedback");

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

        Constant.mApiService.feedback(body).enqueue(new Callback<FeedbackModel>() {
            @Override
            public void onResponse(Call<FeedbackModel> call, Response<FeedbackModel> response) {
                if (response.body() != null) {

                    if (response.body().getStatus().getCode().equals("1000")) {

                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        progress_bar.setVisibility(View.GONE);
                        read_feedback();
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    progress_bar.setVisibility(View.GONE);

                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<FeedbackModel> call, Throwable e) {
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
            }
        });
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

                        // Add bottom margin to FAB to push it above navigation bar
                        MaterialButton fab = findViewById(R.id.fab);
                        if (fab != null) {
                            // Set bottom margin to navigation bar height to ensure FAB is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) fab.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 24; // 24dp original margin + navigation bar height
                                fab.setLayoutParams(params);
                            }
                        }

                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        android.util.Log.e("FeedbackList", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                android.util.Log.e("FeedbackList", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            android.util.Log.e("FeedbackList", "Error setting up window insets: " + e.getMessage(), e);
        }
    }

}
