package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import io.paperdb.Paper;

public class FeedbackMenu extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FeedbackMenu";
    
    private Context context;
    private CardView submitFeedbackCard;
    private CardView viewFeedbackCard;
    private View totalFeedbackCard;
    private View thisMonthCard;
    private TextView totalFeedbackCount;
    private TextView thisMonthCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_feedback_menu);
        
        // Configure status bar for navy blue background with white icons
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
                    0, // No light icons for status bar and navigation bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        // For older Android versions, ensure white icons on dark background
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();
        
        context = this;
        initializeViews();
        setupClickListeners();
        loadFeedbackStatistics();
    }

    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
     */
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            
            if (rootLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                        );

                        // Add bottom margin to footer container to push it above navigation bar
                        android.widget.LinearLayout footerContainer = findViewById(R.id.footer_container);
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
                        return WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e(TAG, "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
        }
    }

    private void initializeViews() {
        // Initialize main cards
        submitFeedbackCard = findViewById(R.id.submit_feedback_card);
        viewFeedbackCard = findViewById(R.id.view_feedback_card);
        
        // Initialize statistics cards
        totalFeedbackCard = findViewById(R.id.total_feedback_card);
        thisMonthCard = findViewById(R.id.this_month_card);
        
        // Initialize statistics
        totalFeedbackCount = findViewById(R.id.total_feedback_count);
        thisMonthCount = findViewById(R.id.this_month_count);
        
        // Initialize back button
        ImageView backIcon = findViewById(R.id.back_icon);
        if (backIcon != null) {
            backIcon.setOnClickListener(this);
        }
        
        Log.d(TAG, "Views initialized successfully");
    }

    private void setupClickListeners() {
        if (submitFeedbackCard != null) {
            submitFeedbackCard.setOnClickListener(this);
            Log.d(TAG, "Submit feedback card click listener set");
        } else {
            Log.e(TAG, "Submit feedback card not found!");
        }
        
        if (viewFeedbackCard != null) {
            viewFeedbackCard.setOnClickListener(this);
            Log.d(TAG, "View feedback card click listener set");
        } else {
            Log.e(TAG, "View feedback card not found!");
        }
        
        if (totalFeedbackCard != null) {
            totalFeedbackCard.setOnClickListener(this);
            Log.d(TAG, "Total feedback card click listener set");
        } else {
            Log.e(TAG, "Total feedback card not found!");
        }
        
        if (thisMonthCard != null) {
            thisMonthCard.setOnClickListener(this);
            Log.d(TAG, "This month card click listener set");
        } else {
            Log.e(TAG, "This month card not found!");
        }
    }

    private void loadFeedbackStatistics() {
        // Load feedback statistics from API
        try {
            // Load constants from Paper
            Constant.loadFromPaper();
            
            // Check if constants are valid
            if (Constant.staff_id == null || Constant.staff_id.isEmpty() || 
                Constant.campus_id == null || Constant.campus_id.isEmpty()) {
                Log.w(TAG, "Staff ID or Campus ID not available, using cached data");
                loadCachedStatistics();
                return;
            }
            
            // Prepare API request
            java.util.HashMap<String, String> postParam = new java.util.HashMap<>();
            postParam.put("staff_id", Constant.staff_id);
            postParam.put("campus_id", Constant.campus_id);
            postParam.put("operation", "read_feedback");
            postParam.put("session_id", Constant.current_session);
            
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                (new org.json.JSONObject(postParam)).toString(), 
                okhttp3.MediaType.parse("application/json; charset=utf-8")
            );
            
            Log.d(TAG, "Fetching feedback statistics from API...");
            
            // Make API call
            Constant.mApiService.feedback(body).enqueue(new retrofit2.Callback<topgrade.parent.com.parentseeks.Teacher.Model.FeedbackModel>() {
                @Override
                public void onResponse(retrofit2.Call<topgrade.parent.com.parentseeks.Teacher.Model.FeedbackModel> call, 
                                     retrofit2.Response<topgrade.parent.com.parentseeks.Teacher.Model.FeedbackModel> response) {
                    if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                        java.util.List<topgrade.parent.com.parentseeks.Teacher.Model.Feedback> feedbackList = response.body().getFeedback();
                        
                        if (feedbackList != null) {
                            int totalCount = feedbackList.size();
                            int thisMonthCount = calculateThisMonthCount(feedbackList);
                            
                            // Cache the statistics
                            Paper.book().write("feedback_total_count", totalCount);
                            Paper.book().write("feedback_this_month_count", thisMonthCount);
                            
                            // Update UI
                            if (totalFeedbackCount != null) {
                                totalFeedbackCount.setText(String.valueOf(totalCount));
                            }
                            if (FeedbackMenu.this.thisMonthCount != null) {
                                FeedbackMenu.this.thisMonthCount.setText(String.valueOf(thisMonthCount));
                            }
                            
                            Log.d(TAG, "Statistics updated - Total: " + totalCount + ", This Month: " + thisMonthCount);
                        } else {
                            loadCachedStatistics();
                        }
                    } else {
                        Log.w(TAG, "API returned unsuccessful response, using cached data");
                        loadCachedStatistics();
                    }
                }
                
                @Override
                public void onFailure(retrofit2.Call<topgrade.parent.com.parentseeks.Teacher.Model.FeedbackModel> call, Throwable t) {
                    Log.e(TAG, "API call failed: " + t.getMessage());
                    loadCachedStatistics();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading feedback statistics: " + e.getMessage());
            loadCachedStatistics();
        }
    }
    
    private void loadCachedStatistics() {
        try {
            int totalCount = Paper.book().read("feedback_total_count", 0);
            int thisMonthCount = Paper.book().read("feedback_this_month_count", 0);
            
            if (totalFeedbackCount != null) {
                totalFeedbackCount.setText(String.valueOf(totalCount));
            }
            if (this.thisMonthCount != null) {
                this.thisMonthCount.setText(String.valueOf(thisMonthCount));
            }
            
            Log.d(TAG, "Loaded cached statistics - Total: " + totalCount + ", This Month: " + thisMonthCount);
        } catch (Exception e) {
            Log.e(TAG, "Error loading cached statistics: " + e.getMessage());
            if (totalFeedbackCount != null) totalFeedbackCount.setText("0");
            if (this.thisMonthCount != null) this.thisMonthCount.setText("0");
        }
    }
    
    private int calculateThisMonthCount(java.util.List<topgrade.parent.com.parentseeks.Teacher.Model.Feedback> feedbackList) {
        int count = 0;
        try {
            java.util.Calendar currentMonth = java.util.Calendar.getInstance();
            int currentMonthValue = currentMonth.get(java.util.Calendar.MONTH);
            int currentYear = currentMonth.get(java.util.Calendar.YEAR);
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            
            for (topgrade.parent.com.parentseeks.Teacher.Model.Feedback feedback : feedbackList) {
                try {
                    String timestamp = feedback.getTimestamp();
                    if (timestamp != null && !timestamp.isEmpty()) {
                        java.util.Date date = sdf.parse(timestamp);
                        if (date != null) {
                            java.util.Calendar feedbackCal = java.util.Calendar.getInstance();
                            feedbackCal.setTime(date);
                            
                            if (feedbackCal.get(java.util.Calendar.MONTH) == currentMonthValue && 
                                feedbackCal.get(java.util.Calendar.YEAR) == currentYear) {
                                count++;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing feedback date: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating this month count: " + e.getMessage());
        }
        return count;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.back_icon) {
            Log.d(TAG, "Back button clicked");
            finish();
            
        } else if (id == R.id.submit_feedback_card) {
            Log.d(TAG, "Submit feedback card clicked - Opening AddFeedback activity");
            try {
                Intent intent = new Intent(this, AddFeedback.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error opening AddFeedback activity: " + e.getMessage());
                Toast.makeText(context, "Error opening feedback form", Toast.LENGTH_SHORT).show();
            }
            
        } else if (id == R.id.view_feedback_card || id == R.id.total_feedback_card || id == R.id.this_month_card) {
            Log.d(TAG, "Feedback list requested - Opening FeedbackList activity");
            try {
                Intent intent = new Intent(this, FeedbackList.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error opening FeedbackList activity: " + e.getMessage());
                Toast.makeText(context, "Error opening feedback list", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh statistics when returning to this screen
        Log.d(TAG, "onResume: Refreshing feedback statistics");
        loadFeedbackStatistics();
    }
    
    /**
     * Update feedback statistics in Paper database
     * This method should be called when feedback is submitted or when statistics need to be updated
     */
    public void updateFeedbackStatistics(int totalCount, int thisMonthCount) {
        try {
            // Save updated statistics to Paper database
            Paper.book().write("feedback_total_count", totalCount);
            Paper.book().write("feedback_this_month_count", thisMonthCount);
            
            // Update UI immediately
            if (totalFeedbackCount != null) {
                totalFeedbackCount.setText(String.valueOf(totalCount));
            }
            if (this.thisMonthCount != null) {
                this.thisMonthCount.setText(String.valueOf(thisMonthCount));
            }
            
            Log.d(TAG, "Feedback statistics updated - Total: " + totalCount + ", This Month: " + thisMonthCount);
        } catch (Exception e) {
            Log.e(TAG, "Error updating feedback statistics: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "FeedbackMenu destroyed");
    }
}
