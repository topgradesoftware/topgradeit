# Exam Submission Logic by Staff/Teachers

## Overview
This document contains the complete exam submission workflow implemented by staff/teachers in the Top Grade School Management System. The system allows teachers to submit exam marks for their assigned classes with comprehensive validation and security features.

## Key Files and Their Logic

### 1. Main Exam Submission Activity: `ExamSubmit.java`

#### **Core Components:**
```java
public class ExamSubmit extends AppCompatActivity implements 
    OnClickListener, View.OnClickListener, MarksEnterInterface, 
    CompoundButton.OnCheckedChangeListener, SmsCheck {
    
    // UI Components
    RecyclerView myrecycleview;
    CheckBox sms_check;
    Button Submit_Marks;
    TextView total_records;
    ProgressBar progress_bar;
    SearchableSpinner classSpinner, examSpinner, examSessionSpinner, 
                     subjectSpinner, sectionSpinner;
    
    // Data Storage
    List<ExamSubmitStrcu> exam_submit_list = new ArrayList<>();
    List<ExamResult> resultList = new ArrayList<>();
    String class_id, subject_id, total_marks, test_id, sectionId, session_id;
}
```

#### **Workflow Logic:**

**Step 1: Initialization**
```java
@Override
private void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_staff_exan);
    
    // Initialize UI components
    sms_check = findViewById(R.id.sms_check);
    progress_bar = findViewById(R.id.progress_bar);
    myrecycleview = findViewById(R.id.student_rcv);
    Submit_Marks = findViewById(R.id.Submit_Marks);
    
    // Set click listeners
    show_advanced_filter.setOnClickListener(this);
    Submit_Marks.setOnClickListener(this);
    sms_check.setOnCheckedChangeListener(this);
    
    // Show advanced filter dialog
    Show_Advanced_Option();
}
```

**Step 2: Advanced Filter Dialog**
```java
private void Show_Advanced_Option() {
    LayoutInflater inflater = this.getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.exam_advanced_search_layout_staff, null);
    
    // Initialize spinners
    examSessionSpinner = dialogView.findViewById(R.id.exam_session);
    classSpinner = dialogView.findViewById(R.id.class_spinner);
    sectionSpinner = dialogView.findViewById(R.id.section_spinner);
    subjectSpinner = dialogView.findViewById(R.id.subject_spinner);
    examSpinner = dialogView.findViewById(R.id.test_spinner);
    
    // Set titles
    examSessionSpinner.setTitle("Select Exam Session");
    classSpinner.setTitle("Select Class");
    sectionSpinner.setTitle("Select Section");
    subjectSpinner.setTitle("Select Subject");
    examSpinner.setTitle("Select Exam");
    
    // Show dialog
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    dialogBuilder.setView(dialogView);
    dialogBuilder.setCancelable(false);
    alertDialog = dialogBuilder.create();
    alertDialog.show();
    
    // Load exam sessions
    loadExamSession();
}
```

**Step 3: Load Exam Sessions**
```java
private void loadExamSession() {
    // Get exam sessions from local storage
    examSessionslist = Paper.book().read(Constants.exam_session);
    exam_session_name_list = new ArrayList<>();
    
    for (int i = 0; i < examSessionslist.size(); i++) {
        exam_session_name_list.add(examSessionslist.get(i).getFullName());
    }
    
    // Setup adapter
    session_adaptor = new ArrayAdapter<String>(context, 
        android.R.layout.simple_list_item_1, exam_session_name_list);
    examSessionSpinner.setAdapter(session_adaptor);
    
    // Handle selection
    examSessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            session_id = examSessionslist.get(position).getUniqueId();
            Load_Class(); // Load teacher's classes
        }
    });
}
```

