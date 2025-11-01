package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExamResult {

    @SerializedName("unique_id")
    @Expose
    private String uniqueId;
    @SerializedName("registration_number")
    @Expose
    private Integer registrationNumber;
    @SerializedName("parent_parent_id")
    @Expose
    private String parentParentId;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("picture")
    @Expose
    private String picture;
    @SerializedName("dob")
    @Expose
    private String dob;
    @SerializedName("section_id")
    @Expose
    private String sectionId;
    @SerializedName("student_class_id")
    @Expose
    private String studentClassId;
    @SerializedName("roll_number")
    @Expose
    private Integer rollNumber;
    @SerializedName("study_group_id")
    @Expose
    private String studyGroupId;
    @SerializedName("parent_name")
    @Expose
    private String parentName;
    @SerializedName("parent_phone")
    @Expose
    private String parentPhone;
    @SerializedName("parent_landline")
    @Expose
    private String parentLandline;
    @SerializedName("parent_address")
    @Expose
    private String parentAddress;
    @SerializedName("class_name")
    @Expose
    private String className;
    @SerializedName("total_marks")
    @Expose
    private Object totalMarks;
    @SerializedName("created_date")
    @Expose
    private String createdDate;





    @SerializedName("result")
    @Expose
    private Result result;


    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Integer getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(Integer registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getParentParentId() {
        return parentParentId;
    }

    public void setParentParentId(String parentParentId) {
        this.parentParentId = parentParentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(String studentClassId) {
        this.studentClassId = studentClassId;
    }

    public Integer getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(Integer rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getStudyGroupId() {
        return studyGroupId;
    }

    public void setStudyGroupId(String studyGroupId) {
        this.studyGroupId = studyGroupId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getParentLandline() {
        return parentLandline;
    }

    public void setParentLandline(String parentLandline) {
        this.parentLandline = parentLandline;
    }

    public String getParentAddress() {
        return parentAddress;
    }

    public void setParentAddress(String parentAddress) {
        this.parentAddress = parentAddress;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Object totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }


}
