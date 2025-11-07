package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import topgrade.parent.com.parentseeks.Teacher.Adaptor.StaffAttendanceSubmitAdapter;
import topgrade.parent.com.parentseeks.Teacher.Interface.AttendanceSubmitInterface;
import topgrade.parent.com.parentseeks.Teacher.Interface.SmsCheck;
import topgrade.parent.com.parentseeks.Teacher.Model.API.AttendanceSubmt_;
import topgrade.parent.com.parentseeks.Teacher.Model.AttendanceSubmitModel;
import topgrade.parent.com.parentseeks.Teacher.Model.SectionTest;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListSigel;
import topgrade.parent.com.parentseeks.Teacher.Model.SubjectTest;
import topgrade.parent.com.parentseeks.Teacher.Model.Teach;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;


public class StaffAttendanceSubmitSubject extends AppCompatActivity
        implements AttendanceSubmitInterface, View.OnClickListener, SmsCheck, CompoundButton.OnCheckedChangeListener {

    RecyclerView myrecycleview;

    Context context;
    TextView total_records;
    List<String> classForSubjectList = new ArrayList<>();
    private ProgressBar progress_bar;
    Spinner class_spinner, sectionSpinner, subjectSpinner;
    ArrayList<SectionTest> sectionList = new ArrayList<>();
    ArrayList<SubjectTest> subjectList = new ArrayList<>();
    String classId, subjectId, sectionId;

    List<AttendanceSubmitModel> attendanceSubmitModels = new ArrayList<>();
    TextView attendence_date;
    Button submit_attendence;
    final Calendar myCalendar = Calendar.getInstance();
    String myFormat = "dd MMMM,yyyy";
    String Api_date_format = "yyyy-MM-dd";
    String attdence_date = "";
    HashMap<String, Object> postParam = new HashMap<String, Object>();
    MaterialCheckBox smsCheck;
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
            attendence_date.setText(sdf.format(myCalendar.getTime()));
            attdence_date = new SimpleDateFormat(Api_date_format, Locale.getDefault()).format(myCalendar.getTime());


        }

    };

    public StaffAttendanceSubmitSubject() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_attendance_submit_subject);
        
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

        total_records = findViewById(R.id.total_records);
        // Find the TextView inside the LinearLayout with ID attendence_date
        LinearLayout attendenceDateLayout = findViewById(R.id.attendance_date);
        attendence_date = findViewById(R.id.attendence_date_text);
        submit_attendence = findViewById(R.id.submit_attendence);


        ImageView back_icon = findViewById(R.id.back_icon);


        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat(myFormat, Locale.getDefault());
        String formattedDate = df.format(c.getTime());
        attendence_date.setText(formattedDate);
        attdence_date = new SimpleDateFormat(Api_date_format, Locale.getDefault()).format(myCalendar.getTime());

        myrecycleview = findViewById(R.id.attendence_rcv);
        smsCheck = findViewById(R.id.sms_header_checkbox);
        if (smsCheck != null) {
            smsCheck.setChecked(true);
            smsCheck.setOnCheckedChangeListener(this);
        }

        back_icon.setOnClickListener(this);

        submit_attendence.setOnClickListener(this);
        attendenceDateLayout.setOnClickListener(this);

        context = StaffAttendanceSubmitSubject.this;
        Paper.init(context);
        
        // Load staff credentials from Paper database
        Constant.loadFromPaper();
        
        Initlization();
        loadClassForSubjects();
    }

    private void Initlization() {


        progress_bar = findViewById(R.id.progress_bar);
        class_spinner = findViewById(R.id.classs_spinner);
        sectionSpinner = findViewById(R.id.section_spinner);
        subjectSpinner = findViewById(R.id.subject_spinner);
        myrecycleview = findViewById(R.id.attendence_rcv);
        total_records = findViewById(R.id.total_records);
        
        // Set up RecyclerView LayoutManager (required for data to display)
        myrecycleview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        
        // Set titles only if spinners exist
        if (class_spinner != null) {
            // Regular Spinner doesn't have setTitle method
        }
        if (sectionSpinner != null) {
            // Regular Spinner doesn't have setTitle method
        }
        if (subjectSpinner != null) {
            // Regular Spinner doesn't have setTitle method
        }


    }

    private void loadClassForSubjects() {


        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("session_id", Constant.current_session);

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

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
                        class_spinner.setAdapter(student_class_adaptor);

                        class_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                classId = list.get(position).getStudentClassId();
                                sectionList.clear();
                                ArrayList<String> sectionNames = new ArrayList<>();
                                
                                // Only show sections where teacher actually teaches subjects for this class
                                for (Teach item : list) {
                                    if (item.getStudentClassId().equals(classId) && 
                                        item.getSubjectId() != null && !item.getSubjectId().isEmpty()){
                                        if (!sectionNames.contains(item.getSectionName())){
                                            sectionNames.add(item.getSectionName());
                                            sectionList.add(new SectionTest(
                                                    item.getSectionId(),
                                                    item.getSectionName()
                                            ));
                                        }
                                    }
                                }
                                
                                // Debug: Log available sections for this class
                                System.out.println("Available sections for class " + classId + ": " + sectionList.size());
                                for (SectionTest section : sectionList) {
                                    System.out.println("Section: " + section.getSection() + " (ID: " + section.getSectionId() + ")");
                                }
                                
                                setupSectionAdapter(list);
                                
                                // Clear previous student data when class changes
                                attendanceSubmitModels.clear();
                                myrecycleview.setVisibility(View.GONE);
                                total_records.setText("Total Records: 0");

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

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
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void setupSectionAdapter(final List<Teach> list) {
        if (sectionSpinner == null) {
            return; // Exit if sectionSpinner doesn't exist
        }
        
        ArrayAdapter<SectionTest> sectionAdapter = new ArrayAdapter<SectionTest>(context, R.layout.simple_list_item_1,
                sectionList);
        sectionSpinner.setAdapter(sectionAdapter);

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                sectionId = sectionList.get(position).getSectionId();
                
                // Setup Subject List for selected section - only show subjects teacher actually teaches
                subjectList.clear();
                ArrayList<String> subjectNames = new ArrayList<>();
                for (Teach item : list) {
                    if (item.getStudentClassId().equals(classId) && item.getSectionId().equals(sectionId)){
                        if (!subjectNames.contains(item.getSubjectName())){
                            subjectNames.add(item.getSubjectName());
                            subjectList.add(new SubjectTest(
                                    item.getSubjectId(),
                                    item.getSubjectName()
                            ));
                        }
                    }
                }
                
                // Debug: Log available subjects for this section
                System.out.println("Available subjects for section " + sectionId + ": " + subjectList.size());
                for (SubjectTest subject : subjectList) {
                    System.out.println("Subject: " + subject.getSubject() + " (ID: " + subject.getSubjectId() + ")");
                }
                
                setupSubjectAdapter();
                
                // Clear previous student data when section changes
                attendanceSubmitModels.clear();
                myrecycleview.setVisibility(View.GONE);
                total_records.setText("Total Records: 0");

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void setupSubjectAdapter() {
        if (subjectSpinner == null) {
            return; // Exit if subjectSpinner doesn't exist
        }
        
        ArrayAdapter<SubjectTest> subjectAdapter = new ArrayAdapter(context, R.layout.simple_list_item_1,
                subjectList);
        subjectSpinner.setAdapter(subjectAdapter);

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                subjectId = subjectList.get(position).getSubjectId();
                loadStudents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        

    }

    private void loadStudents() {
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("student_class_id", classId);
        postParam.put("subject_id", subjectId);
        postParam.put("section_id", sectionId);

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

        Constant.mApiService.load_students(body).enqueue(new Callback<StudentListModel>() {
            @Override
            public void onResponse(Call<StudentListModel> call, Response<StudentListModel> response) {

                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        List<StudentListSigel> list = response.body().getStudents();
                        attendanceSubmitModels.clear();
                        String smsStatus = (smsCheck != null && smsCheck.isChecked()) ? "1" : "0";
                        for (int i = 0; i < list.size(); i++) {
                            attendanceSubmitModels.add(new AttendanceSubmitModel(
                                    "1",
                                    "",
                                    list.get(i).getUniqueId(),
                                    list.get(i).getSectionId(),
                                    list.get(i).getParent_id(),
                                    smsStatus
                            ));
                        }
                        total_records.setText("Total Records: " + list.size());
                        myrecycleview.setVisibility(View.VISIBLE);
                        myrecycleview.setAdapter(new StaffAttendanceSubmitAdapter(context,
                                list,
                                StaffAttendanceSubmitSubject.this,
                                attendanceSubmitModels,
                                StaffAttendanceSubmitSubject.this));

                        progress_bar.setVisibility(View.GONE);

                        updateSmsHeaderState();
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        myrecycleview.setVisibility(View.GONE);
                        total_records.setText("Total Records: " + "0");
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    progress_bar.setVisibility(View.GONE);
                    myrecycleview.setVisibility(View.GONE);
                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<StudentListModel> call, Throwable e) {

                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.back_icon) {
            finish();

        } else if (id == R.id.submit_attendence) {
            if (attendanceSubmitModels.size() > 0) {
                boolean is_attendence_all_student = true;

                for (int i = 0; i < attendanceSubmitModels.size(); i++) {
                    if (attendanceSubmitModels.get(i).getAttendance().isEmpty()) {
                        is_attendence_all_student = false;
                        break;
                    }
                }

                if (is_attendence_all_student) {
                    Submit_Attendance();
                } else {
                    Toast.makeText(context, "Missing", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(context, "No students available for attendance", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.attendance_date) {
            new DatePickerDialog(
                    StaffAttendanceSubmitSubject.this,
                    date,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        }
    }


    private void Submit_Attendance() {
        JSONArray jsonArray = new JSONArray();
        Gson gson = new Gson();
        String listString = gson.toJson(
                attendanceSubmitModels,
                new TypeToken<ArrayList<AttendanceSubmitModel>>() {
                }.getType());
        try {
            jsonArray = new JSONArray(listString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postParam.put("operation", "submit_attendence");
        postParam.put("campus_id", Constant.campus_id);
        postParam.put("student_class_id", classId);
        postParam.put("subject_id", subjectId);
        postParam.put("session_id", Constant.current_session);
        postParam.put("created_date", attdence_date);
        postParam.put("attendances", jsonArray);
        JSONObject jsonObject = new JSONObject(postParam);
        System.out.println("Attendance: " + jsonObject);
        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));
        Constant.mApiService.attendence_student(body).enqueue(new Callback<AttendanceSubmt_>() {
            @Override
            public void onResponse(Call<AttendanceSubmt_> call, Response<AttendanceSubmt_> response) {
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
            public void onFailure(Call<AttendanceSubmt_> call, Throwable e) {
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void StatusSubmit(View view, final int position, String attendence,
                             final StaffAttendanceSubmitAdapter.AttendanceSubmitViewHolder holder) {

        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater()
                .inflate(R.menu.attendence_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                String attendence = item.getTitle().toString();
                int id = item.getItemId();

                String attendence_code = "";

                if (id == R.id.Present) {
                    attendence_code = "1";
                    holder.attendence_status.setBackgroundColor(Color.parseColor("#ffffff")); // White
                    holder.attendence_status.setTextColor(Color.parseColor("#000000")); // Black

                } else if (id == R.id.Absent) {
                    attendence_code = "2";
                    holder.attendence_status.setBackgroundColor(Color.parseColor("#DF4242")); // Red
                    holder.attendence_status.setTextColor(Color.parseColor("#ffffff")); // White

                } else if (id == R.id.Late) {
                    attendence_code = "3";
                    holder.attendence_status.setBackgroundColor(Color.parseColor("#90ee90")); // Green
                    holder.attendence_status.setTextColor(Color.parseColor("#000000")); // Black

                } else if (id == R.id.Leave) {
                    attendence_code = "4";
                    holder.attendence_status.setBackgroundColor(Color.parseColor("#90ee90")); // Green
                    holder.attendence_status.setTextColor(Color.parseColor("#000000")); // Black
                }

                holder.attendence_status.setText(attendence);
                AttendanceSubmitModel attendanceSubmitModel = attendanceSubmitModels.get(position);
                attendanceSubmitModel.setAttendance(attendence_code);
                attendanceSubmitModels.set(position, attendanceSubmitModel);
                return true;
            }
        });

        popup.show();

    }


    @Override
    public void NoteSubmit(int position, String note) {
        AttendanceSubmitModel attendanceSubmitModel = attendanceSubmitModels.get(position);
        attendanceSubmitModel.setNote(note);
        attendanceSubmitModels.set(position, attendanceSubmitModel);


    }

    @Override
    public void OnSmsCheck(boolean is_sms, int position) {
        String status = "";
        if (is_sms) {
            status = "1";
        } else {
            status = "0";
        }

        AttendanceSubmitModel attendence = attendanceSubmitModels.get(position);
        attendence.setSms(status);
        attendanceSubmitModels.set(position, attendence);

        updateSmsHeaderState();

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        String status = b ? "1" : "0";

        if (smsCheck != null && smsCheck != compoundButton) {
            smsCheck.setOnCheckedChangeListener(null);
            smsCheck.setChecked(b);
            smsCheck.setOnCheckedChangeListener(this);
        }
        for (int i = 0; i < attendanceSubmitModels.size(); i++) {
            AttendanceSubmitModel attendence = attendanceSubmitModels.get(i);
            attendence.setSms(status);
            attendanceSubmitModels.set(i, attendence);
        }

        if (myrecycleview != null && myrecycleview.getAdapter() != null) {
            myrecycleview.getAdapter().notifyDataSetChanged();
        }
    }

    private void updateSmsHeaderState() {
        if (smsCheck == null) {
            return;
        }

        boolean allChecked = true;
        for (AttendanceSubmitModel model : attendanceSubmitModels) {
            if (!"1".equals(model.getSms())) {
                allChecked = false;
                break;
            }
        }

        smsCheck.setOnCheckedChangeListener(null);
        smsCheck.setChecked(allChecked);
        smsCheck.setOnCheckedChangeListener(this);
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

                        // Add bottom margin to main content to push it above navigation bar
                        LinearLayout mainContent = findViewById(R.id.main_content);
                        if (mainContent != null) {
                            // Set bottom margin to navigation bar height to ensure content is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) mainContent.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 16; // 16dp extra padding
                                mainContent.setLayoutParams(params);
                            }
                        }

                        // Add bottom margin to bottom layout to push it above navigation bar
                        LinearLayout bottomLayout = findViewById(R.id.bottom_layout);
                        if (bottomLayout != null) {
                            // Set bottom margin to navigation bar height to ensure buttons are visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) bottomLayout.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 16; // 16dp extra padding
                                bottomLayout.setLayoutParams(params);
                            }
                        }

                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        android.util.Log.e("StaffAttendanceSubmitSubject", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                android.util.Log.e("StaffAttendanceSubmitSubject", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            android.util.Log.e("StaffAttendanceSubmitSubject", "Error setting up window insets: " + e.getMessage(), e);
        }
    }
}
