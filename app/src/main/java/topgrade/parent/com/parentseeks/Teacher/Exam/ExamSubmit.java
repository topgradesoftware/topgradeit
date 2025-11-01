package topgrade.parent.com.parentseeks.Teacher.Exam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import components.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
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
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.ExamSession;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Activity.TeacherChildProfile;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.ExamAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.SpinnerExamAdapter;
import topgrade.parent.com.parentseeks.Teacher.Interface.MarksEnterInterface;
import topgrade.parent.com.parentseeks.Teacher.Interface.SmsCheck;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ExamResultModel;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ExamTestRespone;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamResult;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamSubmitStrcu;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamTest;
import topgrade.parent.com.parentseeks.Teacher.Model.SectionTest;
import topgrade.parent.com.parentseeks.Teacher.Model.SubjectTest;
import topgrade.parent.com.parentseeks.Teacher.Model.Teach;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExamSubmit extends AppCompatActivity implements OnClickListener, View.OnClickListener, MarksEnterInterface, CompoundButton.OnCheckedChangeListener, SmsCheck, java.io.Serializable {


    RecyclerView myrecycleview;
    ImageView back_icon, Cancel;

    AlertDialog.Builder builder;
    AlertDialog dialog;
    CheckBox sms_check;
    Button Save_Note, Cancel_, Submit_Marks_;
    TextView total_records;
    TextView attendence_date, show_advanced_filter;
    Context context;
    List<String> classList = new ArrayList<>();
    ArrayList<SubjectTest> subjectList = new ArrayList<>();
    ArrayList<SectionTest> sectionList = new ArrayList<>();
    ArrayList<ExamTest> examList = new ArrayList<>();
    List<String> testNameList = new ArrayList<>();
    List<ExamSubmitStrcu> exam_submit_list = new ArrayList<>();
    List<ExamResult> resultList = new ArrayList<>();
    private ProgressBar progress_bar;
    SearchableSpinner classSpinner, examSpinner, examSessionSpinner, subjectSpinner, sectionSpinner;
    String class_id, subject_id, total_marks, test_id, sectionId, session_id;
    List<String> exam_session_name_list = new ArrayList<>();
    List<ExamSession> examSessionslist = new ArrayList<>();
    ArrayAdapter<String> session_adaptor;
    androidx.appcompat.app.AlertDialog alertDialog;
    EditText staff_pwd;

    Button Submit_Marks;
    List<String> classForSubjectList = new ArrayList<>();
    
    // Flag to track if this is the first launch
    private boolean isFirstLaunch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_exam);
        
        // Initialize UI elements
        total_records = findViewById(R.id.total_records);
        attendence_date = findViewById(R.id.attendence_date);
        sms_check = findViewById(R.id.sms_check);
        progress_bar = findViewById(R.id.progress_bar);
        myrecycleview = findViewById(R.id.exam_rcv);
        Submit_Marks = findViewById(R.id.Submit_Marks);
        
        // Initialize selection button
        Button selection_button = findViewById(R.id.selection_button);

        // Back button click listener
        ImageView back_icon = findViewById(R.id.back_icon);
        if (back_icon != null) {
            back_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if data section is visible, go back to selection page first
                    ScrollView dataSection = findViewById(R.id.data_section);
                    if (dataSection != null && dataSection.getVisibility() == View.VISIBLE) {
                        showSelectionPage();
                    } else {
                        finish();
                    }
                }
            });
        }

        context = ExamSubmit.this;
        Paper.init(context);

        // Set click listeners
        if (selection_button != null) {
            selection_button.setOnClickListener(this);
        }
        
        if (Submit_Marks != null) {
            Submit_Marks.setOnClickListener(this);
        }
        
        // Add null check for sms_check to prevent crash
        if (sms_check != null) {
            sms_check.setOnCheckedChangeListener(this);
        }
        
        // Setup RecyclerView with LayoutManager
        if (myrecycleview != null) {
            myrecycleview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            myrecycleview.setHasFixedSize(true);
            Log.d("ExamSubmit", "RecyclerView setup completed");
        } else {
            Log.e("ExamSubmit", "RecyclerView not found in layout");
        }
        
        // Set dynamic header title based on mode
        updateHeaderTitleBasedOnMode();
        
        // Show selection page initially
        showSelectionPage();
        
        // Show advanced options
        Log.d("ExamSubmit", "=== ONCREATE COMPLETED - SHOWING ADVANCED OPTIONS ===");
        Show_Advanced_Option();


    }
    
    
    /**
     * Safely set progress bar visibility with null check
     */
    private void setProgressBarVisibility(int visibility) {
        if (progress_bar != null) {
            progress_bar.setVisibility(visibility);
        }
    }
    
    /**
     * Safely set RecyclerView visibility with null check
     */
    private void setRecyclerViewVisibility(int visibility) {
        if (myrecycleview != null) {
            myrecycleview.setVisibility(visibility);
        }
    }
    
    /**
     * Show selection page and hide data section
     */
    private void showSelectionPage() {
        LinearLayout selectionPage = findViewById(R.id.selection_page);
        ScrollView dataSection = findViewById(R.id.data_section);
        
        if (selectionPage != null) {
            selectionPage.setVisibility(View.VISIBLE);
        }
        if (dataSection != null) {
            dataSection.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show data section and hide selection page
     */
    private void showDataSection() {
        LinearLayout selectionPage = findViewById(R.id.selection_page);
        ScrollView dataSection = findViewById(R.id.data_section);
        
        if (selectionPage != null) {
            selectionPage.setVisibility(View.GONE);
        }
        if (dataSection != null) {
            dataSection.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Update header title and button text dynamically based on the mode passed via intent
     */
    private void updateHeaderTitleBasedOnMode() {
        try {
            TextView header_title = findViewById(R.id.header_title);
            Button submit_marks_button = findViewById(R.id.Submit_Marks);
            
            // Get mode from intent
            String mode = getIntent().getStringExtra("MODE");
            
            if (mode == null || mode.equalsIgnoreCase("SUBMIT")) {
                // Default mode: Submit new marks
                if (header_title != null) {
                    header_title.setText("Exam Mark Entry");
                }
                if (submit_marks_button != null) {
                    submit_marks_button.setText("Submit Marks");
                    submit_marks_button.setEnabled(true);
                }
                Log.d("ExamSubmit", "Header title set to: Exam Mark Entry (Mode: " + (mode != null ? mode : "Default") + ")");
            } else if (mode.equalsIgnoreCase("UPDATE")) {
                if (header_title != null) {
                    header_title.setText("Update Exam Marks");
                }
                if (submit_marks_button != null) {
                    submit_marks_button.setText("Update Marks");
                    submit_marks_button.setEnabled(true);
                }
                Log.d("ExamSubmit", "Header title set to: Update Exam Marks");
            } else if (mode.equalsIgnoreCase("VIEW") || mode.equalsIgnoreCase("VIEW_RESULTS")) {
                if (header_title != null) {
                    header_title.setText("View Exam Results");
                }
                if (submit_marks_button != null) {
                    submit_marks_button.setText("View Results");
                    submit_marks_button.setEnabled(false); // Disable editing in view mode
                }
                Log.d("ExamSubmit", "Header title set to: View Exam Results (Read-only)");
            } else {
                // Default mode: Submit new marks (fallback for unknown modes)
                if (header_title != null) {
                    header_title.setText("Exam Mark Entry");
                }
                if (submit_marks_button != null) {
                    submit_marks_button.setText("Submit Marks");
                    submit_marks_button.setEnabled(true);
                }
                Log.d("ExamSubmit", "Header title set to: Exam Mark Entry (Unknown mode: " + mode + ")");
            }
        } catch (Exception e) {
            Log.e("ExamSubmit", "Error updating header title and button", e);
        }
    }

    private void Load_Class() {
        final HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("session_id", Constant.current_session);

        setProgressBarVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.load_profile(body).enqueue(new Callback<TeachModel>() {
            @Override
            public void onResponse(Call<TeachModel> call, Response<TeachModel> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        sectionList.clear();
                        final List<Teach> list = response.body().getTeach();
//                        final List<TeachSection> sectionList = response.body().getTeachSection();


                        for (Teach teachSection : list) {
                            if (!classForSubjectList.contains(teachSection.getClassName())){
                                classForSubjectList.add(teachSection.getClassName());
                            }
                        }


                        ArrayAdapter student_class_adaptor = new ArrayAdapter<String>(context, R.layout.simple_list_item_1,
                                classForSubjectList);
                        classSpinner.setAdapter(student_class_adaptor);
                        
                        // Auto-select first item if available
                        if (classForSubjectList.size() > 0) {
                            classSpinner.setSelection(0);
                        }

                        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                class_id = list.get(position).getStudentClassId();
//                                subjectId = list.get(position).getSubjectId();

                                ///////////////Setup Section List/////////////////
                                ArrayList<String> sectionNames = new ArrayList<>();
                                ArrayList<SectionTest> filteredSections = new ArrayList<>();
                                for (Teach item : list) {
                                    if (item.getStudentClassId().equals(class_id)){
                                        if (!sectionNames.contains(item.getSectionName())){
                                            sectionNames.add(item.getSectionName());
                                            filteredSections.add(new SectionTest(
                                                    item.getSectionId(),
                                                    item.getSectionName()
                                            ));
                                        }
                                    }
                                }

                                ///////////////Setup Subject List/////////////////
                                ArrayList<String> subjectNames = new ArrayList<>();
                                subjectList.clear();
                                for (Teach item : list) {
                                    if (item.getStudentClassId().equals(class_id)){
                                        if (!subjectNames.contains(item.getSubjectName())){
                                            subjectNames.add(item.getSubjectName());
                                            subjectList.add(new SubjectTest(
                                                    item.getSubjectId(),
                                                    item.getSubjectName()
                                            ));
                                        }
                                    }

                                }


                                setupSectionAdapter(filteredSections, (ArrayList<Teach>) list);
                                updateSelectionStatus();

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        setProgressBarVisibility(View.GONE);
                    } else {
                        setProgressBarVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    setProgressBarVisibility(View.GONE);

                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<TeachModel> call, Throwable e) {
                e.printStackTrace();
                setProgressBarVisibility(View.GONE);
            }
        });
    }



    private void setupSectionAdapter(ArrayList<SectionTest> filteredSections, ArrayList<Teach> list) {
        ArrayAdapter<SectionTest> sectionAdapter = new ArrayAdapter(context, R.layout.simple_list_item_1,
                filteredSections);
        sectionSpinner.setAdapter(sectionAdapter);
        
        // Auto-select first item if available
        if (filteredSections.size() > 0) {
            sectionSpinner.setSelection(0);
        }

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subjectList.clear();
                sectionId = filteredSections.get(position).getSectionId();
                ArrayList<String> subjectNames = new ArrayList<>();

                for (Teach item : list) {
                    if (item.getStudentClassId().equals(class_id) && item.getSectionId().equals(sectionId)){
                        if (!subjectNames.contains(item.getSubjectName())){
                            subjectNames.add(item.getSubjectName());
                            subjectList.add(new SubjectTest(
                                    item.getSubjectId(),
                                    item.getSubjectName()
                            ));
                        }
                    }
                }
                setupSubjectAdapter();
                updateSelectionStatus();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void setupSubjectAdapter() {
        ArrayAdapter<SubjectTest> subjectAdapter = new ArrayAdapter(context, R.layout.simple_list_item_1,
                subjectList);
        subjectSpinner.setAdapter(subjectAdapter);
        
        // Auto-select first item if available
        if (subjectList.size() > 0) {
            subjectSpinner.setSelection(0);
        }

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                subject_id = subjectList.get(position).getSubjectId();
                loadExams();
                updateSelectionStatus();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadExams() {
        if (BuildConfig.DEBUG) {
            Log.d("ExamSubmit", "=== LOADING EXAMS ===");
        }

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("student_class_id", class_id);
        postParam.put("subject_id", subject_id);
        postParam.put("exam_session_id", session_id);
        postParam.put("section_id", sectionId);

        if (BuildConfig.DEBUG) {
            Log.d("ExamSubmit", "Loading exams with params: " + postParam.toString());
        }

        setProgressBarVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.load_exams(body).enqueue(new Callback<ExamTestRespone>() {
            @Override
            public void onResponse(Call<ExamTestRespone> call, Response<ExamTestRespone> response) {
                if (BuildConfig.DEBUG) {
                    Log.d("ExamSubmit", "API response received");
                }

                try {
                    if (response.body() != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d("ExamSubmit", "Status: " + response.body().getStatus().getCode() + 
                                  " - " + response.body().getStatus().getMessage());
                        }
                        
                        if (response.body().getStatus().getCode().equals("1000")) {
                            final List<ExamTest> testList = response.body().getExams();
                            examList.clear();
                            
                            if (BuildConfig.DEBUG) {
                                Log.d("ExamSubmit", "Total exams received: " + testList.size());
                            }
                            
                            for (int i = 0; i < testList.size(); i++){
                                ExamTest item = testList.get(i);
                                
                                // Use only the original name
                                String testName = item.getFullName();
                                
                                ExamTest examTest = new ExamTest(
                                                item.getUniqueId(),
                                                item.getTotalMarks(),
                                                testName
                                        );
                                examList.add(examTest);
                            }
                            setupExamAdapter(examList);
                            
                            // Auto-select if only one exam, or set default for multiple exams
                            if (examList.size() == 1) {
                                examSpinner.setSelection(0);
                                test_id = examList.get(0).getUniqueId();
                                total_marks = "" + examList.get(0).getTotalMarks();
                                if (BuildConfig.DEBUG) {
                                    Log.d("ExamSubmit", "Auto-selected exam ID: " + test_id);
                                }
                            } else if (examList.size() > 1) {
                                // For multiple exams, set the first one as default but don't auto-select in UI
                                test_id = examList.get(0).getUniqueId();
                                total_marks = "" + examList.get(0).getTotalMarks();
                                if (BuildConfig.DEBUG) {
                                    Log.d("ExamSubmit", "Multiple exams found - Default ID: " + test_id);
                                }
                            }

                            setProgressBarVisibility(View.GONE);
                        } else {
                            setProgressBarVisibility(View.GONE);
                            setRecyclerViewVisibility(View.GONE);
                            total_records.setText("Total Records: " + "0");
                            Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        setProgressBarVisibility(View.GONE);
                        setRecyclerViewVisibility(View.GONE);
                        Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setProgressBarVisibility(View.GONE);
                    Toast.makeText(context, "Error processing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ExamTestRespone> call, Throwable e) {

                e.printStackTrace();
                setProgressBarVisibility(View.GONE);
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupExamAdapter(ArrayList<ExamTest> examList) {
        if (BuildConfig.DEBUG) {
            Log.d("ExamSubmit", "Setting up exam adapter with " + this.examList.size() + " exams");
        }
        
        SpinnerExamAdapter examAdapter = new SpinnerExamAdapter(context, R.layout.simple_list_item_1,
                this.examList);
        examSpinner.setAdapter(examAdapter);
        
        // Auto-select first item if available
        if (this.examList.size() > 0) {
            examSpinner.setSelection(0);
        }

        examSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < ExamSubmit.this.examList.size()) {
                    test_id = ExamSubmit.this.examList.get(position).getUniqueId();
                    total_marks = "" + ExamSubmit.this.examList.get(position).getTotalMarks();
                    
                    // Update the show_advanced_filter TextView with selected exam name
                    String selectedExamName = ExamSubmit.this.examList.get(position).getFullName();
                    if (show_advanced_filter != null) {
                        show_advanced_filter.setText(selectedExamName);
                        Log.d("ExamSubmit", "Updated filter text to: " + selectedExamName);
                    }
                    
                    if (BuildConfig.DEBUG) {
                        Log.d("ExamSubmit", "Selected Exam ID: " + test_id + ", Total Marks: " + total_marks);
                    }
                    updateSelectionStatus();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {

        int id = view.getId();
        if (id == R.id.student_name) {
            startActivity(new Intent(ExamSubmit.this, TeacherChildProfile.class));

        } else if (id == R.id.attendence_note) {
            Show_Note_Dialog();
        }

    }


    private void Show_Note_Dialog() {

        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.enter_note, null);


        Cancel = dialogView.findViewById(R.id.Cancel);
        Cancel_ = dialogView.findViewById(R.id.Cancel_);
        Save_Note = dialogView.findViewById(R.id.Save_Note);


        Cancel.setOnClickListener(this);
        Cancel_.setOnClickListener(this);
        Save_Note.setOnClickListener(this);


        androidx.appcompat.app.AlertDialog.Builder dialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);


        alertDialog = dialogBuilder.create();
        alertDialog.show();


    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.back_icon) {
            finish();

        } else if (id == R.id.Save_Note) {
            Toast.makeText(this, "Save_Note", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.show_advanced_filter) {
            Show_Advanced_Option();

        } else if (id == R.id.selection_button) {
            // Navigate to separate ExamResults activity
            navigateToExamResults();


        } else if (id == R.id.search_filter) {
            // No dialog to dismiss since we're using main layout
            loadExamResult();

        } else if (id == R.id.Cancel || id == R.id.Cancel_) {
            // No dialog to dismiss since we're using main layout
            // Just clear any selections if needed

        } else if (id == R.id.Submit_Marks) {
            if (exam_submit_list.size() > 0) {
                // Check if all students have marks entered
                int studentsWithMarks = 0;
                int studentsWithoutMarks = 0;
                
                for (int i = 0; i < exam_submit_list.size(); i++) {
                    String marks = exam_submit_list.get(i).getMarks();
                    if (marks != null && !marks.isEmpty() && !marks.trim().equals("")) {
                        studentsWithMarks++;
                    } else {
                        studentsWithoutMarks++;
                    }
                }

                if (studentsWithoutMarks == 0) {
                    // All students have marks, proceed to password dialog
                    Show_Pwd_Dialog();
                } else {
                    // Show detailed feedback about missing marks
                    String message = String.format("Please enter marks for %d student(s). %d student(s) already have marks entered.", 
                                                  studentsWithoutMarks, studentsWithMarks);
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "No students available for submitting marks. Please search for students first.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void Show_Pwd_Dialog() {
        // Show confirmation dialog with student count
        String studentCount = String.valueOf(exam_submit_list.size());
        String message = "You are about to submit marks for " + studentCount + " student(s).\n\nPlease enter your password to confirm:";
        
        androidx.appcompat.app.AlertDialog.Builder builder = new
                androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Confirm Submission")
               .setMessage(message)
               .setView(R.layout.enter_pwd);
        alertDialog = builder.create();

        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        
        staff_pwd = alertDialog.findViewById(R.id.staff_pwd);
        Submit_Marks_ = alertDialog.findViewById(R.id.Submit_Marks_);
        ImageView Cancel = alertDialog.findViewById(R.id.Cancel);
        Button Cancel_ = alertDialog.findViewById(R.id.Cancel_);

        Cancel.setOnClickListener(this);
        Cancel_.setOnClickListener(this);
        Submit_Marks_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String staff_pwd_ = staff_pwd.getText().toString();
                String staff_pwd_original = Paper.book().read("staff_password");
                
                if (staff_pwd_.isEmpty()) {
                    Toast.makeText(context, "Please enter your password to submit marks", Toast.LENGTH_SHORT).show();
                } else if (!staff_pwd_.equals(staff_pwd_original)) {
                    Toast.makeText(context, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show();
                    staff_pwd.setText(""); // Clear the password field
                } else {
                    alertDialog.dismiss();
                    Submit_Marks_API();
                }
            }
        });
    }

    private void Submit_Marks_API() {
        Log.d("ExamSubmit", "Starting marks submission for " + exam_submit_list.size() + " students");
        
        JSONArray jsonArray = new JSONArray();
        Gson gson = new Gson();

        String listString = gson.toJson(
                exam_submit_list,
                new TypeToken<ArrayList<ExamSubmitStrcu>>() {
                }.getType());

        try {
            jsonArray = new JSONArray(listString);
            Log.d("ExamSubmit", "Successfully converted exam data to JSON array");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ExamSubmit", "Error converting exam data to JSON", e);
            Toast.makeText(context, "Error preparing data for submission", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> postParam = new HashMap<String, Object>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("student_class_id", class_id);
        postParam.put("subject_id", subject_id);
        postParam.put("section_id", sectionId);
        postParam.put("exam_id", test_id);
        postParam.put("results", jsonArray);

        JSONObject jsonObject = new JSONObject(postParam);
        Log.d("ExamSubmit", "Submission data: " + jsonObject.toString());

        setProgressBarVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.save_exam_results(body).enqueue(new Callback<ExamResultModel>() {
            @Override
            public void onResponse(Call<ExamResultModel> call, Response<ExamResultModel> response) {
                setProgressBarVisibility(View.GONE);
                
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        Log.d("ExamSubmit", "Marks submitted successfully");
                        Toast.makeText(context, "Marks submitted successfully!", Toast.LENGTH_LONG).show();
                        
                        // Optionally refresh the data or clear the form
                        // You can add logic here to refresh the student list or clear marks
                        
                    } else {
                        Log.e("ExamSubmit", "API Error: " + response.body().getStatus().getMessage());
                        Toast.makeText(context, "Submission failed: " + response.body().getStatus().getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("ExamSubmit", "Response body is null");
                    Toast.makeText(context, "No response from server. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ExamResultModel> call, Throwable e) {
                setProgressBarVisibility(View.GONE);
                Log.e("ExamSubmit", "Network error during submission", e);
                Toast.makeText(context, "Network error. Please check your connection and try again.", Toast.LENGTH_LONG).show();
            }
        });

    }


    private void Show_Advanced_Option() {
        Log.d("ExamSubmit", "=== INITIALIZING SELECTION INTERFACE ===");
        Log.d("ExamSubmit", "Context: " + (context != null ? "Valid" : "NULL"));
        
        try {
            // Get spinners from main layout instead of dialog
            Log.d("ExamSubmit", "Finding components from main layout...");
            examSessionSpinner = findViewById(R.id.exam_session_spinner);
            classSpinner = findViewById(R.id.class_spinner);
            sectionSpinner = findViewById(R.id.section_spinner);
            subjectSpinner = findViewById(R.id.subject_spinner);
            examSpinner = findViewById(R.id.exam_spinner);
            
            Log.d("ExamSubmit", "Component initialization results:");
            Log.d("ExamSubmit", "  - examSessionSpinner: " + (examSessionSpinner != null ? "FOUND" : "NULL"));
            Log.d("ExamSubmit", "  - classSpinner: " + (classSpinner != null ? "FOUND" : "NULL"));
            Log.d("ExamSubmit", "  - sectionSpinner: " + (sectionSpinner != null ? "FOUND" : "NULL"));
            Log.d("ExamSubmit", "  - subjectSpinner: " + (subjectSpinner != null ? "FOUND" : "NULL"));
            Log.d("ExamSubmit", "  - examSpinner: " + (examSpinner != null ? "FOUND" : "NULL"));

            if (examSessionSpinner != null) {
                examSessionSpinner.setTitle("Select Exam Session");
                Log.d("ExamSubmit", "Exam session spinner title set");
            } else {
                Log.e("ExamSubmit", "CRITICAL: examSessionSpinner not found in main layout!");
            }
            
            if (classSpinner != null) classSpinner.setTitle("Select Class");
            if (sectionSpinner != null) sectionSpinner.setTitle("Select Section");
            if (subjectSpinner != null) subjectSpinner.setTitle("Select Subject");
            if (examSpinner != null) examSpinner.setTitle("Select Exam");

            Log.d("ExamSubmit", "Starting to load exam sessions...");
            loadExamSession();
        } catch (Exception e) {
            Log.e("ExamSubmit", "CRITICAL ERROR in Show_Advanced_Option", e);
            Log.e("ExamSubmit", "Exception type: " + e.getClass().getSimpleName());
            Log.e("ExamSubmit", "Exception message: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, "Error initializing selection interface: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadExamSession() {
        Log.d("ExamSubmit", "=== LOADING EXAM SESSIONS ===");
        Log.d("ExamSubmit", "Context: " + (context != null ? "Valid" : "NULL"));
        Log.d("ExamSubmit", "Always loading fresh data from API to ensure latest exam sessions");
        
        // Always load fresh data from API instead of using cached data
        loadExamSessionsFromAPI();
    }
    
    private void loadExamSessionFromCache() {
        Log.d("ExamSubmit", "=== LOADING EXAM SESSIONS FROM CACHE ===");
        Log.d("ExamSubmit", "Context: " + (context != null ? "Valid" : "NULL"));
        Log.d("ExamSubmit", "Paper initialized: " + (Paper.book() != null ? "Yes" : "No"));
        Log.d("ExamSubmit", "Constants.exam_session key: " + Constants.exam_session);
        
        try {
            examSessionslist = Paper.book().read(Constants.exam_session);
            Log.d("ExamSubmit", "PaperDB read result: " + (examSessionslist != null ? "SUCCESS" : "NULL"));
            
            if (examSessionslist != null) {
                Log.d("ExamSubmit", "Exam sessions list size: " + examSessionslist.size());
                for (int i = 0; i < examSessionslist.size(); i++) {
                    ExamSession session = examSessionslist.get(i);
                    Log.d("ExamSubmit", "  Session " + i + ": " + session.getFullName() + " (ID: " + session.getUniqueId() + ")");
                }
            }
            
            // Check if examSessionslist is null or empty
            if (examSessionslist == null || examSessionslist.isEmpty()) {
                Log.w("ExamSubmit", "No exam sessions found in PaperDB - loading from API");
                loadExamSessionsFromAPI();
                return;
            }
            
            Log.d("ExamSubmit", "Processing exam sessions from PaperDB (cached)...");
            exam_session_name_list = new ArrayList<>();
            for (int i = 0; i < examSessionslist.size(); i++) {
                String sessionName = examSessionslist.get(i).getFullName();
                exam_session_name_list.add(sessionName);
                Log.d("ExamSubmit", "  Session Name [" + i + "]: '" + sessionName + "'");
            }

            Log.d("ExamSubmit", "Creating ArrayAdapter with " + exam_session_name_list.size() + " items");
            session_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                    exam_session_name_list);

            Log.d("ExamSubmit", "Setting adapter to examSessionSpinner...");
            if (examSessionSpinner != null) {
                examSessionSpinner.setAdapter(session_adaptor);
                Log.d("ExamSubmit", "Adapter set successfully");
                
                // Auto-select first item if available
                if (exam_session_name_list.size() > 0) {
                    examSessionSpinner.setSelection(0);
                    Log.d("ExamSubmit", "Auto-selected first exam session");
                }
            } else {
                Log.e("ExamSubmit", "CRITICAL: examSessionSpinner is NULL!");
                return;
            }

            Log.d("ExamSubmit", "Setting up exam session selection listener...");
            examSessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("ExamSubmit", "Exam session selected at position: " + position);
                    if (position >= 0 && position < examSessionslist.size()) {
                        session_id = examSessionslist.get(position).getUniqueId();
                        Log.d("ExamSubmit", "Selected exam session: " + examSessionslist.get(position).getFullName() + " (ID: " + session_id + ")");
                        Log.d("ExamSubmit", "Calling Load_Class()...");
                        Load_Class();
                    } else {
                        Log.e("ExamSubmit", "Invalid position selected: " + position + " (list size: " + examSessionslist.size() + ")");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d("ExamSubmit", "No exam session selected");
                }
            });
            
            Log.d("ExamSubmit", "Exam session loading completed successfully");
        } catch (Exception e) {
            Log.e("ExamSubmit", "CRITICAL ERROR in loadExamSession", e);
            Log.e("ExamSubmit", "Exception type: " + e.getClass().getSimpleName());
            Log.e("ExamSubmit", "Exception message: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, "Error loading exam sessions: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadExamSessionsFromAPI() {
        Log.d("ExamSubmit", "=== LOADING EXAM SESSIONS FROM API ===");
        Log.d("ExamSubmit", "Constant.campus_id: " + Constant.campus_id);
        Log.d("ExamSubmit", "Constant.staff_id: " + Constant.staff_id);
        Log.d("ExamSubmit", "Constant.mApiService: " + (Constant.mApiService != null ? "Valid" : "NULL"));
        
        try {
            // Load constants from PaperDB to ensure we have the latest values
            Constant.loadFromPaper();
            
            // Validate required parameters
            if (Constant.campus_id == null || Constant.campus_id.isEmpty()) {
                Log.e("ExamSubmit", "Campus ID is empty. Please login again.");
                Toast.makeText(context, "Missing login information. Please login again.", Toast.LENGTH_SHORT).show();
                setProgressBarVisibility(View.GONE);
                return;
            }
            if (Constant.staff_id == null || Constant.staff_id.isEmpty()) {
                Log.e("ExamSubmit", "Staff ID is empty. Please login again.");
                Toast.makeText(context, "Missing login information. Please login again.", Toast.LENGTH_SHORT).show();
                setProgressBarVisibility(View.GONE);
                return;
            }
            if (Constant.current_session == null || Constant.current_session.isEmpty()) {
                Log.e("ExamSubmit", "Current session is empty. Please login again.");
                Toast.makeText(context, "Missing session information. Please login again.", Toast.LENGTH_SHORT).show();
                setProgressBarVisibility(View.GONE);
                return;
            }
            
            HashMap<String, String> postParam = new HashMap<String, String>();
            postParam.put("parent_id", Constant.campus_id);  // API parameter 'parent_id' = campus_id from login
            postParam.put("staff_id", Constant.staff_id);    // Staff ID from login
            postParam.put("session_id", Constant.current_session);  // Current academic session
            
            Log.d("ExamSubmit", "API parameters: " + postParam.toString());
            Log.d("ExamSubmit", "Using dynamic parameters - parent_id (campus): " + Constant.campus_id + 
                  ", staff_id: " + Constant.staff_id + ", session_id: " + Constant.current_session);
            Log.d("ExamSubmit", "Note: API parameter 'parent_id' contains campus_id value");
            
            setProgressBarVisibility(View.VISIBLE);
            String jsonString = (new JSONObject(postParam)).toString();
            Log.d("ExamSubmit", "JSON request body: " + jsonString);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
            
            Log.d("ExamSubmit", "Calling load_exam_session_teacher API...");
            Constant.mApiService.load_exam_session_teacher(body).enqueue(new Callback<topgrade.parent.com.parentseeks.Parent.Model.SessionModel>() {
                @Override
                public void onResponse(Call<topgrade.parent.com.parentseeks.Parent.Model.SessionModel> call, Response<topgrade.parent.com.parentseeks.Parent.Model.SessionModel> response) {
                    setProgressBarVisibility(View.GONE);
                    Log.d("ExamSubmit", "load_exam_session_teacher API response received");
                    Log.d("ExamSubmit", "Response code: " + response.code());
                    Log.d("ExamSubmit", "Response is successful: " + response.isSuccessful());
                    
                    if (response.body() != null) {
                        Log.d("ExamSubmit", "Response body is valid");
                        Log.d("ExamSubmit", "Status code: " + response.body().getStatus().getCode());
                        Log.d("ExamSubmit", "Status message: " + response.body().getStatus().getMessage());
                        
                        if (response.body().getStatus().getCode().equals("1000")) {
                            Log.d("ExamSubmit", "API call successful, processing exam sessions...");
                            examSessionslist = response.body().getExamSession();
                            
                            Log.d("ExamSubmit", "Exam sessions list from API: " + (examSessionslist != null ? "NOT NULL" : "NULL"));
                            if (examSessionslist != null) {
                                Log.d("ExamSubmit", "Exam sessions list size: " + examSessionslist.size());
                            }
                            
                            if (examSessionslist != null && !examSessionslist.isEmpty()) {
                                Log.d("ExamSubmit", "Received " + examSessionslist.size() + " exam sessions from API");
                                for (int i = 0; i < examSessionslist.size(); i++) {
                                    ExamSession session = examSessionslist.get(i);
                                    Log.d("ExamSubmit", "  API Session " + i + ": " + session.getFullName() + " (ID: " + session.getUniqueId() + ")");
                                }
                                
                                // Save to PaperDB for future use
                                try {
                                    Paper.book().write(Constants.exam_session, examSessionslist);
                                    Log.d("ExamSubmit", "Saved " + examSessionslist.size() + " exam sessions to PaperDB");
                                } catch (Exception e) {
                                    Log.e("ExamSubmit", "Error saving to PaperDB", e);
                                }
                                
                                // Process the sessions
                                processExamSessions();
                            } else {
                                Log.w("ExamSubmit", "No exam sessions returned from API - list is null or empty");
                                Log.w("ExamSubmit", "examSessionslist is null: " + (examSessionslist == null));
                                if (examSessionslist != null) {
                                    Log.w("ExamSubmit", "examSessionslist size: " + examSessionslist.size());
                                }
                                Toast.makeText(context, "No exam sessions are currently available. Please contact your administrator to set up exam sessions.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e("ExamSubmit", "API Error - Status code: " + response.body().getStatus().getCode());
                            Log.e("ExamSubmit", "API Error - Message: " + response.body().getStatus().getMessage());
                            Toast.makeText(context, "Error loading exam sessions: " + response.body().getStatus().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e("ExamSubmit", "Response body is null");
                        Log.e("ExamSubmit", "Raw response: " + response.raw());
                        Toast.makeText(context, "No response from server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<topgrade.parent.com.parentseeks.Parent.Model.SessionModel> call, Throwable e) {
                    setProgressBarVisibility(View.GONE);
                    Log.e("ExamSubmit", "Network error loading exam sessions from API", e);
                    Log.e("ExamSubmit", "Error type: " + e.getClass().getSimpleName());
                    Log.e("ExamSubmit", "Error message: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(context, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            setProgressBarVisibility(View.GONE);
            Log.e("ExamSubmit", "Exception loading exam sessions from API", e);
            Log.e("ExamSubmit", "Exception type: " + e.getClass().getSimpleName());
            Log.e("ExamSubmit", "Exception message: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, "Error loading exam sessions: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void processExamSessions() {
        Log.d("ExamSubmit", "=== PROCESSING EXAM SESSIONS ===");
        
        if (examSessionslist == null || examSessionslist.isEmpty()) {
            Log.w("ExamSubmit", "No exam sessions to process");
            return;
        }
        
        Log.d("ExamSubmit", "Processing " + examSessionslist.size() + " exam sessions...");
        exam_session_name_list = new ArrayList<>();
        for (int i = 0; i < examSessionslist.size(); i++) {
            String sessionName = examSessionslist.get(i).getFullName();
            exam_session_name_list.add(sessionName);
            Log.d("ExamSubmit", "  Session Name [" + i + "]: '" + sessionName + "'");
        }

        Log.d("ExamSubmit", "Creating ArrayAdapter with " + exam_session_name_list.size() + " items");
        session_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                exam_session_name_list);

        Log.d("ExamSubmit", "Setting adapter to examSessionSpinner...");
        if (examSessionSpinner != null) {
            examSessionSpinner.setAdapter(session_adaptor);
            Log.d("ExamSubmit", "Adapter set successfully");
            
            // Auto-select first item if available
            if (exam_session_name_list.size() > 0) {
                examSessionSpinner.setSelection(0);
                Log.d("ExamSubmit", "Auto-selected first exam session");
            }
        } else {
            Log.e("ExamSubmit", "CRITICAL: examSessionSpinner is NULL in processExamSessions!");
            return;
        }

        Log.d("ExamSubmit", "Setting up exam session selection listener...");
        examSessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ExamSubmit", "Exam session selected at position: " + position);
                if (position >= 0 && position < examSessionslist.size()) {
                    session_id = examSessionslist.get(position).getUniqueId();
                    Log.d("ExamSubmit", "Selected exam session: " + examSessionslist.get(position).getFullName() + " (ID: " + session_id + ")");
                    Log.d("ExamSubmit", "Calling Load_Class()...");
                    Load_Class();
                } else {
                    Log.e("ExamSubmit", "Invalid position selected: " + position + " (list size: " + examSessionslist.size() + ")");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("ExamSubmit", "No exam session selected");
            }
        });
        
        Log.d("ExamSubmit", "Exam session processing completed successfully");
    }

    private void clearSpinnerReferences() {
        Log.d("ExamSubmit", "Clearing spinner references to prevent serialization issues");
        try {
            if (examSessionSpinner != null) {
                examSessionSpinner.setOnItemSelectedListener(null);
                examSessionSpinner.setAdapter(null);
                examSessionSpinner = null;
            }
            if (classSpinner != null) {
                classSpinner.setOnItemSelectedListener(null);
                classSpinner.setAdapter(null);
                classSpinner = null;
            }
            if (sectionSpinner != null) {
                sectionSpinner.setOnItemSelectedListener(null);
                sectionSpinner.setAdapter(null);
                sectionSpinner = null;
            }
            if (subjectSpinner != null) {
                subjectSpinner.setOnItemSelectedListener(null);
                subjectSpinner.setAdapter(null);
                subjectSpinner = null;
            }
            if (examSpinner != null) {
                examSpinner.setOnItemSelectedListener(null);
                examSpinner.setAdapter(null);
                examSpinner = null;
            }
        } catch (Exception e) {
            Log.e("ExamSubmit", "Error clearing spinner references", e);
        }
    }



    /**
     * Navigate to ExamResults activity with all selection data
     */
    private void navigateToExamResults() {
        try {
            // Validate that all required parameters are set
            if (class_id == null || class_id.isEmpty()) {
                Toast.makeText(context, "Please select a class", Toast.LENGTH_SHORT).show();
                return;
            }
            if (subject_id == null || subject_id.isEmpty()) {
                Toast.makeText(context, "Please select a subject", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sectionId == null || sectionId.isEmpty()) {
                Toast.makeText(context, "Please select a section", Toast.LENGTH_SHORT).show();
                return;
            }
            if (test_id == null || test_id.isEmpty()) {
                Toast.makeText(context, "Please select an exam", Toast.LENGTH_SHORT).show();
                return;
            }
            if (session_id == null || session_id.isEmpty()) {
                Toast.makeText(context, "Please select an exam session", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get mode from intent
            String mode = getIntent().getStringExtra("MODE");
            boolean loadSubmittedData = getIntent().getBooleanExtra("LOAD_SUBMITTED_DATA", false);
            
            // Get selected names from spinners using helper methods (which clean the data)
            String sessionName = getSelectedSessionName();
            String className = getSelectedClassName();
            String sectionName = getSelectedSectionName();
            String subjectName = getSelectedSubjectName();
            String examName = getSelectedExamName();
            
            // Fallback to "N/A" if any value is null
            if (sessionName == null) sessionName = "N/A";
            if (className == null) className = "N/A";
            if (sectionName == null) sectionName = "N/A";
            if (subjectName == null) subjectName = "N/A";
            if (examName == null) examName = "N/A";
            
            Log.d("ExamSubmit", "Navigating to ExamResults with params:");
            Log.d("ExamSubmit", "MODE: " + mode);
            Log.d("ExamSubmit", "Session: " + sessionName + " (ID: " + session_id + ")");
            Log.d("ExamSubmit", "Class: " + className + " (ID: " + class_id + ")");
            Log.d("ExamSubmit", "Section: " + sectionName + " (ID: " + sectionId + ")");
            Log.d("ExamSubmit", "Subject: " + subjectName + " (ID: " + subject_id + ")");
            Log.d("ExamSubmit", "Exam: " + examName + " (ID: " + test_id + ")");
            
            // Create intent and pass all selection data
            Intent intent = new Intent(ExamSubmit.this, ExamResults.class);
            intent.putExtra("MODE", mode);
            intent.putExtra("SESSION_ID", session_id);
            intent.putExtra("SESSION_NAME", sessionName);
            intent.putExtra("CLASS_ID", class_id);
            intent.putExtra("CLASS_NAME", className);
            intent.putExtra("SECTION_ID", sectionId);
            intent.putExtra("SECTION_NAME", sectionName);
            intent.putExtra("SUBJECT_ID", subject_id);
            intent.putExtra("SUBJECT_NAME", subjectName);
            intent.putExtra("EXAM_ID", test_id);
            intent.putExtra("EXAM_NAME", examName);
            intent.putExtra("LOAD_SUBMITTED_DATA", loadSubmittedData);
            
            startActivity(intent);
            
        } catch (Exception e) {
            Log.e("ExamSubmit", "Error navigating to ExamResults", e);
            Toast.makeText(context, "Error loading results page", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadExamResult() {
        try {
            // Validate that all required parameters are set
            if (class_id == null || class_id.isEmpty()) {
                Toast.makeText(context, "Please select a class", Toast.LENGTH_SHORT).show();
                return;
            }
            if (subject_id == null || subject_id.isEmpty()) {
                Toast.makeText(context, "Please select a subject", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sectionId == null || sectionId.isEmpty()) {
                Toast.makeText(context, "Please select a section", Toast.LENGTH_SHORT).show();
                return;
            }
            if (test_id == null || test_id.isEmpty()) {
                Toast.makeText(context, "Please select an exam", Toast.LENGTH_SHORT).show();
                return;
            }
            if (session_id == null || session_id.isEmpty()) {
                Toast.makeText(context, "Please select an exam session", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d("ExamSubmit", "Loading exam results with params - session_id: " + session_id + 
                  ", class_id: " + class_id + ", subject_id: " + subject_id + ", sectionId: " + sectionId + ", test_id: " + test_id);
            
            HashMap<String, String> postParam = new HashMap<String, String>();
            postParam.put("staff_id", Constant.staff_id);
            postParam.put("parent_id", Constant.campus_id);
            postParam.put("student_class_id", class_id);
            postParam.put("subject_id", subject_id);
            postParam.put("section_id", sectionId);
            postParam.put("exam_id", test_id);
            
            JSONObject jsonObject = new JSONObject(postParam);
            Log.d("ExamSubmit", "API Request: " + jsonObject.toString());

            setProgressBarVisibility(View.VISIBLE);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    (new JSONObject(postParam)).toString());

            Constant.mApiService.load_exams_results(body).enqueue(new Callback<ExamResultModel>() {
                @Override
                public void onResponse(Call<ExamResultModel> call, Response<ExamResultModel> response) {
                    setProgressBarVisibility(View.GONE);
                    
                    if (response.body() != null) {
                        if (response.body().getStatus().getCode().equals("1000")) {
                            resultList = response.body().getResult();
                            
                            Log.d("ExamSubmit", "Exam results loaded successfully. Count: " + 
                                  (resultList != null ? resultList.size() : 0));

                            // Make List exam_submit_list
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

                                // Show data section with results
                                showDataSection();
                                
                                // Update the selection summary display
                                updateSelectionSummary();
                                
                                total_records.setText("Total Records: " + resultList.size());
                                setRecyclerViewVisibility(View.VISIBLE);
                                
                                if (myrecycleview != null) {
                                    ExamAdaptor adapter = new ExamAdaptor(context,
                                            ExamSubmit.this, resultList, exam_submit_list,
                                            ExamSubmit.this, ExamSubmit.this, total_marks,
                                            ExamSubmit.this);
                                    myrecycleview.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    
                                    Log.d("ExamSubmit", "Adapter set with " + resultList.size() + " students");
                                } else {
                                    Log.e("ExamSubmit", "RecyclerView is null - cannot set adapter");
                                }
                            } else {
                                Log.w("ExamSubmit", "No exam results found");
                                total_records.setText("Total Records: 0");
                                setRecyclerViewVisibility(View.GONE);
                                
                                // Keep selection page visible
                                showSelectionPage();
                                Toast.makeText(context, "No students found for this exam", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String message = response.body().getStatus().getMessage();
                            Log.e("ExamSubmit", "API Error: " + message);
                            setRecyclerViewVisibility(View.GONE);
                            total_records.setText("Total Records: 0");
                            
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("ExamSubmit", "Response body is null");
                        setRecyclerViewVisibility(View.GONE);
                        total_records.setText("Total Records: 0");
                        
                        Toast.makeText(context, "No response from server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ExamResultModel> call, Throwable e) {
                    setProgressBarVisibility(View.GONE);
                    Log.e("ExamSubmit", "Network error loading exam results", e);
                    setRecyclerViewVisibility(View.GONE);
                    total_records.setText("Total Records: 0");
                    
                    Toast.makeText(context, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            setProgressBarVisibility(View.GONE);
            Log.e("ExamSubmit", "Exception in loadExamResult", e);
            
            Toast.makeText(context, "Error loading exam results: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        String status = "";
        if (b) {
            status = "1";
        } else {
            status = "0";
        }
        for (int i = 0; i < exam_submit_list.size(); i++) {
            ExamSubmitStrcu result = exam_submit_list.get(i);
            result.setSms(status);
            exam_submit_list.set(i, result);
        }


        myrecycleview.setAdapter(new ExamAdaptor(context,
                ExamSubmit.this, resultList, exam_submit_list,
                ExamSubmit.this,
                ExamSubmit.this, total_marks,
                ExamSubmit.this));
    }

    @Override
    public void OnSmsCheck(boolean is_sms, int position) {
        String status = "";
        if (is_sms) {
            status = "1";
        } else {
            status = "0";
        }

        ExamSubmitStrcu result = exam_submit_list.get(position);
        result.setSms(status);
        exam_submit_list.set(position, result);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ExamSubmit", "=== onResume called ===");
        
        // Only reset and reload if this is NOT the first launch
        // (onCreate already handles the first load)
        if (!isFirstLaunch) {
            Log.d("ExamSubmit", "Not first launch - Reloading fresh data");
            
            // Reset all data when coming back to this screen
            resetAllSelections();
            
            // Reload fresh data
            Show_Advanced_Option();
        } else {
            Log.d("ExamSubmit", "First launch - Using onCreate data");
            isFirstLaunch = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear spinner references when activity is paused
        // Note: We no longer use a selection dialog, spinners are in main layout
        if (alertDialog != null && alertDialog.isShowing()) {
            // This is for password dialog only
            alertDialog.dismiss();
            alertDialog = null;
        }
        clearSpinnerReferences();
    }
    
    /**
     * Reset all selections and clear data when resuming the activity
     */
    private void resetAllSelections() {
        Log.d("ExamSubmit", "Resetting all selections to load fresh data");
        
        try {
            // Clear all data lists
            classList.clear();
            subjectList.clear();
            sectionList.clear();
            examList.clear();
            testNameList.clear();
            exam_submit_list.clear();
            resultList.clear();
            classForSubjectList.clear();
            exam_session_name_list.clear();
            
            // Clear cached exam sessions from PaperDB to force fresh API call
            try {
                Paper.book().delete(Constants.exam_session);
                Log.d("ExamSubmit", "Cleared exam session cache from PaperDB");
            } catch (Exception e) {
                Log.e("ExamSubmit", "Error clearing exam session cache", e);
            }
            
            // Reset all IDs
            class_id = null;
            subject_id = null;
            sectionId = null;
            test_id = null;
            session_id = null;
            total_marks = null;
            
            // Clear RecyclerView
            if (myrecycleview != null) {
                myrecycleview.setAdapter(null);
                myrecycleview.setVisibility(View.GONE);
            }
            
            // Reset total records
            if (total_records != null) {
                total_records.setText("Total Records: 0");
            }
            
            // Reset SMS checkbox
            if (sms_check != null) {
                sms_check.setChecked(false);
            }
            
            // Show selection page and hide data section
            showSelectionPage();
            
            Log.d("ExamSubmit", "All selections reset successfully");
        } catch (Exception e) {
            Log.e("ExamSubmit", "Error resetting selections", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearSpinnerReferences();
        // Clear all dialog references
        if (alertDialog != null) {
            alertDialog = null;
        }
        // Reset first launch flag for next time activity is created
        isFirstLaunch = true;
    }
    
    @Override
    public void onBackPressed() {
        // Check if data section is visible, go back to selection page first
        ScrollView dataSection = findViewById(R.id.data_section);
        if (dataSection != null && dataSection.getVisibility() == View.VISIBLE) {
            showSelectionPage();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void OnMarksEnter(int position, String obtained_marks) {
        if (!obtained_marks.isEmpty()) {
            double total_marks_ = Double.parseDouble(total_marks);
            double obtained_marks_ = Double.parseDouble(obtained_marks);

            ExamSubmitStrcu result = exam_submit_list.get(position);
            if (obtained_marks.equals("0")) {
                result.setAttendance("0");
            } else {
                result.setAttendance("1");
            }
            result.setMarks(obtained_marks);
            exam_submit_list.set(position, result);
            if (obtained_marks_ > total_marks_) {
                Toast.makeText(context, "Obainted marks is not greater than total marks", Toast.LENGTH_LONG).show();

            }

        }

    }

    private void updateSelectionStatus() {
        // Selection status TextViews removed from layout - status updates no longer needed
        // The selection state is now managed through the dialog and data loading
        
        /* Previously updated selection_status and selection_summary TextViews
        TextView selection_status = findViewById(R.id.selection_status);
        TextView selection_summary = findViewById(R.id.selection_summary);
        
        if (selection_status != null && selection_summary != null) {
            if (session_id != null && class_id != null && subject_id != null && sectionId != null && test_id != null) {
                selection_status.setText("✅ Exam Selection Complete");
                selection_summary.setText("Ready to load students");
            } else {
                selection_status.setText("🔍 Select Exam Criteria");
                selection_summary.setText("Tap to configure exam selection");
            }
        }
        */
    }
    
    /**
     * Load already submitted exam data for update mode
     */
    private void loadSubmittedExamData() {
        try {
            android.util.Log.d("ExamSubmit", "Loading submitted exam data for update mode");
            
            // TODO: Implement API call to load already submitted exam data
            // This should load the previously submitted marks for the current exam session
            
            // For now, we'll just log that we're in update mode
            android.util.Log.d("ExamSubmit", "Update mode: Will load previously submitted marks");
            
            // You can add your API call here to load submitted data
            // Example:
            // loadSubmittedMarksFromAPI();
            
        } catch (Exception e) {
            android.util.Log.e("ExamSubmit", "Error loading submitted exam data", e);
        }
    }
    
    /**
     * Update UI elements for update mode
     */
    private void updateUIForUpdateMode() {
        try {
            android.util.Log.d("ExamSubmit", "Updating UI for update mode");
            
            // Update button text to indicate update mode
            if (Submit_Marks != null) {
                Submit_Marks.setText("Update Marks");
            }
            
            // Update header title if needed
            TextView header_title = findViewById(R.id.header_title);
            if (header_title != null) {
                header_title.setText("Update Exam Marks");
            }
            
            // You can add more UI updates here for update mode
            // For example:
            // - Change button colors
            // - Add update-specific instructions
            // - Modify validation messages
            
            android.util.Log.d("ExamSubmit", "UI updated for update mode");
            
        } catch (Exception e) {
            android.util.Log.e("ExamSubmit", "Error updating UI for update mode", e);
        }
    }
    
    /**
     * Update UI elements for view results mode
     */
    private void updateUIForViewResultsMode() {
        try {
            android.util.Log.d("ExamSubmit", "Updating UI for view results mode");
            
            // Update button text to indicate view mode
            if (Submit_Marks != null) {
                Submit_Marks.setText("View Results");
                Submit_Marks.setEnabled(false); // Disable editing in view mode
            }
            
            // Update header title if needed
            TextView header_title = findViewById(R.id.header_title);
            if (header_title != null) {
                header_title.setText("View Exam Results");
            }
            
            // You can add more UI updates here for view results mode
            // For example:
            // - Disable input fields
            // - Change button colors to indicate read-only mode
            // - Add view-specific instructions
            
            android.util.Log.d("ExamSubmit", "UI updated for view results mode");
            
        } catch (Exception e) {
            android.util.Log.e("ExamSubmit", "Error updating UI for view results mode", e);
        }
    }
    
    /**
     * Update the selection summary display with actual selected values
     */
    private void updateSelectionSummary() {
        try {
            // Get the selected values from spinners
            String selectedSession = getSelectedSessionName();
            String selectedClass = getSelectedClassName();
            String selectedSection = getSelectedSectionName();
            String selectedSubject = getSelectedSubjectName();
            String selectedExam = getSelectedExamName();
            
            // Update the TextViews in the selection summary (using correct IDs from activity_staff_exam.xml)
            TextView sessionValue = findViewById(R.id.session_value);
            TextView classValue = findViewById(R.id.class_value);
            TextView sectionValue = findViewById(R.id.section_value);
            TextView subjectValue = findViewById(R.id.subject_value);
            TextView examValue = findViewById(R.id.exam_value);
            
            if (sessionValue != null) {
                sessionValue.setText(selectedSession != null ? selectedSession : "Session");
                android.util.Log.d("ExamSubmit", "Updated session_value: " + selectedSession);
            } else {
                android.util.Log.e("ExamSubmit", "session_value TextView not found!");
            }
            if (classValue != null) {
                classValue.setText(selectedClass != null ? selectedClass : "Class");
                android.util.Log.d("ExamSubmit", "Updated class_value: " + selectedClass);
            } else {
                android.util.Log.e("ExamSubmit", "class_value TextView not found!");
            }
            if (sectionValue != null) {
                sectionValue.setText(selectedSection != null ? selectedSection : "Section");
                android.util.Log.d("ExamSubmit", "Updated section_value: " + selectedSection);
            } else {
                android.util.Log.e("ExamSubmit", "section_value TextView not found!");
            }
            if (subjectValue != null) {
                subjectValue.setText(selectedSubject != null ? selectedSubject : "Subject");
                android.util.Log.d("ExamSubmit", "Updated subject_value: " + selectedSubject);
            } else {
                android.util.Log.e("ExamSubmit", "subject_value TextView not found!");
            }
            if (examValue != null) {
                examValue.setText(selectedExam != null ? selectedExam : "Exam");
                android.util.Log.d("ExamSubmit", "Updated exam_value: " + selectedExam);
            } else {
                android.util.Log.e("ExamSubmit", "exam_value TextView not found!");
            }
            
            // Update the date display
            TextView dateValue = findViewById(R.id.date_value);
            if (dateValue != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                String currentDate = dateFormat.format(new Date());
                dateValue.setText(currentDate);
                android.util.Log.d("ExamSubmit", "Updated date: " + currentDate);
            } else {
                android.util.Log.e("ExamSubmit", "date_value TextView not found!");
            }
            
        } catch (Exception e) {
            android.util.Log.e("ExamSubmit", "Error updating selection summary", e);
        }
    }
    
    /**
     * Get the selected session name from the spinner
     */
    private String getSelectedSessionName() {
        try {
            if (examSessionSpinner != null && examSessionSpinner.getSelectedItem() != null) {
                return examSessionSpinner.getSelectedItem().toString();
            }
        } catch (Exception e) {
            android.util.Log.e("ExamSubmit", "Error getting selected session name", e);
        }
        return null;
    }
    
    /**
     * Get the selected class name from the spinner
     */
    private String getSelectedClassName() {
        try {
            if (classSpinner != null && classSpinner.getSelectedItem() != null) {
                return classSpinner.getSelectedItem().toString();
            }
        } catch (Exception e) {
            android.util.Log.e("ExamSubmit", "Error getting selected class name", e);
        }
        return null;
    }
    
    /**
     * Get the selected section name from the spinner
     */
    private String getSelectedSectionName() {
        try {
            if (sectionSpinner != null && sectionSpinner.getSelectedItem() != null) {
                return sectionSpinner.getSelectedItem().toString();
            }
        } catch (Exception e) {
            android.util.Log.e("ExamSubmit", "Error getting selected section name", e);
        }
        return null;
    }
    
    /**
     * Get the selected subject name from the spinner
     */
    private String getSelectedSubjectName() {
        try {
            if (subjectSpinner != null && subjectSpinner.getSelectedItem() != null) {
                return subjectSpinner.getSelectedItem().toString();
            }
        } catch (Exception e) {
            android.util.Log.e("ExamSubmit", "Error getting selected subject name", e);
        }
        return null;
    }
    
    /**
     * Get the selected exam name from the spinner
     */
    private String getSelectedExamName() {
        try {
            if (examSpinner != null && examSpinner.getSelectedItem() != null) {
                return examSpinner.getSelectedItem().toString();
            }
        } catch (Exception e) {
            android.util.Log.e("ExamSubmit", "Error getting selected exam name", e);
        }
        return null;
    }
}
