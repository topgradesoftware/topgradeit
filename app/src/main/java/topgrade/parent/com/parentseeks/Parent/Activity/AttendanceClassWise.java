package topgrade.parent.com.parentseeks.Parent.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import components.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Adaptor.ParentAttendanceAdaptor;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.ParentAttendanceModel;
import topgrade.parent.com.parentseeks.Parent.Model.MonthModel;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;

public class AttendanceClassWise extends AppCompatActivity implements View.OnClickListener {


    private RecyclerView myrecycleview;

    List<ParentAttendanceModel> attendenceList;
    Context context;
    TextView start_date, end_date, show_advanced_filter;
    ImageView back_icon;
    SearchableSpinner select_child_spinner, select_month_spinner;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;
    List<String> month_list = new ArrayList<>();
    List<MonthModel> month_list_id = new ArrayList<>();

    String seleted_child_id = "";
    ArrayAdapter<String> child_adaptor;
    ArrayAdapter<String> month_adaptor;
    List<SharedStudent> studentList = new ArrayList<>();

    boolean started_date;
    String started_date_format = "";
    String end_date_format = "";
    String month_format = "";
    int total_Present = 0;
    int total_Absent = 0;
    int total_Half_leave = 0;
    int total_FullLeave = 0;
    int total_Late = 0;
    String[] months;
    String parent_id;
    String campus_id;
    TextView tvStudentName, Total_Late, working_days, Total_Full_Leave, Total_Half_Leave, Total_Absent, Total_Present;
    ProgressBar progress_bar;
    List<String> student_name_list = new ArrayList<>();

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_attendance_class_wise);
        progress_bar = findViewById(R.id.progress_bar);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Apply theme based on user type
        String userType = Paper.book().read(Constants.User_Type, "");
        Log.d("AttendanceClassWise", "=== THEME DEBUG ===");
        Log.d("AttendanceClassWise", "User Type from Paper: '" + userType + "'");
        Log.d("AttendanceClassWise", "Is STUDENT? " + (userType != null && userType.equals("STUDENT")));
        
        applyTheme(userType);
        
        // Apply comprehensive theme to all UI elements
        applyComprehensiveTheme(userType);
        
        // Apply theme to table header after layout is loaded
        findViewById(R.id.attendance_table_header).post(() -> applyTableHeaderTheme(userType));

        // Get user ID based on user type (student uses student_id, parent uses parent_id)
        if (userType != null && userType.equals("STUDENT")) {
            parent_id = Paper.book().read("student_id");
        } else {
            parent_id = Paper.book().read("parent_id");
        }
        campus_id = Paper.book().read("campus_id");

        innitlization();
    }

    private void innitlization() {


        context = AttendanceClassWise.this;
        Paper.init(context);

        tvStudentName = findViewById(R.id.tv_teacher_title_name_attendance);
        Total_Late = findViewById(R.id.Total_Late);
        working_days = findViewById(R.id.working_days);
        Total_Full_Leave = findViewById(R.id.Total_Full_Leave);
        Total_Half_Leave = findViewById(R.id.Total_Half_Leave);
        Total_Absent = findViewById(R.id.Total_Absent);
        Total_Present = findViewById(R.id.Total_Present);
        myrecycleview = findViewById(R.id.attendence_rcv);
        back_icon = findViewById(R.id.back_icon);
        
        // Setup RecyclerView with LayoutManager
        myrecycleview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        myrecycleview.setHasFixedSize(true);
        
        // Initialize inline filter spinners
        select_child_spinner = findViewById(R.id.select_child_spinner);
        select_month_spinner = findViewById(R.id.select_month_spinner);

        attendenceList = new ArrayList<>();

        back_icon.setOnClickListener(this);

        myCalendar = Calendar.getInstance();

        date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        try {
            studentList = Paper.book().read("students");
        } catch (Exception e) {
            // If there's a serialization error, clear the corrupted data and start fresh
            Paper.book().delete("students");
            studentList = new ArrayList<>();
        }

        if (studentList != null) {
            for (int i = 0; i < studentList.size(); i++) {
                student_name_list.add(studentList.get(i).getFullName());
            }
        }

        month_list_id.add(new MonthModel("", "Select Month"));

        months = new DateFormatSymbols().getMonths();
        for (int i = 0; i < months.length; i++) {
            int new_position = i + 1;
            month_list_id.add(new MonthModel("" + new_position, months[i]));
        }

        for (int i = 0; i < month_list_id.size(); i++) {
            month_list.add(month_list_id.get(i).getMonth());
        }
        
        // Set current month as default
        setCurrentMonth();
        
        // Setup inline filters (after months array is initialized)
        setupInlineFilters();
        
        // Load attendance for current month automatically
        loadCurrentMonthAttendance();


    }

    private void updateLabel() {
        String myFormat = "dd-MMMM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, java.util.Locale.getDefault());
        String selected_date = sdf.format(myCalendar.getTime());
        if (started_date) {
            start_date.setText(selected_date);
            started_date_format = changeDateFormat(selected_date);
        } else {
            end_date.setText(selected_date);
            end_date_format = changeDateFormat(selected_date);

        }

    }

    private String changeDateFormat(String time) {
        String inputPattern = "dd-MMMM-yyyy";
        String outputPattern = "dd/MM/yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, java.util.Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, java.util.Locale.getDefault());

        Date date;
        String str = null;

        try {
            date = inputFormat.parse(time);
            if (date != null) {
                str = outputFormat.format(date);
            }
        } catch (ParseException e) {
            Log.e("AttendanceClassWise", "Error parsing date", e);
        }
        return str;
    }

    /**
     * Set current month as default
     */
    private void setCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        
        // Set month format (e.g., "2024-09" for September 2024)
        month_format = String.format(java.util.Locale.getDefault(), "%04d-%02d", currentYear, currentMonth + 1);
        
        // Update the filter button text to show current month
        if (show_advanced_filter != null) {
            String monthName = new DateFormatSymbols().getMonths()[currentMonth];
            show_advanced_filter.setText(getString(R.string.month_year_format, monthName, currentYear));
            
            // Add visual feedback to show month is selected
            show_advanced_filter.setTextColor(ContextCompat.getColor(this, R.color.dark_brown));
            show_advanced_filter.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        
        Log.d("AttendanceClassWise", "Current month set: " + month_format);
    }

    /**
     * Load attendance for current month automatically
     */
    private void loadCurrentMonthAttendance() {
        if (studentList != null && !studentList.isEmpty()) {
            // Load for first student by default
            if (studentList.size() == 1) {
                seleted_child_id = studentList.get(0).getUniqueId();
                Log.d("AttendanceClassWise", "Auto-loading attendance for single student: " + seleted_child_id);
                load_attendance(parent_id, campus_id, seleted_child_id);
            } else {
                Log.d("AttendanceClassWise", "Multiple students found, inline filters will be shown");
                // Don't auto-load, let user select from inline filters
            }
        } else {
            Log.w("AttendanceClassWise", "No students found in PaperDB");
            Toast.makeText(context, "No students found. Please login again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Setup inline filter controls
     */
    private void setupInlineFilters() {
        // Setup student spinner
        if (studentList.size() == 1) {
            // Hide student spinner if only one student
            select_child_spinner.setVisibility(View.GONE);
            seleted_child_id = studentList.get(0).getUniqueId();
            if (tvStudentName != null) {
                tvStudentName.setText(studentList.get(0).getFullName());
            }
        } else {
            // Show student spinner for multiple students
            select_child_spinner.setVisibility(View.VISIBLE);
            child_adaptor = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, student_name_list);
            select_child_spinner.setAdapter(child_adaptor);
            
            select_child_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    seleted_child_id = studentList.get(position).getUniqueId();
                    if (tvStudentName != null) {
                        tvStudentName.setText(studentList.get(position).getFullName());
                    }
                    // Auto-load attendance when student is selected
                    load_attendance(parent_id, campus_id, seleted_child_id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // Setup month spinner with custom dark brown theme
        month_adaptor = new ArrayAdapter<>(context, R.layout.custom_spinner_item, month_list);
        select_month_spinner.setAdapter(month_adaptor);

        select_month_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    month_format = "";
                } else {
                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    String month_id = month_list_id.get(position).getId();
                    month_format = month_id + "/" + year;
                }
                // Auto-load attendance when month is selected
                if (!seleted_child_id.isEmpty()) {
                    load_attendance(parent_id, campus_id, seleted_child_id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set current month as default selection
        if (months != null) {
            DateFormat dateFormat = new SimpleDateFormat("MMMM", java.util.Locale.getDefault());
            Date currentDate = new Date();
            String current_month = dateFormat.format(currentDate);
            for (int i = 0; i < months.length; i++) {
                if (current_month.equals(months[i])) {
                    select_month_spinner.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    private void load_attendance(final String parent_id, final String campus_id, final String student_id) {

        Log.d("AttendanceClassWise", "Loading attendance for student: " + student_id + ", month: " + month_format);
        progress_bar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                API.load_attendance, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("AttendanceClassWise", "API Response: " + response);
                        try {
                            JSONObject respone = new JSONObject(response);
                            JSONObject status = respone.getJSONObject("status");
                            if (status.getString("code").equals("1000")) {
                                JSONArray attendance_array = respone.getJSONArray("attendance");
                                Log.d("AttendanceClassWise", "Found " + attendance_array.length() + " attendance records");
                                attendenceList.clear();
                                total_Present = 0;
                                total_Absent = 0;
                                total_Half_leave = 0;
                                total_FullLeave = 0;
                                total_Late = 0;
                                // Process attendance data
                                for (int i = 0; i < attendance_array.length(); i++) {
                                    JSONObject attendence = attendance_array.getJSONObject(i);
                                    String created_date = attendence.getString("created_date");
                                    String note = attendence.getString("note");
                                    String attendance = attendence.getString("attendance");
                                    attendenceList.add(new ParentAttendanceModel(created_date, note, attendance));
                                    switch (attendance) {
                                        case "1":
                                            total_Present = total_Present + 1;
                                            break;
                                        case "2":
                                            total_Absent = total_Absent + 1;
                                            break;
                                        case "3":
                                            total_Half_leave = total_Half_leave + 1;
                                            break;
                                        case "4":
                                            total_FullLeave = total_FullLeave + 1;
                                            break;
                                        case "5":
                                            total_Late = total_Late + 1;
                                            break;
                                    }
                                }
                                
                                // Update summary totals
                                Total_Present.setText(String.valueOf(total_Present));
                                Total_Absent.setText(String.valueOf(total_Absent));
                                Total_Half_Leave.setText(String.valueOf(total_Half_leave));
                                Total_Full_Leave.setText(String.valueOf(total_FullLeave));
                                Total_Late.setText(String.valueOf(total_Late));
                                String working_day = respone.getString("days");
                                working_days.setText(working_day);
                                
                                // Always set the adapter, even if no data
                                ParentAttendanceAdaptor attendenceAdaptor = new ParentAttendanceAdaptor(context, attendenceList,
                                        new OnClickListener() {
                                            @Override
                                            public void onItemClick(View view, int position) {
                                                // Handle item click if needed
                                            }
                                        });

                                myrecycleview.setAdapter(attendenceAdaptor);

                                progress_bar.setVisibility(View.GONE);
                            } else {
                                progress_bar.setVisibility(View.GONE);

                                String message = status.getString("message");
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                            }


                        } catch (JSONException e1) {
                            Log.e("AttendanceClassWise", "JSON parsing error", e1);
                            progress_bar.setVisibility(View.GONE);

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                HashMap<String, String> postParam = new HashMap<>();
                postParam.put("parent_parent_id", parent_id);
                postParam.put("parent_id", campus_id);
                postParam.put("student_id", student_id);
                postParam.put("month", month_format);
                postParam.put("start_date", started_date_format);
                postParam.put("end_date", end_date_format);

                return new JSONObject(postParam).toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> header_parameter = new HashMap<>();

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
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.start_date) {
            started_date = true;
            select_month_spinner.setSelection(0);
            setDateTimeField();

        } else if (id == R.id.end_date) {
            started_date = false;
            select_month_spinner.setSelection(0);
            setDateTimeField();

        } else if (id == R.id.back_icon) {
            finish();

        }
    }



    private void setDateTimeField() {


        DatePickerDialog datePickerDialog = new DatePickerDialog(context, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();


    }

    private void applyComprehensiveTheme(String userType) {
        try {
            Log.d("AttendanceClassWise", "Applying comprehensive theme for user type: " + userType);
            
            // Get theme colors
            int primaryColor;
            int headerWaveRes;
            
            if (userType != null && userType.equals("STUDENT")) {
                // Student theme (teal)
                primaryColor = ContextCompat.getColor(AttendanceClassWise.this, R.color.student_primary);
                headerWaveRes = R.drawable.bg_wave_teal;
                Log.d("AttendanceClassWise", "Applying STUDENT theme (teal)");
            } else {
                // Parent theme (dark brown) - default
                primaryColor = ContextCompat.getColor(AttendanceClassWise.this, R.color.dark_brown);
                headerWaveRes = R.drawable.bg_wave_dark_brown;
                Log.d("AttendanceClassWise", "Applying PARENT theme (dark brown)");
            }
            
            // Apply header wave background
            ImageView headerWave = findViewById(R.id.header_wave);
            if (headerWave != null) {
                headerWave.setImageResource(headerWaveRes);
                Log.d("AttendanceClassWise", "Header wave background applied: " + headerWaveRes);
            }
            
            // Apply status bar color
            getWindow().setStatusBarColor(primaryColor);
            
            // Apply theme to summary section
            applySummaryTheme(primaryColor);
            
        } catch (Exception e) {
            Log.e("AttendanceClassWise", "Error applying comprehensive theme", e);
        }
    }
    
    private void applySummaryTheme(int primaryColor) {
        try {
            // Apply theme to summary title
            TextView summaryTitle = findViewById(R.id.summary_title);
            if (summaryTitle != null) {
                summaryTitle.setTextColor(primaryColor);
            }
            
            // Apply theme to all summary value TextViews
            int[] summaryTextIds = {
                R.id.Total_Present, R.id.Total_Absent, R.id.Total_Half_Leave,
                R.id.Total_Full_Leave, R.id.Total_Late, R.id.working_days
            };
            
            for (int id : summaryTextIds) {
                TextView textView = findViewById(id);
                if (textView != null) {
                    textView.setTextColor(primaryColor);
                }
            }
            
        } catch (Exception e) {
            Log.e("AttendanceClassWise", "Error applying summary theme", e);
        }
    }
    
    private void applyTableHeaderTheme(String userType) {
        try {
            Log.d("AttendanceClassWise", "=== TABLE HEADER THEME DEBUG ===");
            Log.d("AttendanceClassWise", "Applying table header theme for user type: '" + userType + "'");
            
            // Get theme colors
            int headerColor;
            if (userType != null && userType.equals("STUDENT")) {
                headerColor = R.color.student_primary;
                Log.d("AttendanceClassWise", "Applying STUDENT theme (teal) to table header");
                Log.d("AttendanceClassWise", "Student primary color: " + ContextCompat.getColor(AttendanceClassWise.this, headerColor));
            } else {
                headerColor = R.color.dark_brown;
                Log.d("AttendanceClassWise", "Applying PARENT theme (dark brown) to table header");
                Log.d("AttendanceClassWise", "Dark brown color: " + ContextCompat.getColor(AttendanceClassWise.this, headerColor));
            }
            
            // Find the included table header layout and apply theme
            View tableHeaderView = findViewById(R.id.attendance_table_header);
            if (tableHeaderView != null) {
                Log.d("AttendanceClassWise", "Table header view found, applying background color");
                
                // Apply background color to the table header - force override XML by setting background to null first
                tableHeaderView.setBackground(null);
                tableHeaderView.setBackgroundColor(ContextCompat.getColor(AttendanceClassWise.this, headerColor));
                
                // Apply theme to all TextViews in the table header
                applyThemeToChildren(tableHeaderView, headerColor);
                
                Log.d("AttendanceClassWise", "Table header theme applied successfully");
            } else {
                Log.e("AttendanceClassWise", "Table header view not found - ID: attendance_table_header");
            }
            
        } catch (Exception e) {
            Log.e("AttendanceClassWise", "Error applying table header theme", e);
        }
    }
    
    private void applyThemeToChildren(View parent, int colorRes) {
        try {
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                Log.d("AttendanceClassWise", "Processing ViewGroup with " + viewGroup.getChildCount() + " children");
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View child = viewGroup.getChildAt(i);
                    if (child instanceof TextView) {
                        TextView textView = (TextView) child;
                        Log.d("AttendanceClassWise", "Found TextView: " + textView.getText() + ", applying background color");
                        // Force override XML background by setting background to null first, then applying color
                        textView.setBackground(null);
                        textView.setBackgroundColor(ContextCompat.getColor(AttendanceClassWise.this, colorRes));
                    } else if (child instanceof ViewGroup) {
                        Log.d("AttendanceClassWise", "Found nested ViewGroup, recursing...");
                        applyThemeToChildren(child, colorRes);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("AttendanceClassWise", "Error applying theme to children", e);
        }
    }
    
    /**
     * Apply theme based on user type (consistent with other activities)
     */
    private void applyTheme(String userType) {
        try {
            Log.d("AttendanceClassWise", "Applying theme for userType: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                int tealColor = ContextCompat.getColor(this, R.color.student_primary);
                getWindow().setStatusBarColor(tealColor);
                getWindow().setNavigationBarColor(tealColor);
                
                // Force light status bar icons for better visibility on teal background
                getWindow().getDecorView().setSystemUiVisibility(
                    getWindow().getDecorView().getSystemUiVisibility() & 
                    ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
                
                // Force dark navigation bar icons (prevent light appearance)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // 0 = do NOT use light icons
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }
                
                Log.d("AttendanceClassWise", "Applied STUDENT theme (teal)");
            } else {
                // Apply unified parent theme for attendance class wise page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for attendance
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for attendance
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Class Wise Attendance");
                
                Log.d("AttendanceClassWise", "Applied PARENT theme (brown)");
            }
            
        } catch (Exception e) {
            Log.e("AttendanceClassWise", "Error applying theme, using parent theme as fallback", e);
            // Fallback to parent theme
            ThemeHelper.applyParentTheme(this);
        }
    }
}
