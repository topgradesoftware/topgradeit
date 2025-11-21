package topgrade.parent.com.parentseeks.Parent.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.RecyclerView;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Parent.Adaptor.FeedbackAdaptor;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.Feedback;
import topgrade.parent.com.parentseeks.Teacher.Model.FeedbackModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import components.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParentFeedback extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar progress_bar;
    Context context;
    RecyclerView feedback_rcv;
    TextView feedback_list;
    com.google.android.material.button.MaterialButton load_feedback_btn;

    String seleted_child_id = "";
    SearchableSpinner select_child_spinner;
    ArrayAdapter<String> child_adaptor;

    List<String> student_name_list = new ArrayList<>();
    List<SharedStudent> studentList = new ArrayList<>();

    String parent_id;
    String campus_id;

    private void applyTheme() {
        try {
            // Check user type and apply appropriate theme
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("ParentFeedback", "User Type: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
                int tealColor = ContextCompat.getColor(this, R.color.student_primary);
                getWindow().setStatusBarColor(tealColor);
                getWindow().setNavigationBarColor(tealColor);
                
                // Force dark navigation bar icons (prevent light appearance)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // 0 = do NOT use light icons
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }
                
                // Change wave background to teal for student theme
                ImageView headerWave = findViewById(R.id.header_wave);
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_teal);
                }
                
                // Change load feedback button color to student theme (teal)
                com.google.android.material.button.MaterialButton loadButton = findViewById(R.id.load_feedback_btn);
                if (loadButton != null) {
                    loadButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.student_primary));
                }
                
                Log.d("ParentFeedback", "Student theme applied successfully");
            } else {
                // For parent theme, don't use ParentThemeHelper as it overwrites navigation bar color
                // System bars are already configured in onCreate() to match child list
                // Header title is already set in onCreate()
                Log.d("ParentFeedback", "Parent theme - system bars already configured in onCreate()");
            }
            
        } catch (Exception e) {
            Log.e("ParentFeedback", "Error applying theme", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_feedback);
        
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
        
        // Setup window insets
        setupWindowInsets();
        
        // Initialize Paper database
        Paper.init(this);
        
        // Apply theme based on user type (only for student theme, parent theme already configured above)
        applyTheme();

        // Initialize back button
        ImageView backIcon = findViewById(R.id.back_icon);
        if (backIcon != null) {
            backIcon.setOnClickListener(v -> finish());
        }
        
        // Initialize header title
        TextView headerTitle = findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(getString(R.string.teacher_remarks));
        }

        innitlization();
        // Student loading and spinner setup now handled in setupInlineFilters() -> loadStudentsAndSetupAdapter()
    }


    private void innitlization() {
        context = ParentFeedback.this;

        // Load all constants from Paper DB
        Constant.loadFromPaper();
        
        progress_bar = findViewById(R.id.progress_bar);
        feedback_rcv = findViewById(R.id.feedback_rcv);
        feedback_list = findViewById(R.id.feedback_list);
        select_child_spinner = findViewById(R.id.select_child_spinner);
        load_feedback_btn = findViewById(R.id.load_feedback_btn);
        
        // Ensure RecyclerView is properly initialized and visible
        if (feedback_rcv != null) {
            feedback_rcv.setVisibility(View.VISIBLE);
            // Ensure layout manager is set
            if (feedback_rcv.getLayoutManager() == null) {
                feedback_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
            }
            // Set empty adapter initially
            feedback_rcv.setAdapter(new FeedbackAdaptor(new ArrayList<>()));
            Log.d("ParentFeedback", "RecyclerView initialized with layout manager");
        } else {
            Log.e("ParentFeedback", "RecyclerView is null!");
        }

        // Setup spinner with timetable-style configuration
        setupInlineFilters();
        
        load_feedback_btn.setOnClickListener(this);
        load_feedback_btn.setEnabled(false); // Initially disabled until child is selected

        parent_id = Paper.book().read("parent_id");
        campus_id = Paper.book().read("campus_id");
        
        // Debug logging for constants
        Log.d("ParentFeedback", "=== CONSTANTS DEBUG ===");
        Log.d("ParentFeedback", "Constant.current_session: " + Constant.current_session);
        Log.d("ParentFeedback", "Constant.campus_id: " + Constant.campus_id);
        Log.d("ParentFeedback", "Constant.parent_id: " + Constant.parent_id);
        Log.d("ParentFeedback", "Local campus_id: " + campus_id);
        Log.d("ParentFeedback", "Local parent_id: " + parent_id);
        Log.d("ParentFeedback", "========================");
    }

    /**
     * Setup inline filters with timetable-style configuration
     * Matches the excellent implementation from ModernStudentTimeTable
     */
    private void setupInlineFilters() {
        // Setup child spinner with title and auto-dismiss behavior
        select_child_spinner.setTitle("Select Child");
        // Removed setPositiveButton("OK") to enable auto-dismiss behavior like staff timetable
        
        // Load students and setup adapter
        loadStudentsAndSetupAdapter();
    }

    /**
     * Load students and setup adapter with timetable-style implementation
     */
    private void loadStudentsAndSetupAdapter() {
        try {
            studentList = Paper.book().read("students");
        } catch (Exception e) {
            // If there's a serialization error, clear the corrupted data and start fresh
            Paper.book().delete("students");
            studentList = new ArrayList<>();
        }

        if (studentList != null && !studentList.isEmpty()) {
            // Create student names list with class information (matching timetable format)
            student_name_list.clear();
            for (SharedStudent student : studentList) {
                String displayName = student.getClassName() + " - " + student.getFullName();
                student_name_list.add(displayName);
            }

            // Setup adapter with proper styling
            child_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, student_name_list);
            select_child_spinner.setAdapter(child_adaptor);

            // Setup selection listener with timetable-style behavior
            select_child_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < studentList.size()) {
                        SharedStudent selectedStudent = studentList.get(position);
                        seleted_child_id = selectedStudent.getUniqueId();
                        
                        // Enable the load feedback button when a child is selected
                        load_feedback_btn.setEnabled(true);
                        
                        // Debug logging
                        Log.d("ParentFeedback", "onItemSelected() - Selected student: " + selectedStudent.getFullName());
                        Log.d("ParentFeedback", "onItemSelected() - Student ID: " + seleted_child_id);
                        
                        // Auto-load feedback when child is selected (like other screens)
                        if (seleted_child_id != null && !seleted_child_id.isEmpty()) {
                            Log.d("ParentFeedback", "Auto-loading feedback for selected child");
                            Load_Feedback();
                        }
                        
                        // Popup will dismiss automatically when item is selected (timetable behavior)
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    seleted_child_id = "";
                    load_feedback_btn.setEnabled(false);
                    Log.d("ParentFeedback", "onNothingSelected() - No student selected");
                }
            });

            // Handle intent extras for pre-selection (matching timetable behavior)
            if (getIntent().hasExtra("child_id")) {
                String child_id = getIntent().getStringExtra("child_id");
                int index = -1;
                for (int i = 0; i < studentList.size(); i++) {
                    if (studentList.get(i).getUniqueId().equals(child_id)) {
                        index = i;
                        break;
                    }
                }
                if (index > -1) {
                    select_child_spinner.setSelection(index);
                    seleted_child_id = studentList.get(index).getUniqueId();
                    load_feedback_btn.setEnabled(true);
                    Log.d("ParentFeedback", "Pre-selected child from intent: " + studentList.get(index).getFullName());
                    // Auto-load feedback for pre-selected child
                    if (seleted_child_id != null && !seleted_child_id.isEmpty()) {
                        // Post to ensure UI is ready
                        select_child_spinner.post(() -> Load_Feedback());
                    }
                }
            } else if (studentList.size() == 1) {
                // Auto-select and load if only one child
                seleted_child_id = studentList.get(0).getUniqueId();
                select_child_spinner.setSelection(0);
                load_feedback_btn.setEnabled(true);
                Log.d("ParentFeedback", "Auto-selected single child: " + studentList.get(0).getFullName());
                // Auto-load feedback for single child
                if (seleted_child_id != null && !seleted_child_id.isEmpty()) {
                    // Post to ensure UI is ready
                    select_child_spinner.post(() -> Load_Feedback());
                }
            } else if (studentList.size() > 1) {
                // If multiple children, check if spinner already has a selection
                select_child_spinner.post(() -> {
                    int selectedPosition = select_child_spinner.getSelectedItemPosition();
                    if (selectedPosition >= 0 && selectedPosition < studentList.size()) {
                        SharedStudent selectedStudent = studentList.get(selectedPosition);
                        seleted_child_id = selectedStudent.getUniqueId();
                        load_feedback_btn.setEnabled(true);
                        Log.d("ParentFeedback", "Found existing selection: " + selectedStudent.getFullName());
                        // Auto-load feedback for existing selection
                        if (seleted_child_id != null && !seleted_child_id.isEmpty()) {
                            Load_Feedback();
                        }
                    }
                });
            }

            Log.d("ParentFeedback", "setupInlineFilters() - Child spinner enabled with " + studentList.size() + " students");
        } else {
            // Handle empty student list
            select_child_spinner.setEnabled(false);
            Toast.makeText(context, "No students found. Please contact your school administrator.", Toast.LENGTH_LONG).show();
            Log.w("ParentFeedback", "setupInlineFilters() - No students available, disabling child spinner");
        }
    }

    // Old Load_child() method removed - replaced with loadStudentsAndSetupAdapter() for timetable-style implementation

    private void Load_Feedback() {

        // Debug logging
        Log.d("ParentFeedback", "=== LOAD FEEDBACK DEBUG ===");
        Log.d("ParentFeedback", "Constant.campus_id: " + Constant.campus_id);
        Log.d("ParentFeedback", "child_id: " + seleted_child_id);
        Log.d("ParentFeedback", "Constant.current_session: " + Constant.current_session);
        Log.d("ParentFeedback", "==========================");

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("campus_id", Constant.campus_id); // Use Constant.campus_id instead of local variable
        postParam.put("child_id", seleted_child_id);
        postParam.put("operation", "read_feedback");
        postParam.put("session_id", Constant.current_session);

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.feedback(body).enqueue(new Callback<FeedbackModel>() {
            @Override
            public void onResponse(Call<FeedbackModel> call, Response<FeedbackModel> response) {
                Log.d("ParentFeedback", "API Response received");
                progress_bar.setVisibility(View.GONE);
                
                if (response.body() != null) {
                    Log.d("ParentFeedback", "Response body not null");
                    Log.d("ParentFeedback", "SharedStatus code: " + response.body().getStatus().getCode());
                    Log.d("ParentFeedback", "SharedStatus message: " + response.body().getStatus().getMessage());
                    
                    if (response.body().getStatus().getCode().equals("1000")) {
                        List<Feedback> list = response.body().getFeedback();
                        
                        // Handle null list
                        if (list == null) {
                            Log.e("ParentFeedback", "Feedback list is null");
                            list = new ArrayList<>();
                        }
                        
                        Log.d("ParentFeedback", "Feedback list size: " + list.size());
                        Log.d("ParentFeedback", "Feedback list content: " + list);
                        
                        // Log each feedback item
                        for (int i = 0; i < list.size(); i++) {
                            Feedback fb = list.get(i);
                            Log.d("ParentFeedback", "Feedback[" + i + "]: Teacher=" + 
                                (fb.getTeacherName() != null ? fb.getTeacherName() : "null") +
                                ", Subject=" + (fb.getSubject_name() != null ? fb.getSubject_name() : "null") +
                                ", Text=" + (fb.getFeedback() != null ? fb.getFeedback() : "null"));
                        }

                        if (list.size() > 0) {
                            Log.d("ParentFeedback", "Setting adapter with " + list.size() + " items");
                            
                            // Ensure RecyclerView is properly configured
                            if (feedback_rcv.getLayoutManager() == null) {
                                Log.d("ParentFeedback", "Setting LinearLayoutManager for RecyclerView");
                                feedback_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
                            }
                            
                            // Ensure RecyclerView is visible
                            feedback_rcv.setVisibility(View.VISIBLE);
                            
                            // Create and set adapter
                            FeedbackAdaptor adapter = new FeedbackAdaptor(list);
                            feedback_rcv.setAdapter(adapter);
                            
                            // Force layout update (matching working code pattern)
                            feedback_rcv.requestLayout();
                            feedback_rcv.invalidate();
                            
                            feedback_list.setVisibility(View.VISIBLE);
                            
                            Log.d("ParentFeedback", "Feedback adapter set successfully");
                            Log.d("ParentFeedback", "RecyclerView child count: " + feedback_rcv.getChildCount());
                            Log.d("ParentFeedback", "Adapter item count: " + adapter.getItemCount());
                            Log.d("ParentFeedback", "RecyclerView visibility: " + (feedback_rcv.getVisibility() == View.VISIBLE ? "VISIBLE" : "NOT VISIBLE"));
                            Log.d("ParentFeedback", "RecyclerView width: " + feedback_rcv.getWidth() + ", height: " + feedback_rcv.getHeight());
                            
                            // Post a delayed check to see if items are displayed
                            feedback_rcv.postDelayed(() -> {
                                Log.d("ParentFeedback", "=== DELAYED CHECK ===");
                                Log.d("ParentFeedback", "RecyclerView child count after delay: " + feedback_rcv.getChildCount());
                                Log.d("ParentFeedback", "Adapter item count after delay: " + adapter.getItemCount());
                                Log.d("ParentFeedback", "RecyclerView is attached to window: " + feedback_rcv.isAttachedToWindow());
                                Log.d("ParentFeedback", "=====================");
                            }, 1000);
                        } else {
                            Log.d("ParentFeedback", "No feedback data available");
                            Toast.makeText(context, "No Feedback Uploaded", Toast.LENGTH_SHORT).show();

                            // Set empty adapter to clear previous data
                            feedback_rcv.setAdapter(new FeedbackAdaptor(new ArrayList<>()));
                            feedback_list.setVisibility(View.GONE);
                        }

                    } else {
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        // Clear RecyclerView on error
                        feedback_rcv.setAdapter(new FeedbackAdaptor(new ArrayList<>()));
                        feedback_list.setVisibility(View.GONE);
                    }
                } else {
                    Log.e("ParentFeedback", "Response body is null");
                    Toast.makeText(context, "Failed to load feedback. Please try again.", Toast.LENGTH_SHORT).show();
                    // Clear RecyclerView on error
                    feedback_rcv.setAdapter(new FeedbackAdaptor(new ArrayList<>()));
                    feedback_list.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<FeedbackModel> call, Throwable e) {
                e.printStackTrace();
                Log.e("ParentFeedback", "API call failed", e);
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, "Failed to load feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Clear RecyclerView on failure
                feedback_rcv.setAdapter(new FeedbackAdaptor(new ArrayList<>()));
                feedback_list.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.load_feedback_btn) {
            if (seleted_child_id.isEmpty()) {
                Toast.makeText(context, "Please select a child first", Toast.LENGTH_SHORT).show();
            } else {
                Load_Feedback();
            }
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
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars()
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
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("ParentFeedback", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e("ParentFeedback", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e("ParentFeedback", "Error setting up window insets: " + e.getMessage(), e);
        }
    }
}
