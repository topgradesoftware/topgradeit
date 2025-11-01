package topgrade.parent.com.parentseeks.Teacher.Model.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Teacher.Model.ExamResult;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class ExamResultModel {


    @SerializedName("status")
    @Expose
    private SharedStatus status;


    @SerializedName("result")
    @Expose
    private List<ExamResult> result = null;


    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<ExamResult> getResult() {
        return result;
    }

    public void setResult(List<ExamResult> result) {
        this.result = result;
    }
}
