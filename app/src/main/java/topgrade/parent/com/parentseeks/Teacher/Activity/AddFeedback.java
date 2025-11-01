package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.snackbar.Snackbar;
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
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.FeedbackModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListSigel;
import topgrade.parent.com.parentseeks.Teacher.Model.Teach;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.Teacher.Utils.Util;

public class AddFeedback extends AppCompatActivity implements View.OnClickListener {

    List<String> class_list = new ArrayList<>();
    List<String> student_list = new ArrayList<>();
    Context context;
    private ProgressBar progress_bar;
    private TextView total_records;
    SearchableSpinner class_spinner, student_spinner;
    String class_id, subject_id;
    String child_id = "";
    String child_name = "";
    String parent_phone_number = "";

    private EditText write_feedback;
    Button submit_feedback;

    String write_feedbackk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_feedback);
        
        // Configure status bar for navy blue background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.navy_blue));
            
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

        initiation();

        // Check if constants are properly initialized
        if (Constant.staff_id == null || Constant.staff_id.isEmpty()) {
            Log.e("AddFeedback", "staff_id is null or empty!");
            Toast.makeText(this, "Staff ID not found. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (Constant.campus_id == null || Constant.campus_id.isEmpty()) {
            Log.e("AddFeedback", "campus_id is null or empty!");
            Toast.makeText(this, "Campus ID not found. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (Constant.current_session == null || Constant.current_session.isEmpty()) {
            Log.e("AddFeedback", "current_session is null or empty!");
            Toast.makeText(this, "Session not found. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        Load_Class_Subject();
    }

    private void initiation() {
        context = AddFeedback.this;
        Paper.init(context);

        progress_bar = findViewById(R.id.progress_bar);
        total_records = findViewById(R.id.total_records);
        class_spinner = findViewById(R.id.classs_spinner);
        student_spinner = findViewById(R.id.student_spinner);
        class_spinner.setTitle("Select Class/Subject");
        student_spinner.setTitle("Select SharedStudent");
        submit_feedback = findViewById(R.id.submit_feedback);
        write_feedback = findViewById(R.id.write_feedback);
        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submit_feedback.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.submit_feedback) {
            write_feedbackk = write_feedback.getText().toString();

            if (child_id.isEmpty()) {
                Snackbar.make(v, "Please select a student", Snackbar.LENGTH_LONG)
                        .show();
            } else if (write_feedbackk.isEmpty()) {
                Snackbar.make(v, "Please enter feedback or message", Snackbar.LENGTH_LONG)
                        .show();
            } else {
                // Show menu with all submission options
                showSubmissionMenu(v);
            }
        }

    }

    // Method to handle onClick from layout
    public void feedback(View view) {
        onClick(view);
    }

    /**
     * Show submission menu with all options
     */
    private void showSubmissionMenu(View view) {
        PopupMenu popup = new PopupMenu(AddFeedback.this, view);
        popup.getMenu().add(0, 1, 1, "Submit to Parent App");
        popup.getMenu().add(0, 2, 2, "Send via WhatsApp");
        popup.getMenu().add(0, 3, 3, "Send via WhatsApp Business");
        popup.getMenu().add(0, 4, 4, "Send via Local SMS");
        popup.getMenu().add(0, 5, 5, "Share via Other Apps");
        
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                String title = item.getTitle().toString();
                
                if (itemId == 1) {
                    // Submit to Parent App
                    Submit_feedback();
                } else {
                    // For sharing options - use same logic as timetable
                    String staff_name = Paper.book().read("full_name", "");
                    String campusName = Paper.book().read("campus_name", "School");
                    String feedback = "Dear Parent,\n\n" +
                            "Feedback: \"" + write_feedbackk + "\"\n\n" +
                            "Student Name: " + child_name + "\n" +
                            "Feedback by: " + staff_name + "\n" +
                            campusName;
                    
                    // Copy timetable logic - read phone from PaperDB each time
                    switch (title) {
                        case "Send via WhatsApp":
                            String phone = Paper.book().read("phone", "");
                            Util.shareToWhatsAppWithNumber(context, feedback, phone, "com.whatsapp");
                            break;
                        case "Send via WhatsApp Business":
                            String phone_business = Paper.book().read("phone", "");
                            Util.shareToWhatsAppWithNumber(context, feedback, phone_business, "com.whatsapp.w4b");
                            break;
                        case "Send via Local SMS":
                            String phone_sms = Paper.book().read("phone", "");
                            Util.showSmsIntent(context, feedback, phone_sms);
                            break;
                        case "Share via Other Apps":
                            String phone_other = Paper.book().read("phone", "");
                            Util.shareWithPhoneNumber(context, feedback, phone_other);
                            break;
                    }
                }
                
                return true;
            }
        });

        popup.show();
    }

    private void Share_Menu(View view, final String feedback) {
        PopupMenu popup = new PopupMenu(AddFeedback.this, view);
        popup.getMenuInflater()
                .inflate(R.menu.share_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                String title = item.getTitle().toString();
                switch (title) {
                    case "Whatsapp":
                        Util.shareToWhatsAppWithNumber(context, feedback, parent_phone_number, "com.whatsapp");
                        break;
                    case "Whatsapp(Business)":
                        Util.shareToWhatsAppWithNumber(context, feedback, parent_phone_number, "com.whatsapp.w4b");
                        break;
                    case "Local SMS":
                        Util.showSmsIntent(context, feedback, parent_phone_number);
                        break;
                    case "Other":
                        Util.share(context, feedback);
                        break;
                }
                return true;
            }
        });

        popup.show();

    }


    private void Load_Class_Subject() {
        Log.d("AddFeedback", "Starting Load_Class_Subject...");
        Log.d("AddFeedback", "staff_id: " + Constant.staff_id);
        Log.d("AddFeedback", "campus_id: " + Constant.campus_id);
        Log.d("AddFeedback", "session_id: " + Constant.current_session);

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("session_id", Constant.current_session);

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

        Log.d("AddFeedback", "API call parameters: " + postParam.toString());

        Constant.mApiService.load_profile(body).enqueue(new Callback<TeachModel>() {
            @Override
            public void onResponse(Call<TeachModel> call, Response<TeachModel> response) {
                Log.d("AddFeedback", "API Response received: " + response.code());
                
                if (response.body() != null) {
                    Log.d("AddFeedback", "Response body status: " + response.body().getStatus().getCode());
                    Log.d("AddFeedback", "Response body message: " + response.body().getStatus().getMessage());
                    
                    if (response.body().getStatus().getCode().equals("1000")) {
                        final List<Teach> list = response.body().getTeach();
                        Log.d("AddFeedback", "Teach list size: " + (list != null ? list.size() : "null"));

                        if (list != null && !list.isEmpty()) {
                            // Use a Set to prevent duplicates and create filtered list
                            Set<String> uniqueClassSubject = new HashSet<>();
                            List<Teach> filteredList = new ArrayList<>();
                            for (Teach teach : list) {
                                String classSubject = teach.getClassName() + " (" + teach.getSubjectName() + " )";
                                Log.d("AddFeedback", "Processing: " + classSubject);
                                if (!uniqueClassSubject.contains(classSubject)) {
                                    uniqueClassSubject.add(classSubject);
                                    class_list.add(classSubject);
                                    filteredList.add(teach);
                                }
                            }

                            Log.d("AddFeedback", "Final class_list size: " + class_list.size());

                            ArrayAdapter student_class_adaptor = new ArrayAdapter<String>(context, R.layout.simple_list_item_1,
                                    class_list);
                            class_spinner.setAdapter(student_class_adaptor);

                            class_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                    class_id = filteredList.get(position).getStudentClassId();
                                    subject_id = filteredList.get(position).getSubjectId();
                                    Load_Student();

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });


                            progress_bar.setVisibility(View.GONE);
                        } else {
                            Log.d("AddFeedback", "No classes or subjects found in response.");
                            progress_bar.setVisibility(View.GONE);
                            Toast.makeText(context, "No classes or subjects found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("AddFeedback", "API call failed with status: " + response.body().getStatus().getCode());
                    }
                } else {
                    progress_bar.setVisibility(View.GONE);
                    Log.d("AddFeedback", "API call failed with raw message: " + response.raw().message());
                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TeachModel> call, Throwable e) {
                Log.e("AddFeedback", "API call failed with error: " + e.getMessage());
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Show fallback data for testing
                showFallbackData();
            }
        });
    }
    
    private void showFallbackData() {
        Log.d("AddFeedback", "Showing fallback data...");
        
        // Add some sample classes/subjects for testing
        class_list.clear();
        class_list.add("9th (Biology)");
        class_list.add("10th (Chemistry)");
        class_list.add("11th (Physics)");
        
        ArrayAdapter student_class_adaptor = new ArrayAdapter<String>(context, R.layout.simple_list_item_1, class_list);
        class_spinner.setAdapter(student_class_adaptor);
        
        Toast.makeText(context, "Showing sample data due to network error", Toast.LENGTH_LONG).show();
    }

    private void Load_Student() {
        Log.d("AddFeedback", "Starting Load_Student...");
        Log.d("AddFeedback", "class_id: " + class_id);
        Log.d("AddFeedback", "subject_id: " + subject_id);

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("student_class_id", class_id);
        postParam.put("subject_id", subject_id);

        Log.d("AddFeedback", "Load_Student API parameters: " + postParam.toString());

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

        Constant.mApiService.load_students(body).enqueue(new Callback<StudentListModel>() {
            @Override
            public void onResponse(Call<StudentListModel> call, Response<StudentListModel> response) {
                Log.d("AddFeedback", "Load_Student API Response received: " + response.code());

                if (response.body() != null) {
                    Log.d("AddFeedback", "Load_Student Response body status: " + response.body().getStatus().getCode());
                    Log.d("AddFeedback", "Load_Student Response body message: " + response.body().getStatus().getMessage());
                    
                    if (response.body().getStatus().getCode().equals("1000")) {
                        final List<StudentListSigel> list = response.body().getStudents();
                        Log.d("AddFeedback", "SharedStudent list size: " + (list != null ? list.size() : "null"));
                        
                        student_list.clear();

                        if (list != null && !list.isEmpty()) {
                            for (StudentListSigel listSigel : list) {
                                String studentName = listSigel.getFullName();
                                student_list.add(studentName);
                                Log.d("AddFeedback", "Added student: " + studentName);
                            }
                            
                            Log.d("AddFeedback", "Final student_list size: " + student_list.size());
                            
                            // Update total records
                            if (total_records != null) {
                                total_records.setText("Total Students: " + student_list.size());
                            }
                            
                            ArrayAdapter student_name_adaptor = new ArrayAdapter<String>(context, R.layout.simple_list_item_1,
                                    student_list);

                            student_spinner.setAdapter(student_name_adaptor);

                            student_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                    child_id = list.get(position).getUniqueId();
                                    child_name = list.get(position).getFullName();
                                    parent_phone_number = list.get(position).getParentPhone();
                                    
                                    // Fallback to student phone if parent phone is empty
                                    if (parent_phone_number == null || parent_phone_number.isEmpty()) {
                                        parent_phone_number = list.get(position).getPhone();
                                        Log.d("AddFeedback", "Parent phone empty, using student phone");
                                    }

                                    Log.d("AddFeedback", "Student selected: " + child_name + " (ID: " + child_id + ")");
                                    Log.d("AddFeedback", "Parent/Contact phone: " + parent_phone_number);

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });

                            progress_bar.setVisibility(View.GONE);
                        } else {
                            Log.d("AddFeedback", "No students found for the selected class/subject.");
                            progress_bar.setVisibility(View.GONE);
                            if (total_records != null) {
                                total_records.setText("Total Students: 0");
                            }
                            Toast.makeText(context, "No students found for the selected class/subject.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        if (total_records != null) {
                            total_records.setText("Total Students: 0");
                        }
                        Log.e("AddFeedback", "Load_Student API call failed with status: " + response.body().getStatus().getCode());
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progress_bar.setVisibility(View.GONE);
                    if (total_records != null) {
                        total_records.setText("Total Students: 0");
                    }
                    Log.e("AddFeedback", "Load_Student API call failed with raw message: " + response.raw().message());
                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StudentListModel> call, Throwable e) {
                Log.e("AddFeedback", "Load_Student API call failed with error: " + e.getMessage());
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
                if (total_records != null) {
                    total_records.setText("Total Students: 0");
                }
                Toast.makeText(context, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Submit_feedback() {

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("campus_id", Constant.campus_id);
        postParam.put("session_id", Constant.current_session);
        postParam.put("child_id", child_id);
        postParam.put("feedback", write_feedbackk);
        postParam.put("operation", "add_feedback");
        postParam.put("subject_id", subject_id);


        JSONObject respone = new JSONObject(postParam);
        Constant.Log("" + respone);
        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

        Constant.mApiService.feedback(body).enqueue(new Callback<FeedbackModel>() {
            @Override
            public void onResponse(Call<FeedbackModel> call, Response<FeedbackModel> response) {
                if (response.body() != null) {


                    if (response.body().getStatus().getCode().equals("1000")) {

                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
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
            public void onFailure(Call<FeedbackModel> call, Throwable e) {
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
            }
        });
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

                        // Add bottom margin to button layout to push it above navigation bar
                        androidx.cardview.widget.CardView buttonLayout = findViewById(R.id.submit_feedback_card);
                        if (buttonLayout != null) {
                            // Set bottom margin to navigation bar height to ensure buttons are visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) buttonLayout.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 16; // 16dp original margin + navigation bar height
                                buttonLayout.setLayoutParams(params);
                            }
                        }

                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        android.util.Log.e("AddFeedback", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                android.util.Log.e("AddFeedback", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            android.util.Log.e("AddFeedback", "Error setting up window insets: " + e.getMessage(), e);
        }
    }

}
