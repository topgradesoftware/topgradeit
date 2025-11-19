package topgrade.parent.com.parentseeks.Teacher.Activites.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;



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
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener2;
import topgrade.parent.com.parentseeks.Parent.Model.ExamSession;
import topgrade.parent.com.parentseeks.Parent.Model.SessionModel;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Activites.Adaptor.ResultReportAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Activites.Adaptor.SubjectReportAdaptor1;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ProgressReportModel;
import topgrade.parent.com.parentseeks.Teacher.Activites.Model.ProgressReport;
import topgrade.parent.com.parentseeks.Teacher.Activites.Utils.Constant;

public class StaffProgress extends AppCompatActivity implements View.OnClickListener, OnClickListener2 {
    String session_id;
    List<String> exam_session_name_list = new ArrayList<>();
    List<ExamSession> examSessionslist = new ArrayList<>();
    ArrayAdapter<String> session_adaptor;
    private ProgressBar progress_bar;
    Context context;
    private RecyclerView result_rcv, subject_rcv;
    TextView show_advanced_filter;
    Spinner select_examp_session;
    Button search_filter;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_progress);
        
        // Configure status bar for navy blue background with white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
            
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
        
        // Setup window insets to respect system bars
        setupWindowInsets();

        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        context = StaffProgress.this;
        Paper.init(context);
        progress_bar = findViewById(R.id.progress_bar);
        result_rcv = findViewById(R.id.result_rcv);
        subject_rcv = findViewById(R.id.subject_rcv);
        show_advanced_filter = findViewById(R.id.show_advanced_filter);

        // Initialize RecyclerViews with empty adapters to prevent "No adapter attached" error
        result_rcv.setAdapter(new ResultReportAdaptor(new ArrayList<>()));
        subject_rcv.setAdapter(new SubjectReportAdaptor1(new ArrayList<>()));

        show_advanced_filter.setOnClickListener(this);

        show_advanced_filter.performClick();
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
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                            }
                        }

                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        android.util.Log.e("StaffProgress", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                android.util.Log.e("StaffProgress", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            android.util.Log.e("StaffProgress", "Error setting up window insets: " + e.getMessage(), e);
        }
    }

    private void Load_Progress_Report() {
        final HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("exam_session_id", session_id);
        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.report_progress(body).enqueue(new Callback<ProgressReportModel>() {
            @Override
            public void onResponse(Call<ProgressReportModel> call, Response<ProgressReportModel> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        final List<ProgressReport> report_list = response.body().getReport();

                        ResultReportAdaptor marks_adaptor = new ResultReportAdaptor(
                                report_list);
                        result_rcv.setAdapter(marks_adaptor);

                        List<String> subject_name = new ArrayList<>();
                        for (int i = 0; i < report_list.size(); i++) {
                            subject_name.add(report_list.get(i).getClassName() + " (" + report_list.get(i).getSubjectName() + " )");

                        }

                        subject_rcv.setAdapter(new SubjectReportAdaptor1(subject_name));

                        progress_bar.setVisibility(View.GONE);
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
            public void onFailure(Call<ProgressReportModel> call, Throwable e) {
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.show_advanced_filter) {
            show_exam_session_dialog();
        }

    }

    private void show_exam_session_dialog() {
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.exam_session, null);
        search_filter = dialogView.findViewById(R.id.search_filter);
        select_examp_session = dialogView.findViewById(R.id.select_examp_session);
        ImageView Cancel = dialogView.findViewById(R.id.Cancel);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        // Note: Using regular Spinner instead of SearchableSpinner
        // setTitle and setPositiveButton methods are not available on regular Spinner
        search_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Load_Progress_Report();
            }
        });
        
        // Load exam sessions using the same pattern as ExamSubmit.java
        loadExamSession();
    }
    
    private void loadExamSessionsFromAPI(View dialogView) {
        // Load constants from Paper database
        Constant.loadFromPaper();
        
        // Also load directly from Paper to ensure we have the values
        String staff_id = Paper.book().read("staff_id", "");
        String campus_id = Paper.book().read("campus_id", "");
        
        // Validate that we have the required parameters
        if (staff_id.isEmpty() || campus_id.isEmpty()) {
            android.util.Log.e("StaffProgress", "Missing required parameters:");
            android.util.Log.e("StaffProgress", "staff_id: " + staff_id);
            android.util.Log.e("StaffProgress", "campus_id: " + campus_id);
            Toast.makeText(context, "Missing login information. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Debug logging
        android.util.Log.d("StaffProgress", "API parameters (teacher API with correct naming):");
        android.util.Log.d("StaffProgress", "staff_id (staff member ID): " + staff_id);
        android.util.Log.d("StaffProgress", "parent_id (campus ID): " + campus_id);
        android.util.Log.d("StaffProgress", "exam_session_id: '' (empty to get all sessions)");
        android.util.Log.d("StaffProgress", "Note: parent_id in API = campus_id in app (confusing naming)");
        
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("staff_id", staff_id);
        postParam.put("parent_id", campus_id);
        postParam.put("exam_session_id", "");    // Empty string to get all exam sessions for this staff/campus
        
        android.util.Log.d("StaffProgress", "Final API request: " + (new JSONObject(postParam)).toString());
        
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());
        
        Constant.mApiService.load_exam_session_teacher(body).enqueue(new Callback<SessionModel>() {
            @Override
            public void onResponse(Call<SessionModel> call, Response<SessionModel> response) {
                // Check if activity is still valid before proceeding
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                android.util.Log.d("StaffProgress", "API Response received:");
                android.util.Log.d("StaffProgress", "Response code: " + response.code());
                android.util.Log.d("StaffProgress", "Response successful: " + response.isSuccessful());
                
                if (response.body() != null) {
                    android.util.Log.d("StaffProgress", "Response body status code: " + response.body().getStatus().getCode());
                    android.util.Log.d("StaffProgress", "Response body status message: " + response.body().getStatus().getMessage());
                    
                    if (response.body().getStatus().getCode().equals("1000")) {
                        examSessionslist = response.body().getExamSession();
                        android.util.Log.d("StaffProgress", "Exam sessions count: " + (examSessionslist != null ? examSessionslist.size() : "null"));
                        
                        // Save to local storage for future use
                        if (examSessionslist != null) {
                            Paper.book().write(Constants.exam_session, examSessionslist);
                        }
                        
                        if (dialogView != null) {
                            setupExamSessionSpinner(dialogView);
                        } else {
                            // Called from loadExamSession, setup spinner directly
                            setupExamSessionSpinnerDirectly();
                        }
                    } else {
                        android.util.Log.w("StaffProgress", "API returned error: " + response.body().getStatus().getMessage());
                        // If API fails, try to load from local storage
                        loadExamSessionsFromLocal(dialogView);
                    }
                } else {
                    android.util.Log.w("StaffProgress", "Response body is null");
                    // If API fails, try to load from local storage
                    loadExamSessionsFromLocal(dialogView);
                }
            }
            
            @Override
            public void onFailure(Call<SessionModel> call, Throwable t) {
                // Check if activity is still valid before proceeding
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                android.util.Log.e("StaffProgress", "API call failed: " + t.getMessage());
                t.printStackTrace();
                
                // If API fails, try to load from local storage
                loadExamSessionsFromLocal(dialogView);
            }
        });
    }
    
    private void loadExamSessionsFromLocal(View dialogView) {
        examSessionslist = Paper.book().read(Constants.exam_session);
        setupExamSessionSpinner(dialogView);
    }
    
    private void loadExamSession() {
        // Load exam sessions from local storage first (same pattern as ExamSubmit.java)
        examSessionslist = Paper.book().read(Constants.exam_session);
        
        if (examSessionslist == null || examSessionslist.isEmpty()) {
            android.util.Log.d("StaffProgress", "No local exam sessions found, loading from API");
            // If no local data, load from API
            loadExamSessionsFromAPI(null);
        } else {
            android.util.Log.d("StaffProgress", "Using local exam sessions: " + examSessionslist.size());
            // Use local data and setup spinner
            exam_session_name_list = new ArrayList<>();
            for (int i = 0; i < examSessionslist.size(); i++) {
                exam_session_name_list.add(examSessionslist.get(i).getFullName());
            }

            session_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                    exam_session_name_list);

            select_examp_session.setAdapter(session_adaptor);

            select_examp_session.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    session_id = examSessionslist.get(position).getUniqueId();
                    android.util.Log.d("StaffProgress", "Selected exam session: " + examSessionslist.get(position).getFullName() + " (ID: " + session_id + ")");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }
    
    private void setupExamSessionSpinner(View dialogView) {
        // Check if activity is still valid before showing dialog
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        if (examSessionslist != null && examSessionslist.size() > 0) {

            exam_session_name_list = new ArrayList<>();
            for (int i = 0; i < examSessionslist.size(); i++) {
                exam_session_name_list.add(examSessionslist.get(i).getFullName());
            }

            session_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                    exam_session_name_list);

            select_examp_session.setAdapter(session_adaptor);

            select_examp_session.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    session_id = examSessionslist.get(position).getUniqueId();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        } else {
            // Handle case when no exam sessions are available
            exam_session_name_list = new ArrayList<>();
            exam_session_name_list.add("No exam sessions available");
            
            session_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                    exam_session_name_list);
            
            select_examp_session.setAdapter(session_adaptor);
            select_examp_session.setEnabled(false);
            
            Toast.makeText(context, "No exam sessions are currently available", Toast.LENGTH_SHORT).show();
        }
        
        // Create and show the dialog only if activity is still valid
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setView(dialogView);

            alertDialog = dialogBuilder.create();
            alertDialog.show();
        } catch (Exception e) {
            // Handle any dialog creation errors gracefully
            e.printStackTrace();
        }
    }
    
    private void setupExamSessionSpinnerDirectly() {
        // Check if activity is still valid
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        if (examSessionslist != null && examSessionslist.size() > 0) {
            android.util.Log.d("StaffProgress", "Setting up exam session spinner with " + examSessionslist.size() + " sessions");
            
            exam_session_name_list = new ArrayList<>();
            for (int i = 0; i < examSessionslist.size(); i++) {
                exam_session_name_list.add(examSessionslist.get(i).getFullName());
            }

            session_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                    exam_session_name_list);

            select_examp_session.setAdapter(session_adaptor);

            select_examp_session.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    session_id = examSessionslist.get(position).getUniqueId();
                    android.util.Log.d("StaffProgress", "Selected exam session: " + examSessionslist.get(position).getFullName() + " (ID: " + session_id + ")");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else {
            android.util.Log.w("StaffProgress", "No exam sessions available for spinner setup");
            Toast.makeText(context, "No exam sessions are currently available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(View view, int position, int position2) {

    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss dialog if it's showing to prevent memory leaks
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}
