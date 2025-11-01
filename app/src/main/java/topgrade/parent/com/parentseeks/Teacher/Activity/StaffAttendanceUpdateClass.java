package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;


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
import topgrade.parent.com.parentseeks.Teacher.Adaptor.AttendanceUpdateAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Interface.AttendanceEditnterface;
import topgrade.parent.com.parentseeks.Teacher.Model.API.TeacherAttendanceModel;
import topgrade.parent.com.parentseeks.Teacher.Model.API.AttendanceSubmt_;
import topgrade.parent.com.parentseeks.Teacher.Model.Teach;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Model.teacher_load_profile.TeacherProfileData;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class StaffAttendanceUpdateClass extends AppCompatActivity
        implements
        View.OnClickListener, AttendanceEditnterface {


    RecyclerView myrecycleview;

    Context context;
    TextView total_records;
    List<String> class_list = new ArrayList<>();
    List<Teach> class_incharges_list = new ArrayList<>();

    private ProgressBar progress_bar;
    Spinner class_spinner;
    String class_id, attedence_status;
    androidx.appcompat.app.AlertDialog alert_dialog;
    EditText note_tv, staff_pwd;
    TextView attendence_date;
    List<TeacherAttendanceModel> attendance_list = new ArrayList<>();
    final Calendar myCalendar = Calendar.getInstance();
    String myFormat = "dd MMMM,yyyy";
    String Api_date_format = "yyyy-MM-dd";
    String attdence_date = "";
    List<String> attendence_student = new ArrayList<>();
    ArrayAdapter<String> attendece_status_spinner;

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


            //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
            attendence_date.setText(sdf.format(myCalendar.getTime()));
            attdence_date = new SimpleDateFormat(Api_date_format, Locale.getDefault()).format(myCalendar.getTime());
            Load_Attendance();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_attendance_update_class);
        total_records = findViewById(R.id.total_records);
        // Find the TextView inside the LinearLayout with ID attendence_date
        LinearLayout attendenceDateLayout = findViewById(R.id.attendance_date);
        attendence_date = findViewById(R.id.attendence_date_text);


        ImageView back_icon = findViewById(R.id.back_icon);
        Button update_attendance = findViewById(R.id.update_attendance);

        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat(myFormat, Locale.getDefault());
        String formattedDate = df.format(c.getTime());
        attendence_date.setText(formattedDate);
        attdence_date = new SimpleDateFormat(Api_date_format, Locale.getDefault()).format(myCalendar.getTime());

        myrecycleview = findViewById(R.id.attendance_rv);

        back_icon.setOnClickListener(this);
        update_attendance.setOnClickListener(this);

        attendenceDateLayout.setOnClickListener(this);

        context = StaffAttendanceUpdateClass.this;
        Paper.init(context);
        
        // Load staff credentials from Paper database
        Constant.loadFromPaper();
        
        Initlization();
        Load_Class();
    }

    private void Initlization() {


        progress_bar = findViewById(R.id.progress_bar);
        class_spinner = findViewById(R.id.class_spinner);
        myrecycleview = findViewById(R.id.attendance_rv);
        total_records = findViewById(R.id.total_records);
        
        // Set up RecyclerView LayoutManager (required for data to display)
        if (myrecycleview != null) {
            myrecycleview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            System.out.println("DEBUG: RecyclerView LayoutManager set");
        } else {
            System.err.println("ERROR: RecyclerView is null - check layout file for correct ID");
        }
        
        // Regular Spinner doesn't have setTitle method


        attendence_student.add("Pre");
        attendence_student.add("Abs");
        attendence_student.add("H.lev");
        attendence_student.add("F.lev");


    }

    private void Load_Class() {


        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("session_id", Constant.current_session);

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.load_profile(body).enqueue(new Callback<TeachModel>() {
            @Override
            public void onResponse(Call<TeachModel> call, Response<TeachModel> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        List<Teach> teachList = response.body().getTeach();
                        TeacherProfileData data = response.body().getData();
                        
                        // Use the same logic as submit class - get class from TeacherProfileData
                        if (data != null && data.getStudentClassId() != null && !data.getStudentClassId().equals("") && 
                            data.getStudentClassName() != null && !data.getStudentClassName().equals("")) {
                            
                            // Clear previous data
                            class_list.clear();
                            class_incharges_list.clear();
                            
                            // Add the single class to the list (same as submit class logic)
                            class_list.add(data.getStudentClassName());
                            class_id = data.getStudentClassId();
                            
                            // DEBUG: Log class information
                            System.out.println("DEBUG: Class loaded - Name: " + data.getStudentClassName() + ", ID: " + class_id);
                            
                            // Find the corresponding Teach object for this class
                            for (Teach teach : teachList) {
                                if (teach.getIsClassIncharge() != null && teach.getIsClassIncharge().equals(data.getStudentClassId())) {
                                    class_incharges_list.add(teach);
                                    break;
                                }
                            }

                            ArrayAdapter student_class_adaptor = new ArrayAdapter<String>(context, R.layout.simple_list_item_1,
                                    class_list);
                            class_spinner.setAdapter(student_class_adaptor);

                            class_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    class_id = data.getStudentClassId();
                                    Load_Attendance();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                            
                            // Automatically select the first class and load attendance
                            class_spinner.setSelection(0);
                            Load_Attendance();
                        } else {
                            Toast.makeText(context, "No class found for this teacher", Toast.LENGTH_SHORT).show();
                        }

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


    private void Load_Attendance() {

        if (class_id == null || class_id.isEmpty()) {
            Toast.makeText(context, "Please select a class first.", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("operation", "read_attendence");
        postParam.put("campus_id", Constant.campus_id);
        postParam.put("student_class_id", class_id);
        postParam.put("subject_id", "");
        postParam.put("session_id", Constant.current_session);
        postParam.put("created_date", attdence_date);


        JSONObject jsonObject = new JSONObject(postParam);
        System.out.println("DEBUG: Load_Attendance API Request: " + jsonObject);
        System.out.println("DEBUG: Parameters - class_id: " + class_id + ", date: " + attdence_date);

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.attendence_student_full(body).enqueue(new Callback<AttendanceSubmt_>() {
            @Override
            public void onResponse(Call<AttendanceSubmt_> call, Response<AttendanceSubmt_> response) {

                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        attendance_list = response.body().getAttendance();
                        
                        // DEBUG: Log the data we received
                        System.out.println("DEBUG: API Response - Status: " + response.body().getStatus().getCode());
                        System.out.println("DEBUG: Attendance list size: " + attendance_list.size());
                        
                        if (attendance_list != null && !attendance_list.isEmpty()) {
                            System.out.println("DEBUG: First attendance record: " + attendance_list.get(0).toString());
                        } else {
                            System.out.println("DEBUG: Attendance list is null or empty!");
                        }

                        total_records.setText("Total Records: " + attendance_list.size());
                        myrecycleview.setVisibility(View.VISIBLE);
                        
                        // DEBUG: Check if RecyclerView has LayoutManager
                        if (myrecycleview.getLayoutManager() == null) {
                            System.out.println("DEBUG: RecyclerView has NO LayoutManager!");
                            myrecycleview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(StaffAttendanceUpdateClass.this));
                        }
                        
                        AttendanceUpdateAdaptor adapter = new AttendanceUpdateAdaptor(context,
                                attendance_list,
                                StaffAttendanceUpdateClass.this);
                        myrecycleview.setAdapter(adapter);
                        
                        // DEBUG: Force notify data changed
                        adapter.notifyDataSetChanged();
                        System.out.println("DEBUG: Adapter set with " + attendance_list.size() + " items");

                        progress_bar.setVisibility(View.GONE);
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        attendance_list.clear();
                        total_records.setText("Total Records: " + "0");
                        myrecycleview.setVisibility(View.VISIBLE);
                        myrecycleview.setAdapter(new AttendanceUpdateAdaptor(context,
                                attendance_list,
                                StaffAttendanceUpdateClass.this));
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    progress_bar.setVisibility(View.GONE);
                    myrecycleview.setVisibility(View.GONE);
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
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.back_icon) {
            finish();

        } else if (id == R.id.attendance_date) {
            new DatePickerDialog(StaffAttendanceUpdateClass.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();

        } else if (id == R.id.update_attendance) {
            // Handle update attendance button click
            Toast.makeText(this, "Update attendance functionality", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.Cancel || id == R.id.Cancel_) {
            alert_dialog.dismiss();
        }
    }


    private void Update_Attendece(int position) {
        // Validate position and list
        if (attendance_list == null || position < 0 || position >= attendance_list.size()) {
            Toast.makeText(this, "Invalid attendance data", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate note_tv
        String note = "";
        if (note_tv != null) {
            note = note_tv.getText() != null ? note_tv.getText().toString() : "";
        }
        
        HashMap<String, Object> postParam = new HashMap<String, Object>();
        postParam.put("operation", "edit_attendence");
        postParam.put("campus_id", Constant.campus_id);
        postParam.put("child_id", attendance_list.get(position).getEmployeeId());
        postParam.put("student_class_id", class_id);
        postParam.put("subject_id", "");
        postParam.put("attendance", attedence_status);
        postParam.put("session_id", Constant.current_session);
        postParam.put("note", note);
        postParam.put("created_date", attdence_date);
        postParam.put("attendance_id", attendance_list.get(position).getUniqueId());

        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());
        Constant.mApiService.attendence_student_full(body).enqueue(new Callback<AttendanceSubmt_>() {
            @Override
            public void onResponse(Call<AttendanceSubmt_> call, Response<AttendanceSubmt_> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        progress_bar.setVisibility(View.GONE);
                        Load_Attendance();
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
    public void AttendanceUpdate(View view, final int position, String status, String note) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setView(R.layout.enter_note);
        alert_dialog = builder.create();


        alert_dialog.setCancelable(true);
        alert_dialog.setCanceledOnTouchOutside(true);
        alert_dialog.show();
        Spinner attendence_status = alert_dialog.findViewById(R.id.attendence_status);
        note_tv = alert_dialog.findViewById(R.id.note);
        staff_pwd = alert_dialog.findViewById(R.id.staff_pwd);
        ImageView Cancel = alert_dialog.findViewById(R.id.Cancel);
        Button Cancel_ = alert_dialog.findViewById(R.id.Cancel_);
        Button Save_Note = alert_dialog.findViewById(R.id.Save_Note);
        note_tv.setText(note);
        attendece_status_spinner = new ArrayAdapter<String>(
                context, R.layout.simple_list_item_1, attendence_student) {
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.parseColor("#000000"));
                tv.setBackgroundColor(Color.parseColor("#F5F5F5"));

                return view;
            }
        };


        attendence_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                String attendence_name = attendence_student.get(i);
                switch (attendence_name) {
                    case "Pre":
                        attedence_status = "1";
                        break;
                    case "Abs":
                        attedence_status = "2";
                        break;
                    case "H.lev":
                        attedence_status = "3";
                        break;
                    case "F.lev":
                        attedence_status = "4";
                        break;

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        attendence_status.setAdapter(attendece_status_spinner);

        Cancel.setOnClickListener(this);
        Cancel_.setOnClickListener(this);
        Save_Note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Password verification removed - direct update
                alert_dialog.dismiss();
                Update_Attendece(position);

            }
        });


        switch (status) {
            case "1":
                attendence_status.setSelection(0);
                break;
            case "2":
                attendence_status.setSelection(1);
                break;
            case "3":
                attendence_status.setSelection(2);
                break;
            case "4":
                attendence_status.setSelection(3);
                break;

        }

    }
}
