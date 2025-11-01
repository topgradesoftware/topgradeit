package topgrade.parent.com.parentseeks.Parent.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    TextView tvTotalRecords, tvStudentName, tvClass, tvHeaderTimeOrSyllabus;
    Button sendDateSheetInSms;
    SharedStudent student;
    Context context;

    RecyclerView timetableRcv;
    List<DateSheetData> list = new ArrayList<>();
    String dateSheetSms = "";
    SearchableSpinner selectChildSpinner, selectExamSession;

    String selectedChildId = "", selectedStudentClassId = "";
    String selectedChildName = "";
    String selectedExamSession = "";
    String selectedExamSessionName = "";
    ArrayAdapter<String> childAdaptor, sessionAdaptor;
    LinearLayout sessionLayout;
    List<SharedStudent> studentList = new ArrayList<>();
    List<String> examSessionNameList = new ArrayList<>();
    List<ExamSession> examSessionList = new ArrayList<>();
    
    // Dialog components (for backward compatibility)
    Button dialogSearchFilterBtn;
    ImageView dialogCancelBtn;
    AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_student_datesheet);
            
            // Apply student theme (teal) for student user type
            String userType = Paper.book().read(Constants.User_Type, "");
            if ("STUDENT".equals(userType)) {
                // Apply student theme using StudentThemeHelper
                topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.applyStudentTheme(this, 100);
                topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setHeaderIconVisibility(this, false);
                topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setMoreOptionsVisibility(this, false);
                topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setFooterVisibility(this, true);
                topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setHeaderTitle(this, "Date Sheet");
            } else {
                // Apply theme based on user type for non-student users
                applyTheme(userType);
            }
            
            context = StudentDateSheet.this;
            init();
            setupInlineFilters();
            
            // Set up back button
            ImageView ivBackIcon = findViewById(R.id.back_button);
            ivBackIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            
            if (student != null) {
                String studentName = student.getFullName();
                String className = student.getClassName();
                
                Log.d("StudentDateSheet", "onCreate() - SharedStudent name: " + studentName);
                Log.d("StudentDateSheet", "onCreate() - Class name: " + className);
            } else {
                Log.d("StudentDateSheet", "onCreate() - SharedStudent is null");
            }
            listeners();
            
            // Inline filters are now set up in setupInlineFilters()
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in onCreate: " + e.getMessage(), e);
            // Show error message to user
            Toast.makeText(this, "Error loading date sheet. Please try again.", Toast.LENGTH_LONG).show();
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
                
                // Set student name with fallback
                if (studentName != null && !studentName.trim().isEmpty()) {
                    tvStudentName.setText(studentName);
                } else {
                    tvStudentName.setText("Please select a student");
                }
                
                // Set class name with fallback
                if (className != null && !className.trim().isEmpty()) {
                    tvClass.setText("Class: " + className);
        } else {
            tvClass.setText("Class: Not Available");
                }
            } else {
                Log.d("StudentDateSheet", "No valid student found, showing default message");
                tvStudentName.setText("Please select a student");
                tvClass.setText("Class: Not Available");
            }
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error updating student display", e);
            tvStudentName.setText("Error loading student");
            tvClass.setText("Class: Error");
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
        // Clear date sheet data
        list.clear();
        
        // Clear RecyclerView adapter
        if (timetableRcv != null) {
            timetableRcv.setAdapter(new StudentDateSheetAdaptor(new ArrayList<>(), this));
        }
        
        // Clear total records
        if (tvTotalRecords != null) {
            tvTotalRecords.setVisibility(View.GONE);
        }
        
        // Clear exam session data
        examSessionList.clear();
        examSessionNameList.clear();
        selectedExamSession = "";
        selectedExamSessionName = "";
        
        // Hide session layout
        if (sessionLayout != null) {
            sessionLayout.setVisibility(View.GONE);
        }
        
        // Hide SMS button
        if (sendDateSheetInSms != null) {
            sendDateSheetInSms.setVisibility(View.GONE);
        }
        
        Log.d("StudentDateSheet", "clearDateSheetData() - All date sheet data cleared");
    }

    private void showNoStudentsDialog() {
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
            timetableRcv = findViewById(R.id.rv_student_timetable);
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
                Toast.makeText(context, "No students found. Please contact your school administrator.", Toast.LENGTH_LONG).show();
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
            View dialogView = inflater.inflate(R.layout.progress_report_advanced_search_layout, null);

            selectChildSpinner = dialogView.findViewById(R.id.selectChildSpinner);
            dialogSearchFilterBtn = dialogView.findViewById(R.id.search_button);
            selectExamSession = dialogView.findViewById(R.id.selectExampSession);
            
            // Check if dialogCancelBtn exists before setting click listener
            dialogCancelBtn = dialogView.findViewById(R.id.Cancel);
            if (dialogCancelBtn != null) {
                dialogCancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (alertDialog != null) {
                            alertDialog.dismiss();
                        }
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
                Toast.makeText(this, "No students available. Please check Child List first.", Toast.LENGTH_LONG).show();
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
                        alertDialog.dismiss();
                        list.clear();
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
            // Validate parameters
            if (campus_id == null || campus_id.isEmpty()) {
                Toast.makeText(this, "Campus ID not available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (child_id == null || child_id.isEmpty()) {
                Toast.makeText(this, "Child ID not available", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, String> postParam = new HashMap<String, String>();
            postParam.put("parent_id", campus_id);
            postParam.put("student_id", child_id);
            postParam.put("session_id", Constant.current_session);
            
            // Log the exam session request parameters
            Log.d("StudentDateSheet", "=== LOADING EXAM SESSIONS ===");
            Log.d("StudentDateSheet", "Campus ID: " + campus_id);
            Log.d("StudentDateSheet", "SharedStudent ID: " + child_id);
            Log.d("StudentDateSheet", "Session ID: " + Constant.current_session);
            
            String jsonBody = new JSONObject(postParam).toString();
            Log.d("StudentDateSheet", "Exam Session JSON Body: " + jsonBody);
            
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Constant.mApiService.load_exam_session(body).enqueue(new Callback<SessionModel>() {
                @Override
                public void onResponse(Call<SessionModel> call, retrofit2.Response<SessionModel> response) {
                    try {
                        Log.d("StudentDateSheet", "Exam Session Response Code: " + response.code());
                        Log.d("StudentDateSheet", "Exam Session Response Message: " + response.message());
                        
                        if (response.isSuccessful()) {
                            SessionModel sessionModel = response.body();
                            if (sessionModel != null && sessionModel.getStatus() != null) {
                                Log.d("StudentDateSheet", "Exam Session API SharedStatus: " + sessionModel.getStatus().getCode());
                                Log.d("StudentDateSheet", "Exam Session API Message: " + sessionModel.getStatus().getMessage());
                                
                                if (sessionModel.getStatus().getCode().equals("1000")) {
                                examSessionList = sessionModel.getExamSession();
                                examSessionNameList.clear(); // Clear previous data
                                    
                                    Log.d("StudentDateSheet", "Exam Sessions received: " + (examSessionList != null ? examSessionList.size() : "null"));
                                
                                if (examSessionList != null && !examSessionList.isEmpty()) {
                                    for (int i = 0; i < examSessionList.size(); i++) {
                                        ExamSession session = examSessionList.get(i);
                                        if (session != null && session.getFullName() != null) {
                                            examSessionNameList.add(session.getFullName());
                                                Log.d("StudentDateSheet", "Exam Session " + i + ": " + session.getFullName() + " (ID: " + session.getUniqueId() + ")");
                                        }
                                    }

                                    if (examSessionList.size() > 0) {
                                        if (examSessionList.size() > 1) {
                                            Log.d("StudentDateSheet", "Multiple sessions, showing session layout");
                                            if (sessionLayout != null) {
                                                sessionLayout.setVisibility(View.VISIBLE);
                                            }
                                            sessionAdaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, examSessionNameList);
                                            if (selectExamSession != null) {
                                                selectExamSession.setAdapter(sessionAdaptor);
                                                selectExamSession.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        selectedExamSession = examSessionList.get(position).getUniqueId();
                                                        selectedExamSessionName = examSessionList.get(position).getFullName();
                                                        Log.d("StudentDateSheet", "Selected session: " + selectedExamSessionName);
                                                        
                                                        // Automatically load date sheet when session is selected
                                                        if (selectedChildId != null && selectedExamSession != null) {
                                                            list.clear();
                                                            if (tvTotalRecords != null) {
                                                                tvTotalRecords.setVisibility(View.GONE);
                                                            }
                                                            loadDateSheet(selectedExamSession);
                                                        }
                                                    }
                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {}
                                                });
                                            }
                                        } else {
                                            Log.d("StudentDateSheet", "Single session, hiding session layout");
                                            if (sessionLayout != null) {
                                                sessionLayout.setVisibility(View.GONE);
                                            }
                                            selectedExamSession = examSessionList.get(0).getUniqueId();
                                            selectedExamSessionName = examSessionList.get(0).getFullName();
                                            Log.d("StudentDateSheet", "Auto-selected session: " + selectedExamSessionName);
                                            
                                            // Automatically load date sheet when session is auto-selected
                                            if (selectedChildId != null && selectedExamSession != null) {
                                                list.clear();
                                                if (tvTotalRecords != null) {
                                                    tvTotalRecords.setVisibility(View.GONE);
                                                }
                                                loadDateSheet(selectedExamSession);
                                            }
                                        }
                                    }
                                } else {
                                        Log.d("StudentDateSheet", "No exam sessions available for this student");
                                        Toast.makeText(context, "No exam sessions available for this student", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                                    Log.d("StudentDateSheet", "Exam Session API Error: " + (sessionModel != null ? sessionModel.getStatus().getMessage() : "Unknown error"));
                                    Toast.makeText(context, sessionModel != null ? sessionModel.getStatus().getMessage() : "Failed to load exam sessions", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                                Log.d("StudentDateSheet", "Invalid exam session response structure");
                                Toast.makeText(context, "Failed to load exam sessions", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Log.d("StudentDateSheet", "Exam Session HTTP Error: " + response.code());
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
                    Toast.makeText(context, "Failed to load exam sessions", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in loadExamSession", e);
            Toast.makeText(this, "Error loading exam sessions", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDateSheet(String sessionId) {
        try {
            // Validate required parameters
            if (selectedChildId == null || selectedChildId.isEmpty()) {
                Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedExamSession == null || selectedExamSession.isEmpty()) {
                Toast.makeText(this, "Please select an exam session", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedStudentClassId == null || selectedStudentClassId.isEmpty()) {
                Toast.makeText(this, "SharedStudent class information not available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate campus and parent IDs
            String campusId = Paper.book().read("campus_id", "");
            if (campusId == null || campusId.isEmpty()) {
                Toast.makeText(this, "Campus information not available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            HashMap<String, String> postParam = new HashMap<String, String>();
            parentId = Paper.book().read("parent_id", "");

            // Log all the parameters being sent
            Log.d("StudentDateSheet", "=== LOADING DATE SHEET ===");
            Log.d("StudentDateSheet", "SharedStudent ID: " + selectedChildId);
            Log.d("StudentDateSheet", "SharedStudent Name: " + selectedChildName);
            Log.d("StudentDateSheet", "SharedStudent Class ID: " + selectedStudentClassId);
            Log.d("StudentDateSheet", "SharedStudent Class Name: " + (student != null ? student.getClassName() : "N/A"));
            Log.d("StudentDateSheet", "Exam Session: " + selectedExamSession);
            Log.d("StudentDateSheet", "Campus ID: " + campusId);
            Log.d("StudentDateSheet", "Parent ID: " + parentId);
            Log.d("StudentDateSheet", "Current Session: " + Constant.current_session);

            // Based on the PHP backend analysis, try different parameter combinations
            // First attempt: Standard parameters
            postParam.put("student_id", selectedChildId);
            postParam.put("campus_id", campusId);
            postParam.put("exam_session_id", selectedExamSession);
            postParam.put("student_class_id", selectedStudentClassId);
            
            // Add session_id parameter that the PHP backend might need
            if (Constant.current_session != null && !Constant.current_session.isEmpty()) {
                postParam.put("session_id", Constant.current_session);
                Log.d("StudentDateSheet", "Added session_id: " + Constant.current_session);
            }
            
            // Add parent_id parameter (some backends expect this)
            if (parentId != null && !parentId.isEmpty()) {
                postParam.put("parent_id", parentId);
                Log.d("StudentDateSheet", "Added parent_id: " + parentId);
            }
            
            // Log the final JSON being sent
            String jsonBody = new JSONObject(postParam).toString();
            Log.d("StudentDateSheet", "JSON Body being sent: " + jsonBody);
            
            // Run network call on background thread to prevent ANR
            new Thread(() -> {
                try {
                    runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
                    
                    RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
                    Constant.mApiService.loadStudentDateSheet(body).enqueue(new Callback<DateSheetResponse>() {
                @Override
                public void onResponse(Call<DateSheetResponse> call, Response<DateSheetResponse> response) {
                    try {
                        runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                        
                        // Log the raw response for debugging
                        Log.d("StudentDateSheet", "Response Code: " + response.code());
                        Log.d("StudentDateSheet", "Response Message: " + response.message());
                        
                        if (response.isSuccessful()) {
                            DateSheetResponse dateSheetResponse = response.body();
                            if (dateSheetResponse != null && dateSheetResponse.status != null) {
                                Log.d("StudentDateSheet", "API SharedStatus Code: " + dateSheetResponse.status.getCode());
                                Log.d("StudentDateSheet", "API SharedStatus Message: " + dateSheetResponse.status.getMessage());
                                
                                if ("1000".equals(dateSheetResponse.status.getCode())) {
                                list = dateSheetResponse.data;
                                    
                                    // Log the data received
                                    Log.d("StudentDateSheet", "Data received: " + (list != null ? list.size() : "null") + " items");
                                    
                                    // Validate the response data
                                    validateDateSheetResponse(list);
                                    
                                    if (list != null && !list.isEmpty()) {
                                    // Data available, display it
                                    DateSheetData firstItem = list.get(0);
                                        
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
                                        
                                    runOnUiThread(() -> {
                                        tvTotalRecords.setVisibility(View.VISIBLE);
                                        tvTotalRecords.setText("Total Records: " + list.size());
                                        timetableRcv.setAdapter(new StudentDateSheetAdaptor(list, StudentDateSheet.this));
                                        
                                        // Show SMS button when data is available
                                        if (sendDateSheetInSms != null) {
                                            sendDateSheetInSms.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    
                                    // Generate SMS text for sharing
                                    dateSheetSms = generateDateSheetSms();
                                } else {
                                        // No data available, try alternative approach
                                        Log.d("StudentDateSheet", "No data in response - trying alternative approach");
                                        tryAlternativeDateSheetCall();
                                }
                                } else {
                                    // API returned error, try alternative approach
                                    String errorMessage = dateSheetResponse.status.getMessage();
                                    Log.d("StudentDateSheet", "API returned error: " + errorMessage);

                                    // Try alternative approach if it's a data not found error
                                    if (errorMessage != null && (errorMessage.contains("not found") || errorMessage.contains("no data"))) {
                                        tryAlternativeDateSheetCall();
                            } else {
                                        runOnUiThread(() -> {
                                            if (errorMessage != null && !errorMessage.isEmpty()) {
                                                Toast.makeText(StudentDateSheet.this, errorMessage, Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(StudentDateSheet.this, "No date sheet data found", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                            }
                        } else {
                                Log.d("StudentDateSheet", "Invalid response structure");
                                runOnUiThread(() -> Toast.makeText(StudentDateSheet.this, "Invalid response from server", Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            // HTTP error
                            String errorMessage = "Server error: " + response.code();
                            Log.d("StudentDateSheet", "HTTP Error: " + errorMessage);
                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    Log.e("StudentDateSheet", "Error response: " + errorBody);
                                } catch (Exception e) {
                                    Log.e("StudentDateSheet", "Error reading error body", e);
                                }
                            }
                            runOnUiThread(() -> Toast.makeText(StudentDateSheet.this, errorMessage, Toast.LENGTH_LONG).show());
                        }
                    } catch (Exception e) {
                        Log.e("StudentDateSheet", "Error in loadDateSheet onResponse", e);
                        runOnUiThread(() -> Toast.makeText(StudentDateSheet.this, "Error processing date sheet data", Toast.LENGTH_LONG).show());
                    }
                }

                @Override
                public void onFailure(Call<DateSheetResponse> call, Throwable e) {
                    Log.e("StudentDateSheet", "Error in loadDateSheet onFailure", e);
                    progressBar.setVisibility(View.GONE);
                    
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
                    
                    runOnUiThread(() -> Toast.makeText(StudentDateSheet.this, errorMessage, Toast.LENGTH_LONG).show());
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
                                    if (list != null && !list.isEmpty()) {
                                        Log.d("StudentDateSheet", "Alternative call successful - Data received: " + list.size() + " items");
                                        
                                        // Display the data
                                        DateSheetData firstItem = list.get(0);
                                        if (student != null && student.getClassName() != null) {
                                            tvClass.setText("Class: " + student.getClassName());
                                        }
                                        
                                        if (firstItem != null && firstItem.start_time == null) {
                                            tvHeaderTimeOrSyllabus.setText("Test Syllabus");
                                        } else {
                                            tvHeaderTimeOrSyllabus.setText("Time");
                                        }
                                        
                                        tvTotalRecords.setVisibility(View.VISIBLE);
                                        tvTotalRecords.setText("Total Records: " + list.size());
                                        if (timetableRcv != null) {
                                        timetableRcv.setAdapter(new StudentDateSheetAdaptor(list, StudentDateSheet.this));
                                        }
                                    } else {
                                        Log.d("StudentDateSheet", "Alternative call also returned no data - trying third alternative");
                                        tryThirdAlternativeDateSheetCall();
                                    }
                                } else {
                                    Log.d("StudentDateSheet", "Alternative call failed: " + dateSheetResponse.status.getMessage() + " - trying third alternative");
                                    tryThirdAlternativeDateSheetCall();
                                }
                            } else {
                                Log.d("StudentDateSheet", "Alternative call invalid response - trying third alternative");
                                tryThirdAlternativeDateSheetCall();
                            }
                        } else {
                            Log.d("StudentDateSheet", "Alternative call HTTP error: " + response.code() + " - trying third alternative");
                            tryThirdAlternativeDateSheetCall();
                        }
                    } catch (Exception e) {
                        Log.e("StudentDateSheet", "Error in alternative call", e);
                        tryThirdAlternativeDateSheetCall();
                    }
                }

                @Override
                public void onFailure(Call<DateSheetResponse> call, Throwable e) {
                    Log.e("StudentDateSheet", "Alternative call failed", e);
                    tryThirdAlternativeDateSheetCall();
                }
            });
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in tryAlternativeDateSheetCall", e);
            Toast.makeText(StudentDateSheet.this, "No date sheet data available for the selected criteria", Toast.LENGTH_LONG).show();
            tvTotalRecords.setVisibility(View.GONE);
        }
    }

    private void tryThirdAlternativeDateSheetCall() {
        try {
            Log.d("StudentDateSheet", "=== TRYING THIRD ALTERNATIVE DATE SHEET CALL ===");
            
            // Try with only basic parameters
            HashMap<String, String> postParam = new HashMap<String, String>();
            String campusId = Paper.book().read("campus_id", "");
            parentId = Paper.book().read("parent_id", "");
            
            postParam.put("student_id", selectedChildId);
            postParam.put("campus_id", campusId);
            postParam.put("exam_session_id", selectedExamSession);
            
            // Add parent_id parameter
            if (parentId != null && !parentId.isEmpty()) {
                postParam.put("parent_id", parentId);
            }
            
            String jsonBody = new JSONObject(postParam).toString();
            Log.d("StudentDateSheet", "Third Alternative JSON Body: " + jsonBody);
            
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
                                    if (list != null && !list.isEmpty()) {
                                        Log.d("StudentDateSheet", "Third alternative call successful - Data received: " + list.size() + " items");
                                        
                                        // Display the data
                                        DateSheetData firstItem = list.get(0);
                                        if (student != null && student.getClassName() != null) {
                                            tvClass.setText("Class: " + student.getClassName());
                                        }
                                        
                                        if (firstItem != null && firstItem.start_time == null) {
                                            tvHeaderTimeOrSyllabus.setText("Test Syllabus");
                                        } else {
                                            tvHeaderTimeOrSyllabus.setText("Time");
                                        }
                                        
                                        tvTotalRecords.setVisibility(View.VISIBLE);
                                        tvTotalRecords.setText("Total Records: " + list.size());
                                        if (timetableRcv != null) {
                                        timetableRcv.setAdapter(new StudentDateSheetAdaptor(list, StudentDateSheet.this));
                                        }
                                    } else {
                                        Log.d("StudentDateSheet", "Third alternative call also returned no data");
                                        showNoDataMessage();
                                    }
                                } else {
                                    Log.d("StudentDateSheet", "Third alternative call failed: " + dateSheetResponse.status.getMessage());
                                    showNoDataMessage();
                                }
                            } else {
                                Log.d("StudentDateSheet", "Third alternative call invalid response");
                                showNoDataMessage();
                            }
                        } else {
                            Log.d("StudentDateSheet", "Third alternative call HTTP error: " + response.code());
                            showNoDataMessage();
                        }
                    } catch (Exception e) {
                        Log.e("StudentDateSheet", "Error in third alternative call", e);
                        showNoDataMessage();
                    }
                }

                @Override
                public void onFailure(Call<DateSheetResponse> call, Throwable e) {
                    Log.e("StudentDateSheet", "Third alternative call failed", e);
                    showNoDataMessage();
                }
            });
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error in tryThirdAlternativeDateSheetCall", e);
            showNoDataMessage();
        }
    }

    private void showNoDataMessage() {
        Toast.makeText(StudentDateSheet.this, "No date sheet data available for the selected criteria", Toast.LENGTH_LONG).show();
        tvTotalRecords.setVisibility(View.GONE);
        
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
        try {
            if (dataList == null || dataList.isEmpty()) {
                Log.d("StudentDateSheet", "No data in response");
                return false;
            }
            
            // Check if the data belongs to the selected student's class
            boolean hasMatchingClass = false;
            for (DateSheetData item : dataList) {
                if (item != null && item.student_class_id != null) {
                    if (item.student_class_id.equals(selectedStudentClassId)) {
                        hasMatchingClass = true;
                        Log.d("StudentDateSheet", "Found matching class ID: " + item.student_class_id);
                        break;
                    }
                }
            }
            
            if (!hasMatchingClass) {
                Log.w("StudentDateSheet", "No data found for selected student's class ID: " + selectedStudentClassId);
                // Don't return false here as the API might return data for different classes
                // Just log the warning
            }
            
            return true;
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error validating date sheet response", e);
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        send_sms(view);
    }

    public void send_sms(View view) {
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
            
            // Group by date if available, otherwise show all subjects
            for (DateSheetData data : list) {
                String subject = data.subject != null ? data.subject : "Unknown Subject";
                String date = data.created_date != null ? data.created_date : "Unknown Date";
                String startTime = data.start_time != null ? data.start_time.toString() : "00:00";
                String endTime = data.end_time != null ? data.end_time.toString() : "00:00";
                
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
                        Toast.makeText(StudentDateSheet.this, "TimeTable Send in Your Number Soon.", Toast.LENGTH_LONG).show();
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

    private void loadDateSheetSession() {


        HashMap<String, String> postParam = new HashMap<String, String>();
//        postParam.put("student_id", student.getUniqueId());
        postParam.put("parent_id", student.getParentId());

        progressBar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

        Constant.mApiService.load_exam_session(body).enqueue(new Callback<SessionModel>() {
            @Override
            public void onResponse(Call<SessionModel> call, Response<SessionModel> response) {

                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        Log.d("appDebug", response.body().toString());
                        List<ExamSession> sessionList = response.body().getExamSession();

                        if (sessionList != null && sessionList.size() != 0) {
                            ExamSession sessionStudent = sessionList.get(0);
                            loadDateSheet(sessionStudent.getUniqueId());
                        }else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, "No session exist!", Toast.LENGTH_SHORT).show();
                        }
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
            public void onFailure(Call<SessionModel> call, Throwable e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
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
                Toast.makeText(context, "Error sharing date sheet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        } catch (Exception e) {
            Log.e("StudentDateSheet", "WhatsApp not installed or error sharing", e);
            Toast.makeText(context, "WhatsApp not installed. Please install WhatsApp to share.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToWhatsAppBusiness(String text) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage("com.whatsapp.w4b");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent, "Share via WhatsApp Business"));
        } catch (Exception e) {
            Log.e("StudentDateSheet", "WhatsApp Business not installed or error sharing", e);
            Toast.makeText(context, "WhatsApp Business not installed. Please install WhatsApp Business to share.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareViaSMS(String text) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:"));
            intent.putExtra("sms_body", text);
            startActivity(Intent.createChooser(intent, "Send SMS"));
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error sharing via SMS", e);
            Toast.makeText(context, "Error sharing via SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            } else {
                // Apply parent theme (brown) - default for parent users
                ThemeHelper.applyParentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    int brownColor = ContextCompat.getColor(this, R.color.parent_primary);
                    getWindow().setStatusBarColor(brownColor);
                    getWindow().setNavigationBarColor(brownColor);
                }
                
                // Force light status bar icons for better visibility on brown background
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
                
                Log.d("StudentDateSheet", "Applied PARENT theme (brown)");
            }
            
        } catch (Exception e) {
            Log.e("StudentDateSheet", "Error applying theme, using parent theme as fallback", e);
            // Fallback to parent theme
            ThemeHelper.applyParentTheme(this);
        }
    }
}

