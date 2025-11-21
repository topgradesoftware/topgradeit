package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.os.Bundle;
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
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.AdvanceSalaryAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Model.AdvancedSalaryModel;

public class AdvancedSalary extends AppCompatActivity {
    private static final String TAG = "AdvancedSalary";
    
    BaseApiService mApiService = API.getAPIService();
    RecyclerView Advanced_salary_rcv;
    ProgressBar progress_bar;
    Context context;
    List<AdvancedSalaryModel.Advanced> list = new ArrayList<>();
    TextView total_records, deuction_amount, deuction_month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_advanced_salary);
        
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
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();

        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Advanced_salary_rcv = findViewById(R.id.Advanced_salary_rcv);
        progress_bar = findViewById(R.id.progress_bar);
        total_records = findViewById(R.id.total_records);
        deuction_amount = findViewById(R.id.deuction_amount);
        deuction_month = findViewById(R.id.deuction_month);

        context = AdvancedSalary.this;
        Paper.init(context);

        load_advance();
    }

    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the footer is visible above the navigation bar, not hidden behind it
     */
    private void setupWindowInsets() {
        android.view.View rootLayout = findViewById(android.R.id.content);
        
        if (rootLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars()
                    );

                    // Add bottom margin to footer container to push it above navigation bar
                    LinearLayout footerContainer = findViewById(R.id.footer_container);
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
    }

    private void load_advance() {
        String staff_id = Paper.book().read("staff_id");
        String campus_id = Paper.book().read("campus_id");
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", staff_id);
        postParam.put("parent_id", campus_id);

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));
        mApiService.load_advance(body).enqueue(new Callback<AdvancedSalaryModel>() {
            @Override
            public void onResponse(Call<AdvancedSalaryModel> call, Response<AdvancedSalaryModel> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        list = response.body().getAdvance();
                        
                        if (list.size() > 0) {
                            AdvanceSalaryAdaptor adapter = new AdvanceSalaryAdaptor(list);
                            Advanced_salary_rcv.setAdapter(adapter);
                            
                            // Use filtered list for calculations and display
                            List<AdvancedSalaryModel.Advanced> filteredList = adapter.getFilteredList();
                            total_records.setText("Total Records: " + filteredList.size());
                            
                            int deuction_amount_ = 0, deuction_month_ = 0;
                            for (int i = 0; i < filteredList.size(); i++) {
                                AdvancedSalaryModel.Advanced item = filteredList.get(i);
                                if (item.getAmount() != null) {
                                    deuction_amount_ = deuction_amount_ + item.getAmount();
                                }
                                if (item.getDeduction() != null) {
                                    deuction_month_ = deuction_month_ + item.getDeduction();
                                }
                            }
                            deuction_amount.setText("" + deuction_amount_);
                            deuction_month.setText("" + deuction_month_);

                        } else {
                            total_records.setText("Total Records: 0");
                            deuction_amount.setText("0");
                            deuction_month.setText("0");
                            Toast.makeText(AdvancedSalary.this, "No Advanced Salary Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        total_records.setText("Total Records: 0");
                        deuction_amount.setText("0");
                        deuction_month.setText("0");
                        Toast.makeText(AdvancedSalary.this, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    total_records.setText("Total Records: 0");
                    deuction_amount.setText("0");
                    deuction_month.setText("0");
                    Toast.makeText(AdvancedSalary.this, "No response from server", Toast.LENGTH_SHORT).show();
                }
                progress_bar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<AdvancedSalaryModel> call, Throwable t) {
                progress_bar.setVisibility(View.GONE);
                total_records.setText("Total Records: 0");
                deuction_amount.setText("0");
                deuction_month.setText("0");
                Toast.makeText(AdvancedSalary.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
