package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import components.searchablespinnerlibrary.SearchableSpinner;
import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.MonthModel;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Parent.Model.Subject;
import topgrade.parent.com.parentseeks.Parent.Model.SubjectAttendeceModel;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;

/**
 * Improved version of AttendanceSubjectWise that creates a proper grid layout
 * matching the design shown in the user's images
 */
public class AttendanceSubjectWiseImproved extends AppCompatActivity implements View.OnClickListener, OnClickListener {

    private static final String TAG = "AttendanceSubjectWiseImproved";

    // UI Components
    private ImageView back_icon;
    private SearchableSpinner select_child_spinner, select_month_spinner;
    private ProgressBar progress_bar;
    // grid_container removed - no longer needed
    // subject_headers_container removed - subjects are now directly in XML
    // data_rows_container removed - using RecyclerView instead
    private TableLayout attendance_table;
    private TableLayout header_table;

    // Data
    private Context context;
    private final List<SubjectAttendeceModel.AttendanceList> attendanceList = new ArrayList<>();
    private final List<String> dateList = new ArrayList<>();
    private final List<String> subjectList = new ArrayList<>();
    private List<String> sortedSubjectsList = new ArrayList<>(); // Sorted and deduplicated subjects for header and data rows
    // subjects_list converted to local variables in methods
    private List<SharedStudent> studentList = new ArrayList<>();
    private final List<String> student_name_list = new ArrayList<>();
    private final List<String> month_list = new ArrayList<>();
    private final List<MonthModel> month_list_id = new ArrayList<>();

    // Adapters - converted to local variables

    // Variables
    private String seleted_child_id = "";
    private String parent_id;
    private String campus_id;
    private String month_format = "";

    // Improved adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fix navigation bar issues and remove system padding
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        
        // Remove system window insets padding
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        setContentView(R.layout.activity_attendance_subject_wise_improved);

        // Apply theme based on user type
        String userType = Paper.book().read(Constants.User_Type, "");
        applyTheme(userType);
        
        // Apply comprehensive theme to all UI elements
        applyComprehensiveTheme(userType);

        context = this;
        initializeViews();
        initializeData();
        setupClickListeners();
        setCurrentMonth();
        loadCurrentMonthAttendance();
        
        // TableLayout automatically handles header and data alignment
        
        // Delay to ensure layout is fully rendered
        // Setup attendance rows immediately
        Log.d(TAG, "Setting up attendance rows immediately");
        
