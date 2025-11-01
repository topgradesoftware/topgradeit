package topgrade.parent.com.parentseeks.Teacher.Exam;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.ExamAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Interface.MarksEnterInterface;
import topgrade.parent.com.parentseeks.Teacher.Interface.SmsCheck;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ExamResultModel;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamResult;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamSubmitStrcu;
import topgrade.parent.com.parentseeks.Teacher.Model.Result;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class ExamResults extends AppCompatActivity implements MarksEnterInterface, SmsCheck {

    private static final String TAG = "ExamResults";
    
    // UI Components - Header
    private ImageView backIcon;
    private TextView headerTitle;
    
    // UI Components - Criteria Display (Read-only)
    private TextView sessionText;
    private TextView classText;
    private TextView sectionText;
    private TextView subjectText;
    private TextView examText;
    private CheckBox smsCheck;
    
    // UI Components - Results Section
    private TextView totalRecords;
    private TextView attendanceDate;
    private RecyclerView examRecyclerView;
    private MaterialButton submitMarksButton;
    private ProgressBar progressBar;
    
    // Data from Intent
    private String mode;
    private String sessionId;
    private String sessionName;
    private String classId;
    private String className;
    private String sectionId;
    private String sectionName;
    private String subjectId;
    private String subjectName;
    private String examId;
    private String examName;
    private boolean loadSubmittedData;
    
    private Context context;
    private List<Object> studentsList = new ArrayList<>();
    
    // Data lists for exam results
    private List<ExamResult> resultList = new ArrayList<>();
    private ArrayList<ExamSubmitStrcu> exam_submit_list = new ArrayList<>();
    private String total_marks = "0";
    private ExamAdaptor examAdaptor; // Store adapter reference for refreshing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_exam_results);
        
        context = ExamResults.this;
        Paper.init(context);
        
        // Get data from intent
        getIntentData();
        
        // Initialize views
        initViews();
        
        // Setup listeners
        setupListeners();
        
        // Display selection criteria
        displayCriteria();
        
        // Load students
        loadStudents();
    }
    
    /**
     * Get data passed from previous activity
     */
    private void getIntentData() {
        try {
            mode = getIntent().getStringExtra("MODE");
            sessionId = getIntent().getStringExtra("SESSION_ID");
            sessionName = getIntent().getStringExtra("SESSION_NAME");
            classId = getIntent().getStringExtra("CLASS_ID");
            className = getIntent().getStringExtra("CLASS_NAME");
            sectionId = getIntent().getStringExtra("SECTION_ID");
            sectionName = getIntent().getStringExtra("SECTION_NAME");
            subjectId = getIntent().getStringExtra("SUBJECT_ID");
            subjectName = getIntent().getStringExtra("SUBJECT_NAME");
            examId = getIntent().getStringExtra("EXAM_ID");
            examName = getIntent().getStringExtra("EXAM_NAME");
            loadSubmittedData = getIntent().getBooleanExtra("LOAD_SUBMITTED_DATA", false);
            
            Log.d(TAG, "=== RECEIVED INTENT DATA ===");
            Log.d(TAG, "MODE: " + mode);
            Log.d(TAG, "Session: " + sessionName + " (ID: " + sessionId + ")");
            Log.d(TAG, "Class: " + className + " (ID: " + classId + ")");
            Log.d(TAG, "Section: " + sectionName + " (ID: " + sectionId + ")");
            Log.d(TAG, "Subject: " + subjectName + " (ID: " + subjectId + ")");
            Log.d(TAG, "Exam: " + examName + " (ID: " + examId + ")");
            Log.d(TAG, "Load Submitted Data: " + loadSubmittedData);
            Log.d(TAG, "============================");
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting intent data", e);
        }
    }
    
    /**
     * Initialize all views
     */
    private void initViews() {
        // Header
        backIcon = findViewById(R.id.back_icon);
        headerTitle = findViewById(R.id.header_title);
        
        // Criteria display (read-only)
        sessionText = findViewById(R.id.session_text);
        classText = findViewById(R.id.class_text);
        sectionText = findViewById(R.id.section_text);
        subjectText = findViewById(R.id.subject_text);
        examText = findViewById(R.id.exam_text);
        smsCheck = findViewById(R.id.sms_check);
        
        // Results section
        totalRecords = findViewById(R.id.total_records);
        attendanceDate = findViewById(R.id.attendence_date);
        examRecyclerView = findViewById(R.id.exam_rcv);
        submitMarksButton = findViewById(R.id.Submit_Marks);
        progressBar = findViewById(R.id.progress_bar);
        
        // Setup RecyclerView
        examRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        examRecyclerView.setHasFixedSize(false);
        
        // Set current date
        String currentDate = new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date());
        attendanceDate.setText(currentDate);
    }
    
    /**
     * Setup click listeners
     */
    private void setupListeners() {
        // Back button
        backIcon.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });
        
        // Submit marks button
        submitMarksButton.setOnClickListener(v -> {
            Log.d(TAG, "Submit Marks button clicked");
            submitMarks();
        });
        
        // Setup SMS checkbox listener
        setupSMSCheckboxListener();
    }
    
    /**
     * Setup SMS checkbox listener (Master checkbox to select/deselect all)
     */
    private void setupSMSCheckboxListener() {
        if (smsCheck != null) {
        smsCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Log.d(TAG, "Master SMS Checkbox: " + isChecked);
                
                // Update all students' SMS preference
                if (exam_submit_list != null && exam_submit_list.size() > 0) {
                    String smsValue = isChecked ? "1" : "0";
                    
                    for (int i = 0; i < exam_submit_list.size(); i++) {
                        ExamSubmitStrcu examSubmit = exam_submit_list.get(i);
                        examSubmit.setSms(smsValue);
                        exam_submit_list.set(i, examSubmit);
                    }
                    
                    // Refresh the adapter to update all checkboxes in the list
                    if (examAdaptor != null) {
                        examAdaptor.notifyDataSetChanged();
                        Log.d(TAG, "Updated all " + exam_submit_list.size() + " students' SMS preference to: " + (isChecked ? "Checked" : "Unchecked"));
                    } else {
                        Log.w(TAG, "Adapter is null, cannot refresh UI");
                    }
                }
            });
        }
    }
    
    /**
     * Display selection criteria in read-only format
     */
    private void displayCriteria() {
        try {
            // Update header based on mode - dynamically set title
            Log.d(TAG, "Setting header title based on mode: " + mode);
            
            if ("VIEW_RESULTS".equals(mode)) {
                headerTitle.setText("View Results");
                submitMarksButton.setVisibility(View.GONE);
                smsCheck.setChecked(false); // Uncheck SMS checkbox in view mode
                smsCheck.setEnabled(false);  // Disable SMS checkbox (read-only)
                Log.d(TAG, "Mode: VIEW_RESULTS - Header set to 'View Results', Submit button hidden, SMS unchecked and disabled");
            } else if ("UPDATE".equals(mode) || "UPDATE_MARKS".equals(mode)) {
                headerTitle.setText("Update Exam Marks");
                submitMarksButton.setText("Update Marks");
                Log.d(TAG, "Mode: UPDATE - Header set to 'Update Exam Marks'");
            } else {
                // Default mode is SUBMIT
                headerTitle.setText("Submit Exam Marks");
                submitMarksButton.setText("Submit Marks");
                Log.d(TAG, "Mode: SUBMIT (default) - Header set to 'Submit Exam Marks'");
            }
            
            // Display criteria (read-only)
            sessionText.setText(sessionName != null ? sessionName : "N/A");
            classText.setText(className != null ? className : "N/A");
            sectionText.setText(sectionName != null ? sectionName : "N/A");
            subjectText.setText(subjectName != null ? subjectName : "N/A");
            examText.setText(examName != null ? examName : "N/A");
            
            Log.d(TAG, "Criteria displayed successfully - Header title: " + headerTitle.getText());
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying criteria", e);
        }
    }
    
    /**
     * Load students based on selection
     */
    private void loadStudents() {
        try {
            Log.d(TAG, "Loading students...");
            Log.d(TAG, "Params - classId: " + classId + ", sectionId: " + sectionId + 
                  ", subjectId: " + subjectId + ", examId: " + examId);
            
            // Validate required parameters
            if (classId == null || classId.isEmpty()) {
                Toast.makeText(context, "Class ID is missing", Toast.LENGTH_SHORT).show();
                return;
            }
            if (subjectId == null || subjectId.isEmpty()) {
                Toast.makeText(context, "Subject ID is missing", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sectionId == null || sectionId.isEmpty()) {
                Toast.makeText(context, "Section ID is missing", Toast.LENGTH_SHORT).show();
                return;
            }
            if (examId == null || examId.isEmpty()) {
                Toast.makeText(context, "Exam ID is missing", Toast.LENGTH_SHORT).show();
                return;
            }
            
            progressBar.setVisibility(View.VISIBLE);
            
            // Prepare API request parameters
            HashMap<String, String> postParam = new HashMap<>();
            postParam.put("staff_id", Constant.staff_id);
            postParam.put("parent_id", Constant.campus_id);
            postParam.put("student_class_id", classId);
            postParam.put("subject_id", subjectId);
            postParam.put("section_id", sectionId);
            postParam.put("exam_id", examId);
            
            JSONObject jsonObject = new JSONObject(postParam);
            Log.d(TAG, "API Request: " + jsonObject.toString());
            
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    jsonObject.toString());
            
            // Make API call to load exam results
            Constant.mApiService.load_exams_results(body).enqueue(new Callback<ExamResultModel>() {
                @Override
                public void onResponse(Call<ExamResultModel> call, Response<ExamResultModel> response) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.body() != null) {
                        if (response.body().getStatus().getCode().equals("1000")) {
                            resultList = response.body().getResult();
                            
                            Log.d(TAG, "Exam results loaded successfully. Count: " + 
                                  (resultList != null ? resultList.size() : 0));
                            
                            // Get total marks from first student if available
                            if (resultList != null && !resultList.isEmpty() && resultList.get(0).getTotalMarks() != null) {
                                total_marks = String.valueOf(resultList.get(0).getTotalMarks());
                            }
                            
                            // Prepare exam_submit_list for adapter
                            exam_submit_list.clear();
                            if (resultList != null && !resultList.isEmpty()) {
                                for (int i = 0; i < resultList.size(); i++) {
                                    if (resultList.get(i).getResult() != null) {
                                        exam_submit_list.add(new ExamSubmitStrcu(
                                                resultList.get(i).getResult().getAttendance(),
                                                resultList.get(i).getResult().getNote(),
                                                resultList.get(i).getUniqueId(),
                                                "1",
                                                resultList.get(i).getResult().getObtained_marks(),
                                                "0"));
                                    } else {
                                        exam_submit_list.add(new ExamSubmitStrcu("",
                                                "",
                                                resultList.get(i).getUniqueId(),
                                                "1",
                                                "",
                                                "0"));
                                    }
                                }
                                
                                // Update UI with results
                                updateRecyclerView();
                                
                                Log.d(TAG, "Adapter set with " + resultList.size() + " students");
                            } else {
                                Log.w(TAG, "No exam results found");
                                totalRecords.setText("Total Records: 0");
                                Toast.makeText(context, "No students found for this exam", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String message = response.body().getStatus().getMessage();
                            Log.e(TAG, "API Error: " + message);
                            totalRecords.setText("Total Records: 0");
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Response body is null");
                        totalRecords.setText("Total Records: 0");
                        Toast.makeText(context, "No response from server", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ExamResultModel> call, Throwable throwable) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Network error: " + throwable.getMessage());
                    totalRecords.setText("Total Records: 0");
                    Toast.makeText(context, "Network error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading students", e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(context, "Error loading students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Update RecyclerView with students data
     */
    private void updateRecyclerView() {
        try {
            // Set adapter with exam results data
            if (examRecyclerView != null && resultList != null && !resultList.isEmpty()) {
                examAdaptor = new ExamAdaptor(context,
                        ExamResults.this, resultList, exam_submit_list,
                        this, null, total_marks, this);
                examRecyclerView.setAdapter(examAdaptor);
                examAdaptor.notifyDataSetChanged();
            
            // Update total records
                totalRecords.setText("Total Records: " + resultList.size());
                
                // Set master SMS checkbox to checked by default (after data is loaded)
                if (smsCheck != null) {
                    // Temporarily remove listener to avoid triggering it
                    smsCheck.setOnCheckedChangeListener(null);
                    smsCheck.setChecked(true);
                    // Restore the listener
                    setupSMSCheckboxListener();
                    Log.d(TAG, "Master SMS checkbox set to checked by default");
                }
                
                Log.d(TAG, "RecyclerView updated with " + resultList.size() + " students");
            } else {
                totalRecords.setText("Total Records: 0");
                Log.w(TAG, "RecyclerView or resultList is empty");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating RecyclerView", e);
            totalRecords.setText("Total Records: 0");
        }
    }
    
    /**
     * Submit marks to database - Direct submission without confirmation
     */
    private void submitMarks() {
        try {
            Log.d(TAG, "Submitting marks...");
            
            if (exam_submit_list.size() > 0) {
                // Submit directly without confirmation dialog
                submitMarksAPI();
            } else {
                Toast.makeText(context, "No students to submit marks for", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error submitting marks", e);
            showErrorDialog("Failed to submit marks. Please try again.");
        }
    }
    
    /**
     * Submit marks to API
     */
    private void submitMarksAPI() {
        try {
            Log.d(TAG, "Starting marks submission for " + exam_submit_list.size() + " students");
            
            JSONArray jsonArray = new JSONArray();
            Gson gson = new Gson();
            
            String listString = gson.toJson(
                    exam_submit_list,
                    new TypeToken<ArrayList<ExamSubmitStrcu>>() {
                    }.getType());
            
            try {
                jsonArray = new JSONArray(listString);
                Log.d(TAG, "Successfully converted exam data to JSON array");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error converting exam data to JSON", e);
                Toast.makeText(context, "Error preparing data for submission", Toast.LENGTH_SHORT).show();
                return;
            }
            
            HashMap<String, Object> postParam = new HashMap<>();
            postParam.put("staff_id", Constant.staff_id);
            postParam.put("parent_id", Constant.campus_id);
            postParam.put("student_class_id", classId);
            postParam.put("subject_id", subjectId);
            postParam.put("section_id", sectionId);
            postParam.put("exam_id", examId);
            postParam.put("results", jsonArray);
            
            JSONObject jsonObject = new JSONObject(postParam);
            Log.d(TAG, "Submission data: " + jsonObject.toString());
            
            progressBar.setVisibility(View.VISIBLE);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    jsonObject.toString());
            
            Constant.mApiService.save_exam_results(body).enqueue(new Callback<ExamResultModel>() {
                @Override
                public void onResponse(Call<ExamResultModel> call, Response<ExamResultModel> response) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.body() != null) {
                        if (response.body().getStatus().getCode().equals("1000")) {
                            Log.d(TAG, "Marks submitted successfully");
                            showSuccessDialog();
                        } else {
                            Log.e(TAG, "API Error: " + response.body().getStatus().getMessage());
                            showErrorDialog("Submission failed: " + response.body().getStatus().getMessage());
                        }
                    } else {
                        Log.e(TAG, "Response body is null");
                        showErrorDialog("No response from server. Please try again.");
                    }
                }
                
                @Override
                public void onFailure(Call<ExamResultModel> call, Throwable e) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Network error submitting marks", e);
                    showErrorDialog("Network error: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error in submitMarksAPI", e);
            showErrorDialog("Error submitting marks: " + e.getMessage());
        }
    }
    
    /**
     * Validate marks before submission
     */
    private boolean validateMarks() {
        try {
            // TODO: Validate that all students have marks entered
            // TODO: Validate marks are within valid range
            
            if (studentsList.isEmpty()) {
                showErrorDialog("No students to submit marks for.");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating marks", e);
            return false;
        }
    }
    
    /**
     * Show success dialog
     */
    private void showSuccessDialog() {
        try {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Marks submitted successfully!")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing success dialog", e);
        }
    }
    
    /**
     * Show error dialog
     */
    private void showErrorDialog(String message) {
        try {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing error dialog", e);
        }
    }
    
    // ==================== INTERFACE IMPLEMENTATIONS ====================
    
    /**
     * Called when marks are entered for a student
     * @param position Position in the list
     * @param obtained_marks Marks obtained by the student
     */
    @Override
    public void OnMarksEnter(int position, String obtained_marks) {
        try {
            Log.d(TAG, "Marks entered at position " + position + ": " + obtained_marks);
            
            // Update the exam_submit_list with the new marks
            if (position >= 0 && position < exam_submit_list.size()) {
                ExamSubmitStrcu examSubmit = exam_submit_list.get(position);
                examSubmit.setMarks(obtained_marks);
                exam_submit_list.set(position, examSubmit);
                
                Log.d(TAG, "Updated marks for position " + position + " to " + obtained_marks);
            } else {
                Log.e(TAG, "Invalid position: " + position + " (list size: " + exam_submit_list.size() + ")");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in OnMarksEnter", e);
        }
    }
    
    /**
     * Called when SMS checkbox is checked/unchecked for a student
     * @param is_sms Whether SMS should be sent
     * @param position Position in the list
     */
    @Override
    public void OnSmsCheck(boolean is_sms, int position) {
        try {
            Log.d(TAG, "SMS check at position " + position + ": " + is_sms);
            
            // Update the exam_submit_list with SMS preference
            if (position >= 0 && position < exam_submit_list.size()) {
                ExamSubmitStrcu examSubmit = exam_submit_list.get(position);
                examSubmit.setSms(is_sms ? "1" : "0");
                exam_submit_list.set(position, examSubmit);
                
                Log.d(TAG, "Updated SMS preference for position " + position + " to " + (is_sms ? "Yes" : "No"));
            } else {
                Log.e(TAG, "Invalid position: " + position + " (list size: " + exam_submit_list.size() + ")");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in OnSmsCheck", e);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() - ExamResults destroyed");
    }
}

