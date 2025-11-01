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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
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
import topgrade.parent.com.parentseeks.Teacher.Adaptor.LegderAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Model.LegderModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffLedger extends AppCompatActivity implements View.OnClickListener {


    BaseApiService mApiService = API.getAPIService();
    RecyclerView Advanced_salary_rcv;
    RecyclerView payment_history_rcv;
    ProgressBar progress_bar;
    Context context;
    TextView show_advanced_filter;
    AlertDialog alertDialog;
    SearchableSpinner select_month_spinner;
    Button search_filter, this_month;
    String month_format = "";
    List<String> month_list = new ArrayList<>();

    List<LegderModel.Invoice> list = new ArrayList<>();
    List<LegderModel.Invoice> payment_history_list = new ArrayList<>();
    List<MonthModel> month_list_id = new ArrayList<>();

    TextView total_records, Remaining, Debit_Total, Credit_Total;
    TextView campus_name, campus_address, campus_mobile;
    TextView staff_name, Parent_Name, Contact_No, Gender, D_O_B;
    ImageView Cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_ledger);
        
        // Apply navy blue color to both status bar and navigation bar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.navy_blue));
            
            // For Android M and above, ensure white status bar icons on dark background
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
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
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();
        
        context = StaffLedger.this;
        Paper.init(context);


        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Advanced_salary_rcv = findViewById(R.id.Advanced_salary_rcv);
        payment_history_rcv = findViewById(R.id.payment_history_rcv);
        progress_bar = findViewById(R.id.progress_bar);
        total_records = findViewById(R.id.total_records);
        Remaining = findViewById(R.id.Remaining);
        Debit_Total = findViewById(R.id.Debit_Total);
        Credit_Total = findViewById(R.id.Credit_Total);
        show_advanced_filter = findViewById(R.id.show_advanced_filter);

        campus_name = findViewById(R.id.campus_name);
        campus_address = findViewById(R.id.campus_address);
        campus_mobile = findViewById(R.id.campus_mobile);

        staff_name = findViewById(R.id.staff_name);
        Parent_Name = findViewById(R.id.Parent_Name);
        Contact_No = findViewById(R.id.Contact_No);
        Gender = findViewById(R.id.Gender);
        D_O_B = findViewById(R.id.D_O_B);


        campus_name.setText(Paper.book().read("campus_name", ""));
        campus_address.setText(Paper.book().read("campus_address", ""));
        campus_mobile.setText(Paper.book().read("campus_phone", ""));


        StaffModel parentModel = Paper.book().read("Staff_Model", new StaffModel());

        staff_name.setText(parentModel.getFull_name());
        Parent_Name.setText(parentModel.getParent_name());
        Contact_No.setText(parentModel.getPhone());
        
        // Fix gender display with proper null handling
        String genderText = parentModel.getGender() != null && !parentModel.getGender().equals("null") ? parentModel.getGender() : "Not specified";
        Gender.setText(genderText);
        
        D_O_B.setText(parentModel.getDob());


        show_advanced_filter.setOnClickListener(this);


        month_list_id.add(new MonthModel("", "Select Month"));
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

        load_ledger();
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
     */
    private void setupWindowInsets() {
        View rootView = findViewById(android.R.id.content);
        
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    // Apply safe padding to avoid overlapping with system bars
                    view.setPadding(
                        systemInsets.left,
                        systemInsets.top,
                        systemInsets.right,
                        systemInsets.bottom
                    );

                    // Return CONSUMED to prevent child views from getting default padding
                    return WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e("StaffLedger", "Error in window insets listener: " + e.getMessage());
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        } else {
            Log.e("StaffLedger", "rootView is null - cannot setup window insets");
        }
    }


    private void load_ledger() {
        String staff_id = Paper.book().read("staff_id");
        String campus_id = Paper.book().read("campus_id");
        
        if (staff_id == null || campus_id == null || staff_id.isEmpty() || campus_id.isEmpty()) {
            Toast.makeText(context, "Staff ID or Campus ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", staff_id);
        postParam.put("parent_id", campus_id);
        
        // Add month parameter if selected
        if (month_format != null && !month_format.isEmpty()) {
            postParam.put("month", month_format);
            Log.d("StaffLedger", "Month filter applied: " + month_format);
        } else {
            Log.d("StaffLedger", "No month filter applied");
        }

        progress_bar.setVisibility(View.VISIBLE);
        String requestBody = (new JSONObject(postParam)).toString();
        Log.d("StaffLedger", "API Request: " + requestBody);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody);
        mApiService.load_ledger(body).enqueue(new Callback<LegderModel>() {
            @Override
            public void onResponse(Call<LegderModel> call, Response<LegderModel> response) {
                progress_bar.setVisibility(View.GONE);
                
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        list = response.body().getInvoice();
                        payment_history_list = response.body().getInvoice(); // Payment history uses same data
                        total_records.setText("Total Records: " + list.size());

                        if (list.size() > 0) {

                            float debit_total = 0, credit_total = 0, balance = 0;
                            for (int i = 0; i < list.size(); i++) {

                                if (list.get(i).getIsType().equals(Constant.Credit)) {
                                    credit_total = credit_total + Float.parseFloat(list.get(i).getTotal());

                                }
                                if (list.get(i).getIsType().equals(Constant.Debit)) {
                                    debit_total = debit_total + Float.parseFloat(list.get(i).getTotal());
                                }
                            }
                            Advanced_salary_rcv.setAdapter(new LegderAdaptor(list));
                            payment_history_rcv.setAdapter(new LegderAdaptor(payment_history_list));
                            balance = debit_total - credit_total;
                            Debit_Total.setText("" + debit_total);
                            Credit_Total.setText("" + credit_total);
                            Remaining.setText("" + balance);
                        } else {
                            Toast.makeText(context, "No Ledger Records Found", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        String errorMessage = response.body().getStatus().getMessage();
                        Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("StaffLedger", "API Error: " + errorMessage);
                    }
                } else {
                    String errorMessage = response.raw().message();
                    Toast.makeText(context, "Network Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("StaffLedger", "Network Error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<LegderModel> call, Throwable e) {
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, "Connection Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("StaffLedger", "API Failure: " + e.getMessage());
                e.printStackTrace();
            }
        });
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
            load_ledger();

        } else if (id == R.id.show_advanced_filter) {
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.select_month, null);

            select_month_spinner = dialogView.findViewById(R.id.select_month_spinner);
            this_month = dialogView.findViewById(R.id.this_month);
            search_filter = dialogView.findViewById(R.id.search_filter);
            Cancel = dialogView.findViewById(R.id.Cancel);

            select_month_spinner.setTitle("Select Month");
            // Removed setPositiveButton("OK") to enable auto-dismiss behavior like staff timetable

            this_month.setOnClickListener(this);
            search_filter.setOnClickListener(this);
            Cancel.setOnClickListener(this);

            ArrayAdapter<String> month_adaptor = new ArrayAdapter<>(context, R.layout.simple_list_item_1, month_list);
            select_month_spinner.setAdapter(month_adaptor);

            select_month_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        month_format = "";
                        Log.d("StaffLedger", "Month selection: No month selected (position 0)");
                    } else {
                        Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        String month_id = month_list_id.get(position).getId();
                        month_format = month_id + "/" + year;
                        Log.d("StaffLedger", "Month selection: " + month_list_id.get(position).getMonth() + " -> " + month_format);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setView(dialogView);
            alertDialog = dialogBuilder.create();
            alertDialog.show();

        } else if (id == R.id.this_month) {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            month_format = "";
            load_ledger();
        }
    }
}
