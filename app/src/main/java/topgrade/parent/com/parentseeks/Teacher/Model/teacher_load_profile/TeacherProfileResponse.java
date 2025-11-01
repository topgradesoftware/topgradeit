package topgrade.parent.com.parentseeks.Teacher.Model.teacher_load_profile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class TeacherProfileResponse {
    @SerializedName("status")
    @Expose
    private SharedStatus status;
    @SerializedName("data")
    @Expose
    private TeacherProfileData data;
    @SerializedName("campus")
    @Expose
    private Campus campus;
    @SerializedName("teach")
    @Expose
    private List<TeachProfileTeacher> teach;
    @SerializedName("section")
    @Expose
    private List<Section> section;
    private final static long serialVersionUID = 1658721632617682873L;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public TeacherProfileData getData() {
        return data;
    }

    public void setData(TeacherProfileData data) {
        this.data = data;
    }

    public Campus getCampus() {
        return campus;
    }

    public void setCampus(Campus campus) {
        this.campus = campus;
    }

    public List<TeachProfileTeacher> getTeach() {
        return teach;
    }

    public void setTeach(List<TeachProfileTeacher> teach) {
        this.teach = teach;
    }

    public List<Section> getSection() {
        return section;
    }

    public void setSection(List<Section> section) {
        this.section = section;
    }

}
