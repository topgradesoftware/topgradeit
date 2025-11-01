package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class StudentListModel {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("students")
    @Expose
    private List<StudentListSigel> students = null;


    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<StudentListSigel> getStudents() {
        return students;
    }

    public void setStudents(List<StudentListSigel> students) {
        this.students = students;
    }
}
