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
import topgrade.parent.com.parentseeks.Teacher.Model.TeachSection;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;


public class StaffAttendanceSubmitSection extends AppCompatActivity
        implements AttendanceSubmitInterface, View.OnClickListener, SmsCheck, CompoundButton.OnCheckedChangeListener {

    RecyclerView myrecycleview;

    Context context;
    TextView total_records;
    List<String> class_list = new ArrayList<>();
    private ProgressBar progress_bar;
    Spinner classSpinner, sectionSpinner;
    ArrayList<SectionTest> sectionList = new ArrayList<>();

    String classId, sectionId;

    List<AttendanceSubmitModel> attendanceSubmitModels = new ArrayList<>();
    TextView attendence_date;
    Button submit_attendence;
    final Calendar myCalendar = Calendar.getInstance();
    String myFormat = "dd MMM yy";
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

    public StaffAttendanceSubmitSection() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_attendance_submit_section);
        
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
        // Find the TextView inside the LinearLayout with ID attendance_date
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

        context = StaffAttendanceSubmitSection.this;
        Paper.init(context);
        
        // Load staff credentials from Paper database
        Constant.loadFromPaper();
        
        Initlization();
        Load_Class();
    }

    private void Initlization() {


        progress_bar = findViewById(R.id.progress_bar);
        classSpinner = findViewById(R.id.classs_spinner);
        sectionSpinner = findViewById(R.id.section_spinner);
        myrecycleview = findViewById(R.id.attendence_rcv);
        total_records = findViewById(R.id.total_records);
        
        // Set up RecyclerView LayoutManager (required for data to display)
        myrecycleview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        
        // Regular Spinners don't have setTitle method


    }

    private void Load_Class() {


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
                        final List<TeachSection> list = response.body().getTeachSection();


                        for (TeachSection teachSection : list) {
                            if (!class_list.contains(teachSection.getClassName())){
                                class_list.add(teachSection.getClassName());
                            }
                        }


                        // Ensure UI updates happen on main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter student_class_adaptor = new ArrayAdapter<String>(context, R.layout.simple_list_item_1,
                                        class_list);
                                classSpinner.setAdapter(student_class_adaptor);

                                classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                classId = list.get(position).getStudentClassId();
                                android.util.Log.d("StaffAttendanceSubmitSection", "Class selected: " + class_list.get(position) + " ID: " + classId);
                                
                                sectionList.clear();
                                ArrayList<String> sectionNames = new ArrayList<>();
                                android.util.Log.d("StaffAttendanceSubmitSection", "Processing " + list.size() + " teach sections for class: " + classId);
                                
                                for (TeachSection item : list) {
                                    android.util.Log.d("StaffAttendanceSubmitSection", "TeachSection - Class: " + item.getStudentClassId() + ", Section: " + item.getSectionName() + ", SectionID: " + item.getSectionId());
                                    
                                    // Only add sections that belong to the selected class
                                    if (item.getStudentClassId().equals(classId) && !sectionNames.contains(item.getSectionName())){
                                        sectionNames.add(item.getSectionName());
                                        sectionList.add(new SectionTest(
                                                item.getSectionId(),
                                                item.getSectionName()
                                        ));
                                        android.util.Log.d("StaffAttendanceSubmitSection", "Added section: " + item.getSectionName());
                                    }
                                }
                                android.util.Log.d("StaffAttendanceSubmitSection", "Sections found: " + sectionList.size());
                                setupSectionAdapter();
                                
                                // Clear previous student data when class changes
                                attendanceSubmitModels.clear();
                                if (myrecycleview != null) {
                                    if (myrecycleview != null) {
                                    myrecycleview.setVisibility(View.GONE);
                                }
                                }
                                if (total_records != null) {
                                    total_records.setText("Total Records: 0");
                                }

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                                
                                // Automatically select the first class to trigger section loading
                                if (class_list.size() > 0) {
                                    classSpinner.setSelection(0);
                                }
                            }
                        });

                        progress_bar.setVisibility(View.GONE);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress_bar.setVisibility(View.GONE);
                                Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress_bar.setVisibility(View.GONE);
                            Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<TeachModel> call, Throwable e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupSectionAdapter() {
        if (sectionSpinner == null) {
            android.util.Log.d("StaffAttendanceSubmitSection", "sectionSpinner is null, cannot setup adapter");
            return; // Exit if sectionSpinner doesn't exist
        }
        
        android.util.Log.d("StaffAttendanceSubmitSection", "Setting up section adapter with " + sectionList.size() + " sections");
        
        ArrayAdapter<SectionTest> sectionAdapter = new ArrayAdapter<SectionTest>(context, R.layout.simple_list_item_1,
                sectionList);
        sectionSpinner.setAdapter(sectionAdapter);
        
        android.util.Log.d("StaffAttendanceSubmitSection", "Section adapter set successfully");

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                sectionId = sectionList.get(position).getSectionId();
                android.util.Log.d("StaffAttendanceSubmitSection", "Section selected: " + sectionList.get(position).getSection() + " ID: " + sectionId);
                loadStudents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        
        // Automatically select the first section to trigger student loading
        if (sectionList.size() > 0) {
            android.util.Log.d("StaffAttendanceSubmitSection", "Auto-selecting first section from list size: " + sectionList.size());
            sectionSpinner.setSelection(0);
            android.util.Log.d("StaffAttendanceSubmitSection", "Section selection set to 0");
        } else {
            android.util.Log.d("StaffAttendanceSubmitSection", "Section list is empty, cannot auto-select");
        }

    }

    private void loadStudents() {
        android.util.Log.d("StaffAttendanceSubmitSection", "loadStudents() called with classId: " + classId + " sectionId: " + sectionId);
        
        // Check if both classId and sectionId are set
        if (classId == null || classId.isEmpty()) {
            android.util.Log.d("StaffAttendanceSubmitSection", "classId is null or empty, cannot load students");
            Toast.makeText(context, "Please select a class first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (sectionId == null || sectionId.isEmpty()) {
            android.util.Log.d("StaffAttendanceSubmitSection", "sectionId is null or empty, cannot load students");
            Toast.makeText(context, "Please select a section first.", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("student_class_id", classId);
        postParam.put("section_id", sectionId);
        
        if (progress_bar != null) {
            progress_bar.setVisibility(View.VISIBLE);
            android.util.Log.d("StaffAttendanceSubmitSection", "Progress bar set to visible");
        } else {
            android.util.Log.d("StaffAttendanceSubmitSection", "Progress bar is null");
        }
        
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));
        
        // Debug: Log the load students request
        android.util.Log.d("StaffAttendanceSubmitSection", "loadStudents request: " + new JSONObject(postParam).toString());

        Constant.mApiService.load_students(body).enqueue(new Callback<StudentListModel>() {
            @Override
            public void onResponse(Call<StudentListModel> call, Response<StudentListModel> response) {

                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        List<StudentListSigel> list = response.body().getStudents();
                        
                        // Ensure UI updates happen on main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
                                if (myrecycleview != null) {
                                    myrecycleview.setVisibility(View.VISIBLE);
                                    myrecycleview.setAdapter(new StaffAttendanceSubmitAdapter(context,
                                        list,
                                        StaffAttendanceSubmitSection.this,
                                        attendanceSubmitModels,
                                        StaffAttendanceSubmitSection.this));
                                }

                                updateSmsHeaderState();

                                progress_bar.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress_bar.setVisibility(View.GONE);
                                if (myrecycleview != null) {
                                    myrecycleview.setVisibility(View.GONE);
                                }
                                total_records.setText("Total Records: " + "0");
                                Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress_bar.setVisibility(View.GONE);
                            myrecycleview.setVisibility(View.GONE);
                            Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            @Override
            public void onFailure(Call<StudentListModel> call, Throwable e) {

                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                boolean is_attendence_all_student = false;

                for (int i = 0; i < attendanceSubmitModels.size(); i++) {
                    if (attendanceSubmitModels.get(i).getAttendance().isEmpty()) {
                        is_attendence_all_student = false;
                        break;
                    } else {
                        is_attendence_all_student = true;
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
                    StaffAttendanceSubmitSection.this,
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
        postParam.put("section_id", sectionId);
        postParam.put("session_id", Constant.current_session);
        postParam.put("created_date", attdence_date);
        postParam.put("attendances", jsonArray);
        JSONObject jsonObject = new JSONObject(postParam);
        System.out.println("Attendance: " + jsonObject);
        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));
//        Constant.mApiService.attendence_student(body).enqueue(new Callback<AttendanceSubmt_>() {
        Constant.mApiService.attendence_student_full (body).enqueue(new Callback<AttendanceSubmt_>() {
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

                        // Add bottom margin to submit button to push it above navigation bar
                        com.google.android.material.button.MaterialButton submitButton = findViewById(R.id.submit_attendence);
                        if (submitButton != null) {
                            // Set bottom margin to navigation bar height to ensure button is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) submitButton.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 16; // 16dp extra padding
                                submitButton.setLayoutParams(params);
                            }
                        }

                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        android.util.Log.e("StaffAttendanceSubmitSection", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                android.util.Log.e("StaffAttendanceSubmitSection", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            android.util.Log.e("StaffAttendanceSubmitSection", "Error setting up window insets: " + e.getMessage(), e);
        }
    }
}