        // Create initial header with sample subjects
        // Don't create table until a student is selected
        // The table will be created when:
        // 1. A student is selected (if multiple students)
        // 2. Auto-loading (if single student)
        // 3. Manual search
    }

    private void initializeViews() {
        back_icon = findViewById(R.id.back_icon);
        select_child_spinner = findViewById(R.id.select_child_spinner);
        select_month_spinner = findViewById(R.id.select_month_spinner);
        progress_bar = findViewById(R.id.progress_bar);
        // grid_container removed - no longer needed
        // subject_headers_container removed - subjects are now directly in XML
        // Subject headers are now directly in XML layout
        // data_rows_container removed - using RecyclerView instead
        attendance_table = findViewById(R.id.attendance_table);
        header_table = findViewById(R.id.header_table);
    }

    private void initializeData() {
        // Get user ID based on user type (student uses student_id, parent uses parent_id)
        String userType = Paper.book().read(Constants.User_Type, "");
        if (userType != null && userType.equals("STUDENT")) {
            parent_id = Paper.book().read("student_id");
        } else {
            parent_id = Paper.book().read("parent_id");
        }
        campus_id = Paper.book().read("campus_id");
        List<SharedStudent> tempStudentList = Paper.book().read("students", new ArrayList<>());
        studentList = (tempStudentList != null) ? tempStudentList : new ArrayList<>();
        
        // Initialize student names list
        student_name_list.clear();
        for (SharedStudent student : studentList) {
            student_name_list.add(student.getFullName());
        }
        
        // Setup child adapter - use same layout as class-wise attendance
        ArrayAdapter<String> child_adaptor = new ArrayAdapter<>(context, R.layout.spinner_selected_item, student_name_list);
        child_adaptor.setDropDownViewResource(android.R.layout.simple_list_item_1);
        select_child_spinner.setAdapter(child_adaptor);
        if (!student_name_list.isEmpty()) {
            select_child_spinner.setTitle("Select Child");
        }
    }

    private void setupClickListeners() {
        back_icon.setOnClickListener(this);
        
        select_child_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (studentList != null && position < studentList.size()) {
                    seleted_child_id = studentList.get(position).getUniqueId();
                    // Update spinner title to show selected student
                    if (position < student_name_list.size()) {
                        select_child_spinner.setTitle(student_name_list.get(position));
                    }
                    // Auto-load attendance if month is also selected
                    checkAndLoadAttendance();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Setup month spinner
        setupMonthSpinner();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_icon) {
            finish();
        }
    }

    private void checkAndLoadAttendance() {
        if (seleted_child_id != null && !seleted_child_id.isEmpty() && 
            month_format != null && !month_format.isEmpty()) {
            Log.d(TAG, "Auto-loading attendance for child: " + seleted_child_id + ", month: " + month_format);
            load_attendance(parent_id, campus_id, seleted_child_id);
        }
    }

    private void setupMonthSpinner() {
        // No need to check for initialization since month_adaptor is now a local variable
        
        // Initialize month list
        month_list.clear();
        month_list_id.clear();
        
        // Get current year
        Calendar calendar = Calendar.getInstance();
        
        // Create month list
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        
        // Add months with IDs
        for (int i = 0; i < months.length; i++) {
            int new_position = i + 1;
            month_list_id.add(new MonthModel(String.valueOf(new_position), months[i]));
            month_list.add(months[i]); // Add to month_list directly
        }
        
        // Setup month adapter - use same layout as class-wise attendance
        ArrayAdapter<String> month_adaptor = new ArrayAdapter<>(context, R.layout.spinner_selected_item, month_list);
        month_adaptor.setDropDownViewResource(android.R.layout.simple_list_item_1);
        select_month_spinner.setAdapter(month_adaptor);
        
        // Set current month as default selection
        int currentMonth = calendar.get(Calendar.MONTH);
        
        // Set month selection listener
        select_month_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Month selected: position=" + position + ", id=" + id);
                
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                String month_id = month_list_id.get(position).getId();
                // Use consistent YYYY-MM format
                month_format = String.format(Locale.getDefault(), "%04d-%02d", year, Integer.parseInt(month_id));
                Log.d(TAG, "Month selected: " + month_format + " (position: " + position + ", month_id: " + month_id + ")");
                
                // Update spinner title to show selected month
                if (position < month_list.size()) {
                    select_month_spinner.setTitle(month_list.get(position));
                }
                
                // Fix text truncation by updating the selected view
                select_month_spinner.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            View selectedView = select_month_spinner.getSelectedView();
                            if (selectedView instanceof TextView) {
                                TextView textView = (TextView) selectedView;
                                textView.setSingleLine(false);
                                textView.setMaxLines(2);
                                textView.setEllipsize(null);
                                textView.setHorizontallyScrolling(false);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error fixing month spinner text display", e);
                        }
                    }
                });
                
                // Generate month dates locally first
                generateMonthDates();
                
                // Auto-load attendance if child is also selected
                checkAndLoadAttendance();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Set current month selection and title immediately
        if (currentMonth < month_list.size()) {
            select_month_spinner.setTitle(month_list.get(currentMonth));
        }
        
        // Set current month selection after listener is set up
        select_month_spinner.post(new Runnable() {
            @Override
            public void run() {
                select_month_spinner.setSelection(currentMonth, false); // false = don't trigger listener
                // Ensure title is set after selection
                if (currentMonth < month_list.size()) {
                    select_month_spinner.setTitle(month_list.get(currentMonth));
                }
                
                // Fix text truncation by updating the selected view - use nested post to ensure view is ready
                select_month_spinner.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            View selectedView = select_month_spinner.getSelectedView();
                            if (selectedView instanceof TextView) {
                                TextView textView = (TextView) selectedView;
                                textView.setSingleLine(false);
                                textView.setMaxLines(2);
                                textView.setEllipsize(null);
                                textView.setHorizontallyScrolling(false);
                                // Force layout update
                                textView.requestLayout();
                                select_month_spinner.requestLayout();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error fixing month spinner text display", e);
                        }
                    }
                });
            }
        });
    }

    private void setCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        
        month_format = String.format(Locale.getDefault(), "%04d-%02d", currentYear, currentMonth + 1);
        Log.d(TAG, "Current month set: " + month_format);
    }

    private void loadCurrentMonthAttendance() {
        if (studentList != null && !studentList.isEmpty()) {
            if (studentList.size() == 1) {
                seleted_child_id = studentList.get(0).getUniqueId();
                load_attendance(parent_id, campus_id, seleted_child_id);
            } else {
                Log.d(TAG, "Multiple students found, filter card already visible");
            }
        } else {
            Log.w(TAG, "No students found in PaperDB");
            Toast.makeText(context, "No students found. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(context, ParentLoginActivity.class));
            finish();
        }
    }

    private void load_attendance(final String parent_id, final String campus_id, final String student_id) {
        Log.d(TAG, "Loading attendance for student: " + student_id);

        progress_bar.setVisibility(View.VISIBLE);

        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("parent_parent_id", parent_id);
        postParam.put("parent_id", campus_id);
        postParam.put("student_id", student_id);
        postParam.put("month", month_format);
        postParam.put("subject_id", "");
        postParam.put("start_date", "");
        postParam.put("end_date", "");
        
        // Log API parameters for debugging
        Log.d(TAG, "API Parameters:");
        Log.d(TAG, "parent_parent_id: " + parent_id);
        Log.d(TAG, "parent_id (campus_id): " + campus_id);
        Log.d(TAG, "student_id: " + student_id);
        Log.d(TAG, "month: " + month_format);
        Log.d(TAG, "subject_id: (empty)");
        Log.d(TAG, "start_date: (empty)");
        Log.d(TAG, "end_date: (empty)");

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        // Use the proper API constant instead of hardcoded URL
        String apiUrl = API.load_attendance_subjectwise;
        Log.d(TAG, "Making API request to: " + apiUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progress_bar.setVisibility(View.GONE);
                        Log.d(TAG, "API Response: " + response);
                        parseAttendanceResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress_bar.setVisibility(View.GONE);
                        Log.e(TAG, "API Error: " + error.getMessage());
                        
                        // Show user-friendly error message
                        String errorMessage = "Unable to load attendance data";
                        if (error.getMessage() != null) {
                            if (error.getMessage().contains("UnknownHostException")) {
                                errorMessage = "No internet connection. Please check your network and try again.";
                            } else if (error.getMessage().contains("timeout")) {
                                errorMessage = "Connection timeout. Please try again.";
                            } else {
                                errorMessage = "Network error: " + error.getMessage();
                            }
                        }
                        
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                        
                        // Show empty table with message
                        setupEmptyTable();
                    }
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                return postParam;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void parseAttendanceResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONObject status = jsonResponse.getJSONObject("status");
            
            Log.d(TAG, "API Response Status: " + status.getString("code") + " - " + status.getString("message"));
            
            if (status.getString("code").equals("1000")) {
                JSONArray attendance_array = jsonResponse.getJSONArray("attendance_list");
                Log.d(TAG, "Found " + attendance_array.length() + " attendance records");
                
                attendanceList.clear();
                dateList.clear();
                subjectList.clear();
                
                // Parse attendance data
                for (int i = 0; i < attendance_array.length(); i++) {
                    JSONObject attendance = attendance_array.getJSONObject(i);
                    String subject = attendance.getString("subject");
                    subjectList.add(subject);
                    Log.d(TAG, "Added subject: " + subject);
                    
                    JSONArray attendance_records = attendance.getJSONArray("attendence");
                    List<SubjectAttendeceModel.Attendance> attendanceRecords = new ArrayList<>();
                    
                    for (int j = 0; j < attendance_records.length(); j++) {
                        JSONObject record = attendance_records.getJSONObject(j);
                        String created_date = record.getString("created_date");
                        String attendance_status = record.getString("attendance");
                        Object note = record.opt("note");
                        
                        if (i == 0) { // Only add dates from first subject to avoid duplicates
                            dateList.add(created_date);
                        }
                        
                        attendanceRecords.add(new SubjectAttendeceModel().new Attendance());
                        attendanceRecords.get(j).setCreatedDate(created_date);
                        attendanceRecords.get(j).setAttendance(attendance_status);
                        attendanceRecords.get(j).setNote(note);
                    }
                    
                    SubjectAttendeceModel.AttendanceList attendanceListObj = new SubjectAttendeceModel().new AttendanceList();
                    attendanceListObj.setSubject(subject);
                    attendanceListObj.setAttendance(attendanceRecords);
                    attendanceList.add(attendanceListObj);
                }
                
                Log.d(TAG, "Total subjects found: " + subjectList.size());
                Log.d(TAG, "Total dates found: " + dateList.size());
                Log.d(TAG, "Subjects: " + subjectList);
                Log.d(TAG, "Dates: " + dateList);
                
                // Log debug info if available
                if (jsonResponse.has("debug_info")) {
                    try {
                        JSONObject debugInfo = jsonResponse.getJSONObject("debug_info");
                        Log.d(TAG, "Debug Info - Subjects found: " + debugInfo.optString("subjects_found", "N/A"));
                        Log.d(TAG, "Debug Info - Date range: " + debugInfo.optString("date_range", "N/A"));
                        Log.d(TAG, "Debug Info - Student ID: " + debugInfo.optString("student_id", "N/A"));
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing debug_info: " + e.getMessage());
                    }
                }
                
                // Force header creation with the loaded subjects
                createHeaderRow();
                
                // Only setup attendance rows if we have real data
                if (!attendanceList.isEmpty() && !dateList.isEmpty()) {
                    setupAttendanceRows();
                } else {
                    Log.w(TAG, "No API data available, creating empty rows with local subjects");
                    createEmptyRowsWithLocalSubjects();
                }
                
            } else {
                String errorCode = status.optString("code", "unknown");
                String errorMessage = status.optString("message", "Unknown error occurred");
                
                Log.e(TAG, "API Error Code: " + errorCode + " - Message: " + errorMessage);
                
                // Show user-friendly error messages based on error codes
                String userMessage;
                switch (errorCode) {
                    case "2001":
                        userMessage = "Parent not found. Please login again.";
                        break;
                    case "2002":
                        userMessage = "Student ID is required.";
                        break;
                    case "2003":
                        userMessage = "No active session found.";
                        break;
                    case "2004":
                        userMessage = "No subjects found for this student.";
                        break;
                    case "2005":
                        userMessage = "No valid subjects found for this student.";
                        break;
                    default:
                        userMessage = "Error: " + errorMessage;
                        break;
                }
                
                Toast.makeText(context, userMessage, Toast.LENGTH_LONG).show();
                // Show empty table with error message
                setupEmptyTable();
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
            Log.e(TAG, "Response was: " + response);
            
            // Check if response is empty
            if (response == null || response.trim().isEmpty()) {
                Log.d(TAG, "API returned empty response - no data available");
                Toast.makeText(context, "No attendance data available for this month", Toast.LENGTH_LONG).show();
                // Create header first, then empty rows with local subjects when API returns empty response
                createHeaderRow();
                createEmptyRowsWithLocalSubjects();
            } else {
                Log.d(TAG, "API returned invalid JSON: " + response);
                Toast.makeText(context, "Invalid data format received from server", Toast.LENGTH_LONG).show();
                // Create header first, then empty rows with local subjects when API returns invalid JSON
                createHeaderRow();
                createEmptyRowsWithLocalSubjects();
            }
            
            // Don't call setupEmptyTable() here because we already created the table with data rows
            // setupEmptyTable(); // This was clearing the rows we just added!
        }
    }


    /**
     * Get sorted and deduplicated subjects list
     * This ensures consistent ordering between header and data rows
     * Priority: Paper DB (ALL subjects from employee_subject) > SharedStudent subjects > API subjects (only subjects with attendance) > Sample subjects
     * 
     * Note: Paper DB should contain ALL subjects for the student (loaded during login/profile from employee_subject table),
     * matching the PHP backend behavior where subjects are loaded from employee_subject table and stored in $value['subjects'].
     * If Paper DB is empty, we fall back to SharedStudent.getSubjects() which also contains subjects from employee_subject.
     * API only returns subjects that have attendance records for the selected month.
     */
    private void getSortedSubjectsList() {
        List<String> subjectsToShow = new ArrayList<>();
        
        // PRIORITY 1: Load ALL subjects from Paper DB first (matches web behavior - loads from employee_subject table)
        // Paper DB should have all subjects for the student, not just those with attendance records
        if (seleted_child_id != null && !seleted_child_id.isEmpty()) {
            Log.d(TAG, "Loading ALL subjects from Paper DB with key: " + seleted_child_id);
            List<Subject> tempSubjectsList = Paper.book().read(seleted_child_id, new ArrayList<>());
            List<Subject> subjects_list = (tempSubjectsList != null) ? tempSubjectsList : new ArrayList<>();
            Log.d(TAG, "Paper DB subjects found: " + subjects_list.size());
            
            for (Subject subject : subjects_list) {
                if (subject != null && subject.getSubject_name() != null && !subject.getSubject_name().trim().isEmpty()) {
                    String subjectName = subject.getSubject_name().trim();
                    if (!subjectsToShow.contains(subjectName)) {
                        subjectsToShow.add(subjectName);
                        Log.d(TAG, "Added Paper DB subject: " + subjectName);
                    }
                }
            }
        } else {
            Log.w(TAG, "seleted_child_id is null or empty: " + seleted_child_id);
        }
        
        // PRIORITY 1.5: If Paper DB is empty, try loading from SharedStudent object (subjects are part of student data structure)
        // This matches the PHP backend where $value['subjects'] is part of each student object from employee_subject table
        if (subjectsToShow.isEmpty() && seleted_child_id != null && !seleted_child_id.isEmpty() && studentList != null) {
            Log.d(TAG, "Paper DB empty, loading subjects from SharedStudent object for: " + seleted_child_id);
            for (SharedStudent student : studentList) {
                if (student != null && student.getUniqueId() != null && student.getUniqueId().equals(seleted_child_id)) {
                    List<Subject> studentSubjects = student.getSubjects();
                    if (studentSubjects != null && !studentSubjects.isEmpty()) {
                        Log.d(TAG, "Found " + studentSubjects.size() + " subjects in SharedStudent object");
                        for (Subject subject : studentSubjects) {
                            if (subject != null && subject.getSubject_name() != null && !subject.getSubject_name().trim().isEmpty()) {
                                String subjectName = subject.getSubject_name().trim();
                                if (!subjectsToShow.contains(subjectName)) {
                                    subjectsToShow.add(subjectName);
                                    Log.d(TAG, "Added SharedStudent subject: " + subjectName);
                                }
                            }
                        }
                        // Also save to Paper DB for future use
                        try {
                            Paper.book().write(seleted_child_id, studentSubjects);
                            Log.d(TAG, "Saved subjects to Paper DB for future use");
                        } catch (Exception e) {
                            Log.e(TAG, "Error saving subjects to Paper DB", e);
                        }
                    }
                    break; // Found the student, no need to continue
                }
            }
        }
        
        // PRIORITY 2: Merge with API subjects (subjects that have attendance records for this month)
        // This ensures we have all subjects, even if some don't have attendance records yet
        if (!subjectList.isEmpty()) {
            Log.d(TAG, "Merging API subjects (subjects with attendance records): " + subjectList.size());
            for (String apiSubject : subjectList) {
                if (apiSubject != null && !apiSubject.trim().isEmpty()) {
                    String subjectName = apiSubject.trim();
                    if (!subjectsToShow.contains(subjectName)) {
                        subjectsToShow.add(subjectName);
                        Log.d(TAG, "Added API subject (not in Paper DB or SharedStudent): " + subjectName);
                    }
                }
            }
            Log.d(TAG, "API subjects: " + subjectList);
        } else {
            Log.w(TAG, "No API subjects found in subjectList");
        }
        
        // PRIORITY 3: Final fallback to sample subjects (only if all other sources are empty)
        if (subjectsToShow.isEmpty()) {
            Log.w(TAG, "No subjects found from Paper DB, SharedStudent, or API, using sample subjects");
            subjectsToShow = new ArrayList<>(java.util.Arrays.asList("MATH", "ENG", "SCI", "HIST"));
        }
        
        // Remove duplicates using LinkedHashSet to preserve order, then convert back to list
        java.util.LinkedHashSet<String> uniqueSubjects = new java.util.LinkedHashSet<>(subjectsToShow);
        sortedSubjectsList = new ArrayList<>(uniqueSubjects);
        
        // Sort alphabetically (case-insensitive)
        java.util.Collections.sort(sortedSubjectsList, String.CASE_INSENSITIVE_ORDER);
        
        Log.d(TAG, "Final sorted and deduplicated subjects (" + sortedSubjectsList.size() + "): " + sortedSubjectsList);
    }
    
    private void createHeaderRow() {
        Log.d(TAG, "createHeaderRow() called");
        Log.d(TAG, "header_table is null: " + (header_table == null));
        
        if (header_table == null) {
            Log.e(TAG, "header_table is null! Cannot create header row.");
            return;
        }
        
        // Clear existing header
        header_table.removeAllViews();
        
        // Make header table visible
        header_table.setVisibility(View.VISIBLE);
        
        TableRow headerRow = new TableRow(context);
        headerRow.setLayoutParams(new TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        ));
        headerRow.setMinimumHeight(80); // Increased height for better visibility
        
        // Date header - reduced width to accommodate more subjects
        TextView dateHeader = createHeaderCell("Date", 0.1f);
        headerRow.addView(dateHeader);
        Log.d(TAG, "Added Date header");
        
        // Get sorted and deduplicated subjects list
        getSortedSubjectsList();
        
        Log.d(TAG, "Final sorted subjects to show in header: " + sortedSubjectsList);
        
        // Calculate weight per subject (remaining space divided by number of subjects)
        float subjectWeight = (1.0f - 0.1f) / sortedSubjectsList.size(); // 0.1f is for Date column
        
        for (String subject : sortedSubjectsList) {
            TextView subjectHeader = createHeaderCell(subject, subjectWeight);
            headerRow.addView(subjectHeader);
            Log.d(TAG, "Added subject header: " + subject + " with weight: " + subjectWeight);
        }
        
        header_table.addView(headerRow);
        Log.d(TAG, "Header row added to header_table. Total children: " + header_table.getChildCount());
        
        // Force layout update
        header_table.requestLayout();
        header_table.invalidate();
    }
    
    private TextView createHeaderCell(String text, float weight) {
        TextView cell = new TextView(context);
        cell.setText(text);
        cell.setTextSize(12); // Smaller text size to ensure fit
        cell.setTextColor(android.graphics.Color.WHITE);
        cell.setGravity(android.view.Gravity.CENTER);
        cell.setMinHeight(80); // Increased height for better text display
        cell.setMaxHeight(80); // Increased height for better text display
        cell.setPadding(4, 4, 4, 4); // Reduced padding for more text space
        
        // Apply theme-based background color instead of hardcoded drawables
        String userType = Paper.book().read(Constants.User_Type, "");
        int headerColor;
        if (userType != null && userType.equals("STUDENT")) {
            headerColor = ContextCompat.getColor(context, R.color.student_primary);
            Log.d(TAG, "Applying STUDENT theme (teal) to header cell: " + text);
        } else {
            headerColor = ContextCompat.getColor(context, R.color.dark_brown);
            Log.d(TAG, "Applying PARENT theme (dark brown) to header cell: " + text);
        }
        
        // Set background color directly instead of using drawable resources
        cell.setBackgroundColor(headerColor);
        
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, 80, weight);
        cell.setLayoutParams(params);
        
        Log.d(TAG, "Created header cell: " + text + " with weight: " + weight + " and color: " + headerColor);
        
        return cell;
    }

    private void setupAttendanceRows() {
        // Clear all existing data rows
        attendance_table.removeAllViews();
        
        // Check if we have real data
        if (attendanceList.isEmpty() || dateList.isEmpty()) {
            Log.w(TAG, "setupAttendanceRows called with no data - this should not happen");
            Toast.makeText(context, "No attendance data found for selected month", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Setting up table with " + dateList.size() + " dates and " + attendanceList.size() + " subjects");
        
        // Use the same sorted subjects list as header
        getSortedSubjectsList();
        
        // Create attendance matrix: [date][subject] - use sorted subjects list
        String[][] attendanceMatrix = new String[dateList.size()][sortedSubjectsList.size()];
        
        // Initialize matrix with empty values
        for (int i = 0; i < dateList.size(); i++) {
            for (int j = 0; j < sortedSubjectsList.size(); j++) {
                attendanceMatrix[i][j] = "-"; // Default empty value
            }
        }
        
        // Fill matrix with actual attendance data
        // Map API subjects to sorted subjects list
        for (int apiSubjectIndex = 0; apiSubjectIndex < attendanceList.size(); apiSubjectIndex++) {
            SubjectAttendeceModel.AttendanceList subjectAttendance = attendanceList.get(apiSubjectIndex);
            String apiSubjectName = subjectAttendance.getSubject();
            List<SubjectAttendeceModel.Attendance> records = subjectAttendance.getAttendance();
            
            // Find matching subject index in sorted subjects list
            int localSubjectIndex = sortedSubjectsList.indexOf(apiSubjectName);
            if (localSubjectIndex == -1) {
                Log.w(TAG, "Subject not found in local storage: " + apiSubjectName);
                continue; // Skip this subject if not found in local storage
            }
            
            for (SubjectAttendeceModel.Attendance record : records) {
                String date = record.getCreatedDate();
                String status = record.getAttendance();
                
                // Find date index
                int dateIndex = dateList.indexOf(date);
                if (dateIndex >= 0 && dateIndex < attendanceMatrix.length && 
                    localSubjectIndex >= 0 && localSubjectIndex < attendanceMatrix[dateIndex].length) {
                    // Convert status to display format
                    String displayStatus = convertAttendanceStatus(status);
                    attendanceMatrix[dateIndex][localSubjectIndex] = displayStatus;
                }
            }
        }
        
        // Add data rows
        for (int i = 0; i < dateList.size(); i++) {
            TableRow row = new TableRow(context);
            row.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setMinimumHeight(80);
            
            // Date cell (reduced weight to accommodate more subjects) - use theme color
            String formattedDate = formatDateForDisplay(dateList.get(i));
            String userType = Paper.book().read(Constants.User_Type, "");
            String dateCellColor = (userType != null && userType.equals("STUDENT")) ? 
                "#004d40" : "#8B4513"; // Teal for student, dark brown for parent
            TextView dateCell = createCell(formattedDate, dateCellColor, android.graphics.Color.WHITE, 0.1f);
            row.addView(dateCell);
            
            // Check if this date is a Sunday
            boolean isSunday = isDateSunday(dateList.get(i));
            
            // Calculate weight per subject (same as header)
            float subjectWeight = (1.0f - 0.1f) / attendanceMatrix[i].length; // 0.1f is for Date column
            
            // Attendance cells for each subject
            for (int j = 0; j < attendanceMatrix[i].length; j++) {
                String status;
                if (isSunday) {
                    status = "Sunday"; // Show "Sunday" for Sundays
                } else {
                    status = attendanceMatrix[i][j];
                }
                
                String bgColor;
                int textColor;
                if (status.equals("Sunday")) {
                    // Use theme color for Sunday - dark brown for parent, teal for student
                    bgColor = (userType != null && userType.equals("STUDENT")) ? 
                        "#004d40" : "#8B4513"; // Teal for student, dark brown for parent
                    textColor = android.graphics.Color.WHITE;
                } else if (status.equals("A")) {
                    bgColor = "#DF4242"; // Red for absent
                    textColor = android.graphics.Color.WHITE;
                } else {
                    bgColor = "#FFFFFF"; // White for present/other
                    textColor = android.graphics.Color.BLACK;
                }
                TextView cell = createCell(status, bgColor, textColor, subjectWeight);
                row.addView(cell);
            }
            
            attendance_table.addView(row);
        }
        
        Toast.makeText(context, "Table loaded with " + dateList.size() + " dates", Toast.LENGTH_SHORT).show();
    }
    
    private String convertAttendanceStatus(String status) {
        // Convert API status codes to display format
        switch (status) {
            case "1":
                return "P"; // Present
            case "2":
                return "A"; // Absent
            case "3":
                return "H"; // Half Leave
            case "4":
                return "F"; // Full Leave
            case "5":
                return "L"; // Late
            default:
                return "-";  // Unknown/No data
        }
    }
    
    private String formatDateForDisplay(String dateStr) {
        try {
            // Parse the date and format it for display (day and month only)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + dateStr, e);
            return dateStr; // Return original if formatting fails
        }
    }
    
    private boolean isDateSunday(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            return dayOfWeek == Calendar.SUNDAY;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date for Sunday check: " + dateStr, e);
            return false;
        }
    }

    private void createEmptyRowsWithLocalSubjects() {
        // Generate month dates first if not already generated
        if (dateList.isEmpty()) {
            Log.d(TAG, "createEmptyRowsWithLocalSubjects - Generating month dates first");
            generateMonthDates();
        }
        
        // Use the same sorted subjects list as header
        getSortedSubjectsList();
        
        Log.d(TAG, "createEmptyRowsWithLocalSubjects - Creating empty rows with sorted subjects: " + sortedSubjectsList);
        
        // Header row is already created by parseAttendanceResponse(), so we don't need to create it again
        // Create empty rows for each date
        for (int i = 0; i < dateList.size(); i++) {
            TableRow row = new TableRow(context);
            row.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setMinimumHeight(80);
            
            // Date cell (reduced weight to accommodate more subjects) - use theme color
            String formattedDate = formatDateForDisplay(dateList.get(i));
            String userType = Paper.book().read(Constants.User_Type, "");
            String dateCellColor = (userType != null && userType.equals("STUDENT")) ? 
                "#004d40" : "#8B4513"; // Teal for student, dark brown for parent
            TextView dateCell = createCell(formattedDate, dateCellColor, android.graphics.Color.WHITE, 0.1f);
            row.addView(dateCell);
            
            // Check if this date is a Sunday
            boolean isSunday = isDateSunday(dateList.get(i));
            
            // Calculate weight per subject (same as header)
            float subjectWeight = (1.0f - 0.1f) / sortedSubjectsList.size(); // 0.1f is for Date column
            
            // Empty attendance cells for each subject
            for (int j = 0; j < sortedSubjectsList.size(); j++) {
                String status;
                if (isSunday) {
                    status = "Sunday"; // Show "Sunday" for Sundays
                } else {
                    status = "P"; // Show "P" for other days (temporary until API is fixed)
                }
                
                String bgColor;
                int textColor;
                if (status.equals("Sunday")) {
                    // Use theme color for Sunday - dark brown for parent, teal for student
                    bgColor = (userType != null && userType.equals("STUDENT")) ? 
                        "#004d40" : "#8B4513"; // Teal for student, dark brown for parent
                    textColor = android.graphics.Color.WHITE;
                } else {
                    bgColor = "#FFFFFF"; // White for empty
                    textColor = android.graphics.Color.BLACK;
                }
                
                TextView cell = createCell(status, bgColor, textColor, subjectWeight);
                row.addView(cell);
            }
            
            attendance_table.addView(row);
        }
        
        // Make sure the attendance table is visible
        attendance_table.setVisibility(View.VISIBLE);
        Log.d(TAG, "createEmptyRowsWithLocalSubjects - Added " + dateList.size() + " rows to attendance_table");
        Log.d(TAG, "createEmptyRowsWithLocalSubjects - attendance_table visibility: " + attendance_table.getVisibility());
    }

    private void setupEmptyTable() {
        // Clear all existing data rows
        attendance_table.removeAllViews();
        
        // Generate month dates locally
        generateMonthDates();
        
        // Add a message row
        TableRow messageRow = new TableRow(context);
        messageRow.setLayoutParams(new TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        ));
        messageRow.setMinimumHeight(80);
        
        // Create a cell that spans all columns
        TextView messageCell = new TextView(context);
        messageCell.setText("No attendance data available for this month. The student may not have any subjects assigned or no attendance records exist for this period.");
        messageCell.setTextColor(android.graphics.Color.BLACK);
        messageCell.setTextSize(14);
        messageCell.setGravity(android.view.Gravity.CENTER);
        messageCell.setBackgroundColor(android.graphics.Color.LTGRAY);
        messageCell.setPadding(16, 16, 16, 16);
        
        // Make it span all 5 columns (Date + 4 subjects)
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, 80, 5.0f);
        messageCell.setLayoutParams(params);
        
        messageRow.addView(messageCell);
        attendance_table.addView(messageRow);
    }
    
    private void generateMonthDates() {
        // Generate all dates for the selected month
        if (month_format == null || month_format.isEmpty()) {
            Log.w(TAG, "No month selected, using current month");
            Calendar calendar = Calendar.getInstance();
            month_format = String.format(Locale.getDefault(), "%04d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        }
        
        try {
            // Parse month format (e.g., "2025-04" or "4/2025")
            String[] parts = month_format.split("[/-]");
            int year, month;
            
            if (parts.length == 2) {
                if (month_format.contains("/")) {
                    // Format: "4/2025"
                    month = Integer.parseInt(parts[0]);
                    year = Integer.parseInt(parts[1]);
                } else {
                    // Format: "2025-04"
                    year = Integer.parseInt(parts[0]);
                    month = Integer.parseInt(parts[1]);
                }
            } else {
                // Fallback to current month
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH) + 1;
            }
            
            Log.d(TAG, "Generating dates for year: " + year + ", month: " + month);
            
            // Clear existing date list
            dateList.clear();
            
            // Generate all dates in the month
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, 1); // month is 0-based in Calendar
            
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            for (int day = 1; day <= daysInMonth; day++) {
                calendar.set(year, month - 1, day);
                
                // Include all days (including Sundays) - we'll show "OFF" for Sundays in the display
                // No need to skip any days here
                
                // Format date as "yyyy-MM-dd"
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dateStr = dateFormat.format(calendar.getTime());
                dateList.add(dateStr);
            }
            
            Log.d(TAG, "Generated " + dateList.size() + " working days for month " + month + "/" + year);
            
            // Don't create sample data here - let the API response handle the data
            // createSampleAttendanceData(); // Removed - this was overriding real data
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating month dates", e);
            // Fallback to current month
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateList.add(dateFormat.format(calendar.getTime()));
        }
    }
    
    private TextView createCell(String text, String bgColor, int textColor, float weight) {
        TextView cell = new TextView(context);
        cell.setText(text);
        cell.setTextSize(16); // Increased from 14 to 16 for better visibility
        cell.setGravity(android.view.Gravity.CENTER);
        
        // Use theme colors directly for date/Sunday cells, drawables for status cells
        if (bgColor.equals("#8B4513") || bgColor.equals("#004d40")) {
            // Dark brown for parent theme or teal for student theme - set color directly
            cell.setBackgroundColor(android.graphics.Color.parseColor(bgColor));
        } else if (bgColor.equals("#DF4242")) {
            cell.setBackgroundResource(R.drawable.attendance_absent_background);
        } else {
            cell.setBackgroundResource(R.drawable.attendance_present_background);
        }
        
        cell.setTextColor(textColor);
        cell.setMinHeight(80); // Increased to 80 for data rows only
        cell.setMaxHeight(80); // Increased to 80 for data rows only
        cell.setPadding(4, 4, 4, 4); // Added small padding for better text spacing
        cell.setIncludeFontPadding(false);
        
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, 80, weight); // Use custom weight
        cell.setLayoutParams(params);
        
        return cell;
    }

    @Override
    public void onItemClick(View view, int position) {
        // Handle item clicks if needed
    }

    /**
     * Apply theme based on user type
     */
    private void applyTheme(String userType) {
        try {
            Log.d(TAG, "Applying theme for userType: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    int tealColor = ContextCompat.getColor(this, R.color.student_primary);
                    getWindow().setStatusBarColor(tealColor);
                    getWindow().setNavigationBarColor(tealColor);
                }
                
                // Force light status bar icons for better visibility on teal background
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(
                        getWindow().getDecorView().getSystemUiVisibility() & 
                        ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    );
                }
                
                Log.d(TAG, "Applied STUDENT theme (teal)");
            } else {
                // Apply unified parent theme for attendance subject wise page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for attendance
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for attendance
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Subject Wise Attendance");
                
                Log.d(TAG, "Applied PARENT theme (brown)");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme, using parent theme as fallback", e);
            // Fallback to parent theme
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int brownColor = ContextCompat.getColor(this, R.color.dark_brown);
                getWindow().setStatusBarColor(brownColor);
                getWindow().setNavigationBarColor(brownColor);
            }
        }
    }

    /**
     * Apply comprehensive theme to all UI elements
     */
    private void applyComprehensiveTheme(String userType) {
        try {
            Log.d(TAG, "Applying comprehensive theme for user type: " + userType);
            
            // Get theme colors
            int primaryColor;
            
            if (userType != null && userType.equals("STUDENT")) {
                // Student theme (teal)
                primaryColor = ContextCompat.getColor(this, R.color.student_primary);
                Log.d(TAG, "Applying STUDENT theme (teal)");
            } else {
                // Parent theme (dark brown) - default
                primaryColor = ContextCompat.getColor(this, R.color.dark_brown);
                Log.d(TAG, "Applying PARENT theme (dark brown)");
            }
            
            // Apply header background
            LinearLayout header = findViewById(R.id.header);
            if (header != null) {
                header.setBackgroundColor(primaryColor);
                Log.d(TAG, "Header background applied with color: " + primaryColor);
            }
            
            // Apply status bar color
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(primaryColor);
            }
            
            // Apply theme to table header after layout is loaded
            View headerTable = findViewById(R.id.header_table);
            if (headerTable != null) {
                headerTable.post(() -> applyTableHeaderTheme(userType));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying comprehensive theme", e);
        }
    }
    
    /**
     * Apply theme to table header
     */
    private void applyTableHeaderTheme(String userType) {
        try {
            Log.d(TAG, "Applying table header theme for user type: " + userType);
            
            // Get theme colors
            int headerColor;
            if (userType != null && userType.equals("STUDENT")) {
                headerColor = R.color.student_primary;
                Log.d(TAG, "Applying STUDENT theme (teal) to table header");
            } else {
                headerColor = R.color.dark_brown;
                Log.d(TAG, "Applying PARENT theme (dark brown) to table header");
            }
            
            // Find the table header and apply theme
            View headerTable = findViewById(R.id.header_table);
            if (headerTable != null) {
                // Apply background color to the table header - force override XML
                headerTable.setBackground(null);
                headerTable.setBackgroundColor(ContextCompat.getColor(this, headerColor));
                
                // Apply theme to all TextViews in the table header
                applyThemeToChildren(headerTable, headerColor);
                
                Log.d(TAG, "Table header theme applied successfully");
            } else {
                Log.e(TAG, "Table header view not found");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying table header theme", e);
        }
    }
    
    /**
     * Apply theme to children views
     */
    private void applyThemeToChildren(View parent, int colorRes) {
        try {
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View child = viewGroup.getChildAt(i);
                    if (child instanceof TextView) {
                        TextView textView = (TextView) child;
                        // Force override XML background by setting background to null first, then applying color
                        textView.setBackground(null);
                        textView.setBackgroundColor(ContextCompat.getColor(this, colorRes));
                    } else if (child instanceof ViewGroup) {
                        applyThemeToChildren(child, colorRes);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme to children", e);
        }
    }
}