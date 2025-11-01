package topgrade.parent.com.parentseeks.Parent.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Adaptor.ChalanAdaptor;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.New.Challan;
import topgrade.parent.com.parentseeks.Parent.Model.New.FeeChalanModel;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;

public class FeeChalan extends AppCompatActivity implements OnClickListener, View.OnClickListener {

    private RecyclerView myrecycleview;

    List<Challan> challanList = new ArrayList<>();
    Context context;
    SearchableSpinner select_child_spinner;
    Button search_filter;
    ImageView back_icon, Cancel;

    String seleted_child_id = "";
    ArrayAdapter<String> child_adaptor;
    List<SharedStudent> studentList = new ArrayList<>();

    HorizontalScrollView horizontal_scroll;
    String parent_id;
    String campus_id;
    int scrollX = 0;
    ProgressBar progress_bar;
    TextView show_advanced_filter;
    AlertDialog alertDialog;

    private void applyTheme() {
        try {
            // Check user type and apply appropriate theme
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("FeeChalan", "User Type: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
int tealColor = ContextCompat.getColor(this, R.color.student_primary);
                    getWindow().setStatusBarColor(tealColor);
                    getWindow().setNavigationBarColor(tealColor);
                
                // Force dark navigation bar icons (prevent light appearance)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // 0 = do NOT use light icons
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }
                
                // Change header background to teal for student theme
                View header = findViewById(R.id.header);
                if (header != null) {
                    header.setBackgroundColor(ContextCompat.getColor(this, R.color.student_primary));
                }
                
                // Change header row background to teal for student theme
                View headerRow = findViewById(R.id.header_row);
                if (headerRow != null) {
                    headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.student_primary));
                }
                
                // Keep horizontal scroll view background transparent/white for student theme
                HorizontalScrollView horizontalScrollView = findViewById(R.id.table_scroll);
                if (horizontalScrollView != null) {
                    horizontalScrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                }
                
