package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.SalaryAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Model.SalaryModel;

public class StaffSalary extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StaffSalary";

    BaseApiService mApiService;
    String staff_id;
    String campus_id;
    ProgressBar progress_bar;
    TextView total_records, Payable_total, Paid_total, Remain_total;
    Context context;
    RecyclerView salary_rcv;
    List<SalaryModel.Salary> list = new ArrayList<>();
    
    // Simple background executor for heavy operations
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Set edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            
            setContentView(R.layout.activity_staff_salary);
            
            // Initialize UI components first (light operations)
            initUIComponents();
            
            // Apply styling and setup in background
            applyStylingInBackground();
            
            // Initialize data in background
            initializeDataInBackground();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Initialize UI components (light operations only)
     */
    private void initUIComponents() {
        Remain_total = findViewById(R.id.Remain_total);
        Paid_total = findViewById(R.id.Paid_total);
        Payable_total = findViewById(R.id.Payable_total);
        total_records = findViewById(R.id.total_records);
        salary_rcv = findViewById(R.id.salary_rcv);
        progress_bar = findViewById(R.id.progress_bar);
        
        // Setup back button
        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // Show progress immediately
        progress_bar.setVisibility(View.VISIBLE);
    }
    
    /**
     * Apply styling in background to avoid blocking main thread
     */
    private void applyStylingInBackground() {
        backgroundExecutor.execute(() -> {
            try {
                // Configure status bar for navy blue background with white icons - COMPREHENSIVE FIX
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    mainHandler.post(() -> {
                        try {
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
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting status bar colors: " + e.getMessage());
                        }
                    });
                }
                
                // Configure status bar and navigation bar icons for Android R and above
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    mainHandler.post(() -> {
                        try {
                            if (getWindow().getInsetsController() != null) {
                                getWindow().getInsetsController().setSystemBarsAppearance(
                                    0, // No light icons for status bar (white icons on dark background)
                                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                                );
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting navigation bar appearance: " + e.getMessage());
                        }
                    });
                }

                // Additional fix for older Android versions - Force white icons
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    mainHandler.post(() -> {
                        try {
                            // Ensure status bar icons are light (white) on dark background
                            getWindow().getDecorView().setSystemUiVisibility(
                                getWindow().getDecorView().getSystemUiVisibility() & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            );
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting system UI visibility: " + e.getMessage());
                        }
                    });
                }
                
                // Setup window insets to respect system bars (status bar, navigation bar, notches)
                setupWindowInsets();
                
            } catch (Exception e) {
                Log.e(TAG, "Error applying styling: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Initialize data in background
     */
    private void initializeDataInBackground() {
        backgroundExecutor.execute(() -> {
            try {
                // Initialize Paper DB
                Paper.init(StaffSalary.this);
                
                // Initialize API service
                mApiService = API.getAPIService();
                context = StaffSalary.this;
                
                // Load salary data
                loadSalaryData();
                
            } catch (Exception e) {
                Log.e(TAG, "Error initializing data: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    progress_bar.setVisibility(View.GONE);
                    Toast.makeText(StaffSalary.this, "Failed to initialize data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the footer is visible above the navigation bar, not hidden behind it
     */
    private void setupWindowInsets() {
        mainHandler.post(() -> {
            try {
                android.view.View rootLayout = findViewById(android.R.id.content);
                
                if (rootLayout != null) {
                    androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                        try {
                            androidx.core.graphics.Insets systemInsets = insets.getInsets(
                                androidx.core.view.WindowInsetsCompat.Type.systemBars()
                            );

                            // Add bottom margin to buttons layout to push it above navigation bar
                            LinearLayout buttonsLayout = findViewById(R.id.layout);
                            if (buttonsLayout != null) {
                                // Set bottom margin to navigation bar height + extra padding to ensure buttons are visible
                                int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                                android.view.ViewGroup.MarginLayoutParams params = 
                                    (android.view.ViewGroup.MarginLayoutParams) buttonsLayout.getLayoutParams();
                                if (params != null) {
                                    // 16dp base margin + navigation bar height + 8dp extra padding
                                    params.bottomMargin = bottomMargin + 24; 
                                    buttonsLayout.setLayoutParams(params);
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
        });
    }

    /**
     * Load salary data with basic error handling
     */
    private void loadSalaryData() {
        try {
            staff_id = Paper.book().read("staff_id");
            campus_id = Paper.book().read("campus_id");
            
            if (staff_id == null || campus_id == null) {
                mainHandler.post(() -> {
                    progress_bar.setVisibility(View.GONE);
                    Toast.makeText(StaffSalary.this, "Required data not found", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            
            HashMap<String, String> postParam = new HashMap<String, String>();
            postParam.put("staff_id", staff_id);
            postParam.put("parent_id", campus_id);

            String jsonBody = (new org.json.JSONObject(postParam)).toString();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);
            
            // Execute API call on main thread (Retrofit handles background threading)
            mainHandler.post(() -> {
                // Check if activity is still valid before making API call
                if (!isFinishing() && !isDestroyed()) {
                    mApiService.load_salary(body).enqueue(new Callback<SalaryModel>() {
                        @Override
                        public void onResponse(Call<SalaryModel> call, Response<SalaryModel> response) {
                            try {
                                // Check if activity is still valid before processing response
                                if (!isFinishing() && !isDestroyed()) {
                                    if (response.body() != null) {
                                        if (response.body().getStatus().getCode().equals("1000")) {
                                            list = response.body().getSalary();
                                            updateUIWithSalaryData();
                                        } else {
                                            if (progress_bar != null) progress_bar.setVisibility(View.GONE);
                                            Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (progress_bar != null) progress_bar.setVisibility(View.GONE);
                                        Toast.makeText(context, "Network error: " + response.raw().message(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.w(TAG, "Activity is finishing/destroyed, skipping response processing");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing response: " + e.getMessage(), e);
                                if (!isFinishing() && !isDestroyed()) {
                                    if (progress_bar != null) progress_bar.setVisibility(View.GONE);
                                    Toast.makeText(context, "Error processing data", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<SalaryModel> call, Throwable e) {
                            Log.e(TAG, "========== SALARY API FAILURE ==========");
                            Log.e(TAG, "API call failed: " + e.getClass().getSimpleName());
                            Log.e(TAG, "Error message: " + e.getMessage());
                            
                            // Check if it's a JSON parsing error
                            if (e instanceof com.google.gson.JsonSyntaxException) {
                                Log.e(TAG, "JSON Parsing Error - API returned non-JSON response");
                                Log.e(TAG, "Possible causes: HTML error page, plain text, or server error");
                                Log.e(TAG, "Full error: ", e);
                                
                                if (!isFinishing() && !isDestroyed()) {
                                    if (progress_bar != null) progress_bar.setVisibility(View.GONE);
                                    Toast.makeText(context, "Server returned invalid data format", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Log.e(TAG, "Network/Connection Error", e);
                                if (!isFinishing() && !isDestroyed()) {
                                    if (progress_bar != null) progress_bar.setVisibility(View.GONE);
                                    Toast.makeText(context, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            Log.e(TAG, "========================================");
                        }
                    });
                } else {
                    Log.w(TAG, "Activity is finishing/destroyed, skipping API call");
                    if (progress_bar != null) progress_bar.setVisibility(View.GONE);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading salary data: " + e.getMessage(), e);
            mainHandler.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    if (progress_bar != null) progress_bar.setVisibility(View.GONE);
                    Toast.makeText(StaffSalary.this, "Error loading data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * Update UI with salary data
     */
    private void updateUIWithSalaryData() {
        try {
            // Check if activity is still valid before updating UI
            if (!isFinishing() && !isDestroyed()) {
                if (total_records != null) total_records.setText("Total Records: " + list.size());
                
                if (list.size() > 0) {
                    if (salary_rcv != null) salary_rcv.setAdapter(new SalaryAdaptor(list));
                    
                    int Payable_total_ = 0, Paid_total_ = 0, Remain_total_ = 0;
                    for (int i = 0; i < list.size(); i++) {
                        Paid_total_ = Paid_total_ + list.get(i).getPaid();
                        Payable_total_ = Payable_total_ + list.get(i).getPayable();
                        Remain_total_ = Remain_total_ + list.get(i).getRemaining();
                    }
                    
                    if (Paid_total != null) Paid_total.setText("" + Payable_total_);
                    if (Payable_total != null) Payable_total.setText("" + Paid_total_);
                    if (Remain_total != null) Remain_total.setText("" + Remain_total_);
                } else {
                    Toast.makeText(StaffSalary.this, "No Salary Uploaded", Toast.LENGTH_SHORT).show();
                }
                
                if (progress_bar != null) progress_bar.setVisibility(View.GONE);
            } else {
                Log.w(TAG, "Activity is finishing/destroyed, skipping UI update");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
            if (!isFinishing() && !isDestroyed()) {
                if (progress_bar != null) progress_bar.setVisibility(View.GONE);
                Toast.makeText(StaffSalary.this, "Error displaying data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.back_icon) {
            finish();
        }
    }

    public void View_Advance_Salary(View view) {
        startActivity(new Intent(StaffSalary.this, AdvancedSalary.class));
    }

    public void View_Ledger(View view) {
        startActivity(new Intent(StaffSalary.this, PaymentHistory.class));
    }

    public void View_Payment_History(View view) {
        startActivity(new Intent(StaffSalary.this, PaymentHistory.class));
    }

    public void View_Invoice(View view) {
        if (list.size() > 0) {
            // Find a salary record that has items for detailed breakdown
            SalaryModel.Salary salaryWithItems = null;
            for (SalaryModel.Salary salary : list) {
                if (salary.getItems() != null && !salary.getItems().isEmpty()) {
                    salaryWithItems = salary;
                    break;
                }
            }
            
            // If no salary with items found, use the first one
            if (salaryWithItems == null) {
                salaryWithItems = list.get(0);
            }
            
            String data = new Gson().toJson(salaryWithItems);
            startActivity(new Intent(StaffSalary.this, SalaryDetail.class)
                    .putExtra("data", data));
        } else {
            Toast.makeText(StaffSalary.this, "No salary data available", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup background executor
        if (!backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
}