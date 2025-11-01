package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import components.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Parent.Adaptor.DiaryListAdapter;
import topgrade.parent.com.parentseeks.Parent.Adaptor.SubjectListAdapter;
import topgrade.parent.com.parentseeks.Parent.Utils.ChildListCrashFix;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import android.util.Log;
import topgrade.parent.com.parentseeks.Parent.Model.Diary;
import topgrade.parent.com.parentseeks.Parent.Model.DiaryEntry;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class LoadChildrenDiary extends AppCompatActivity {

    private Context context;


    Calendar calendar;

    private TextView select_date;
    private SearchableSpinner select_child_spinner;

    private DiaryListAdapter adapter; // Add the adapter variable.

    private SubjectListAdapter subjectListAdapter;

    private ArrayAdapter<String> child_adapter;

    List<DiaryEntry> subject_list = new ArrayList<>();

    List<DiaryEntry> subject_entry_list = new ArrayList<>();

    private RecyclerView main_rcv;

    private RecyclerView subjects_rcv;
    private List<String> student_name_list = new ArrayList<>();
    private List<SharedStudent> studentList = new ArrayList<>();
    private String seleted_child_id = "";

    private ProgressBar progress_bar;

    private String selected_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_children_diary);

        // Apply theme based on user type
        applyTheme();

        context = LoadChildrenDiary.this;
        subjectListAdapter = new SubjectListAdapter(subject_entry_list, context);
        adapter = new DiaryListAdapter(subject_list, context); // Initialize the adapter here.
        Paper.init(context);

        main_rcv = findViewById(R.id.main_rcv);
        subjects_rcv = findViewById(R.id.subjects_rcv);

        // Set up the RecyclerView with the adapter.
        subjects_rcv.setLayoutManager(new LinearLayoutManager(this));
        subjects_rcv.setAdapter(subjectListAdapter);

        // Set up the RecyclerView with the adapter.
        main_rcv.setLayoutManager(new LinearLayoutManager(this));
        main_rcv.setAdapter(adapter);

        select_child_spinner = findViewById(R.id.select_child_spinner);
        progress_bar = findViewById(R.id.progress_bar);

        innitlization();
        
        // Use the crash fix utility to safely load student list
        try {
            studentList = ChildListCrashFix.safeLoadStudentList(context);
            Log.d("LoadChildrenDiary", "SharedStudent list loaded safely: " + (studentList != null ? studentList.size() : "null") + " students");
        } catch (Exception e) {
            Log.e("LoadChildrenDiary", "Error loading student list safely", e);
            studentList = new ArrayList<>();
        }
        
        // Safely populate student name list
        student_name_list.clear();
        if (ChildListCrashFix.isStudentListSafe(studentList)) {
            for (SharedStudent student : studentList) {
                if (student != null && student.getFullName() != null && !student.getFullName().isEmpty()) {
                    student_name_list.add(student.getFullName());
                    Log.d("LoadChildrenDiary", "Added student: " + student.getFullName());
                } else {
                    Log.w("LoadChildrenDiary", "Invalid student found, skipping");
                }
            }
        } else {
            Log.w("LoadChildrenDiary", "SharedStudent list is not safe, showing empty state");
            // Add fallback message
            student_name_list.add("No Students Available");
        }
        Load_child();
    }

    private void innitlization() {
        context = LoadChildrenDiary.this;
        Paper.init(context);
    }

    private void reloadDiaryData() {
        if (seleted_child_id.isEmpty()) {
            Toast.makeText(context, "Please select a student first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load diary data for the selected student
        load_diary_main();
    }

    private void Load_child() {
        try {
            child_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, student_name_list);
            select_child_spinner.setAdapter(child_adapter);

            select_child_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        // Use safe method to get student at position
                        SharedStudent selectedStudent = ChildListCrashFix.getSafeStudentAt(studentList, position);
                        
                        if (selectedStudent != null) {
                            seleted_child_id = selectedStudent.getUniqueId();
                            Log.d("LoadChildrenDiary", "Selected student: " + selectedStudent.getFullName() + " with ID: " + seleted_child_id);
                            reloadDiaryData(); // Call the method to reload diary data
                        } else {
                            Toast.makeText(context, "Invalid student selection.", Toast.LENGTH_SHORT).show();
                            Log.w("LoadChildrenDiary", "Invalid student at position: " + position);
                        }
                    } catch (Exception e) {
                        Log.e("LoadChildrenDiary", "Error handling student selection", e);
                        Toast.makeText(context, "Error selecting student.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // No action needed
                }
            });
            
            Log.d("LoadChildrenDiary", "Child spinner setup completed successfully");
            
        } catch (Exception e) {
            Log.e("LoadChildrenDiary", "Error setting up child spinner", e);
            Toast.makeText(context, "Error setting up student selection.", Toast.LENGTH_SHORT).show();
        }
    }


    private void load_diary_main() {
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("parent_parent_id", Constant.parent_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("employee_id",seleted_child_id);
        postParam.put("date", selected_date);
        postParam.put("date2",selected_date);

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.load_diary(body).enqueue(new Callback<Diary>() {
            @Override
            public void onResponse(Call<Diary> call, Response<Diary> response) {
                progress_bar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Diary diary = response.body();
                                         if (diary != null && diary.getStatus() != null) {
                         if (diary.getStatus().status.getCode().equals("1000")) {
                             // Data is available and has status code 1000
                             DiaryEntry subjectData = diary.getTitle().get(Integer.parseInt("0")); // Get the subject data from "0" key
                             if (subjectData != null) {
                                 // Convert the DiaryEntry to a HashMap
                                 HashMap<String, DiaryEntry> subjectDataMap = new HashMap<>();
                                 subjectDataMap.put("0", subjectData);
 
                                 // Convert the subjectData HashMap to a List of DiaryEntry
                                 subject_list = new ArrayList<>(subjectDataMap.values());
                                 main_rcv.setAdapter(new DiaryListAdapter(subject_list, LoadChildrenDiary.this));
                             } else {
                                 // No subject data found for the selected child
                                 Toast.makeText(context, "No subject data found for the selected child.", Toast.LENGTH_SHORT).show();
                             }
                         } else {
                             // Data is not available or has a status code other than 1000
                             Toast.makeText(context, diary.getStatus().status.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                    } else {
                        Toast.makeText(context, "Response body is empty or invalid.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Response is not successful, show "no_records_text"
                    Toast.makeText(context, "Response was not successful.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Diary> call, Throwable e) {
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void load_subjects_diary() {
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("parent_parent_id", Constant.parent_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("employee_id",seleted_child_id);
        postParam.put("date", selected_date);
        postParam.put("date2",selected_date);

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.load_diary(body).enqueue(new Callback<Diary>() {
            @Override
            public void onResponse(Call<Diary> call, Response<Diary> response) {
                progress_bar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Diary diary = response.body();
                                         if (diary != null && diary.getStatus() != null) {
                         if (diary.getStatus().status.getCode().equals("1000")) {
                             // Data is available and has status code 1000
                             DiaryEntry subjectData = diary.getTitle().get(Integer.parseInt("0")); // Get the subject data from "0" key
                             if (subjectData != null) {
                                 // Convert the DiaryEntry to a HashMap
                                 HashMap<String, DiaryEntry> subjectDataMap = new HashMap<>();
                                 subjectDataMap.put("0", subjectData);
 
                                 // Convert the subjectData HashMap to a List of DiaryEntry
                                 subject_entry_list = new ArrayList<>(subjectDataMap.values());
                                 subjects_rcv.setAdapter(new SubjectListAdapter(subject_entry_list, LoadChildrenDiary.this));
                             } else {
                                 // No subject data found for the selected child
                                 Toast.makeText(context, "No subject data found for the selected child.", Toast.LENGTH_SHORT).show();
                             }
                         } else {
                             // Data is not available or has a status code other than 1000
                             Toast.makeText(context, diary.getStatus().status.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                    } else {
                        Toast.makeText(context, "Response body is empty or invalid.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Response is not successful, show "no_records_text"
                    Toast.makeText(context, "Response was not successful.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Diary> call, Throwable e) {
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load_diary_main();
        load_subjects_diary();
    }
    
    /**
     * Apply theme based on user type
     */
    private void applyTheme() {
        try {
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("LoadChildrenDiary", "User Type: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.student_primary));
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.student_primary));
                }
                
                Log.d("LoadChildrenDiary", "Student theme applied");
            } else {
                // Apply unified parent theme for children diary page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for diary
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for diary
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Children Diary");
                
                Log.d("LoadChildrenDiary", "Parent theme applied");
            }
            
            // Apply footer theme
            ThemeHelper.applyFooterTheme(this, userType);
            
        } catch (Exception e) {
            Log.e("LoadChildrenDiary", "Error applying theme", e);
        }
    }
}
