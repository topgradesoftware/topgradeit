package topgrade.parent.com.parentseeks.Teacher.Exam.Components;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
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
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Exam.Models.ExamState;
import topgrade.parent.com.parentseeks.Teacher.Exam.Models.ExamSubmissionData;
import topgrade.parent.com.parentseeks.Teacher.Model.API.ExamResultModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class ExamSubmissionManager {

    private static final String TAG = "ExamSubmissionManager";
    private Context context;
    private Gson gson;
    private android.app.AlertDialog passwordDialog;

    public interface PasswordCallback {
        void onPasswordVerified();
        void onPasswordError(String message);
    }

    public interface SubmissionCallback {
        void onSuccess();
        void onError(String message);
    }

    public ExamSubmissionManager(Context context) {
        this.context = context;
        this.gson = new Gson();
        // Load constants from PaperDB to ensure we have login credentials
        Constant.loadFromPaper();
    }

    public void showPasswordDialog(PasswordCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.enter_pwd, null);
        builder.setView(dialogView);

        passwordDialog = builder.create();
        passwordDialog.setCancelable(true);
        passwordDialog.setCanceledOnTouchOutside(true);
        passwordDialog.show();

        EditText passwordInput = passwordDialog.findViewById(R.id.staff_pwd);
        Button submitButton = passwordDialog.findViewById(R.id.Submit_Marks_);
        ImageView cancelButton = passwordDialog.findViewById(R.id.Cancel);
        Button cancelButton2 = passwordDialog.findViewById(R.id.Cancel_);

        if (submitButton != null) {
            submitButton.setOnClickListener(v -> {
                String enteredPassword = passwordInput != null ? passwordInput.getText().toString() : "";
                verifyPassword(enteredPassword, callback);
            });
        }

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dismissPasswordDialog());
        }

        if (cancelButton2 != null) {
            cancelButton2.setOnClickListener(v -> dismissPasswordDialog());
        }
    }

    private void verifyPassword(String enteredPassword, PasswordCallback callback) {
        if (enteredPassword == null || enteredPassword.isEmpty()) {
            callback.onPasswordError("Please enter your password");
            return;
        }

        String storedPassword = Paper.book().read("staff_password", "");
        if (storedPassword == null || storedPassword.isEmpty()) {
            callback.onPasswordError("No stored password found");
            return;
        }

        if (!enteredPassword.equals(storedPassword)) {
            callback.onPasswordError("Incorrect password");
            return;
        }

        dismissPasswordDialog();
        callback.onPasswordVerified();
    }

    private void dismissPasswordDialog() {
        if (passwordDialog != null && passwordDialog.isShowing()) {
            passwordDialog.dismiss();
        }
    }

    public void submitMarks(ExamState state, SubmissionCallback callback) {
        if (state == null || state.getExamSubmissionData() == null) {
            callback.onError("No exam data to submit");
            return;
        }

        try {
            JSONArray jsonArray = convertToJsonArray(state.getExamSubmissionData());
            HashMap<String, Object> postParam = new HashMap<>();
            postParam.put("staff_id", Constant.staff_id);           // Staff ID from login
            postParam.put("parent_id", Constant.campus_id);         // API param 'parent_id' = campus_id
            postParam.put("student_class_id", state.getClassId());
            postParam.put("subject_id", state.getSubjectId());
            postParam.put("section_id", state.getSectionId());
            postParam.put("exam_id", state.getExamId());
            postParam.put("results", jsonArray);

            JSONObject jsonObject = new JSONObject(postParam);
            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));

            logSubmissionData(state, jsonObject);

            Constant.mApiService.save_exam_results(body).enqueue(new Callback<ExamResultModel>() {
                @Override
                public void onResponse(Call<ExamResultModel> call, Response<ExamResultModel> response) {
                    if (response.body() != null && "1000".equals(response.body().getStatus().getCode())) {
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onSuccess();
                    } else {
                        callback.onError(response.body() != null ? response.body().getStatus().getMessage() : "Unknown error");
                    }
                }

                @Override
                public void onFailure(Call<ExamResultModel> call, Throwable t) {
                    callback.onError("Network error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onError("Error preparing submission: " + e.getMessage());
        }
    }

    private JSONArray convertToJsonArray(List<ExamSubmissionData> students) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (ExamSubmissionData student : students) {
            JSONObject studentData = new JSONObject();
            studentData.put("student_id", student.getStudentId());
            studentData.put("obtained_marks", student.getObtainedMarks());
            studentData.put("total_marks", student.getTotalMarks());
            studentData.put("note", student.getNote() != null ? student.getNote() : "");
            studentData.put("sms_enabled", student.isSmsEnabled() ? "1" : "0");
            studentData.put("parent_id", student.getParentId());
            studentData.put("section_id", student.getSectionId());
            jsonArray.put(studentData);
        }

        return jsonArray;
    }

    private void logSubmissionData(ExamState state, JSONObject jsonObject) {
        Log.d(TAG, "Submitting exam data:");
        Log.d(TAG, "Class ID: " + state.getClassId());
        Log.d(TAG, "Subject ID: " + state.getSubjectId());
        Log.d(TAG, "Section ID: " + state.getSectionId());
        Log.d(TAG, "Exam ID: " + state.getExamId());
        Log.d(TAG, "Student count: " + (state.getExamSubmissionData() != null ? state.getExamSubmissionData().size() : 0));
        Log.d(TAG, "JSON data: " + jsonObject.toString());
    }

    public boolean validateSubmissionData(ExamState state) {
        if (state == null || state.getExamSubmissionData() == null || state.getExamSubmissionData().isEmpty()) {
            Log.e(TAG, "Invalid exam state or empty submission list");
            return false;
        }

        for (ExamSubmissionData student : state.getExamSubmissionData()) {
            if (!student.hasMarks()) {
                Log.e(TAG, "Student " + student.getStudentName() + " has no marks");
                return false;
            }
            if (!student.isValidMarks()) {
                Log.e(TAG, "Student " + student.getStudentName() + " has invalid marks");
                return false;
            }
        }
        return true;
    }

    public String getSubmissionSummary(ExamState state) {
        if (state == null || state.getExamSubmissionData() == null) return "No data to summarize";

        int total = state.getExamSubmissionData().size();
        int withMarks = 0, validMarks = 0;

        for (ExamSubmissionData s : state.getExamSubmissionData()) {
            if (s.hasMarks()) withMarks++;
            if (s.isValidMarks()) validMarks++;
        }

        return String.format("Total: %d, With Marks: %d, Valid Marks: %d", total, withMarks, validMarks);
    }

    public void cleanup() {
        dismissPasswordDialog();
    }
}
