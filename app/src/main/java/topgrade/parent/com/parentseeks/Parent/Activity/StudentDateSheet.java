package topgrade.parent.com.parentseeks.Parent.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import components.searchablespinnerlibrary.SearchableSpinner;

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
import topgrade.parent.com.parentseeks.Parent.Adaptor.StudentDateSheetAdaptor;
import topgrade.parent.com.parentseeks.Parent.Model.ExamSession;
import topgrade.parent.com.parentseeks.Parent.Model.SessionModel;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Parent.Model.date_sheet.DateSheetData;
import topgrade.parent.com.parentseeks.Parent.Model.date_sheet.DateSheetResponse;
import topgrade.parent.com.parentseeks.Parent.Utils.ShareUtils;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.TimetableSmsModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.Teacher.Utils.Util;
import topgrade.parent.com.parentseeks.Parent.Activity.ChildList;

public class StudentDateSheet extends AppCompatActivity implements View.OnClickListener {

    String staff_id;


    String campusId, parentId;
    ProgressBar progressBar;

    TextView tvTotalRecords, tvStudentName, tvClass, tvHeaderTimeOrSyllabus, debugResponseText;
    android.widget.ScrollView debugResponseScroll;
    Button sendDateSheetInSms;
    android.view.View btnViewDateSheetImages;
    SharedStudent student;
    Context context;
    List<topgrade.parent.com.parentseeks.Parent.Model.date_sheet.Image> dateSheetImages = new ArrayList<>();

    RecyclerView timetableRcv;
    List<DateSheetData> list = new ArrayList<>();
    String dateSheetSms = "";
    SearchableSpinner selectChildSpinner, selectExamSession;

    String selectedChildId = "", selectedStudentClassId = "";
    String selectedChildName = "";
    String selectedExamSession = "";
    String selectedExamSessionName = "";
    boolean dataLoadedSuccessfully = false; // Flag to prevent toast when data loads
    ArrayAdapter<String> childAdaptor, sessionAdaptor;
    LinearLayout sessionLayout;
    List<SharedStudent> studentList = new ArrayList<>();
    List<String> examSessionNameList = new ArrayList<>();
    List<ExamSession> examSessionList = new ArrayList<>();
    
    // Dialog components (for backward compatibility)
    Button dialogSearchFilterBtn;
    ImageView dialogCancelBtn;
    AlertDialog alertDialog;
    View dialogView; // Store dialog view reference for accessing views


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Set edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            
            setContentView(R.layout.activity_student_datesheet);
            
            // Apply student theme (teal) for student user type
            String userType = Paper.book().read(Constants.User_Type, "");
            if ("STUDENT".equals(userType)) {
                // Apply student theme using StudentThemeHelper
                topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.applyStudentTheme(this, 100);
                topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setHeaderIconVisibility(this, false);
                topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setMoreOptionsVisibility(this, false);
                // Footer removed - SMS button is now at the bottom
                topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setFooterVisibility(this, false);
                topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setHeaderTitle(this, "Date Sheet");
            } else {
                // Configure status bar for dark brown background with white icons
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    // Set transparent status bar to allow header wave to cover it
                    getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
                    
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
                
                // For parent theme, don't use ParentThemeHelper as it overwrites navigation bar color
                // System bars are already configured above to match child list
                Log.d("StudentDateSheet", "Parent theme - system bars already configured in onCreate()");
            }
            
            // Setup window insets
            setupWindowInsets();
            
            // Initialize Paper database
            Paper.init(this);
            
            context = StudentDateSheet.this;
            init();
            setupInlineFilters();
            
