package topgrade.parent.com.parentseeks.Teacher.Model.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Model.ExamSession;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class ExamSessionResponse {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("exam_sessions")
    @Expose
    private List<ExamSession> examSessions = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<ExamSession> getExamSessions() {
        return examSessions;
    }

    public void setExamSessions(List<ExamSession> examSessions) {
        this.examSessions = examSessions;
    }
} 