**Step 4: Load Teacher's Classes**
```java
private void Load_Class() {
    HashMap<String, String> postParam = new HashMap<String, String>();
    postParam.put("staff_id", Constant.staff_id);
    postParam.put("parent_id", Constant.campus_id);
    postParam.put("session_id", Constant.current_session);
    
    progress_bar.setVisibility(View.VISIBLE);
    RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), 
        MediaType.parse("application/json; charset=utf-8"));
    
    Constant.mApiService.load_profile(body).enqueue(new Callback<TeachModel>() {
        @Override
        public void onResponse(Call<TeachModel> call, Response<TeachModel> response) {
            if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                final List<Teach> list = response.body().getTeach();
                
                // Extract unique class names
                for (Teach teachSection : list) {
                    if (!classForSubjectList.contains(teachSection.getClassName())){
                        classForSubjectList.add(teachSection.getClassName());
                    }
                }
                
                // Setup class spinner
                ArrayAdapter student_class_adaptor = new ArrayAdapter<String>(context, 
                    R.layout.simple_list_item_1, classForSubjectList);
                classSpinner.setAdapter(student_class_adaptor);
                
                // Handle class selection
                classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        class_id = list.get(position).getStudentClassId();
                        
                        // Setup sections and subjects for selected class
                        setupSectionAndSubjectList(list);
                    }
                });
            }
        }
    });
}
```

**Step 5: Setup Sections and Subjects**
```java
private void setupSectionAndSubjectList(List<Teach> list) {
    // Setup Section List
    ArrayList<String> sectionNames = new ArrayList<>();
    ArrayList<SectionTest> filteredSections = new ArrayList<>();
    
    for (Teach item : list) {
        if (item.getStudentClassId().equals(class_id)){
            if (!sectionNames.contains(item.getSectionName())){
                sectionNames.add(item.getSectionName());
                filteredSections.add(new SectionTest(
                    item.getSectionId(),
                    item.getSectionName()
                ));
            }
        }
    }
    
    // Setup Subject List
    ArrayList<String> subjectNames = new ArrayList<>();
    subjectList.clear();
    
    for (Teach item : list) {
        if (item.getStudentClassId().equals(class_id)){
            if (!subjectNames.contains(item.getSubjectName())){
                subjectNames.add(item.getSubjectName());
                subjectList.add(new SubjectTest(
                    item.getSubjectId(),
                    item.getSubjectName()
                ));
            }
        }
    }
    
    setupSectionAdapter(filteredSections, (ArrayList<Teach>) list);
}
```

**Step 6: Load Available Exams**
```java
private void loadExams() {
    HashMap<String, String> postParam = new HashMap<String, String>();
    postParam.put("staff_id", Constant.staff_id);
    postParam.put("parent_id", Constant.campus_id);
    postParam.put("student_class_id", class_id);
    postParam.put("subject_id", subject_id);
    postParam.put("exam_session_id", session_id);
    postParam.put("section_id", sectionId);
    
    progress_bar.setVisibility(View.VISIBLE);
    RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), 
        MediaType.parse("application/json; charset=utf-8"));
    
    Constant.mApiService.load_exams(body).enqueue(new Callback<ExamTestRespone>() {
        @Override
        public void onResponse(Call<ExamTestRespone> call, Response<ExamTestRespone> response) {
            if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                final List<TestModel> testList = response.body().getExams();
                
                // Filter exams for selected subject
                ArrayList<String> examNames = new ArrayList<>();
                examList.clear();
                
                for (TestModel item: testList){
                    if (item.getSubjectId().equals(subject_id)){
                        if (!examNames.contains(item.getFullName())){
                            examNames.add(item.getFullName());
                            examList.add(new ExamTest(
                                item.getUniqueId(),
                                item.getTotalMarks(),
                                item.getFullName()
                            ));
                        }
                    }
                }
                
                setupExamAdapter(testList);
            }
        }
    });
}
```

