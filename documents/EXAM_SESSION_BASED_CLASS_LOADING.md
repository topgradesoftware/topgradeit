# Exam Session Based Class Loading

## Problem Statement
Currently, the system loads all classes assigned to the teacher regardless of whether they have tests/exams created for the selected exam session. We need to modify this to only show classes that actually have tests in the selected exam session.

## Current Logic Issues

### Current Flow:
1. Select Exam Session
2. Load ALL teacher's classes (regardless of exam session)
3. Select Class
4. Select Section
5. Select Subject
6. Load Exams (only then we find if tests exist)

### Problem:
- Classes without tests in the selected session are still shown
- User wastes time selecting classes that have no tests
- Poor user experience

## Solution: Modified Class Loading Logic

### New Flow:
1. Select Exam Session
2. Load classes that have tests in the selected session
3. Select Class (only from filtered list)
4. Select Section
5. Select Subject
6. Load Exams

## Implementation

### 1. Modify `Load_Class()` Method

```java
private void Load_Class() {
    // First, get all exams for the selected session
    loadExamsForSession();
}

private void loadExamsForSession() {
    HashMap<String, String> postParam = new HashMap<String, String>();
    postParam.put("staff_id", Constant.staff_id);
    postParam.put("parent_id", Constant.campus_id);
    postParam.put("exam_session_id", session_id);
    // Don't specify class_id, subject_id, or section_id to get ALL exams for session
    
    progress_bar.setVisibility(View.VISIBLE);
    RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), 
        MediaType.parse("application/json; charset=utf-8"));
    
    Constant.mApiService.load_exams(body).enqueue(new Callback<ExamTestRespone>() {
        @Override
        public void onResponse(Call<ExamTestRespone> call, Response<ExamTestRespone> response) {
            if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                final List<TestModel> allExamsForSession = response.body().getExams();
                
                // Extract unique classes that have tests in this session
                Set<String> classesWithTests = new HashSet<>();
                for (TestModel exam : allExamsForSession) {
                    classesWithTests.add(exam.getStudentClassId());
                }
                
                // Now load teacher's profile and filter classes
                loadTeacherProfileAndFilterClasses(classesWithTests);
            }
        }
        
        @Override
        public void onFailure(Call<ExamTestRespone> call, Throwable e) {
            e.printStackTrace();
            progress_bar.setVisibility(View.GONE);
            Toast.makeText(context, "Failed to load exams for session", Toast.LENGTH_SHORT).show();
        }
    });
}
```

### 2. Add New Method to Load Teacher Profile and Filter Classes

```java
private void loadTeacherProfileAndFilterClasses(Set<String> classesWithTests) {
    HashMap<String, String> postParam = new HashMap<String, String>();
    postParam.put("staff_id", Constant.staff_id);
    postParam.put("parent_id", Constant.campus_id);
    postParam.put("session_id", Constant.current_session);
    
    RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), 
        MediaType.parse("application/json; charset=utf-8"));
    
    Constant.mApiService.load_profile(body).enqueue(new Callback<TeachModel>() {
        @Override
        public void onResponse(Call<TeachModel> call, Response<TeachModel> response) {
            if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                final List<Teach> allTeacherClasses = response.body().getTeach();
                
                // Filter classes that have tests in the selected session
                List<Teach> filteredClasses = new ArrayList<>();
                classForSubjectList.clear();
                
                for (Teach teachClass : allTeacherClasses) {
                    if (classesWithTests.contains(teachClass.getStudentClassId())) {
                        filteredClasses.add(teachClass);
                        if (!classForSubjectList.contains(teachClass.getClassName())) {
                            classForSubjectList.add(teachClass.getClassName());
                        }
                    }
                }
                
                if (classForSubjectList.isEmpty()) {
                    Toast.makeText(context, "No classes have tests in the selected exam session", Toast.LENGTH_LONG).show();
                    progress_bar.setVisibility(View.GONE);
                    return;
                }
                
                // Setup class spinner with filtered classes
                ArrayAdapter student_class_adaptor = new ArrayAdapter<String>(context, 
                    R.layout.simple_list_item_1, classForSubjectList);
                classSpinner.setAdapter(student_class_adaptor);
                
                // Store filtered classes for later use
                final List<Teach> finalFilteredClasses = filteredClasses;
                
                classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // Find the selected class from filtered list
                        String selectedClassName = classForSubjectList.get(position);
                        for (Teach teachClass : finalFilteredClasses) {
                            if (teachClass.getClassName().equals(selectedClassName)) {
                                class_id = teachClass.getStudentClassId();
                                break;
                            }
                        }
                        
                        // Setup sections and subjects for selected class
                        setupSectionAndSubjectList(finalFilteredClasses);
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
        }
        
        @Override
        public void onFailure(Call<TeachModel> call, Throwable e) {
            e.printStackTrace();
            progress_bar.setVisibility(View.GONE);
            Toast.makeText(context, "Failed to load teacher profile", Toast.LENGTH_SHORT).show();
        }
    });
}
```

