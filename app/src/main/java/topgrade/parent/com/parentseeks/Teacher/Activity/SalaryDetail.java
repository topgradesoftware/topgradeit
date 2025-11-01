package topgrade.parent.com.parentseeks.Teacher.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.SalaryDetailAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Model.SalaryModel;

public class SalaryDetail extends AppCompatActivity {


    TextView Total_Salary, Lectures, Working_Days, Per_Rate, Present;
    TextView Absent, Leave, Half_Leave, Late, Adv_Deduct, Deduction;
    TextView Incntv, Payable, Paid, Remaining, Salary_Month, Paid_Date, total;
    RecyclerView salary_rcv;
    SalaryModel.Salary salary_model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_detail);

        // Configure status bar to be transparent so header can cover it
        setupStatusBar();


        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        String data = getIntent().getStringExtra("data");


        salary_model = new Gson().fromJson(data, new TypeToken<SalaryModel.Salary>() {
        }.getType());

        salary_rcv = findViewById(R.id.salary_rcv);
        Paid_Date = findViewById(R.id.Paid_Date);
        total = findViewById(R.id.total);
        Salary_Month = findViewById(R.id.Salary_Month);
        Remaining = findViewById(R.id.Remaining);
        Incntv = findViewById(R.id.Incntv);
        Payable = findViewById(R.id.Payable);
        Paid = findViewById(R.id.Paid);
        Total_Salary = findViewById(R.id.Total_Salary);
        Lectures = findViewById(R.id.Lectures);
        Working_Days = findViewById(R.id.Working_Days);
        Per_Rate = findViewById(R.id.Per_Rate);
        Present = findViewById(R.id.Present);
        Absent = findViewById(R.id.Absent);
        Leave = findViewById(R.id.Leave);
        Half_Leave = findViewById(R.id.Half_Leave);
        Late = findViewById(R.id.Late);
        Adv_Deduct = findViewById(R.id.Adv_Deduct);
        Deduction = findViewById(R.id.Deduction);


        Salary_Month.setText(salary_model.getSalaryMonthName() + " Month Salary");
        Adv_Deduct.setText("" + salary_model.getAdvance());
        Deduction.setText("" + salary_model.getDeduction());
        Remaining.setText("" + salary_model.getRemaining());
        Payable.setText("" + salary_model.getPayable());
        Paid.setText("" + salary_model.getPaid());
        Paid_Date.setText(salary_model.getPaidDate());
        Total_Salary.setText("" + salary_model.getAmount());
        Incntv.setText("" + salary_model.getIncentive());
        Lectures.setText(salary_model.getLectureAssigned());
        Working_Days.setText(salary_model.getLectureAttended());
        Per_Rate.setText(salary_model.getPerDayRate());
        Present.setText("" + salary_model.getPresents());
        Absent.setText("" + salary_model.getAbsents());
        Half_Leave.setText("" + salary_model.getHalfLeaves());
        Leave.setText("" + salary_model.getLates());
        Late.setText("" + salary_model.getLates());

        List<SalaryModel.Item> salary_head = salary_model.getItems();
        Log.d("SalaryDetail", "Salary items count: " + (salary_head != null ? salary_head.size() : "null"));
        
        int sum = 0;
        if (salary_head != null) {
            for (int i = 0; i < salary_head.size(); i++) {
                SalaryModel.Item item = salary_head.get(i);
                Log.d("SalaryDetail", "Item " + i + ": " + item.getSalaryHeadName() + " = " + item.getAmount());
                sum += item.getAmount();
            }
        }
        total.setText("" + sum);
        
        if (salary_head != null && !salary_head.isEmpty()) {
            salary_rcv.setAdapter(new SalaryDetailAdaptor(salary_head));
            Log.d("SalaryDetail", "RecyclerView adapter set with " + salary_head.size() + " items");
        } else {
            Log.w("SalaryDetail", "No salary items to display");
        }
    }

    private void setupStatusBar() {
        // Configure status bar to be transparent so header can cover it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
            
            // For Android M and above, ensure white status bar icons on dark background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
            
            // For Android R and above, configure navigation bar appearance
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (getWindow().getInsetsController() != null) {
                    getWindow().getInsetsController().setSystemBarsAppearance(
                        0, // No light icons for navigation bar (white icons on dark background)
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                }
            }
        }
    }
}
