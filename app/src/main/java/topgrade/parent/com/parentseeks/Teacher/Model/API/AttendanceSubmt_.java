package topgrade.parent.com.parentseeks.Teacher.Model.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class AttendanceSubmt_ {


    @SerializedName("status")
    @Expose
    private SharedStatus status;


    @SerializedName("attendence")
    @Expose
    private List<TeacherAttendanceModel> attendence = null;


    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<TeacherAttendanceModel> getAttendance() {
        return attendence;
    }

    public void setAttendance(List<TeacherAttendanceModel> attendence) {
        this.attendence = attendence;
    }
}
