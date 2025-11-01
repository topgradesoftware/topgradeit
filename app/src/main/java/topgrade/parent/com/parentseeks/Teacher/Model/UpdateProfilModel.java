package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;public class UpdateProfilModel {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("staff")
    @Expose
    private List<StaffModel> staff = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<StaffModel> getStaff() {
        return staff;
    }

    public void setStaff(List<StaffModel> staff) {
        this.staff = staff;
    }
}
