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
                // Apply unified parent theme for feedback page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for feedback
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for feedback
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Feedback");
                
                Log.d("ParentFeedback", "Parent theme applied successfully");
            }
            
            // Apply footer theming based on user type
            ThemeHelper.applyFooterTheme(this, userType);
            
        } catch (Exception e) {
            Log.e("ParentFeedback", "Error applying theme", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_feedback);

        // Configure status bar for dark brown background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.dark_brown));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar and navigation bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        setupWindowInsets();

        // Apply student theme using ThemeHelper
        applyTheme();

        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        innitlization();
        // Student loading and spinner setup now handled in setupInlineFilters() -> loadStudentsAndSetupAdapter()
    }


    private void innitlization() {


        context = ParentFeedback.this;
        Paper.init(context);

        // Load all constants from Paper DB
        Constant.loadFromPaper();
        
        progress_bar = findViewById(R.id.progress_bar);
        feedback_rcv = findViewById(R.id.feedback_rcv);
        feedback_list = findViewById(R.id.feedback_list);
        select_child_spinner = findViewById(R.id.select_child_spinner);
        load_feedback_btn = findViewById(R.id.load_feedback_btn);

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
                    Log.d("ParentFeedback", "Pre-selected child from intent: " + studentList.get(index).getFullName());
                }
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
                if (response.body() != null) {
                    Log.d("ParentFeedback", "Response body not null");
                    Log.d("ParentFeedback", "SharedStatus code: " + response.body().getStatus().getCode());
                    Log.d("ParentFeedback", "SharedStatus message: " + response.body().getStatus().getMessage());
                    
                    if (response.body().getStatus().getCode().equals("1000")) {
                        final List<Feedback> list = response.body().getFeedback();
                        Log.d("ParentFeedback", "Feedback list size: " + list.size());

                        if (list.size() > 0) {
                            Log.d("ParentFeedback", "Setting adapter with feedback data");
                            feedback_rcv.setAdapter(new FeedbackAdaptor(list));
                            feedback_list.setVisibility(View.VISIBLE);
                            progress_bar.setVisibility(View.GONE);
                        } else {
                            Log.d("ParentFeedback", "No feedback data available");
                            Toast.makeText(context, "No Feedback Uploaded", Toast.LENGTH_SHORT).show();

                            feedback_rcv.setAdapter(new FeedbackAdaptor(list));
                            feedback_list.setVisibility(View.GONE);
                            progress_bar.setVisibility(View.GONE);
                        }

                    } else {
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    progress_bar.setVisibility(View.GONE);

                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<FeedbackModel> call, Throwable e) {
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
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

                        // Add bottom margin to footer container to push it above navigation bar
                        android.widget.LinearLayout footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            // Set bottom margin to navigation bar height to ensure footer is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            Log.d("ParentFeedback", "Setting footer bottom margin: " + bottomMargin + "dp");
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                                Log.d("ParentFeedback", "Footer margin applied successfully");
                            }
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("ParentFeedback", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("ParentFeedback", "Error setting up window insets: " + e.getMessage());
        }
    }
}