            // Load exam sessions if student is already available
            if (student != null && campusId != null && !campusId.isEmpty()) {
                selectedChildId = student.getUniqueId();
                selectedChildName = student.getFullName();
                selectedStudentClassId = student.getStudentClassId();
                
                if (selectedChildId != null && !selectedChildId.isEmpty()) {
                    Log.d("StudentDateSheet", "onCreate() - Auto-loading exam sessions for student: " + selectedChildName);
                    // Delay slightly to ensure UI is ready
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        loadExamSession(campusId, selectedChildId);
                    }, 300);
                }
            }
            
            // Initialize back button
            ImageView backIcon = findViewById(R.id.back_button);
            if (backIcon != null) {
                backIcon.setOnClickListener(v -> finish());
            }
            
            // Initialize header title
            TextView headerTitle = findViewById(R.id.header_title);
            if (headerTitle != null) {
                headerTitle.setText(getString(R.string.date_sheet));
            }
            
            // Apply table header theme based on user type
            applyTableHeaderTheme(userType);
            
            if (student != null) {
                String studentName = student.getFullName();
                String className = student.getClassName();
                
                Log.d("StudentDateSheet", "onCreate() - SharedStudent name: " + studentName);
                Log.d("StudentDateSheet", "onCreate() - Class name: " + className);
                
                // Set selected student info for exam session loading
                selectedChildId = student.getUniqueId();
                selectedChildName = student.getFullName();
                selectedStudentClassId = student.getStudentClassId();
                
                // Load exam sessions automatically if student is already selected
                if (campusId != null && !campusId.isEmpty() && selectedChildId != null && !selectedChildId.isEmpty()) {
                    Log.d("StudentDateSheet", "onCreate() - Auto-loading exam sessions for student: " + selectedChildName);
                    loadExamSession(campusId, selectedChildId);
                } else {
                    Log.w("StudentDateSheet", "onCreate() - Cannot auto-load exam sessions - campusId: " + campusId + ", selectedChildId: " + selectedChildId);
                }
            } else {
                Log.d("StudentDateSheet", "onCreate() - SharedStudent is null");
            }
            listeners();
            
            // Inline filters are now set up in setupInlineFilters()
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in onCreate: " + e.getMessage(), e);
            // Show error message to user
            Toast.makeText(this, "Error loading date sheet. Please try again.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity gracefully
        }
    }

    private void updateStudentDisplay() {
        try {
            // First try to get student from current storage
            SharedStudent currentStudent = Paper.book().read("current_child_model");
            if (currentStudent != null) {
                student = currentStudent;
            }
            
            // If no current student, try to get from student list
            if (student == null && studentList != null && !studentList.isEmpty()) {
                student = studentList.get(0);
                Paper.book().write("current_child_model", student);
            }
            
            // Validate and fix student data
            validateAndFixStudentData();
            
            // Update the display with proper null checks
            if (student != null && !isTestData(student)) {
                String studentName = student.getFullName();
                String className = student.getClassName();
                
                // Log the student information for debugging
                Log.d("StudentDateSheet", "SharedStudent Name: " + studentName);
                Log.d("StudentDateSheet", "SharedStudent Class: " + className);
                Log.d("StudentDateSheet", "SharedStudent ID: " + student.getUniqueId());
                
                // Set student name with fallback (only if TextView exists)
                if (tvStudentName != null) {
                    if (studentName != null && !studentName.trim().isEmpty()) {
                        tvStudentName.setText(studentName);
                    } else {
                        tvStudentName.setText("Please select a student");
                    }
                }
                
                // Set class name with fallback (only if TextView exists)
                if (tvClass != null) {
                    if (className != null && !className.trim().isEmpty()) {
                        tvClass.setText("Class: " + className);
                    } else {
                        tvClass.setText("Class: Not Available");
                    }
                }
            } else {
                Log.d("StudentDateSheet", "No valid student found, showing default message");
                if (tvStudentName != null) {
                    tvStudentName.setText("Please select a student");
                }
                if (tvClass != null) {
                    tvClass.setText("Class: Not Available");
                }
            }
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error updating student display", e);
            if (tvStudentName != null) {
                tvStudentName.setText("Error loading student");
            }
            if (tvClass != null) {
                tvClass.setText("Class: Error");
            }
        }
    }

    private void validateAndFixStudentData() {
        try {
            if (student != null) {
                // Log current student data for debugging
                Log.d("StudentDateSheet", "=== VALIDATING STUDENT DATA ===");
                Log.d("StudentDateSheet", "SharedStudent ID: " + student.getUniqueId());
                Log.d("StudentDateSheet", "SharedStudent Full Name: " + student.getFullName());
                Log.d("StudentDateSheet", "SharedStudent Class: " + student.getClassName());
                Log.d("StudentDateSheet", "SharedStudent Class ID: " + student.getStudentClassId());
                
                // Check if student has valid data
                boolean hasValidData = false;
                
                if (student.getUniqueId() != null && !student.getUniqueId().trim().isEmpty()) {
                    hasValidData = true;
                }
                
                if (student.getFullName() != null && !student.getFullName().trim().isEmpty()) {
                    hasValidData = true;
                }
                
                if (student.getStudentClassId() != null && !student.getStudentClassId().trim().isEmpty()) {
                    hasValidData = true;
                }
                
                Log.d("StudentDateSheet", "SharedStudent has valid data: " + hasValidData);
                
                // Check for test data
                if (isTestData(student)) {
                    Log.d("StudentDateSheet", "Detected test data, clearing student");
                    student = null;
                    Paper.book().delete("current_child_model");
                    hasValidData = false;
                }
                
                // If student data is invalid, try to find a better student from the list
                if (!hasValidData && studentList != null && !studentList.isEmpty()) {
                    Log.d("StudentDateSheet", "Current student data is invalid, looking for better student...");
                    
                    for (SharedStudent s : studentList) {
                        if (s != null && !isTestData(s) && 
                            s.getUniqueId() != null && !s.getUniqueId().trim().isEmpty() &&
                            s.getFullName() != null && !s.getFullName().trim().isEmpty()) {
                            
                            Log.d("StudentDateSheet", "Found better student: " + s.getFullName() + " (ID: " + s.getUniqueId() + ")");
                            student = s;
                            Paper.book().write("current_child_model", student);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error validating student data", e);
        }
    }

    private boolean isTestData(SharedStudent student) {
        if (student == null) return true;
        
        String fullName = student.getFullName();
        if (fullName != null) {
            String lowerName = fullName.toLowerCase().trim();
            // Check for common test data patterns
            if (lowerName.contains("test") || 
                lowerName.contains("hello") || 
                lowerName.contains("sms") ||
                lowerName.equals("student") ||
                lowerName.isEmpty()) {
                Log.d("StudentDateSheet", "Detected test data in name: " + fullName);
                return true;
            }
        }
        
        // Check for invalid IDs
        String uniqueId = student.getUniqueId();
        if (uniqueId != null) {
            String lowerId = uniqueId.toLowerCase().trim();
            if (lowerId.contains("test") || 
                lowerId.isEmpty() ||
                lowerId.equals("0") ||
                lowerId.equals("null")) {
                Log.d("StudentDateSheet", "Detected test data in ID: " + uniqueId);
                return true;
            }
        }
        
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Refresh student data when returning from other activities
        try {
            SharedStudent previousStudent = student;
            student = Paper.book().read("current_child_model");
            try {
                studentList = Paper.book().read("students", new ArrayList<>());
            } catch (Exception e) {
                // If there's a serialization error, clear the corrupted data and start fresh
                Paper.book().delete("students");
                studentList = new ArrayList<>();
            }
            
            // Update the display
            updateStudentDisplay();
            
            // Load exam sessions if student is already selected
            if (student != null && campusId != null && !campusId.isEmpty()) {
                String previousClassId = selectedStudentClassId;
                selectedChildId = student.getUniqueId();
                selectedChildName = student.getFullName();
                selectedStudentClassId = student.getStudentClassId();
                
                // Validate that class ID is still correct
                if (selectedStudentClassId == null || selectedStudentClassId.isEmpty()) {
                    Log.w("StudentDateSheet", "onResume() - WARNING: Student class ID is null or empty after reload");
                    selectedStudentClassId = previousClassId; // Keep previous class ID if new one is invalid
                } else if (previousClassId != null && !previousClassId.equals(selectedStudentClassId)) {
                    Log.w("StudentDateSheet", "onResume() - WARNING: Student class ID changed from " + previousClassId + " to " + selectedStudentClassId);
                    // Class ID changed - this might be intentional, but log it
                }
                
                Log.d("StudentDateSheet", "onResume() - Student: " + selectedChildName + ", Class ID: " + selectedStudentClassId);
                
                if (selectedChildId != null && !selectedChildId.isEmpty()) {
                    // Only reload exam sessions if student changed or exam sessions are not loaded
                    if (previousStudent == null || !previousStudent.getUniqueId().equals(selectedChildId) || examSessionList == null || examSessionList.isEmpty()) {
                        Log.d("StudentDateSheet", "onResume() - Auto-loading exam sessions for student: " + selectedChildName);
                        loadExamSession(campusId, selectedChildId);
                    } else {
                        Log.d("StudentDateSheet", "onResume() - Skipping exam session reload (student unchanged and sessions already loaded)");
                    }
                }
            }
            
            // Update child adapter if student list has changed
            if (studentList != null && !studentList.isEmpty()) {
                List<String> student_name_list = new ArrayList<>();
                for (int i = 0; i < studentList.size(); i++) {
                    SharedStudent s = studentList.get(i);
                    if (s != null) {
                        String name = s.getFullName();
                        if (name != null && !name.trim().isEmpty()) {
                            student_name_list.add(name);
                        } else {
                            student_name_list.add("SharedStudent " + (i + 1));
                        }
                    } else {
                        student_name_list.add("SharedStudent " + (i + 1));
                    }
                }
                childAdaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, student_name_list);
            }
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error refreshing data in onResume", e);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Dismiss dialog when activity is paused to prevent window leaks
        dismissDialogSafely();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure dialog is dismissed when activity is destroyed
        dismissDialogSafely();
        alertDialog = null;
    }
    
    /**
     * Safely dismisses the dialog, checking for null and activity state
     */
    private void dismissDialogSafely() {
        try {
            if (alertDialog != null && alertDialog.isShowing()) {
                // Check if activity is not finishing or destroyed
                if (!isFinishing() && !isDestroyed()) {
                    alertDialog.dismiss();
                }
            }
            // Clear dialog view reference when dialog is dismissed
            dialogView = null;
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error dismissing dialog", e);
        }
    }

    private void listeners() {
        // Wire SMS button click listener
        if (sendDateSheetInSms != null) {
            sendDateSheetInSms.setOnClickListener(this);
            Log.d("StudentDateSheet", "listeners() - SMS button click listener attached");
        } else {
            Log.e("StudentDateSheet", "listeners() - SMS button is null, cannot attach click listener");
        }
        
        // Inline filters are now set up in setupInlineFilters()
    }

    private void clearDateSheetData() {
        try {
            // Clear date sheet data
            if (list != null) {
                list.clear();
            } else {
                list = new ArrayList<>();
            }
            
            // Clear RecyclerView adapter
            if (timetableRcv != null) {
                timetableRcv.setAdapter(new StudentDateSheetAdaptor(new ArrayList<>(), this));
            }
            
            // Clear total records
            if (tvTotalRecords != null) {
                tvTotalRecords.setVisibility(View.GONE);
            }
            
            // Clear exam session data
            if (examSessionList != null) {
                examSessionList.clear();
            } else {
                examSessionList = new ArrayList<>();
            }
            
            if (examSessionNameList != null) {
                examSessionNameList.clear();
            } else {
                examSessionNameList = new ArrayList<>();
            }
            
            selectedExamSession = "";
            selectedExamSessionName = "";
            
            // Hide session layout
            if (sessionLayout != null) {
                sessionLayout.setVisibility(View.GONE);
            }
            
            // Keep SMS button visible (always visible)
            if (sendDateSheetInSms != null) {
                sendDateSheetInSms.setVisibility(View.VISIBLE);
            }
            
            Log.d("StudentDateSheet", "clearDateSheetData() - All date sheet data cleared");
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error clearing date sheet data: " + e.getMessage(), e);
            // Ensure lists are initialized even if clearing failed
            if (list == null) {
                list = new ArrayList<>();
            }
            if (examSessionList == null) {
                examSessionList = new ArrayList<>();
            }
            if (examSessionNameList == null) {
                examSessionNameList = new ArrayList<>();
            }
        }
    }

    private void showNoStudentsDialog() {
        // Check if activity is still valid before showing dialog
        if (isFinishing() || isDestroyed()) {
            Log.w("StudentDateSheet", "Activity is finishing or destroyed, cannot show dialog");
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Students Available");
        builder.setMessage("No students found. Would you like to go to Child List to add students?");
        builder.setPositiveButton("Go to Child List", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                // Navigate to Child List
                Intent intent = new Intent(StudentDateSheet.this, ChildList.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void init() {
        try {
            Paper.init(context);
            selectChildSpinner = findViewById(R.id.selectChildSpinner);
            selectExamSession = findViewById(R.id.selectExamSession);
            tvTotalRecords = findViewById(R.id.total_records);
            sendDateSheetInSms = findViewById(R.id.send_datesheet_in_sms);
            btnViewDateSheetImages = findViewById(R.id.btn_view_date_sheet_images);
            debugResponseText = findViewById(R.id.debug_response_text);
            debugResponseScroll = findViewById(R.id.debug_response_scroll);
            
            // Setup click listener for view images button
            if (btnViewDateSheetImages != null) {
                btnViewDateSheetImages.setOnClickListener(v -> openDateSheetImages());
            }
            // Find RecyclerView in the included layout
            android.view.View dateSheetTable = findViewById(R.id.date_sheet_table);
            if (dateSheetTable != null) {
                timetableRcv = dateSheetTable.findViewById(R.id.date_sheet_rcv);
            } else {
                timetableRcv = findViewById(R.id.rv_student_timetable); // Fallback to old ID
            }
            progressBar = findViewById(R.id.progress_bar_student_timetable);
            sessionLayout = findViewById(R.id.session_layout);
            
            // Optimize RecyclerView for better performance
            if (timetableRcv != null) {
                timetableRcv.setHasFixedSize(true);
                timetableRcv.setItemViewCacheSize(20);
                timetableRcv.setDrawingCacheEnabled(true);
                timetableRcv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            }
            
            // Log button initialization for debugging
            if (sendDateSheetInSms != null) {
                Log.d("StudentDateSheet", "init() - SMS button found and initialized");
            } else {
                Log.e("StudentDateSheet", "init() - SMS button is null!");
            }
            
            // Initialize RecyclerView
            if (timetableRcv != null) {
                timetableRcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
                // Initialize with empty adapter
                if (list == null) {
                    list = new ArrayList<>();
                }
                StudentDateSheetAdaptor adapter = new StudentDateSheetAdaptor(list, this);
                timetableRcv.setAdapter(adapter);
                // Update header syllabus column width to match data
                updateHeaderSyllabusWidth(list);
                // Hide total records initially
                if (tvTotalRecords != null) {
                    tvTotalRecords.setVisibility(View.GONE);
                }
                // Set an empty adapter initially to prevent "No adapter attached" errors
                timetableRcv.setAdapter(new StudentDateSheetAdaptor(new ArrayList<>(), this));
                Log.d("StudentDateSheet", "init() - RecyclerView initialized with LinearLayoutManager and empty adapter");
            } else {
                Log.e("StudentDateSheet", "init() - RecyclerView is null!");
            }
            
            // Log all available data for debugging
            Log.d("StudentDateSheet", "=== INITIALIZING STUDENT DATE SHEET ===");
            
            // Clear any test data first
            clearTestData();
            
            // Read data with null checks and fallback mechanisms
            student = Paper.book().read("current_child_model");
            parentId = Paper.book().read("parent_id", "");
            campusId = Paper.book().read("campus_id", "");
            try {
                studentList = Paper.book().read("students", new ArrayList<>());
            } catch (Exception e) {
                // If there's a serialization error, clear the corrupted data and start fresh
                Paper.book().delete("students");
                studentList = new ArrayList<>();
            }
            
            // Log what we found
            Log.d("StudentDateSheet", "Current student: " + (student != null ? "Found" : "Null"));
            Log.d("StudentDateSheet", "Parent ID: " + parentId);
            Log.d("StudentDateSheet", "Campus ID: " + campusId);
            Log.d("StudentDateSheet", "SharedStudent list size: " + (studentList != null ? studentList.size() : "null"));
            
            // If student is null but studentList has data, use the first student
            if (student == null && studentList != null && !studentList.isEmpty()) {
                Log.d("StudentDateSheet", "No current student, using first student from list");
                student = studentList.get(0);
                // Save the current student for future use
                Paper.book().write("current_child_model", student);
                Log.d("StudentDateSheet", "Selected first student: " + (student.getFullName() != null ? student.getFullName() : "No name"));
            }
            
            // If still no student data, try to load from other sources
            if (student == null) {
                Log.d("StudentDateSheet", "No student found, trying alternative sources");
                // Try to get student from other storage keys
                student = Paper.book().read("selected_student");
                if (student != null) {
                    Log.d("StudentDateSheet", "Found student from selected_student: " + (student.getFullName() != null ? student.getFullName() : "No name"));
                } else {
                    Log.d("StudentDateSheet", "No student found in any storage, will show dialog");
                    // Don't create a default student - let the user select one
                    student = null;
                }
            }
            
            // Set selected student info if student is available
            if (student != null) {
                selectedChildId = student.getUniqueId();
                selectedChildName = student.getFullName();
                selectedStudentClassId = student.getStudentClassId();
                Log.d("StudentDateSheet", "init() - Set selected student info - ID: " + selectedChildId + ", Name: " + selectedChildName);
                Log.d("StudentDateSheet", "init() - Student class_id from current_child_model: " + selectedStudentClassId);
                Log.d("StudentDateSheet", "init() - Student class_name from current_child_model: " + (student.getClassName() != null ? student.getClassName() : "NULL"));
                
                // Also check if student exists in studentList and has different class_id (more recent data)
                if (studentList != null && !studentList.isEmpty()) {
                    for (SharedStudent s : studentList) {
                        if (s.getUniqueId().equals(selectedChildId)) {
                            Log.d("StudentDateSheet", "init() - Found student in students list - class_id: " + s.getStudentClassId() + ", class_name: " + (s.getClassName() != null ? s.getClassName() : "NULL"));
                            // Use the class_id from the list if it's different (more up-to-date from profile/API)
                            if (s.getStudentClassId() != null && !s.getStudentClassId().isEmpty()) {
                                if (!s.getStudentClassId().equals(selectedStudentClassId)) {
                                    Log.w("StudentDateSheet", "init() - ⚠️ class_id MISMATCH! current_child_model: " + selectedStudentClassId + ", students list: " + s.getStudentClassId());
                                    Log.w("StudentDateSheet", "init() - Using class_id from students list (more recent from API)");
                                    selectedStudentClassId = s.getStudentClassId();
                                    // Update the current student model with fresh data
                                    student = s;
                                    Paper.book().write("current_child_model", student);
                                }
                            }
                            break;
                        }
                    }
                }
            }
            
            // Initialize child adapter with null checks
            List<String> student_name_list = new ArrayList<>();
            if (studentList != null && !studentList.isEmpty()) {
                Log.d("StudentDateSheet", "Creating student list adapter with " + studentList.size() + " students");
                for (int i = 0; i < studentList.size(); i++) {
                    SharedStudent s = studentList.get(i);
                    if (s != null && s.getFullName() != null && !s.getFullName().trim().isEmpty()) {
                        String name = s.getFullName().trim();
                        student_name_list.add(name);
                        Log.d("StudentDateSheet", "Added student " + i + ": " + name);
                    } else {
                        String fallbackName = "SharedStudent " + (i + 1);
                        student_name_list.add(fallbackName);
                        Log.d("StudentDateSheet", "Added fallback student " + i + ": " + fallbackName);
                    }
                }
            } else {
                Log.d("StudentDateSheet", "No student list available, creating empty adapter");
                // If no student list, don't add any default entries
                // The user will need to go to Child List to add students
            }
            childAdaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, student_name_list);
            
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in init()", e);
            Toast.makeText(this, "Error initializing date sheet", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupInlineFilters() {
        if (selectChildSpinner != null) {
            selectChildSpinner.setTitle("Select Child");
            selectChildSpinner.setPositiveButton("OK");
            
            // Check if student list is empty
            if (studentList == null || studentList.isEmpty()) {
                selectChildSpinner.setEnabled(false);
                Toast.makeText(context, "No students found. Please contact your school administrator.", Toast.LENGTH_SHORT).show();
                Log.w("StudentDateSheet", "setupInlineFilters() - No students available, disabling child spinner");
            } else {
                selectChildSpinner.setAdapter(childAdaptor);
                selectChildSpinner.setEnabled(true);
                Log.d("StudentDateSheet", "setupInlineFilters() - Child spinner enabled with " + studentList.size() + " students");
            }
        }
        
        if (selectExamSession != null) {
            selectExamSession.setTitle("Select Exam Session");
            selectExamSession.setPositiveButton("OK");
            
            // Set up listener for exam session selection in main layout
            selectExamSession.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("StudentDateSheet", "[DEBUG] Main layout Exam Session onItemSelected() CALLED");
                    Log.d("StudentDateSheet", "[DEBUG] Position: " + position);
                    
                    if (examSessionList != null && position < examSessionList.size()) {
                        selectedExamSession = examSessionList.get(position).getUniqueId();
                        selectedExamSessionName = examSessionList.get(position).getFullName();
                        Log.d("StudentDateSheet", "[DEBUG] Selected session ID: " + selectedExamSession);
                        Log.d("StudentDateSheet", "[DEBUG] Selected session Name: " + selectedExamSessionName);
                        
                        // Auto-load date sheet when exam session is selected
                        if (selectedChildId != null && !selectedChildId.isEmpty() && selectedExamSession != null && !selectedExamSession.isEmpty()) {
                            Log.d("StudentDateSheet", "[DEBUG] Auto-loading date sheet when exam session selected in main layout");
                            loadDateSheet(selectedExamSession);
                        } else {
                            Log.w("StudentDateSheet", "[DEBUG] Cannot auto-load date sheet - missing student ID or exam session");
                        }
                    } else {
                        Log.e("StudentDateSheet", "[DEBUG] Invalid position: " + position + " (list size: " + (examSessionList != null ? examSessionList.size() : 0) + ")");
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
        
        if (selectChildSpinner != null) {
            selectChildSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Clear previous date sheet data when student changes
                    clearDateSheetData();
                    
                    student = studentList.get(position);
                    selectedChildId = studentList.get(position).getUniqueId();
                    selectedChildName = studentList.get(position).getFullName();
                    selectedStudentClassId = studentList.get(position).getStudentClassId();
                    
                    // Debug logging
                    Log.d("StudentDateSheet", "onItemSelected() - Selected student: " + selectedChildName);
                    Log.d("StudentDateSheet", "onItemSelected() - SharedStudent ID: " + selectedChildId);
                    Log.d("StudentDateSheet", "onItemSelected() - Class ID: " + selectedStudentClassId);
                    
                    // Update the UI to show selected student info
                    if (student != null) {
                        String studentName = student.getFullName();
                        String className = student.getClassName();
                        
                        Log.d("StudentDateSheet", "onItemSelected() - SharedStudent name: " + studentName);
                        Log.d("StudentDateSheet", "onItemSelected() - Class name: " + className);
                        
                        Log.d("StudentDateSheet", "onItemSelected() - Updated UI with: " + studentName + " - " + className);
                        loadExamSession(campusId, selectedChildId);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void popupSelectChildDialog() {
        try {
            LayoutInflater inflater = this.getLayoutInflater();
            dialogView = inflater.inflate(R.layout.progress_report_advanced_search_layout, null);

            selectChildSpinner = dialogView.findViewById(R.id.selectChildSpinner);
            dialogSearchFilterBtn = dialogView.findViewById(R.id.search_button);
            selectExamSession = dialogView.findViewById(R.id.selectExampSession);
            
            // Check if dialogCancelBtn exists before setting click listener
            dialogCancelBtn = dialogView.findViewById(R.id.Cancel);
            if (dialogCancelBtn != null) {
                dialogCancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialogSafely();
                    }
                });
            }

            if (selectChildSpinner != null) {
                selectChildSpinner.setTitle("Select Child");
                selectChildSpinner.setPositiveButton("OK");
            }
            
            if (selectExamSession != null) {
                selectExamSession.setTitle("Select Exam Session");
                selectExamSession.setPositiveButton("OK");
            }

            // Check if we have students to show
            if (studentList == null || studentList.isEmpty()) {
                // Show message to user
                Toast.makeText(this, "No students available. Please check Child List first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectChildSpinner != null && childAdaptor != null) {
                selectChildSpinner.setAdapter(childAdaptor);
                
                selectChildSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        if (studentList != null && position < studentList.size()) {
                            SharedStudent selectedStudent = studentList.get(position);
                            if (selectedStudent != null) {
                                selectedChildId = selectedStudent.getUniqueId();
                                selectedChildName = selectedStudent.getFullName();
                                selectedStudentClassId = selectedStudent.getStudentClassId();
                                
                                // Update the current student
                                student = selectedStudent;
                                Paper.book().write("current_child_model", student);
                                
                                // Update the UI with the selected student's information
                                updateStudentDisplay();
                                
                                // Log the selected student information for debugging
                                Log.d("StudentDateSheet", "Selected SharedStudent - ID: " + selectedChildId + 
                                      ", Name: " + selectedChildName + 
                                      ", Class ID: " + selectedStudentClassId + 
                                      ", Class Name: " + (student.getClassName() != null ? student.getClassName() : "N/A") +
                                      ", Full Name: " + (student.getFullName() != null ? student.getFullName() : "N/A"));
                                
                                // Validate that we have all required information
                                if (selectedChildId == null || selectedChildId.isEmpty()) {
                                    Toast.makeText(StudentDateSheet.this, "SharedStudent ID not available", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                
                                if (selectedStudentClassId == null || selectedStudentClassId.isEmpty()) {
                                    Toast.makeText(StudentDateSheet.this, "SharedStudent class ID not available", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                
                                if (campusId == null || campusId.isEmpty()) {
                                    Toast.makeText(StudentDateSheet.this, "Campus ID not available", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                
                                // All validations passed, load exam sessions
                                loadExamSession(campusId, selectedChildId);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("StudentDateSheet", "Error in onItemSelected", e);
                        Toast.makeText(StudentDateSheet.this, "Error selecting student", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
                });
            }

            // Check if activity is still valid before showing dialog
            if (isFinishing() || isDestroyed()) {
                Log.w("StudentDateSheet", "Activity is finishing or destroyed, cannot show dialog");
                return;
            }
            
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setView(dialogView);

            alertDialog = dialogBuilder.create();
            alertDialog.show();

            if (dialogSearchFilterBtn != null) {
                dialogSearchFilterBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (selectedChildId == null || selectedChildId.isEmpty()) {
                            Toast.makeText(StudentDateSheet.this, "Please select a student first", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (selectedExamSession == null || selectedExamSession.isEmpty()) {
                            Toast.makeText(StudentDateSheet.this, "Please select an exam session first", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dismissDialogSafely();
                        if (list != null) {
                            list.clear();
                        } else {
                            list = new ArrayList<>();
                        }
                        tvTotalRecords.setVisibility(View.GONE);
                        loadDateSheet(selectedExamSession);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in popupSelectChildDialog", e);
            Toast.makeText(this, "Error showing dialog", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadExamSession(final String campus_id, String child_id) {
        try {
            // Validate parameters - API only requires parent_id (campus_id)
            if (campus_id == null || campus_id.isEmpty()) {
                Log.e("StudentDateSheet", "[DEBUG] Campus ID validation failed");
                Toast.makeText(this, "Campus ID not available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress bar
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

            // Build request exactly as per curl command:
            // curl -X POST "https://topgradesoftware.com/api.php?page=parent/load_exam_session"
            // -H "Content-Type: application/json"
            // -d '{"parent_id": "YOUR_CAMPUS_ID"}'
            HashMap<String, String> postParam = new HashMap<String, String>();
            postParam.put("parent_id", campus_id);
            
            // Log the exam session request parameters
            Log.d("StudentDateSheet", "========================================");
            Log.d("StudentDateSheet", "=== LOADING EXAM SESSIONS ===");
            Log.d("StudentDateSheet", "API Endpoint: api.php?page=parent/load_exam_session");
            Log.d("StudentDateSheet", "Base URL: " + API.base_url);
            Log.d("StudentDateSheet", "Full URL: " + API.base_url + "api.php?page=parent/load_exam_session");
            Log.d("StudentDateSheet", "Request Method: POST");
            Log.d("StudentDateSheet", "Content-Type: application/json");
            Log.d("StudentDateSheet", "Parent ID (Campus ID): " + campus_id);
            Log.d("StudentDateSheet", "[DEBUG] Note: API only requires parent_id parameter");
            
            String jsonBody = new JSONObject(postParam).toString();
            Log.d("StudentDateSheet", "Exam Session JSON Body: " + jsonBody);
            Log.d("StudentDateSheet", "========================================");
            
            // Show request parameters on screen immediately
            StringBuilder examSessionRequestDebug = new StringBuilder();
            examSessionRequestDebug.append("=== EXAM SESSION REQUEST ===\n\n");
            examSessionRequestDebug.append("URL: ").append(API.base_url).append("api.php?page=parent/load_exam_session\n");
            examSessionRequestDebug.append("Method: POST\n");
            examSessionRequestDebug.append("Content-Type: application/json\n\n");
            examSessionRequestDebug.append("Parameters:\n");
            examSessionRequestDebug.append("  parent_id: ").append(campus_id).append("\n\n");
            examSessionRequestDebug.append("JSON Body:\n").append(jsonBody).append("\n\n");
            examSessionRequestDebug.append("Status: Sending request...\n");
            
            // Debug info removed from screen - only logged to Logcat
            Log.d("StudentDateSheet", "[DEBUG] Exam session request parameters: " + examSessionRequestDebug.toString());
            
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Constant.mApiService.load_exam_session(body).enqueue(new Callback<SessionModel>() {
                @Override
                public void onResponse(Call<SessionModel> call, retrofit2.Response<SessionModel> response) {
                    try {
                        Log.d("StudentDateSheet", "Exam Session Response Code: " + response.code());
                        Log.d("StudentDateSheet", "Exam Session Response Message: " + response.message());
                        
                        // Build debug response text
                        StringBuilder examSessionResponseDebug = new StringBuilder();
                        examSessionResponseDebug.append("=== EXAM SESSION REQUEST ===\n");
                        examSessionResponseDebug.append("URL: ").append(API.base_url).append("api.php?page=parent/load_exam_session\n");
                        examSessionResponseDebug.append("Method: POST\n\n");
                        examSessionResponseDebug.append("Request Parameters:\n");
                        examSessionResponseDebug.append("  parent_id: ").append(campus_id).append("\n\n");
                        examSessionResponseDebug.append("=== RESPONSE ===\n\n");
                        examSessionResponseDebug.append("HTTP Code: ").append(response.code()).append("\n");
                        examSessionResponseDebug.append("Status: ").append(response.isSuccessful() ? "SUCCESS" : "FAILED").append("\n\n");
                        
                        if (response.isSuccessful()) {
                            SessionModel sessionModel = response.body();
                            if (sessionModel != null && sessionModel.getStatus() != null) {
                                Log.d("StudentDateSheet", "Exam Session API SharedStatus: " + sessionModel.getStatus().getCode());
                                Log.d("StudentDateSheet", "Exam Session API Message: " + sessionModel.getStatus().getMessage());
                                
                                examSessionResponseDebug.append("API Status Code: ").append(sessionModel.getStatus().getCode()).append("\n");
                                examSessionResponseDebug.append("API Message: ").append(sessionModel.getStatus().getMessage()).append("\n\n");
                                
                                if (sessionModel.getStatus().getCode().equals("1000")) {
                                examSessionList = sessionModel.getExamSession();
                                examSessionNameList.clear(); // Clear previous data
                                
                                examSessionResponseDebug.append("=== EXAM SESSIONS ===\n");
                                examSessionResponseDebug.append("Total Sessions: ").append(examSessionList != null ? examSessionList.size() : 0).append("\n\n");
                                    
                                    Log.d("StudentDateSheet", "Exam Sessions received: " + (examSessionList != null ? examSessionList.size() : "null"));
                                
                                if (examSessionList != null && !examSessionList.isEmpty()) {
                                    // Add session details to debug text
                                    for (int i = 0; i < Math.min(examSessionList.size(), 10); i++) {
                                        ExamSession session = examSessionList.get(i);
                                        if (session != null && session.getFullName() != null) {
                                            examSessionNameList.add(session.getFullName());
                                            Log.d("StudentDateSheet", "Exam Session " + i + ": " + session.getFullName() + " (ID: " + session.getUniqueId() + ")");
                                            
                                            examSessionResponseDebug.append("Session ").append(i + 1).append(":\n");
                                            examSessionResponseDebug.append("  ID: ").append(session.getUniqueId() != null ? session.getUniqueId() : "N/A").append("\n");
                                            examSessionResponseDebug.append("  Name: ").append(session.getFullName()).append("\n\n");
                                        }
                                    }
                                    if (examSessionList.size() > 10) {
                                        examSessionResponseDebug.append("... and ").append(examSessionList.size() - 10).append(" more sessions\n");
                                    }

                                    if (examSessionList.size() > 0) {
                                        // Hide progress bar on success
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        
                                        if (examSessionList.size() > 1) {
                                            Log.d("StudentDateSheet", "Multiple sessions, showing session layout");
                                            if (sessionLayout != null) {
                                                sessionLayout.setVisibility(View.VISIBLE);
                                            }
                                            sessionAdaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, examSessionNameList);
                                            
                                            // Ensure selectExamSession is initialized - check dialog first, then main layout
                                            components.searchablespinnerlibrary.SearchableSpinner spinnerToUpdate = null;
                                            
                                            // First, try to find spinner in dialog (if dialog is showing)
                                            if (alertDialog != null && alertDialog.isShowing() && dialogView != null) {
                                                spinnerToUpdate = dialogView.findViewById(R.id.selectExampSession);
                                                if (spinnerToUpdate != null) {
                                                    Log.d("StudentDateSheet", "Found spinner in dialog");
                                                    selectExamSession = spinnerToUpdate;
                                                }
                                            }
                                            
                                            // If not in dialog, try main layout
                                            if (spinnerToUpdate == null) {
                                                spinnerToUpdate = findViewById(R.id.selectExamSession);
                                                if (spinnerToUpdate != null) {
                                                    Log.d("StudentDateSheet", "Found spinner in main layout");
                                                    selectExamSession = spinnerToUpdate;
                                                }
                                            }
                                            
                                            // If still null and dialog view exists, try stored dialog view
                                            if (spinnerToUpdate == null && dialogView != null) {
                                                spinnerToUpdate = dialogView.findViewById(R.id.selectExampSession);
                                                if (spinnerToUpdate != null) {
                                                    Log.d("StudentDateSheet", "Found spinner in stored dialog view");
                                                    selectExamSession = spinnerToUpdate;
                                                }
                                            }
                                            
                                            if (spinnerToUpdate != null) {
                                                selectExamSession = spinnerToUpdate;
                                                Log.d("StudentDateSheet", "Setting exam session adapter with " + examSessionNameList.size() + " sessions");
                                                // Ensure we're on UI thread
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            selectExamSession.setAdapter(sessionAdaptor);
                                                            Log.d("StudentDateSheet", "Exam session adapter set successfully");
                                                        } catch (Exception e) {
                                                            Log.e("StudentDateSheet", "Error setting exam session adapter", e);
                                                        }
                                                    }
                                                });
                                                // Use a flag to prevent auto-loading when adapter is set programmatically
                                                final boolean[] isInitialSelection = {true};
                                                selectExamSession.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        Log.d("StudentDateSheet", "[DEBUG] Exam Session onItemSelected() CALLED");
                                                        Log.d("StudentDateSheet", "[DEBUG] Position: " + position);
                                                        Log.d("StudentDateSheet", "[DEBUG] examSessionList size: " + examSessionList.size());
                                                        Log.d("StudentDateSheet", "[DEBUG] Is initial selection: " + isInitialSelection[0]);
                                                        
                                                        if (position < examSessionList.size()) {
                                                            String newSessionId = examSessionList.get(position).getUniqueId();
                                                            String newSessionName = examSessionList.get(position).getFullName();
                                                            
                                                            // Only auto-load if this is a user selection (not initial programmatic selection)
                                                            // or if the session actually changed
                                                            boolean shouldLoad = false;
                                                            if (!isInitialSelection[0]) {
                                                                // User manually selected a different session
                                                                shouldLoad = true;
                                                                Log.d("StudentDateSheet", "[DEBUG] User selected different exam session");
                                                            } else if (!newSessionId.equals(selectedExamSession)) {
                                                                // Session changed during initial selection
                                                                shouldLoad = true;
                                                                Log.d("StudentDateSheet", "[DEBUG] Exam session changed during initial selection");
                                                            } else {
                                                                Log.d("StudentDateSheet", "[DEBUG] Skipping auto-load (same session, initial selection)");
                                                            }
                                                            
                                                            selectedExamSession = newSessionId;
                                                            selectedExamSessionName = newSessionName;
                                                            Log.d("StudentDateSheet", "[DEBUG] Selected session ID: " + selectedExamSession);
                                                            Log.d("StudentDateSheet", "[DEBUG] Selected session Name: " + selectedExamSessionName);
                                                            Log.d("StudentDateSheet", "[DEBUG] Selected student class ID: " + selectedStudentClassId);
                                                            
                                                            // Validate class ID before loading
                                                            if (shouldLoad && selectedChildId != null && !selectedChildId.isEmpty() && 
                                                                selectedExamSession != null && !selectedExamSession.isEmpty()) {
                                                                if (selectedStudentClassId != null && !selectedStudentClassId.isEmpty()) {
                                                                    Log.d("StudentDateSheet", "[DEBUG] Auto-loading date sheet when exam session selected");
                                                                    loadDateSheet(selectedExamSession);
                                                                } else {
                                                                    Log.w("StudentDateSheet", "[DEBUG] Cannot auto-load date sheet - student class ID is missing");
                                                                }
                                                            } else if (shouldLoad) {
                                                                Log.w("StudentDateSheet", "[DEBUG] Cannot auto-load date sheet - missing student ID or exam session");
                                                            }
                                                            
                                                            // Reset flag after first selection
                                                            isInitialSelection[0] = false;
                                                        } else {
                                                            Log.e("StudentDateSheet", "[DEBUG] Invalid position: " + position + " (list size: " + examSessionList.size() + ")");
                                                        }
                                                    }
                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {}
                                                });
                                            } else {
                                                Log.w("StudentDateSheet", "selectExamSession is null, cannot set adapter");
                                                Toast.makeText(context, "Exam session spinner not available", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Log.d("StudentDateSheet", "Single session, showing in spinner");
                                            // Hide progress bar on success
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                            
                                            // Even with single session, show it in the spinner so user can see it
                                            selectedExamSession = examSessionList.get(0).getUniqueId();
                                            selectedExamSessionName = examSessionList.get(0).getFullName();
                                            Log.d("StudentDateSheet", "[DEBUG] Auto-selected session ID: " + selectedExamSession);
                                            Log.d("StudentDateSheet", "[DEBUG] Auto-selected session Name: " + selectedExamSessionName);
                                            
                                            // Show the session in spinner so user can see it
                                            runOnUiThread(() -> {
                                                // Try to find spinner in dialog first (if dialog is showing)
                                                components.searchablespinnerlibrary.SearchableSpinner spinnerToUpdate = null;
                                                
                                                if (alertDialog != null && alertDialog.isShowing() && dialogView != null) {
                                                    // Dialog is showing, use dialog spinner
                                                    spinnerToUpdate = dialogView.findViewById(R.id.selectExampSession);
                                                    if (spinnerToUpdate != null) {
                                                        Log.d("StudentDateSheet", "[DEBUG] Found spinner in dialog");
                                                        selectExamSession = spinnerToUpdate;
                                                    }
                                                }
                                                
                                                // If not in dialog, try main layout
                                                if (spinnerToUpdate == null) {
                                                    spinnerToUpdate = findViewById(R.id.selectExamSession);
                                                    if (spinnerToUpdate != null) {
                                                        Log.d("StudentDateSheet", "[DEBUG] Found spinner in main layout");
                                                        selectExamSession = spinnerToUpdate;
                                                    }
                                                }
                                                
                                                if (spinnerToUpdate != null) {
                                                    // Create adapter with single session
                                                    List<String> singleSessionList = new ArrayList<>();
                                                    singleSessionList.add(selectedExamSessionName);
                                                    sessionAdaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, singleSessionList);
                                                    spinnerToUpdate.setAdapter(sessionAdaptor);
                                                    
                                                    // Set the selected item
                                                    spinnerToUpdate.setSelection(0);
                                                    
                                                    // Add listener to auto-load date sheet when session is selected
                                                    spinnerToUpdate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                            if (position < examSessionList.size()) {
                                                                selectedExamSession = examSessionList.get(position).getUniqueId();
                                                                selectedExamSessionName = examSessionList.get(position).getFullName();
                                                                Log.d("StudentDateSheet", "[DEBUG] Single session selected - ID: " + selectedExamSession);
                                                                // Auto-load date sheet for single session
                                                                if (selectedChildId != null && !selectedChildId.isEmpty() && selectedExamSession != null && !selectedExamSession.isEmpty()) {
                                                                    Log.d("StudentDateSheet", "[DEBUG] Auto-loading date sheet for single session");
                                                                    loadDateSheet(selectedExamSession);
                                                                }
                                                            }
                                                        }
                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> parent) {}
                                                    });
                                                    
                                                    Log.d("StudentDateSheet", "[DEBUG] Single session displayed in spinner: " + selectedExamSessionName);
                                                    
                                                    // Auto-load date sheet immediately for single session
                                                    if (selectedChildId != null && !selectedChildId.isEmpty() && selectedExamSession != null && !selectedExamSession.isEmpty()) {
                                                        Log.d("StudentDateSheet", "[DEBUG] Auto-loading date sheet for single session immediately");
                                                        loadDateSheet(selectedExamSession);
                                                    }
                                                } else {
                                                    Log.w("StudentDateSheet", "[DEBUG] selectExamSession is null in both dialog and main layout");
                                                }
                                            });
                                        }
                                    }
                                } else {
                                        // Hide progress bar when no sessions found
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        Log.d("StudentDateSheet", "No exam sessions available for this student");
                                        
                                        examSessionResponseDebug.append("⚠️ No exam sessions found\n");
                                        examSessionResponseDebug.append("The API returned success but exam_session array is empty\n");
                                        final String emptyDebugText = examSessionResponseDebug.toString();
                                        // Debug info removed from screen - only logged to Logcat
                                        Log.d("StudentDateSheet", "[DEBUG] Empty exam session response: " + emptyDebugText);
                                        
                                        Toast.makeText(context, "No exam sessions available for this student", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                                    Log.d("StudentDateSheet", "Exam Session API Error: " + (sessionModel != null ? sessionModel.getStatus().getMessage() : "Unknown error"));
                                    
                                    examSessionResponseDebug.append("ERROR: API Status Code is not 1000\n");
                                    final String errorDebugText = examSessionResponseDebug.toString();
                                    // Debug info removed from screen - only logged to Logcat
                                    Log.e("StudentDateSheet", "[DEBUG] Exam session error: " + errorDebugText);
                                    
                                    Toast.makeText(context, sessionModel != null ? sessionModel.getStatus().getMessage() : "Failed to load exam sessions", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                                Log.d("StudentDateSheet", "Invalid exam session response structure");
                                
                                examSessionResponseDebug.append("ERROR: Invalid response structure\n");
                                examSessionResponseDebug.append("Response body is null: ").append(sessionModel == null).append("\n");
                                final String invalidDebugText = examSessionResponseDebug.toString();
                                // Debug info removed from screen - only logged to Logcat
                                Log.e("StudentDateSheet", "[DEBUG] Invalid exam session response: " + invalidDebugText);
                                
                                Toast.makeText(context, "Failed to load exam sessions", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Log.d("StudentDateSheet", "Exam Session HTTP Error: " + response.code());
                            
                            examSessionResponseDebug.append("ERROR: HTTP Error\n");
                            examSessionResponseDebug.append("Response Message: ").append(response.message()).append("\n");
                            final String httpErrorDebugText = examSessionResponseDebug.toString();
                            // Debug info removed from screen - only logged to Logcat
                            Log.e("StudentDateSheet", "[DEBUG] Exam session HTTP error: " + httpErrorDebugText);
                            
                            Toast.makeText(context, "Failed to load exam sessions", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("StudentDateSheet", "Error in loadExamSession onResponse", e);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, "Error processing exam sessions", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SessionModel> call, Throwable e) {
                    Log.e("StudentDateSheet", "Error in loadExamSession onFailure", e);
                    progressBar.setVisibility(View.GONE);
                    
                    // Build error debug text
                    StringBuilder errorDebug = new StringBuilder();
                    errorDebug.append("=== EXAM SESSION REQUEST ===\n");
                    errorDebug.append("URL: ").append(API.base_url).append("api.php?page=parent/load_exam_session\n");
                    errorDebug.append("Method: POST\n\n");
                    errorDebug.append("Request Parameters:\n");
                    errorDebug.append("  parent_id: ").append(campus_id).append("\n\n");
                    errorDebug.append("=== API CALL FAILED ===\n\n");
                    errorDebug.append("Error Type: ").append(e.getClass().getSimpleName()).append("\n");
                    errorDebug.append("Error Message: ").append(e.getMessage() != null ? e.getMessage() : "Unknown error").append("\n");
                    if (e.getCause() != null) {
                        errorDebug.append("Cause: ").append(e.getCause().getMessage()).append("\n");
                    }
                    
                    final String errorDebugText = errorDebug.toString();
                    // Debug info removed from screen - only logged to Logcat
                    Log.e("StudentDateSheet", "[DEBUG] Exam session failure: " + errorDebugText);
                    runOnUiThread(() -> {
                        Toast.makeText(context, "Failed to load exam sessions", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in loadExamSession", e);
            Toast.makeText(this, "Error loading exam sessions", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDateSheet(String sessionId) {
        // Reset flag when starting new load
        dataLoadedSuccessfully = false;
        
        Log.d("StudentDateSheet", "========================================");
        Log.d("StudentDateSheet", "=== loadDateSheet() CALLED ===");
        Log.d("StudentDateSheet", "Session ID parameter: " + sessionId);
        Log.d("StudentDateSheet", "========================================");
        
        try {
            // Validate required parameters
            Log.d("StudentDateSheet", "[DEBUG] Validating parameters...");
            Log.d("StudentDateSheet", "[DEBUG] selectedChildId: " + selectedChildId + " (null: " + (selectedChildId == null) + ", empty: " + (selectedChildId != null && selectedChildId.isEmpty()) + ")");
            
            if (selectedChildId == null || selectedChildId.isEmpty()) {
                Log.e("StudentDateSheet", "[DEBUG] VALIDATION FAILED: Student not selected");
                Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d("StudentDateSheet", "[DEBUG] selectedExamSession: " + selectedExamSession + " (null: " + (selectedExamSession == null) + ", empty: " + (selectedExamSession != null && selectedExamSession.isEmpty()) + ")");
            if (selectedExamSession == null || selectedExamSession.isEmpty()) {
                Log.e("StudentDateSheet", "[DEBUG] VALIDATION FAILED: Exam session not selected");
                Toast.makeText(this, "Please select an exam session", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d("StudentDateSheet", "[DEBUG] selectedStudentClassId: " + selectedStudentClassId + " (null: " + (selectedStudentClassId == null) + ", empty: " + (selectedStudentClassId != null && selectedStudentClassId.isEmpty()) + ")");
            if (selectedStudentClassId == null || selectedStudentClassId.isEmpty()) {
                Log.e("StudentDateSheet", "[DEBUG] VALIDATION FAILED: Student class ID not available");
                Toast.makeText(this, "SharedStudent class information not available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate campus and parent IDs
            String campusId = Paper.book().read("campus_id", "");
            Log.d("StudentDateSheet", "[DEBUG] campusId from Paper: " + campusId + " (null: " + (campusId == null) + ", empty: " + (campusId != null && campusId.isEmpty()) + ")");
            if (campusId == null || campusId.isEmpty()) {
                Log.e("StudentDateSheet", "[DEBUG] VALIDATION FAILED: Campus ID not available");
                Toast.makeText(this, "Campus information not available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            HashMap<String, String> postParam = new HashMap<String, String>();
            parentId = Paper.book().read("parent_id", "");
            Log.d("StudentDateSheet", "[DEBUG] parentId from Paper: " + parentId + " (null: " + (parentId == null) + ", empty: " + (parentId != null && parentId.isEmpty()) + ")");

            // Log all the parameters being sent
            Log.d("StudentDateSheet", "========================================");
            Log.d("StudentDateSheet", "=== LOADING DATE SHEET ===");
            Log.d("StudentDateSheet", "SharedStudent ID: " + selectedChildId);
            Log.d("StudentDateSheet", "SharedStudent Name: " + selectedChildName);
            Log.d("StudentDateSheet", "SharedStudent Class ID: " + selectedStudentClassId);
            Log.d("StudentDateSheet", "SharedStudent Class Name: " + (student != null ? student.getClassName() : "N/A"));
            Log.d("StudentDateSheet", "Exam Session ID: " + selectedExamSession);
            Log.d("StudentDateSheet", "Exam Session Name: " + selectedExamSessionName);
            Log.d("StudentDateSheet", "Campus ID: " + campusId);
            Log.d("StudentDateSheet", "Parent ID: " + parentId);
            Log.d("StudentDateSheet", "Current Session: " + Constant.current_session);
            Log.d("StudentDateSheet", "========================================");

            // Based on the PHP backend API analysis and working alternative call, the endpoint expects:
            // - campus_id (required) - campus ID
            // - student_id (required) - the student/child ID
            // - exam_session_id (optional) - filters by exam session
            // - parent_id (optional) - parent ID (from Paper storage)
            // - session_id (optional) - current session ID
            // - student_class_id (optional) - the student's class ID from student profile
            // Note: Including student_class_id may cause empty results if backend doesn't handle it correctly
            Log.d("StudentDateSheet", "[DEBUG] Building request parameters...");
            postParam.put("campus_id", campusId); // Required
            postParam.put("student_id", selectedChildId); // Required - student/child ID
            Log.d("StudentDateSheet", "[DEBUG] Added campus_id: " + campusId);
            Log.d("StudentDateSheet", "[DEBUG] Added student_id: " + selectedChildId);
            
            // Optional parameters
            if (selectedExamSession != null && !selectedExamSession.isEmpty()) {
                postParam.put("exam_session_id", selectedExamSession);
                Log.d("StudentDateSheet", "[DEBUG] Added exam_session_id: " + selectedExamSession);
            }
            
            // Add parent_id and session_id like the working alternative call
            if (parentId != null && !parentId.isEmpty()) {
                postParam.put("parent_id", parentId);
                Log.d("StudentDateSheet", "[DEBUG] Added parent_id: " + parentId);
            }
            
            if (Constant.current_session != null && !Constant.current_session.isEmpty()) {
                postParam.put("session_id", Constant.current_session);
                Log.d("StudentDateSheet", "[DEBUG] Added session_id: " + Constant.current_session);
            }
            
            // student_class_id is required for filtering
            postParam.put("student_class_id", selectedStudentClassId);
            Log.d("StudentDateSheet", "[DEBUG] Added student_class_id: " + selectedStudentClassId + " (from student profile - REQUIRED)");
            
            // Log the final JSON being sent
            String jsonBody = new JSONObject(postParam).toString();
            Log.d("StudentDateSheet", "========================================");
            Log.d("StudentDateSheet", "[DEBUG] ===== REQUEST DETAILS =====");
            Log.d("StudentDateSheet", "[DEBUG] API Endpoint: api.php?page=parent/load_datesheet");
            Log.d("StudentDateSheet", "[DEBUG] Base URL: " + API.base_url);
            Log.d("StudentDateSheet", "[DEBUG] Full URL: " + API.base_url + "api.php?page=parent/load_datesheet");
            Log.d("StudentDateSheet", "[DEBUG] Request Method: POST");
            Log.d("StudentDateSheet", "[DEBUG] Content-Type: application/json");
            Log.d("StudentDateSheet", "[DEBUG] JSON Body being sent: " + jsonBody);
            Log.d("StudentDateSheet", "[DEBUG] Request Parameters Count: " + postParam.size());
            for (String key : postParam.keySet()) {
                Log.d("StudentDateSheet", "[DEBUG]   - " + key + ": " + postParam.get(key));
            }
            Log.d("StudentDateSheet", "========================================");
            
            // Show request parameters on screen immediately
            StringBuilder requestDebug = new StringBuilder();
            requestDebug.append("=== REQUEST PARAMETERS ===\n\n");
            requestDebug.append("URL: ").append(API.base_url).append("api.php?page=parent/load_datesheet\n");
            requestDebug.append("Method: POST\n");
            requestDebug.append("Content-Type: application/json\n\n");
            requestDebug.append("Parameters:\n");
            requestDebug.append("  campus_id: ").append(campusId).append("\n");
            requestDebug.append("  student_id: ").append(selectedChildId).append("\n");
            requestDebug.append("  student_class_id: ").append(selectedStudentClassId).append(" (from student profile)\n");
            if (selectedExamSession != null && !selectedExamSession.isEmpty()) {
                requestDebug.append("  exam_session_id: ").append(selectedExamSession).append("\n");
            }
            requestDebug.append("\nJSON Body:\n").append(jsonBody).append("\n\n");
            requestDebug.append("Status: Sending request...\n");
            
            // Debug info removed from screen - only logged to Logcat
            Log.d("StudentDateSheet", "[DEBUG] Date sheet request parameters: " + requestDebug.toString());
            
            // Run network call on background thread to prevent ANR
            Log.d("StudentDateSheet", "[DEBUG] Starting network call in background thread...");
            new Thread(() -> {
                try {
                    Log.d("StudentDateSheet", "[DEBUG] Thread started, showing progress bar...");
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.VISIBLE);
                            Log.d("StudentDateSheet", "[DEBUG] Progress bar made visible");
                        } else {
                            Log.w("StudentDateSheet", "[DEBUG] Progress bar is null!");
                        }
                    });
                    
                    Log.d("StudentDateSheet", "[DEBUG] Creating RequestBody...");
                    RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
                    Log.d("StudentDateSheet", "[DEBUG] RequestBody created successfully");
                    Log.d("StudentDateSheet", "[DEBUG] RequestBody content length: " + body.contentLength());
                    Log.d("StudentDateSheet", "[DEBUG] Calling API: loadStudentDateSheet()...");
                    Constant.mApiService.loadStudentDateSheet(body).enqueue(new Callback<DateSheetResponse>() {
                @Override
                public void onResponse(Call<DateSheetResponse> call, Response<DateSheetResponse> response) {
                    Log.d("StudentDateSheet", "========================================");
                    Log.d("StudentDateSheet", "[DEBUG] ===== API RESPONSE RECEIVED =====");
                    Log.d("StudentDateSheet", "[DEBUG] Thread: " + Thread.currentThread().getName());
                    Log.d("StudentDateSheet", "[DEBUG] Response Code: " + response.code());
                    Log.d("StudentDateSheet", "[DEBUG] Response Message: " + response.message());
                    Log.d("StudentDateSheet", "[DEBUG] Response isSuccessful: " + response.isSuccessful());
                    Log.d("StudentDateSheet", "[DEBUG] Response body is null: " + (response.body() == null));
                    
                    // Log response headers
                    if (response.headers() != null) {
                        Log.d("StudentDateSheet", "[DEBUG] Response Headers:");
                        for (String headerName : response.headers().names()) {
                            Log.d("StudentDateSheet", "[DEBUG]   - " + headerName + ": " + response.headers().get(headerName));
                        }
                    }
                    
                    // Log response body size if available
                    String rawResponseBody = null;
                    if (response.raw() != null && response.raw().body() != null) {
                        try {
                            okhttp3.ResponseBody rawBody = response.raw().body();
                            Log.d("StudentDateSheet", "[DEBUG] Response raw body size: " + rawBody.contentLength());
                        } catch (Exception e) {
                            Log.d("StudentDateSheet", "[DEBUG] Could not get raw body size: " + e.getMessage());
                        }
                    }
                    
                    // We'll serialize the parsed response to JSON for debugging instead of reading raw body
                    // This avoids issues with response body being consumed
                    
                    Log.d("StudentDateSheet", "========================================");
                    try {
                        runOnUiThread(() -> {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                                Log.d("StudentDateSheet", "[DEBUG] Progress bar hidden");
                            }
                        });
                        
                        if (response.isSuccessful()) {
                            Log.d("StudentDateSheet", "[DEBUG] Response is successful, parsing body...");
                            
                            DateSheetResponse dateSheetResponse = response.body();
                            Log.d("StudentDateSheet", "[DEBUG] dateSheetResponse is null: " + (dateSheetResponse == null));
                            
                            // If response body is null, log error
                            if (dateSheetResponse == null) {
                                Log.e("StudentDateSheet", "[DEBUG] Response body is NULL!");
                                Log.e("StudentDateSheet", "[DEBUG] This might indicate a JSON parsing error");
                                
                                // Show error on screen for debugging
                                // Debug info removed from screen - only logged to Logcat
                                Log.e("StudentDateSheet", "[DEBUG] PARSING ERROR - Response body is NULL, HTTP Code: " + response.code());
                                runOnUiThread(() -> {
                                    Toast.makeText(StudentDateSheet.this, "Error parsing response. Please try again.", Toast.LENGTH_SHORT).show();
                                });
                                return;
                            }
                            
                            if (dateSheetResponse != null) {
                                // Build debug response text for on-screen display
                                StringBuilder debugResponse = new StringBuilder();
                                debugResponse.append("=== REQUEST ===\n");
                                debugResponse.append("URL: ").append(API.base_url).append("api.php?page=parent/load_datesheet\n");
                                debugResponse.append("Method: POST\n\n");
                                debugResponse.append("Request Parameters:\n");
                                debugResponse.append("  campus_id: ").append(campusId).append("\n");
                                debugResponse.append("  student_id: ").append(selectedChildId).append("\n");
                                if (selectedExamSession != null && !selectedExamSession.isEmpty()) {
                                    debugResponse.append("  exam_session_id: ").append(selectedExamSession).append("\n");
                                }
                                if (selectedStudentClassId != null && !selectedStudentClassId.isEmpty()) {
                                    debugResponse.append("  student_class_id: ").append(selectedStudentClassId).append("\n");
                                }
                                debugResponse.append("\n=== RESPONSE ===\n\n");
                                debugResponse.append("HTTP Code: ").append(response.code()).append("\n");
                                debugResponse.append("Status: ").append(response.isSuccessful() ? "SUCCESS" : "FAILED").append("\n");
                                
                                // Serialize response to JSON for debugging
                                try {
                                    com.google.gson.Gson gson = new com.google.gson.Gson();
                                    String jsonResponse = gson.toJson(dateSheetResponse);
                                    debugResponse.append("\nResponse JSON (first 500 chars):\n");
                                    debugResponse.append(jsonResponse.substring(0, Math.min(500, jsonResponse.length())));
                                    if (jsonResponse.length() > 500) {
                                        debugResponse.append("...");
                                    }
                                    debugResponse.append("\n\n");
                                } catch (Exception e) {
                                    debugResponse.append("\nCould not serialize response: ").append(e.getMessage()).append("\n\n");
                                }
                                
                                // Log all fields in the response
                                Log.d("StudentDateSheet", "========================================");
                                Log.d("StudentDateSheet", "[DEBUG] COMPLETE RESPONSE STRUCTURE:");
                                Log.d("StudentDateSheet", "[DEBUG] dateSheetResponse.status: " + (dateSheetResponse.status != null ? "NOT NULL" : "NULL"));
                                Log.d("StudentDateSheet", "[DEBUG] dateSheetResponse.data: " + (dateSheetResponse.data != null ? "NOT NULL (size: " + dateSheetResponse.data.size() + ")" : "NULL"));
                                Log.d("StudentDateSheet", "[DEBUG] dateSheetResponse.images: " + (dateSheetResponse.images != null ? "NOT NULL (size: " + dateSheetResponse.images.size() + ")" : "NULL"));
                                Log.d("StudentDateSheet", "[DEBUG] dateSheetResponse.files: " + (dateSheetResponse.files != null ? "NOT NULL (size: " + dateSheetResponse.files.size() + ")" : "NULL"));
                                Log.d("StudentDateSheet", "[DEBUG] dateSheetResponse.header_footer: " + (dateSheetResponse.header_footer != null ? "NOT NULL" : "NULL"));
                                
                                if (dateSheetResponse.status != null) {
                                    Log.d("StudentDateSheet", "[DEBUG] API SharedStatus Code: " + dateSheetResponse.status.getCode());
                                    Log.d("StudentDateSheet", "[DEBUG] API SharedStatus Message: " + dateSheetResponse.status.getMessage());
                                    
                                    debugResponse.append("API Status Code: ").append(dateSheetResponse.status.getCode()).append("\n");
                                    debugResponse.append("API Message: ").append(dateSheetResponse.status.getMessage()).append("\n\n");
                                    
                                    if ("1000".equals(dateSheetResponse.status.getCode())) {
                                        Log.d("StudentDateSheet", "[DEBUG] Status code is 1000 (success), extracting data...");
                                        list = dateSheetResponse.data;
                                        
                                        debugResponse.append("=== DATA ===\n");
                                        debugResponse.append("Data Items: ").append(list != null ? list.size() : 0).append("\n");
                                        debugResponse.append("Images: ").append(dateSheetResponse.images != null ? dateSheetResponse.images.size() : 0).append("\n");
                                        debugResponse.append("Files: ").append(dateSheetResponse.files != null ? dateSheetResponse.files.size() : 0).append("\n\n");
                                        
                                        // Check other fields for data
                                        if (list == null || list.isEmpty()) {
                                            Log.w("StudentDateSheet", "[DEBUG] data field is empty, checking images and files...");
                                            debugResponse.append("⚠️ Data list is EMPTY\n");
                                            if (dateSheetResponse.images != null && !dateSheetResponse.images.isEmpty()) {
                                                Log.w("StudentDateSheet", "[DEBUG] Found " + dateSheetResponse.images.size() + " images in response");
                                                debugResponse.append("Found ").append(dateSheetResponse.images.size()).append(" images\n");
                                            }
                                            if (dateSheetResponse.files != null && !dateSheetResponse.files.isEmpty()) {
                                                Log.w("StudentDateSheet", "[DEBUG] Found " + dateSheetResponse.files.size() + " files in response");
                                                debugResponse.append("Found ").append(dateSheetResponse.files.size()).append(" files\n");
                                            }
                                        }
                                        
                                        // Log the data received
                                        Log.d("StudentDateSheet", "[DEBUG] dateSheetResponse.data is null: " + (dateSheetResponse.data == null));
                                        Log.d("StudentDateSheet", "[DEBUG] Data received: " + (list != null ? list.size() : "null") + " items");
                                        
                                        // If list is null, initialize it
                                        if (list == null) {
                                            Log.w("StudentDateSheet", "[DEBUG] Response data is null, initializing empty list");
                                            list = new ArrayList<>();
                                        } else {
                                            Log.d("StudentDateSheet", "[DEBUG] ===== DATA ITEMS DETAILS =====");
                                            Log.d("StudentDateSheet", "[DEBUG] Data list has " + list.size() + " items");
                                            
                                            // Log each item in detail and add to debug text
                                            debugResponse.append("=== ITEMS ===\n");
                                            for (int i = 0; i < Math.min(list.size(), 5); i++) {
                                                DateSheetData item = list.get(i);
                                                if (item != null) {
                                                    Log.d("StudentDateSheet", "[DEBUG] Item " + i + ":");
                                                    Log.d("StudentDateSheet", "[DEBUG]   - unique_id: " + item.unique_id);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - full_name: " + item.full_name);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - subject: " + item.subject);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - staff: " + item.staff);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - created_date: " + item.created_date);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - start_time: " + item.start_time);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - end_time: " + item.end_time);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - syllabus: " + item.syllabus);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - student_class_id: " + item.student_class_id);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - exam_session_id: " + item.exam_session_id);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - class_name: " + item.class_name);
                                                    Log.d("StudentDateSheet", "[DEBUG]   - exam_session_name: " + item.exam_session_name);
                                                    
                                                    // Add to debug text
                                                    debugResponse.append("\nItem ").append(i + 1).append(":\n");
                                                    debugResponse.append("  Subject: ").append(item.subject != null ? item.subject : "N/A").append("\n");
                                                    debugResponse.append("  Date: ").append(item.created_date != null ? item.created_date : "N/A").append("\n");
                                                    debugResponse.append("  Time: ").append(item.start_time != null ? item.start_time : "N/A");
                                                    if (item.end_time != null) {
                                                        debugResponse.append(" - ").append(item.end_time);
                                                    }
                                                    debugResponse.append("\n");
                                                    debugResponse.append("  Syllabus: ").append(item.syllabus != null ? item.syllabus : "N/A").append("\n");
                                                } else {
                                                    Log.w("StudentDateSheet", "[DEBUG] Item " + i + " is NULL");
                                                    debugResponse.append("\nItem ").append(i + 1).append(": NULL\n");
                                                }
                                            }
                                            if (list.size() > 5) {
                                                Log.d("StudentDateSheet", "[DEBUG] ... and " + (list.size() - 5) + " more items");
                                                debugResponse.append("\n... and ").append(list.size() - 5).append(" more items");
                                            }
                                            Log.d("StudentDateSheet", "========================================");
                                        }
                                        
                                        // Debug info removed from screen - only logged to Logcat
                                        Log.d("StudentDateSheet", "[DEBUG] Date sheet response: " + debugResponse.toString());
                                    
                                        // Filter data to only show items matching selected student and class
                                        Log.d("StudentDateSheet", "[DEBUG] ===== FILTERING DATE SHEET DATA =====");
                                        Log.d("StudentDateSheet", "[DEBUG] Original data count: " + (list != null ? list.size() : 0));
                                        Log.d("StudentDateSheet", "[DEBUG] Selected student_id: " + selectedChildId);
                                        Log.d("StudentDateSheet", "[DEBUG] Selected student_class_id: " + selectedStudentClassId);
                                        Log.d("StudentDateSheet", "[DEBUG] Selected exam_session_id: " + selectedExamSession);
                                        
                                        if (list != null && !list.isEmpty()) {
                                            // STRICT FILTERING: Only show data if student_class_id from profile matches date sheet class_id
                                            Log.d("StudentDateSheet", "[DEBUG] ===== STRICT CLASS ID FILTERING =====");
                                            Log.d("StudentDateSheet", "[DEBUG] Original list size from API: " + list.size());
                                            Log.d("StudentDateSheet", "[DEBUG] Student profile class_id: " + selectedStudentClassId);
                                            
                                            // Filter by student_class_id from student profile
                                            List<DateSheetData> filteredList = new ArrayList<>();
                                            int filteredOutCount = 0;
                                            
                                            for (DateSheetData item : list) {
                                                boolean matchesClass = false;
                                                
                                                // Check if item's class_id matches student profile's class_id
                                                if (item.student_class_id != null && !item.student_class_id.isEmpty()) {
                                                    if (selectedStudentClassId != null && !selectedStudentClassId.isEmpty()) {
                                                        if (item.student_class_id.equals(selectedStudentClassId)) {
                                                            matchesClass = true;
                                                            Log.d("StudentDateSheet", "[DEBUG] ✅ Item matches class_id: " + item.student_class_id + " (Subject: " + item.subject + ")");
                                                        } else {
                                                            Log.d("StudentDateSheet", "[DEBUG] ❌ Item class_id mismatch: " + item.student_class_id + " (expected: " + selectedStudentClassId + ", Subject: " + item.subject + ")");
                                                        }
                                                    }
                                                } else {
                                                    Log.d("StudentDateSheet", "[DEBUG] ⚠️ Item has null/empty class_id (Subject: " + (item.subject != null ? item.subject : "N/A") + ")");
                                                }
                                                
                                                if (matchesClass) {
                                                    filteredList.add(item);
                                                } else {
                                                    filteredOutCount++;
                                                }
                                            }
                                            
                                            Log.d("StudentDateSheet", "[DEBUG] Filtered result: " + filteredList.size() + " items match, " + filteredOutCount + " items filtered out");
                                            
                                            // Replace list with filtered list
                                            list = filteredList;
                                            
                                            if (list.isEmpty()) {
                                                Log.w("StudentDateSheet", "[DEBUG] ⚠️ No items match student profile class_id: " + selectedStudentClassId);
                                                Log.w("StudentDateSheet", "[DEBUG] This means the student's class has changed or the date sheet is for a different class");
                                            }
                                            
                                            Log.d("StudentDateSheet", "[DEBUG] ===== STRICT FILTERING END =====");
                                        }
                                        
                                        // Validate the response data
                                        Log.d("StudentDateSheet", "[DEBUG] Validating date sheet response...");
                                        validateDateSheetResponse(list);
                                        
                                        // Handle images - only show if filtered data is available
                                        if (list != null && !list.isEmpty()) {
                                            // Data is available after filtering, check if images should be shown
                                            if (dateSheetResponse.images != null && !dateSheetResponse.images.isEmpty()) {
                                                Log.d("StudentDateSheet", "[DEBUG] Found " + dateSheetResponse.images.size() + " images in date sheet response - showing images");
                                                displayDateSheetImages(dateSheetResponse.images);
                                            } else {
                                                Log.d("StudentDateSheet", "[DEBUG] No images in date sheet response");
                                                hideDateSheetImages();
                                            }
                                            
                                            // Mark that data loaded successfully to prevent toast
                                            dataLoadedSuccessfully = true;
                                            Log.d("StudentDateSheet", "[DEBUG] Data list is not empty, preparing to display...");
                                        } else {
                                            // No data available after filtering, hide images
                                            Log.d("StudentDateSheet", "[DEBUG] No data available after filtering - hiding images");
                                            hideDateSheetImages();
                                        }
                                        
                                        if (list != null && !list.isEmpty()) {
                                            // Data available, display it
                                            DateSheetData firstItem = list.get(0);
                                            Log.d("StudentDateSheet", "[DEBUG] First item details:");
                                            Log.d("StudentDateSheet", "[DEBUG]   - full_name: " + (firstItem != null ? firstItem.full_name : "null"));
                                            Log.d("StudentDateSheet", "[DEBUG]   - subject: " + (firstItem != null ? firstItem.subject : "null"));
                                            Log.d("StudentDateSheet", "[DEBUG]   - start_time: " + (firstItem != null ? firstItem.start_time : "null"));
                                        
                                        // Log the API response class information
                                        Log.d("StudentDateSheet", "=== API RESPONSE ===");
                                        Log.d("StudentDateSheet", "First item class name from API: " + (firstItem != null ? firstItem.class_name : "N/A"));
                                        Log.d("StudentDateSheet", "First item class ID from API: " + (firstItem != null ? firstItem.student_class_id : "N/A"));
                                        Log.d("StudentDateSheet", "Selected student class name: " + (student != null ? student.getClassName() : "N/A"));
                                        Log.d("StudentDateSheet", "Selected student class ID: " + selectedStudentClassId);
                                        
                                        // Always use the selected student's class information for consistency
                                        // The API might return different class information based on the exam session
                                        if (student != null && student.getClassName() != null) {
                                            Log.d("StudentDateSheet", "Using student's class name: " + student.getClassName());
                                        } else {
                                            Log.d("StudentDateSheet", "No student class name available");
                                        }
                                        
                                        // Update header based on whether it's syllabus or timetable
                                    if (firstItem != null && firstItem.start_time == null) {
                                        Log.d("StudentDateSheet", "Setting header to Test Syllabus");
                                        } else {
                                            Log.d("StudentDateSheet", "Setting header to Time");
                                    }
                                        
                                            Log.d("StudentDateSheet", "[DEBUG] ===== UPDATING UI WITH DATA =====");
                                            Log.d("StudentDateSheet", "[DEBUG] Data list size AFTER filtering: " + list.size());
                                            Log.d("StudentDateSheet", "[DEBUG] RecyclerView is null: " + (timetableRcv == null));
                                            
                                            // Check if list is empty after filtering
                                            if (list == null || list.isEmpty()) {
                                                Log.w("StudentDateSheet", "[DEBUG] ⚠️ WARNING: List is empty after filtering! All items were filtered out.");
                                                Log.w("StudentDateSheet", "[DEBUG] This might be because exam_session_id doesn't match. Trying without exam_session filter...");
                                                
                                                // Try again without exam_session filter - maybe the exam_session_id changed
                                                if (selectedExamSession != null && !selectedExamSession.isEmpty()) {
                                                    Log.d("StudentDateSheet", "[DEBUG] Retrying with original list without exam_session filter...");
                                                    List<DateSheetData> relaxedFilterList = new ArrayList<>();
                                                    for (DateSheetData item : dateSheetResponse.data) {
                                                        // Only filter by student_id if available, accept all class_ids
                                                        boolean include = true;
                                                        if (item.unique_id != null && !item.unique_id.isEmpty() && selectedChildId != null && !selectedChildId.isEmpty()) {
                                                            if (!item.unique_id.equals(selectedChildId)) {
                                                                include = false;
                                                            }
                                                        }
                                                        if (include) {
                                                            relaxedFilterList.add(item);
                                                        }
                                                    }
                                                    
                                                    if (!relaxedFilterList.isEmpty()) {
                                                        Log.d("StudentDateSheet", "[DEBUG] Found " + relaxedFilterList.size() + " items with relaxed filtering");
                                                        list = relaxedFilterList;
                                                    } else {
                                                        runOnUiThread(() -> {
                                                            if (tvTotalRecords != null) {
                                                                tvTotalRecords.setText("Total Records: 0");
                                                                tvTotalRecords.setVisibility(View.VISIBLE);
                                                            }
                                                            Toast.makeText(StudentDateSheet.this, 
                                                                "No data available for the selected exam session.", 
                                                                Toast.LENGTH_SHORT).show();
                                                        });
                                                        return;
                                                    }
                                                } else {
                                                    runOnUiThread(() -> {
                                                        if (tvTotalRecords != null) {
                                                            tvTotalRecords.setText("Total Records: 0");
                                                            tvTotalRecords.setVisibility(View.VISIBLE);
                                                        }
                                                        Toast.makeText(StudentDateSheet.this, 
                                                            "No data available.", 
                                                            Toast.LENGTH_SHORT).show();
                                                    });
                                                    return;
                                                }
                                            }
                                            
                                            runOnUiThread(() -> {
                                                Log.d("StudentDateSheet", "[DEBUG] Running on UI thread: " + (Looper.getMainLooper() == Looper.myLooper()));
                                                Log.d("StudentDateSheet", "[DEBUG] Setting tvTotalRecords visibility...");
                                                // Show total records count (FILTERED list size)
                                                if (tvTotalRecords != null) {
                                                    tvTotalRecords.setText("Total Records: " + list.size());
                                                    tvTotalRecords.setVisibility(View.VISIBLE);
                                                    Log.d("StudentDateSheet", "[DEBUG] Total records TextView set to: " + list.size() + " (filtered)");
                                                } else {
                                                    Log.w("StudentDateSheet", "[DEBUG] tvTotalRecords is null!");
                                                }
                                                
                                                Log.d("StudentDateSheet", "[DEBUG] Setting RecyclerView adapter with " + list.size() + " items...");
                                                if (timetableRcv != null) {
                                                    try {
                                                        // Ensure RecyclerView is visible
                                                        timetableRcv.setVisibility(View.VISIBLE);
                                                        Log.d("StudentDateSheet", "[DEBUG] RecyclerView visibility set to VISIBLE");
                                                        
                                                        // Ensure LayoutManager is set
                                                        if (timetableRcv.getLayoutManager() == null) {
                                                            Log.w("StudentDateSheet", "[DEBUG] LayoutManager is null, setting LinearLayoutManager...");
                                                            timetableRcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(StudentDateSheet.this));
                                                        }
                                                        Log.d("StudentDateSheet", "[DEBUG] LayoutManager: " + (timetableRcv.getLayoutManager() != null ? "SET" : "NULL"));
                                                        
                                                        // Create and set adapter
                                                        Log.d("StudentDateSheet", "[DEBUG] Creating adapter with " + list.size() + " items...");
                                                        StudentDateSheetAdaptor adapter = new StudentDateSheetAdaptor(list, StudentDateSheet.this);
                                                        Log.d("StudentDateSheet", "[DEBUG] Adapter created, item count: " + adapter.getItemCount());
                                                        
                                                        timetableRcv.setAdapter(adapter);
                                                        Log.d("StudentDateSheet", "[DEBUG] RecyclerView adapter set successfully");
                                                        
                                                        // Update header syllabus column width to match data
                                                        updateHeaderSyllabusWidth(list);
                                                        
                                                        // Force entire table to remeasure after adapter is set
                                                        forceTableRemeasure(timetableRcv);
                                                        Log.d("StudentDateSheet", "[DEBUG] Adapter item count: " + adapter.getItemCount());
                                                        Log.d("StudentDateSheet", "[DEBUG] List size passed to adapter: " + list.size());
                                                        Log.d("StudentDateSheet", "[DEBUG] RecyclerView adapter: " + (timetableRcv.getAdapter() != null ? "SET" : "NULL"));
                                                        
                                                        // Verify adapter has data
                                                        if (adapter.getItemCount() == 0) {
                                                            Log.e("StudentDateSheet", "[DEBUG] ⚠️ WARNING: Adapter item count is 0 but list size is " + list.size());
                                                            Log.e("StudentDateSheet", "[DEBUG] This means the adapter is not receiving the data correctly!");
                                                        } else {
                                                            Log.d("StudentDateSheet", "[DEBUG] ✅ Adapter has " + adapter.getItemCount() + " items - should display correctly");
                                                        }
                                                        
                                                        // Notify adapter of data change
                                                        adapter.notifyDataSetChanged();
                                                        Log.d("StudentDateSheet", "[DEBUG] Adapter notified of data change");
                                                        
                                                        // Force layout update
                                                        timetableRcv.requestLayout();
                                                        timetableRcv.invalidate();
                                                        timetableRcv.post(() -> {
                                                            Log.d("StudentDateSheet", "[DEBUG] RecyclerView post - child count: " + timetableRcv.getChildCount());
                                                            Log.d("StudentDateSheet", "[DEBUG] RecyclerView post - adapter item count: " + (timetableRcv.getAdapter() != null ? timetableRcv.getAdapter().getItemCount() : 0));
                                                        });
                                                        Log.d("StudentDateSheet", "[DEBUG] RecyclerView layout requested and invalidated");
                                                        
                                                        // Also ensure the parent date_sheet_table is visible
                                                        android.view.View dateSheetTable = findViewById(R.id.date_sheet_table);
                                                        if (dateSheetTable != null) {
                                                            dateSheetTable.setVisibility(View.VISIBLE);
                                                            Log.d("StudentDateSheet", "[DEBUG] date_sheet_table visibility set to VISIBLE");
                                                        } else {
                                                            Log.w("StudentDateSheet", "[DEBUG] date_sheet_table view is NULL!");
                                                        }
                                                        
                                                        // Check RecyclerView dimensions
                                                        timetableRcv.post(() -> {
                                                            Log.d("StudentDateSheet", "[DEBUG] RecyclerView dimensions - Width: " + timetableRcv.getWidth() + ", Height: " + timetableRcv.getHeight());
                                                            Log.d("StudentDateSheet", "[DEBUG] RecyclerView visibility: " + (timetableRcv.getVisibility() == View.VISIBLE ? "VISIBLE" : "NOT VISIBLE"));
                                                        });
                                                    } catch (Exception adapterException) {
                                                        Log.e("StudentDateSheet", "[DEBUG] Error setting adapter", adapterException);
                                                        adapterException.printStackTrace();
                                                    }
                                                } else {
                                                    Log.e("StudentDateSheet", "[DEBUG] RecyclerView is null! Cannot set adapter");
                                                    // Try to re-initialize RecyclerView
                                                    android.view.View dateSheetTable = findViewById(R.id.date_sheet_table);
                                                    if (dateSheetTable != null) {
                                                        timetableRcv = dateSheetTable.findViewById(R.id.date_sheet_rcv);
                                                        if (timetableRcv != null) {
                                                            Log.d("StudentDateSheet", "[DEBUG] RecyclerView re-initialized, setting adapter...");
                                                            StudentDateSheetAdaptor adapter = new StudentDateSheetAdaptor(list, StudentDateSheet.this);
                                                            timetableRcv.setAdapter(adapter);
                                                            // Update header syllabus column width to match data
                                                            updateHeaderSyllabusWidth(list);
                                                            adapter.notifyDataSetChanged();
                                                            
                                                            // Force entire table to remeasure after adapter is set
                                                            forceTableRemeasure(timetableRcv);
                                                        }
                                                    }
                                                }
                                                
                                                // SMS button is always visible - no need to set visibility
                                                Log.d("StudentDateSheet", "[DEBUG] SMS button should be visible");
                                                
                                                Log.d("StudentDateSheet", "[DEBUG] UI update complete");
                                            });
                                            
                                            // Generate SMS text for sharing
                                            Log.d("StudentDateSheet", "[DEBUG] Generating SMS text...");
                                            dateSheetSms = generateDateSheetSms();
                                            Log.d("StudentDateSheet", "[DEBUG] UI update complete!");
                                        } else {
                                            // No data available - this might be because student_class_id filter is too strict
                                            // Try alternative call without student_class_id, then filter client-side
                                            Log.w("StudentDateSheet", "[DEBUG] Data list is empty or null - trying alternative approach without student_class_id");
                                            Log.w("StudentDateSheet", "[DEBUG] This might be because backend filtering with student_class_id is too strict");
                                            runOnUiThread(() -> {
                                                // Hide total records display
                                                if (tvTotalRecords != null) {
                                                    tvTotalRecords.setVisibility(View.GONE);
                                                }
                                        // SMS button always visible - no need to hide
                                                if (timetableRcv != null) {
                                                    timetableRcv.setAdapter(new StudentDateSheetAdaptor(new ArrayList<>(), StudentDateSheet.this));
                                                }
                                            });
                                            // Try without student_class_id - the alternative call will filter client-side
                                            tryAlternativeDateSheetCall();
                                        }
                                    } else {
                                        // Status code is not 1000
                                        Log.w("StudentDateSheet", "[DEBUG] Status code is NOT 1000: " + dateSheetResponse.status.getCode());
                                        // API returned error, try alternative approach
                                        String errorMessage = dateSheetResponse.status.getMessage();
                                        Log.d("StudentDateSheet", "[DEBUG] API returned error: " + errorMessage);
                                        
                                        // Show error in debug text
                                        StringBuilder statusErrorDebug = new StringBuilder();
                                        statusErrorDebug.append("=== REQUEST ===\n");
                                        statusErrorDebug.append("URL: ").append(API.base_url).append("api.php?page=parent/load_datesheet\n");
                                        statusErrorDebug.append("Method: POST\n\n");
                                        statusErrorDebug.append("Request Parameters:\n");
                                        statusErrorDebug.append("  campus_id: ").append(campusId).append("\n");
                                        statusErrorDebug.append("  student_id: ").append(selectedChildId).append("\n");
                                        if (selectedExamSession != null && !selectedExamSession.isEmpty()) {
                                            statusErrorDebug.append("  exam_session_id: ").append(selectedExamSession).append("\n");
                                        }
                                        if (selectedStudentClassId != null && !selectedStudentClassId.isEmpty()) {
                                            statusErrorDebug.append("  student_class_id: ").append(selectedStudentClassId).append("\n");
                                        }
                                        statusErrorDebug.append("\n=== API ERROR ===\n\n");
                                        statusErrorDebug.append("HTTP Code: ").append(response.code()).append("\n");
                                        statusErrorDebug.append("API Status Code: ").append(dateSheetResponse.status.getCode()).append("\n");
                                        statusErrorDebug.append("API Message: ").append(errorMessage != null ? errorMessage : "No message").append("\n");
                                        
                                        final String statusErrorDebugText = statusErrorDebug.toString();

                                    // Try alternative approach if it's a data not found error (without student_class_id)
                                    if (errorMessage != null && (errorMessage.contains("not found") || errorMessage.contains("no data"))) {
                                        Log.d("StudentDateSheet", "Trying alternative call without student_class_id parameter");
                                        tryAlternativeDateSheetCall();
                            } else {
                                        // Debug info removed from screen - only logged to Logcat
                                        Log.e("StudentDateSheet", "[DEBUG] Date sheet API error: " + statusErrorDebugText);
                                        runOnUiThread(() -> {
                                            if (errorMessage != null && !errorMessage.isEmpty()) {
                                                Toast.makeText(StudentDateSheet.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(StudentDateSheet.this, "No date sheet data found", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                            }
                                } else {
                                    Log.e("StudentDateSheet", "[DEBUG] dateSheetResponse.status is NULL!");
                                    
                                    // Show error in debug text
                                    StringBuilder nullStatusDebug = new StringBuilder();
                                    nullStatusDebug.append("=== REQUEST ===\n");
                                    nullStatusDebug.append("URL: ").append(API.base_url).append("api.php?page=parent/load_datesheet\n");
                                    nullStatusDebug.append("Method: POST\n\n");
                                    nullStatusDebug.append("Request Parameters:\n");
                                    nullStatusDebug.append("  campus_id: ").append(campusId).append("\n");
                                    nullStatusDebug.append("  student_id: ").append(selectedChildId).append("\n");
                                    if (selectedExamSession != null && !selectedExamSession.isEmpty()) {
                                        nullStatusDebug.append("  exam_session_id: ").append(selectedExamSession).append("\n");
                                    }
                                    if (selectedStudentClassId != null && !selectedStudentClassId.isEmpty()) {
                                        nullStatusDebug.append("  student_class_id: ").append(selectedStudentClassId).append("\n");
                                    }
                                    nullStatusDebug.append("\n=== ERROR ===\n\n");
                                    nullStatusDebug.append("ERROR: Response status is NULL\n");
                                    nullStatusDebug.append("HTTP Code: ").append(response.code()).append("\n");
                                    
                                    final String nullStatusDebugText = nullStatusDebug.toString();
                                    // Debug info removed from screen - only logged to Logcat
                                    Log.e("StudentDateSheet", "[DEBUG] Date sheet null status error: " + nullStatusDebugText);
                                }
                                } else {
                                    Log.e("StudentDateSheet", "[DEBUG] dateSheetResponse is NULL!");
                                    Log.d("StudentDateSheet", "[DEBUG] Invalid response structure");
                                    runOnUiThread(() -> Toast.makeText(StudentDateSheet.this, "Invalid response from server", Toast.LENGTH_SHORT).show());
                                }
                        } else {
                            // HTTP error
                            String errorMessage = "Server error: " + response.code();
                            Log.e("StudentDateSheet", "[DEBUG] HTTP Error: " + errorMessage);
                            Log.e("StudentDateSheet", "[DEBUG] Response not successful!");
                            
                            // Show error in debug text
                            StringBuilder errorDebug = new StringBuilder();
                            errorDebug.append("=== HTTP ERROR ===\n\n");
                            errorDebug.append("HTTP Code: ").append(response.code()).append("\n");
                            errorDebug.append("Message: ").append(response.message()).append("\n");
                            
                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    Log.e("StudentDateSheet", "Error response: " + errorBody);
                                    errorDebug.append("\nError Body:\n").append(errorBody);
                                } catch (Exception e) {
                                    Log.e("StudentDateSheet", "Error reading error body", e);
                                    errorDebug.append("\nCould not read error body: ").append(e.getMessage());
                                }
                            }
                            
                            final String errorDebugText = errorDebug.toString();
                            // Debug info removed from screen - only logged to Logcat
                            Log.e("StudentDateSheet", "[DEBUG] Date sheet HTTP error: " + errorDebugText);
                            runOnUiThread(() -> {
                                Toast.makeText(StudentDateSheet.this, errorMessage, Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        Log.e("StudentDateSheet", "Error in loadDateSheet onResponse", e);
                        runOnUiThread(() -> Toast.makeText(StudentDateSheet.this, "Error processing date sheet data", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onFailure(Call<DateSheetResponse> call, Throwable e) {
                    Log.e("StudentDateSheet", "========================================");
                    Log.e("StudentDateSheet", "[DEBUG] ===== API CALL FAILED =====");
                    Log.e("StudentDateSheet", "[DEBUG] Thread: " + Thread.currentThread().getName());
                    Log.e("StudentDateSheet", "[DEBUG] Error Type: " + e.getClass().getName());
                    Log.e("StudentDateSheet", "[DEBUG] Error Message: " + e.getMessage());
                    Log.e("StudentDateSheet", "[DEBUG] Error Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
                    
                    // Build error debug text
                    StringBuilder errorDebug = new StringBuilder();
                    errorDebug.append("=== REQUEST ===\n");
                    errorDebug.append("URL: ").append(API.base_url).append("api.php?page=parent/load_datesheet\n");
                    errorDebug.append("Method: POST\n\n");
                    errorDebug.append("Request Parameters:\n");
                    errorDebug.append("  campus_id: ").append(campusId).append("\n");
                    errorDebug.append("  student_id: ").append(selectedChildId).append("\n");
                    if (selectedExamSession != null && !selectedExamSession.isEmpty()) {
                        errorDebug.append("  exam_session_id: ").append(selectedExamSession).append("\n");
                    }
                    if (selectedStudentClassId != null && !selectedStudentClassId.isEmpty()) {
                        errorDebug.append("  student_class_id: ").append(selectedStudentClassId).append("\n");
                    }
                    errorDebug.append("\n=== API CALL FAILED ===\n\n");
                    errorDebug.append("Error Type: ").append(e.getClass().getSimpleName()).append("\n");
                    errorDebug.append("Error Message: ").append(e.getMessage() != null ? e.getMessage() : "Unknown error").append("\n");
                    if (e.getCause() != null) {
                        errorDebug.append("Cause: ").append(e.getCause().getMessage()).append("\n");
                    }
                    
                    // Check if it's a network error
                    if (e instanceof java.net.UnknownHostException) {
                        Log.e("StudentDateSheet", "[DEBUG] Network Error: Unknown Host - Check internet connection");
                        errorDebug.append("\nNetwork Error: Unknown Host\nCheck internet connection");
                    } else if (e instanceof java.net.SocketTimeoutException) {
                        Log.e("StudentDateSheet", "[DEBUG] Network Error: Connection Timeout");
                        errorDebug.append("\nNetwork Error: Connection Timeout");
                    } else if (e instanceof java.io.IOException) {
                        Log.e("StudentDateSheet", "[DEBUG] Network Error: IO Exception - " + e.getMessage());
                        errorDebug.append("\nNetwork Error: IO Exception\n").append(e.getMessage());
                    } else if (e instanceof retrofit2.HttpException) {
                        retrofit2.HttpException httpException = (retrofit2.HttpException) e;
                        Log.e("StudentDateSheet", "[DEBUG] HTTP Error Code: " + httpException.code());
                        Log.e("StudentDateSheet", "[DEBUG] HTTP Error Message: " + httpException.message());
                        errorDebug.append("\nHTTP Error Code: ").append(httpException.code()).append("\n");
                        errorDebug.append("HTTP Error Message: ").append(httpException.message()).append("\n");
                        try {
                            if (httpException.response() != null && httpException.response().errorBody() != null) {
                                String errorBody = httpException.response().errorBody().string();
                                Log.e("StudentDateSheet", "[DEBUG] HTTP Error Body: " + errorBody);
                                errorDebug.append("\nError Body:\n").append(errorBody);
                            }
                        } catch (Exception ex) {
                            Log.e("StudentDateSheet", "[DEBUG] Could not read error body: " + ex.getMessage());
                            errorDebug.append("\nCould not read error body: ").append(ex.getMessage());
                        }
                    }
                    
                    // Log stack trace
                    Log.e("StudentDateSheet", "[DEBUG] Stack Trace:");
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    for (int i = 0; i < Math.min(stackTrace.length, 15); i++) {
                        Log.e("StudentDateSheet", "[DEBUG]   at " + stackTrace[i].toString());
                    }
                    
                    // Check if request was executed
                    if (call != null) {
                        Log.d("StudentDateSheet", "[DEBUG] Request was executed: " + call.isExecuted());
                        Log.d("StudentDateSheet", "[DEBUG] Request was canceled: " + call.isCanceled());
                    }
                    
                    Log.e("StudentDateSheet", "========================================");
                    
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    
                    final String errorMessage;
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("timeout")) {
                            errorMessage = "Request timeout. Please check your internet connection.";
                        } else if (e.getMessage().contains("Unable to resolve host")) {
                            errorMessage = "No internet connection. Please check your network.";
                        } else {
                            errorMessage = "Network error: " + e.getMessage();
                        }
                    } else {
                        errorMessage = "Network error";
                    }
                    
                    final String errorDebugText = errorDebug.toString();
                    // Debug info removed from screen - only logged to Logcat
                    Log.e("StudentDateSheet", "[DEBUG] Date sheet failure: " + errorDebugText);
                    runOnUiThread(() -> {
                        Toast.makeText(StudentDateSheet.this, errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });
                } catch (Exception e) {
                    Log.e("StudentDateSheet", "Error in loadDateSheet thread", e);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(StudentDateSheet.this, "Error loading date sheet", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in loadDateSheet", e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error loading date sheet", Toast.LENGTH_SHORT).show();
        }
    }

    private void tryAlternativeDateSheetCall() {
        try {
            Log.d("StudentDateSheet", "=== TRYING ALTERNATIVE DATE SHEET CALL ===");
            
            // Try without student_class_id parameter
            HashMap<String, String> postParam = new HashMap<String, String>();
            String campusId = Paper.book().read("campus_id", "");
            parentId = Paper.book().read("parent_id", "");
            
            postParam.put("student_id", selectedChildId);
            postParam.put("campus_id", campusId);
            postParam.put("exam_session_id", selectedExamSession);
            
            // Add session_id parameter
            if (Constant.current_session != null && !Constant.current_session.isEmpty()) {
                postParam.put("session_id", Constant.current_session);
            }
            
            // Add parent_id parameter
            if (parentId != null && !parentId.isEmpty()) {
                postParam.put("parent_id", parentId);
            }
            
            String jsonBody = new JSONObject(postParam).toString();
            Log.d("StudentDateSheet", "Alternative JSON Body: " + jsonBody);
            
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Constant.mApiService.loadStudentDateSheet(body).enqueue(new Callback<DateSheetResponse>() {
                @Override
                public void onResponse(Call<DateSheetResponse> call, Response<DateSheetResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            DateSheetResponse dateSheetResponse = response.body();
                            if (dateSheetResponse != null && dateSheetResponse.status != null) {
                                if ("1000".equals(dateSheetResponse.status.getCode())) {
                                    list = dateSheetResponse.data;
                                    
                                    // Initialize list if null
                                    if (list == null) {
                                        Log.w("StudentDateSheet", "[DEBUG] Alternative call - Response data is null, initializing empty list");
                                        list = new ArrayList<>();
                                    }
                                    
                                    // Always apply filtering, even if list is empty
                                    Log.d("StudentDateSheet", "Alternative call - Data received: " + (list != null ? list.size() : 0) + " items");
                                    
                                    if (list != null && !list.isEmpty()) {
                                        // STRICT FILTERING: Only show data if student_class_id from profile matches date sheet class_id
                                        Log.d("StudentDateSheet", "[DEBUG] ===== STRICT CLASS ID FILTERING (Alternative Call) =====");
                                        Log.d("StudentDateSheet", "[DEBUG] Original list size from API: " + list.size());
                                        Log.d("StudentDateSheet", "[DEBUG] Student profile class_id: " + selectedStudentClassId);
                                        
                                        // Filter by student_class_id from student profile
                                        List<DateSheetData> filteredList = new ArrayList<>();
                                        int filteredOutCount = 0;
                                        
                                        for (DateSheetData item : list) {
                                            boolean matchesClass = false;
                                            
                                            // Check if item's class_id matches student profile's class_id
                                            if (item.student_class_id != null && !item.student_class_id.isEmpty()) {
                                                if (selectedStudentClassId != null && !selectedStudentClassId.isEmpty()) {
                                                    if (item.student_class_id.equals(selectedStudentClassId)) {
                                                        matchesClass = true;
                                                        Log.d("StudentDateSheet", "[DEBUG] ✅ Item matches class_id: " + item.student_class_id + " (Subject: " + item.subject + ")");
                                                    } else {
                                                        Log.d("StudentDateSheet", "[DEBUG] ❌ Item class_id mismatch: " + item.student_class_id + " (expected: " + selectedStudentClassId + ", Subject: " + item.subject + ")");
                                                    }
                                                }
                                            } else {
                                                Log.d("StudentDateSheet", "[DEBUG] ⚠️ Item has null/empty class_id (Subject: " + (item.subject != null ? item.subject : "N/A") + ")");
                                            }
                                            
                                            if (matchesClass) {
                                                filteredList.add(item);
                                            } else {
                                                filteredOutCount++;
                                            }
                                        }
                                        
                                        Log.d("StudentDateSheet", "[DEBUG] Filtered result: " + filteredList.size() + " items match, " + filteredOutCount + " items filtered out");
                                        
                                        // Replace list with filtered list
                                        list = filteredList;
                                        
                                        if (list.isEmpty()) {
                                            Log.w("StudentDateSheet", "[DEBUG] ⚠️ No items match student profile class_id: " + selectedStudentClassId);
                                            Log.w("StudentDateSheet", "[DEBUG] This means the student's class has changed or the date sheet is for a different class");
                                        }
                                        
                                        Log.d("StudentDateSheet", "[DEBUG] ===== STRICT FILTERING END =====");
                                    } else {
                                        // List was empty from API, hide images
                                        Log.d("StudentDateSheet", "[DEBUG] Alternative call - No data received from API");
                                        hideDateSheetImages();
                                    }
                                    
                                    // Handle images and display data - only if filtered data is available
                                    if (list != null && !list.isEmpty()) {
                                        // Data is available after filtering, check if images should be shown
                                        if (dateSheetResponse.images != null && !dateSheetResponse.images.isEmpty()) {
                                            Log.d("StudentDateSheet", "[DEBUG] Alternative call - Found " + dateSheetResponse.images.size() + " images - showing images");
                                            displayDateSheetImages(dateSheetResponse.images);
                                        } else {
                                            Log.d("StudentDateSheet", "[DEBUG] Alternative call - No images in response");
                                            hideDateSheetImages();
                                        }
                                        
                                        // Mark that data loaded successfully to prevent toast
                                        dataLoadedSuccessfully = true;
                                        
                                        // Display the data
                                        DateSheetData firstItem = list.get(0);
                                    if (tvClass != null && student != null && student.getClassName() != null) {
                                        tvClass.setText("Class: " + student.getClassName());
                                    }
                                    
                                    if (firstItem != null && firstItem.start_time == null) {
                                        if (tvHeaderTimeOrSyllabus != null) {
                                            tvHeaderTimeOrSyllabus.setText("Test Syllabus");
                                        }
                                    } else {
                                        if (tvHeaderTimeOrSyllabus != null) {
                                            tvHeaderTimeOrSyllabus.setText("Time");
                                        }
                                    }
                                    
                                    // Show total records count
                                    if (tvTotalRecords != null) {
                                        tvTotalRecords.setText("Total Records: " + list.size());
                                        tvTotalRecords.setVisibility(View.VISIBLE);
                                    }
                                    if (timetableRcv != null) {
                                        try {
                                            // Ensure LayoutManager is set
                                            if (timetableRcv.getLayoutManager() == null) {
                                                timetableRcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(StudentDateSheet.this));
                                            }
                                            
                                            // Ensure RecyclerView is visible
                                            timetableRcv.setVisibility(View.VISIBLE);
                                            
                                            // Create and set adapter
                                            StudentDateSheetAdaptor adapter = new StudentDateSheetAdaptor(list, StudentDateSheet.this);
                                            timetableRcv.setAdapter(adapter);
                                            adapter.notifyDataSetChanged();
                                            
                                            // Update header syllabus column width to match data
                                            updateHeaderSyllabusWidth(list);
                                            
                                            // Force entire table to remeasure after adapter is set
                                            forceTableRemeasure(timetableRcv);
                                            
                                            Log.d("StudentDateSheet", "[DEBUG] Alternative call - Adapter set with " + adapter.getItemCount() + " items");
                                            
                                            // Ensure parent view is visible
                                            android.view.View dateSheetTable = findViewById(R.id.date_sheet_table);
                                            if (dateSheetTable != null) {
                                                dateSheetTable.setVisibility(View.VISIBLE);
                                            }
                                        } catch (Exception e) {
                                            Log.e("StudentDateSheet", "[DEBUG] Error setting adapter in alternative call", e);
                                            e.printStackTrace();
                                        }
                                    }
                                    
                                    // Generate SMS text for sharing
                                    dateSheetSms = generateDateSheetSms();
                                    
                                        // SMS button is always visible - no need to set visibility
                                        Log.d("StudentDateSheet", "Alternative call successfully displayed " + list.size() + " items");
                                    } else {
                                        // No data available after filtering, hide images
                                        Log.d("StudentDateSheet", "[DEBUG] Alternative call - No data available after filtering - hiding images");
                                        hideDateSheetImages();
                                        Log.w("StudentDateSheet", "After filtering, no data matches selected student/class");
                                        showNoDataMessage();
                                    }
                                } else {
                                    Log.d("StudentDateSheet", "Alternative call failed: " + dateSheetResponse.status.getMessage());
                                    showNoDataMessage();
                                }
                            } else {
                                Log.d("StudentDateSheet", "Alternative call invalid response");
                                showNoDataMessage();
                            }
                        } else {
                            Log.d("StudentDateSheet", "Alternative call HTTP error: " + response.code());
                            showNoDataMessage();
                        }
                    } catch (Exception e) {
                        Log.e("StudentDateSheet", "Error in alternative call", e);
                        showNoDataMessage();
                    }
                }

                @Override
                public void onFailure(Call<DateSheetResponse> call, Throwable e) {
                    Log.e("StudentDateSheet", "Alternative call failed", e);
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    showNoDataMessage();
                }
            });
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in tryAlternativeDateSheetCall", e);
            Toast.makeText(StudentDateSheet.this, "No date sheet data available for the selected criteria", Toast.LENGTH_SHORT).show();
            tvTotalRecords.setVisibility(View.GONE);
        }
    }


    private void showNoDataMessage() {
        // Only show toast if data hasn't been loaded successfully
        if (!dataLoadedSuccessfully) {
            Toast.makeText(StudentDateSheet.this, "No date sheet data available for the selected criteria", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("StudentDateSheet", "Skipping toast - data already loaded successfully");
        }
        if (tvTotalRecords != null) {
            tvTotalRecords.setVisibility(View.GONE);
        }
        
        // SMS button always visible - no need to hide
        
        // Log detailed information for debugging
        Log.d("StudentDateSheet", "=== NO DATA ANALYSIS ===");
        Log.d("StudentDateSheet", "SharedStudent ID: " + selectedChildId);
        Log.d("StudentDateSheet", "SharedStudent Name: " + selectedChildName);
        Log.d("StudentDateSheet", "SharedStudent Class ID: " + selectedStudentClassId);
        Log.d("StudentDateSheet", "SharedStudent Class Name: " + (student != null ? student.getClassName() : "N/A"));
        Log.d("StudentDateSheet", "Exam Session: " + selectedExamSession);
        Log.d("StudentDateSheet", "Campus ID: " + Paper.book().read("campus_id", ""));
        Log.d("StudentDateSheet", "Parent ID: " + parentId);
        Log.d("StudentDateSheet", "Current Session: " + Constant.current_session);
        Log.d("StudentDateSheet", "All attempts failed - no data available");
    }

    private boolean validateDateSheetResponse(List<DateSheetData> dataList) {
        Log.d("StudentDateSheet", "[DEBUG] ===== VALIDATING DATE SHEET RESPONSE =====");
        try {
            if (dataList == null || dataList.isEmpty()) {
                Log.w("StudentDateSheet", "[DEBUG] Validation FAILED: Data list is null or empty");
                Log.d("StudentDateSheet", "[DEBUG]   - dataList is null: " + (dataList == null));
                Log.d("StudentDateSheet", "[DEBUG]   - dataList is empty: " + (dataList != null && dataList.isEmpty()));
                return false;
            }
            
            Log.d("StudentDateSheet", "[DEBUG] Validating " + dataList.size() + " data items");
            Log.d("StudentDateSheet", "[DEBUG] Selected Student Class ID: " + selectedStudentClassId);
            Log.d("StudentDateSheet", "[DEBUG] Selected Student ID: " + selectedChildId);
            Log.d("StudentDateSheet", "[DEBUG] Selected Exam Session ID: " + selectedExamSession);
            
            // Check if the data belongs to the selected student's class
            boolean hasMatchingClass = false;
            int matchingCount = 0;
            int nullClassCount = 0;
            int differentClassCount = 0;
            
            for (int i = 0; i < dataList.size(); i++) {
                DateSheetData item = dataList.get(i);
                if (item != null) {
                    if (item.student_class_id != null) {
                        if (item.student_class_id.equals(selectedStudentClassId)) {
                            hasMatchingClass = true;
                            matchingCount++;
                            if (matchingCount == 1) {
                                Log.d("StudentDateSheet", "[DEBUG] Found matching class ID at index " + i + ": " + item.student_class_id);
                            }
                        } else {
                            differentClassCount++;
                            if (differentClassCount == 1) {
                                Log.w("StudentDateSheet", "[DEBUG] Found different class ID at index " + i + ": " + item.student_class_id + " (expected: " + selectedStudentClassId + ")");
                            }
                        }
                    } else {
                        nullClassCount++;
                        if (nullClassCount == 1) {
                            Log.w("StudentDateSheet", "[DEBUG] Found item with null class_id at index " + i);
                        }
                    }
                } else {
                    Log.w("StudentDateSheet", "[DEBUG] Found null item at index " + i);
                }
            }
            
            Log.d("StudentDateSheet", "[DEBUG] Validation Summary:");
            Log.d("StudentDateSheet", "[DEBUG]   - Items with matching class: " + matchingCount);
            Log.d("StudentDateSheet", "[DEBUG]   - Items with different class: " + differentClassCount);
            Log.d("StudentDateSheet", "[DEBUG]   - Items with null class: " + nullClassCount);
            Log.d("StudentDateSheet", "[DEBUG]   - Has matching class: " + hasMatchingClass);
            
            if (!hasMatchingClass) {
                Log.w("StudentDateSheet", "[DEBUG] WARNING: No data found for selected student's class ID: " + selectedStudentClassId);
                // Don't return false here as the API might return data for different classes
                // Just log the warning
            }
            
            Log.d("StudentDateSheet", "[DEBUG] Validation completed successfully");
            return true;
        } catch (Exception e) {
            Log.e("StudentDateSheet", "[DEBUG] Validation FAILED with exception", e);
            Log.e("StudentDateSheet", "[DEBUG] Exception type: " + e.getClass().getName());
            Log.e("StudentDateSheet", "[DEBUG] Exception message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_datesheet_in_sms) {
            send_sms(view);
        }
    }

    public void send_sms(View view) {
        // Check if data is available
        if (list == null || list.isEmpty()) {
            Toast.makeText(this, "No date sheet data available. Please load date sheet first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if student and session are selected
        if (selectedChildName == null || selectedChildName.isEmpty()) {
            Toast.makeText(this, "Please select a student first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedExamSessionName == null || selectedExamSessionName.isEmpty()) {
            Toast.makeText(this, "Please select an exam session first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Generate SMS text right before showing popup
        dateSheetSms = generateDateSheetSms();
        
        if (dateSheetSms == null || dateSheetSms.isEmpty() || dateSheetSms.equals("No date sheet data available.")) {
            Toast.makeText(this, "No date sheet data available to share.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Send_Sms_Option(view);
    }

    private String generateDateSheetSms() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("📅 Date Sheet - ").append(selectedChildName).append("\n");
            sb.append("📚 Session: ").append(selectedExamSessionName).append("\n\n");
            
            if (list.isEmpty()) {
                sb.append("No date sheet data available.");
                return sb.toString();
            }
            
            // Format time to 24-hour format (HH:mm)
            java.text.SimpleDateFormat time24Format = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.ENGLISH);
            java.text.SimpleDateFormat time24FormatWithSeconds = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.ENGLISH);
            
            // Group by date if available, otherwise show all subjects
            for (DateSheetData data : list) {
                String subject = data.subject != null ? data.subject : "Unknown Subject";
                String date = data.created_date != null ? data.created_date : "Unknown Date";
                
                // Format start time to 24-hour format
                String startTime = "00:00";
                if (data.start_time != null) {
                    try {
                        String timeStr = data.start_time.toString().trim();
                        // Remove seconds if present
                        if (timeStr.length() > 5) {
                            String[] parts = timeStr.split(":");
                            if (parts.length >= 3) {
                                timeStr = parts[0] + ":" + parts[1];
                            }
                        }
                        // Parse and format to ensure 24-hour format
                        java.util.Date time = time24Format.parse(timeStr);
                        startTime = time24Format.format(time);
                    } catch (Exception e) {
                        // If parsing fails, try to clean the string
                        String cleaned = data.start_time.toString().trim();
                        if (cleaned.length() > 5) {
                            String[] parts = cleaned.split(":");
                            if (parts.length >= 2) {
                                startTime = parts[0] + ":" + parts[1];
                            }
                        } else {
                            startTime = cleaned;
                        }
                    }
                }
                
                // Format end time to 24-hour format
                String endTime = "00:00";
                if (data.end_time != null) {
                    try {
                        String timeStr = data.end_time.toString().trim();
                        // Remove seconds if present
                        if (timeStr.length() > 5) {
                            String[] parts = timeStr.split(":");
                            if (parts.length >= 3) {
                                timeStr = parts[0] + ":" + parts[1];
                            }
                        }
                        // Parse and format to ensure 24-hour format
                        java.util.Date time = time24Format.parse(timeStr);
                        endTime = time24Format.format(time);
                    } catch (Exception e) {
                        // If parsing fails, try to clean the string
                        String cleaned = data.end_time.toString().trim();
                        if (cleaned.length() > 5) {
                            String[] parts = cleaned.split(":");
                            if (parts.length >= 2) {
                                endTime = parts[0] + ":" + parts[1];
                            }
                        } else {
                            endTime = cleaned;
                        }
                    }
                }
                
                sb.append("📅 ").append(date).append("\n");
                sb.append("• ").append(subject);
                if (startTime != null && !startTime.equals("00:00")) {
                    sb.append(" (").append(startTime);
                    if (endTime != null && !endTime.equals("00:00")) {
                        sb.append(" - ").append(endTime);
                    }
                    sb.append(")");
                }
                sb.append("\n\n");
            }
            
            sb.append("📱 Shared from KMU Parent App");
            return sb.toString();
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error generating date sheet SMS", e);
            return "Error generating date sheet data.";
        }
    }

    private void send_timetable() {

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("message", dateSheetSms);

        progressBar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));
        Constant.mApiService.load_timetable_sms(body).enqueue(new Callback<TimetableSmsModel>() {
            @Override
            public void onResponse(Call<TimetableSmsModel> call, Response<TimetableSmsModel> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        Toast.makeText(StudentDateSheet.this, "TimeTable Send in Your Number Soon.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<TimetableSmsModel> call, Throwable e) {

                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void Send_Sms_Option(View view) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenu().add("Local SMS");
        popup.getMenu().add("WhatsApp");
        popup.getMenu().add("WhatsApp Business");
        popup.getMenu().add("Other");

        popup.setOnMenuItemClickListener(item -> {
            try {
                switch (item.getTitle().toString()) {
                    case "WhatsApp":
                        shareToWhatsApp(dateSheetSms);
                        break;
                    case "WhatsApp Business":
                        shareToWhatsAppBusiness(dateSheetSms);
                        break;
                    case "Local SMS":
                        shareViaSMS(dateSheetSms);
                        break;
                    case "Other":
                        ShareUtils.shareText(context, dateSheetSms, "SharedStudent Date Sheet");
                        break;
                }
            } catch (Exception e) {
                Log.e("StudentDateSheet", "Error sharing date sheet", e);
                // Toast removed when data is available - only log error
            }
            return true;
        });

        popup.show();
    }

    private void shareToWhatsApp(String text) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage("com.whatsapp");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent, "Share via WhatsApp"));
            // Toast removed when data is available
        } catch (Exception e) {
            Log.e("StudentDateSheet", "WhatsApp not installed or error sharing", e);
            // Only log error, no toast when data is available
        }
    }

    private void shareToWhatsAppBusiness(String text) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage("com.whatsapp.w4b");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent, "Share via WhatsApp Business"));
            // Toast removed when data is available
        } catch (Exception e) {
            Log.e("StudentDateSheet", "WhatsApp Business not installed or error sharing", e);
            // Only log error, no toast when data is available
        }
    }

    private void shareViaSMS(String text) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:"));
            intent.putExtra("sms_body", text);
            startActivity(Intent.createChooser(intent, "Send SMS"));
            // Toast removed when data is available
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error sharing via SMS", e);
            // Only log error, no toast when data is available
        }
    }

    private void clearTestData() {
        try {
            Log.d("StudentDateSheet", "Clearing test data from storage");
            
            // Clear test data from current student
            SharedStudent currentStudent = Paper.book().read("current_child_model");
            if (currentStudent != null && isTestData(currentStudent)) {
                Log.d("StudentDateSheet", "Clearing test data from current_child_model");
                Paper.book().delete("current_child_model");
            }
            
            // Clear test data from selected student
            SharedStudent selectedStudent = Paper.book().read("selected_student");
            if (selectedStudent != null && isTestData(selectedStudent)) {
                Log.d("StudentDateSheet", "Clearing test data from selected_student");
                Paper.book().delete("selected_student");
            }
            
            // Clear test data from student list
            List<SharedStudent> students = Paper.book().read("students", new ArrayList<>());
            if (students != null && !students.isEmpty()) {
                List<SharedStudent> validStudents = new ArrayList<>();
                for (SharedStudent s : students) {
                    if (s != null && !isTestData(s)) {
                        validStudents.add(s);
                    } else {
                        Log.d("StudentDateSheet", "Removing test student from list: " + (s != null ? s.getFullName() : "null"));
                    }
                }
                
                if (validStudents.size() != students.size()) {
                    Log.d("StudentDateSheet", "Updated student list: " + validStudents.size() + " valid students out of " + students.size());
                    Paper.book().write("students", validStudents);
                    studentList = validStudents;
                }
            }
            
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error clearing test data", e);
        }
    }
    
    /**
     * Apply theme based on user type (consistent with other activities)
     */
    private void applyTheme(String userType) {
        try {
            Log.d("StudentDateSheet", "Applying theme for userType: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
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
                
                // Force dark navigation bar icons (prevent light appearance)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // 0 = do NOT use light icons
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }
                
                Log.d("StudentDateSheet", "Applied STUDENT theme (teal)");
            }
            
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error applying theme, using parent theme as fallback", e);
        }
    }
    
    /**
     * Apply table header theme based on user type
     * Sets the date sheet table header background color to match the theme
     */
    private void applyTableHeaderTheme(String userType) {
        try {
            // Find the table header layout
            android.view.View tableHeaderView = findViewById(R.id.header_rv_date_sheet);
            if (tableHeaderView == null) {
                // Try to find it in the included layout
                android.view.View dateSheetTable = findViewById(R.id.date_sheet_table);
                if (dateSheetTable != null) {
                    tableHeaderView = dateSheetTable.findViewById(R.id.header_rv_date_sheet);
                }
            }
            
            if (tableHeaderView != null) {
                int headerColor;
                if (userType != null && userType.equals("STUDENT")) {
                    // Student theme - teal
                    headerColor = ContextCompat.getColor(this, R.color.student_primary);
                    Log.d("StudentDateSheet", "Applied STUDENT theme (teal) to date sheet table header");
                } else {
                    // Parent theme - dark brown
                    headerColor = ContextCompat.getColor(this, R.color.dark_brown);
                    Log.d("StudentDateSheet", "Applied PARENT theme (dark brown) to date sheet table header");
                }
                
                tableHeaderView.setBackgroundColor(headerColor);
            } else {
                Log.w("StudentDateSheet", "Table header view not found - cannot apply theme");
            }
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error applying table header theme", e);
        }
    }
    
    /**
     * Force entire table to remeasure all rows for dynamic height adjustment
     */
    private void forceTableRemeasure(androidx.recyclerview.widget.RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        }
        
        try {
            // Post to ensure this runs after layout
            recyclerView.post(() -> {
                try {
                    // Force all visible items to remeasure
                    androidx.recyclerview.widget.RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int childCount = recyclerView.getChildCount();
                        for (int i = 0; i < childCount; i++) {
                            View child = recyclerView.getChildAt(i);
                            if (child != null) {
                                child.requestLayout();
                                child.invalidate();
                            }
                        }
                    }
                    
                    // Force RecyclerView to remeasure
                    recyclerView.requestLayout();
                    recyclerView.invalidate();
                    
                    // Notify adapter to refresh all items
                    androidx.recyclerview.widget.RecyclerView.Adapter adapter = recyclerView.getAdapter();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    
                    Log.d("StudentDateSheet", "Forced table remeasure - child count: " + recyclerView.getChildCount());
                } catch (Exception e) {
                    Log.e("StudentDateSheet", "Error forcing table remeasure", e);
                }
            });
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in forceTableRemeasure", e);
        }
    }
    
    /**
     * Update header syllabus column width dynamically based on content length
     */
    private void updateHeaderSyllabusWidth(List<DateSheetData> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        
        try {
            // Find the header syllabus TextView
            TextView headerSyllabus = findViewById(R.id.header_syllabus);
            if (headerSyllabus == null) {
                // Try to find it in the included layout
                android.view.View dateSheetTable = findViewById(R.id.date_sheet_table);
                if (dateSheetTable != null) {
                    headerSyllabus = dateSheetTable.findViewById(R.id.header_syllabus);
                }
            }
            
            if (headerSyllabus == null) {
                Log.w("StudentDateSheet", "Header syllabus TextView not found");
                return;
            }
            
            // Find the maximum syllabus length across all items
            int maxSyllabusLength = 0;
            for (DateSheetData item : list) {
                if (item != null && item.syllabus != null && !item.syllabus.trim().isEmpty()) {
                    String syllabusText = item.syllabus.trim();
                    // Strip HTML tags to get actual text length
                    String plainText = syllabusText.replaceAll("<[^>]+>", "").trim();
                    int textLength = plainText.length();
                    if (textLength > maxSyllabusLength) {
                        maxSyllabusLength = textLength;
                    }
                }
            }
            
            // Calculate dynamic weight based on maximum text length
            float dynamicWeight;
            if (maxSyllabusLength <= 20) {
                dynamicWeight = 1.5f; // Short content
            } else if (maxSyllabusLength <= 50) {
                dynamicWeight = 2.0f; // Medium content (default)
            } else if (maxSyllabusLength <= 100) {
                dynamicWeight = 2.5f; // Long content
            } else if (maxSyllabusLength <= 200) {
                dynamicWeight = 3.0f; // Very long content
            } else {
                dynamicWeight = 3.5f; // Extremely long content
            }
            
            // Update layout params with new weight
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) headerSyllabus.getLayoutParams();
            if (params != null) {
                params.weight = dynamicWeight;
                headerSyllabus.setLayoutParams(params);
                
                Log.d("StudentDateSheet", "Updated header syllabus column weight to " + dynamicWeight + 
                    " for max text length: " + maxSyllabusLength);
            }
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error updating header syllabus column width", e);
        }
    }
    
    /**
     * Display date sheet images if present in the response
     */
    private void displayDateSheetImages(List<topgrade.parent.com.parentseeks.Parent.Model.date_sheet.Image> images) {
        if (images == null || images.isEmpty()) {
            hideDateSheetImages();
            return;
        }
        
        // Store images for later use
        dateSheetImages = images;
        
        runOnUiThread(() -> {
            try {
                Log.d("StudentDateSheet", "[DEBUG] Displaying " + images.size() + " date sheet images");
                
                // Log image details
                for (int i = 0; i < images.size(); i++) {
                    topgrade.parent.com.parentseeks.Parent.Model.date_sheet.Image img = images.get(i);
                    if (img != null && img.picture != null && !img.picture.isEmpty()) {
                        String picturePath = img.picture;
                        String imageUrl;
                        if (picturePath.startsWith("http://") || picturePath.startsWith("https://")) {
                            imageUrl = picturePath;
                        } else {
                            // Date sheet images are stored in uploads/document/ folder
                            imageUrl = API.base_url + "uploads/document/" + picturePath;
                        }
                        Log.d("StudentDateSheet", "[DEBUG] Image " + (i + 1) + ": " + imageUrl);
                        Log.d("StudentDateSheet", "[DEBUG]   - Picture Path: " + picturePath);
                        Log.d("StudentDateSheet", "[DEBUG]   - Name: " + (img.full_name != null ? img.full_name : "N/A"));
                        Log.d("StudentDateSheet", "[DEBUG]   - Display Order: " + img.display_order);
                    }
                }
                
                // Show forward arrow button to view images
                if (btnViewDateSheetImages != null) {
                    btnViewDateSheetImages.setVisibility(View.VISIBLE);
                    // Update text in the TextView inside the LinearLayout
                    TextView tvViewImagesText = btnViewDateSheetImages.findViewById(R.id.tv_view_images_text);
                    if (tvViewImagesText != null) {
                        tvViewImagesText.setText("View Images (" + images.size() + ")");
                    }
                    Log.d("StudentDateSheet", "[DEBUG] View Images button made visible");
                }
                
            } catch (Exception e) {
                Log.e("StudentDateSheet", "[DEBUG] Error displaying date sheet images", e);
            }
        });
    }
    
    /**
     * Hide date sheet images button
     */
    private void hideDateSheetImages() {
        runOnUiThread(() -> {
            if (btnViewDateSheetImages != null) {
                btnViewDateSheetImages.setVisibility(View.GONE);
            }
            dateSheetImages.clear();
        });
    }
    
    /**
     * Open date sheet images in ZoomImage activity
     * Supports multiple images with navigation
     */
    private void openDateSheetImages() {
        if (dateSheetImages == null || dateSheetImages.isEmpty()) {
            Toast.makeText(this, "No images available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Build list of all image URLs
            java.util.ArrayList<String> imageUrls = new java.util.ArrayList<>();
            
            for (topgrade.parent.com.parentseeks.Parent.Model.date_sheet.Image img : dateSheetImages) {
                if (img != null && img.picture != null && !img.picture.isEmpty()) {
                    String picturePath = img.picture;
                    String imageUrl;
                    
                    // If it's already a full URL, use it as is
                    if (picturePath.startsWith("http://") || picturePath.startsWith("https://")) {
                        imageUrl = picturePath;
                    } else {
                        // Date sheet images are stored in uploads/document/ folder
                        // Build URL dynamically based on the picture path from API
                        imageUrl = API.base_url + "uploads/document/" + picturePath;
                    }
                    
                    imageUrls.add(imageUrl);
                    Log.d("StudentDateSheet", "[DEBUG] Added image URL: " + imageUrl);
                }
            }
            
            if (imageUrls.isEmpty()) {
                Toast.makeText(this, "No valid images found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Use "Date Sheet" as the name (ZoomImage will append " Image" to it)
            String imageName = "Date Sheet";
            
            // If only one image, open it directly
            if (imageUrls.size() == 1) {
                Intent intent = new Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.ZoomImage.class);
                intent.putExtra("image_url", imageUrls.get(0));
                intent.putExtra("name", imageName);
                startActivity(intent);
                Log.d("StudentDateSheet", "[DEBUG] Opening single date sheet image: " + imageUrls.get(0));
            } else {
                // Multiple images: pass all URLs and current index (0 for first image)
                Intent intent = new Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.ZoomImage.class);
                intent.putExtra("image_url", imageUrls.get(0)); // First image
                intent.putStringArrayListExtra("image_urls", imageUrls); // All images
                intent.putExtra("current_index", 0); // Start with first image
                intent.putExtra("name", imageName);
                startActivity(intent);
                Log.d("StudentDateSheet", "[DEBUG] Opening " + imageUrls.size() + " date sheet images, starting with: " + imageUrls.get(0));
            }
        } catch (Exception e) {
            Log.e("StudentDateSheet", "[DEBUG] Error opening date sheet images", e);
            Toast.makeText(this, "Error opening images", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * Uses margin approach like child list - footer pushed above navigation bar,
     * navigation bar's dark_brown color creates transparent/blended appearance
     */
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            
            if (rootLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                        );

                        // Footer removed - apply bottom margin to SMS button instead
                        if (sendDateSheetInSms != null) {
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) sendDateSheetInSms.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 16; // 16dp base margin + system inset
                                sendDateSheetInSms.setLayoutParams(params);
                            }
                        }
                        
                        view.setPadding(0, 0, 0, 0);
                        return WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("StudentDateSheet", "Error in window insets listener: " + e.getMessage());
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e("StudentDateSheet", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error setting up window insets: " + e.getMessage(), e);
        }
    }
}