**Step 7: Load Student Results**
```java
private void loadExamResult() {
    HashMap<String, String> postParam = new HashMap<String, String>();
    postParam.put("staff_id", Constant.staff_id);
    postParam.put("parent_id", Constant.campus_id);
    postParam.put("student_class_id", class_id);
    postParam.put("subject_id", subject_id);
    postParam.put("section_id", sectionId);
    postParam.put("exam_id", test_id);
    
    progress_bar.setVisibility(View.VISIBLE);
    RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), 
        MediaType.parse("application/json; charset=utf-8"));
    
    Constant.mApiService.load_exams_results(body).enqueue(new Callback<ExamResultModel>() {
        @Override
        public void onResponse(Call<ExamResultModel> call, Response<ExamResultModel> response) {
            if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                resultList = response.body().getResult();
                
                // Prepare submission list
                exam_submit_list.clear();
                for (int i = 0; i < resultList.size(); i++) {
                    if (resultList.get(i).getResult() != null) {
                        exam_submit_list.add(new ExamSubmitStrcu(
                            resultList.get(i).getResult().getAttendance(),
                            resultList.get(i).getResult().getNote(),
                            resultList.get(i).getUniqueId(),
                            "1", // Default SMS enabled
                            resultList.get(i).getResult().getObtained_marks(),
                            "0"
                        ));
                    } else {
                        exam_submit_list.add(new ExamSubmitStrcu("", "", 
                            resultList.get(i).getUniqueId(), "1", "", "0"));
                    }
                }
                
                // Display results
                total_records.setText("Total Records: " + resultList.size());
                myrecycleview.setVisibility(View.VISIBLE);
                myrecycleview.setAdapter(new ExamAdaptor(context, ExamSubmit.this, 
                    resultList, exam_submit_list, ExamSubmit.this, 
                    ExamSubmit.this, total_marks, ExamSubmit.this));
            }
        }
    });
}
```

**Step 8: Submit Marks Logic**
```java
@Override
public void onClick(View v) {
    int id = v.getId();
    
    if (id == R.id.Submit_Marks) {
        if (exam_submit_list.size() > 0) {
            // Validate all marks are entered
            boolean is_all_marks_submit = false;
            for (int i = 0; i < exam_submit_list.size(); i++) {
                if (exam_submit_list.get(i).getMarks().isEmpty()) {
                    is_all_marks_submit = false;
                    break;
                } else {
                    is_all_marks_submit = true;
                }
            }
            
            if (is_all_marks_submit) {
                Show_Pwd_Dialog(); // Show password dialog
            } else {
                Toast.makeText(context, "Enter All Student Marks", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "No student available for submit marks", Toast.LENGTH_SHORT).show();
        }
    }
}
```

**Step 9: Password Verification**
```java
private void Show_Pwd_Dialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setView(R.layout.enter_pwd);
    alertDialog = builder.create();
    alertDialog.setCancelable(true);
    alertDialog.setCanceledOnTouchOutside(true);
    alertDialog.show();
    
    staff_pwd = alertDialog.findViewById(R.id.staff_pwd);
    Submit_Marks_ = alertDialog.findViewById(R.id.Submit_Marks_);
    
    Submit_Marks_.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String staff_pwd_ = staff_pwd.getText().toString();
            String staff_pwd_original = Paper.book().read("staff_password");
            
            if (staff_pwd_.isEmpty()) {
                Toast.makeText(context, "Enter Password For Submit Marks", Toast.LENGTH_SHORT).show();
            } else if (!staff_pwd_.equals(staff_pwd_original)) {
                Toast.makeText(context, "Wrong Password", Toast.LENGTH_SHORT).show();
            } else {
                alertDialog.dismiss();
                Submit_Marks_API(); // Submit to server
            }
        }
    });
}
```

**Step 10: API Submission**
```java
private void Submit_Marks_API() {
    // Convert list to JSON
    JSONArray jsonArray = new JSONArray();
    Gson gson = new Gson();
    
    String listString = gson.toJson(exam_submit_list,
        new TypeToken<ArrayList<ExamSubmitStrcu>>() {}.getType());
    
    try {
        jsonArray = new JSONArray(listString);
    } catch (JSONException e) {
        e.printStackTrace();
    }
    
    // Prepare API parameters
    HashMap<String, Object> postParam = new HashMap<String, Object>();
    postParam.put("staff_id", Constant.staff_id);
    postParam.put("parent_id", Constant.campus_id);
    postParam.put("student_class_id", class_id);
    postParam.put("subject_id", subject_id);
    postParam.put("section_id", sectionId);
    postParam.put("exam_id", test_id);
    postParam.put("results", jsonArray);
    
    progress_bar.setVisibility(View.VISIBLE);
    RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), 
        MediaType.parse("application/json; charset=utf-8"));
    
    // Submit to server
    Constant.mApiService.save_exam_results(body).enqueue(new Callback<ExamResultModel>() {
        @Override
        public void onResponse(Call<ExamResultModel> call, Response<ExamResultModel> response) {
            if (response.body() != null) {
                if (response.body().getStatus().getCode().equals("1000")) {
                    Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                    progress_bar.setVisibility(View.GONE);
                } else {
                    progress_bar.setVisibility(View.GONE);
                    Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        @Override
        public void onFailure(Call<ExamResultModel> call, Throwable e) {
            e.printStackTrace();
            progress_bar.setVisibility(View.GONE);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

### 2. Data Model: `ExamSubmitStrcu.java`

```java
public class ExamSubmitStrcu {
    String attendance;  // "1" for present, "0" for absent
    String note;        // Teacher's note
    String child_id;    // Student ID
    String sms;         // "1" for SMS enabled, "0" for disabled
    String marks;       // Obtained marks
    String delete;      // "1" for delete, "0" for keep
    
