package topgrade.parent.com.parentseeks.Teacher.Exam;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
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
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Model.ExamSession;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class CreateExam extends AppCompatActivity {

    private Context context;
    private ProgressBar progressBar;
    private EditText examName, startTime, endTime, syllabus;
    private SearchableSpinner examSessionSpinner, sectionsSpinner, subjectsSpinner;
    private MaterialButton createExamBtn;
    
    private List<ExamSession> examSessionsList = new ArrayList<>();
    private List<String> examSessionNames = new ArrayList<>();
    private String selectedSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_exam);
        
        context = this;
        Paper.init(context);
        
        initViews();
        setupClickListeners();
        loadExamSessions();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progress_bar);
        examName = findViewById(R.id.exam_name);
        startTime = findViewById(R.id.start_time);
        endTime = findViewById(R.id.end_time);
        syllabus = findViewById(R.id.syllabus);
        examSessionSpinner = findViewById(R.id.exam_session_spinner);
        sectionsSpinner = findViewById(R.id.sections_spinner);
        subjectsSpinner = findViewById(R.id.subjects_spinner);
        createExamBtn = findViewById(R.id.create_exam_btn);
    }

    private void setupClickListeners() {
        // Back button
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Create exam button
        createExamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createExam();
            }
        });
    }

    private void loadExamSessions() {
        examSessionsList = Paper.book().read(Constants.exam_session);
        examSessionNames = new ArrayList<>();
        
        if (examSessionsList != null) {
            for (ExamSession session : examSessionsList) {
                examSessionNames.add(session.getFullName());
            }
        }

        // Set up exam session spinner
        if (examSessionSpinner != null && !examSessionNames.isEmpty()) {
            examSessionSpinner.setAdapter(new android.widget.ArrayAdapter<>(context, 
                android.R.layout.simple_list_item_1, examSessionNames));
            
            examSessionSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    selectedSessionId = examSessionsList.get(position).getUniqueId();
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                }
            });
        }
    }

    private void createExam() {
        // Load constants from PaperDB
        Constant.loadFromPaper();
        
        // Validate login information
        if (Constant.campus_id == null || Constant.campus_id.isEmpty()) {
            Toast.makeText(context, "Missing campus information. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Constant.staff_id == null || Constant.staff_id.isEmpty()) {
            Toast.makeText(context, "Missing staff information. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate inputs
        if (examName.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Please enter exam name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSessionId == null) {
            Toast.makeText(context, "Please select exam session", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTime.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Please enter start time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endTime.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Please enter end time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare API request with dynamic parameters
        HashMap<String, Object> postParam = new HashMap<>();
        postParam.put("staff_id", Constant.staff_id);          // Staff ID from login
        postParam.put("parent_id", Constant.campus_id);        // API param 'parent_id' = campus_id
        postParam.put("full_name", examName.getText().toString().trim());
        postParam.put("start_time", startTime.getText().toString().trim());
        postParam.put("end_time", endTime.getText().toString().trim());
        postParam.put("syllabus", syllabus.getText().toString().trim());
        postParam.put("exam_session_id", selectedSessionId);

        // Mock sections and subjects data - replace with actual data
        JSONArray sectionsArray = new JSONArray();
        JSONArray subjectsArray = new JSONArray();
        
        try {
            // Add sample sections
            sectionsArray.put(new JSONObject().put("section", "A"));
            sectionsArray.put(new JSONObject().put("section", "B"));
            
            // Add sample subjects
            subjectsArray.put(new JSONObject()
                .put("subject", "Math")
                .put("teacher", Constant.staff_id)
                .put("total_marks", "100")
                .put("start_time", startTime.getText().toString().trim())
                .put("syllabus", syllabus.getText().toString().trim())
                .put("end_time", endTime.getText().toString().trim())
                .put("created_date", java.text.DateFormat.getDateTimeInstance().format(new java.util.Date())));
                
        } catch (JSONException e) {
            e.printStackTrace();
        }

        postParam.put("sections", sectionsArray);
        postParam.put("subjects", subjectsArray);

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        createExamBtn.setEnabled(false);

        // Make API call
        RequestBody body = RequestBody.create(
            new JSONObject(postParam).toString(),
            MediaType.parse("application/json; charset=utf-8")
        );

        BaseApiService apiService = API.getAPIService();
        Call<Object> call = apiService.createExam(body);
        
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                progressBar.setVisibility(View.GONE);
                createExamBtn.setEnabled(true);
                
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Exam created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(context, "Failed to create exam", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                createExamBtn.setEnabled(true);
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
