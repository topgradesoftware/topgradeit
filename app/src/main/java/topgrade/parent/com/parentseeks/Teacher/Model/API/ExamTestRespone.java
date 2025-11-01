package topgrade.parent.com.parentseeks.Teacher.Model.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamTest;

public class ExamTestRespone {

    @SerializedName("status")
    @Expose
    private SharedStatus status;


    @SerializedName("exams")
    @Expose
    private List<ExamTest> exams = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<ExamTest> getExams() {
        return exams;
    }

    public void setExams(List<ExamTest> exams) {
        this.exams = exams;
    }
}