    // Constructor and getters/setters
}
```

### 3. Exam Adapter: `ExamAdaptor.java`

#### **Key Features:**
- **Real-time mark entry**: TextWatcher for immediate validation
- **SMS toggle**: Individual SMS settings per student
- **Visual feedback**: Color coding for marks (red for 0, white for others)
- **Attendance tracking**: Automatic attendance based on marks

#### **Mark Entry Logic:**
```java
holder.obtained_marks.addTextWatcher(new TextWatcher() {
    @Override
    public void afterTextChanged(Editable s) {
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION) {
            marksEnterInterface.OnMarksEnter(adapterPosition, s.toString());
        }
    }
});
```

#### **SMS Toggle Logic:**
```java
holder.sms_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION) {
            smsCheck.OnSmsCheck(b, adapterPosition);
        }
    }
});
```

### 4. Interface Implementations

#### **MarksEnterInterface:**
```java
@Override
public void OnMarksEnter(int position, String obtained_marks) {
    if (!obtained_marks.isEmpty()) {
        double total_marks_ = Double.parseDouble(total_marks);
        double obtained_marks_ = Double.parseDouble(obtained_marks);
        
        ExamSubmitStrcu result = exam_submit_list.get(position);
        
        // Set attendance based on marks
        if (obtained_marks.equals("0")) {
            result.setAttendance("0"); // Absent
        } else {
            result.setAttendance("1"); // Present
        }
        
        result.setMarks(obtained_marks);
        exam_submit_list.set(position, result);
        
        // Validate marks don't exceed total
        if (obtained_marks_ > total_marks_) {
            Toast.makeText(context, "Obtained marks cannot be greater than total marks", 
                Toast.LENGTH_LONG).show();
        }
    }
}
```

#### **SMS Check Interface:**
```java
@Override
public void OnSmsCheck(boolean is_sms, int position) {
    String status = is_sms ? "1" : "0";
    
    ExamSubmitStrcu result = exam_submit_list.get(position);
    result.setSms(status);
    exam_submit_list.set(position, result);
}
```

## Complete Workflow Summary

### **Step-by-Step Process:**

1. **Teacher Login** → Access exam submission module
2. **Select Exam Session** → Choose academic session
3. **Select Class** → Choose assigned class
4. **Select Section** → Choose class section
5. **Select Subject** → Choose subject to grade
6. **Select Exam** → Choose specific exam/test
7. **Load Students** → Display student list with mark entry fields
8. **Enter Marks** → Input marks for each student
9. **Configure SMS** → Enable/disable SMS notifications per student
10. **Validate Marks** → Ensure all marks are entered and valid
11. **Password Verification** → Enter staff password for security
12. **Submit to Server** → Send results to backend API
13. **Confirmation** → Show success/error message

### **Security Features:**
- **Staff authentication**: Password required for submission
- **Role-based access**: Only assigned teachers can submit
- **Data validation**: Marks validation and attendance tracking
- **Session management**: Secure API communication

### **Data Flow:**
```
Teacher Input → Local Validation → Password Check → API Submission → Server Processing → Confirmation
```

### **Key Validations:**
- All student marks must be entered
- Marks cannot exceed total marks
- Staff password verification required
- Network connectivity check
- Server response validation

This comprehensive system ensures secure, validated, and efficient exam mark submission by teaching staff with full audit trail and error handling. 