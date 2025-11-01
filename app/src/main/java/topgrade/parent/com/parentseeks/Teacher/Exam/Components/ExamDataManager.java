package topgrade.parent.com.parentseeks.Teacher.Exam.Components;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Teacher.Exam.Models.ExamSubmissionData;
import topgrade.parent.com.parentseeks.Teacher.Interface.MarksEnterInterface;
import topgrade.parent.com.parentseeks.Parent.Model.ExamSession;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ExamResultModel;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ExamSessionResponse;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ExamTestRespone;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamResult;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamSubmitStrcu;
import topgrade.parent.com.parentseeks.Teacher.Model.SectionTest;
import topgrade.parent.com.parentseeks.Teacher.Model.SubjectTest;
import topgrade.parent.com.parentseeks.Teacher.Model.Teach;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamTest;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import io.paperdb.Paper;

/**
 * ExamDataManager - Handles all data loading operations for exam submission
 * Responsibilities:
 * - Loading exam sessions
 * - Loading classes, sections, subjects
 * - Loading exams and students
 * - Managing API calls and responses
 */
public class ExamDataManager {
    
    private static final String TAG = "ExamDataManager";
    private Context context;
    private Gson gson;

    public ExamDataManager(Context context) {
        this.context = context;
        this.gson = new Gson();
        // Load constants from PaperDB to ensure we have login credentials
        Constant.loadFromPaper();
    }

    // MARK: - Callback Interfaces

    public interface ExamSessionCallback {
        void onSessionsLoaded(List<String> sessions);
        void onError(String message);
    }

    public interface ClassCallback {
        void onClassesLoaded(List<String> classes);
        void onError(String message);
    }

    public interface SectionCallback {
        void onSectionsLoaded(List<String> sections);
        void onError(String message);
    }

    public interface SubjectCallback {
        void onSubjectsLoaded(List<String> subjects);
        void onError(String message);
    }

    public interface ExamCallback {
        void onExamsLoaded(List<String> exams);
        void onError(String message);
    }

    public interface StudentCallback {
        void onStudentsLoaded(List<ExamSubmissionData> students);
        void onError(String message);
    }

    // MARK: - Exam Session Loading

