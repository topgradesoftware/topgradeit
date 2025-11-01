package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import com.google.android.material.button.MaterialButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import components.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Model.MonthModel;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.PaymentHistoryAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Model.SalaryModel;

public class PaymentHistory extends AppCompatActivity implements View.OnClickListener {

    BaseApiService mApiService = API.getAPIService();
    RecyclerView payment_history_rcv;
    ProgressBar progress_bar;
    Context context;
    TextView show_advanced_filter;
    AlertDialog alertDialog;
    SearchableSpinner select_month_spinner;
    MaterialButton search_filter, this_month;
    String month_format = "";
    List<String> month_list = new ArrayList<>();

    List<SalaryModel.Salary> all_payments = new ArrayList<>();
    List<SalaryModel.Salary> filtered_payments = new ArrayList<>();
    List<MonthModel> month_list_id = new ArrayList<>();

    private PaymentHistoryAdaptor currentAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_payment_history);
        
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
        
        // Force light status bar icons and dark navigation bar icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        // Setup window insets to respect system bars
        setupWindowInsets();
        
        context = PaymentHistory.this;
        Paper.init(context);

        // Initialize views
        initializeViews();
        
        // Setup month list for filtering
        setupMonthList();
        
        // Load payment history
        loadPaymentHistory();
    }
    
    private void initializeViews() {
        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(v -> finish());

        payment_history_rcv = findViewById(R.id.payment_history_rcv);
        progress_bar = findViewById(R.id.progress_bar);
        show_advanced_filter = findViewById(R.id.show_advanced_filter);

        show_advanced_filter.setOnClickListener(this);
    }
    
    private void setupMonthList() {
        month_list_id.add(new MonthModel("", "All Months"));
        month_list_id.add(new MonthModel("1", "JANUARY"));
        month_list_id.add(new MonthModel("2", "FEBRUARY"));
        month_list_id.add(new MonthModel("3", "MARCH"));
        month_list_id.add(new MonthModel("4", "APRIL"));
        month_list_id.add(new MonthModel("5", "MAY"));
        month_list_id.add(new MonthModel("6", "JUNE"));
        month_list_id.add(new MonthModel("7", "JULY"));
        month_list_id.add(new MonthModel("8", "AUGUST"));
        month_list_id.add(new MonthModel("9", "SEPTEMBER"));
        month_list_id.add(new MonthModel("10", "OCTOBER"));
        month_list_id.add(new MonthModel("11", "NOVEMBER"));
        month_list_id.add(new MonthModel("12", "DECEMBER"));

        for (int i = 0; i < month_list_id.size(); i++) {
            month_list.add(month_list_id.get(i).getMonth());
        }
    }
    
    private void setupWindowInsets() {
        android.view.View rootLayout = findViewById(android.R.id.content);
        
        if (rootLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars()
                    );

                    // No padding on root layout to avoid touch interference
                    view.setPadding(0, 0, 0, 0);

                    // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e("PaymentHistory", "Error in window insets listener: " + e.getMessage());
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                }
            });
        } else {
            Log.e("PaymentHistory", "rootLayout is null - cannot setup window insets");
        }
    }

    private void loadPaymentHistory() {
        String staff_id = Paper.book().read("staff_id");
        String campus_id = Paper.book().read("campus_id");
        
        if (staff_id == null || campus_id == null || staff_id.isEmpty() || campus_id.isEmpty()) {
            Toast.makeText(context, "Staff ID or Campus ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", staff_id);
        postParam.put("parent_id", campus_id);

        progress_bar.setVisibility(View.VISIBLE);
        String requestBody = (new JSONObject(postParam)).toString();
        Log.d("PaymentHistory", "API Request: " + requestBody);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody);
        
        mApiService.load_salary(body).enqueue(new Callback<SalaryModel>() {
            @Override
            public void onResponse(Call<SalaryModel> call, Response<SalaryModel> response) {
                progress_bar.setVisibility(View.GONE);
                
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        all_payments = response.body().getSalary();
                        filtered_payments = new ArrayList<>(all_payments);
                        
                        // Debug: Log all payment data
                        Log.d("PaymentHistory", "Total payments loaded: " + all_payments.size());
                        for (int i = 0; i < all_payments.size(); i++) {
                            SalaryModel.Salary payment = all_payments.get(i);
                            Log.d("PaymentHistory", "Payment " + i + ": Month=" + payment.getSalaryMonthName() + 
                                  ", MonthID=" + payment.getSalaryMonthId() + 
                                  ", StartDate=" + payment.getStartDate() + 
                                  ", Payable=" + payment.getPayable());
                        }
                        
                        updateUI();
                        
                    } else {
                        String errorMessage = response.body().getStatus().getMessage();
                        Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("PaymentHistory", "API Error: " + errorMessage);
                    }
                } else {
                    String errorMessage = response.raw().message();
                    Toast.makeText(context, "Network Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("PaymentHistory", "Network Error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<SalaryModel> call, Throwable e) {
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, "Connection Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("PaymentHistory", "API Failure: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void updateUI() {
        if (filtered_payments.size() > 0) {
            // Set up RecyclerView
            payment_history_rcv.setLayoutManager(new LinearLayoutManager(context));
            
            if (currentAdapter == null) {
                currentAdapter = new PaymentHistoryAdaptor(filtered_payments, context);
                payment_history_rcv.setAdapter(currentAdapter);
            } else {
                // Update existing adapter
                currentAdapter.updateData(filtered_payments);
            }
            
            Log.d("PaymentHistory", "UI Updated - Showing " + filtered_payments.size() + " payment records");
            
        } else {
            if (month_format != null && !month_format.isEmpty()) {
                Toast.makeText(context, "No Payment Records Found for Selected Month", Toast.LENGTH_SHORT).show();
                Log.w("PaymentHistory", "No records found for month: " + month_format);
            } else {
                Toast.makeText(context, "No Payment Records Found", Toast.LENGTH_SHORT).show();
                Log.w("PaymentHistory", "No payment records found at all");
            }
            
            // Clear the RecyclerView when no data
            if (currentAdapter != null) {
                currentAdapter.updateData(new ArrayList<>());
            }
        }
    }
    
    private void filterByMonth() {
        Log.d("PaymentHistory", "Starting filterByMonth() - month_format: '" + month_format + "'");
        Log.d("PaymentHistory", "Total payments before filtering: " + all_payments.size());
        
        if (month_format == null || month_format.isEmpty()) {
            // Show all payments when no month is selected
            filtered_payments = new ArrayList<>(all_payments);
            Log.d("PaymentHistory", "Showing all months payments: " + filtered_payments.size() + " records");
        } else {
            // Filter by selected month
            filtered_payments.clear();
            String selectedMonth = month_format.split("/")[0]; // Get month number
            Log.d("PaymentHistory", "Filtering by month number: " + selectedMonth);
            
            for (SalaryModel.Salary payment : all_payments) {
                // Check multiple possible month fields
                boolean matches = false;
                
                // Check salaryMonthId
                if (payment.getSalaryMonthId() != null && payment.getSalaryMonthId().equals(selectedMonth)) {
                    matches = true;
                    Log.d("PaymentHistory", "Match found by salaryMonthId: " + payment.getSalaryMonthId());
                }
                
                // Check salaryMonthName (in case it contains month number)
                if (!matches && payment.getSalaryMonthName() != null) {
                    String monthName = payment.getSalaryMonthName().toLowerCase();
                    if (monthName.contains(selectedMonth) || 
                        (selectedMonth.equals("1") && monthName.contains("jan")) ||
                        (selectedMonth.equals("2") && monthName.contains("feb")) ||
                        (selectedMonth.equals("3") && monthName.contains("mar")) ||
                        (selectedMonth.equals("4") && monthName.contains("apr")) ||
                        (selectedMonth.equals("5") && monthName.contains("may")) ||
                        (selectedMonth.equals("6") && monthName.contains("jun")) ||
                        (selectedMonth.equals("7") && monthName.contains("jul")) ||
                        (selectedMonth.equals("8") && monthName.contains("aug")) ||
                        (selectedMonth.equals("9") && monthName.contains("sep")) ||
                        (selectedMonth.equals("10") && monthName.contains("oct")) ||
                        (selectedMonth.equals("11") && monthName.contains("nov")) ||
                        (selectedMonth.equals("12") && monthName.contains("dec"))) {
                        matches = true;
                        Log.d("PaymentHistory", "Match found by salaryMonthName: " + payment.getSalaryMonthName());
                    }
                }
                
                // Check startDate (in case it contains month)
                if (!matches && payment.getStartDate() != null) {
                    String startDate = payment.getStartDate();
                    if (startDate.contains("/" + selectedMonth + "/") || 
                        startDate.contains("-" + selectedMonth + "-")) {
                        matches = true;
                        Log.d("PaymentHistory", "Match found by startDate: " + payment.getStartDate());
                    }
                }
                
                if (matches) {
                    filtered_payments.add(payment);
                    Log.d("PaymentHistory", "Added payment for month: " + payment.getSalaryMonthName() + 
                          " (ID: " + payment.getSalaryMonthId() + ", Date: " + payment.getStartDate() + ")");
                }
            }
            Log.d("PaymentHistory", "Filtered payments count: " + filtered_payments.size());
        }
        
        updateUI();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.Cancel) {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }

        } else if (id == R.id.search_filter) {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            filterByMonth();

        } else if (id == R.id.show_advanced_filter) {
            showMonthFilterDialog();

        } else if (id == R.id.this_month) {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            // Reset to show all months
            month_format = "";
            Log.d("PaymentHistory", "Reset button clicked - showing all months payments");
            filterByMonth();
        }
    }
    
    private void showMonthFilterDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.select_month, null);

        select_month_spinner = dialogView.findViewById(R.id.select_month_spinner);
        this_month = dialogView.findViewById(R.id.this_month);
        search_filter = dialogView.findViewById(R.id.search_filter);
        ImageView cancel = dialogView.findViewById(R.id.Cancel);

        select_month_spinner.setTitle("Select Month");
        // Removed setPositiveButton("OK") to enable auto-dismiss behavior like staff timetable

        this_month.setOnClickListener(this);
        search_filter.setOnClickListener(this);
        cancel.setOnClickListener(this);

        ArrayAdapter<String> month_adaptor = new ArrayAdapter<>(context, R.layout.simple_list_item_1, month_list);
        select_month_spinner.setAdapter(month_adaptor);

        select_month_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // "All Months" selected - show all payments
                    month_format = "";
                    Log.d("PaymentHistory", "Month selection: All months selected - will show all payments");
                } else {
                    // Specific month selected - filter by that month
                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    String month_id = month_list_id.get(position).getId();
                    month_format = month_id + "/" + year;
                    Log.d("PaymentHistory", "Month selection: " + month_list_id.get(position).getMonth() + " -> " + month_format + " (will filter by month " + month_id + ")");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
