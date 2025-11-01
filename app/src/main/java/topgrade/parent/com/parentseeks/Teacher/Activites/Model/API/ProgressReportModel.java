package topgrade.parent.com.parentseeks.Teacher.Model.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Teacher.Activites.Model.ProgressReport;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class ProgressReportModel {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("report")
    @Expose
    private List<ProgressReport> report = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<ProgressReport> getReport() {
        return report;
    }

    public void setReport(List<ProgressReport> report) {
        this.report = report;
    }
}
