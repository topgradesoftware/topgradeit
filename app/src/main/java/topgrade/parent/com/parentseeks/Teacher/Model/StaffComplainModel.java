package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class StaffComplainModel {
    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("data")
    @Expose
    private List<ComplaintModel.Complaint> data = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<ComplaintModel.Complaint> getData() {
        return data;
    }

    public void setData(List<ComplaintModel.Complaint> data) {
        this.data = data;
    }
}
