package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ExamTest implements Serializable {
    
    private static final long serialVersionUID = 1L;


    @SerializedName("unique_id")
    @Expose
    private String uniqueId;
    @SerializedName("total_marks")
    @Expose
    private Integer totalMarks;

    @SerializedName("full_name")
    @Expose
    private String fullName;

    @SerializedName("student_class_id")
    @Expose
    private String studentClassId;

    public ExamTest(String uniqueId, Integer totalMarks, String fullName) {
        this.uniqueId = uniqueId;
        this.fullName = fullName;
        this.totalMarks = totalMarks;
    }


    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }


    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(String studentClassId) {
        this.studentClassId = studentClassId;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
