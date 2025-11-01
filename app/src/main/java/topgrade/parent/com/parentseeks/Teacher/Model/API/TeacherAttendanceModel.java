package topgrade.parent.com.parentseeks.Teacher.Model.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Teacher-specific attendance model for API responses
 * Renamed from ParentAttendanceModel to avoid conflicts with Parent module
 */
public class TeacherAttendanceModel {

    @SerializedName("unique_id")
    @Expose
    private String uniqueId;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("parent_parent_id")
    @Expose
    private String parentParentId;
    @SerializedName("parent")
    @Expose
    private String parent;
    @SerializedName("note")
    @Expose
    private String note;
    @SerializedName("attendance")
    @Expose
    private Integer attendance;
    @SerializedName("is_active")
    @Expose
    private Integer isActive;
    @SerializedName("roll_no")
    @Expose
    private Integer roll_no;
    @SerializedName("picture")
    @Expose
    private Object picture;
    @SerializedName("employee_id")
    @Expose
    private String employeeId;
    @SerializedName("student_class_id")
    @Expose
    private String studentClassId;
    @SerializedName("section_id")
    @Expose
    private String sectionId;
    @SerializedName("created_date")
    @Expose
    private String createdDate;
    @SerializedName("subject_id")
    @Expose
    private String subjectId;
    @SerializedName("subject")
    @Expose
    private String subject;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("classorder")
    @Expose
    private Integer classorder;
    @SerializedName("subjectorder")
    @Expose
    private Integer subjectorder;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getParentParentId() {
        return parentParentId;
    }

    public void setParentParentId(String parentParentId) {
        this.parentParentId = parentParentId;
    }

    public String getParent() {
        return parent;
    }

    public Integer getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(Integer roll_no) {
        this.roll_no = roll_no;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getAttendance() {
        return attendance;
    }

    public void setAttendance(Integer attendance) {
        this.attendance = attendance;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Object getPicture() {
        return picture;
    }

    public void setPicture(Object picture) {
        this.picture = picture;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(String studentClassId) {
        this.studentClassId = studentClassId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getClassorder() {
        return classorder;
    }

    public void setClassorder(Integer classorder) {
        this.classorder = classorder;
    }

    public Integer getSubjectorder() {
        return subjectorder;
    }

    public void setSubjectorder(Integer subjectorder) {
        this.subjectorder = subjectorder;
    }
}
