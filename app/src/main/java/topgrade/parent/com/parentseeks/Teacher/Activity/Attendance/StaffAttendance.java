package topgrade.parent.com.parentseeks.Teacher.Activity.Attendance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
import topgrade.parent.com.parentseeks.Teacher.Adaptor.StaffAttendanceAdaptor;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.ParentAttendanceModel;
import topgrade.parent.com.parentseeks.Parent.Model.MonthModel;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffAttendance extends AppCompatActivity implements View.OnClickListener {


    private RecyclerView myrecycleview;

    List<ParentAttendanceModel> attendenceList;
    Context context;
    TextView start_date, end_date, teacherName;
    ImageView back_icon, Cancel;
    TextView show_advanced_filter;
    AlertDialog alertDialog;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;
    List<String> month_list = new ArrayList<>();
    List<MonthModel> month_list_id = new ArrayList<>();
    String[] months;
    Button search_filter, this_month;
    ArrayAdapter<String> month_adaptor;

    boolean started_date;
    String started_date_format = "";
    String end_date_format = "";
    String month_format = "";
    int total_Present = 0;
    int total_Absent = 0;
    int total_Half_leave = 0;
    int total_FullLeave = 0;
    int total_Late = 0;


    TextView Total_Late, working_days, Total_Full_Leave, Total_Half_Leave, Total_Absent, Total_Present;
    ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Use the correct layout file
        setContentView(R.layout.activity_staff_view_attendance);
        
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

        // Additional fix for older Android versions - Force white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Ensure status bar icons are light (white) on dark background
            getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility() & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();

        progress_bar = findViewById(R.id.progress_bar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        innitlization();
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

                        // Add bottom margin to main content to push it above navigation bar
                        LinearLayout mainContent = findViewById(R.id.main_content);
                        if (mainContent != null) {
                            // Set bottom margin to navigation bar height to ensure content is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) mainContent.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 16; // 16dp extra padding
                                mainContent.setLayoutParams(params);
                            }
                        }

                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("StaffAttendance", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e("StaffAttendance", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e("StaffAttendance", "Error setting up window insets: " + e.getMessage(), e);
        }
    }

    private void innitlization() {


        context = StaffAttendance.this;
        Paper.init(context);
        
        // Load staff credentials from Paper database
        Constant.loadFromPaper();

        teacherName = findViewById(R.id.tv_teacher_title_name_attendance);
        Total_Late = findViewById(R.id.Total_Late);
        working_days = findViewById(R.id.working_days);
        Total_Full_Leave = findViewById(R.id.Total_Full_Leave);
        Total_Half_Leave = findViewById(R.id.Total_Half_Leave);
        Total_Absent = findViewById(R.id.Total_Absent);
        Total_Present = findViewById(R.id.Total_Present);
        myrecycleview = findViewById(R.id.attendence_rcv);
        
        // Setup RecyclerView with LayoutManager
        myrecycleview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        myrecycleview.setHasFixedSize(true);
        
        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String staff_name = Paper.book().read("full_name", "");
        teacherName.setText(staff_name);
        show_advanced_filter = findViewById(R.id.show_advanced_filter);

        attendenceList = new ArrayList<>();

        // Initialize months array first
        months = new DateFormatSymbols().getMonths();
        
        // Initialize month lists
        month_list_id.add(new MonthModel("", "Select Month"));
        for (int i = 0; i < months.length; i++) {
            int new_position = i + 1;
            month_list_id.add(new MonthModel("" + new_position, months[i]));
        }
        for (int i = 0; i < month_list_id.size(); i++) {
            month_list.add(month_list_id.get(i).getMonth());
        }

        setupInlineFilters();
        
        // Set current month as default with year
        Calendar currentCalendar = Calendar.getInstance();
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int currentYear = currentCalendar.get(Calendar.YEAR);
        if (months != null && currentMonth >= 0 && currentMonth < months.length) {
            month_format = String.valueOf(currentMonth + 1) + "/" + currentYear;
        } else {
            // Fallback if months array is not properly initialized
            month_format = String.valueOf(currentMonth + 1) + "/" + currentYear;
        }
        
        Log.d("StaffAttendance", "Initial month format set to: " + month_format);
        Log.d("StaffAttendance", "Current month index: " + currentMonth + " (0-based)");
        Log.d("StaffAttendance", "Current year: " + currentYear);

        myCalendar = Calendar.getInstance();

        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        load_attendance();

    }

    private void setupInlineFilters() {
        try {
            if (show_advanced_filter != null) {
                // Set current month as default
                Calendar currentCalendar = Calendar.getInstance();
                int currentMonth = currentCalendar.get(Calendar.MONTH);
                int currentYear = currentCalendar.get(Calendar.YEAR);
                
                String currentMonthName = months[currentMonth];
                show_advanced_filter.setText(currentMonthName + " " + currentYear);
                
                // Set click listener to show month picker dialog
                show_advanced_filter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMonthSpinner();
                    }
                });
            }
        } catch (Exception e) {
            Log.e("StaffAttendance", "Error setting up inline filters", e);
        }
    }
    
    private void showMonthSpinner() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.StaffDialogTheme);
        builder.setTitle("Select Month");
        
        // Create month list for dialog
        String[] monthOptions = new String[months.length];
        for (int i = 0; i < months.length; i++) {
            monthOptions[i] = months[i] + " " + Calendar.getInstance().get(Calendar.YEAR);
        }
        
        // Create custom adapter with navy blue theme
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item_staff, monthOptions) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(R.color.white));
                textView.setBackgroundColor(getResources().getColor(R.color.navy_blue));
                return view;
            }
        };
        
        builder.setAdapter(adapter, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                // Set month format with year
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                month_format = String.valueOf(which + 1) + "/" + year;
                
                // Update filter button text
                if (show_advanced_filter != null) {
                    show_advanced_filter.setText(monthOptions[which]);
                    show_advanced_filter.setTextColor(getResources().getColor(R.color.navy_blue));
                    show_advanced_filter.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                
                // Load attendance for selected month
                load_attendance();
                dialog.dismiss();
            }
        });
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.white);
        
        // Set dialog width and height to be more compact
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.7), // 70% of screen width
                (int) (getResources().getDisplayMetrics().heightPixels * 0.5)  // 50% of screen height
            );
            
            // Position dialog on the left side
            android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.gravity = android.view.Gravity.LEFT | android.view.Gravity.CENTER_VERTICAL;
            params.x = 50; // 50dp margin from left edge
            dialog.getWindow().setAttributes(params);
        }
    }


    private void updateLabel() {
        String myFormat = "dd-MMMM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
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
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    private void load_attendance() {
        Log.d("StaffAttendance", "=== LOADING ATTENDANCE ===");
        Log.d("StaffAttendance", "Month format: " + month_format);
        Log.d("StaffAttendance", "Staff ID: " + Constant.staff_id);
        Log.d("StaffAttendance", "Campus ID: " + Constant.campus_id);
        Log.d("StaffAttendance", "Start date format: " + started_date_format);
        Log.d("StaffAttendance", "End date format: " + end_date_format);
        
        if (progress_bar != null) {
            progress_bar.setVisibility(View.VISIBLE);
        }
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                API.staff_load_attendance, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                if (progress_bar != null) {
                    progress_bar.setVisibility(View.GONE);
                }
                Log.d("StaffAttendance", "=== API RESPONSE ===");
                Log.d("StaffAttendance", "Full Response: " + response);
                try {
                    JSONObject respone = new JSONObject(response);
                    JSONObject status = respone.getJSONObject("status");
                    Log.d("StaffAttendance", "Status Code: " + status.getString("code"));
                    Log.d("StaffAttendance", "Status Message: " + status.getString("message"));
                    if (status.getString("code").equals("1000")) {
                        JSONArray attendance_array = respone.getJSONArray("attendance");
                        Log.d("StaffAttendance", "Found " + attendance_array.length() + " attendance records");
                        attendenceList.clear();
                        total_Present = 0;
                        total_Absent = 0;
                        total_Half_leave = 0;
                        total_FullLeave = 0;
                        total_Late = 0;
                        
                        // Process attendance data
                        for (int i = 0; i < attendance_array.length(); i++) {
                            JSONObject attendence = attendance_array.getJSONObject(i);
                            Log.d("StaffAttendance", "Raw JSON object: " + attendence.toString());
                            
                            String created_date = attendence.getString("created_date");
                            
                            // Handle note parsing more carefully - check if it's JSON null
                            String note;
                            if (attendence.isNull("note")) {
                                note = null;
                                Log.d("StaffAttendance", "Note is JSON null");
                            } else {
                                note = attendence.getString("note");
                                // Handle the case where backend returns "null" as string
                                if ("null".equals(note)) {
                                    note = null;
                                    Log.d("StaffAttendance", "Note is string 'null', converted to null");
                                } else {
                                    Log.d("StaffAttendance", "Note is not JSON null: '" + note + "'");
                                }
                            }
                            
                            String attendance = attendence.getString("attendance");
                            
                            Log.d("StaffAttendance", "=== RECORD " + (i+1) + " ===");
                            Log.d("StaffAttendance", "Date: " + created_date);
                            Log.d("StaffAttendance", "Note: '" + note + "' (length: " + (note != null ? note.length() : "null") + ")");
                            Log.d("StaffAttendance", "Status: " + attendance);
                            Log.d("StaffAttendance", "Note is null: " + (note == null));
                            Log.d("StaffAttendance", "Note equals 'null': " + "null".equals(note));
                            attendenceList.add(new ParentAttendanceModel(created_date, note, attendance));
                                
                                // Handle attendance status with proper null and type checking
                                if (attendance != null && !attendance.equals("null")) {
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
                                        case "0":
                                            // Sunday or non-working day - don't count in summary
                                            Log.d("StaffAttendance", "Non-working day (Sunday) - not counted in summary");
                                            break;
                                        default:
                                            Log.d("StaffAttendance", "Unknown attendance status: " + attendance);
                                            break;
                                    }
                                } else {
                                    Log.d("StaffAttendance", "No attendance marked for this date");
                                }
                            }
                            
                            // Update summary statistics with null checks (moved outside the if condition)
                            if (Total_Present != null) Total_Present.setText("" + total_Present);
                            if (Total_Absent != null) Total_Absent.setText("" + total_Absent);
                            if (Total_Half_Leave != null) Total_Half_Leave.setText("" + total_Half_leave);
                            if (Total_Full_Leave != null) Total_Full_Leave.setText("" + total_FullLeave);
                            if (Total_Late != null) Total_Late.setText("" + total_Late);
                            
                            String working_day = respone.getString("days");
                            if (working_days != null) working_days.setText("" + working_day);
                            
                            Log.d("StaffAttendance", "Summary - Present: " + total_Present + 
                                  ", Absent: " + total_Absent + 
                                  ", Half Leave: " + total_Half_leave + 
                                  ", Full Leave: " + total_FullLeave + 
                                  ", Late: " + total_Late + 
                                  ", Working Days: " + working_day);
                            
                            // Always set the adapter, even if no data
                            StaffAttendanceAdaptor attendenceAdaptor = new StaffAttendanceAdaptor(context, attendenceList,
                                    new OnClickListener() {
                                        @Override
                                        public void onItemClick(View view, int position) {
                                            // Handle item click if needed
                                        }
                                    });

                            myrecycleview.setAdapter(attendenceAdaptor);
                            
                            progress_bar.setVisibility(View.GONE);
                    } else {
                        String message = status.getString("message");
                        Log.e("StaffAttendance", "API Error: " + message);
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e1) {
                    Log.e("StaffAttendance", "JSON parsing error", e1);
                    if (progress_bar != null) {
                        progress_bar.setVisibility(View.GONE);
                    }
                    Toast.makeText(context, "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progress_bar != null) {
                    progress_bar.setVisibility(View.GONE);
                }
                Log.e("StaffAttendance", "Network error loading attendance", error);
                Toast.makeText(context, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                HashMap<String, String> postParam = new HashMap<String, String>();
                postParam.put("staff_id", Constant.staff_id);
                postParam.put("parent_id", Constant.campus_id);
                postParam.put("month", month_format);
                postParam.put("start_date", started_date_format);
                postParam.put("end_date", end_date_format);
                
                String requestBody = new JSONObject(postParam).toString();
                Log.d("StaffAttendance", "Request Body: " + requestBody);
                
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
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.start_date) {
            started_date = true;
            // Reset to current month when date picker is opened
            Calendar currentCalendar = Calendar.getInstance();
            int currentMonth = currentCalendar.get(Calendar.MONTH);
            int currentYear = currentCalendar.get(Calendar.YEAR);
            String currentMonthName = months[currentMonth];
            show_advanced_filter.setText(currentMonthName + " " + currentYear);
            setDateTimeField();

        } else if (id == R.id.end_date) {
            started_date = false;
            // Reset to current month when date picker is opened
            Calendar currentCalendar = Calendar.getInstance();
            int currentMonth = currentCalendar.get(Calendar.MONTH);
            int currentYear = currentCalendar.get(Calendar.YEAR);
            String currentMonthName = months[currentMonth];
            show_advanced_filter.setText(currentMonthName + " " + currentYear);
            setDateTimeField();

        } else if (id == R.id.Cancel) {
            alertDialog.dismiss();


        } else if (id == R.id.search_filter) {
            alertDialog.dismiss();
            load_attendance();

        } else if (id == R.id.this_month) {
            alertDialog.dismiss();
            month_format = "";
            started_date_format = "";
            end_date_format = "";
            load_attendance();
        }
    }

    private void setDateTimeField() {


        DatePickerDialog datePickerDialog = new DatePickerDialog(context, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();


    }

}
