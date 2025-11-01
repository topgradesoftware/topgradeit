package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import components.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.StudentListAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListSigel;
import topgrade.parent.com.parentseeks.Teacher.Model.Teach;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffStudentList extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StaffStudnetList";

    private RecyclerView myrecycleview;
    List<SharedStudent> students;
    Context context;
    TextView total_records;
    SwipeRefreshLayout swipe_refresh;
    List<String> class_list = new ArrayList<>();
    private ProgressBar progress_bar;
    SearchableSpinner class_spinner;
    String class_id, subject_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_studnet_list);
        
        // Configure status bar for navy blue background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
            
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

        // Additional fix for older Android versions - Force white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Ensure status bar icons are light (white) on dark background
            getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility() & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();

        context = StaffStudentList.this;
        Paper.init(context);

        // Load data from PaperDB if needed
        Constant.loadFromPaper();

        progress_bar = findViewById(R.id.progress_bar);
        class_spinner = findViewById(R.id.classs_spinner);
        myrecycleview = findViewById(R.id.student_rcv);
        total_records = findViewById(R.id.total_records);
        class_spinner.setTitle("Select Class/Subject");
        
        // Setup RecyclerView with LayoutManager
        myrecycleview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        myrecycleview.setHasFixedSize(true);
        
        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Load_Session();
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
                        Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e(TAG, "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
        }
    }

    private void Load_Session() {
        Log.d(TAG, "Load_Session() - Starting session load");
        Log.d(TAG, "Load_Session() - staff_id: " + Constant.staff_id);
        Log.d(TAG, "Load_Session() - campus_id: " + Constant.campus_id);
        Log.d(TAG, "Load_Session() - current_session: " + Constant.current_session);

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("session_id", Constant.current_session);

        JSONObject jsonObject = new JSONObject(postParam);
        Log.d(TAG, "Load_Session() - Request params: " + jsonObject.toString());
        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

        Constant.mApiService.load_profile(body).enqueue(new Callback<TeachModel>() {
            @Override
            public void onResponse(Call<TeachModel> call, Response<TeachModel> response) {
                Log.d(TAG, "Load_Session() - API response code: " + response.code());
                
                if (response.body() != null) {
                    Log.d(TAG, "Load_Session() - SharedStatus code: " + response.body().getStatus().getCode());
                    Log.d(TAG, "Load_Session() - SharedStatus message: " + response.body().getStatus().getMessage());
                    
                    if (response.body().getStatus().getCode().equals("1000")) {

                        final List<Teach> list = response.body().getTeach();

                        // Use a Set to prevent duplicates and create filtered list
                        Set<String> uniqueClassSubject = new HashSet<>();
                        List<Teach> filteredList = new ArrayList<>();
                        for (Teach teach : list) {
                            String classSubject = teach.getClassName() + " (" + teach.getSubjectName() + " )";
                            if (!uniqueClassSubject.contains(classSubject)) {
                                uniqueClassSubject.add(classSubject);
                                class_list.add(classSubject);
                                filteredList.add(teach);
                            }
                        }


                        ArrayAdapter student_class_adaptor = new ArrayAdapter<String>(context, R.layout.simple_list_item_1,
                                class_list);
                        class_spinner.setAdapter(student_class_adaptor);

                        // Auto-select first item if available
                        if (!filteredList.isEmpty()) {
                            class_spinner.setSelection(0);
                            class_id = filteredList.get(0).getStudentClassId();
                            subject_id = filteredList.get(0).getSubjectId();
                            Load_Student();
                        } else {
                            Toast.makeText(context, "No classes/subjects available", Toast.LENGTH_SHORT).show();
                        }

                        class_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position >= 0 && position < filteredList.size()) {
                                    class_id = filteredList.get(position).getStudentClassId();
                                    subject_id = filteredList.get(position).getSubjectId();
                                    Log.d(TAG, "Selected class_id: " + class_id + ", subject_id: " + subject_id);
                                    Load_Student();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                Log.d(TAG, "No class/subject selected");
                                myrecycleview.setVisibility(View.GONE);
                                total_records.setText("Total Records: 0");
                            }
                        });


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
            public void onFailure(Call<TeachModel> call, Throwable e) {
                Log.e(TAG, "Load_Session() - Network error", e);
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, "Network error loading session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    private void Load_Student() {
        try {
            HashMap<String, String> postParam = new HashMap<String, String>();
            postParam.put("staff_id", Constant.staff_id);
            postParam.put("parent_id", Constant.campus_id);
            postParam.put("student_class_id", class_id);
            postParam.put("subject_id", subject_id);

            Log.d(TAG, "Loading students with params: " + postParam.toString());

            progress_bar.setVisibility(View.VISIBLE);
            RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

            Constant.mApiService.load_students(body).enqueue(new Callback<StudentListModel>() {
                @Override
                public void onResponse(Call<StudentListModel> call, Response<StudentListModel> response) {
                    progress_bar.setVisibility(View.GONE);
                    
                    if (response.body() != null) {
                        if (response.body().getStatus().getCode().equals("1000")) {
                            List<StudentListSigel> list = response.body().getStudents();
                            
                            Log.d(TAG, "Students loaded successfully. Count: " + (list != null ? list.size() : 0));
                            
                            if (list != null && !list.isEmpty()) {
                                total_records.setText("Total Records: " + list.size());
                                myrecycleview.setVisibility(View.VISIBLE);
                                
                                // Log first few students for debugging
                                for (int i = 0; i < Math.min(3, list.size()); i++) {
                                    StudentListSigel student = list.get(i);
                                    Log.d(TAG, "SharedStudent " + (i+1) + ": " + 
                                          "Name=" + student.getFullName() + 
                                          ", Roll=" + student.getRollNumber() + 
                                          ", Parent=" + student.getParentName());
                                }
                                
                                StudentListAdaptor adapter = new StudentListAdaptor(list);
                                myrecycleview.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                
                                Log.d(TAG, "Adapter set with " + list.size() + " items");
                            } else {
                                Log.w(TAG, "SharedStudent list is null or empty");
                                total_records.setText("Total Records: 0");
                                myrecycleview.setVisibility(View.GONE);
                                Toast.makeText(context, "No students found for this class/subject", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "API Error: " + response.body().getStatus().getMessage());
                            myrecycleview.setVisibility(View.GONE);
                            total_records.setText("Total Records: 0");
                            Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Response body is null");
                        myrecycleview.setVisibility(View.GONE);
                        total_records.setText("Total Records: 0");
                        Toast.makeText(context, "No response from server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<StudentListModel> call, Throwable e) {
                    progress_bar.setVisibility(View.GONE);
                    Log.e(TAG, "Network error loading students", e);
                    myrecycleview.setVisibility(View.GONE);
                    total_records.setText("Total Records: 0");
                    Toast.makeText(context, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progress_bar.setVisibility(View.GONE);
            Log.e(TAG, "Exception in Load_Student", e);
            Toast.makeText(context, "Error loading students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