                Log.d("FeeChalan", "Student theme applied successfully");
            } else {
                // Apply unified parent theme for fee challan page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for challan
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for challan
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Fee Challan");
                
                // Ensure header background is brown for parent theme
                View header = findViewById(R.id.header);
                if (header != null) {
                    header.setBackgroundColor(ContextCompat.getColor(this, R.color.parent_primary));
                }
                
                // Ensure header row background is brown for parent theme
                View headerRow = findViewById(R.id.header_row);
                if (headerRow != null) {
                    headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.parent_primary));
                }
                
                // Keep horizontal scroll view background white for parent theme
                HorizontalScrollView horizontalScrollView = findViewById(R.id.table_scroll);
                if (horizontalScrollView != null) {
                    horizontalScrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                }
                
                Log.d("FeeChalan", "Parent theme applied successfully");
            }
            
            // Footer has been removed from layout - no footer theming needed
            
        } catch (Exception e) {
            Log.e("FeeChalan", "Error applying theme", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_fee_chalan);

        // Configure status bar for dark brown background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.dark_brown));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar and navigation bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        setupWindowInsets();

        // Apply student theme using ThemeHelper
        applyTheme();
        
        // Ensure main container has white background
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        }
        
        // Ensure the main content area (below header) has white background
        View mainContent = findViewById(R.id.table_scroll);
        if (mainContent != null) {
            mainContent.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // Load all constants from Paper DB (same as other working parent activities)
        Constant.loadFromPaper();
        campus_id = Constant.campus_id;
        
        String userType = Paper.book().read(Constants.User_Type, "");
        
        // ==================== COMPREHENSIVE DEBUGGING ====================
        Log.d("FeeChalan", "=== FEE CHALAN DEBUGGING START ===");
        Log.d("FeeChalan", "User Type: " + userType);
        Log.d("FeeChalan", "Constant.parent_id: " + Constant.parent_id);
        Log.d("FeeChalan", "Constant.campus_id: " + Constant.campus_id);
        Log.d("FeeChalan", "Constant.current_session: " + Constant.current_session);
        
        // Debug Paper DB data
        Log.d("FeeChalan", "=== PAPER DB DEBUG ===");
        Log.d("FeeChalan", "Paper parent_id: " + Paper.book().read("parent_id"));
        Log.d("FeeChalan", "Paper campus_id: " + Paper.book().read("campus_id"));
        Log.d("FeeChalan", "Paper full_name: " + Paper.book().read("full_name"));
        Log.d("FeeChalan", "Paper email: " + Paper.book().read("email"));
        Log.d("FeeChalan", "Paper is_login: " + Paper.book().read("is_login"));
        Log.d("FeeChalan", "Paper User_Type: " + Paper.book().read("User_Type"));
        
        // Both parent and student contexts use the same parent_id from Constant
        // When student logs in, their ID is stored as parent_id in Constant
        // When parent logs in, their ID is stored as parent_id in Constant
        parent_id = Constant.parent_id;
        Log.d("FeeChalan", userType + " context: Using parent_id: " + parent_id);
        
        Log.d("FeeChalan", userType + " context: parent_id=" + parent_id + ", campus_id=" + campus_id);

        try {
            // Both parent and student contexts use the same students list from Paper DB
            // This is because both login types store students in the same "students" key
            studentList = Paper.book().read("students");
            Log.d("FeeChalan", "=== STUDENTS LIST DEBUG ===");
            Log.d("FeeChalan", "Loaded " + (studentList != null ? studentList.size() : 0) + " students for " + userType + " context");
            
            if (studentList != null && !studentList.isEmpty()) {
                for (int i = 0; i < studentList.size(); i++) {
                    SharedStudent student = studentList.get(i);
                    Log.d("FeeChalan", "Student " + i + ": name='" + student.getFullName() + "', id='" + student.getUniqueId() + "', parentId='" + student.getParentId() + "'");
                }
            } else {
                Log.w("FeeChalan", "No students found in Paper DB!");
            }
        } catch (Exception e) {
            // If there's a serialization error, clear the corrupted data and start fresh
            Paper.book().delete("students");
            studentList = new ArrayList<>();
            Log.e("FeeChalan", "Error loading students", e);
        }


        innitlization();

    }

    private void innitlization() {

        Log.d("FeeChalan", "=== INITIALIZATION DEBUG ===");
        context = FeeChalan.this;
        Paper.init(context);

        back_icon = findViewById(R.id.back_icon);
        horizontal_scroll = findViewById(R.id.table_scroll);
        show_advanced_filter = findViewById(R.id.show_advanced_filter);
        myrecycleview = findViewById(R.id.attendance_rcv);
        progress_bar = findViewById(R.id.progress_bar);
        show_advanced_filter.setOnClickListener(FeeChalan.this);
        back_icon.setOnClickListener(FeeChalan.this);
        
        // Configure RecyclerView for proper height adjustment
        myrecycleview.setHasFixedSize(false);
        myrecycleview.setNestedScrollingEnabled(false);
        
        Log.d("FeeChalan", "Views initialized successfully");
        Log.d("FeeChalan", "=== END INITIALIZATION DEBUG ===");

        List<String> student_name_list = new ArrayList<>();
        // Check if studentList is not null before accessing it
        if (studentList != null) {
            for (int i = 0; i < studentList.size(); i++) {
                student_name_list.add(studentList.get(i).getFullName());
            }
        } else {
            // Initialize empty list if null
            studentList = new ArrayList<>();
            Toast.makeText(context, "No students found. Please add students first.", Toast.LENGTH_SHORT).show();
        }
        child_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                student_name_list);


        show_advanced_filter.performClick();


    }


    private void load_attendance(final String parent_id, final String campus_id, final String student_id) {

        // ==================== API CALL DEBUGGING ====================
        Log.d("FeeChalan", "=== LOADING CHALLAN API CALL ===");
        Log.d("FeeChalan", "API URL: " + API.load_challan);
        Log.d("FeeChalan", "parent_id parameter: " + parent_id);
        Log.d("FeeChalan", "campus_id parameter: " + campus_id);
        Log.d("FeeChalan", "student_id parameter: " + student_id);
        Log.d("FeeChalan", "User Type: " + Paper.book().read(Constants.User_Type, ""));
        Log.d("FeeChalan", "=== END API CALL DEBUGGING ===");

        progress_bar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                API.load_challan, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                Log.d("FeeChalan", "=== API RESPONSE DEBUG ===");
                Log.d("FeeChalan", "Raw API Response: " + response);
                Log.d("FeeChalan", "Response length: " + (response != null ? response.length() : "null"));
                
                try {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    FeeChalanModel feeChalanModel = gson.fromJson(response, FeeChalanModel.class);

                    Log.d("FeeChalan", "Parsed FeeChalanModel: " + (feeChalanModel != null ? "SUCCESS" : "NULL"));
                    if (feeChalanModel != null) {
                        Log.d("FeeChalan", "Status: " + (feeChalanModel.getStatus() != null ? feeChalanModel.getStatus().getCode() : "NULL"));
                        Log.d("FeeChalan", "Message: " + (feeChalanModel.getStatus() != null ? feeChalanModel.getStatus().getMessage() : "NULL"));
                        Log.d("FeeChalan", "Challan list size: " + (feeChalanModel.getChallan() != null ? feeChalanModel.getChallan().size() : "NULL"));
                    }

                    // Add null checks to prevent NullPointerException
                    if (feeChalanModel != null && feeChalanModel.getStatus() != null && 
                        feeChalanModel.getStatus().getCode() != null && 
                        feeChalanModel.getStatus().getCode().equals("1000")) {

                        Log.d("FeeChalan", "API call successful - status code 1000");
                        challanList = feeChalanModel.getChallan();
                        if (challanList != null && challanList.size() > 0) {
                            Log.d("FeeChalan", "Found " + challanList.size() + " challan records");
                            ChalanAdaptor attendenceAdaptor = new ChalanAdaptor(context, challanList,
                                    FeeChalan.this);

                            myrecycleview.setAdapter(attendenceAdaptor);
                            
                            // Adjust RecyclerView height to fit content
                            adjustRecyclerViewHeight();

                        } else {
                            Log.w("FeeChalan", "No challan records found in response");
                            Toast.makeText(context, "No Chalan Record Found", Toast.LENGTH_SHORT).show();
                        }

                        progress_bar.setVisibility(View.GONE);
                    } else if (feeChalanModel != null && feeChalanModel.getStatus() != null) {
                        Log.w("FeeChalan", "API call failed - Status: " + feeChalanModel.getStatus().getCode() + ", Message: " + feeChalanModel.getStatus().getMessage());
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, feeChalanModel.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle case when response is null or invalid
                        Log.e("FeeChalan", "Invalid response or null data");
                        progress_bar.setVisibility(View.GONE);
                        
                        // Set empty adapter to prevent "No adapter attached" error
                        challanList = new ArrayList<>();
                        ChalanAdaptor emptyAdaptor = new ChalanAdaptor(context, challanList, FeeChalan.this);
                        myrecycleview.setAdapter(emptyAdaptor);
                        
                        Toast.makeText(context, "No data available", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                    progress_bar.setVisibility(View.GONE);
                    
                    // Set empty adapter to prevent "No adapter attached" error
                    challanList = new ArrayList<>();
                    ChalanAdaptor emptyAdaptor = new ChalanAdaptor(context, challanList, FeeChalan.this);
                    myrecycleview.setAdapter(emptyAdaptor);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
                
                // Set empty adapter to prevent "No adapter attached" error
                challanList = new ArrayList<>();
                ChalanAdaptor emptyAdaptor = new ChalanAdaptor(context, challanList, FeeChalan.this);
                myrecycleview.setAdapter(emptyAdaptor);

                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                HashMap<String, String> postParam = new HashMap<String, String>();
                postParam.put("parent_parent_id", parent_id);
                postParam.put("parent_id", campus_id);
                postParam.put("student_id", student_id);

                String requestBody = new JSONObject(postParam).toString();
                Log.d("FeeChalan", "=== REQUEST BODY DEBUG ===");
                Log.d("FeeChalan", "Request Body: " + requestBody);
                Log.d("FeeChalan", "=== END REQUEST BODY DEBUG ===");

                return requestBody.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header_parameter = new HashMap<String, String>();

                header_parameter.put("Content-Type", "application/json");

                return header_parameter;
            }


        };


        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);

    }


    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.search_filter) {
            alertDialog.dismiss();
            load_attendance(parent_id, campus_id, seleted_child_id);

        } else if (id == R.id.back_icon) {
            finish();

        } else if (id == R.id.Cancel) {
            alertDialog.dismiss();

        } else if (id == R.id.show_advanced_filter) {
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.fee_chalan_advanced_search_layout, null);

            select_child_spinner = dialogView.findViewById(R.id.select_child_spinner);
            search_filter = dialogView.findViewById(R.id.search_filter);

            // Apply parent theme colors programmatically
            // MaterialButton will use app:backgroundTint from XML
            // SearchableSpinner will inherit from MaterialCardView styling

            select_child_spinner.setTitle("Select Child");
            // Removed setPositiveButton("OK") to enable auto-dismiss behavior like staff timetable
            search_filter.setOnClickListener(FeeChalan.this);
            Cancel = dialogView.findViewById(R.id.Cancel);
            Cancel.setOnClickListener(FeeChalan.this);

            select_child_spinner.setAdapter(child_adaptor);

            select_child_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Check if studentList is not null and position is valid
                    if (studentList != null && position < studentList.size()) {
                        seleted_child_id = studentList.get(position).getUniqueId();
                        Log.d("FeeChalan", "=== STUDENT SELECTION DEBUG ===");
                        Log.d("FeeChalan", "Selected student position: " + position);
                        Log.d("FeeChalan", "Selected student name: " + studentList.get(position).getFullName());
                        Log.d("FeeChalan", "Selected student ID: " + seleted_child_id);
                        Log.d("FeeChalan", "=== END STUDENT SELECTION DEBUG ===");
                    } else {
                        Log.e("FeeChalan", "Invalid student selection - position: " + position + ", studentList size: " + (studentList != null ? studentList.size() : "null"));
                        Toast.makeText(context, "Invalid student selection.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });

            if (studentList != null && studentList.size() > 1) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setView(dialogView);

                alertDialog = dialogBuilder.create();
                alertDialog.show();
            } else {
                // Check if studentList is not null and has at least one element
                if (studentList != null && studentList.size() > 0) {
                    Log.d("FeeChalan", "=== AUTO-SELECT FIRST STUDENT DEBUG ===");
                    Log.d("FeeChalan", "StudentList size: " + studentList.size());
                    seleted_child_id = studentList.get(0).getUniqueId();
                    Log.d("FeeChalan", "Auto-selected student name: " + studentList.get(0).getFullName());
                    Log.d("FeeChalan", "Auto-selected student ID: " + seleted_child_id);
                    Log.d("FeeChalan", "=== END AUTO-SELECT DEBUG ===");
                    load_attendance(parent_id, campus_id, seleted_child_id);
                } else {
                    Log.e("FeeChalan", "No students available for auto-selection");
                    Toast.makeText(context, "No students available.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void adjustRecyclerViewHeight() {
        // Post a runnable to adjust height after layout is complete
        myrecycleview.post(() -> {
            if (myrecycleview.getAdapter() != null) {
                // Calculate total height needed for all items
                int totalHeight = 0;
                for (int i = 0; i < myrecycleview.getAdapter().getItemCount(); i++) {
                    View itemView = myrecycleview.getLayoutManager().findViewByPosition(i);
                    if (itemView != null) {
                        totalHeight += itemView.getHeight();
                    }
                }
                
                // Set the RecyclerView height to wrap content
                ViewGroup.LayoutParams params = myrecycleview.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                myrecycleview.setLayoutParams(params);
                
                Log.d("FeeChalan", "RecyclerView height adjusted to wrap content");
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

                        // Add bottom margin to footer container to push it above navigation bar
                        android.widget.LinearLayout footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            // Set bottom margin to navigation bar height to ensure footer is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            Log.d("FeeChalan", "Setting footer bottom margin: " + bottomMargin + "dp");
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                                Log.d("FeeChalan", "Footer margin applied successfully");
                            }
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("FeeChalan", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("FeeChalan", "Error setting up window insets: " + e.getMessage());
        }
    }

}