### 3. Modify `setupSectionAndSubjectList()` Method

```java
private void setupSectionAndSubjectList(List<Teach> filteredClasses) {
    // Setup Section List
    ArrayList<String> sectionNames = new ArrayList<>();
    ArrayList<SectionTest> filteredSections = new ArrayList<>();
    
    for (Teach item : filteredClasses) {
        if (item.getStudentClassId().equals(class_id)) {
            if (!sectionNames.contains(item.getSectionName())) {
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
    
    for (Teach item : filteredClasses) {
        if (item.getStudentClassId().equals(class_id)) {
            if (!subjectNames.contains(item.getSubjectName())) {
                subjectNames.add(item.getSubjectName());
                subjectList.add(new SubjectTest(
                    item.getSubjectId(),
                    item.getSubjectName()
                ));
            }
        }
    }
    
    setupSectionAdapter(filteredSections, (ArrayList<Teach>) filteredClasses);
}
```

### 4. Update `loadExams()` Method

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
                
                for (TestModel item : testList) {
                    if (item.getSubjectId().equals(subject_id)) {
                        if (!examNames.contains(item.getFullName())) {
                            examNames.add(item.getFullName());
                            examList.add(new ExamTest(
                                item.getUniqueId(),
                                item.getTotalMarks(),
                                item.getFullName()
                            ));
                        }
                    }
                }
                
                if (examList.isEmpty()) {
                    Toast.makeText(context, "No exams found for selected subject in this session", Toast.LENGTH_LONG).show();
                }
                
                setupExamAdapter(testList);
                progress_bar.setVisibility(View.GONE);
            } else {
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        
        @Override
        public void onFailure(Call<ExamTestRespone> call, Throwable e) {
            e.printStackTrace();
            progress_bar.setVisibility(View.GONE);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

## API Changes Required

### 1. Modify `load_exams` API
The API should support loading all exams for a session without requiring class_id, subject_id, or section_id:

```java
// In BaseApiService.java
@Headers("Content-Type:application/json")
@POST("api.php?page=teacher/load_exams")
Call<ExamTestRespone> load_exams(@Body RequestBody body);
```

### 2. Backend API Logic
The backend should handle the case where class_id, subject_id, or section_id are not provided and return all exams for the session.

## Benefits of This Approach

1. **Better User Experience**: Only shows relevant classes
2. **Reduced Confusion**: No empty class selections
3. **Performance**: Fewer API calls and UI updates
4. **Data Integrity**: Ensures selected classes have tests
5. **Clear Feedback**: Shows message when no classes have tests

## Error Handling

```java
// Add these error scenarios
if (classForSubjectList.isEmpty()) {
    Toast.makeText(context, "No classes have tests in the selected exam session", Toast.LENGTH_LONG).show();
    // Disable class spinner
    classSpinner.setEnabled(false);
    return;
}

if (examList.isEmpty()) {
    Toast.makeText(context, "No exams found for selected subject in this session", Toast.LENGTH_LONG).show();
    // Disable exam spinner
    examSpinner.setEnabled(false);
    return;
}
```

## Summary

This modification ensures that:
1. Only classes with tests in the selected exam session are shown
2. Better user experience with filtered options
3. Clear feedback when no tests exist
4. Maintains all existing functionality while improving efficiency

The key change is loading exams first to determine which classes have tests, then filtering the teacher's class list accordingly. 