package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.StaffAttendanceSubmitAdapter;
import topgrade.parent.com.parentseeks.Teacher.Interface.AttendanceSubmitInterface;
import topgrade.parent.com.parentseeks.Teacher.Interface.SmsCheck;
import topgrade.parent.com.parentseeks.Teacher.Model.API.AttendanceSubmt_;
import topgrade.parent.com.parentseeks.Teacher.Model.AttendanceSubmitModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListSigel;
import topgrade.parent.com.parentseeks.Teacher.Model.Teach;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Model.teacher_load_profile.TeacherProfileData;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffAttendanceSubmitClass extends AppCompatActivity
        implements AttendanceSubmitInterface, View.OnClickListener, SmsCheck, CompoundButton.OnCheckedChangeListener {

    // ==================== CONSTANTS ====================
    private static final String TAG = "StaffAttendanceSubmitClass";
    private static final String DATE_FORMAT_DISPLAY = "dd MMMM, yyyy";
    private static final String DATE_FORMAT_API = "yyyy-MM-dd";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_BASE = 1000L; // 1 second base delay
    
    // Attendance status codes
    private static final String ATTENDANCE_PRESENT = "1";
    private static final String ATTENDANCE_ABSENT = "2";
    private static final String ATTENDANCE_HALF_LEAVE = "3";
    private static final String ATTENDANCE_FULL_LEAVE = "4";
    
    // Colors for attendance status
    private static final String COLOR_PRESENT_BG = "#ffffff";
    private static final String COLOR_PRESENT_TEXT = "#000000";
    private static final String COLOR_ABSENT_BG = "#DF4242";
    private static final String COLOR_ABSENT_TEXT = "#ffffff";
    private static final String COLOR_LEAVE_BG = "#90ee90";
    private static final String COLOR_LEAVE_TEXT = "#000000";

    // ==================== UI COMPONENTS ====================
    private RecyclerView recyclerView;
    private TextView totalRecords;
    private ProgressBar progressBar;
    private Spinner classSpinner;
    private TextView attendanceDate;
    private LinearLayout attendanceDateLayout;
    private ImageView backIcon;
    private Button submitAttendance;
    private MaterialCheckBox smsCheck;

    // ==================== DATA ====================
    private List<Teach> classInchargesList = new ArrayList<>();
    private List<String> classList = new ArrayList<>();
    private List<AttendanceSubmitModel> attendanceSubmitModels = new ArrayList<>();
    private String selectedClassId;
    private String attendanceDateString = "";
    private final Calendar calendar = Calendar.getInstance();
    private final HashMap<String, Object> postParams = new HashMap<>();

    // ==================== CONTEXT ====================
    private Context context;

    // ==================== DATE PICKER LISTENER ====================
    private final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateDisplay();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initializeUI();
        setupSystemBars();
        initializeComponents();
        setupClickListeners();
        initializeData();
        loadClasses();
    }

    // ==================== INITIALIZATION METHODS ====================
    
    private void initializeUI() {
        setContentView(R.layout.activity_staff_attendance_submit_class);
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }
        
    private void setupSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.navy_blue));
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0,
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | 
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        setupWindowInsets();
    }

    private void initializeComponents() {
        context = this;
        Paper.init(context);
        Constant.loadFromPaper();
        
        // Initialize UI components
        totalRecords = findViewById(R.id.total_records);
        attendanceDateLayout = findViewById(R.id.attendance_date);
        attendanceDate = findViewById(R.id.attendence_date_text);
        submitAttendance = findViewById(R.id.submit_attendence);
        backIcon = findViewById(R.id.back_icon);
        recyclerView = findViewById(R.id.attendence_rcv);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        
        // Initialize other components
        progressBar = findViewById(R.id.progress_bar);
        classSpinner = findViewById(R.id.classs_spinner);
        // Standard Spinner doesn't need setTitle
        smsCheck = findViewById(R.id.sms_header_checkbox);
        smsCheck.setChecked(true);
        smsCheck.setOnCheckedChangeListener(this);

        // Set click listeners
        backIcon.setOnClickListener(this);
        submitAttendance.setOnClickListener(this);
        attendanceDateLayout.setOnClickListener(this);
        
        // Set initial date
        updateDateDisplay();
        
        android.util.Log.d(TAG, "Staff ID: " + Constant.staff_id);
        android.util.Log.d(TAG, "Campus ID: " + Constant.campus_id);
        android.util.Log.d(TAG, "Session ID: " + Constant.current_session);
    }

    private void setupClickListeners() {
        // Click listeners are set in initializeComponents()
    }

    private void initializeData() {
        // Data initialization is handled in loadClasses()
    }

    private void loadClasses() {
        Load_Class();
    }

    private void updateDateDisplay() {
        SimpleDateFormat displayFormat = new SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault());
        SimpleDateFormat apiFormat = new SimpleDateFormat(DATE_FORMAT_API, Locale.getDefault());
        
        attendanceDate.setText(displayFormat.format(calendar.getTime()));
        attendanceDateString = apiFormat.format(calendar.getTime());
    }

    private void Load_Class() {
        if (!validateRequiredConstants()) {
            return;
        }

        clearPreviousData();
        showLoading(true);
        
        HashMap<String, String> requestParams = new HashMap<>();
        requestParams.put("staff_id", Constant.staff_id);
        requestParams.put("parent_id", Constant.campus_id);
        requestParams.put("session_id", Constant.current_session);
        
        android.util.Log.d(TAG, "Request parameters:");
        android.util.Log.d(TAG, "  staff_id: " + Constant.staff_id);
        android.util.Log.d(TAG, "  campus_id (parent_id): " + Constant.campus_id);
        android.util.Log.d(TAG, "  session_id: " + Constant.current_session);
        
        RequestBody body = RequestBody.create(
            new JSONObject(requestParams).toString(),
            MediaType.parse("application/json; charset=utf-8")
        );
        
        android.util.Log.d(TAG, "Load_Class request: " + new JSONObject(requestParams).toString());
        loadClassWithRetry(body, 0);
    }

    private void clearPreviousData() {
        classList.clear();
        classInchargesList.clear();
    }

    private boolean validateRequiredConstants() {
        if (Constant.staff_id == null || Constant.staff_id.isEmpty()) {
            showErrorDialog("Configuration Error", "Staff ID not found. Please login again.");
            return false;
        }
        
        if (Constant.campus_id == null || Constant.campus_id.isEmpty()) {
            showErrorDialog("Configuration Error", "Campus ID not found. Please login again.");
            return false;
        }
        
        if (Constant.current_session == null || Constant.current_session.isEmpty()) {
            showErrorDialog("Configuration Error", "Session not found. Please login again.");
            return false;
        }
        
        return true;
    }

    private void loadClassWithRetry(RequestBody body, int retryCount) {
        Constant.mApiService.load_profile(body).enqueue(new Callback<TeachModel>() {
            @Override
            public void onResponse(Call<TeachModel> call, Response<TeachModel> response) {
                showLoading(false);
                
                if (response.body() != null) {
                    handleClassLoadSuccess(response.body());
                } else {
                    handleClassLoadError("Empty response from server", retryCount, body);
                }
            }

            @Override
            public void onFailure(Call<TeachModel> call, Throwable e) {
                showLoading(false);
                handleClassLoadError(e.getMessage(), retryCount, body);
            }
        });
    }

    private void handleClassLoadSuccess(TeachModel response) {
        if (response.getStatus().getCode().equals("1000")) {
            List<Teach> teachList = response.getTeach();
            TeacherProfileData data = response.getData();
            
            android.util.Log.d(TAG, "Teach list size: " + (teachList != null ? teachList.size() : "null"));
            android.util.Log.d(TAG, "TeacherProfileData: " + (data != null ? "present" : "null"));
            
            // Use the working code logic: get class from TeacherProfileData
            if (data != null && data.getStudentClassId() != null && !data.getStudentClassId().equals("") && 
                data.getStudentClassName() != null && !data.getStudentClassName().equals("")) {
                
                android.util.Log.d(TAG, "Found class from TeacherProfileData: " + data.getStudentClassName() + 
                                 " (ID: " + data.getStudentClassId() + ")");
                
                // Add the single class to the list
                classList.clear();
                classList.add(data.getStudentClassName());
                selectedClassId = data.getStudentClassId();
                
                // Set up spinner with single class
                setupClassSpinnerWithSingleClass();
                
            } else {
                android.util.Log.d(TAG, "No valid class found in TeacherProfileData");
                showNoClassesDialog();
            }
        } else {
            showErrorDialog("Error", response.getStatus().getMessage());
        }
    }



    private void setupClassSpinnerWithSingleClass() {
        android.util.Log.d(TAG, "Setting up spinner with single class: " + classList.get(0));
        
        // Use the working pattern from StaffStudentList.java
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            context, 
            R.layout.simple_list_item_1, 
            classList
        );
        
        classSpinner.setAdapter(adapter);
        
        // Set item selection listener
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                android.util.Log.d(TAG, "Class selected: " + classList.get(position) + " ID: " + selectedClassId);
                
                clearAttendanceData();
                loadStudent();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                android.util.Log.d(TAG, "No class selected");
            }
        });
        
        // Auto-select the single class to trigger student loading
        if (classList.size() > 0) {
            android.util.Log.d(TAG, "Auto-selecting single class: " + classList.get(0));
            classSpinner.setSelection(0);
            
            // Manually trigger student loading since setSelection might not trigger onItemSelected immediately
            android.util.Log.d(TAG, "Manually loading students for auto-selected class: " + selectedClassId);
            clearAttendanceData();
            loadStudent();
        }
    }

    private void setupClassSpinnerWithMultipleClasses(Map<String, String> classNameToIdMap) {
        android.util.Log.d(TAG, "Setting up spinner with " + classList.size() + " classes");
        for (int i = 0; i < classList.size(); i++) {
            android.util.Log.d(TAG, "Class " + i + ": " + classList.get(i) + " -> ID: " + classNameToIdMap.get(classList.get(i)));
        }
        
        // Use the working pattern from StaffStudentList.java
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            context, 
            R.layout.simple_list_item_1, 
            classList
        );
        
        classSpinner.setAdapter(adapter);
        
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedClassName = classList.get(position);
                selectedClassId = classNameToIdMap.get(selectedClassName);
                
                android.util.Log.d(TAG, "Class selected: " + selectedClassName + " ID: " + selectedClassId);
                
                clearAttendanceData();
                                                loadStudent();
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {
                android.util.Log.d(TAG, "No class selected");
            }
        });
        
        // Auto-select first class to trigger student loading
        if (classList.size() > 0) {
            android.util.Log.d(TAG, "Auto-selecting first class from list size: " + classList.size());
            classSpinner.setSelection(0);
        }
    }

    private void clearAttendanceData() {
        attendanceSubmitModels.clear();
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (totalRecords != null) {
            totalRecords.setText("Total Records: 0");
        }
    }

    private void handleClassLoadError(String errorMessage, int retryCount, RequestBody body) {
        android.util.Log.e(TAG, "Class load error (attempt " + (retryCount + 1) + "): " + errorMessage);
        
        // Keep retrying without showing retry dialog - just continue loading
        if (retryCount < MAX_RETRY_ATTEMPTS * 2) { // Double the retry attempts
            // Retry with exponential backoff but shorter delays
            long delay = (long) Math.pow(1.5, retryCount) * RETRY_DELAY_BASE; // Reduced exponential growth
            
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                showLoading(true);
                loadClassWithRetry(body, retryCount + 1);
            }, delay);
        } else {
            // After many attempts, just keep loading without showing dialog
            showLoading(true);
            android.util.Log.w(TAG, "Maximum retry attempts reached, continuing to load...");
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (classSpinner != null) {
            classSpinner.setEnabled(!show);
        }
        if (submitAttendance != null) {
            submitAttendance.setEnabled(!show);
        }
    }

    private void showNoClassesDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("No Classes Found")
            .setMessage("You are not assigned as class incharge for any class. Please contact administrator.")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    private void showErrorDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private void showRetryDialog(String title, String message, Runnable retryAction) {
        // DISABLED: No retry dialog - just continue loading automatically
        android.util.Log.w(TAG, "Retry dialog disabled - continuing automatic loading: " + message);
        
        // Automatically retry after a short delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            showLoading(true);
            retryAction.run();
        }, 2000); // 2 second delay before auto-retry
    }

    private void loadStudent() {
        android.util.Log.d(TAG, "loadStudent() called with class_id: " + selectedClassId);
        
        if (selectedClassId == null || selectedClassId.isEmpty()) {
            android.util.Log.d(TAG, "class_id is null or empty, cannot load students");
            Toast.makeText(context, "Please select a class first.", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, String> requestParams = new HashMap<>();
        requestParams.put("staff_id", Constant.staff_id);
        requestParams.put("parent_id", Constant.campus_id);
        requestParams.put("student_class_id", selectedClassId);
        requestParams.put("subject_id", "");
        
        showLoading(true);
        
        RequestBody body = RequestBody.create(
            new JSONObject(requestParams).toString(), 
            MediaType.parse("application/json; charset=utf-8")
        );
        
        android.util.Log.d(TAG, "loadStudent request: " + new JSONObject(requestParams).toString());

        Constant.mApiService.load_students(body).enqueue(new Callback<StudentListModel>() {
            @Override
            public void onResponse(Call<StudentListModel> call, Response<StudentListModel> response) {
                showLoading(false);

                if (response.body() != null) {
                    handleStudentLoadSuccess(response.body());
                } else {
                    handleStudentLoadError("Empty response from server");
                }
            }

                            @Override
            public void onFailure(Call<StudentListModel> call, Throwable e) {
                showLoading(false);
                handleStudentLoadError("Network error: " + e.getMessage());
                            }
                        });
                    }

    private void handleStudentLoadSuccess(StudentListModel response) {
        if (response.getStatus().getCode().equals("1000")) {
            List<StudentListSigel> students = response.getStudents();
            android.util.Log.d(TAG, "Students list size: " + (students != null ? students.size() : "null"));

            if (students != null && !students.isEmpty()) {
                setupStudentList(students);
                } else {
                showNoStudentsMessage();
            }
        } else {
            String errorMessage = response.getStatus().getMessage();
                            Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
    }

    private void setupStudentList(List<StudentListSigel> students) {
        attendanceSubmitModels.clear();
        String smsStatus = (smsCheck != null && smsCheck.isChecked()) ? ATTENDANCE_PRESENT : "0";
        
        for (StudentListSigel student : students) {
            attendanceSubmitModels.add(new AttendanceSubmitModel(
                ATTENDANCE_PRESENT, // Default to present
                "",
                student.getUniqueId(),
                student.getSectionId(),
                student.getParent_id(),
                smsStatus
            ));
        }
        
        if (totalRecords != null) {
            totalRecords.setText("Total Records: " + students.size());
        }
        
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new StaffAttendanceSubmitAdapter(
                context,
                students,
                StaffAttendanceSubmitClass.this,
                attendanceSubmitModels, 
                StaffAttendanceSubmitClass.this
            ));
        }
    }

    private void showNoStudentsMessage() {
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (totalRecords != null) {
            totalRecords.setText("Total Records: 0");
        }
        Toast.makeText(context, "No students found for this class.", Toast.LENGTH_SHORT).show();
    }

    private void handleStudentLoadError(String errorMessage) {
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.back_icon) {
            finish();
        } else if (id == R.id.submit_attendence) {
            handleSubmitAttendance();
        } else if (id == R.id.attendance_date) {
            showDatePicker();
        }
    }

    private void handleSubmitAttendance() {
        if (attendanceSubmitModels.isEmpty()) {
                Toast.makeText(context, "No students available for attendance", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAllAttendanceMarked()) {
            submitAttendance();
                    } else {
            Toast.makeText(context, "Please mark attendance for all students", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isAllAttendanceMarked() {
        for (AttendanceSubmitModel model : attendanceSubmitModels) {
            if (model.getAttendance().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void showDatePicker() {
            new DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        }

    private void submitAttendance() {
        JSONArray attendanceArray = createAttendanceJsonArray();
        if (attendanceArray == null) {
            Toast.makeText(context, "Error preparing attendance data", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("operation", "submit_attendence");
        requestParams.put("campus_id", Constant.campus_id);
        requestParams.put("student_class_id", selectedClassId);
        requestParams.put("subject_id", "");
        requestParams.put("session_id", Constant.current_session);
        requestParams.put("created_date", attendanceDateString);
        requestParams.put("attendances", attendanceArray);

        android.util.Log.d(TAG, "Submit attendance request: " + new JSONObject(requestParams).toString());
        
        showLoading(true);
        
        RequestBody body = RequestBody.create(
            new JSONObject(requestParams).toString(), 
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Constant.mApiService.attendence_student_full(body).enqueue(new Callback<AttendanceSubmt_>() {
            @Override
            public void onResponse(Call<AttendanceSubmt_> call, Response<AttendanceSubmt_> response) {
                showLoading(false);
                
                if (response.body() != null) {
                    handleAttendanceSubmitResponse(response.body());
                    } else {
                    Toast.makeText(context, "Empty response from server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AttendanceSubmt_> call, Throwable e) {
                showLoading(false);
                android.util.Log.e(TAG, "Attendance submission failed", e);
                Toast.makeText(context, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private JSONArray createAttendanceJsonArray() {
        try {
            Gson gson = new Gson();
            String jsonString = gson.toJson(
                attendanceSubmitModels,
                new TypeToken<ArrayList<AttendanceSubmitModel>>() {}.getType()
            );
            return new JSONArray(jsonString);
        } catch (JSONException e) {
            android.util.Log.e(TAG, "Error creating attendance JSON array", e);
            return null;
        }
    }

    private void handleAttendanceSubmitResponse(AttendanceSubmt_ response) {
        if (response.getStatus().getCode().equals("1000")) {
            Toast.makeText(context, response.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
            // Optionally finish activity or show success dialog
        } else {
            Toast.makeText(context, response.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void StatusSubmit(View view, final int position, String attendance,
                             final StaffAttendanceSubmitAdapter.AttendanceSubmitViewHolder holder) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.attendence_menu_class, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            String attendanceText = item.getTitle().toString();
            String attendanceCode = getAttendanceCode(item.getItemId());
            
            if (attendanceCode != null) {
                updateAttendanceStatus(holder, attendanceText, attendanceCode, position);
            }
                return true;
        });

        popup.show();
    }

    private String getAttendanceCode(int itemId) {
        if (itemId == R.id.Present) {
            return ATTENDANCE_PRESENT;
        } else if (itemId == R.id.Absent) {
            return ATTENDANCE_ABSENT;
        } else if (itemId == R.id.Hlev) {
            return ATTENDANCE_HALF_LEAVE;
        } else if (itemId == R.id.Flev) {
            return ATTENDANCE_FULL_LEAVE;
        } else {
            return null;
        }
    }

    private void updateAttendanceStatus(StaffAttendanceSubmitAdapter.AttendanceSubmitViewHolder holder, 
                                      String attendanceText, String attendanceCode, int position) {
        // Update UI colors based on attendance status
        if (attendanceCode.equals(ATTENDANCE_PRESENT)) {
            holder.attendence_status.setBackgroundColor(Color.parseColor(COLOR_PRESENT_BG));
            holder.attendence_status.setTextColor(Color.parseColor(COLOR_PRESENT_TEXT));
        } else if (attendanceCode.equals(ATTENDANCE_ABSENT)) {
            holder.attendence_status.setBackgroundColor(Color.parseColor(COLOR_ABSENT_BG));
            holder.attendence_status.setTextColor(Color.parseColor(COLOR_ABSENT_TEXT));
        } else if (attendanceCode.equals(ATTENDANCE_HALF_LEAVE) || attendanceCode.equals(ATTENDANCE_FULL_LEAVE)) {
            holder.attendence_status.setBackgroundColor(Color.parseColor(COLOR_LEAVE_BG));
            holder.attendence_status.setTextColor(Color.parseColor(COLOR_LEAVE_TEXT));
        }

        holder.attendence_status.setText(attendanceText);
        
        // Update data model
        AttendanceSubmitModel model = attendanceSubmitModels.get(position);
        model.setAttendance(attendanceCode);
        attendanceSubmitModels.set(position, model);
    }


    @Override
    public void NoteSubmit(int position, String note) {
        if (position >= 0 && position < attendanceSubmitModels.size()) {
            AttendanceSubmitModel model = attendanceSubmitModels.get(position);
            model.setNote(note);
            attendanceSubmitModels.set(position, model);
        }
    }

    @Override
    public void OnSmsCheck(boolean isSms, int position) {
        if (position >= 0 && position < attendanceSubmitModels.size()) {
            String status = isSms ? ATTENDANCE_PRESENT : "0";
            AttendanceSubmitModel model = attendanceSubmitModels.get(position);
            model.setSms(status);
            attendanceSubmitModels.set(position, model);
            
            if (smsCheck != null) {
                boolean allChecked = true;
                for (AttendanceSubmitModel submitModel : attendanceSubmitModels) {
                    if (!ATTENDANCE_PRESENT.equals(submitModel.getSms())) {
                        allChecked = false;
                        break;
                    }
                }

                smsCheck.setOnCheckedChangeListener(null);
                smsCheck.setChecked(allChecked);
                smsCheck.setOnCheckedChangeListener(this);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        String status = isChecked ? ATTENDANCE_PRESENT : "0";
        
        for (int i = 0; i < attendanceSubmitModels.size(); i++) {
            AttendanceSubmitModel model = attendanceSubmitModels.get(i);
            model.setSms(status);
            attendanceSubmitModels.set(i, model);
        }

        if (recyclerView != null && recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
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

                        // Add bottom margin to bottom layout to push it above navigation bar
                        LinearLayout bottomLayout = findViewById(R.id.bottom_layout);
                        if (bottomLayout != null) {
                            // Set bottom margin to navigation bar height to ensure buttons are visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) bottomLayout.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 16; // 16dp extra padding
                                bottomLayout.setLayoutParams(params);
                            }
                        }

                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        android.util.Log.e("StaffAttendanceSubmitClass", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                android.util.Log.e("StaffAttendanceSubmitClass", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            android.util.Log.e("StaffAttendanceSubmitClass", "Error setting up window insets: " + e.getMessage(), e);
        }
    }
}
