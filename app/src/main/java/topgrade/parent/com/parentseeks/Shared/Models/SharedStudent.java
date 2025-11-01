package topgrade.parent.com.parentseeks.Shared.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Shared Student model for use across Parent and Teacher modules
 * Consolidates duplicate Student classes to prevent conflicts
 */
public class SharedStudent {

    @SerializedName("unique_id")
    @Expose
    private String uniqueId;
    
    @SerializedName("full_name")
    @Expose
    private String fullName;
    
    @SerializedName(value = "roll_number", alternate = {"roll_no", "rollNumber", "roll", "student_roll_no", "student_roll_number", "studentRollNo", "studentRollNumber"})
    @Expose
    private String rollNo;
    
    @SerializedName("class_id")
    @Expose
    private String classId;
    
    @SerializedName("student_class_id")
    @Expose
    private String studentClassId;
    
    @SerializedName("class_name")
    @Expose
    private String className;
    
    @SerializedName("section_id")
    @Expose
    private String sectionId;
    
    @SerializedName("parent_id")
    @Expose
    private String parentId;
    
    @SerializedName("picture")
    @Expose
    private String picture;
    
    @SerializedName("is_active")
    @Expose
    private Integer isActive;
    
    @SerializedName("created_date")
    @Expose
    private String createdDate;
    
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    
    @SerializedName("dob")
    @Expose
    private String dob;
    
    @SerializedName("gender")
    @Expose
    private String gender;
    
    @SerializedName("parent_phone")
    @Expose
    private String parentPhone;
    
    @SerializedName("registration_number")
    @Expose
    private String registrationNumber;
    
    @SerializedName("form_submission_date")
    @Expose
    private String formSubmissionDate;
    
    @SerializedName("section_name")
    @Expose
    private String sectionName;
    
    @SerializedName("advance")
    @Expose
    private String advance;
    
    @SerializedName("parent_name")
    @Expose
    private String parentName;
    
    @SerializedName("subjects")
    @Expose
    private java.util.List<topgrade.parent.com.parentseeks.Parent.Model.Subject> subjects;

    public SharedStudent() {
    }

    public SharedStudent(String uniqueId, String fullName, String rollNo, String classId, 
                        String studentClassId, String className, String sectionId, String parentId, 
                        String picture, Integer isActive, String createdDate, String timestamp,
                        String dob, String gender, String parentPhone, String registrationNumber,
                        String formSubmissionDate, String sectionName, String advance, String parentName,
                        java.util.List<topgrade.parent.com.parentseeks.Parent.Model.Subject> subjects) {
        this.uniqueId = uniqueId;
        this.fullName = fullName;
        this.rollNo = rollNo;
        this.classId = classId;
        this.studentClassId = studentClassId;
        this.className = className;
        this.sectionId = sectionId;
        this.parentId = parentId;
        this.picture = picture;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.timestamp = timestamp;
        this.dob = dob;
        this.gender = gender;
        this.parentPhone = parentPhone;
        this.registrationNumber = registrationNumber;
        this.formSubmissionDate = formSubmissionDate;
        this.sectionName = sectionName;
        this.advance = advance;
        this.parentName = parentName;
        this.subjects = subjects;
    }

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

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(String studentClassId) {
        this.studentClassId = studentClassId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getFormSubmissionDate() {
        return formSubmissionDate;
    }

    public void setFormSubmissionDate(String formSubmissionDate) {
        this.formSubmissionDate = formSubmissionDate;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getAdvance() {
        return advance;
    }

    public void setAdvance(String advance) {
        this.advance = advance;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public java.util.List<topgrade.parent.com.parentseeks.Parent.Model.Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(java.util.List<topgrade.parent.com.parentseeks.Parent.Model.Subject> subjects) {
        this.subjects = subjects;
    }

    @Override
    public String toString() {
        return "SharedStudent{" +
                "uniqueId='" + uniqueId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", rollNo='" + rollNo + '\'' +
                ", classId='" + classId + '\'' +
                ", studentClassId='" + studentClassId + '\'' +
                ", className='" + className + '\'' +
                ", sectionId='" + sectionId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", picture='" + picture + '\'' +
                ", isActive=" + isActive +
                ", createdDate='" + createdDate + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", parentPhone='" + parentPhone + '\'' +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", formSubmissionDate='" + formSubmissionDate + '\'' +
                ", sectionName='" + sectionName + '\'' +
                ", advance='" + advance + '\'' +
                ", parentName='" + parentName + '\'' +
                ", subjects=" + subjects +
                '}';
    }
}