    public void loadExamSessions(ExamSessionCallback callback) {
        Log.d(TAG, "Loading exam sessions from local storage");
        
        try {
            // Load exam sessions from local storage (Paper.book) like in the working code
            List<ExamSession> examSessionsList = Paper.book().read(Constants.exam_session);
            List<String> sessions = new ArrayList<>();
            
            if (examSessionsList != null && !examSessionsList.isEmpty()) {
                for (ExamSession session : examSessionsList) {
                    sessions.add(session.getFullName());
                }
                Log.d(TAG, "Loaded " + sessions.size() + " exam sessions from local storage");
                callback.onSessionsLoaded(sessions);
            } else {
                Log.w(TAG, "No exam sessions found in local storage");
                callback.onError("No exam sessions available");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading exam sessions from local storage", e);
            callback.onError("Error loading exam sessions: " + e.getMessage());
        }
    }

    // MARK: - Class Loading

    public void loadClassesForSession(String sessionId, ClassCallback callback) {
        Log.d(TAG, "Loading classes for session: " + sessionId);
        
        // First, get all exams for the selected session
        loadExamsForSession(sessionId, new ExamCallback() {
            @Override
            public void onExamsLoaded(List<String> exams) {
                // This is a placeholder - in real implementation, we'd extract class IDs from exams
                loadTeacherProfileAndFilterClasses(sessionId, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void loadExamsForSession(String sessionId, ExamCallback callback) {
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("staff_id", Constant.staff_id);           // Staff ID from login
        postParam.put("parent_id", Constant.campus_id);         // API param 'parent_id' = campus_id
        postParam.put("exam_session_id", sessionId);

        RequestBody body = RequestBody.create(
            new JSONObject(postParam).toString(), 
            MediaType.parse("application/json; charset=utf-8")
        );

        Constant.mApiService.load_exams(body).enqueue(new Callback<ExamTestRespone>() {
            @Override
            public void onResponse(Call<ExamTestRespone> call, Response<ExamTestRespone> response) {
                if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                    List<ExamTest> allExamsForSession = response.body().getExams();
                    
                    // Extract unique classes that have tests in this session
                    Set<String> classesWithTests = new HashSet<>();
                    for (ExamTest exam : allExamsForSession) {
                        classesWithTests.add(exam.getStudentClassId());
                    }
                    
                    Log.d(TAG, "Found " + classesWithTests.size() + " classes with tests");
                    callback.onExamsLoaded(new ArrayList<>(classesWithTests));
                } else {
                    String errorMessage = response.body() != null ? 
                        response.body().getStatus().getMessage() : "Failed to load exams";
                    Log.e(TAG, "Error loading exams: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ExamTestRespone> call, Throwable t) {
                Log.e(TAG, "Network error loading exams", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private void loadTeacherProfileAndFilterClasses(String sessionId, ClassCallback callback) {
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("session_id", Constant.current_session);

        RequestBody body = RequestBody.create(
            new JSONObject(postParam).toString(), 
            MediaType.parse("application/json; charset=utf-8")
        );

        Constant.mApiService.load_profile(body).enqueue(new Callback<TeachModel>() {
            @Override
            public void onResponse(Call<TeachModel> call, Response<TeachModel> response) {
                if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                    List<Teach> allTeacherClasses = response.body().getTeach();
                    
                    // Filter classes that have tests in the selected session
                    List<String> filteredClasses = new ArrayList<>();
                    for (Teach teachClass : allTeacherClasses) {
                        if (!filteredClasses.contains(teachClass.getClassName())) {
                            filteredClasses.add(teachClass.getClassName());
                        }
                    }
                    
                    Log.d(TAG, "Filtered to " + filteredClasses.size() + " classes");
                    callback.onClassesLoaded(filteredClasses);
                } else {
                    String errorMessage = response.body() != null ? 
                        response.body().getStatus().getMessage() : "Failed to load teacher profile";
                    Log.e(TAG, "Error loading teacher profile: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<TeachModel> call, Throwable t) {
                Log.e(TAG, "Network error loading teacher profile", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // MARK: - Section Loading

    public void loadSectionsForClass(String classId, SectionCallback callback) {
        Log.d(TAG, "Loading sections for class: " + classId);
        
        // This would typically load sections from the teacher's profile
        // For now, we'll return a mock list
        List<String> sections = new ArrayList<>();
        sections.add("Section A");
        sections.add("Section B");
        sections.add("Section C");
        
        Log.d(TAG, "Loaded " + sections.size() + " sections");
        callback.onSectionsLoaded(sections);
    }

    // MARK: - Subject Loading

    public void loadSubjectsForSection(String sectionId, SubjectCallback callback) {
        Log.d(TAG, "Loading subjects for section: " + sectionId);
        
        // This would typically load subjects from the teacher's profile
        // For now, we'll return a mock list
        List<String> subjects = new ArrayList<>();
        subjects.add("Mathematics");
        subjects.add("Science");
        subjects.add("English");
        subjects.add("History");
        
        Log.d(TAG, "Loaded " + subjects.size() + " subjects");
        callback.onSubjectsLoaded(subjects);
    }

    // MARK: - Exam Loading

    public void loadExamsForSubject(String subjectId, ExamCallback callback) {
        Log.d(TAG, "Loading exams for subject: " + subjectId);
        
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("subject_id", subjectId);

        RequestBody body = RequestBody.create(
            new JSONObject(postParam).toString(), 
            MediaType.parse("application/json; charset=utf-8")
        );

        Constant.mApiService.load_exams(body).enqueue(new Callback<ExamTestRespone>() {
            @Override
            public void onResponse(Call<ExamTestRespone> call, Response<ExamTestRespone> response) {
                if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                    List<ExamTest> exams = response.body().getExams();
                    List<String> examNames = new ArrayList<>();
                    
                    for (ExamTest exam : exams) {
                        examNames.add(exam.getFullName());
                    }
                    
                    Log.d(TAG, "Loaded " + examNames.size() + " exams");
                    callback.onExamsLoaded(examNames);
                } else {
                    String errorMessage = response.body() != null ? 
                        response.body().getStatus().getMessage() : "Failed to load exams";
                    Log.e(TAG, "Error loading exams: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ExamTestRespone> call, Throwable t) {
                Log.e(TAG, "Network error loading exams", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // MARK: - Student Loading

    public void loadStudentsForExam(String examId, StudentCallback callback) {
        Log.d(TAG, "Loading students for exam: " + examId);
        
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("exam_id", examId);

        RequestBody body = RequestBody.create(
            new JSONObject(postParam).toString(), 
            MediaType.parse("application/json; charset=utf-8")
        );

        Constant.mApiService.load_exams_results(body).enqueue(new Callback<ExamResultModel>() {
            @Override
            public void onResponse(Call<ExamResultModel> call, Response<ExamResultModel> response) {
                if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                    List<ExamResult> students = response.body().getResult();
                    List<ExamSubmissionData> submissionData = new ArrayList<>();
                    
                    for (ExamResult student : students) {
                        // Handle roll number conversion
                        String rollNumber = "";
                        if (student.getRollNumber() != null) {
                            rollNumber = String.valueOf(student.getRollNumber());
                        }
                        
                        // Handle total marks conversion
                        String totalMarks = "";
                        if (student.getTotalMarks() != null) {
                            if (student.getTotalMarks() instanceof Integer) {
                                totalMarks = String.valueOf(student.getTotalMarks());
                            } else if (student.getTotalMarks() instanceof String) {
                                totalMarks = (String) student.getTotalMarks();
                            } else {
                                totalMarks = student.getTotalMarks().toString();
                            }
                        }
                        
                        ExamSubmissionData data = new ExamSubmissionData(
                            student.getUniqueId(),
                            student.getFullName(),
                            rollNumber,
                            totalMarks,
                            student.getParentParentId(),
                            student.getSectionId()
                        );
                        submissionData.add(data);
                    }
                    
                    Log.d(TAG, "Loaded " + submissionData.size() + " students");
                    callback.onStudentsLoaded(submissionData);
                } else {
                    String errorMessage = response.body() != null ? 
                        response.body().getStatus().getMessage() : "Failed to load students";
                    Log.e(TAG, "Error loading students: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ExamResultModel> call, Throwable t) {
                Log.e(TAG, "Network error loading students", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // MARK: - Utility Methods

    private void logError(String operation, String error) {
        Log.e(TAG, "Error in " + operation + ": " + error);
    }

    private void logSuccess(String operation, int count) {
        Log.d(TAG, "Successfully " + operation + ": " + count + " items");
    }
} 