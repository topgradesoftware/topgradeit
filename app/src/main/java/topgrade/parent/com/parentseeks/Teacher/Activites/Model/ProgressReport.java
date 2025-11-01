package topgrade.parent.com.parentseeks.Teacher.Activites.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProgressReport {

    @SerializedName("subject_id")
    @Expose
    private String subjectId;
    @SerializedName("display_order_class")
    @Expose
    private Integer displayOrderClass;
    @SerializedName("display_order_subject")
    @Expose
    private Integer displayOrderSubject;
    @SerializedName("subject_name")
    @Expose
    private String subjectName;
    @SerializedName("class_name")
    @Expose
    private String className;
    @SerializedName("student_class_id")
    @Expose
    private String studentClassId;
    @SerializedName("NoOfTest")
    @Expose
    private Integer noOfTest;
    @SerializedName("Students")
    @Expose
    private Integer students;
    @SerializedName("StudentsAppeared")
    @Expose
    private Integer studentsAppeared;
    @SerializedName("90percent")
    @Expose
    private Integer _90percent;
    @SerializedName("80percent")
    @Expose
    private Integer _80percent;
    @SerializedName("70percent")
    @Expose
    private Integer _70percent;
    @SerializedName("60percent")
    @Expose
    private Integer _60percent;
    @SerializedName("50percent")
    @Expose
    private Integer _50percent;
    @SerializedName("33percent")
    @Expose
    private Integer _33percent;
    @SerializedName("25percent")
    @Expose
    private Integer _25percent;
    @SerializedName("passed")
    @Expose
    private Integer passed;
    @SerializedName("failed")
    @Expose
    private Integer failed;
    @SerializedName("passedpercent")
    @Expose
    private Integer passedpercent;
    @SerializedName("failedpercent")
    @Expose
    private Integer failedpercent;


    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getDisplayOrderClass() {
        return displayOrderClass;
    }

    public void setDisplayOrderClass(Integer displayOrderClass) {
        this.displayOrderClass = displayOrderClass;
    }

    public Integer getDisplayOrderSubject() {
        return displayOrderSubject;
    }

    public void setDisplayOrderSubject(Integer displayOrderSubject) {
        this.displayOrderSubject = displayOrderSubject;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public Integer getNoOfTest() {
        return noOfTest;
    }

    public void setNoOfTest(Integer noOfTest) {
        this.noOfTest = noOfTest;
    }

    public Integer getStudents() {
        return students;
    }

    public void setStudents(Integer students) {
        this.students = students;
    }

    public Integer getStudentsAppeared() {
        return studentsAppeared;
    }

    public void setStudentsAppeared(Integer studentsAppeared) {
        this.studentsAppeared = studentsAppeared;
    }

    public Integer get_90percent() {
        return _90percent;
    }

    public void set_90percent(Integer _90percent) {
        this._90percent = _90percent;
    }

    public Integer get_80percent() {
        return _80percent;
    }

    public void set_80percent(Integer _80percent) {
        this._80percent = _80percent;
    }

    public Integer get_70percent() {
        return _70percent;
    }

    public void set_70percent(Integer _70percent) {
        this._70percent = _70percent;
    }

    public Integer get_60percent() {
        return _60percent;
    }

    public void set_60percent(Integer _60percent) {
        this._60percent = _60percent;
    }

    public Integer get_50percent() {
        return _50percent;
    }

    public void set_50percent(Integer _50percent) {
        this._50percent = _50percent;
    }

    public Integer get_33percent() {
        return _33percent;
    }

    public void set_33percent(Integer _33percent) {
        this._33percent = _33percent;
    }

    public Integer get_25percent() {
        return _25percent;
    }

    public void set_25percent(Integer _25percent) {
        this._25percent = _25percent;
    }

    public Integer getPassed() {
        return passed;
    }

    public void setPassed(Integer passed) {
        this.passed = passed;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    public Integer getPassedpercent() {
        return passedpercent;
    }

    public void setPassedpercent(Integer passedpercent) {
        this.passedpercent = passedpercent;
    }

    public Integer getFailedpercent() {
        return failedpercent;
    }

    public void setFailedpercent(Integer failedpercent) {
        this.failedpercent = failedpercent;
    }
}
