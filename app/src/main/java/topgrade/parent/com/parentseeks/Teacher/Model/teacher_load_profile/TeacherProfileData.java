package topgrade.parent.com.parentseeks.Teacher.Model.teacher_load_profile;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TeacherProfileData implements Serializable
{

    @SerializedName("unique_id")
    @Expose
    private String uniqueId;
    @SerializedName("parent_id")
    @Expose
    private String parentId;
    @SerializedName("login_id")
    @Expose
    private Integer loginId;
    @SerializedName("added_by")
    @Expose
    private String addedBy;
    @SerializedName("assigned_to")
    @Expose
    private Object assignedTo;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("fcm_token")
    @Expose
    private String fcmToken;
    @SerializedName("display_order")
    @Expose
    private Integer displayOrder;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("campus_name")
    @Expose
    private Object campusName;
    @SerializedName("landline")
    @Expose
    private String landline;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("city_id")
    @Expose
    private String cityId;
    @SerializedName("state_id")
    @Expose
    private String stateId;
    @SerializedName("student_class_id")
    @Expose
    private String studentClassId;
    @SerializedName("picture")
    @Expose
    private String picture;
    @SerializedName("dob")
    @Expose
    private String dob;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("designation")
    @Expose
    private String designation;
    @SerializedName("parent_name")
    @Expose
    private String parentName;
    @SerializedName("created_date")
    @Expose
    private String createdDate;
    @SerializedName("security")
    @Expose
    private String security;
    @SerializedName("qualification")
    @Expose
    private String qualification;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("left_date")
    @Expose
    private String leftDate;
    @SerializedName("left_note")
    @Expose
    private String leftNote;
    @SerializedName("cv")
    @Expose
    private Object cv;
    @SerializedName("cnic")
    @Expose
    private String cnic;
    @SerializedName("subject")
    @Expose
    private String subject;
    @SerializedName("salary")
    @Expose
    private Integer salary;
    @SerializedName("salary_type")
    @Expose
    private Integer salaryType;
    @SerializedName("daily_lectures")
    @Expose
    private Integer dailyLectures;
    @SerializedName("is_active")
    @Expose
    private Integer isActive;
    @SerializedName("is_in_attendance")
    @Expose
    private Integer isInAttendance;
    @SerializedName("staff_type")
    @Expose
    private Integer staffType;
    @SerializedName("is_delete")
    @Expose
    private Integer isDelete;
    @SerializedName("last_login")
    @Expose
    private Object lastLogin;
    @SerializedName("attendance_date")
    @Expose
    private String attendanceDate;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("student_class_name")
    @Expose
    private String studentClassName;
    private final static long serialVersionUID = -358247963753158293L;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getLoginId() {
        return loginId;
    }

    public void setLoginId(Integer loginId) {
        this.loginId = loginId;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public Object getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Object assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Object getCampusName() {
        return campusName;
    }

    public void setCampusName(Object campusName) {
        this.campusName = campusName;
    }

    public String getLandline() {
        return landline;
    }

    public void setLandline(String landline) {
        this.landline = landline;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(String studentClassId) {
        this.studentClassId = studentClassId;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLeftDate() {
        return leftDate;
    }

    public void setLeftDate(String leftDate) {
        this.leftDate = leftDate;
    }

    public String getLeftNote() {
        return leftNote;
    }

    public void setLeftNote(String leftNote) {
        this.leftNote = leftNote;
    }

    public Object getCv() {
        return cv;
    }

    public void setCv(Object cv) {
        this.cv = cv;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public Integer getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(Integer salaryType) {
        this.salaryType = salaryType;
    }

    public Integer getDailyLectures() {
        return dailyLectures;
    }

    public void setDailyLectures(Integer dailyLectures) {
        this.dailyLectures = dailyLectures;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Integer getIsInAttendance() {
        return isInAttendance;
    }

    public void setIsInAttendance(Integer isInAttendance) {
        this.isInAttendance = isInAttendance;
    }

    public Integer getStaffType() {
        return staffType;
    }

    public void setStaffType(Integer staffType) {
        this.staffType = staffType;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Object getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Object lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(String attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStudentClassName() {
        return studentClassName;
    }

    public void setStudentClassName(String studentClassName) {
        this.studentClassName = studentClassName;
    }

}
