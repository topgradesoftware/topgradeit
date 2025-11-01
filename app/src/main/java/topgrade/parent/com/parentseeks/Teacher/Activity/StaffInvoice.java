package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.os.Bundle;
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
import topgrade.parent.com.parentseeks.Teacher.Adaptor.InvoiceAdapter;
import topgrade.parent.com.parentseeks.Teacher.Model.Invoice_Model;

public class StaffInvoice extends AppCompatActivity {
    private static final String TAG = "StaffInvoice";
    
    BaseApiService mApiService = API.getAPIService();
    RecyclerView invoiceRcv;
    ProgressBar progressBar;
    Context context;
    List<Invoice_Model.InvoiceItem> invoiceItems = new ArrayList<>();
    TextView totalRecords, debitTotal, creditTotal, remaining;
    
    // Adapter
    InvoiceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_invoice);
        
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

        // Initialize views
        initializeViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Load data
        loadInvoiceData();
    }
    
    private void initializeViews() {
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        invoiceRcv = findViewById(R.id.invoice_rcv);
        progressBar = findViewById(R.id.progress_bar);
        totalRecords = findViewById(R.id.total_records);
        debitTotal = findViewById(R.id.Debit_Total);
        creditTotal = findViewById(R.id.Credit_Total);
        remaining = findViewById(R.id.Remaining);

        context = StaffInvoice.this;
        Paper.init(context);
    }
    
    private void setupRecyclerView() {
        invoiceRcv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvoiceAdapter(invoiceItems);
        invoiceRcv.setAdapter(adapter);
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
     */
    private void setupWindowInsets() {
        View rootLayout = findViewById(android.R.id.content);
        
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
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
                    Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        } else {
            Log.e(TAG, "rootLayout is null - cannot setup window insets");
        }
    }

    private void loadInvoiceData() {
        progressBar.setVisibility(View.VISIBLE);
        invoiceItems.clear();
        
        // TODO: Replace with actual API call
        // For now, load sample data
        loadSampleData();
    }
    
    private void loadSampleData() {
        // Sample invoice data
        invoiceItems.add(new Invoice_Model.InvoiceItem("1", "Tuition Fee", "5000", "0", "2024-01-15"));
        invoiceItems.add(new Invoice_Model.InvoiceItem("2", "Library Fee", "500", "0", "2024-01-16"));
        invoiceItems.add(new Invoice_Model.InvoiceItem("3", "Payment Received", "0", "3000", "2024-01-20"));
        invoiceItems.add(new Invoice_Model.InvoiceItem("4", "Transport Fee", "2000", "0", "2024-01-25"));
        invoiceItems.add(new Invoice_Model.InvoiceItem("5", "Payment Received", "0", "4000", "2024-01-30"));
        
        updateAdapter();
        progressBar.setVisibility(View.GONE);
    }
    
    private void updateAdapter() {
        Log.d(TAG, "Updating adapter with " + invoiceItems.size() + " invoice items");
        adapter.updateData(invoiceItems);
        updateTotals();
        updateTotalRecords();
        
        if (invoiceItems.isEmpty()) {
            Toast.makeText(context, "No invoice data found", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateTotals() {
        double totalDebit = 0;
        double totalCredit = 0;
        
        for (Invoice_Model.InvoiceItem item : invoiceItems) {
            try {
                totalDebit += Double.parseDouble(item.getDebit());
                totalCredit += Double.parseDouble(item.getCredit());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing amount: " + e.getMessage());
            }
        }
        
        double balance = totalDebit - totalCredit;
        
        debitTotal.setText(String.format("%.2f", totalDebit));
        creditTotal.setText(String.format("%.2f", totalCredit));
        remaining.setText(String.format("%.2f", balance));
    }
    
    private void updateTotalRecords() {
        int count = invoiceItems.size();
        totalRecords.setText("Total Records: " + count);
    }
}
