package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Teacher.Model.teacher_load_profile.TeacherProfileData;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class TeachModel {
    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("data")
    @Expose
    private TeacherProfileData data;
    @SerializedName("teach")
    @Expose
    private List<Teach> teach = null;

    @SerializedName("section")
    @Expose
    private List<TeachSection> teachSection;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<Teach> getTeach() {
        return teach;
    }

    public List<TeachSection> getTeachSection() {
        return teachSection;
    }


    public void setTeach(List<Teach> teach) {
        this.teach = teach;
    }
    public void setTeachSection(List<TeachSection> teach) {
        this.teachSection = getTeachSection();
    }


    public TeacherProfileData getData() {
        return data;
    }

    public void setData(TeacherProfileData data) {
        this.data = data;
    }
}
