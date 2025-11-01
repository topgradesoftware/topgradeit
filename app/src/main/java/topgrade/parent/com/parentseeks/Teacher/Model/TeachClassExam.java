package topgrade.parent.com.parentseeks.Teacher.Model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TeachClassExam {

    @SerializedName("display_order_class")
    @Expose
    private Integer displayOrderClass;
    @SerializedName("class_name")
    @Expose
    private String className;
    @SerializedName("student_class_id")
    @Expose
    private String studentClassId;

    @SerializedName("is_class_incharge")
    @Expose
    private String isClassInCharge;

    public TeachClassExam(Integer displayOrderClass, String className, String studentClassId, String isClassInCharge) {
        this.displayOrderClass = displayOrderClass;
        this.className = className;
        this.studentClassId = studentClassId;
        this.isClassInCharge = isClassInCharge;
    }

    public Integer getDisplayOrderClass() {
        return displayOrderClass;
    }

    public void setDisplayOrderClass(Integer displayOrderClass) {
        this.displayOrderClass = displayOrderClass;
    }

    public String getIsClassInCharge() {
        return isClassInCharge;
    }

    public void setIsClassInCharge(String isClassInCharge) {
        this.isClassInCharge = isClassInCharge;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(String studentClassId) {
        this.studentClassId = studentClassId;
    }

    @NonNull
    @Override
    public String toString() {
        return className;
    }
}

