package topgrade.parent.com.parentseeks.Parent.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Utils.ChildListCrashFix;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Parent.Adaptor.DiaryListAdapter;
import topgrade.parent.com.parentseeks.Parent.Model.Diary;
import topgrade.parent.com.parentseeks.Parent.Model.DiaryEntry;
import topgrade.parent.com.parentseeks.Parent.Model.StatusModel;
import topgrade.parent.com.parentseeks.Parent.Utils.ChildListCrashFix;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class ParentViewClassDiary extends AppCompatActivity {

    private static final String TAG = "ParentViewClassDiary";
    
    private Context context;
    private ProgressBar progressBar;
    private DiaryListAdapter adapter;
    private final List<DiaryEntry> diaryList = new ArrayList<>();
    private String selectedChildId = "";
    private SharedStudent selectedStudent = null; // Store selected student to access section name
    private String selectedDate = "";
    private String selectedEndDate = "";
    private Calendar calendar;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    private AutoCompleteTextView select_child_spinner;
    private TextInputEditText start_date;
    private TextInputEditText end_date;
    private ArrayAdapter<String> child_adapter;
    private final List<String> student_name_list = new ArrayList<>();
    private List<SharedStudent> studentList = new ArrayList<>();
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_parent_view_class_diary);
        
        // Configure status bar for dark brown background with white icons
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (getWindow().getInsetsController() != null) {
            getWindow().getInsetsController().setSystemBarsAppearance(
                0,
                android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | 
                android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            );
            }
        }
        
        setupWindowInsets();

        context = this;
        Paper.init(context);
        
        calendar = Calendar.getInstance();
        // Set default date to today
        selectedDate = dateFormat.format(calendar.getTime());
        
        // Initialize views
        ImageView backIcon = findViewById(R.id.back_icon);
        if (backIcon != null) {
            backIcon.setOnClickListener(v -> finish());
        }
        
        progressBar = findViewById(R.id.progress_bar);
        RecyclerView diaryRecyclerView = findViewById(R.id.diary_rcv);
        diaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new DiaryListAdapter(diaryList, context);
        diaryRecyclerView.setAdapter(adapter);
        
        // Initialize filter views
        select_child_spinner = findViewById(R.id.select_child_spinner);
        start_date = findViewById(R.id.start_date);
        end_date = findViewById(R.id.end_date);
        
        // Set default date to today for both from and to
        selectedDate = dateFormat.format(calendar.getTime());
        selectedEndDate = dateFormat.format(calendar.getTime()); // Initialize end date too
        if (start_date != null) {
            start_date.setText("From: " + displayDateFormat.format(calendar.getTime()));
            start_date.setOnClickListener(v -> showDatePicker(true));
        }
        if (end_date != null) {
            end_date.setText("To: " + displayDateFormat.format(calendar.getTime()));
            end_date.setOnClickListener(v -> showDatePicker(false));
        }
        
        // Load student list
        loadStudentList();
        
        // Setup child spinner
        setupChildSpinner();
        
        // Load diary if child is already selected (single child case)
        if (!selectedChildId.isEmpty()) {
        loadClassDiary();
        }
    }
    
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            
            if (rootLayout != null) {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars()
                        );

                        android.widget.LinearLayout footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            int bottomMargin = Math.max(systemInsets.bottom, 0);
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                            }
                        }

                        view.setPadding(0, 0, 0, 0);
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
        }
    }
    
    private void loadStudentList() {
        try {
            studentList = ChildListCrashFix.safeLoadStudentList(context);
            Log.d(TAG, "Student list loaded: " + studentList.size() + " students");
            
            // Populate student name lists
            student_name_list.clear();
            if (ChildListCrashFix.isStudentListSafe(studentList)) {
                for (SharedStudent student : studentList) {
                    if (student != null && student.getFullName() != null && !student.getFullName().isEmpty()) {
                        String fullName = student.getFullName();
                        student_name_list.add(fullName);
                    }
                }
            }
            
            // Auto-select if only one child, otherwise show popup
            if (ChildListCrashFix.isStudentListSafe(studentList) && studentList.size() == 1) {
                SharedStudent singleStudent = studentList.get(0);
                if (singleStudent != null) {
                    selectedChildId = singleStudent.getUniqueId();
                    selectedStudent = singleStudent; // Store selected student
                    Log.d(TAG, "Auto-selected single student: " + singleStudent.getFullName() + 
                        ", Section: " + (singleStudent.getSectionName() != null ? singleStudent.getSectionName() : "N/A"));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading student list", e);
            studentList = new ArrayList<>();
        }
    }
    
    private void setupChildSpinner() {
        try {
            child_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, student_name_list);
            if (select_child_spinner != null) {
                select_child_spinner.setAdapter(child_adapter);
                
                select_child_spinner.setOnItemClickListener((parent, view, position, id) -> {
                    if (position >= 0 && position < student_name_list.size()) {
                        String selectedStudentName = student_name_list.get(position);
                        
                        // Find the student in the original list by name
                        SharedStudent selectedStudent = null;
                        for (SharedStudent student : studentList) {
                            if (student != null && student.getFullName() != null && 
                                student.getFullName().equals(selectedStudentName)) {
                                selectedStudent = student;
                                break;
                            }
                        }
                        
                        if (selectedStudent != null) {
                            selectedChildId = selectedStudent.getUniqueId();
                            ParentViewClassDiary.this.selectedStudent = selectedStudent; // Store selected student
                            Log.d(TAG, "Selected student: " + selectedStudent.getFullName() + 
                                ", Section: " + (selectedStudent.getSectionName() != null ? selectedStudent.getSectionName() : "N/A"));
                            // Set the selected child name in the spinner
                            select_child_spinner.setText(selectedStudentName, false);
                            loadClassDiary();
                        }
                    }
                });
                
                // If single child, set the text in spinner; if multiple, show popup
                if (student_name_list.size() == 1 && !selectedChildId.isEmpty()) {
                    // Set the selected child name in the spinner for single child
                    SharedStudent singleStudent = studentList.get(0);
                    if (singleStudent != null && singleStudent.getFullName() != null) {
                        select_child_spinner.setText(singleStudent.getFullName(), false);
                    }
                } else if (student_name_list.size() > 1) {
                    // Automatically show the dropdown popup when activity loads (only if multiple children)
                    select_child_spinner.post(() -> {
                        if (select_child_spinner != null && selectedChildId.isEmpty()) {
                            select_child_spinner.showDropDown();
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up child spinner", e);
        }
    }
    
    private void showDatePicker(boolean isStartDate) {
        Log.d(TAG, "=== DATE PICKER DEBUG ===");
        Log.d(TAG, "Opening date picker for: " + (isStartDate ? "START DATE" : "END DATE"));
        Log.d(TAG, "Current selectedDate: " + selectedDate);
        Log.d(TAG, "Current selectedEndDate: " + selectedEndDate);
        
        Calendar dateCalendar = Calendar.getInstance();
        // Always start with current date to avoid 1970 dates
        dateCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        dateCalendar.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
        dateCalendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        
        String initialDate = dateFormat.format(dateCalendar.getTime());
        Log.d(TAG, "Date picker initialized with: " + initialDate);
        
        if (isStartDate && start_date != null && start_date.getText() != null && !start_date.getText().toString().isEmpty()) {
            // Try to parse existing date, but ensure year is current year
            try {
                String existingText = start_date.getText().toString().replace("From: ", "").trim();
                Log.d(TAG, "Parsing existing start date text: " + existingText);
                Calendar parsedCalendar = Calendar.getInstance();
                parsedCalendar.setTime(displayDateFormat.parse(existingText));
                // Use parsed month and day, but ensure year is current year
                dateCalendar.set(Calendar.MONTH, parsedCalendar.get(Calendar.MONTH));
                dateCalendar.set(Calendar.DAY_OF_MONTH, parsedCalendar.get(Calendar.DAY_OF_MONTH));
                dateCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                String parsedDate = dateFormat.format(dateCalendar.getTime());
                Log.d(TAG, "Parsed start date for picker: " + parsedDate);
            } catch (Exception e) {
                // Use current date if parsing fails - already set above
                Log.d(TAG, "Could not parse start date, using today: " + e.getMessage());
            }
        } else if (!isStartDate && end_date != null && end_date.getText() != null && !end_date.getText().toString().isEmpty()) {
            try {
                String existingText = end_date.getText().toString().replace("To: ", "").trim();
                Log.d(TAG, "Parsing existing end date text: " + existingText);
                Calendar parsedCalendar = Calendar.getInstance();
                parsedCalendar.setTime(displayDateFormat.parse(existingText));
                // Use parsed month and day, but ensure year is current year
                dateCalendar.set(Calendar.MONTH, parsedCalendar.get(Calendar.MONTH));
                dateCalendar.set(Calendar.DAY_OF_MONTH, parsedCalendar.get(Calendar.DAY_OF_MONTH));
                dateCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                String parsedDate = dateFormat.format(dateCalendar.getTime());
                Log.d(TAG, "Parsed end date for picker: " + parsedDate);
            } catch (Exception e) {
                // Use current date if parsing fails - already set above
                Log.d(TAG, "Could not parse end date, using today: " + e.getMessage());
            }
        }
        
        String pickerDate = dateFormat.format(dateCalendar.getTime());
        Log.d(TAG, "Date picker will show: " + pickerDate + " (Year: " + dateCalendar.get(Calendar.YEAR) + 
            ", Month: " + dateCalendar.get(Calendar.MONTH) + ", Day: " + dateCalendar.get(Calendar.DAY_OF_MONTH) + ")");
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            context,
            (view, year, month, dayOfMonth) -> {
                dateCalendar.set(Calendar.YEAR, year);
                dateCalendar.set(Calendar.MONTH, month);
                dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                String formattedDate = dateFormat.format(dateCalendar.getTime());
                String displayDate = displayDateFormat.format(dateCalendar.getTime());
                
                if (isStartDate) {
                    String oldSelectedDate = selectedDate;
                    selectedDate = formattedDate;
                    Log.d(TAG, "=== START DATE SELECTED ===");
                    Log.d(TAG, "Old selectedDate: " + oldSelectedDate);
                    Log.d(TAG, "New selectedDate: " + selectedDate);
                    Log.d(TAG, "Display format: " + displayDate);
                    Log.d(TAG, "Year: " + year + ", Month: " + month + ", Day: " + dayOfMonth);
                    if (start_date != null) {
                        start_date.setText("From: " + displayDate);
                        Log.d(TAG, "Updated start_date field text: " + start_date.getText());
                    }
                } else {
                    // For end date, store it and use it as date2 in API
                    String oldSelectedEndDate = selectedEndDate;
                    selectedEndDate = formattedDate;
                    Log.d(TAG, "=== END DATE SELECTED ===");
                    Log.d(TAG, "Old selectedEndDate: " + oldSelectedEndDate);
                    Log.d(TAG, "New selectedEndDate: " + selectedEndDate);
                    Log.d(TAG, "Display format: " + displayDate);
                    Log.d(TAG, "Year: " + year + ", Month: " + month + ", Day: " + dayOfMonth);
                    if (end_date != null) {
                        end_date.setText("To: " + displayDate);
                        Log.d(TAG, "Updated end_date field text: " + end_date.getText());
                    }
                }
                
                // Reload diary data when date changes
                if (!selectedChildId.isEmpty()) {
                    loadClassDiary();
                }
            },
            dateCalendar.get(Calendar.YEAR),
            dateCalendar.get(Calendar.MONTH),
            dateCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void loadClassDiary() {
        if (selectedChildId.isEmpty()) {
            // Show the child selection popup automatically
            if (select_child_spinner != null && !student_name_list.isEmpty()) {
                select_child_spinner.showDropDown();
            } else {
            Toast.makeText(context, "Please select a child first", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        Log.d(TAG, "=== DATE EXTRACTION DEBUG START ===");
        Log.d(TAG, "Current state - selectedDate: " + selectedDate + ", selectedEndDate: " + selectedEndDate);
        if (start_date != null) {
            Log.d(TAG, "start_date field text: " + start_date.getText());
        }
        if (end_date != null) {
            Log.d(TAG, "end_date field text: " + end_date.getText());
        }
        
        // Get start date - prioritize selectedDate (set by date picker), then parse from field, then use today
        String startDateValue = "";
        if (!selectedDate.isEmpty()) {
            // Use the date stored from date picker (already in yyyy-MM-dd format)
            startDateValue = selectedDate;
            Log.d(TAG, "✓ Using selectedDate for start: " + startDateValue);
        } else if (start_date != null && start_date.getText() != null && !start_date.getText().toString().isEmpty()) {
            try {
                String startDateText = start_date.getText().toString().replace("From: ", "").trim();
                Log.d(TAG, "Parsing start date from field: " + startDateText);
                // Parse with display format, but ensure year is current year
                Calendar startCalendar = Calendar.getInstance();
                try {
                    startCalendar.setTime(displayDateFormat.parse(startDateText));
                    // Ensure year is current year (display format doesn't include year)
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    startCalendar.set(Calendar.YEAR, currentYear);
                    startDateValue = dateFormat.format(startCalendar.getTime());
                    // Also update selectedDate so it's tracked
                    selectedDate = startDateValue;
                    Log.d(TAG, "Parsed start date from field: " + startDateValue);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing start date from display format", e);
                    startDateValue = dateFormat.format(Calendar.getInstance().getTime());
                    selectedDate = startDateValue;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing start date", e);
                startDateValue = dateFormat.format(Calendar.getInstance().getTime());
                selectedDate = startDateValue;
            }
        }
        if (startDateValue.isEmpty()) {
            startDateValue = dateFormat.format(Calendar.getInstance().getTime());
            selectedDate = startDateValue; // Track it
            Log.d(TAG, "Using today's date for start: " + startDateValue);
        }
        
        // Get end date - prioritize selectedEndDate (set by date picker), then parse from field, then use today (not start date)
        String endDateValue = "";
        if (!selectedEndDate.isEmpty()) {
            // Use the date stored from date picker (already in yyyy-MM-dd format)
            endDateValue = selectedEndDate;
            Log.d(TAG, "✓ Using selectedEndDate for end: " + endDateValue);
        } else if (end_date != null && end_date.getText() != null && !end_date.getText().toString().isEmpty()) {
            try {
                String endDateText = end_date.getText().toString().replace("To: ", "").trim();
                Log.d(TAG, "Parsing end date from field: " + endDateText);
                Calendar endCalendar = Calendar.getInstance();
                try {
                    endCalendar.setTime(displayDateFormat.parse(endDateText));
                    // Ensure year is current year (display format doesn't include year)
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    endCalendar.set(Calendar.YEAR, currentYear);
                    endDateValue = dateFormat.format(endCalendar.getTime());
                    // Also update selectedEndDate so it's tracked
                    selectedEndDate = endDateValue;
                    Log.d(TAG, "Parsed end date from field: " + endDateValue);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing end date from display format", e);
                    endDateValue = dateFormat.format(Calendar.getInstance().getTime());
                    selectedEndDate = endDateValue;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing end date", e);
                endDateValue = dateFormat.format(Calendar.getInstance().getTime());
                selectedEndDate = endDateValue;
            }
        }
        if (endDateValue.isEmpty()) {
            // If no end date selected, use today (not start date) - both dates should be independent
            endDateValue = dateFormat.format(Calendar.getInstance().getTime());
            selectedEndDate = endDateValue; // Track it
            Log.d(TAG, "Using today's date for end: " + endDateValue);
        }
        
        Log.d(TAG, "=== FINAL DATE VALUES FOR API ===");
        Log.d(TAG, "startDateValue (date parameter): " + startDateValue);
        Log.d(TAG, "endDateValue (date2 parameter): " + endDateValue);
        Log.d(TAG, "selectedDate state: " + selectedDate);
        Log.d(TAG, "selectedEndDate state: " + selectedEndDate);
        Log.d(TAG, "=== DATE EXTRACTION DEBUG END ===");
        
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("parent_parent_id", Constant.parent_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("employee_id", selectedChildId);
        postParam.put("date", startDateValue);
        postParam.put("date2", endDateValue);
        
        // Log the parameters being sent
        Log.d(TAG, "API Request Parameters:");
        Log.d(TAG, "  parent_parent_id: " + Constant.parent_id);
        Log.d(TAG, "  parent_id: " + Constant.campus_id);
        Log.d(TAG, "  employee_id: " + selectedChildId);
        Log.d(TAG, "  date: " + startDateValue);
        Log.d(TAG, "  date2: " + endDateValue);
        
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String requestJson = (new JSONObject(postParam)).toString();
        RequestBody body = RequestBody.create(mediaType, requestJson);
        
        Log.d(TAG, "=== API REQUEST DEBUG ===");
        Log.d(TAG, "Request JSON: " + requestJson);
        Log.d(TAG, "Request URL: load_diary");

        Constant.mApiService.load_diary(body).enqueue(new Callback<Diary>() {
            @Override
            public void onResponse(@NonNull Call<Diary> call, @NonNull Response<Diary> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                Log.d(TAG, "=== API RESPONSE DEBUG START ===");
                Log.d(TAG, "HTTP Response Code: " + response.code());
                Log.d(TAG, "Response isSuccessful: " + response.isSuccessful());
                Log.d(TAG, "Response message: " + response.message());
                Log.d(TAG, "Response headers: " + response.headers());

                if (response.isSuccessful()) {
                    Diary diary = response.body();
                    
                    Log.d(TAG, "=== DESERIALIZED OBJECT DEBUG ===");
                    Log.d(TAG, "Diary object is null: " + (diary == null));
                    
                    if (diary != null) {
                        // Log full Diary object structure
                        try {
                            Gson gson = new Gson();
                            String diaryJson = gson.toJson(diary);
                            Log.d(TAG, "Diary object as JSON (first 1000 chars): " + 
                                (diaryJson.length() > 1000 ? diaryJson.substring(0, 1000) + "..." : diaryJson));
                        } catch (Exception e) {
                            Log.e(TAG, "Error serializing Diary to JSON: " + e.getMessage());
                        }
                    }
                    
                    // Match working code exactly: if (diary != null && diary.getStatus() != null)
                    if (diary != null && diary.getStatus() != null) {
                        StatusModel statusModel = diary.getStatus();
                        
                        Log.d(TAG, "=== STATUS MODEL DEBUG ===");
                        Log.d(TAG, "StatusModel object: " + statusModel);
                        Log.d(TAG, "StatusModel.status field is null: " + (statusModel.status == null));
                            
                            if (statusModel.status != null) {
                            Log.d(TAG, "StatusModel.status object: " + statusModel.status);
                            Log.d(TAG, "StatusModel.status.code: " + statusModel.status.getCode());
                            Log.d(TAG, "StatusModel.status.message: " + statusModel.status.getMessage());
                            } else {
                            Log.e(TAG, "⚠️ CRITICAL: StatusModel.status is NULL - deserialization failed!");
                            // Try to serialize StatusModel to see what Gson got
                            try {
                                Gson gson = new Gson();
                                String statusJson = gson.toJson(statusModel);
                                Log.d(TAG, "StatusModel as JSON: " + statusJson);
                            } catch (Exception e) {
                                Log.e(TAG, "Error serializing StatusModel: " + e.getMessage());
                            }
                        }
                        
                        // Try helper method first
                        String statusCode = statusModel.getCode();
                        String statusMessage = statusModel.getMessage();
                        Log.d(TAG, "Status code from getCode() helper: " + statusCode);
                        Log.d(TAG, "Status message from getMessage() helper: " + statusMessage);
                        
                        // Fallback: Access status.status.code directly (matching Kotlin: diary.status.status.code)
                        if (statusCode == null && statusModel.status != null) {
                            statusCode = statusModel.status.getCode();
                            Log.d(TAG, "✓ Status code from status.status.code (direct): " + statusCode);
                        }
                        if (statusMessage == null && statusModel.status != null) {
                            statusMessage = statusModel.status.getMessage();
                            Log.d(TAG, "✓ Status message from status.status.message (direct): " + statusMessage);
                        }
                        
                        Log.d(TAG, "=== FINAL STATUS VALUES ===");
                        Log.d(TAG, "Final status code: " + statusCode);
                        Log.d(TAG, "Final status message: " + statusMessage);
                        Log.d(TAG, "=== DATA STRUCTURE DEBUG ===");
                        Log.d(TAG, "Title map is null: " + (diary.getTitle() == null));
                        if (diary.getTitle() != null) {
                            Log.d(TAG, "Title map size: " + diary.getTitle().size());
                            Log.d(TAG, "Title map keys: " + diary.getTitle().keySet());
                            // Log each entry in title map
                            for (String key : diary.getTitle().keySet()) {
                                DiaryEntry entry = diary.getTitle().get(key);
                                Log.d(TAG, "  Title[" + key + "]: " + (entry != null ? entry.toString() : "null"));
                            }
                        } else {
                            Log.w(TAG, "⚠️ Title map is NULL");
                        }
                        
                        Log.d(TAG, "Data list is null: " + (diary.getData() == null));
                        boolean hasData = diary.getData() != null && !diary.getData().isEmpty();
                        if (diary.getData() != null) {
                            Log.d(TAG, "Data list size: " + diary.getData().size());
                            // Log first few items in data list
                            for (int i = 0; i < Math.min(diary.getData().size(), 3); i++) {
                                Object item = diary.getData().get(i);
                                Log.d(TAG, "  Data[" + i + "] type: " + (item != null ? item.getClass().getName() : "null"));
                                if (item != null) {
                                    try {
                                        Gson gson = new Gson();
                                        String itemJson = gson.toJson(item);
                                        Log.d(TAG, "  Data[" + i + "] content (first 200 chars): " + 
                                            (itemJson.length() > 200 ? itemJson.substring(0, 200) + "..." : itemJson));
                                    } catch (Exception e) {
                                        Log.d(TAG, "  Data[" + i + "] toString(): " + item.toString());
                                    }
                                }
                            }
                        } else {
                            Log.w(TAG, "⚠️ Data list is NULL");
                        }
                        
                        // Handle case where status is empty but data exists (API returns status:{} but has data)
                        boolean isSuccess = (statusCode != null && statusCode.equals("1000")) || 
                                           (statusCode == null && hasData);
                        
                        if (isSuccess) {
                            if (statusCode == null && hasData) {
                                Log.d(TAG, "✓ Status is empty but data exists - treating as success");
                            }
                            // Clear the list first
                            diaryList.clear();
                            
                            // First, try to get data from title map (if available)
                            if (diary.getTitle() != null && !diary.getTitle().isEmpty()) {
                                Log.d(TAG, "Using title map data. Keys: " + diary.getTitle().keySet());
                                // Add all entries from title map
                                diaryList.addAll(diary.getTitle().values());
                                Log.d(TAG, "Added " + diaryList.size() + " entries from title map");
                            }
                            
                            // Fallback: try data array if title is empty or null
                            if (diaryList.isEmpty() && diary.getData() != null && !diary.getData().isEmpty()) {
                                try {
                                    Log.d(TAG, "Parsing diary data from array (size: " + diary.getData().size() + ")");
                                    Gson gson = new Gson();
                                    
                                    // Process all data entries (each entry represents a date)
                                    // Create ONE class diary entry per date (not individual subject entries)
                                    diaryList.clear();
                                    
                                    for (Object dataItem : diary.getData()) {
                                        JsonElement jsonElement = gson.toJsonTree(dataItem);
                                    JsonObject dataObject = jsonElement.getAsJsonObject();
                                    
                                        Log.d(TAG, "Processing data object with keys: " + dataObject.keySet());
                                        
                                        // Extract common fields from parent object
                                        String className = dataObject.has("class_name") ? 
                                            dataObject.get("class_name").getAsString() : null;
                                        String date = dataObject.has("full_name") ? 
                                            dataObject.get("full_name").getAsString() : null; // full_name contains the date in format "2025-11-20"
                                        
                                        // Get actual student name from selected student
                                        String studentFullName = null;
                                        if (selectedStudent != null && selectedStudent.getFullName() != null) {
                                            studentFullName = selectedStudent.getFullName();
                                        }
                                        
                                        // Get section name from selected student
                                        String sectionName = null;
                                        if (selectedStudent != null && selectedStudent.getSectionName() != null) {
                                            sectionName = selectedStudent.getSectionName();
                                        }
                                        
                                        // Create a single class diary entry for this date
                                        DiaryEntry classDiaryEntry = new DiaryEntry();
                                        classDiaryEntry.setClassName(className);
                                        classDiaryEntry.setFullName(studentFullName); // Set actual student name, not date
                                        classDiaryEntry.setStudentName(studentFullName); // Set actual student name, not date
                                        classDiaryEntry.setDate(date); // Set date separately
                                        classDiaryEntry.setSectionName(sectionName);
                                        
                                        // Get the main body from the data object (if exists)
                                        String mainBody = null;
                                        if (dataObject.has("body") && !dataObject.get("body").isJsonNull()) {
                                            mainBody = dataObject.get("body").getAsString();
                                        }
                                        
                                        // If no main body, try to get from first subject or combine all subject bodies
                                        if (mainBody == null || mainBody.isEmpty()) {
                                            StringBuilder combinedBody = new StringBuilder();
                                            
                                            // Look through numeric keys to find subject bodies
                                            for (String key : dataObject.keySet()) {
                                                try {
                                                    Integer.parseInt(key); // Check if it's a numeric key
                                                    
                                                    JsonElement subjectElement = dataObject.get(key);
                                                    if (subjectElement != null && subjectElement.isJsonObject()) {
                                                        JsonObject subjectObject = subjectElement.getAsJsonObject();
                                                        if (subjectObject.has("body") && !subjectObject.get("body").isJsonNull()) {
                                                            String subjectBody = subjectObject.get("body").getAsString();
                                                            if (subjectBody != null && !subjectBody.trim().isEmpty()) {
                                                                if (combinedBody.length() > 0) {
                                                                    combinedBody.append("\n");
                                                                }
                                                                combinedBody.append(subjectBody);
                                                            }
                                                        }
                                                    }
                                                } catch (NumberFormatException e) {
                                                    // Not a numeric key, skip it
                                                    continue;
                                                }
                                            }
                                            
                                            if (combinedBody.length() > 0) {
                                                mainBody = combinedBody.toString();
                                            }
                                        }
                                        
                                        // Set the body/description
                                        if (mainBody != null && !mainBody.isEmpty()) {
                                            classDiaryEntry.setBody(mainBody);
                                            classDiaryEntry.setDescription(mainBody);
                                        }
                                        
                                        // Extract image URL from data object
                                        String imageUrl = null;
                                        
                                        // Log all available keys for debugging
                                        Log.d(TAG, "Available keys in data object: " + dataObject.keySet());
                                        
                                        // Check various possible image field names
                                        if (dataObject.has("image_url") && !dataObject.get("image_url").isJsonNull()) {
                                            imageUrl = dataObject.get("image_url").getAsString();
                                            Log.d(TAG, "Found image_url in parent: " + imageUrl);
                                        } else if (dataObject.has("picture") && !dataObject.get("picture").isJsonNull()) {
                                            imageUrl = dataObject.get("picture").getAsString();
                                            Log.d(TAG, "Found picture in parent: " + imageUrl);
                                        } else if (dataObject.has("image") && !dataObject.get("image").isJsonNull()) {
                                            imageUrl = dataObject.get("image").getAsString();
                                            Log.d(TAG, "Found image in parent: " + imageUrl);
                                        } else if (dataObject.has("diary_image") && !dataObject.get("diary_image").isJsonNull()) {
                                            imageUrl = dataObject.get("diary_image").getAsString();
                                            Log.d(TAG, "Found diary_image in parent: " + imageUrl);
                                        }
                                        
                                        // If no image in parent, try to get from first subject
                                        if ((imageUrl == null || imageUrl.isEmpty()) && !dataObject.keySet().isEmpty()) {
                                            Log.d(TAG, "No image in parent object, checking subject objects...");
                                            for (String key : dataObject.keySet()) {
                                                try {
                                                    Integer.parseInt(key); // Check if it's a numeric key
                                                    
                                                    JsonElement subjectElement = dataObject.get(key);
                                                    if (subjectElement != null && subjectElement.isJsonObject()) {
                                                        JsonObject subjectObject = subjectElement.getAsJsonObject();
                                                        Log.d(TAG, "Checking subject key " + key + ", available fields: " + subjectObject.keySet());
                                                        
                                                        if (subjectObject.has("image_url") && !subjectObject.get("image_url").isJsonNull()) {
                                                            imageUrl = subjectObject.get("image_url").getAsString();
                                                            Log.d(TAG, "Found image_url in subject " + key + ": " + imageUrl);
                                                            break;
                                                        } else if (subjectObject.has("picture") && !subjectObject.get("picture").isJsonNull()) {
                                                            imageUrl = subjectObject.get("picture").getAsString();
                                                            Log.d(TAG, "Found picture in subject " + key + ": " + imageUrl);
                                                            break;
                                                        } else if (subjectObject.has("image") && !subjectObject.get("image").isJsonNull()) {
                                                            imageUrl = subjectObject.get("image").getAsString();
                                                            Log.d(TAG, "Found image in subject " + key + ": " + imageUrl);
                                                            break;
                                                        } else if (subjectObject.has("diary_image") && !subjectObject.get("diary_image").isJsonNull()) {
                                                            imageUrl = subjectObject.get("diary_image").getAsString();
                                                            Log.d(TAG, "Found diary_image in subject " + key + ": " + imageUrl);
                                                            break;
                                                        }
                                                    }
                                                } catch (NumberFormatException e) {
                                                    continue;
                                                }
                                            }
                                        }
                                        
                                        // Set image URL if found
                                        if (imageUrl != null && !imageUrl.isEmpty()) {
                                            classDiaryEntry.setImageUrl(imageUrl);
                                            classDiaryEntry.setPicture(imageUrl);
                                            Log.d(TAG, "Set image URL in DiaryEntry: " + imageUrl);
                                        } else {
                                            Log.w(TAG, "No image URL found in data object or subjects");
                                        }
                                        
                                        // Add the single class diary entry to the list
                                        diaryList.add(classDiaryEntry);
                                        
                                        Log.d(TAG, "Added class diary entry for date: " + date + 
                                            ", class: " + className + 
                                            ", body length: " + (mainBody != null ? mainBody.length() : 0));
                                    }
                                    
                                    Log.d(TAG, "Total diary entries created: " + diaryList.size());
                                    
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing diary data from array", e);
                                    diaryList.clear();
                                }
                            }
                            
                            if (!diaryList.isEmpty()) {
                                Log.d(TAG, "Adding " + diaryList.size() + " diary entries to adapter");
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "Diary list updated. Size: " + diaryList.size());
                            } else {
                                Log.w(TAG, "No diary entries to display");
                                diaryList.clear();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(context, "No diary data found for the selected date.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Working code: Toast.makeText(context, diary.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Status code is NOT 1000. Code: " + statusCode + ", Message: " + statusMessage);
                            Toast.makeText(context, statusMessage != null ? statusMessage : "Unknown error", Toast.LENGTH_SHORT).show();
                        }
                        } else {
                            Log.e(TAG, "=== ERROR: Diary or StatusModel is NULL ===");
                            Log.e(TAG, "Diary is null: " + (diary == null));
                            if (diary != null) {
                                Log.e(TAG, "StatusModel is null: " + (diary.getStatus() == null));
                            }
                            Toast.makeText(context, "Response body is empty or invalid.", Toast.LENGTH_SHORT).show();
                        }
                } else {
                    Log.e(TAG, "=== ERROR: HTTP Response Not Successful ===");
                    Log.e(TAG, "Response code: " + response.code());
                    Log.e(TAG, "Response message: " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(context, "Response was not successful. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                
                Log.d(TAG, "=== API RESPONSE DEBUG END ===");
            }

            @Override
            public void onFailure(@NonNull Call<Diary> call, @NonNull Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Error loading class diary", t);
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
}

