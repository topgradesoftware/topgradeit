package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;public class SessionModel {


    @SerializedName("status")
    @Expose
    private SharedStatus status;
    @SerializedName("exam_session")
    @Expose
    private List<ExamSession> examSession = null;


    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<ExamSession> getExamSession() {
        return examSession;
    }

    public void setExamSession(List<ExamSession> examSession) {
        this.examSession = examSession;
    }
}
