package topgrade.parent.com.parentseeks.Parent.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import components.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Parent.Adaptor.DiaryListAdapter;
import topgrade.parent.com.parentseeks.Parent.Adaptor.SubjectListAdapter;
import topgrade.parent.com.parentseeks.Parent.Utils.ChildListCrashFix;
import android.util.Log;
import topgrade.parent.com.parentseeks.Parent.Model.Diary;
import topgrade.parent.com.parentseeks.Parent.Model.DiaryEntry;
import topgrade.parent.com.parentseeks.Parent.Model.StatusModel;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class LoadChildrenDiary extends AppCompatActivity {

    private Context context;

    private SearchableSpinner select_child_spinner;
    private TextInputEditText start_date;
    private TextInputEditText student_name_filter;

    private DiaryListAdapter adapter;
    private ArrayAdapter<String> child_adapter;
    private final List<String> all_student_names = new ArrayList<>();

    private final List<DiaryEntry> subject_list = new ArrayList<>();
    private final List<DiaryEntry> subject_entry_list = new ArrayList<>();

    private RecyclerView main_rcv;
    private RecyclerView subjects_rcv;
    private final List<String> student_name_list = new ArrayList<>();
    private List<SharedStudent> studentList = new ArrayList<>();
    private String seleted_child_id = "";

    private ProgressBar progress_bar;

    private String selected_date = "";
    private Calendar calendar;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_parent_children_diary);
        
        // Configure status bar for dark brown background with white icons
        // Set transparent status bar to allow header wave to cover it
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
        
        // Ensure white status bar icons on dark background
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
        flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        // Configure status bar and navigation bar icons for Android R (API 30) and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        // Setup window insets
        setupWindowInsets();
        
        // Initialize Paper database
        Paper.init(this);
        
        // Apply theme based on user type (only for student theme, parent theme already configured above)
        applyTheme();

        context = LoadChildrenDiary.this;
        
        // Initialize back button
        ImageView backIcon = findViewById(R.id.back_icon);
        if (backIcon != null) {
            backIcon.setOnClickListener(v -> finish());
        }
        
        // Initialize header title
        TextView headerTitle = findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(R.string.diary);
        }
        SubjectListAdapter subjectListAdapter = new SubjectListAdapter(subject_entry_list, context);
        adapter = new DiaryListAdapter(subject_list, context);
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
        start_date = findViewById(R.id.start_date);
        student_name_filter = findViewById(R.id.student_name_filter);
        
        calendar = Calendar.getInstance();
        // Set default date to today
        selected_date = dateFormat.format(calendar.getTime());
        start_date.setText(selected_date);
        
        // Setup date picker
        start_date.setOnClickListener(v -> showDatePicker());
        
        // Setup student name filter
        setupStudentNameFilter();
        
        innitlization();
        
        // Use the crash fix utility to safely load student list
        try {
            studentList = ChildListCrashFix.safeLoadStudentList(context);
            Log.d("LoadChildrenDiary", "SharedStudent list loaded safely: " + studentList.size() + " students");
        } catch (Exception e) {
            Log.e("LoadChildrenDiary", "Error loading student list safely", e);
            studentList = new ArrayList<>();
        }
        
        // Safely populate student name list
        student_name_list.clear();
        all_student_names.clear();
        if (ChildListCrashFix.isStudentListSafe(studentList)) {
            for (SharedStudent student : studentList) {
                if (student != null && student.getFullName() != null && !student.getFullName().isEmpty()) {
                    String fullName = student.getFullName();
                    student_name_list.add(fullName);
                    all_student_names.add(fullName);
                    Log.d("LoadChildrenDiary", "Added student: " + fullName);
                } else {
                    Log.w("LoadChildrenDiary", "Invalid student found, skipping");
                }
            }
        } else {
            Log.w("LoadChildrenDiary", "SharedStudent list is not safe, showing empty state");
            // Add fallback message
            student_name_list.add("No Students Available");
            all_student_names.add("No Students Available");
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

    private void setupStudentNameFilter() {
        student_name_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudentList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterStudentList(String filterText) {
        student_name_list.clear();
        
        if (filterText == null || filterText.trim().isEmpty()) {
            // Show all students if filter is empty
            student_name_list.addAll(all_student_names);
        } else {
            // Filter students by name (case-insensitive)
            String lowerFilter = filterText.toLowerCase().trim();
            for (String studentName : all_student_names) {
                if (studentName.toLowerCase().contains(lowerFilter)) {
                    student_name_list.add(studentName);
                }
            }
        }
        
        // Update the adapter
        if (child_adapter != null) {
            child_adapter.notifyDataSetChanged();
        }
    }

    private void Load_child() {
        try {
            child_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, student_name_list);
            select_child_spinner.setAdapter(child_adapter);

            select_child_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
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
                                seleted_child_id = selectedStudent.getUniqueId();
                                Log.d("LoadChildrenDiary", "Selected student: " + selectedStudent.getFullName() + " with ID: " + seleted_child_id);
                                reloadDiaryData(); // Call the method to reload diary data
                            } else {
                                Toast.makeText(context, "Invalid student selection.", Toast.LENGTH_SHORT).show();
                                Log.w("LoadChildrenDiary", "Student not found: " + selectedStudentName);
                            }
                        } else {
                            Toast.makeText(context, "Invalid student selection.", Toast.LENGTH_SHORT).show();
                            Log.w("LoadChildrenDiary", "Invalid position: " + position);
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


    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            context,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                selected_date = dateFormat.format(calendar.getTime());
                start_date.setText(selected_date);
                // Reload diary data when date changes
                if (!seleted_child_id.isEmpty()) {
                    load_diary_main();
                    load_subjects_diary();
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void load_diary_main() {
        if (seleted_child_id.isEmpty()) {
            Toast.makeText(context, "Please select a child first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selected_date.isEmpty()) {
            Toast.makeText(context, "Please select a date first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("parent_parent_id", Constant.parent_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("employee_id", seleted_child_id);
        postParam.put("date", selected_date);
        postParam.put("date2", selected_date);

        progress_bar.setVisibility(View.VISIBLE);
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, (new JSONObject(postParam)).toString());

        Constant.mApiService.load_diary(body).enqueue(new Callback<Diary>() {
            @Override
            public void onResponse(@NonNull Call<Diary> call, @NonNull Response<Diary> response) {
                progress_bar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Diary diary = response.body();
                    StatusModel statusModel = (diary != null) ? diary.getStatus() : null;
                    if (diary != null && statusModel != null && statusModel.status != null) {
                        String statusCode = statusModel.status.getCode();
                        if (statusCode != null && statusCode.equals("1000")) {
                            // Parse the data array - it contains the main diary entry
                            if (diary.getData() != null && !diary.getData().isEmpty()) {
                                try {
                                    // Convert the first data object to JSON
                                    Gson gson = new Gson();
                                    JsonElement jsonElement = gson.toJsonTree(diary.getData().get(0));
                                    JsonObject dataObject = jsonElement.getAsJsonObject();
                                    
                                    // Create DiaryEntry from main diary data
                                    DiaryEntry mainDiaryEntry = new DiaryEntry();
                                    if (dataObject.has("body")) {
                                        mainDiaryEntry.setBody(dataObject.get("body").getAsString());
                                    }
                                    if (dataObject.has("full_name")) {
                                        mainDiaryEntry.setFullName(dataObject.get("full_name").getAsString());
                                    }
                                    if (dataObject.has("picture")) {
                                        mainDiaryEntry.setPicture(dataObject.get("picture").getAsString());
                                    }
                                    if (dataObject.has("class_name")) {
                                        mainDiaryEntry.setClassName(dataObject.get("class_name").getAsString());
                                    }
                                    
                                    // Add main diary entry to list
                                    subject_list.clear();
                                    subject_list.add(mainDiaryEntry);
                                    main_rcv.setAdapter(new DiaryListAdapter(subject_list, LoadChildrenDiary.this));
                                } catch (Exception e) {
                                    Log.e("LoadChildrenDiary", "Error parsing main diary data", e);
                                    Toast.makeText(context, "Error parsing diary data.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "No diary data found for the selected date.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String message = statusModel.status.getMessage() != null 
                                    ? statusModel.status.getMessage() : "Unknown error";
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Response body is empty or invalid.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Response was not successful.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Diary> call, @NonNull Throwable e) {
                Log.e("LoadChildrenDiary", "Error loading main diary", e);
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void load_subjects_diary() {
        if (seleted_child_id.isEmpty()) {
            return;
        }
        
        if (selected_date.isEmpty()) {
            return;
        }
        
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("parent_parent_id", Constant.parent_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("employee_id", seleted_child_id);
        postParam.put("date", selected_date);
        postParam.put("date2", selected_date);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, (new JSONObject(postParam)).toString());

        Constant.mApiService.load_diary(body).enqueue(new Callback<Diary>() {
            @Override
            public void onResponse(@NonNull Call<Diary> call, @NonNull Response<Diary> response) {
                if (response.isSuccessful()) {
                    Diary diary = response.body();
                    StatusModel statusModel = (diary != null) ? diary.getStatus() : null;
                    if (diary != null && statusModel != null && statusModel.status != null) {
                        String statusCode = statusModel.status.getCode();
                        if (statusCode != null && statusCode.equals("1000")) {
                            // Parse subject entries from data array
                            if (diary.getData() != null && !diary.getData().isEmpty()) {
                                try {
                                    Gson gson = new Gson();
                                    JsonElement jsonElement = gson.toJsonTree(diary.getData().get(0));
                                    JsonObject dataObject = jsonElement.getAsJsonObject();
                                    
                                    // Extract numbered keys (0, 1, 2, etc.) which are subject entries
                                    subject_entry_list.clear();
                                    for (int i = 0; i < 20; i++) { // Check up to 20 subjects
                                        String key = String.valueOf(i);
                                        if (dataObject.has(key)) {
                                            JsonObject subjectObj = dataObject.getAsJsonObject(key);
                                            DiaryEntry subjectEntry = gson.fromJson(subjectObj, DiaryEntry.class);
                                            if (subjectEntry != null) {
                                                subject_entry_list.add(subjectEntry);
                                            }
                                        }
                                    }
                                    
                                    if (!subject_entry_list.isEmpty()) {
                                        subjects_rcv.setAdapter(new SubjectListAdapter(subject_entry_list, LoadChildrenDiary.this));
                                    } else {
                                        // No subjects found
                                        subjects_rcv.setAdapter(new SubjectListAdapter(new ArrayList<>(), LoadChildrenDiary.this));
                                    }
                                } catch (Exception e) {
                                    Log.e("LoadChildrenDiary", "Error parsing subject diary data", e);
                                    subjects_rcv.setAdapter(new SubjectListAdapter(new ArrayList<>(), LoadChildrenDiary.this));
                                }
                            } else {
                                subjects_rcv.setAdapter(new SubjectListAdapter(new ArrayList<>(), LoadChildrenDiary.this));
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Diary> call, @NonNull Throwable e) {
                Log.e("LoadChildrenDiary", "Error loading subject diary", e);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only load if we have both child and date selected
        if (!seleted_child_id.isEmpty() && !selected_date.isEmpty()) {
            load_diary_main();
            load_subjects_diary();
        }
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
                getWindow().setStatusBarColor(getResources().getColor(R.color.student_primary));
                getWindow().setNavigationBarColor(getResources().getColor(R.color.student_primary));
                
                Log.d("LoadChildrenDiary", "Student theme applied");
            } else {
                // For parent theme, don't use ParentThemeHelper as it overwrites navigation bar color
                // System bars are already configured in onCreate() to match child list
                // Header title is already set in onCreate()
                Log.d("LoadChildrenDiary", "Parent theme - system bars already configured in onCreate()");
            }
            
        } catch (Exception e) {
            Log.e("LoadChildrenDiary", "Error applying theme", e);
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

                        android.view.View footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                            }
                        }
                        
                        view.setPadding(0, 0, 0, 0);
                        return WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("LoadChildrenDiary", "Error in window insets listener: " + e.getMessage());
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e("LoadChildrenDiary", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e("LoadChildrenDiary", "Error setting up window insets: " + e.getMessage(), e);
        }
    }
}
